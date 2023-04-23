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

import org.jeyzer.analyzer.data.ProcessCard;
import org.jeyzer.analyzer.data.ProcessCard.ProcessCardProperty;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigParamMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.system.PatternSystemProvider;
import org.jeyzer.monitor.impl.event.system.ProcessCardPropertyPatternEvent;

public class ProcessCardPropertyPatternRule extends MonitorSystemRule implements PatternSystemProvider{

	public static final String RULE_NAME = "Process card property pattern";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to check process card property values. "
			+ "process card properties are either Java system properties or system environment variables.";
	
	private Pattern paramNamePattern;
	private String paramDisplay;
	
	public ProcessCardPropertyPatternRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + ((ConfigParamMonitorRule)def).getDisplayName(),
				((ConfigParamMonitorRule)def).getDisplayName() + " value contains the (pattern) regex.");
		ConfigParamMonitorRule mxDef = (ConfigParamMonitorRule) def;
		this.paramNamePattern = Pattern.compile(mxDef.getParamName());
		this.paramDisplay = mxDef.getDisplayName();
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
		ProcessCard card = session.getProcessCard();
		if  (card == null)
			return false; // not available
		
		ProcessCardProperty property = card.getValue(paramNamePattern);
		if (property == null)
			return false;
		
		return pattern.matcher(property.getValue()).find();
	}

	@Override
	public MonitorSystemEvent createSystemEvent(MonitorEventInfo info) {
		return new ProcessCardPropertyPatternEvent(
				this.paramDisplay,
				info,
				this.paramNamePattern
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
