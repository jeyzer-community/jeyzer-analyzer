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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.jeyzer.analyzer.JeyzerAnalyzer;
import org.jeyzer.analyzer.config.ConfigAnalyzer;
import org.jeyzer.analyzer.config.ConfigThreadLocal;
import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.config.graph.ConfigContentionGraphPlayer;
import org.jeyzer.analyzer.config.graph.ConfigFunctionGraphPlayer;
import org.jeyzer.analyzer.config.graph.ConfigGraphPlayer;
import org.jeyzer.analyzer.config.setup.ConfigSetupManager;
import org.jeyzer.analyzer.data.AnalysisContext;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrMonitorInitializationException;
import org.jeyzer.analyzer.output.ReportDescriptor;
import org.jeyzer.analyzer.output.graph.motion.ContentionGraphPlayer;
import org.jeyzer.analyzer.output.graph.motion.FunctionGraphPlayer;
import org.jeyzer.analyzer.output.graph.motion.GraphSnapshot;
import org.jeyzer.analyzer.session.JzrMonitorErrorSession;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.analyzer.setup.JzrSetupManager;
import org.jeyzer.analyzer.util.SystemHelper;
import org.jeyzer.monitor.config.ConfigMonitor;
import org.jeyzer.monitor.config.ConfigMonitorSession;
import org.jeyzer.monitor.config.applicative.ConfigMonitorApplicativeRuleManager;
import org.jeyzer.monitor.config.engine.ConfigMonitorRules;
import org.jeyzer.monitor.config.publisher.ConfigMailPublisher;
import org.jeyzer.monitor.config.publisher.ConfigWebPublisher;
import org.jeyzer.monitor.config.sticker.ConfigStickers;
import org.jeyzer.monitor.engine.event.MonitorAnalyzerEvent;
import org.jeyzer.monitor.engine.event.MonitorEvent;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.Level;
import org.jeyzer.monitor.engine.rule.MonitorAnalyzerRule;
import org.jeyzer.monitor.impl.rule.MonitorRuleBuilder;
import org.jeyzer.monitor.publisher.Publisher;
import org.jeyzer.monitor.publisher.PublisherBuilder;
import org.jeyzer.monitor.report.MonitorLogger;
import org.jeyzer.monitor.report.MonitorLoggerBuilder;
import org.jeyzer.monitor.sticker.StickerBuilder;
import org.jeyzer.monitor.util.MonitorHelper;
import org.jeyzer.service.JzrServiceManager;
import org.jeyzer.service.action.id.ActionIdMonitorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;


public class JeyzerMonitor implements Runnable{
	
	private static final String NAME = "Jeyzer Monitor";
	
	public static final Logger logger = LoggerFactory.getLogger(JeyzerMonitor.class);
	
	public static final int ONESHOT_SCANNING = -1;
	
	private ConfigAnalyzer analyzerCfg = null;
	private ConfigMonitor monitorCfg = null;
	private JeyzerAnalyzer analyzer = null;
	private JzrSetupManager setupMgr = null;
	private JzrServiceManager serviceMgr = null;
	private Map<String, String> localProps;
	private ActionIdMonitorGenerator actionIdGenerator = new ActionIdMonitorGenerator();
	
	private Duration period;
	private boolean oneshotMonitoring;
	private boolean persistPreviousDate;
	private Date previous;
	
	private boolean duplicateEventCleanup;
	
	private StickerBuilder stickerBuilder;
	
	private MonitorRuleBuilder ruleBuilder;
	private List<MonitorAnalyzerRule> analyzerRules;
	
	private ConfigMonitorApplicativeRuleManager applicativeRulesManager;
	private boolean publisherRulesAllowed;
	
	private Multimap<String, MonitorTaskEvent> taskEvents;
	private Multimap<String, MonitorSessionEvent> sessionEvents;
	private Multimap<String, MonitorSystemEvent> systemEvents;
	private Multimap<String, MonitorAnalyzerEvent> analyzerEvents;
		
	// JZR report
	private boolean analysisReportEnabled = false;
	private List<String> analysisReportPublisherNames;
	private Level analysisReportEventThreshold;
	
	// graph snapshot
	private boolean graphSnapshotEnabled = false;
	private List<String> graphSnapshotPublisherNames;
	private FunctionGraphPlayer functionPlayer; // can be null
	private ContentionGraphPlayer contentionPlayer; // can be null
	
	private Map<String, List<String>> publisherPaths = new HashMap<>();
	
	private List<MonitorLogger> loggers;
	private List<Publisher> publishers;
	
	private ConfigMonitorSession configSession;

	private Semaphore semaphore; // can be null
	
	public JeyzerMonitor() {
		this.semaphore = null;
	}
	
	// multi-monitor mode constructor
	public JeyzerMonitor(Semaphore semaphore) {
		this.semaphore = semaphore;
	}

	@SuppressWarnings("unchecked")
	public void init() throws JzrMonitorInitializationException{
		
		// load Jeyzer analyzer configuration
		try{
			this.analyzerCfg = new ConfigAnalyzer();
		}
		catch(Exception ex){
			throw new JzrMonitorInitializationException("Failed to load the Jeyzer Analyzer configuration.", ex);
		}
		
		// Analyzer configuration must be in production state
		if (!this.analyzerCfg.isProductionReady())
			throw new JzrMonitorInitializationException("Failed to load the Jeyzer Analyzer configuration : analyzer configuration must be in production state. "
					+ "Current state is : " + this.analyzerCfg.getState().getDisplayValue());

		// load Jeyzer setup configuration
		File setupConfigFile = new File(this.analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_SETUP_CONFIG_FILE));
		try{

			ConfigSetupManager tdSetupCfg = new ConfigSetupManager(setupConfigFile);
			this.setupMgr = new JzrSetupManager(tdSetupCfg);
		}
		catch(Exception ex){
			throw new JzrMonitorInitializationException("Failed to load the Jeyzer setup configuration.", ex);
		}
		
		// load Jeyzer service configuration
		try {
			this.serviceMgr = new JzrServiceManager(this.actionIdGenerator, this.setupMgr, this.analyzerCfg);
		} catch (JzrInitializationException ex) {
			throw new JzrMonitorInitializationException("Failed to load the Jeyzer Service configuration.", ex);
		}
		
		// keep copy of thread local properties as analysis will be performed in different thread
		localProps = ConfigThreadLocal.getProperties();
		
		// load Jeyzer monitor configuration
		try{
			this.monitorCfg = new ConfigMonitor(this.analyzerCfg, this.serviceMgr);
		}
		catch(Exception ex){
			throw new JzrMonitorInitializationException("Failed to load the Jeyzer Monitor configuration.", ex);
		}

		try {
			checkRecordingDirectory();
			
			String scanPeriod = (String)this.monitorCfg.getValue(ConfigMonitor.JZRM_MONITOR_SCAN_PERIOD);
			try{
				oneshotMonitoring = Long.parseLong(scanPeriod) == ONESHOT_SCANNING;
			}catch (NumberFormatException ex){
				oneshotMonitoring = false;
			}
			period = ConfigUtil.parseDuration(scanPeriod); // Will be -1 sec if disabled
			
			stickerBuilder = new StickerBuilder(
					(ConfigStickers)this.monitorCfg.getValue(ConfigStickers.JZRM_STICKERS)
					);
			
			// load the monitoring rules
			ConfigMonitorRules cfgMonitorRules = (ConfigMonitorRules)this.monitorCfg.getValue(ConfigMonitorRules.JZRM_RULES);
			
			ruleBuilder = new MonitorRuleBuilder(cfgMonitorRules, this.setupMgr);
			analyzerRules = ruleBuilder.getAnalyzerRules();
			
			applicativeRulesManager = cfgMonitorRules.getApplicativeRuleManager();
			publisherRulesAllowed = cfgMonitorRules.isPublisherRulesAllowed();
			
			// monitoring events to be filled
			taskEvents = LinkedListMultimap.create();
			systemEvents = LinkedListMultimap.create();
			sessionEvents = LinkedListMultimap.create();
			analyzerEvents = LinkedListMultimap.create();
			duplicateEventCleanup = Boolean.parseBoolean((String)monitorCfg.getValue(ConfigMonitor.JZRM_CLEAN_DUPLICATE_EVENTS));

			// loggers
			loggers = MonitorLoggerBuilder.newInstance().buildLoggers(monitorCfg);
			
			// publishers
			publishers = PublisherBuilder.newInstance().buildPublishers(monitorCfg);
			
			// JZR report
			analysisReportEnabled = Boolean.parseBoolean((String)monitorCfg.getValue(ConfigMonitor.JZRM_ANALYSIS_REPORT_ENABLED));
			analysisReportPublisherNames =  (List<String>)monitorCfg.getValue(ConfigMonitor.JZRM_ANALYSIS_REPORT_PUBLISHERS);
			analysisReportEventThreshold = Level.getLevel((String)monitorCfg.getValue(ConfigMonitor.JZRM_ANALYSIS_REPORT_THRESHOLD));
			if (Level.UNKNOWN.equals(analysisReportEventThreshold))
				analysisReportEventThreshold = Level.CRITICAL; // default
			
			// session
			configSession = new ConfigMonitorSession(
					(String)monitorCfg.getValue(ConfigMonitor.JZRM_SESSION_DIR), 
					analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_APPLICATION_ID)
					);
			
			// start date
			previous = SystemHelper.getUnixEpochDate(); // default
			persistPreviousDate = Boolean.parseBoolean((String)monitorCfg.getValue(ConfigMonitor.JZRM_STARTUP_POINT_PERSISTED));
			if (persistPreviousDate){
				String previousDateValue = configSession.getProperty(ConfigMonitorSession.JZRM_PREVIOUS_SESSION_DATE);
				if (previousDateValue != null){
					previous = MonitorHelper.parseLocalDate(previousDateValue);
				}
			}
			
			// graph player to generate a graph snapshot at the end of each scanning
			graphSnapshotEnabled = Boolean.parseBoolean((String)monitorCfg.getValue(ConfigMonitor.JZRM_GRAPH_SNAPSHOT_ENABLED));
			graphSnapshotPublisherNames =  (List<String>)monitorCfg.getValue(ConfigMonitor.JZRM_GRAPH_SNAPSHOT_PUBLISHERS);
			if (this.monitorCfg.getValue(ConfigGraphPlayer.JZRA_FUNCTION_GRAPH_PLAYER) != null)
				functionPlayer = new FunctionGraphPlayer(
					(ConfigFunctionGraphPlayer)this.monitorCfg.getValue(ConfigGraphPlayer.JZRA_FUNCTION_GRAPH_PLAYER), 
					getNodeName(),
					analyzerCfg.getThreadDumpPeriod());
			if (this.monitorCfg.getValue(ConfigGraphPlayer.JZRA_CONTENTION_GRAPH_PLAYER) != null)
				contentionPlayer = new ContentionGraphPlayer(
					(ConfigContentionGraphPlayer)this.monitorCfg.getValue(ConfigGraphPlayer.JZRA_CONTENTION_GRAPH_PLAYER), 
					getNodeName(),
					analyzerCfg.getThreadDumpPeriod());
		} catch (Exception ex) {
			throw new JzrMonitorInitializationException("Failed to initialize Jeyzer Monitor.", ex);
		}
	}

	protected void abort(Exception ex, String msg){
		if (ex != null)
			logger.error(msg, ex);
		else
			logger.error(msg);
		System.exit(-1);
	}	

	protected JzrMonitorSession analyze() {
		JzrMonitorSession session;
		AnalysisContext analysisCtx = new AnalysisContext(NAME);
	
		try {
			analyzer = new JeyzerAnalyzer(analysisCtx, this.analyzerCfg, this.setupMgr, this.serviceMgr);
			analyzer.init();
			
			logger.info("Jeyzer analysis starting.");
			
			// analyze the recording snapshots
			analyzer.loadSince(this.previous);
			analyzer.applyRules();
			analyzer.updateLocks();
			analyzer.updateActions();
			
			session = analyzer.getSession();
			logger.info("Jeyzer analysis done.");
			
		} catch (JzrInitializationException ex) {
			logger.error("Analysis failed. Error is : " + ex.getMessage());
			// any monitoring/analysis exception = internal error or no thread dump found --> generate event
			session = new JzrMonitorErrorSession(previous, new Date(), ex);
		} catch (Exception ex) {
			// any monitoring/analysis exception = internal error or no thread dump found --> generate event
			session = new JzrMonitorErrorSession(previous, new Date(), ex);
		}
		
		return session;
	}
	
	public MonitorResult monitor() throws Exception{
		JzrMonitorSession session = null;
		MonitorResult result = null;
			
		try {
			// in multi-monitor mode, wait for execution slot
			if (semaphore != null)
				semaphore.acquire();
			
			long startTime = System.currentTimeMillis();
			
			ConfigThreadLocal.putAll(localProps);

			logger.info("Jeyzer monitoring starting.");
			
			try
			{
				// 1. perform standard TD analysis
				session = analyze(); 
				
				// todo : previous open action events must be updated (end date + in progress) 
				//  as it is not done automatically by the thresholds
				
				// 2 - add the dynamic rules and stickers
				if (session instanceof JzrSession) {
					this.ruleBuilder.loadDynamicRules((JzrSession)session, this.serviceMgr.getResourcePathResolver());
					this.stickerBuilder.loadDynamicStickers((JzrSession)session, this.serviceMgr.getResourcePathResolver());
					this.stickerBuilder.addStickers(setupMgr.getMonitorSetupManager().getStickers((JzrSession)session, this.serviceMgr.getResourcePathResolver()));
				}
	
				// 3. apply the rules on the current session
				session.applyMonitorStickers(ruleBuilder.getAllRules(), this.stickerBuilder.getStickers());
				session.applyMonitorTaskRules(ruleBuilder.getTaskRules(), taskEvents, applicativeRulesManager.getApplicativeTaskRuleFilter());
				session.applyMonitorSessionRules(ruleBuilder.getSessionRules(), sessionEvents, applicativeRulesManager.getApplicativeSessionRuleFilter(), publisherRulesAllowed);
				session.applyMonitorSystemRules(ruleBuilder.getSystemRules(), systemEvents, applicativeRulesManager.getApplicativeSystemRuleFilter());
				session.applyMonitorAnalyzerRules(analyzerRules, analyzerEvents);
	
				logger.info("Monitoring events generation done.");
	
				// 4. keep date of the last file. In case of error/empty session, keep as it is
				if (session instanceof JzrSession)
					this.previous = new Date(session.getEndDate().getTime() + 2000L); // Add 2 sec to not include previous last file
	
				List<MonitorEvent> sortedEvents = MonitorHelper.buildElectedEventSortedList(
						taskEvents, 
						sessionEvents, 
						systemEvents, 
						analyzerEvents,
						duplicateEventCleanup);
				
				// 5. Generate the JZR report
				if (isReportGenerationRequested(session, sortedEvents))
					generateReport();
	
				// 6. Generate the graph picture
				if (graphSnapshotEnabled)
					generateSnapshot(session);
				
				// 7. Log the elected events
				logger.info("Logging monitoring events.");
				for (MonitorLogger eventLogger : this.loggers){
					eventLogger.logEvents(session, sortedEvents, publisherPaths);
				}
				
				// 8. Publish the events
				logger.info("Publishing monitoring events.");
				for (Publisher publisher : publishers){
					publisher.publish(session, sortedEvents, publisherPaths);
				}
				
				// 9. set the code result
				if(isOneshotMonitoring())
					result = new MonitorResult(
							!sortedEvents.isEmpty(), 
							getResultCode(sortedEvents), 
							publisherPaths);
					
				// 10. clean monitoring events
				cleanupEvents(session.getEndDate());
				
				// 11. cache action ids of the ongoing task events
				this.actionIdGenerator.cacheActionIds(taskEvents);
					
				// 12. persist previous
				if (this.persistPreviousDate)
					persistPrevious(this.previous);
	
				// 13. Print memory statistics
				MonitorHelper.displayMemoryUsage();
				long endTime = System.currentTimeMillis();
				logger.info("Monitoring duration : " + MonitorHelper.getPrintableDuration(endTime - startTime));
				
			}catch(Exception ex){
				throw ex;
			}
			finally{
				// 14. free memory and release resources
				if (session != null)
					session.close();
				session = null;
				publisherPaths.clear();
				analyzer = null;
				ConfigThreadLocal.empty();
				System.gc();
			}

			logger.info("Jeyzer monitoring done.");
			
			// 15. wait for period
			if(!isOneshotMonitoring())
				logger.info("Next monitoring in " + period.getSeconds() + " seconds...");
			
		}catch(Exception ex){
			if (isOneshotMonitoring())
				throw ex;
			else
				abort(ex,"Jeyzer Monitor internal error. Abort.");
		}
		finally{
			// in multi-monitor mode, release the permit
			if (semaphore != null)
				semaphore.release();
		}
		
		return result;
	}
	
	private void checkRecordingDirectory() throws JzrMonitorInitializationException {
		String path = this.analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_JEYZER_RECORD_DIRECTORY).replace('\\', '/');
		if (path == null || path.isEmpty()){
			logger.error("Recording directory is mandatory. Please set it.");
			throw new JzrMonitorInitializationException("Recording directory is mandatory. Please set it."); 
		}
		
		File dir = new File(path);
		if (!dir.exists()){
			logger.warn("Recording directory not found. Monitor will now create it. Path is : " + dir.getPath());
			boolean result = dir.mkdirs();
			if (!result)
				throw new JzrMonitorInitializationException("failed to create the recording directory : " + dir.getPath()); 
		}
	}

	private void generateSnapshot(JzrMonitorSession session) {
		if (functionPlayer != null)
			generateFunctionSnapshot(session);
		if (contentionPlayer != null)
			generateContentionSnapshot(session);
	}

	private void generateContentionSnapshot(JzrMonitorSession session) {
		logger.info("Generating the contention graph.");
		
		if (session != null && !session.getDumps().isEmpty()){
			for (ThreadDump dump : session.getDumps())
				contentionPlayer.play(dump);
		}else{
			contentionPlayer.displayNoThreadDump();
		}
		
		GraphSnapshot snapshot = contentionPlayer.snapshot(session);
		if (snapshot != null){
			if (snapshot.getPicturePath()!= null)
				updatePublisherPaths(graphSnapshotPublisherNames, snapshot.getPicturePath());
			updatePublisherPaths(graphSnapshotPublisherNames, snapshot.getHtmlPath());
		}
	}

	private void generateFunctionSnapshot(JzrMonitorSession session) {
		logger.info("Generating the action graph.");
		
		if (session != null && !session.getDumps().isEmpty()){
			for (ThreadDump dump : session.getDumps())
				functionPlayer.play(dump);
		}else{
			functionPlayer.displayNoThreadDump();
		}
		
		GraphSnapshot snapshot = functionPlayer.snapshot(session);
		if (snapshot != null){
			if (snapshot.getPicturePath()!= null)
				updatePublisherPaths(graphSnapshotPublisherNames, snapshot.getPicturePath());
			updatePublisherPaths(graphSnapshotPublisherNames, snapshot.getHtmlPath());
		}
	}

	private Level getResultCode(List<MonitorEvent> events) {
		if (events.isEmpty()){
			return Level.INFO; // all is ok
		}
		else if (MonitorHelper.isEventCategoryMatched(events, Level.ERROR)){
			return Level.ERROR;
		}
		else if (MonitorHelper.isEventCategoryMatched(events, Level.CRITICAL)){
			return Level.CRITICAL;
		}
		else if (MonitorHelper.isEventCategoryMatched(events, Level.WARNING)){
			return Level.WARNING;
		}
		else if (MonitorHelper.isEventCategoryMatched(events, Level.INFO)){
			return Level.INFO;
		}
		else{
			return Level.UNKNOWN;
		}
	}

	private void generateReport() throws JzrException {
		// generate JZR report
		ReportDescriptor reportDesc = new ReportDescriptor();
		analyzer.report(reportDesc);
		
		if (reportDesc!= null && !analysisReportPublisherNames.isEmpty())
			updatePublisherPaths(analysisReportPublisherNames, reportDesc.getReportPath());
	}

	private void updatePublisherPaths(List<String> publisherNames, String path) {
		for(String publisherName : publisherNames){
			List<String> paths = this.publisherPaths.computeIfAbsent(publisherName, p -> new ArrayList<String>());
			paths.add(path);
		}
	}

	private boolean isReportGenerationRequested(JzrMonitorSession session, List<MonitorEvent> sortedEvents) {
		if (!analysisReportEnabled)
			return false;

		if (session.getDumps().isEmpty())
			return false;  // no recording snapshot found
		
		if (MonitorHelper.isEventThresholdMatched(sortedEvents, analysisReportEventThreshold, false)){
			logger.info("Event of category " + analysisReportEventThreshold + " at least detected : generate report.");
			return true;
		}
		
		return false;
	}
	
	public boolean isOneshotMonitoring() {
		return oneshotMonitoring;
	}

	@Override
	public void run() {
		try{
			this.monitor();
		}
		catch(Throwable ex){
			logger.error("Failed to monitor the application : " 
					+ this.getApplicationType() 
					+ "-" 
					+ this.getNodeName() 
					+ ". Error is : "
					+ ex.getMessage());
		}
	}

	private void persistPrevious(Date previous){
		String value = MonitorHelper.formatLocalDate(previous);
		this.configSession.setProperty(ConfigMonitorSession.JZRM_PREVIOUS_SESSION_DATE, value);
		this.configSession.saveSession();
	}	
	
	/**
	 * Remove events that ended before the session end date and set remaining events as published 
	 * @param sessionEndDate 
	 */
	private void cleanupEvents(Date sessionEndDate){
		List<MonitorEvent> candidates = new ArrayList<>();
		for (MonitorEvent event : systemEvents.values()){
			if (sessionEndDate.after(event.getEndDate())){
				candidates.add(event);
			}
			event.publish();
		}
		for (MonitorEvent monitorEventToRemove : candidates){
			systemEvents.remove(monitorEventToRemove.getId(), monitorEventToRemove);
		}
		
		for (MonitorEvent event : sessionEvents.values()){
			if (sessionEndDate.after(event.getEndDate())){
				candidates.add(event);
			}
			event.publish();
		}
		for (MonitorEvent monitorEventToRemove : candidates){
			sessionEvents.remove(monitorEventToRemove.getId(), monitorEventToRemove);
		}
		
		candidates.clear();
		for (MonitorEvent event : taskEvents.values()){
			if (sessionEndDate.after(event.getEndDate())){
				candidates.add(event);
			}
			event.publish();
		}
		for (MonitorEvent monitorEventToRemove : candidates){
			taskEvents.remove(monitorEventToRemove.getId(), monitorEventToRemove);
		}
		
		candidates.clear();
		for (MonitorEvent event : analyzerEvents.values())
			event.publish();
		
		// Analysis events are one shot, not maintained. 
		// Those will be regenerated if issue is still present
		analyzerEvents.clear();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		JeyzerMonitor monitor = new JeyzerMonitor();
		try {
			monitor.init();
		} catch (JzrMonitorInitializationException ex) {
			monitor.abort(ex,"Failed to initialize Jeyzer Monitor. Abort.");
		}

		if (monitor.getPeriod().getSeconds() < 60)
			monitor.abort(null, "Failed to initialize Jeyzer Monitor. Monitoring period is not set with valid value. Must be greater than 60 sec. Current value is : " + monitor.getPeriod().getSeconds() + " sec");
		
		// start the periodic monitoring
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleWithFixedDelay(monitor, 0, monitor.getPeriod().getSeconds(), TimeUnit.SECONDS);
	}
	
	public Duration getPeriod(){
		return this.period;
	}

	public String getNodeName() {
		return (String)this.monitorCfg.getValue(ConfigMonitor.JZRM_NODE);
	}

	public String getApplicationType() {
		return this.analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_APPLICATION_TYPE);
	}

	public boolean isAnalysisReportEnabled() {
		return analysisReportEnabled;
	}
	
	public static class MonitorResult{
		
		private boolean eventDetected;
		private Level top = Level.UNKNOWN;
		private List<String> docPaths = new ArrayList<>();
		private List<String> webPaths = new ArrayList<>();
		
		public MonitorResult(boolean eventDetected, Level category, Map<String, List<String>> publisherPaths){
			this.eventDetected = eventDetected;
			this.top = category;
			if (publisherPaths.get(ConfigMailPublisher.NAME) != null)
				this.docPaths.addAll(publisherPaths.get(ConfigMailPublisher.NAME));
			if (publisherPaths.get(ConfigWebPublisher.NAME) != null)
				this.webPaths.addAll(publisherPaths.get(ConfigWebPublisher.NAME));
		}

		public Level getTopCategory() {
			return top;
		}

		public List<String> getDocPaths() {
			return docPaths;
		}
		
		public List<String> getWebPaths() {
			return webPaths;
		}
		
		public boolean isEventDetected() {
			return this.eventDetected;
		}
	}
}
