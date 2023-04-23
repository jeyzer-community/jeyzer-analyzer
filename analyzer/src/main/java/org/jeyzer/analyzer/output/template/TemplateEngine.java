package org.jeyzer.analyzer.output.template;

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







import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jeyzer.analyzer.config.ConfigTemplate;

public class TemplateEngine {
	
	// Common key attributes
	public static final String TARGET_KEY 			= "target";
	public static final String GENERATION_TIME_KEY  = "generation_time";
	public static final String APPLICATION_ID_KEY   = "application_id";
	public static final String APPLICATION_TYPE_KEY = "application_type";
	public static final String EVENTS_LIST_KEY 		= "events_list";
	public static final String EVENT_KEY 		    = "event";
	public static final String LAST_EVENT_KEY 		= "last_event";
	public static final String REFRESH_PERIOD_KEY	= "refresh_period";

	// velocity specific
	private static final String TEMPLATE_DIRECTORY_PROPERTY = "file.resource.loader.path";
	private static final String OUTPUT_ENCODING = "ISO-8859-1";
	private VelocityEngine velocity;
	private VelocityContext context;
	
	private String templateFileName;
	
	public TemplateEngine(ConfigTemplate templateCfg) {
		Properties props = new Properties();
		props.setProperty(TEMPLATE_DIRECTORY_PROPERTY, templateCfg.getTemplateDirectory());
		this.velocity = new VelocityEngine();
		velocity.init(props);
		
		this.context = new VelocityContext(); 
		this.templateFileName = templateCfg.getTemplateName();
	}
	
	public void addContextEntry(String key, Object value){
		if (key != null)
			this.context.put(key, value);
	}

	public String generate() {
        StringWriter writer = new StringWriter();
       	velocity.mergeTemplate(this.templateFileName, OUTPUT_ENCODING, context, writer );
        return writer.toString();
	}
}
