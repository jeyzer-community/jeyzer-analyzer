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
import org.jeyzer.monitor.impl.event.system.ProcessJarVersionEvent;

public class ProcessJarVersionRule extends MonitorSystemRule implements SignalSystemProvider{

	public static final String RULE_NAME = "Process jar version";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to check the presence of a library version "
			+ "and provide a message about it like an End Of Life warning, or a security issue requiring an immediate library upgrade."
			+ "This rule must get associated at least to one process jar version sticker. It has no check body and relies only on the sticker(s). "
			+ "It is quite similar to the Sticker match rule by nature."
			+ "Associated sticker(s) should have a strict appliance (which is the default).";
	
	private String extraInfo;
	
	public ProcessJarVersionRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + def.getExtraInfo(),
				def.getStickerRefs() + (def.getStickerRefs().size() > 1 ?  " stickers are matching." : " sticker is matching."));
		if (def.getStickerRefs().isEmpty())
			throw new JzrInitializationException(RULE_NAME + " is invalid : it must have at least one sticker.");
		this.extraInfo = def.getExtraInfo();
	}
	
	@Override
	public boolean matchSignal(JzrSession session) {
		return true;
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return true;
	}

	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SYSTEM_SIGNAL);
	}

	@Override
	public MonitorSystemEvent createSystemEvent(MonitorEventInfo info) {
		return new ProcessJarVersionEvent(this.extraInfo, info);
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
