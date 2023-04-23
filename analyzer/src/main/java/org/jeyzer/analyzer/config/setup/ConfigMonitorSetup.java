package org.jeyzer.analyzer.config.setup;

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




import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.sticker.ConfigStickers;
import org.w3c.dom.Element;

public class ConfigMonitorSetup {

	private ConfigStickers stickers;
	
	public ConfigMonitorSetup(Element monitorSetupsNode) throws JzrInitializationException {
		Element stickersNode = ConfigUtil.getFirstChildNode(monitorSetupsNode, ConfigStickers.JZRM_STICKERS);
		this.stickers = new ConfigStickers(stickersNode, null, true);
	}
	
	public ConfigStickers getConfigStickers(){
		return this.stickers;
	}
}
