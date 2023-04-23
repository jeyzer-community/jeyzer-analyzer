package org.jeyzer.monitor.report;

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







import java.util.ArrayList;
import java.util.List;

import org.jeyzer.monitor.JeyzerMonitor;
import org.jeyzer.monitor.config.ConfigMonitor;
import org.jeyzer.monitor.report.MonitorLogger.MonitorLoggerDefinition;


public class MonitorLoggerBuilder {
	
	private static final MonitorLoggerBuilder builder = new MonitorLoggerBuilder();
	
	private MonitorLoggerBuilder(){}
	
	public static MonitorLoggerBuilder newInstance(){
		return builder;
	}
	
	public List<MonitorLogger> buildLoggers(ConfigMonitor conf){
		List<MonitorLogger> loggers = new ArrayList<>(0);
		String node = (String)conf.getValue(ConfigMonitor.JZRM_NODE);
		
		@SuppressWarnings("unchecked")
		List<MonitorLoggerDefinition> defs = (List<MonitorLoggerDefinition>)conf.getValue(ConfigMonitor.JZRM_LOGGERS);
		
		for (MonitorLoggerDefinition def : defs){
			MonitorLogger logger;
			
			if (MonitorLoggerDefinition.FORMAT_SINGLE_LINE.equals(def.getFormat())){
				logger = new SingleLineLogger(def, node);
				loggers.add(logger);
			}
			else if (MonitorLoggerDefinition.FORMAT_SINGLE_EXCEL_LINE.equals(def.getFormat())){
				logger = new SingleExcelLineLogger(def, node);
				loggers.add(logger);
			}
			else if (MonitorLoggerDefinition.FORMAT_JOURNAL.equals(def.getFormat())){
				logger = new JournalLogger(def, node);
				loggers.add(logger);
			}
			else if (MonitorLoggerDefinition.FORMAT_CSV.equals(def.getFormat())){
				logger = new CSVLogger(def, node);
				loggers.add(logger);
			}
			else if (MonitorLoggerDefinition.FORMAT_HTML.equals(def.getFormat())){
				logger = new HTMLLogger(def, node);
				loggers.add(logger);
			}
			else{
				JeyzerMonitor.logger.error("Logger not found for format : " + def.getFormat());
				System.exit(-1);
			}
		}
		
		return loggers;
	}
	
}
