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







import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.error.JzrMonitorException;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.analyzer.util.SystemHelper;
import org.jeyzer.monitor.config.publisher.ConfigWebPublisher;
import org.jeyzer.monitor.engine.event.MonitorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebPublisher implements Publisher {

	public static final Logger logger = LoggerFactory.getLogger(WebPublisher.class);
	
	private ConfigWebPublisher publisherCfg;
	
	public WebPublisher(ConfigWebPublisher publisherCfg) {
		this.publisherCfg = publisherCfg;
	}

	@Override
	public void publish(JzrMonitorSession session, List<MonitorEvent> events, Map<String, List<String>> publisherPaths) throws JzrMonitorException {
		if (!publisherCfg.isEnabled())
			return;

		logger.info("Deploying web resources.");
		
		List<String> webPaths = publisherPaths.get(publisherCfg.getName());
		if (webPaths == null || webPaths.isEmpty())
			return;
		
		List<String> webFinalPaths = new ArrayList<>();
		for (String path : webPaths){
			File source = new File(path);
			
			try{
				SystemHelper.copyFile(source, new File(publisherCfg.getDeploy() + "/" + source.getName()));
			}catch(IOException ex){
				throw new JzrMonitorException(
						"Failed to deploy the web resource " 
							+ path + " under directory : " + publisherCfg.getDeploy(), ex);
			}
			
			String webUrl = this.publisherCfg.getUrl() + "/" + source.getName();

			webFinalPaths.add(webUrl);
		}
		
		// Replace all the paths by the final paths
		webPaths.clear();
		webPaths.addAll(webFinalPaths);
	}
}
