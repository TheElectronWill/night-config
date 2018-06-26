package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.utils.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Utility class for detecting the format of configurations files.
 *
 * @author TheElectronWill
 */
public final class FormatDetector {
	private static final Map<String, Supplier<ConfigFormat<?>>> registry = new ConcurrentHashMap<>();

	/**
	 * Registers a ConfigFormat for a specific fileExtension.
	 *
	 * @param fileExtension the file extension
	 * @param format        the config format
	 */
	public static void registerExtension(String fileExtension, ConfigFormat<?> format) {
		registry.put(fileExtension, () -> format);
	}

	/**
	 * Registers a ConfigFormat's supplier for a specific fileExtension.
	 *
	 * @param fileExtension  the file extension
	 * @param formatSupplier the Supplier of the config format
	 */
	public static void registerExtension(String fileExtension,
										 Supplier<ConfigFormat<?>> formatSupplier) {
		registry.put(fileExtension, formatSupplier);
	}

	/**
	 * Detects the ConfigFormat of a file.
	 *
	 * @param file the file
	 * @return the associated ConfigFormat, or null if not found
	 */
	public static ConfigFormat<?> detect(File file) {
		return detectByName(file.getName());
	}

	public static ConfigFormat<?> detect(Path path) {
		return detectByName(path.getFileName().toString());
	}
	/**
	 * Detects the ConfigFormat of a filename.
	 *
	 * @param fileName the file name
	 * @return the associated ConfigFormat, or null if not found
	 */
	public static ConfigFormat<?> detectByName(String fileName) {
		List<String> splitted = StringUtils.split(fileName, '.');
		String fileExtension = splitted.get(splitted.size() - 1);//the last part
		Supplier<ConfigFormat<?>> supplier = registry.get(fileExtension);
		return (supplier == null) ? null : supplier.get();
	}

	// Automatic registration of the officialy supported formats
	// Custom formats must be loaded by the user
	static {
		tryLoad("com.electronwill.nightconfig.toml.TomlFormat");
		tryLoad("com.electronwill.nightconfig.hocon.HoconFormat");
		tryLoad("com.electronwill.nightconfig.json.JsonFormat");
		tryLoad("com.electronwill.nightconfig.yaml.YamlFormat");
	}

	private static void tryLoad(String className) {
		try {
			Class.forName(className);
		} catch (ClassNotFoundException e) {
			//ignore: the class is not found, that's not a problem
		}
	}

	private FormatDetector() {}// Utility class cannot be constructed
}