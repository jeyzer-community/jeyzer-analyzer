package org.jeyzer.analyzer.data.tag;

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







public class ATBITag extends Tag{

	public static final String DISPLAY_NAME = "ATBI";	
	
	public ATBITag(String name) {
		super(name);
	}

	@Override
	public String getTypeName() {
		return DISPLAY_NAME;
	}
	
}
