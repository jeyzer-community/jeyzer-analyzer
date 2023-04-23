package org.jeyzer.monitor.impl.rule.session.advanced;

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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_GLOBAL_PATTERN;
import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SESSION_PATTERN;




import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigParamMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.session.PatternSessionProvider;
import org.jeyzer.monitor.impl.event.session.advanced.JeyzerMXContextParamPatternEvent;

public class JeyzerMXContextParamPatternRule  extends MonitorSessionRule implements PatternSessionProvider {

	public static final String RULE_NAME = "Jeyzer MX context parameter pattern";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect signals upon applicative data parsing. "
			+ "The data is originally published by the application through the Jeyzer publisher API. "
			+ "It is usually used to emit applicative signal alerts. "
			+ "A good alternative is to publish directly Jeyzer applicative events through the Jeyzer publisher API.";

	private String paramName;
	private String paramDisplay;
	
	public JeyzerMXContextParamPatternRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + ": " + ((ConfigParamMonitorRule)def).getDisplayName(), 
				((ConfigParamMonitorRule)def).getDisplayName() + " contains the (pattern) regex.");
		ConfigParamMonitorRule mxDef = (ConfigParamMonitorRule) def;
		this.paramName = mxDef.getParamName();
		this.paramDisplay = mxDef.getDisplayName();
	}

	@Override
	public boolean matchPattern(ThreadDump dump, Pattern pattern) {
		String value = dump.getJeyzerMXContextParams().get(paramName);
		
		if (value == null || value.isEmpty())
			return false;

		return pattern.matcher(value).find();
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new JeyzerMXContextParamPatternEvent(
				this.paramDisplay,
				info,
				td,
				this.paramName
			);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return true;
	}
	
	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SESSION_PATTERN, THRESHOLD_GLOBAL_PATTERN);
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
