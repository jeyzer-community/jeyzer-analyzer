package org.jeyzer.analyzer.util;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 - 2024 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

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
