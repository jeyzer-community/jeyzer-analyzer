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

import java.io.File;

import org.jeyzer.monitor.config.publisher.zabbix.ConfigZabbixSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZabbixSenderAccessor {
	
	private ConfigZabbixSetup setupCfg;
	
	public static final Logger logger = LoggerFactory.getLogger(ZabbixSenderAccessor.class);

	public ZabbixSenderAccessor(ConfigZabbixSetup setupCfg) {
		this.setupCfg = setupCfg;
	}

	public void executeSender(File input) {
		long startTime;
		long endTime;
		long duration;
		int result = -1;
        
		String command = setupCfg.getSender() + " " + setupCfg.getParamsValue() + " -i " + input.getAbsolutePath();
		
		try {
	        if (logger.isDebugEnabled())
	        	logger.debug("Executing command line : {}", command);
	    	startTime = System.currentTimeMillis();
	        final Process p = Runtime.getRuntime().exec(command);
	        
	        Thread outputThread = new Thread(
	        		new ZabbixSenderOutputProcessor(
	        				p.getInputStream()
	        				));

	        Thread errorThread = new Thread(
	        		new ZabbixSenderErrorProcessor(
	        				p.getErrorStream()
	        				));
	        
	        outputThread.start();
	        errorThread.start();
	        
	        result = p.waitFor();        

	        endTime = System.currentTimeMillis();
	        duration = endTime - startTime;

	        if (logger.isDebugEnabled())
	        	logger.debug("Zabbix sender execution time : {} ms", duration);        
			
	        logResult(result, command);
	        
		}catch (Exception e) {
			logger.error("Zabbix sender command line is : {}", command);
			logger.error("Failed to execute the Zabbix sender", e);
		}
	}

	private void logResult(int result, String command) {
        if (result == 0){
			logger.info("Zabbix sending successful : data succesfully processed by the Zabbix server");
        }
        else if (result == 1){
			logger.error("Zabbix sending failed. Process exit status is 1");
			logger.error("Reexecute the Zabbix sender command line from a shell to examine the error in console.");
			logger.error("Zabbix sender command line is : {}", command);
       }
       else if (result == 2){
     	    logger.error("Zabbix server processing failed. Process exit status is 2");
			logger.error("The Zabbix sender transmitted the data but the Zabbix server failed to process at least one entry.");
			logger.error("Please check the Zabbix server logs.");
			logger.error("You can still execute the Zabbix sender command line to get the number of errors.");
			logger.error("Zabbix sender command line is : {}", command);
       }
       else {
			logger.error("Failed to execute the Zabbix sender. Process result is : {}", result);
			logger.error("Reexecute the Zabbix sender command line from a shell to examine the console error.");
			logger.error("Zabbix sender command line is : {}", command);
       }
	}
}
