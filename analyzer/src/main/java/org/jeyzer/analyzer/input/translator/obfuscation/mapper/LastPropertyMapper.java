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

public class LastPropertyMapper implements PropertyMapper {

	@Override
	public void setNextMapper(PropertyMapper nextMapper) {
		// last one by contract
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void resolveProperties(String templatePath, List<String> configPaths) {
		if (templatePath != null && !templatePath.contains(PROPERTY_TOKEN))
			configPaths.add(templatePath);
	}

}
