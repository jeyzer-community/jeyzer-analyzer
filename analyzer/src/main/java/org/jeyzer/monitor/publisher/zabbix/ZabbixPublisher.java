package org.jeyzer.monitor.publisher.zabbix;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 - 2023 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jeyzer.analyzer.config.ConfigTemplate;
import org.jeyzer.analyzer.error.JzrMonitorException;
import org.jeyzer.analyzer.output.template.TemplateEngine;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.analyzer.util.SystemHelper;
import org.jeyzer.analyzer.util.SystemHelper.OldFileFilter;
import org.jeyzer.monitor.config.publisher.zabbix.ConfigZabbixPublisher;
import org.jeyzer.monitor.engine.event.MonitorEvent;
import org.jeyzer.monitor.publisher.Publisher;
import org.jeyzer.monitor.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZabbixPublisher implements Publisher{

	public static final Logger logger = LoggerFactory.getLogger(ZabbixPublisher.class);	
	
	public static final String ZABBIX_INPUT_FILE_PREFIX = "zabbix_input-";
	public static final String ZABBIX_INPUT_FILE_SUFFIX = ".txt";
	
	private ConfigZabbixPublisher cfg;
	private ZabbixSenderAccessor sender;
	
	public ZabbixPublisher(ConfigZabbixPublisher publisherCfg) {
		this.cfg = publisherCfg;
		this.sender = new ZabbixSenderAccessor(publisherCfg.getSetupCfg());
	}

	@Override
	public void publish(JzrMonitorSession monitorSession, List<MonitorEvent> events, Map<String, List<String>> publisherPaths) throws JzrMonitorException {	
		
		if (!(monitorSession instanceof JzrSession)) {
			logger.warn("Monitor session incomplete : skip the the monitoring event publishing to Zabbix.");
			
			if (events.get(0) instanceof org.jeyzer.monitor.impl.event.analyzer.RecordingSnapshotNotFoundEvent)
				logger.warn("Reason : new recording snapshots not found.");
			
			return;
		}
		
		JzrSession session = (JzrSession) monitorSession;
		
		logger.info("Publishing the monitoring events in Zabbix.");
		if (events.isEmpty()) {
			logger.info("No events to publish : skipping the Zabbix sending (because Zabbix does not process empty input content).");
			return;
		}
		
		File input = buildInputFile(events, session);
		if (input == null)
			return;
		
		sender.executeSender(input);
		
		cleanInput();
	}
	
	private void cleanInput() {
		long time = Calendar.getInstance().getTimeInMillis();
		long delay = this.cfg.getSetupCfg().getKeepDuration().toMillis();
		
		OldFileFilter filter = new OldFileFilter(ZABBIX_INPUT_FILE_PREFIX, ZABBIX_INPUT_FILE_SUFFIX, time - delay);		
		File storageDir = this.cfg.getSetupCfg().getStorageDirectory();
		
		File[] filesToDelete = storageDir.listFiles(filter);
		for (File file : filesToDelete) {
			if (!file.delete())
				logger.warn("Failed to delete the Zabbix input file : {}", file.getAbsolutePath());
		}
	}

	private File buildInputFile(List<MonitorEvent> events, JzrSession session) {
        // Sort it by external id
        events.sort((MonitorEvent event1, MonitorEvent event2) -> event1.getExtId().compareTo(event2.getExtId()));
		
        String content = buildInput(events, session, this.cfg.getInputTemplateCfg());
		        
		File storageDir = this.cfg.getSetupCfg().getStorageDirectory();
		String fileName = FileUtil.getTimeStampedFileName(ZABBIX_INPUT_FILE_PREFIX, new Date(), ZABBIX_INPUT_FILE_SUFFIX);
		File input = new File(storageDir, fileName);
		
		try (
			FileOutputStream   fos = new FileOutputStream(input);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
			BufferedWriter     writer = new BufferedWriter(osw)
			)
		{
			writer.write(content);
		} catch (Exception ex) {
			logger.error("Failed to write the Zabbix input file : " + input.getAbsolutePath(), ex);
			return null;
		}
		
		return input;
	}
	
	private String buildInput(List<MonitorEvent> events, JzrSession session, ConfigTemplate templateCfg) {
		TemplateEngine templateEngine = new TemplateEngine(templateCfg);

		templateEngine.addContextEntry(TemplateEngine.EVENTS_LIST_KEY, events);
		templateEngine.addContextEntry(
				TemplateEngine.APPLICATION_ID_KEY, 
				session.getApplicationId());
        templateEngine.addContextEntry(
        		TemplateEngine.APPLICATION_TYPE_KEY, 
        		session.getApplicationId());
        templateEngine.addContextEntry(
        		TemplateEngine.HOST_NAME_KEY, 
        		SystemHelper.getHostName("NA"));
        
        return templateEngine.generate();
	}
}
