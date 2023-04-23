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


import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;

import org.jeyzer.analyzer.JeyzerAnalyzer;
import org.jeyzer.analyzer.config.ConfigAnalyzer;
import org.jeyzer.analyzer.config.ConfigThreadLocal;
import org.jeyzer.analyzer.data.AnalysisContext;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.error.JzrMasterProfileRedirectException;
import org.jeyzer.analyzer.output.ReportDescriptor;
import org.jeyzer.analyzer.status.JeyzerStatusEvent;
import org.jeyzer.analyzer.status.JeyzerStatusEventDispatcher;
import org.jeyzer.web.JeyzerServlet;
import org.jeyzer.web.error.JzrMasterProfileRedirectFailureException;
import org.jeyzer.web.error.JzrWebException;
import org.jeyzer.web.util.TempFileManager;
import org.jeyzer.web.util.WorkingDirectoryIdManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JzrController implements Callable<ReportDescriptor>{
	
	private static final String NAME = "Jeyzer Web Analyzer";

	private static final Logger logger = LoggerFactory.getLogger(JzrController.class);
	
	public static class JzrControllerThreadFactory implements ThreadFactory {
		
		// guarded by class instance
		private static int nextId = 0;

		private static int getNextId() {
			synchronized (JzrControllerThreadFactory.class) {
				return nextId++;
			}
		}
		
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName("Jeyzer-controller #" + getNextId());
			t.setDaemon(false);
			return t;
		}
	}	
	
	// Required properties for the Jeyzer Analyzer
	protected static final String PROPERTY_JEYZER_TARGET_PROFILE = "JEYZER_TARGET_PROFILE";
	private static final String PROPERTY_JEYZER_ANALYSIS_ID = "JEYZER_ANALYSIS_ID";
	private static final String PROPERTY_JEYZER_RECORD_DIRECTORY = "JEYZER_RECORD_DIRECTORY";
	private static final String PROPERTY_JEYZER_RECORD_FILE = "JEYZER_RECORD_FILE";
	private static final String PROPERTY_JEYZER_OUTPUT_DIRECTORY = "JEYZER_OUTPUT_DIR";
	private static final String PROPERTY_JEYZER_TARGET_NAME = "JEYZER_TARGET_NAME";
	private static final String PROPERTY_JEYZER_RECORD_PERIOD = "JEYZER_RECORD_PERIOD";
	private static final String PROPERTY_JEYZER_DISPLAY_TIME_ZONE_ID_USER_SELECTED = "JEYZER_DISPLAY_TIME_ZONE_ID_USER_SELECTED"; // not defined by any external variable
	private static final String PROPERTY_JEYZER_DISPLAY_TIME_ZONE_ID = "JEYZER_DISPLAY_TIME_ZONE_ID";
	private static final String PROPERTY_JEYZER_RECORDING_TIME_ZONE_ID_USER_SELECTED = "JEYZER_RECORDING_TIME_ZONE_ID_USER_SELECTED"; // not defined by any external variable
	private static final String PROPERTY_JEYZER_RECORDING_TIME_ZONE_ID = "JEYZER_RECORDING_TIME_ZONE_ID";
	private static final String PROPERTY_JEYZER_ANALYZER_DISCOVERY_FUNCTIONS_PREFIX = "JEYZER_ANALYZER_DISCOVERY_FUNCTIONS_";
	private static final String PROPERTY_JEYZER_ANALYZER_DISCOVERY_FUNCTIONS_COLOR_PREFIX = "JEYZER_ANALYZER_DISCOVERY_FUNCTIONS_COLOR_";
	private static final String PROPERTY_JEYZER_TARGET_DESCRIPTION = "JEYZER_TARGET_DESCRIPTION";
	private static final String PROPERTY_JEYZER_TARGET_ISSUER = "JEYZER_TARGET_ISSUER";
	private static final String PROPERTY_JEYZER_REPORT_PASSWORD = "JEYZER_REPORT_PASSWORD";
	private static final String PROPERTY_JEYZER_ANALYZER_GENERATE_PROCESS_MUSIC = "JEYZER_ANALYZER_GENERATE_PROCESS_MUSIC";
	private static final String PROPERTY_JEYZER_JZR_REPORT_ENABLED = "JEYZER_JZR_REPORT_ENABLED";
	private static final String PROPERTY_JEYZER_TRANSLATOR_FILE_MAX_SIZE = "JEYZER_TRANSLATOR_FILE_MAX_SIZE";
	private static final String PROPERTY_JEYZER_TRANSLATOR_UNCOMPRESS_MAX_SIZE = "JEYZER_TRANSLATOR_UNCOMPRESS_MAX_SIZE";
	private static final String PROPERTY_JEYZER_TRANSLATOR_UNCOMPRESS_MAX_FILES = "JEYZER_TRANSLATOR_UNCOMPRESS_MAX_FILES";

	private JzrSetup analyzerSetup;
	private JeyzerStatusEventDispatcher eventDispatcher;
	
	private TempFileManager tempFileManager;
	private WorkingDirectoryIdManager workDirManager;
	
	private String tdRootDir;
	private String recordingFileName;
	
	private JzrAnalysisRequest request;

	public JzrController(
			JzrAnalysisRequest request, 
			JzrSetup analyzerSetup, 
			JeyzerStatusEventDispatcher eventDispatcher, 
			String tdRoorDir, 
			TempFileManager tempFileManager, 
			WorkingDirectoryIdManager workDirManager){
		this.request = request;
		this.analyzerSetup = analyzerSetup;
		this.eventDispatcher = eventDispatcher;
		this.tdRootDir = tdRoorDir;
		this.tempFileManager = tempFileManager;
		this.workDirManager = workDirManager;
	}

	@Override
	public ReportDescriptor call() throws Exception {
		try{
			return analyze(request);
		}
		catch(JzrMasterProfileRedirectException ex) {
			logger.info("Profile redirection requested from profile " + request.getProfile() + " to profile " + ex.getTargetRedirectProfile());
			JzrAnalysisRequest redirectRequest = new JzrAnalysisRequest(request, ex.getTargetRedirectProfile());
			try{
				return analyze(redirectRequest);
			}
			catch(JzrMasterProfileRedirectException ex2) {
				 // Redirect can only be done once
				String msg = "Profile redirection 2nd attempt has been prevented as it is not supported. "
						+ "Please check the chain of involved profiles : " 
						+ request.getProfile() + ", " 
						+ redirectRequest.getProfile() + ", "
						+ ex2.getTargetRedirectProfile();
				throw new JzrMasterProfileRedirectFailureException(msg); // will be processed as Throwable error in the UI
			}
		}
	}
	
	private ReportDescriptor analyze(JzrAnalysisRequest req) throws JzrWebException, JzrException {
		if (!req.isProfileRedirect())
			eventDispatcher.fireStatusEvent(JeyzerStatusEvent.STATE.INITIALIZING);
		else
			eventDispatcher.fireStatusEvent(JeyzerStatusEvent.STATE.PROFILE_REDIRECTION);
		
		Integer workingDirId = workDirManager.pollId(req.getProfile());
		String tdDir = this.tdRootDir + File.separatorChar + req.getProfile() + File.separatorChar + req.getNodeName() + File.separatorChar + workingDirId;
		req.setTDDirectory(tdDir);
		checkTargetDirectory(tdDir);
		
		AnalysisContext analysisContext;
		if (!req.isProfileRedirect()) {
			analysisContext = new AnalysisContext(NAME, this.analyzerSetup.getMasterRepository());
			// copy the recording file to TD profile directory
			this.recordingFileName = tempFileManager.moveZipFile(req.getTempZipFileId(), tdDir, req.getId());
		}
		else {
			analysisContext = new AnalysisContext(NAME, this.analyzerSetup.getMasterRepository(), req.getPreviousProfile());
			
			// copy the recording file to from previous TD profile directory to new one
			// recordingFileName remains unchanged
			File source = new File(req.getPreviousTDDirectory() + File.separatorChar + this.recordingFileName);
			File target = new File(tdDir + File.separatorChar + this.recordingFileName);
			
			if (logger.isDebugEnabled())
				logger.debug("Moving the file " + source.getAbsolutePath() + " to directory " + tdDir);
			try {
				com.google.common.io.Files.copy(source, target);
			} catch (IOException e) {
				throw new JzrWebException("Profile redirection : failed to move file " + this.recordingFileName + " to directory " + tdDir, e);
			}
		}
		
		// Required properties for the Jeyzer Analyzer
		Properties props = initAnalyzerProperties(req);
		
		ConfigThreadLocal.put(props);
		
		if (logger.isInfoEnabled())
			dumpRequest(req, this.recordingFileName);

		try{
			JeyzerAnalyzer analyzer = new JeyzerAnalyzer(analysisContext);
			analyzer.setStatusEventDispatcher(eventDispatcher);
			
			logger.info("Calling Jeyzer Analyzer");
			ReportDescriptor report = analyzer.analyze();
			logger.info("Jeyzer analysis done");
			
			return report;
			
		}finally{
			// remove thread local configuration
			ConfigThreadLocal.empty();
			
			// release the id
			workDirManager.releaseId(req.getProfile(), workingDirId);
		}
	}
	
	private Properties initAnalyzerProperties(JzrAnalysisRequest req) {
		Properties props = new Properties();
		
		props.put(PROPERTY_JEYZER_ANALYSIS_ID, req.getId());
		props.put(PROPERTY_JEYZER_TARGET_PROFILE, analyzerSetup.getProfile(req.getProfile()).getTarget()); // used for built paths
		props.put(PROPERTY_JEYZER_OUTPUT_DIRECTORY, req.getTDDirectory());
		props.put(PROPERTY_JEYZER_RECORD_DIRECTORY, req.getTDDirectory());
		props.put(PROPERTY_JEYZER_RECORD_FILE, recordingFileName);
		props.put(PROPERTY_JEYZER_TARGET_NAME, req.getProfile() + "-" + req.getNodeName()); // suffix with profile
		props.put(PROPERTY_JEYZER_RECORD_PERIOD, req.getISOPeriod());
		props.put(PROPERTY_JEYZER_DISPLAY_TIME_ZONE_ID_USER_SELECTED, req.isReportTimeZoneUserSpecified());
		if (req.isReportTimeZoneUserSpecified())
			props.put(PROPERTY_JEYZER_DISPLAY_TIME_ZONE_ID, req.getReportTimeZoneId());
		props.put(PROPERTY_JEYZER_RECORDING_TIME_ZONE_ID_USER_SELECTED, req.isRecordingTimeZoneUserSpecified());
		if (req.isRecordingTimeZoneUserSpecified())
			props.put(PROPERTY_JEYZER_RECORDING_TIME_ZONE_ID, req.getRecordingTimeZoneId());
		props.put(PROPERTY_JEYZER_TARGET_DESCRIPTION, req.getDescription());
		props.put(PROPERTY_JEYZER_TARGET_ISSUER, req.getEmail());
		props.put(PROPERTY_JEYZER_REPORT_PASSWORD, req.getPassword());
		props.put(PROPERTY_JEYZER_ANALYZER_GENERATE_PROCESS_MUSIC, analyzerSetup.isProcessMusicGenerationEnabled());
		props.put(ConfigAnalyzer.ANALYSIS_CONFIG_FILE, analyzerSetup.getProfile(req.getProfile()).getAnalysisFilePath());
		props.put(PROPERTY_JEYZER_JZR_REPORT_ENABLED, true);
		props.put(PROPERTY_JEYZER_TRANSLATOR_FILE_MAX_SIZE, JeyzerServlet.getConfigWeb().getUploadRecordingMaxSize());
		props.put(PROPERTY_JEYZER_TRANSLATOR_UNCOMPRESS_MAX_SIZE, JeyzerServlet.getConfigWeb().getUploadRecordingUncompressedMaxSize());
		props.put(PROPERTY_JEYZER_TRANSLATOR_UNCOMPRESS_MAX_FILES, JeyzerServlet.getConfigWeb().getUploadRecordingUncompressedMaxFiles());

		addDiscoveryProperties(props, req);
		
		return props;
	}

	private void addDiscoveryProperties(Properties props, JzrAnalysisRequest req) {		
		if (req.getDiscoveryItems() == null)
			return;
		
		int i = 1;
		for (JzrDiscoveryItem item : req.getDiscoveryItems()){
			props.put(PROPERTY_JEYZER_ANALYZER_DISCOVERY_FUNCTIONS_PREFIX + i, item.getKeyWords());
			props.put(PROPERTY_JEYZER_ANALYZER_DISCOVERY_FUNCTIONS_COLOR_PREFIX + i, item.getColor());
			i++;
		}
	}
		
	private void dumpRequest(JzrAnalysisRequest req, String fileName) {
		StringBuilder msg = new StringBuilder();
		
		msg.append("\nProcessing request :\n");
		msg.append("\t- Id          : " + req.getId() +"\n");
		msg.append("\t- Profile     : " + req.getProfile() +"\n");
		msg.append("\t- Td period   : " + (req.getPeriod() == -1 ? "not set / to detect" : req.getPeriod()) +" sec\n");
		msg.append("\t- Rec file    : " + fileName +"\n");
		msg.append("\t- Submitter   : " + req.getEmail() +"\n");
		if (req.getClientAddresses() != null)
			msg.append("\t- IP   : " + req.getClientAddresses() +"\n");
		msg.append("\t- Description : " + req.getDescription() +"\n");
		
		logger.info(msg.toString());	
	}
	
	private void checkTargetDirectory(String targetPath) {
		File targetDir = new File(targetPath);
		if (!targetDir.exists())
			targetDir.mkdirs();
	}
}
