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

public class ZabbixSenderErrorProcessor implements Runnable {

	public static final Logger logger = LoggerFactory.getLogger(ZabbixSenderErrorProcessor.class);		
	
	private InputStream es;	
	
	public ZabbixSenderErrorProcessor(InputStream es) {
		this.es = es;
	}

	@Override
	public void run() {
        try (
                InputStreamReader esr = new InputStreamReader(es);
                BufferedReader br = new BufferedReader(esr);
        	)
        {
            String line=null;
            while ( (line = br.readLine()) != null)
            {
           		logger.error("Zabbix sender error message : {}", line);
            }
        } catch (IOException ioe){
        	logger.error("Failed to read the Zabbix sender error message.");  
        }
	}

}
