package me.wiefferink.interactivemessenger.translation;

import me.wiefferink.interactivemessenger.processing.Message;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

/**
 * Convert to and from a format suitable for Transifex (online collaborative translation platform)
 */
public class Transifex {

	/**
	 * Test if a YamlConfiguration is downloaded from Transifex and needs to be converted
	 * @param configuration The configuration to check
	 * @return true if the file needs to be converted, otherwise false
	 */
	public static boolean needsConversion(YamlConfiguration configuration) {
		// Detect language files downloaded from Transifex and convert them
		if(configuration.getKeys(false).size() == 1) {
			for(String languageKey : configuration.getKeys(false)) {
				if(configuration.isConfigurationSection(languageKey)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Convert a language file downloaded from Transifex into a proper format
	 * @param inputFile The input file to
	 * @return true if the conversion was successful, otherwise false
	 */
	public static boolean convertFrom(File inputFile) {
		if(inputFile == null || !inputFile.exists() || !inputFile.isFile()) {
			warn("Could not convert Transifix file, it does not exist");
			return false;
		}
		Message.info("Transifex conversion of", inputFile.getAbsolutePath());

		File newFile = new File(inputFile.getAbsolutePath()+".new");
		try(
				BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				BufferedWriter writer = new BufferedWriter(new FileWriter(newFile))
		) {
			// Create target file
			newFile.createNewFile();

			// Add translation strings
			String line = reader.readLine();
			boolean first = true;
			String lastLineStart = null;
			while(line != null) {
				// Skip line with the language on it
				if(first) {
					first = false;
					line = reader.readLine();
					continue;
				}
				// Skip comment lines (normally there are none)
				if(line.startsWith("#")) {
					line = reader.readLine();
					continue;
				}
				// Strip whitespace at the front
				if(line.length() > 2) {
					line = line.substring(2);
				}
				// Add empty lines between messages of a different type (the part before - decides the type)
				if(lastLineStart != null && !line.trim().isEmpty() && !line.startsWith(lastLineStart) && !line.trim().startsWith("-")) {
					writer.newLine();
				}
				// Write line to the new file
				if(!line.trim().isEmpty()) {
					// Detect special fancyformat lines
					if(line.contains("\\n")) {
						String[] specials = line.split("\\\\n");
						//print("Contains newline: "+line+", split into "+specials.length+" parts");
						// Write identifier
						writer.write(specials[0].substring(0, specials[0].indexOf(":")+1));
						writer.newLine();
						// Strip identifier from first part
						specials[0] = specials[0].substring(specials[0].indexOf(":")+3);
						// Write special lines
						for(String special : specials) {
							writer.write("  - \"");
							if(special.startsWith("\\t")) {
								special = special.substring(2);
								writer.write("	");
							}
							writer.write(special);
							if(!special.endsWith("\"")) {
								writer.write("\"");
							}
							writer.newLine();
						}
					} else {
						// Simply copy the line
						writer.write(line);
						writer.newLine();
					}
				}
				// Update last line start
				String[] splittedLine = line.split("-");
				if(splittedLine.length > 0 && !line.trim().isEmpty()) {
					lastLineStart = splittedLine[0];
				}
				line = reader.readLine();
			}
		} catch(IOException e) {
			warn("Could not convert Transifix file:", ExceptionUtils.getStackTrace(e));
			newFile.delete();
			return false;
		}

		// Move current file to .old, move new file to original location
		File oldFile = new File(inputFile.getAbsolutePath()+".transifex");
		if(oldFile.exists() && oldFile.isFile()) {
			if(!oldFile.delete()) {
				warn("Could not remove existing old file:", oldFile.getAbsolutePath());
				return false;
			}
		}
		if(!inputFile.renameTo(oldFile)) {
			warn("Could not move input file to .old, from", inputFile.getAbsolutePath(), "to", oldFile.getAbsolutePath());
			return false;
		}
		if(!newFile.renameTo(inputFile)) {
			warn("Could not move new file to original file location, from", newFile.getAbsolutePath(), "to", inputFile.getAbsolutePath());
			oldFile.renameTo(inputFile); // Try to recover original file
			return false;
		}
		Message.info("Converted "+inputFile.getName()+" from the Transifex layout to the AreaShop layout, original saved in "+oldFile.getName());
		return true;
	}

	/**
	 * Print a warning to the console
	 * @param message The message to print
	 */
	private static void warn(String... message) {
		Message.warn("[Transifex language file conversion]", StringUtils.join(message, " "));
	}
}
