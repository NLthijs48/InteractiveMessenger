package me.wiefferink.interactivemessenger.testing.ingame;

import me.wiefferink.interactivemessenger.testing.Log;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TestRun implements Listener {
	private Player tester;
	private Pattern testFilter;
	private List<TestCase> testCases;
	private int progress = 0;

	/**
	 * Constructor, setup and start the test run
	 * @param tester     The player that is going to test the messages
	 * @param testFilter Regex to filter the tests that should be shown
	 */
	public TestRun(Player tester, String testFilter) {
		this.tester = tester;
		if(testFilter == null) {
			testFilter = ".*";
		}
		this.testFilter = Pattern.compile(testFilter);
		info("Setting up test environment...");
		Bukkit.getServer().getPluginManager().registerEvents(this, IngameTest.getInstance());
		boolean started = saveResources()
				&& createTestCases()
				&& doNextTestCase();
		if(!started) {
			IngameTest.getInstance().finishTestRun();
		}
	}

	/**
	 * Stop the test run and cleanup
	 */
	public void exit() {
		HandlerList.unregisterAll(this);
		tester = null;
	}

	/**
	 * Get the player that is running the test
	 * @return The player running the test
	 */
	public Player getTester() {
		return tester;
	}

	/**
	 * Get the progress
	 * @return At which test the run is (can be 0 until getTestCases().size()-1)
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * Get the test cases defined for this test run
	 * @return The test cases
	 */
	public List<TestCase> getTestCases() {
		return testCases;
	}

	/**
	 * Show raw test run information
	 * @param message The message to print
	 */
	public void message(Object... message) {
		tester.sendMessage(StringUtils.join(message, " "));
		IngameTest.info(prefix(message));
	}

	/**
	 * Show test run information
	 * @param message The message to print
	 */
	public void info(Object... message) {
		tester.sendMessage(ChatColor.BLUE+""+ChatColor.BOLD+">>> "+StringUtils.join(message, " "));
		IngameTest.info(prefix(message));
	}


	/**
	 * Show test run information to the console
	 * @param message The message to print
	 */
	public void console(Object... message) {
		IngameTest.info(prefix(message));
	}

	/**
	 * Show test run error information
	 * @param message The message to print
	 */
	public void error(Object... message) {
		tester.sendMessage(ChatColor.DARK_RED+""+ChatColor.BOLD+">>> "+StringUtils.join(message, " "));
		IngameTest.error(prefix(message));
	}

	public Object[] prefix(Object... message) {
		Object[] result = new Object[message.length+1];
		System.arraycopy(message, 0, result, 1, message.length);
		result[0] = "[TestRun:"+tester.getName()+"]";
		return result;
	}

	/**
	 * Clean the visible screen (20 max visible, scrolling will still show old results)
	 */
	public void cleanScreen() {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+tester.getName()+" [\"\\n\\n\\n\\n\\n\\n\\n\\n\\n\\n\\n\\n\\n\\n\\n\\n\\n\\n\\n\\n\"]");
	}

	/**
	 * Save files used in the test to disk
	 */
	private boolean saveResources() {
		try {
			// Delete old files
			deleteFolderRecursive(new File(IngameTest.getInstance().getDataFolder()+File.separator+"resources"));
		} catch(IOException e) {
			error("Deleting old resources failed:", ExceptionUtils.getStackTrace(e));
			return false;
		}
		try {
			// Read jar as ZIP file
			File jarPath = new File(IngameTest.getInstance().getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
			ZipFile jar = new ZipFile(jarPath);
			Enumeration<? extends ZipEntry> entries = jar.entries();

			// Each entry is a file or directory
			while(entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();

				// Filter to YAML files in the language directory
				if(!entry.isDirectory() && entry.getName().startsWith("parsergenerator/") && entry.getName().endsWith(".txt")) {
					// Save the file to disk
					File targetFile = new File(IngameTest.resourcesFolder.getAbsolutePath()+
							File.separator+
							entry.getName());
					if(!targetFile.getParentFile().exists()) {
						if(!targetFile.getParentFile().mkdirs()) {
							error("Could not create resources folder:", targetFile.getParentFile().getAbsolutePath());
							continue; // Skip this one, keep trying the rest
						}
					}
					try(
							InputStream input = jar.getInputStream(entry);
							OutputStream output = new FileOutputStream(targetFile)
					) {
						int read;
						byte[] bytes = new byte[1024];
						while((read = input.read(bytes)) != -1) {
							output.write(bytes, 0, read);
						}
					} catch(IOException e) {
						error("Something went wrong saving a test resource file:", targetFile.getAbsolutePath());
					}
				}
			}
		} catch(URISyntaxException e) {
			error("Failed to find location of jar file:", ExceptionUtils.getStackTrace(e));
			return false;
		} catch(IOException e) {
			error("Failed to read zip file:", ExceptionUtils.getStackTrace(e));
			return false;
		}
		return true;
	}

	/**
	 * Deletes Folder with all of its content
	 * @param folder path to folder which should be deleted
	 */
	private void deleteFolderRecursive(File folder) throws IOException {
		if(!folder.exists()) {
			return;
		}

		Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if(exc != null) {
					throw exc;
				}
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Setup test cases based on files on disk
	 */
	private boolean createTestCases() {
		testCases = new ArrayList<>();

		// Recursive lookup of files in the resources folder
		addTestCases(testCases, IngameTest.resourcesFolder);
		Log.info("added test cases:", testCases);
		return true;
	}

	private void addTestCases(List<TestCase> list, File folder) {
		File[] files = folder.listFiles();
		if(files == null) {
			return;
		}

		for(File file : files) {
			if(file.isFile()) {
				list.add(new TestCase(this, file));
			} else if(file.isDirectory()) {
				addTestCases(list, file);
			}
		}
	}

	/**
	 * Do next test case
	 */
	public boolean doNextTestCase() {
		// Either no testcases or something wrong
		if(testCases == null || testCases.size() == 0) {
			tester.sendMessage("There are no test cases defined, check if there are any in the folder: "+IngameTest.resourcesFolder.getAbsolutePath());
			return false;
		}

		while(true) {
			// Done, print result
			if(progress >= testCases.size()) {
				printResults();
				return true;
			}

			TestCase next = testCases.get(progress);

			// Filter test cases
			if(testFilter.matcher(next.getName()).matches()) {
				next.show();
				return true;
			} else {
				progress++;
			}
		}
	}

	/**
	 * Confirm the result of the running test case and do the next
	 * @param result Result of the current test case
	 */
	public void result(TestCase.TestResult result) {
		testCases.get(progress).result(result);
		progress++;
		doNextTestCase();
	}

	/**
	 * Print the results of a test run
	 */
	private void printResults() {
		cleanScreen();

		// Collect results
		SortedSet<TestCase> success = new TreeSet<>();
		SortedSet<TestCase> skipped = new TreeSet<>();
		SortedSet<TestCase> failed = new TreeSet<>();
		for(TestCase testCase : testCases) {
			if(testCase.getResult() == TestCase.TestResult.SUCCESS) {
				success.add(testCase);
			} else if(testCase.getResult() == TestCase.TestResult.FAILED) {
				failed.add(testCase);
			} else {
				skipped.add(testCase);
			}
		}

		// Print report
		info("Testing complete, result:");
		message("Test count: "+testCases.size());

		if(!success.isEmpty()) {
			message(ChatColor.GREEN+"Success: "+success.size()+" test"+(success.size() != 1 ? "s" : ""));
		}

		if(!skipped.isEmpty()) {
			message("Skipped: "+skipped.size()+" test"+(skipped.size() != 1 ? "s" : ""));
			for(TestCase testCase : skipped) {
				message("  "+testCase.getName());
			}
		}

		if(!failed.isEmpty()) {
			message(ChatColor.RED+"Failed: "+failed.size()+" test"+(failed.size() != 1 ? "s" : ""));
			for(TestCase testCase : failed) {
				message("  "+testCase.getName());
			}
		}

		IngameTest.getInstance().finishTestRun();
	}
}
