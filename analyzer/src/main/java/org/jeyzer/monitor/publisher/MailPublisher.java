package org.jeyzer.monitor.publisher;

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







import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.error.JzrMonitorException;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.monitor.communication.MonitorMailer;
import org.jeyzer.monitor.config.publisher.ConfigMailPublisher;
import org.jeyzer.monitor.engine.event.MonitorEvent;
import org.jeyzer.monitor.util.MonitorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailPublisher implements Publisher {

	public static final Logger logger = LoggerFactory.getLogger(MailPublisher.class);
	
	private ConfigMailPublisher publisherCfg;
	
	public MailPublisher(ConfigMailPublisher publisherCfg) {
		this.publisherCfg = publisherCfg;
	}

	@Override
	public void publish(JzrMonitorSession session, List<MonitorEvent> events, Map<String, List<String>> publisherPaths) throws JzrMonitorException {
		if (!publisherCfg.getMailCfg().isEnabled())
			return;
		
		MonitorMailer mailer = new MonitorMailer(publisherCfg.getMailCfg());
		
		List<String> attachmentPaths = publisherPaths.get(publisherCfg.getName());
		if (attachmentPaths == null || attachmentPaths.isEmpty())
			return;
		
		if (MonitorHelper.isEventThresholdMatched(events, this.publisherCfg.getThreshold(), this.publisherCfg.isReemitPublishedEvents())){
			logger.info("Emailing monitoring events.");
			mailer.sendAlert(session, events, attachmentPaths);
		}
	}

}
