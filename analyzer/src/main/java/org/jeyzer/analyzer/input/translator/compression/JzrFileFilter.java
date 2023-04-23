package org.jeyzer.analyzer.input.translator.compression;

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
import java.io.FileFilter;

public class JzrFileFilter implements FileFilter {
	
	@Override
	public boolean accept(File f) {
    	// exclude compressed files
    	if (f.getName().endsWith(".zip"))
    		return false;
    	if (f.getName().endsWith(".gz"))
    		return false;
    	if (f.getName().endsWith(".tar"))
    		return false;
    	// exclude report files
    	if (f.getName().endsWith(".xlsx"))
    		return false; 
        return f.isFile();
	}

}
