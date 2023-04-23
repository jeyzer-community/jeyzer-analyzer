package org.jeyzer.analyzer.input.translator;

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




import java.util.Date;

import org.jeyzer.analyzer.config.translator.ConfigTranslator;
import org.jeyzer.analyzer.error.JzrTranslatorException;
import org.jeyzer.analyzer.parser.io.SnapshotFileNameFilter;
import org.jeyzer.analyzer.status.JeyzerStatusEvent;

public interface Translator {
	
	public TranslateData translate(TranslateData input, SnapshotFileNameFilter filter, Date sinceDate) throws JzrTranslatorException;
	
	public JeyzerStatusEvent.STATE getStatusEventState();
	
	public short getPriority();
	
	public ConfigTranslator getConfiguration();

	public boolean isEnabled();	
	
	public void close();

}
