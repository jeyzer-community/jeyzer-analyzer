package org.jeyzer.analyzer.input.translator.jfr.mapper;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jeyzer.analyzer.input.translator.jfr.mapper.data.JFRModuleDescriptor;
import org.jeyzer.analyzer.input.translator.jfr.reader.JFRDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdk.jfr.consumer.RecordedClassLoader;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedObject;

public class JZRProcessModuleMapper {
	
	private static final Logger logger = LoggerFactory.getLogger(JZRProcessModuleMapper.class);
	
	public static final String EXPORTED_PACKAGE = "exportedPackage";
	public static final String MODULE = "module";
	public static final String MODULE_NAME = "name";
	public static final String MODULE_VERSION = "version";
	public static final String MODULE_CLASSLOADER = "classLoader";
	
	public static final String SOURCE_MODULE = "source";
	public static final String REQUIRED_MODULE = "requiredModule";

	public void mapModules(JFRDescriptor descriptor, PrintWriter writer) {
		Map<String, JFRModuleDescriptor> modules = new HashMap<>();
		
		// Event reference : https://bestsolution-at.github.io/jfr-doc/openjdk-15.html
		
		// Create first the export
		for (RecordedEvent event : descriptor.getModuleExportEvents())
			createJavaModuleDescriptor(modules, event);
		
		// Add the requires
		for (RecordedEvent event : descriptor.getModuleRequireEvents())
			addRequires(modules, event);

		for (Entry<String, JFRModuleDescriptor> entry : modules.entrySet()) {
			JFRModuleDescriptor desc = entry.getValue();
			desc.print(writer);
		}
	}

	private void createJavaModuleDescriptor(Map<String, JFRModuleDescriptor> modules, RecordedEvent event) {
		RecordedObject exportedPackageEvent = event.getValue(EXPORTED_PACKAGE);
		RecordedObject moduleEvent = exportedPackageEvent.getValue(MODULE);
		if (moduleEvent == null)
			return;
		String name = moduleEvent.getString(MODULE_NAME);
		
		// Under Linux, the event module name can be empty for the com/sun/proxy package
		if (name == null)
			return;

		JFRModuleDescriptor desc = modules.computeIfAbsent(
				name,
				x-> new JFRModuleDescriptor(
						name,
						moduleEvent.getString(MODULE_VERSION),
						moduleEvent.getValue(MODULE_CLASSLOADER)
						)
				);
		String exported = exportedPackageEvent.getString(MODULE_NAME);
		desc.addExport(exported);
	}

	private void addRequires(Map<String, JFRModuleDescriptor> modules, RecordedEvent event) {
		RecordedObject sourceModuleEvent = event.getValue(SOURCE_MODULE);
		String name = sourceModuleEvent.getString(MODULE_NAME);
		JFRModuleDescriptor desc = modules.get(name);
		if (desc != null) {
			addRequiredModule(desc, event);
		}
		else {
			String version = sourceModuleEvent.getString(MODULE_VERSION);
			RecordedClassLoader classLoader = sourceModuleEvent.getValue(MODULE_CLASSLOADER);
			logger.warn("Unexpected case : the Java module is not exported but exposed as required : " + name + "/" + version);
			desc = new JFRModuleDescriptor(name, version, classLoader);
			modules.put(name, desc);
			addRequiredModule(desc, event);
		}		
	}

	private void addRequiredModule(JFRModuleDescriptor desc, RecordedObject event) {
		RecordedObject requiredModuleEvent = event.getValue(REQUIRED_MODULE);
		String requiredName = requiredModuleEvent.getString(MODULE_NAME);
		if (requiredName != null)
			desc.addRequire(requiredName);			
	}
}
