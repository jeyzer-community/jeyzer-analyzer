package org.jeyzer.analyzer.communication;

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

import org.jeyzer.analyzer.config.ConfigMail;
import org.jeyzer.analyzer.output.template.TemplateEngine;
import org.jeyzer.analyzer.session.JzrSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalysisMailer extends Mailer {

	private static final Logger logger = LoggerFactory.getLogger(AnalysisMailer.class);
	
	private static final String REPORT_DESCRIPTION_KEY = "report_description";
	private static final String REPORT_ISSUER_KEY = "report_issuer";
	
	private String profile;
	
	public AnalysisMailer(ConfigMail cfg, String profile){
		super(cfg);
		this.profile = profile;
	}

	public void sendReport(List<String> attachmentPaths, JzrSession session) {

		// Add data to the context
        templateEngine.addContextEntry(REPORT_DESCRIPTION_KEY, session.getDescription());
        templateEngine.addContextEntry(REPORT_ISSUER_KEY, session.getIssuer());
		templateEngine.addContextEntry(
				TemplateEngine.APPLICATION_ID_KEY, 
				session instanceof JzrSession?session.getApplicationId():null);
        templateEngine.addContextEntry(
        		TemplateEngine.APPLICATION_TYPE_KEY, 
        		session instanceof JzrSession?session.getApplicationType():null);
        
		// prepare content
		String text = templateEngine.generate();
		
		try {
			sendMail(text, attachmentPaths);
		} catch (Exception ex) {
			logger.warn("Failed to send the Jeyzer Analyzer report for the " + this.profile + " profile.", ex);
		}
	}

	@Override
	protected boolean isHtmlFormat() {
		return false;
	}
	
}
