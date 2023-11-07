package org.jeyzer.monitor.util;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Recorder
 * --
 * Copyright (C) 2020 - 2023 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtil {

	public static final String JZR_FILE_DATE_FORMAT = "yyyy-MM-dd---HH-mm-ss"; 
	
	private FileUtil(){
	}
	
	public static String getTimeStampedFileName(String prefix, Date dateStamp, String extension) {
		StringBuilder name = new StringBuilder(prefix);
		SimpleDateFormat sdf = new SimpleDateFormat(JZR_FILE_DATE_FORMAT);
		name.append(sdf.format(dateStamp));
		name.append(extension);
		return name.toString();
	}
	
}
