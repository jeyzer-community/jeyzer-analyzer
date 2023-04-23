package org.jeyzer.analyzer.parser.io;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */







import java.io.File;
import java.util.Comparator;
import java.util.Date;

public class ThreadDumpFileDateComparator implements Comparator<File> {

	private SnapshotFileNameFilter filter;
	
	public ThreadDumpFileDateComparator(SnapshotFileNameFilter filter){
		this.filter = filter;
	}
	
	@Override
	public int compare(File file1, File file2) {
		Date date1 = ThreadDumpFileDateHelper.getFileDate(filter, file1);
		Date date2 = ThreadDumpFileDateHelper.getFileDate(filter, file2);
		
        return date1.compareTo(date2);
	}

}
