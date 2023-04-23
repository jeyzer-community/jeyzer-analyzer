package org.jeyzer.monitor.engine.rule.condition.system;

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







import java.util.regex.Pattern;

import org.jeyzer.analyzer.session.JzrSession;

public interface PatternSystemProvider {

	public boolean matchPattern(JzrSession session, Pattern pattern);	
	
}
