package org.jeyzer.monitor.engine.rule.threshold.action;

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




import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.monitor.config.engine.ConfigMonitorTaskThreshold;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.info.Scope;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;
import org.jeyzer.monitor.engine.rule.condition.task.SignalTaskProvider;

public class ActionSignalPercentageThreshold extends ActionPercentageThreshold {
	
	public ActionSignalPercentageThreshold(ConfigMonitorThreshold thCfg, SubLevel defaultSubLevel) {
		super(thCfg, Scope.ACTION, MatchType.SIGNAL_PERCENTAGE, defaultSubLevel);
	}
	
	@Override
	public String getDisplayCondition() {
		ConfigMonitorTaskThreshold taskCfg = (ConfigMonitorTaskThreshold) this.cfg;
		return "Percentage of signal >= " + taskCfg.getPercentageInAction();
	}

	@Override
	protected boolean matchCondition(MonitorTaskRule rule, ThreadStack stack) {
		SignalTaskProvider signalProvider = (SignalTaskProvider) rule;
		return signalProvider.matchSignal(stack);
	}

}
