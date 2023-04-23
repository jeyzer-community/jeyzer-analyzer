package org.jeyzer.analyzer.input.translator.jfr.mapper.data;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 - 2021 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import jdk.jfr.consumer.RecordedClassLoader;

public class JFRModuleDescriptor {

	private String name;
	private String version; // can be null
	private List<String> exports = new ArrayList<>();
	private List<String> requires = new ArrayList<>();
	private String classLoader; // can be null
	
	public JFRModuleDescriptor(String name, String version, RecordedClassLoader classLoader) {
		this.name = name;
		this.version = version;
		this.classLoader = classLoader != null ? classLoader.getType() != null ? classLoader.getName() : null : null;
	}
	
	public void addRequire(String module){
		this.requires.add(module);
	}
	
	public void addExport(String module){
		this.exports.add(module);
	}

	public void print(PrintWriter writer) {
		writer.print(this.name);
		writer.write(';');
		if (this.version != null)
			writer.write(this.version);
		
		writer.write(";false;false;"); // open and automatic fields are not available in JFR
		
		// requires
		String delimitor = "";
		for (String req : this.requires) {
			writer.write(delimitor);
			writer.write(req);
			delimitor = ",";
		}
		writer.write(';');

		// exports
		delimitor = "";
		for (String export : this.exports) {
			writer.write(delimitor);
			writer.write(export);
			delimitor = ",";
		}

		writer.write(";;;"); // uses and provides fields are not available in JFR
		
		// class loader
		if (classLoader != null)
			writer.write(classLoader);
		
		writer.write(System.lineSeparator());
	}
}
