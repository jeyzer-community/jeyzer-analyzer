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

import org.jeyzer.analyzer.data.tag.ContentionTypeTag;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigParamMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.system.SignalSystemProvider;
import org.jeyzer.monitor.impl.event.system.ContentionTypePresenceEvent;

import com.google.common.collect.Multiset;

public class ContentionTypePresenceRule extends MonitorSystemRule implements SignalSystemProvider{

	public static final String RULE_NAME = "Contention type presence";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect a particular contention type."
			+ "Exotic or custom contention types should be detected with this rule.";
	
	private String contentionType;
	private String paramDisplay;
	
	public ContentionTypePresenceRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + ((ConfigParamMonitorRule)def).getDisplayName(), 
				((ConfigParamMonitorRule)def).getDisplayName() + " contention type is detected.");
		ConfigParamMonitorRule mxDef = (ConfigParamMonitorRule) def;
		this.contentionType = mxDef.getParamName();
		this.paramDisplay = mxDef.getDisplayName();
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
	public boolean matchSignal(JzrSession session) {
		Multiset<Tag> contentionTypeTags = session.getContentionTypeSet();
		Tag contentionTypeTag = new ContentionTypeTag(contentionType);
    	return contentionTypeTags.count(contentionTypeTag) > 0;
	}

	@Override
	public MonitorSystemEvent createSystemEvent(MonitorEventInfo info) {
		return new ContentionTypePresenceEvent(
				this.paramDisplay,
				info,
				this.contentionType
			);
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
