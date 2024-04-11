package org.jeyzer.monitor.config.publisher.zabbix;

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
import java.time.Duration;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;

public class ConfigZabbixSetup {
	
	private static final String JEYZER_MONITOR_ZABBIX_SENDER = "zabbix_sender";
	
	private static final String JEYZER_MONITOR_ZABBIX_PROCESS = "process";
	private static final String JEYZER_MONITOR_ZABBIX_PATH = "path";

	private static final String JEYZER_MONITOR_ZABBIX_PARAMETERS = "parameters";
	private static final String JEYZER_MONITOR_ZABBIX_VALUE = "value";
	
	private static final String JEYZER_MONITOR_ZABBIX_INPUT_FILE = "input_file";
	private static final String JEYZER_MONITOR_ZABBIX_STORAGE_DIRECTORY = "storage_directory";
	private static final String JEYZER_MONITOR_ZABBIX_KEEP = "keep";
	
	
	
	private File sender;
	private String paramsValue;

	private File storageDirectory;
	private Duration keepFiles;

	public ConfigZabbixSetup(Element zabbixSetupNode) throws JzrInitializationException {
		if (zabbixSetupNode == null)
			throw new JzrInitializationException(
					"Failed to initialize the monitor Zabbix publisher : Zabbix setup is missing.");
		
		Element zabbixSenderNode = ConfigUtil.getFirstChildNode(zabbixSetupNode, JEYZER_MONITOR_ZABBIX_SENDER);
		if (zabbixSenderNode == null)
			throw new JzrInitializationException(
					"Failed to initialize the monitor Zabbix sender : Zabbix sender configuration is missing.");
		
		loadandValidateProcessFile(zabbixSenderNode);
		loadParameters(zabbixSenderNode);
		loadInputFile(zabbixSenderNode);
	}
	
	public File getSender() {
		return sender;
	}

	public String getParamsValue() {
		return paramsValue;
	}

	public File getStorageDirectory() {
		return storageDirectory;
	}

	public Duration getKeepDuration() {
		return keepFiles;
	}

	private void loadInputFile(Element zabbixSenderNode) throws JzrInitializationException {
		Element zabbixInputFileNode = ConfigUtil.getFirstChildNode(zabbixSenderNode, JEYZER_MONITOR_ZABBIX_INPUT_FILE);
		if (zabbixInputFileNode == null)
			throw new JzrInitializationException(
					"Failed to initialize the monitor Zabbix sender : Zabbix input file configuration is missing.");
		
		String storageDirectoryValue = ConfigUtil.getAttributeValue(zabbixInputFileNode, JEYZER_MONITOR_ZABBIX_STORAGE_DIRECTORY);
		if (storageDirectoryValue == null || storageDirectoryValue.isEmpty())
			throw new JzrInitializationException(
					"Failed to initialize the monitor Zabbix sender : Zabbix input storage location is missing.");
		
		// check its existence
		storageDirectory = new File(storageDirectoryValue);		
		if (!storageDirectory.exists() && !storageDirectory.mkdirs())
			throw new JzrInitializationException(
					"Failed to initialize the monitor Zabbix sender : Zabbix storage directory cannot be created : " + storageDirectory.getAbsolutePath());
		
		this.keepFiles = ConfigUtil.getAttributeDuration(zabbixInputFileNode, JEYZER_MONITOR_ZABBIX_KEEP);
		if (this.keepFiles == null)
			throw new JzrInitializationException(
					"Failed to initialize the monitor Zabbix sender : Zabbix input file keep parameter is not a duration.");
	}

	private void loadParameters(Element zabbixSenderNode) throws JzrInitializationException {
		Element zabbixParametersNode = ConfigUtil.getFirstChildNode(zabbixSenderNode, JEYZER_MONITOR_ZABBIX_PARAMETERS);
		if (zabbixParametersNode == null)
			throw new JzrInitializationException(
					"Failed to initialize the monitor Zabbix sender : Zabbix process parameters are missing.");
		
		paramsValue = ConfigUtil.getAttributeValue(zabbixParametersNode, JEYZER_MONITOR_ZABBIX_VALUE);
		if (paramsValue == null || paramsValue.isEmpty())
			throw new JzrInitializationException(
					"Failed to initialize the monitor Zabbix sender : Zabbix process parameters value is missing.");
		
		// could validate the Zabbix parameters..
	}

	private void loadandValidateProcessFile(Element zabbixSenderNode) throws JzrInitializationException {
		Element zabbixProcessNode = ConfigUtil.getFirstChildNode(zabbixSenderNode, JEYZER_MONITOR_ZABBIX_PROCESS);
		if (zabbixProcessNode == null)
			throw new JzrInitializationException(
					"Failed to initialize the monitor Zabbix sender : Zabbix process configuration is missing.");
		
		String processPath = ConfigUtil.getAttributeValue(zabbixProcessNode, JEYZER_MONITOR_ZABBIX_PATH);
		if (processPath == null || processPath.isEmpty())
			throw new JzrInitializationException(
					"Failed to initialize the monitor Zabbix sender : Zabbix process path is missing.");
		// check its existence
		sender = new File(processPath);
		if (!sender.exists() || sender.isDirectory())
			throw new JzrInitializationException(
					"Failed to initialize the monitor Zabbix sender : Zabbix sender executable is not found.");
		if (!sender.canExecute())
			throw new JzrInitializationException(
					"Failed to initialize the monitor Zabbix sender : Zabbix sender does not have exection permissions.");
	}

}
