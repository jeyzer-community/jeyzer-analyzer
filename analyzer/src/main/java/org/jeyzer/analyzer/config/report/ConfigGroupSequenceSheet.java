package org.jeyzer.analyzer.config.report;

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

import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;


public class ConfigGroupSequenceSheet extends ConfigSequenceSheet {
	
	public ConfigGroupSequenceSheet(Element configNode, int index) throws JzrInitializationException {
		super(configNode, index);
	}

	public static final String TYPE = "group_sequence";
	
	public static final String DEFAULT_DESCRIPTION = "Displays the groups of stacks on a time line, along with the process info (thread count, CPU..).\n"
			+ "Each configured task sequence sheet permits to focus on specific topics : operations, locks, thread state, memory, gc..";
	
	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}
}
