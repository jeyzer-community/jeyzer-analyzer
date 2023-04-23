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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SYSTEM_PATTERN;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.system.PatternSystemProvider;
import org.jeyzer.monitor.impl.event.system.QuietActivityEvent;

import com.google.common.collect.Multiset;

public class QuietActivityRule extends MonitorSystemRule implements PatternSystemProvider{

	public static final String RULE_NAME = "Quiet activity";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect the absence of activity of interest.\n"
			+ " It means basically that the task sequence views will be empty at the exception of the uninteresting principal functions.\n"
			+ " The rule accepts a list of principal functions of no interest, defined through a regular expression.";

	private String extraInfo;
	
	public QuietActivityRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + def.getExtraInfo(), 
				"The global list of detected actions is empty after excluding the ones whom principal function is listed in the (pattern) list.");
		
		this.extraInfo = def.getExtraInfo();
	}

	@Override	
	public boolean isAdvancedMonitoringBased() {
		return false;
	}
	
	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SYSTEM_PATTERN);
	}

	@Override
	public boolean matchPattern(JzrSession session, Pattern pattern) {
		if (session.getActionsSize() == 0)
			return true;
		
		// Some actions detected
		Multiset<Tag> principalFunctionTags = session.getPrincipalFunctionSet();
		for (Tag principalFunctionTag : principalFunctionTags.elementSet()){
			if (!pattern.matcher(principalFunctionTag.getName()).find())
				return false; // on the first non matching element, it means there is activity, just then stop here
		}
		
		return true;
	}

	@Override
	public MonitorSystemEvent createSystemEvent(MonitorEventInfo info) {
		return new QuietActivityEvent(this.extraInfo, info);
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_VERY_HIGH;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}

}
