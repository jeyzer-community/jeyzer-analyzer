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

import org.jeyzer.analyzer.config.setup.ConfigPoolMemorySetup;

public class MemoryPoolSetupManager {

	private List<String> youngPoolNames;
	private List<String> oldPoolNames;

	public MemoryPoolSetupManager(ConfigPoolMemorySetup memoryPoolSetupConfig) {
		this.youngPoolNames = memoryPoolSetupConfig.getYoungPoolNames();
		this.oldPoolNames = memoryPoolSetupConfig.getOldPoolNames();
	}
	public boolean isOldMemoryPool(String name){
		if (name == null)
			return false;
		for (String oldName : oldPoolNames){
			if (name.contains(oldName))
				return true;
		}
		return false;
	}
	
	public boolean isYoungMemoryPool(String name){
		if (name == null)
			return false;
		for (String youngName : youngPoolNames){
			if (name.contains(youngName))
				return true;
		}
		return false;
	}
	
}
