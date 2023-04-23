package org.jeyzer.monitor.sticker;

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




import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.sticker.ConfigSticker;

public class AnalyzerSticker extends Sticker {

	public static final String STICKER_NAME = "analyzer sticker";
	
	public AnalyzerSticker(ConfigSticker stickerCfg) {
		super(stickerCfg);
	}

	@Override
	public boolean match(JzrSession session, boolean negative) {
		// Note that negative doesn't make really sense. 
		// Prefer removing the sticker reference from the ambient environment (usually defined within an environment variable)
		return negative ? false : true;
	}
}
