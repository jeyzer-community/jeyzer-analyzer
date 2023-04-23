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




import java.util.Arrays;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;

public class ConfigSticker {
	
	protected enum StickerAppliance { LAZY, STRICT }
	
	public static final String JZRM_STICKER = "sticker";
	public static final String JZRM_TYPE = "type";
	private static final String JZRM_NAME = "name";
	private static final String JZRM_APPLIANCE = "appliance";
	
	private String group;
	private String type;
	private String name;
	private StickerAppliance appliance;
	
	private boolean dynamic;
	
	public ConfigSticker(Element stickerNode, String group, boolean dynamic) throws JzrInitializationException {
		this.group = group;
		
		this.type = ConfigUtil.getAttributeValue(stickerNode,JZRM_TYPE);
		if (this.type == null || this.type.isEmpty())
			throw new JzrInitializationException("Sticker is defined without any type.");		
		
		this.name = ConfigUtil.getAttributeValue(stickerNode,JZRM_NAME);
		if (this.name == null || this.name.isEmpty())
			throw new JzrInitializationException("Sticker is defined without any name.");
		
		loadAppliance(stickerNode);
		this.dynamic = dynamic;
	}

	public ConfigSticker(String name, String type, String group, boolean dynamic) throws JzrInitializationException {
		this.group = group;
		this.type = type;
		if (this.type == null || this.type.isEmpty())
			throw new JzrInitializationException("Sticker is defined without any type.");
		
		this.name = name;
		if (this.name == null || this.name.isEmpty())
			throw new JzrInitializationException("Sticker is defined without any name.");
		
		this.appliance = StickerAppliance.STRICT; // no effect
		this.dynamic = dynamic;
	}

	public String getType(){
		return this.type;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getFullName() {
		return this.group.isEmpty() ? this.name : (this.group + "." + this.name);
	}

	public String getGroup(){
		return this.group;
	}
	
	public boolean isStrictAppliance() {
		return StickerAppliance.STRICT.equals(appliance);
	}
	
	public boolean isLazyAppliance() {
		return StickerAppliance.LAZY.equals(appliance);
	}
	
	public boolean isDynamic() {
		return dynamic;
	}

	private void loadAppliance(Element stickerNode) throws JzrInitializationException {
		String value = ConfigUtil.getAttributeValue(stickerNode,JZRM_APPLIANCE);
		
		if (value == null || value.isEmpty()) {
			this.appliance = StickerAppliance.STRICT; // default
			return;
		}
		
		try {
			this.appliance = StickerAppliance.valueOf(value.trim().toUpperCase());
		}catch(IllegalArgumentException ex) {
			throw new JzrInitializationException("Sticker is defined with an invalid appliance value : " + value + " . "
				+ "Must be one of those values :" + Arrays.toString(StickerAppliance.values()).toLowerCase());
		}
	}
}
