package org.jeyzer.monitor.impl.rule.system;

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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SYSTEM_SIGNAL;




import java.util.Arrays;
import java.util.List;

import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.system.SignalSystemProvider;
import org.jeyzer.monitor.impl.event.system.StickerMatchEvent;

public class StickerMatchRule extends MonitorSystemRule implements SignalSystemProvider{

	public static final String RULE_NAME = "Sticker match";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect the sticker matching. "
			+ "This rule must get associated to one or several stickers. It has no check body and relies only on the stickers. "
			+ "Process card property rules are quite similar, but stickers can also be ambient based and it prevents anyway from duplicating the sticker property conditions in the monitoring rules."
			+ "Associated stickers should have a strict appliance (which is the default).";
	
	private String extraInfo;
	private List<String> stickers;
	
	public StickerMatchRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + def.getExtraInfo(),
				def.getStickerRefs() + (def.getStickerRefs().size() > 1 ?  " stickers are matching." : " sticker is matching."));
		if (def.getStickerRefs().isEmpty())
			throw new JzrInitializationException(RULE_NAME + " rule is invalid : it must have at least one sticker.");
		this.extraInfo = def.getExtraInfo();
		this.stickers = def.getStickerRefs();
	}
	
	@Override
	public boolean matchSignal(JzrSession session) {
		return true;
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return false;
	}

	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SYSTEM_SIGNAL);
	}

	@Override
	public MonitorSystemEvent createSystemEvent(MonitorEventInfo info) {
		return new StickerMatchEvent(this.extraInfo, this.stickers, info);
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_VERY_LOW;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
}
