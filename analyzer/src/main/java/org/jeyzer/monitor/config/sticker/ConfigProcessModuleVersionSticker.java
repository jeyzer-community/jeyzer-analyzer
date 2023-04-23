package org.jeyzer.monitor.config.sticker;

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




import java.util.regex.Pattern;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;

public class ConfigProcessModuleVersionSticker extends ConfigSticker{
	
	private static final String JZRM_MODULE_NAME = "module_name";
	private static final String JZRM_PATTERN = "pattern";
	
	private String moduleName;
	private Pattern pattern;
	
	public ConfigProcessModuleVersionSticker(Element stickerNode, String group, boolean dynamic) throws JzrInitializationException {
		super(stickerNode, group, dynamic);
		
		this.moduleName = ConfigUtil.getAttributeValue(stickerNode,JZRM_MODULE_NAME);
		if (this.moduleName == null || this.moduleName.isEmpty())
			throw new JzrInitializationException("Sticker is defined without any Java module name.");
		
		String patternValue = ConfigUtil.getAttributeValue(stickerNode,JZRM_PATTERN);
		if (patternValue == null || patternValue.isEmpty())
			throw new JzrInitializationException("Sticker is defined without any Java module pattern value.");		
		this.pattern = Pattern.compile(patternValue);
	}

	public String getModuleName(){
		return this.moduleName;
	}
	
	public Pattern getPattern(){
		return this.pattern;
	}
}
