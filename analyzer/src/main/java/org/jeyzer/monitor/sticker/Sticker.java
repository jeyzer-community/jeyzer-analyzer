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

public abstract class Sticker {

	protected ConfigSticker stickerCfg;
	protected Boolean cachedValue = null;
	
	public Sticker(ConfigSticker stickerCfg) {
		this.stickerCfg = stickerCfg;
	}
	
	public ConfigSticker getConfigSticker(){
		return this.stickerCfg;
	}

	public abstract boolean match(JzrSession session, boolean negative);

}
