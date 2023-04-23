package org.jeyzer.monitor.config.publisher;

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




import org.jeyzer.analyzer.config.ConfigMail;
import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.engine.event.info.Level;
import org.w3c.dom.Element;

public class ConfigMailPublisher extends ConfigPublisher{
	
	public static final String NAME = "mailer";
	
	private static final String JZRM_NOTIFICATION_THRESHOLD = "threshold";
	private static final String JZRM_REEMIT_PUBLISHED_EVENTS = "reemit_published_events";	

	private boolean reemitPublishedEvents;
	private Level threshold;
	private ConfigMail mailCfg;
	
	public ConfigMailPublisher(Element node) throws Exception, JzrInitializationException {
		super(NAME, node);
		
		// notification
		threshold = Level.getLevel(ConfigUtil.getAttributeValue(node, JZRM_NOTIFICATION_THRESHOLD));
		reemitPublishedEvents = Boolean.parseBoolean(ConfigUtil.getAttributeValue(node, JZRM_REEMIT_PUBLISHED_EVENTS));
		if (Level.UNKNOWN.equals(threshold))
			threshold = Level.CRITICAL; // default		
		
		mailCfg = new ConfigMail(node);
	}

	public boolean isReemitPublishedEvents() {
		return reemitPublishedEvents;
	}

	public Level getThreshold() {
		return threshold;
	}

	public ConfigMail getMailCfg() {
		return mailCfg;
	}
	
	@Override
	public boolean isEnabled() {
		return mailCfg.isEnabled();
	}

}
