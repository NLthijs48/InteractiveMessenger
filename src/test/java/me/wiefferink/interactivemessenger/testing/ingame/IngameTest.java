package me.wiefferink.interactivemessenger.testing.ingame;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class IngameTest extends JavaPlugin {

	public static File resourcesFolder;

	private TestRun testRun;
	private static IngameTest instance;

	@Override
	public void onEnable() {
		instance = this;
		resourcesFolder = new File(IngameTest.getInstance().getDataFolder().getAbsolutePath()+File.separator+"resources");
	}

	public static IngameTest getInstance() {
		return instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission("interactivemessenger.test")) {
			sender.sendMessage("You don't have permission to test the InteractiveMessenger library.");
			return true;
		}

		if(args.length == 0) {
			sender.sendMessage("/tim <start|stop|correct|wrong|continue>.");
			return true;
		}

		// TODO console test version with ConsoleGenerator?
		if(!(sender instanceof Player)) {
			sender.sendMessage("Only players can run this test.");
			return true;
		}

		Player player = (Player)sender;

		// Start test run
		if("start".equalsIgnoreCase(args[0])) {
			if(testRun != null) {
				sender.sendMessage("There is already a test running by "+testRun.getTester().getName()+".");
				return true;
			}

			String filter = null;
			if(args.length > 1) {
				filter = args[1];
			}
			testRun = new TestRun(player, filter);
		}

		// Set result of the case
		else {
			if(testRun == null) {
				sender.sendMessage("There is no active test run, start with '/tim start'.");
				return true;
			}

			if(!testRun.getTester().equals(player)) {
				sender.sendMessage(testRun.getTester().getName()+" is doing a test run, wait for it to be complete.");
				return true;
			}

			// Stop the test run
			if("stop".equalsIgnoreCase(args[0])) {
				sender.sendMessage("Stopped test run");
				finishTestRun();
			}

			// Continue to the next case
			else if("continue".equalsIgnoreCase(args[0])) {
				testRun.doNextTestCase();
			}

			// Confirm test case as correct
			else if("correct".equalsIgnoreCase(args[0])) {
				testRun.result(TestCase.TestResult.SUCCESS);
			}

			// Confirm test case as wrong
			else if("wrong".equalsIgnoreCase(args[0])) {
				testRun.result(TestCase.TestResult.FAILED);
			}

		}
		return true;
	}

	/**
	 * Cleanup the current test run
	 */
	public void finishTestRun() {
		testRun = null;
	}

	/**
	 * Print information to the console
	 * @param message The message to print
	 */
	public static void info(Object... message) {
		instance.getLogger().info(ChatColor.stripColor(StringUtils.join(message, " ")));
	}

	/**
	 * Print a warning to the console
	 * @param message The message to print
	 */
	public static void warn(Object... message) {
		instance.getLogger().warning(ChatColor.stripColor(StringUtils.join(message, " ")));
	}

	/**
	 * Print an error to the console
	 * @param message The messagfe to print
	 */
	public static void error(Object... message) {
		instance.getLogger().severe(ChatColor.stripColor(StringUtils.join(message, " ")));
	}

}
