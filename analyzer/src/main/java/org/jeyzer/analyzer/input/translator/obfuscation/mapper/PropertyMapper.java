package org.jeyzer.analyzer.input.translator.obfuscation.mapper;

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

public interface PropertyMapper {

	public static final String PROPERTY_TOKEN = "@@";	
	
	public void setNextMapper(PropertyMapper nextMapper);
	
	public boolean isValid();
	
	public void resolveProperties(String templatePath, List<String> configPaths);
}
