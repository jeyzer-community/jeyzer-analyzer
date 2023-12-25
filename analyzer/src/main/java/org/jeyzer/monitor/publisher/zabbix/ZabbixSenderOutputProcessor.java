package org.jeyzer.monitor.publisher.zabbix;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 - 2023 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZabbixSenderOutputProcessor implements Runnable {

	public static final Logger logger = LoggerFactory.getLogger(ZabbixSenderOutputProcessor.class);	
	
	private InputStream is;
	
	public ZabbixSenderOutputProcessor(InputStream inputStream) {
		this.is = inputStream;
	}

	@Override
	public void run() {
        try (
                InputStreamReader isr = new InputStreamReader(is);
        		BufferedReader br = new BufferedReader(isr);
        	)
        {
            String line = null;
            while ( (line = br.readLine()) != null)
            {
            	logger.info("Zabbix sender output : {}", line);
            }
        } catch (IOException ioe){
        	logger.error("Failed to read Zabbix sender output.", ioe);  
		} catch (Exception e){
			logger.error("Failed to read Zabbix sender output. Unexpected exception.", e);  
		}
	}
}
