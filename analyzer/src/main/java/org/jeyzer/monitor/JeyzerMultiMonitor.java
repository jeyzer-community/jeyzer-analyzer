package org.jeyzer.monitor;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.jeyzer.analyzer.config.ConfigThreadLocal;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrMonitorInitializationException;
import org.jeyzer.monitor.config.ConfigMultiMonitor;
import org.jeyzer.monitor.config.multimonitor.ConfigMultiMonitorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JeyzerMultiMonitor {

	public static final Logger logger = LoggerFactory.getLogger(JeyzerMultiMonitor.class);
	
	private List<JeyzerMonitor> monitors = new ArrayList<>();
	
	private void init() {
		ConfigMultiMonitor multiMonitorCfg = null;
		
		try {
			multiMonitorCfg = new ConfigMultiMonitor();
		} catch (JzrInitializationException ex) {
			abort(ex,"Failed to load the Jeyzer Multi Monitor configuration.");
		}
		
		Semaphore semaphore = new Semaphore(multiMonitorCfg.getMaxParallelExecutions());
		
		// initialize each monitor in its own environment context
		for (ConfigMultiMonitorEntry entry : multiMonitorCfg.getMultiMonitors()){
			
			MonitorInitializer monitorInit = new MonitorInitializer(entry, semaphore);
			Thread monitorInitThread = new Thread(monitorInit);
			monitorInitThread.start();
			try {
				monitorInitThread.join();
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				abort(ex,"Failed to load the Jeyzer Monitor configurations. Init monitor thread interrupted.");
			}
			
			JeyzerMonitor monitor = monitorInit.getMonitor();
			if (monitor != null)
				monitors.add(monitor);
		}
	}

	private void monitor() {
		logMonitors();
		
		for (JeyzerMonitor monitor : monitors){
			ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
					new JeyzerMonitorThreadFactory(monitor.getApplicationType() + "-" + monitor.getNodeName())
					);
			executor.scheduleWithFixedDelay(monitor, 0, monitor.getPeriod().getSeconds(), TimeUnit.SECONDS);
		}
	}
	
	private void logMonitors() {
		if (!logger.isInfoEnabled())
			return;
		
		logger.info("-----------------------------------------------");
		logger.info("Jeyzer Multi Monitor initialized");
		logger.info(monitors.size() + " monitors loaded :");
		logger.info("-----------------------------------------------");
		for (JeyzerMonitor monitor : monitors){
			logger.info("");
			logger.info("Monitor node       : " + monitor.getNodeName());
			logger.info("Monitor profile    : " + monitor.getApplicationType());
			logger.info("Monitor period     : " + monitor.getPeriod().getSeconds() + " sec");
			logger.info("XLS Report enabled : " + Boolean.toString(monitor.isAnalysisReportEnabled()));
		}
		logger.info("");
		logger.info("-----------------------------------------------");
	}

	protected void abort(Exception ex, String msg){
		logger.error(msg, ex);
		System.exit(-1);
	}
	
	public static class MonitorInitializer implements Runnable{

		private ConfigMultiMonitorEntry entry;
		private JeyzerMonitor monitor;
		private Semaphore semaphore;
		
		public MonitorInitializer(ConfigMultiMonitorEntry entry, Semaphore semaphore) {
			this.entry = entry;
			this.semaphore = semaphore;
		}

		public JeyzerMonitor getMonitor() {
			return monitor;
		}

		@Override
		public void run() {
			// inject the environment, monitor file path and analysis file path  
			ConfigThreadLocal.put(entry.getParams());
			monitor = new JeyzerMonitor(semaphore);
			try {
				monitor.init();
			} catch (JzrMonitorInitializationException ex) {
				logger.error("Failed to initialize the Monitor Configuration.", ex);
				System.exit(-1);
			}
		}
	}
	
	public static class JeyzerMonitorThreadFactory implements ThreadFactory {
		
		private String name;
		
		public JeyzerMonitorThreadFactory(String name){
			this.name = name;
		}
		
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName(name);
			t.setDaemon(false);
			return t;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JeyzerMultiMonitor multiMonitor = new JeyzerMultiMonitor();
		multiMonitor.init();
		multiMonitor.monitor();
	}	
	
}
