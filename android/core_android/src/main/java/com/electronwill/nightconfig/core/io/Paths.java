package com.electronwill.nightconfig.core.io;

import java.io.File;

/**
 * Implements some functions of java.nio.file.Paths without the package java.nio.file,
 * in order to be compatible with Android &lt; 8.
 * <p>
 * Don't use this class in your application! It's only there for internal use.
 */
public class Paths {
	public static File get(String filePath) {
		return new File(filePath);
	}
}
