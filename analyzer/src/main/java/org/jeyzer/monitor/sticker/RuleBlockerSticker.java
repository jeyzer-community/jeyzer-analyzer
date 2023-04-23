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

public class RuleBlockerSticker extends Sticker {

	public static final String STICKER_PREFIX = "rule-block-";	
	
	public static final String STICKER_NAME = "rule blocker sticker";
	
	public RuleBlockerSticker(ConfigSticker stickerCfg) {
		super(stickerCfg);
	}

	@Override
	public boolean match(JzrSession session, boolean negative) {
		// will never be used
		return negative ? false : true;
	}
}
