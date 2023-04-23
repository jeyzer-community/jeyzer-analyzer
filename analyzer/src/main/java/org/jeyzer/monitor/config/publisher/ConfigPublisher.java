package org.jeyzer.monitor.config.publisher;

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


import org.w3c.dom.Element;

public abstract class ConfigPublisher {

	private String name;
	
	public ConfigPublisher(String name, Element node){
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public abstract boolean isEnabled();
}
