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







import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jeyzer.analyzer.JeyzerAnalyzer;
import org.jeyzer.analyzer.config.ConfigAnalyzer;
import org.jeyzer.analyzer.config.setup.ConfigSetupManager;
import org.jeyzer.analyzer.data.AnalysisContext;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrNoThreadDumpFileFound;
import org.jeyzer.analyzer.output.graph.motion.ContentionGraphPlayer;
import org.jeyzer.analyzer.output.graph.motion.FunctionGraphPlayer;
import org.jeyzer.analyzer.session.JzrMonitorErrorSession;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.analyzer.setup.JzrSetupManager;
import org.jeyzer.monitor.config.ConfigMonitorConsole;
import org.jeyzer.monitor.util.MonitorHelper;
import org.jeyzer.service.JzrServiceManager;
import org.jeyzer.service.action.id.ActionIdAnalyzerGenerator;
import org.jeyzer.service.action.id.ActionIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JeyzerMonitorConsole implements Runnable {
	
	private static final String NAME = "Jeyzer Monitor Console";
	
	public static final Logger logger = LoggerFactory.getLogger(JeyzerMonitorConsole.class);
	
	private ConfigAnalyzer analyzerCfg;
	private ConfigMonitorConsole monitorConsoleCfg;
	
	private JeyzerAnalyzer analyzer;
	private FunctionGraphPlayer functionPlayer;
	private ContentionGraphPlayer contentionPlayer;
	private ActionIdGenerator actionIdGenerator = new ActionIdAnalyzerGenerator();
	private JzrSetupManager setupMgr = null;
	private JzrServiceManager serviceMgr = null;

	private Date previous;

	public void init(){
		
		// load analyzer config
		try{
			this.analyzerCfg = new ConfigAnalyzer();
		}
		catch(Exception ex){
			abort(ex,"Failed to load the Jeyzer Analyzer configuration.");
		}
		
		// load setup configuration
		File setupConfigFile = new File(this.analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_SETUP_CONFIG_FILE));
		try{

			ConfigSetupManager tdSetupCfg = new ConfigSetupManager(setupConfigFile);
			this.setupMgr = new JzrSetupManager(tdSetupCfg);
		}
		catch(Exception ex){
			abort(ex,"Failed to load the Jeyzer setup configuration.");
		}
		
		// load Jeyzer service config
		try {
			this.serviceMgr = new JzrServiceManager(this.actionIdGenerator, this.setupMgr, this.analyzerCfg);
		} catch (JzrInitializationException ex) {
			abort(ex,"Failed to load the Jeyzer Service configuration.");
		}		
		
		// load Jeyzer Monitor Console config
		try{
			monitorConsoleCfg = new ConfigMonitorConsole();
		}
		catch(Exception ex){
			abort(ex,"Failed to load the Jeyzer Monitor Console configuration.");
		}
			
		// start date
		previous = new Date(); // default now
		
		try {
			functionPlayer= new FunctionGraphPlayer(
					monitorConsoleCfg.getFunctionGraphPlayerCfg(), 
					analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_APPLICATION_ID), 
					analyzerCfg.getThreadDumpPeriod());
		} catch (JzrInitializationException ex) {
			abort(ex,"Failed to load the monitor function graph player.");
		}
		
		try {
			contentionPlayer = new ContentionGraphPlayer(
					monitorConsoleCfg.getContentionGraphPlayerCfg(), 
					analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_APPLICATION_ID), 
					analyzerCfg.getThreadDumpPeriod());
		} catch (JzrInitializationException ex) {
			abort(ex,"Failed to load the monitor contention graph player.");
		}		
	}
	
	public long getPeriod(){
		return this.analyzerCfg.getThreadDumpPeriod();
	}
	
	protected void abort(Exception ex, String msg){
		logger.error(msg, ex);
		System.exit(-1);
	}	

	protected JzrMonitorSession analyze() {
		JzrMonitorSession session;
		AnalysisContext analysisCtx = new AnalysisContext(NAME);
	
		try {		
			analyzer = new JeyzerAnalyzer(analysisCtx, this.analyzerCfg, this.setupMgr, this.serviceMgr);
			analyzer.init();
			
			logger.info("Thread dump analysis starting.");
			
			// analyze thread dumps
			analyzer.loadSince(this.previous);
			analyzer.applyRules();
			analyzer.updateLocks();
			analyzer.updateActions();
			
			session = analyzer.getSession();
			logger.info("Thread dump analysis done.");
			analyzer.close();
			
			// free memory
			analyzer = null;
		} catch (JzrNoThreadDumpFileFound ex) {
			logger.warn("Thread dump not found.");
			session = new JzrMonitorErrorSession(previous, new Date(), ex);
		} catch (Exception ex) {
			logger.error("Failed to analyze the thread dump.", ex);
			session = new JzrMonitorErrorSession(previous, new Date(), ex);
		}
		
		return session;
	}
	
	public void monitor(){
		JzrMonitorSession session;
			
		try {

			logger.info("Thread dump monitoring starting.");
			
			// 1. perform standard TD analysis on 1 thread dump
			session = analyze();

			// 2. update the function graph
			logger.info("Generating the action graph.");
			if (session != null && !session.getDumps().isEmpty()){
				List<ThreadDump> dumps = session.getDumps();
				int last = dumps.size()-1;
				functionPlayer.play(dumps.get(last)); // always take the last one
				contentionPlayer.play(dumps.get(last));
				if (dumps.size()>1)
					logger.warn("Session contains more than one dump. Not expected. Size : " + dumps.size());
			}else{
				functionPlayer.displayNoThreadDump();
				contentionPlayer.displayNoThreadDump();
			}
			// always generate a snapshot image and HTML container page
			functionPlayer.snapshot(session);
			contentionPlayer.snapshot(session);

			// 3. Update previous date
			this.previous = new Date(this.previous.getTime() + this.analyzerCfg.getThreadDumpPeriod()*1000L);
			
			// 4. Print memory statistics
			MonitorHelper.displayMemoryUsage();
				
			// free memory
			session = null;
			System.gc();
				
			// 5. wait for period
			logger.info("Graph monitor refresh done.");
			logger.info("Next graph update in " + analyzerCfg.getThreadDumpPeriod() + " seconds...");
				
		}catch(Exception ex){
			abort(ex,"Jeyzer graph monitor internal error. Abort.");
		}
		
	}

	@Override
	public void run() {
		this.monitor();	
	}
		
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		JeyzerMonitorConsole monitorConsole = new JeyzerMonitorConsole();
		monitorConsole.init();
		
		// start the periodic monitoring
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleWithFixedDelay(monitorConsole, 0, monitorConsole.getPeriod(), TimeUnit.SECONDS);
	}

}
