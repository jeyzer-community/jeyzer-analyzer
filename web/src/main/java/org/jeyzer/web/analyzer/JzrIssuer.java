package org.jeyzer.web.analyzer;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Web
 * --
 * Copyright (C) 2020 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.VaadinRequest;

public class JzrIssuer {

	private static final Logger logger = LoggerFactory.getLogger(JzrIssuer.class);
	private static final String ISSUER_FILE_NAME = "/issuer.info";
	
	private String email;
	private String ip;
	private String host;
	
	public JzrIssuer(VaadinRequest request){
		this.ip = request.getRemoteAddr();
		this.host = request.getRemoteHost();
	}
	
	public void setEmail(String email){
		this.email = email;
	}
	
	public void dump(String outputDir) {
		File issuerFile = new File(outputDir + ISSUER_FILE_NAME);
		try (
				FileOutputStream output = new FileOutputStream(issuerFile);
				OutputStreamWriter bw = new OutputStreamWriter(output);
				Writer writer = new BufferedWriter(bw);
			)
		{
			writer.write(this.toString());
		} catch (Exception ex) {
			logger.warn("Failed to write the issuer info in the file : " + issuerFile.getAbsolutePath(), ex);
		}
	}
	
	public String toString(){
		StringBuilder b = new StringBuilder();
		String cr = System.getProperty("line.separator");
		b.append("Issuer info" + cr);
		b.append(" - Email : " + this.email + cr);
		b.append(" - IP    : " + this.ip + cr);
		b.append(" - Host  : " + this.host + cr);
		return b.toString();
	}
	
}
