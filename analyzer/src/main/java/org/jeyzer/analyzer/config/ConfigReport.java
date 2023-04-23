package org.jeyzer.analyzer.config;

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



import java.io.File;

import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.config.report.ConfigXLSX;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.setup.JzrSetupManager;
import org.jeyzer.service.location.JzrLocationResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class ConfigReport {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigReport.class);
	
	private static final String JZRR_REPORT = "report";
	private static final String JZRR_ENABLED = "enabled";
	private static final String JZRR_XLSX_REPORT = "xlsx_report";
	private static final String JZRR_AUDIO_MIDI = "audio_midi";
	
	private boolean enabled = false;
	private ConfigXLSX configXSLX = null;
	private ConfigDisplay configAudio = null;
	
	public ConfigReport(File file, JzrSetupManager setupMgr, JzrLocationResolver resolver) throws JzrInitializationException{
		loadConfiguration(file, setupMgr, resolver);
	}		
	
	private void loadConfiguration(File file, JzrSetupManager setupMgr, JzrLocationResolver resolver) throws JzrInitializationException{
		
		try {
			Document doc = ConfigUtil.loadDOM(file);
			
			// report
			NodeList nodes = doc.getElementsByTagName(JZRR_REPORT);
			Element reportNode = (Element)nodes.item(0);
			
			String value = ConfigUtil.getAttributeValue(reportNode, JZRR_ENABLED);
			// by default enable it
			enabled =  value.isEmpty() || Boolean.parseBoolean(value);
			if (!enabled)
				return;
			
			Element xslxNode = ConfigUtil.getFirstChildNode(reportNode, JZRR_XLSX_REPORT);
			if (xslxNode != null)
				this.configXSLX = new ConfigXLSX(xslxNode, setupMgr, resolver);
			
			Element audioNode = ConfigUtil.getFirstChildNode(reportNode, JZRR_AUDIO_MIDI);
			if (audioNode != null)
				this.configAudio = new ConfigDisplay(audioNode);
			
		} catch (Exception e) {
			logger.error("Failed to load the JZR report configuration.", e);
			throw new JzrInitializationException("Failed to load the JZR report configuration.", e);
		}
		
	}

	public ConfigXLSX getConfigXSLX() {
		return configXSLX;
	}
	
	public ConfigDisplay getConfigAudio() {
		return configAudio;
	}

	public boolean isEnabled() {
		return enabled;
	}

}
