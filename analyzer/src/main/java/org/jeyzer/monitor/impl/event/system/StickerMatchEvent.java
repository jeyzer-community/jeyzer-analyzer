package org.jeyzer.monitor.impl.event.system;

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

import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class StickerMatchEvent extends MonitorSystemEvent {
	
	private List<String> stickers;
	
	public StickerMatchEvent(String eventName, List<String> stickers, MonitorEventInfo info) {
		super(eventName, info);
		this.stickers = stickers;
	}
	
	@Override
	public void updateContext(JzrSession session) {
		// nothing to store
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Stickers");
		params.add(buildStickersDisplayList());
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Stickers : " + buildStickersDisplayList());
	}

	private String buildStickersDisplayList() {
		boolean start = true;
		String result = null;
		for (String sticker : this.stickers) {
			result = (start ? sticker : (result + " , " + sticker));
			start = false;
		}
		return result;
	}
}
