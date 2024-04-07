package org.jeyzer.analyzer.util;

import java.io.File;

public class JFRHelper {

	public static boolean isJFRFile(String filename) {
		return filename != null && filename.toLowerCase().endsWith(JFRHelper.JFR_EXTENSION);
	}

	public static boolean detectJFRFile(File[] files) {
		for (File file : files)
			if (isJFRFile(file.getName()))
				return true;
		return false;
	}

	public static final String JFR_EXTENSION = ".jfr";

}
