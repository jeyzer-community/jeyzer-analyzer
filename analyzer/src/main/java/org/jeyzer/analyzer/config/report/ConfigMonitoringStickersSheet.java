package org.jeyzer.analyzer.config.report;

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




import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.sticker.ConfigStickers;
import org.jeyzer.service.location.JzrLocationResolver;
import org.w3c.dom.Element;

public class ConfigMonitoringStickersSheet extends ConfigSheet {

	public static final String DEFAULT_DESCRIPTION = "Displays the monitoring stickers applied on the current session.\n"
			+ "Includes sticker positive and negative matching.";
	
	public static final String TYPE = "monitoring_stickers";
	
	private ConfigStickers configStickers; // can be null
	private JzrLocationResolver resolver;

	public ConfigMonitoringStickersSheet(Element configNode, int index, JzrLocationResolver resolver) throws JzrInitializationException {
		super(configNode, index);
		
		Element stickersNode = ConfigUtil.getFirstChildNode(configNode, ConfigStickers.JZRM_STICKERS);
		if (stickersNode != null)
			configStickers = new ConfigStickers(stickersNode, resolver);
		this.resolver = resolver;
	}

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}
	
	public ConfigStickers getConfigStickers(){
		return this.configStickers;
	}
	
	public JzrLocationResolver getJzrResolver() {
		return resolver;
	}
}
