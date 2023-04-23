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







import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.jeyzer.analyzer.config.ConfigTemplate;
import org.jeyzer.analyzer.output.template.TemplateEngine;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTMLLogger extends MonitorLogger{

	private static final Logger logger = LoggerFactory.getLogger(HTMLLogger.class);
	
	public HTMLLogger(MonitorLoggerDefinition def, String node) {
		super(def, node);
	}

	@Override
	public void printHeader(JzrMonitorSession session, BufferedWriter out,
			List<MonitorEvent> events) throws IOException {
		// do nothing
	}

	@Override
	public void printEvents(JzrMonitorSession session, BufferedWriter out, List<MonitorEvent> events) throws IOException {
		TemplateEngine templateEngine = new TemplateEngine(((HTMLLoggerDefinition)def).getTemplateConfiguration());

        // Add data to the context
		templateEngine.addContextEntry(TemplateEngine.TARGET_KEY, this.node);
		templateEngine.addContextEntry(
				TemplateEngine.APPLICATION_ID_KEY, 
				session instanceof JzrSession?((JzrSession)session).getApplicationId():null);
        templateEngine.addContextEntry(
        		TemplateEngine.APPLICATION_TYPE_KEY, 
        		session instanceof JzrSession?((JzrSession)session).getApplicationType():null);
        templateEngine.addContextEntry(TemplateEngine.EVENTS_LIST_KEY, events);
        templateEngine.addContextEntry(TemplateEngine.GENERATION_TIME_KEY, new Date());

        try
        {
            String result = templateEngine.generate();
            out.write(result);
        }
        catch (Exception ex)
        {
        	logger.error("HTML logger skipped : failed to instanciate the Velocity template.", ex);
            return;
        }
	}

	@Override
	public void printFooter(JzrMonitorSession session, BufferedWriter out,
			List<MonitorEvent> events) throws IOException {
		// do nothing
	}
	
	@Override
	public boolean isAppend() {
		return false;
	}
	
	public static class HTMLLoggerDefinition extends MonitorLoggerDefinition{

		private ConfigTemplate templateCfg;
		
		public HTMLLoggerDefinition(String format, String outputDir, String fileName, List<String> publishers, ConfigTemplate templateCfg){
			super(format, outputDir, fileName, publishers);
			this.templateCfg = templateCfg;
		}
		
		public ConfigTemplate getTemplateConfiguration(){
			return this.templateCfg;
		}
	}
}
