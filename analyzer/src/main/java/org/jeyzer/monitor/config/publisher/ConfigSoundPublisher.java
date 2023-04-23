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




import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;

public class ConfigSoundPublisher extends ConfigPublisher{
	
	public static final String NAME = "sound";
	
	private static final String JZRM_SOUND_ENABLED = "sound_enabled";	

	private boolean soundEnabled;
	
	public ConfigSoundPublisher(Element node) throws Exception, JzrInitializationException {
		super(NAME, node);
		this.soundEnabled = Boolean.parseBoolean(ConfigUtil.getAttributeValue(node, JZRM_SOUND_ENABLED));
	}

	@Override
	public boolean isEnabled() {
		return soundEnabled;
	}

}
