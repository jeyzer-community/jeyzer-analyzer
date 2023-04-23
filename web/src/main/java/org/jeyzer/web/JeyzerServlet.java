package org.jeyzer.web;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.jeyzer.web.analyzer.JzrController;
import org.jeyzer.web.analyzer.JzrSetup;
import org.jeyzer.web.config.ConfigWeb;
import org.jeyzer.web.util.TempFileManager;
import org.jeyzer.web.util.WorkingDirectoryIdManager;

import javax.servlet.annotation.WebInitParam;

import com.vaadin.flow.server.VaadinServlet;

@WebServlet(value = "/*", asyncSupported = true, initParams = {
        @WebInitParam( name = "heartbeatInterval", value = "120"),
        @WebInitParam( name = "org.atmosphere.cpr.Broadcaster.supportOutOfOrderBroadcast", value = "true"),
        @WebInitParam( name = "org.atmosphere.cpr.broadcaster.maxProcessingThreads", value = "10"),
        @WebInitParam( name = "org.atmosphere.cpr.broadcaster.maxAsyncWriteThreads", value = "10"),
        @WebInitParam( name = "function-discovery-display", value = "true"),
        @WebInitParam( name = "ufo-stack-file-link-display", value = "true"),
        @WebInitParam( name = "analyzer-thread-pool-size", value = "3"),
        @WebInitParam( name = "temp-upload-recording-max-retention-time", value = "5m"),
        @WebInitParam( name = "portal-title-display", value = "true"),
        @WebInitParam( name = "portal-profiles-display", value = "true"),
        @WebInitParam( name = "portal-period-display", value = "true"),
        @WebInitParam( name = "portal-time-zone-display", value = "true"),
        @WebInitParam( name = "portal-agreement-display", value = "false"),
        @WebInitParam( name = "portal-privacy-policy-url", value = "https://jeyzer.org/privacy-policy"),
        @WebInitParam( name = "portal-captcha-display", value = "false"),
        @WebInitParam( name = "portal-captcha-secret-key", value = "secret-key-to-set"),
        @WebInitParam( name = "portal-captcha-website-key", value = "website-key-to-set"),
        @WebInitParam( name = "portal-forum-url", value = "https://jeyzer.org/forum"),
        @WebInitParam( name = "upload-recording-max-size", value = "100‬"), // Mb
        @WebInitParam( name = "upload-recording-uncompressed-max-size", value = "1500‬"), // Mb, zip expansion ratio of 15 
        @WebInitParam( name = "upload-recording-uncompressed-max-files", value = "2882"), // 24 hours with a 30 sec period + the property card and process jar files
        @WebInitParam( name = "default-submitter-email", value = "optional-email@domain.com"),
        @WebInitParam( name = "default-issue-description", value = "Please describe the issue : context, time, symptoms..\nAny first guess is also welcome.\nFeel free to add questions.")

})

public class JeyzerServlet extends VaadinServlet {
	
	private static final long serialVersionUID = 405874179395675002L;

	public static final String ENV_JEYZER_WEB_ANALYZER_WORK_DIR = "JEYZER_WEB_ANALYZER_WORK_DIR";
	
	// constant
    private static JzrSetup jzrSetup;
    private static String tdRoorDir;
    
    // configuration
    private static ConfigWeb cfg;
    
    // thread safe
    private static WorkingDirectoryIdManager workDirManager;
    private static TempFileManager tempFileManager;

	private static ExecutorService executorService;	
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		
		cfg = new ConfigWeb(servletConfig);
		
		jzrSetup = new JzrSetup();			
		workDirManager = new WorkingDirectoryIdManager(
				jzrSetup.getProfiles(), 
				cfg.getAnalyzerThreadPoolSize()
			);
		
		tempFileManager = new TempFileManager(
				cfg.getTempUploadDirectory(),
				cfg.getTempUploadRecordingMaxRetentionTime()
			);

		executorService = Executors.newFixedThreadPool(
				cfg.getAnalyzerThreadPoolSize(), 
				new JzrController.JzrControllerThreadFactory()
			);
		
		initTDRootDir();
	}
	
	@Override
	public void destroy(){
		executorService.shutdown();
		tempFileManager.shutdown();
	}
	
	public static JzrSetup getJzrSetup() {
		return jzrSetup;
	}
	
	public static ConfigWeb getConfigWeb() {
		return cfg;
	}
	
    public static TempFileManager getTempFileManager() {
		return tempFileManager;
	}

	public static String getTdRoorDir() {
		return tdRoorDir;
	}

	public static WorkingDirectoryIdManager getWorkDirManager() {
		return workDirManager;
	}

	public static ExecutorService getExecutorService() {
		return executorService;
	}
	
	private void initTDRootDir() throws ServletException{
		tdRoorDir = System.getenv(ENV_JEYZER_WEB_ANALYZER_WORK_DIR);
		if (tdRoorDir == null)
			throw new ServletException("Failed to initialize the Jeyzer web server. The " + ENV_JEYZER_WEB_ANALYZER_WORK_DIR + " is not set.");
		File rootDir = new File(tdRoorDir);
		if (!rootDir.exists()){
			rootDir.mkdirs();
		}
	}
}
