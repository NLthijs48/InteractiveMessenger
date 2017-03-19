package me.wiefferink.interactivemessenger.source;

import com.google.common.base.Charsets;
import me.wiefferink.interactivemessenger.processing.Message;
import me.wiefferink.interactivemessenger.translation.Transifex;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LanguageManager implements MessageProvider {
	private JavaPlugin plugin;
	private Map<String, List<String>> currentLanguage, defaultLanguage;
	private File languageFolder;
	private String jarLanguagePath;
	private List<String> chatPrefix;

	/**
	 * Constructor
	 * @param plugin The plugin creating this LanguageManager (used for logging and finding the language files in the jar)
	 * @param jarLanguagePath The path in the jar to the folder with the language files
	 * @param currentLanguageName The name of the language that should be active (without '.yml')
	 * @param defaultLanguageName The name of the language that
	 * @param chatPrefix The chat prefix for Message#prefix()
	 */
	public LanguageManager(JavaPlugin plugin, String jarLanguagePath, String currentLanguageName, String defaultLanguageName, List<String> chatPrefix) {
		this.plugin = plugin;
		this.jarLanguagePath = jarLanguagePath;
		this.chatPrefix = chatPrefix;
		this.languageFolder = new File(plugin.getDataFolder() + File.separator + jarLanguagePath);

		Message.init(this, plugin.getLogger());
		saveDefaults();
		currentLanguage = loadLanguage(currentLanguageName);
		if(defaultLanguageName.equals(currentLanguageName)) {
			defaultLanguage = currentLanguage;
		} else {
			defaultLanguage = loadLanguage(defaultLanguageName);
		}
	}

	/**
	 * Get the message for a certain key (result can be modified)
	 * @param key The key of the message to get
	 * @return The message as a list of strings
	 */
	@Override
	public List<String> getMessage(String key) {
		List<String> message;
		if(key.equalsIgnoreCase(Message.CHATLANGUAGEVARIABLE)) {
			message = chatPrefix;
		} else if(currentLanguage.containsKey(key)) {
			message = currentLanguage.get(key);
		} else {
			message = defaultLanguage.get(key);
		}
		if(message == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(message);
	}

	/**
	 * Saves the default language files if not already present
	 */
	private void saveDefaults() {
		// Create the language folder if it not exists
		File langFolder;
		if(!languageFolder.exists()) {
			if(!languageFolder.mkdirs()) {
				Message.warn("Could not create language directory: "+languageFolder.getAbsolutePath());
				return;
			}
		}

		try {
			// Read jar as ZIP file
			File jarPath = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
			ZipFile jar = new ZipFile(jarPath);
			Enumeration<? extends ZipEntry> entries = jar.entries();

			// Each entry is a file or directory
			while(entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();

				// Filter to YAML files in the language directory
				if(!entry.isDirectory() && entry.getName().startsWith(jarLanguagePath+"/") && entry.getName().endsWith(".yml")) {
					// Save the file to disk
					File targetFile = new File(languageFolder.getAbsolutePath() + File.separator + entry.getName().substring(entry.getName().lastIndexOf("/")));
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
						Message.warn("Something went wrong saving a default language file: "+targetFile.getAbsolutePath());
					}
				}
			}
		} catch(URISyntaxException e) {
			Message.error("Failed to find location of jar file:", ExceptionUtils.getStackTrace(e));
		} catch(IOException e) {
			Message.error("Failed to read zip file:", ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Loads the specified language
	 * @param key The language to load
	 * @return Map with the messages loaded from the file
	 */
	private Map<String, List<String>> loadLanguage(String key) {
		return loadLanguage(key, true);
	}

	/**
	 * Loads the specified language
	 * @param key	 The language to load
	 * @param convert try conversion or not (infinite recursion prevention)
	 * @return Map with the messages loaded from the file
	 */
	private Map<String, List<String>> loadLanguage(String key, boolean convert) {
		Map<String, List<String>> result = new HashMap<>();

		// Load the language file
		boolean convertFromTransifex = false;
		File file = new File(languageFolder.getAbsolutePath()+File.separator+key+".yml");
		try(
				InputStreamReader reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8)
		) {
			// Detect empty language files, happens when the YAML parsers prints an exception (it does return an empty YamlConfiguration though)
			YamlConfiguration ymlFile = YamlConfiguration.loadConfiguration(reader);
			if(ymlFile.getKeys(false).isEmpty()) {
				Message.warn("Language file "+key+".yml has zero messages.");
				return result;
			}

			// Retrieve the messages from the YAML file and create the result
			if(!convert || !Transifex.needsConversion(ymlFile)) {
				for(String messageKey : ymlFile.getKeys(false)) {
					if(ymlFile.isList(messageKey)) {
						result.put(messageKey, new ArrayList<>(ymlFile.getStringList(messageKey)));
					} else {
						result.put(messageKey, new ArrayList<>(Collections.singletonList(ymlFile.getString(messageKey))));
					}
				}
			} else {
				convertFromTransifex = true;
			}
		} catch(IOException e) {
			Message.warn("Could not load language file: "+file.getAbsolutePath());
		}

		// Do conversion (after block above closed the reader)
		if(convertFromTransifex) {
			if(!Transifex.convertFrom(file)) {
				Message.warn("Failed to convert "+file.getName()+" from the Transifex layout to the AreaShop layout, check the errors above");
			}
			return loadLanguage(key, false);
		}

		return result;
	}

}

































