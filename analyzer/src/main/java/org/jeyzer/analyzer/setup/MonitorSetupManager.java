package org.jeyzer.analyzer.setup;

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
import java.util.Map;

import org.jeyzer.analyzer.config.setup.ConfigMonitorSetup;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.sticker.Sticker;
import org.jeyzer.monitor.sticker.StickerBuilder;
import org.jeyzer.service.location.JzrLocationResolver;

public class MonitorSetupManager {

	private StickerBuilder stickerBuilder;
	
	public MonitorSetupManager(ConfigMonitorSetup monitorSetupConfig) {
		stickerBuilder = new StickerBuilder(monitorSetupConfig.getConfigStickers());
	}

	public Map<String, Sticker> getStickers(JzrSession session, JzrLocationResolver jzrLocationResolver) {
		stickerBuilder.loadDynamicStickers(session, jzrLocationResolver);
		return stickerBuilder.getStickers();
	}

	public List<Sticker> getStickerList(JzrSession session, JzrLocationResolver jzrLocationResolver) {
		stickerBuilder.loadDynamicStickers(session, jzrLocationResolver);
		return stickerBuilder.getStickerList();
	}
}
