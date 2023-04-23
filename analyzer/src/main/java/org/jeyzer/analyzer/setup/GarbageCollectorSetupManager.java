package org.jeyzer.analyzer.setup;

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







import java.util.List;

import org.jeyzer.analyzer.config.setup.ConfigGarbageCollectorSetup;

public class GarbageCollectorSetupManager {

	private List<String> youngGCNames;
	private List<String> oldGCNames;
	
	public GarbageCollectorSetupManager(ConfigGarbageCollectorSetup garbageCollectorSetupConfig) {
		youngGCNames = garbageCollectorSetupConfig.getYoungGCNames();
		oldGCNames = garbageCollectorSetupConfig.getOldGCNames();
	}

	public boolean isOldGarbageCollector(String name){
		if (name == null)
			return false;
		for (String oldName : oldGCNames){
			if (name.contains(oldName))
				return true;
		}
		return false;
	}
	
	public boolean isYoungGarbageCollector(String name){
		if (name == null)
			return false;
		for (String youngName : youngGCNames){
			if (name.contains(youngName))
				return true;
		}
		return false;
	}	
	
}
