package org.jeyzer.analyzer;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jeyzer.analyzer.communication.AnalysisMailer;
import org.jeyzer.analyzer.config.ConfigAnalyzer;
import org.jeyzer.analyzer.config.ConfigReport;
import org.jeyzer.analyzer.config.patterns.ConfigAggregatorPatterns;
import org.jeyzer.analyzer.config.patterns.ConfigPatterns;
import org.jeyzer.analyzer.config.setup.ConfigSetupManager;
import org.jeyzer.analyzer.data.AnalysisContext;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.error.JzrExecutionException;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrMasterProfileRedirectException;
import org.jeyzer.analyzer.output.GraphReplayer;
import org.jeyzer.analyzer.output.JZRReport;
import org.jeyzer.analyzer.output.MusicGenerator;
import org.jeyzer.analyzer.output.ReportDescriptor;
import org.jeyzer.analyzer.output.ReportHelper;
import org.jeyzer.analyzer.rule.AnalysisPreFilter;
import org.jeyzer.analyzer.rule.Rule;
import org.jeyzer.analyzer.rule.RuleBuilder;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.analyzer.setup.JzrSetupManager;
import org.jeyzer.analyzer.status.JeyzerStatusEvent;
import org.jeyzer.analyzer.status.JeyzerStatusEventDispatcher;
import org.jeyzer.analyzer.status.JeyzerStatusListener;
import org.jeyzer.monitor.engine.event.info.Level;
import org.jeyzer.monitor.util.MonitorHelper;
import org.jeyzer.service.JzrServiceManager;
import org.jeyzer.service.action.id.ActionIdAnalyzerGenerator;
import org.jeyzer.service.location.JzrLocationResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JeyzerAnalyzer {

	private static final String NAME = "Jeyzer Analyzer";
	
	private static final Logger logger = LoggerFactory.getLogger(JeyzerAnalyzer.class);
	private static final  String FAILURE_MESSAGE = "Jeyzer analysis failed : ";
	
	private ConfigAnalyzer cfg = null;
	private ConfigPatterns patternsCfg = null;
	private ConfigReport reportCfg = null;
	
	private JzrSession session = null;
	private JzrSetupManager setupMgr = null;
	private JzrServiceManager serviceMgr = null;
	
	private AnalysisContext analysisContext;
	
	private JeyzerStatusEventDispatcher eventDispatcher = new JeyzerStatusEventDispatcher(); // can be overriden
		
	public JeyzerAnalyzer(AnalysisContext context) throws JzrInitializationException{
		this.analysisContext = context;
		
		// load the Analyzer configuration
		try{
			this.cfg = new ConfigAnalyzer();
		}
		catch(Exception ex){
			logger.error("Failed to load the Jeyzer Analyzer configuration.");
			throw new JzrInitializationException("Failed to load the Jeyzer Analyzer configuration.", ex);
		}
		
		// load the Jeyzer setup configuration
		File setupConfigFile = new File(this.cfg.getParamValue(ConfigAnalyzer.JZRA_SETUP_CONFIG_FILE));
		try{
			ConfigSetupManager tdSetupCfg = new ConfigSetupManager(setupConfigFile);
			this.setupMgr = new JzrSetupManager(tdSetupCfg);
		}
		catch(Exception ex){
			throw new JzrInitializationException("Failed to load the Jeyzer setup configuration.", ex);
		}
		
		// load the Jeyzer service configuration
		try{
			this.serviceMgr = new JzrServiceManager(new ActionIdAnalyzerGenerator(), this.setupMgr, this.cfg);
		}
		catch(Exception ex){
			throw new JzrInitializationException("Failed to load the Jeyzer Service configuration.", ex);
		}
	}
	
	public JeyzerAnalyzer(AnalysisContext context, ConfigAnalyzer analyzerCfg, JzrSetupManager setupMgr, JzrServiceManager serviceMgr){
		this.analysisContext = context;
		this.cfg = analyzerCfg;
		this.setupMgr = setupMgr;
		this.serviceMgr = serviceMgr;
	}
	
	protected void load() throws JzrInitializationException, JzrExecutionException{
		session = new JzrSession(this.setupMgr, this.cfg, null, this.eventDispatcher, this.analysisContext);
		session.build(this.cfg);
	}

	public void loadSince(Date date)  throws JzrInitializationException, JzrExecutionException{
		session = new JzrSession(this.setupMgr, this.cfg, date, this.eventDispatcher, this.analysisContext);
		session.build(this.cfg);
	}	
	
	public JzrSession getSession(){
		return this.session;
	}

	public void setStatusEventDispatcher(JeyzerStatusEventDispatcher dispatcher){
		this.eventDispatcher = dispatcher;
	}	
	
	public void registerStatusListener(JeyzerStatusListener listener){
		eventDispatcher.registerListener(listener);
	}
	
	public void applyRules(){
		
		this.eventDispatcher.fireStatusEvent(JeyzerStatusEvent.STATE.DATA_PROCESSING);
		
		// update the patterns based on the JZR recording jar process info
		if (this.cfg.getDynamicPatternsLoadingCfg().isDynamicLoadingActive()) {
			List<String> paths = this.serviceMgr.getResourcePathResolver().resolveDynamicPatternsLocations(
					session.getProcessJars(),
					session.getProcessModules(),
					this.cfg.getDynamicPatternsLoadingCfg().isDeclaredRepositoryOnly()
					);
			((ConfigAggregatorPatterns)this.patternsCfg).loadDynamicPatterns(paths);
		}
		
		// create rules
		RuleBuilder builder = RuleBuilder.newInstance();
		AnalysisPreFilter filter = new AnalysisPreFilter(cfg.getPreFilterCfg(), session);
		List<Rule> rules = builder.buildRules(filter, patternsCfg, this.session);
		
		// apply rules on session
		this.session.applyRules(rules);
		this.session.setStackMinimumSize(filter.getStackMinimumSize());
		
		// filter restart
	}

	public void updateLocks(){
		this.session.updateLocks();
	}	
	
	public void updateActions(){
		this.session.updateActions(this.serviceMgr.getActionIdGenerator());
	}
	
	protected void dumpUFOThreads(ReportDescriptor desc){
		this.session.dumpUFOThreads(desc);
	}
	
	protected void dumpIgnoredStacks(){
		this.session.dumpIgnoredStacks();
	}
	
	public ReportDescriptor report(ReportDescriptor desc) throws JzrException{
		if (!this.reportCfg.isEnabled())
			return desc;
		
		// JMusic - Activated usually in easter egg mode in post mortem analysis only
		if (this.session.isPostMortemAnalysis()) {
			MusicGenerator music = MusicGenerator.buildMusicGenerator(
					this.reportCfg, 
					this.session.getApplicationId()
					);
			music.createSong(session);
			music.fillReportDescriptor(desc);			
		}
		
		// XLSX - POI
		if (this.reportCfg.getConfigXSLX() != null){
			JZRReport jzrReport; 
			
			jzrReport = new JZRReport(
					this.reportCfg.getConfigXSLX(),
					this.patternsCfg,
					this.session, 
					this.setupMgr,
					this.eventDispatcher);
			jzrReport.report();
			jzrReport.closeWorkBook();
			jzrReport.fillReportDescriptor(desc);

			return desc;
		}
		
		logger.error("Invalid report configuration : only Excel reports are supported.");
		return null;
	}


	protected void distributeReport(ReportDescriptor desc) {
		if (!this.reportCfg.isEnabled() || !this.cfg.getConfigMail().isEnabled() || !this.cfg.isProductionReady())
			return;
		
		Level threshold = Level.getLevel(this.cfg.getParamValue(ConfigAnalyzer.JZRA_NOTIFICATION_MONITORING_EVENT_THRESHOLD));
		if (Level.UNKNOWN.equals(threshold))
			return;
		
		if (this.session.getHighestEventCategory().isMoreCritical(threshold) < 0)
			return;
		
		logger.info("Monitoring event threshold "
				+ threshold 
				+ " reached : emailing the report to " 
				+ this.cfg.getConfigMail().getRecipientEmails());
		
		AnalysisMailer mailer = new AnalysisMailer(
				this.cfg.getConfigMail(), 
				this.session.getApplicationType()
				);
		
		// prepare attachments
		List<String> attachments = new ArrayList<>();
		attachments.add(desc.getReportPath());
		if (this.reportCfg.getConfigAudio() != null)
			attachments.add(desc.getMusicFilePath());
		
		// send email
		mailer.sendReport(attachments, this.session);
	}
	
	public void init() throws JzrInitializationException{
		JzrLocationResolver resolver = this.serviceMgr.getResourcePathResolver();
		
		// resolve the pattern set paths
		List<String> paths = null;
		try{
			paths = resolver.resolvePatternsLocations(this.cfg.getPatternsLocations());
		}
		catch(Exception ex){
			throw new JzrInitializationException("Analysis patterns configuration loading failed. Failed to resolve the pattern set locations.", ex);
		}
				
		// load the analysis patterns configuration
		try{
			this.patternsCfg = new ConfigAggregatorPatterns(paths);
		}
		catch(Exception ex){
			throw new JzrInitializationException("Failed to load the analysis patterns.", ex);
		}
		
		File reportConfigFile = new File(this.cfg.getParamValue(ConfigAnalyzer.JZRA_REPORT_CONFIG_FILE));
		if (!reportConfigFile.exists()){
			throw new JzrInitializationException("File " + reportConfigFile.getPath() + " not found.");
		}
		
		// load report configuration
		try{
			this.reportCfg = new ConfigReport(reportConfigFile, this.setupMgr, resolver);
		}
		catch(Exception ex){
			throw new JzrInitializationException("Failed to load " + reportConfigFile.getPath() + " configuration file", ex);
		}
	}

	public void close() {
		if (this.session != null)
			this.session.close();
	}
	
	public ReportDescriptor analyze() throws JzrException{
		ReportDescriptor desc = new ReportDescriptor();

		try {
			init();
			load();
			applyRules();
			dumpIgnoredStacks();
			updateLocks();
			updateActions();
			dumpUFOThreads(desc);
			report(desc);
			distributeReport(desc);
			replay();
		} catch (JzrMasterProfileRedirectException e) {
			logger.info("Jeyzer analysis ended up with a profile redirection");
			throw e;
		} catch (JzrException e) {
			logger.error(FAILURE_MESSAGE + e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error(FAILURE_MESSAGE + e.getMessage(), e);
			throw new JzrException(FAILURE_MESSAGE + e.getMessage(), e);
		}
		finally{
			close();
		}
		
		return desc;
	}


	protected void replay() throws JzrInitializationException {
		if (cfg.getReplayCfg() == null || !cfg.getReplayCfg().isEnabled())
			return;
		
		GraphReplayer replay = new GraphReplayer(
				cfg.getReplayCfg(), 
				this.session,
				this.cfg.getParamValue(ConfigAnalyzer.JZRA_APPLICATION_ID));
		replay.replay();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JeyzerAnalyzer analyzer = null;
		try {
			analyzer = new JeyzerAnalyzer(new AnalysisContext(NAME));
		} catch (Exception e) {
			logger.error("Failed to instanciate Jeyzer Analyzer", e);
			System.exit(-1);
		}
		long startTime = System.currentTimeMillis();
		
		try{
			analyzer.analyze();
			long endTime = System.currentTimeMillis();
			MonitorHelper.displayMemoryUsage();
			logger.info("Generation time : " + ReportHelper.getPrintableDuration(endTime - startTime));
			logger.info("Thread dump analysis done.");
		}catch(Exception ex){
			 logger.error(FAILURE_MESSAGE + ex.getMessage(), ex);
		}
	}
	
}
