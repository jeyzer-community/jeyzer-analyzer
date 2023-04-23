package org.jeyzer.analyzer.output;

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


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jeyzer.analyzer.config.patterns.ConfigPatterns;
import org.jeyzer.analyzer.config.report.ConfigATBIProfilingSheet;
import org.jeyzer.analyzer.config.report.ConfigAboutSheet;
import org.jeyzer.analyzer.config.report.ConfigActionDashboardSheet;
import org.jeyzer.analyzer.config.report.ConfigActionDistinctHistogramSheet;
import org.jeyzer.analyzer.config.report.ConfigActionDistinctProfilingSheet;
import org.jeyzer.analyzer.config.report.ConfigActionProfilingSheet;
import org.jeyzer.analyzer.config.report.ConfigActionSheet;
import org.jeyzer.analyzer.config.report.ConfigEventJournalSheet;
import org.jeyzer.analyzer.config.report.ConfigExecutorFunctionHistogramSheet;
import org.jeyzer.analyzer.config.report.ConfigExecutorHistogramSheet;
import org.jeyzer.analyzer.config.report.ConfigFunctionOperationHistogramSheet;
import org.jeyzer.analyzer.config.report.ConfigGroupSequenceSheet;
import org.jeyzer.analyzer.config.report.ConfigJVMFlagsSheet;
import org.jeyzer.analyzer.config.report.ConfigMonitoringRulesSheet;
import org.jeyzer.analyzer.config.report.ConfigMonitoringSequenceSheet;
import org.jeyzer.analyzer.config.report.ConfigMonitoringSheet;
import org.jeyzer.analyzer.config.report.ConfigMonitoringStickersSheet;
import org.jeyzer.analyzer.config.report.ConfigNavigationMenuSheet;
import org.jeyzer.analyzer.config.report.ConfigPrincipalHistogramSheet;
import org.jeyzer.analyzer.config.report.ConfigProcessCardSheet;
import org.jeyzer.analyzer.config.report.ConfigProcessJarsSheet;
import org.jeyzer.analyzer.config.report.ConfigProcessModulesSheet;
import org.jeyzer.analyzer.config.report.ConfigAnalysisPatternsSheet;
import org.jeyzer.analyzer.config.report.ConfigSequenceSheet;
import org.jeyzer.analyzer.config.report.ConfigSessionDetailsSheet;
import org.jeyzer.analyzer.config.report.ConfigSheet;
import org.jeyzer.analyzer.config.report.ConfigTopStackSheet;
import org.jeyzer.analyzer.config.report.ConfigXLSX;
import org.jeyzer.analyzer.config.report.security.ConfigSecurity;
import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.error.JzrExecutionException;
import org.jeyzer.analyzer.error.JzrReportException;
import org.jeyzer.analyzer.error.JzrSecurityInvalidPasswordException;
import org.jeyzer.analyzer.output.poi.CellRefRepository;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.output.poi.sheet.ATBIProfilingSheet;
import org.jeyzer.analyzer.output.poi.sheet.AboutSheet;
import org.jeyzer.analyzer.output.poi.sheet.ActionDashboardSheet;
import org.jeyzer.analyzer.output.poi.sheet.ActionDistinctHistogramSheet;
import org.jeyzer.analyzer.output.poi.sheet.ActionDistinctProfilingSheet;
import org.jeyzer.analyzer.output.poi.sheet.ActionListSheet;
import org.jeyzer.analyzer.output.poi.sheet.ActionProfilingSheet;
import org.jeyzer.analyzer.output.poi.sheet.EventJournalSheet;
import org.jeyzer.analyzer.output.poi.sheet.ExecutorFunctionHistogramSheet;
import org.jeyzer.analyzer.output.poi.sheet.ExecutorHistogramSheet;
import org.jeyzer.analyzer.output.poi.sheet.FunctionOperationHistogramSheet;
import org.jeyzer.analyzer.output.poi.sheet.JVMFlagsSheet;
import org.jeyzer.analyzer.output.poi.sheet.JeyzerSheet;
import org.jeyzer.analyzer.output.poi.sheet.MonitoringRulesSheet;
import org.jeyzer.analyzer.output.poi.sheet.MonitoringSequenceSheet;
import org.jeyzer.analyzer.output.poi.sheet.MonitoringSheet;
import org.jeyzer.analyzer.output.poi.sheet.MonitoringStickersSheet;
import org.jeyzer.analyzer.output.poi.sheet.NavigationMenuSheet;
import org.jeyzer.analyzer.output.poi.sheet.PrincipalHistogramSheet;
import org.jeyzer.analyzer.output.poi.sheet.ProcessCardSheet;
import org.jeyzer.analyzer.output.poi.sheet.ProcessJarsSheet;
import org.jeyzer.analyzer.output.poi.sheet.ProcessModulesSheet;
import org.jeyzer.analyzer.output.poi.sheet.AnalysisPatternsSheet;
import org.jeyzer.analyzer.output.poi.sheet.SessionDetailsSheet;
import org.jeyzer.analyzer.output.poi.sheet.TaskGroupSequenceSheet;
import org.jeyzer.analyzer.output.poi.sheet.TaskSequenceSheet;
import org.jeyzer.analyzer.output.poi.sheet.TopStackSheet;
import org.jeyzer.analyzer.output.poi.style.CellStyles;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.analyzer.setup.JzrSetupManager;
import org.jeyzer.analyzer.status.JeyzerStatusEvent;
import org.jeyzer.analyzer.status.JeyzerStatusEventDispatcher;
import org.jeyzer.analyzer.util.SystemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.LinkedListMultimap;



public class JZRReport {
	
	private static final Logger logger = LoggerFactory.getLogger(JZRReport.class);
	
	public static final String FILE_EXTENSION = ".xlsx";
	
	public static final String EXCEL_AUTHOR = "Jeyzer Analyzer";
	public static final String EXCEL_TITLE = "JZR report";
	
	public static final String DUMMY_SHEET_PREFIX = "Dummy-";
	
	public static final String LOG_GENERATION_TRACE = "Generating JZR sheet : {}";
	public static final String LOG_DISABLING_TRACE = "Disabling JZR sheet : ";
	
	private JeyzerStatusEventDispatcher eventDispatcher;
	
	private JzrSession session;
	private XSSFWorkbook workbook;
	private ConfigXLSX configXslx;
	private ConfigPatterns tdPatternsCfg;
	private CellStyles styles;
	private JzrSetupManager setup;
	
	public JZRReport(ConfigXLSX configXslx, ConfigPatterns tdPatternsCfg, JzrSession session, JzrSetupManager setup, JeyzerStatusEventDispatcher eventDispatcher){
		this.session = session;
		this.eventDispatcher = eventDispatcher;
		this.configXslx = configXslx;
		this.tdPatternsCfg = tdPatternsCfg;
		this.setup = setup;
		workbook = new XSSFWorkbook();
		prepareWorkbook(configXslx);
		this.styles = new CellStyles(configXslx, workbook);
	}
	
	private void prepareWorkbook(ConfigXLSX config) {
		POIXMLProperties xmlProps = workbook.getProperties();
		POIXMLProperties.CoreProperties coreProps =  xmlProps.getCoreProperties();
		coreProps.setCreator(EXCEL_AUTHOR);
		coreProps.setTitle(EXCEL_TITLE);		
		coreProps.setContentStatus("Jeyzer Community license");
		
		// create dummy sheets
		// This is workaround to manage the creation of any number of sheets 
		// in any order (ex : process card, memory, CPU sheets may not always be created)
		for (int i=0; i<config.getSheetSize(); i++){
			workbook.createSheet(DUMMY_SHEET_PREFIX + i);
		}
	}

	public void closeWorkBook() throws JzrExecutionException{
		boolean success = false;
		String path = ReportHelper.getFilePath(session, this.configXslx.getOutputDirectory(), this.configXslx.getOutputFilePrefix(), FILE_EXTENSION);
		
		// cleanup the dummy sheets, starting from the last sheet
		for (int i=this.workbook.getNumberOfSheets()-1; i!=-1; i--){
			Sheet sheet = this.workbook.getSheetAt(i);
			if (sheet.getSheetName().startsWith(DUMMY_SHEET_PREFIX))
				this.workbook.removeSheetAt(i);
		}
    	
    	this.workbook.setSelectedTab(0);
    	this.workbook.setFirstVisibleTab(0);
    	this.workbook.setActiveSheet(0); // this one makes the difference !
    	
		try {
			// output directory
			SystemHelper.createDirectory(this.configXslx.getOutputDirectory());
    	
			if (this.configXslx.getSecurityConfig().isReportSecured())
				writeSecuredWorkbook(path);
			else
				writeWorkbook(path);
			
			success = true;
		} catch (Exception e) {
			logger.error("Failed to generate the JZR report.", e);
			throw new JzrReportException("Failed to generate the JZR report.", e);
		}
		
		// Release style cache which is workbook specific
		this.styles.close();
		
		if (success){
			logger.info("JZR report created : " + SystemHelper.sanitizePathSeparators(path));
		}
	}
	
	private void writeSecuredWorkbook(String path) throws IOException, GeneralSecurityException, JzrSecurityInvalidPasswordException {
		ConfigSecurity security = this.configXslx.getSecurityConfig();
		
		EncryptionInfo info = new EncryptionInfo(
				security.getEncryption().getEncryptionMode(), 
				security.getEncryption().getCipherAlgorithm(), 
				security.getEncryption().getHashAlgorithm(), 
				-1, 
				-1, 
				null);
    	Encryptor enc = info.getEncryptor();
    	enc.confirmPassword(security.getPassword()); 
    	
    	try (
    	    	FileOutputStream fos = new FileOutputStream(path);
    			POIFSFileSystem fs = new POIFSFileSystem();
    		)
    	{
    		logger.info("Encrypting the JZR report");
    		this.eventDispatcher.fireStatusEvent(JeyzerStatusEvent.STATE.REPORT_ENCRYPTION);

    		// encrypt
	    	OutputStream os = enc.getDataStream(fs);
    		this.workbook.write(os);
    		// os already closed through the write
    		
    		// write on disk
			fs.writeFilesystem(fos);
    	}
	}

	private void writeWorkbook(String path) throws IOException, FileNotFoundException {
		try (
				FileOutputStream fileOut = new FileOutputStream(path);
			)
		{
			 workbook.write(fileOut);
		}
	}

	public void fillReportDescriptor(ReportDescriptor desc){
		desc.setStartTime(session.getStartDate());
		desc.setEndTime(session.getEndDate());
		desc.setLevel(session.getHighestEventCategory());
		desc.setApplicationType(session.getApplicationType());
		desc.setApplicationId(session.getApplicationId());
		desc.setAnalysisProductionReady(session.getConfigurationState().isProduction());
		desc.setReportFileName(ReportHelper.getFileName(session, this.configXslx.getOutputFilePrefix(), FILE_EXTENSION));
		desc.setReportFilePath(ReportHelper.getFilePath(session, this.configXslx.getOutputDirectory(), this.configXslx.getOutputFilePrefix(), FILE_EXTENSION));
		desc.setTimeZoneInfo(session.getDisplayTimeZoneInfo());
	}
    
    public void report() throws JzrException{
    	logger.info("Generating JZR report");
    	
    	JeyzerSheet sheet;

    	DisplayContext displayContext = new DisplayContext(
    			workbook, 
    			styles, 
    			setup,
    			new CellRefRepository(
    					session.getActionsSize(),
    					session.getActionsStackSize()) // Created a bit late, this may be an issue if the link creator is coming after the one that needs it..
    			);

    	// at this stage, we know which configuration is finally valid for display
    	validateSheetConfigurations();
    	    	
    	if (isDisplayable(this.configXslx.getSessionDetailsSheet())){
    		ConfigSessionDetailsSheet sheetCfg = this.configXslx.getSessionDetailsSheet();
        	logger.info(LOG_GENERATION_TRACE, SessionDetailsSheet.SHEET_NAME);
        	//this.eventDispatcher.fireReportStatusEvent(SessionDetailsSheet.SHEET_NAME); // session sheet generation is fast operation
    		sheet = new SessionDetailsSheet(sheetCfg, session, displayContext);
    		sheet.display();
    		setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    	}
		
    	if (isDisplayable(this.configXslx.getProcessCardSheet())){
    		ConfigProcessCardSheet sheetCfg = this.configXslx.getProcessCardSheet();
    		logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
    		// this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName()); // process card sheet generation is fast operation
    		sheet = new ProcessCardSheet(sheetCfg, session, displayContext);
    		sheet.display();
    		setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    	}
    	
    	if (isDisplayable(this.configXslx.getProcessJarsSheet())){
    		ConfigProcessJarsSheet sheetCfg = this.configXslx.getProcessJarsSheet();
    		logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
    		// this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName()); // process jars sheet generation is fast operation
    		sheet = new ProcessJarsSheet(sheetCfg, session, displayContext);
    		sheet.display();
    		setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    	}
    	
    	if (isDisplayable(this.configXslx.getProcessModulesSheet())){
    		ConfigProcessModulesSheet sheetCfg = this.configXslx.getProcessModulesSheet();
    		logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
    		// this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName()); // process modules sheet generation is fast operation
    		sheet = new ProcessModulesSheet(sheetCfg, session, displayContext);
    		sheet.display();
    		setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    	}
    	
    	if (isDisplayable(this.configXslx.getJVMFlagsSheet())){
    		ConfigJVMFlagsSheet sheetCfg = this.configXslx.getJVMFlagsSheet();
    		logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
    		this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName());
    		sheet = new JVMFlagsSheet(sheetCfg, session, displayContext);
    		sheet.display();
    		setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    	}
    	
   		reportSequenceSheets(configXslx.getSequenceSheets(), displayContext);
   		reportGroupSequenceSheets(configXslx.getGroupSequenceSheets(), displayContext);
    	
    	if (isDisplayable(this.configXslx.getActionSheet())){
    		ConfigActionSheet sheetCfg = this.configXslx.getActionSheet();
    		// display action list
    		logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
    		this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName());
    		sheet = new ActionListSheet(sheetCfg, session, displayContext);
    		sheet.display();
    		setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    	}
    	
    	if (isDisplayable(this.configXslx.getTopStackSheet())){
    		ConfigTopStackSheet sheetCfg = this.configXslx.getTopStackSheet();
    		// display top stacks
    		logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
    		this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName());
    		sheet = new TopStackSheet(sheetCfg, session, displayContext);
    		sheet.display();
    		setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    	}
    	
    	if (isDisplayable(this.configXslx.getActionProfilingSheet())){
    		ConfigActionProfilingSheet sheetCfg = this.configXslx.getActionProfilingSheet();
    		// display action profiling
    		logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
    		this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName());
    		sheet = new ActionProfilingSheet(sheetCfg, session, displayContext);
    		sheet.display();
    		setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    	}

    	if (isDisplayable(this.configXslx.getATBIProfilingSheet())){
    		ConfigATBIProfilingSheet sheetCfg = this.configXslx.getATBIProfilingSheet();
    		// display ATBI profiling
    		logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
    		this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName());
    		sheet = new ATBIProfilingSheet(sheetCfg, session, displayContext);
    		sheet.display();
    		setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    	}
    	
    	if (isDisplayable(this.configXslx.getActionDistinctProfilingSheet())){
    		ConfigActionDistinctProfilingSheet sheetCfg = this.configXslx.getActionDistinctProfilingSheet();
    		// display action distinct profiling
    		logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
    		this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName());
    		sheet = new ActionDistinctProfilingSheet(sheetCfg, session, displayContext);
    		sheet.display();
    		setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    	}
    	
    	if (isDisplayable(this.configXslx.getPrincipalHistogramSheet())){
    		ConfigPrincipalHistogramSheet sheetCfg = this.configXslx.getPrincipalHistogramSheet();
    		// display histogram
    		logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
    		this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName());
    		sheet = new PrincipalHistogramSheet(sheetCfg, session, displayContext);
    		sheet.display();
    		setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    	}

    	if (isDisplayable(this.configXslx.getExecutorFunctionHistogramSheet())){
    		ConfigExecutorFunctionHistogramSheet sheetCfg = this.configXslx.getExecutorFunctionHistogramSheet();
    		// display histogram
    		logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
    		this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName());
    		sheet = new ExecutorFunctionHistogramSheet(sheetCfg, session, displayContext);
    		sheet.display();
    		setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    	}
    	
    	if (isDisplayable(this.configXslx.getExecutorHistogramSheet())){
    		ConfigExecutorHistogramSheet sheetCfg = this.configXslx.getExecutorHistogramSheet();
    		// display histogram
    		logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
    		this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName());
    		sheet = new ExecutorHistogramSheet(sheetCfg, session, displayContext);
    		sheet.display();
    		setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    	}

    	if (isDisplayable(this.configXslx.getFunctionOperationHistogramSheet())){
    		ConfigFunctionOperationHistogramSheet sheetCfg = this.configXslx.getFunctionOperationHistogramSheet();
    		// display histogram
    		logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
    		this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName());
    		sheet = new FunctionOperationHistogramSheet(sheetCfg, session, displayContext);
    		sheet.display();
    		setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    	}

    	if (isDisplayable(this.configXslx.getActionDistinctHistogramSheet())){
    		ConfigActionDistinctHistogramSheet sheetCfg = this.configXslx.getActionDistinctHistogramSheet();
    		// display histogram
    		logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
    		this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName());
    		sheet = new ActionDistinctHistogramSheet(sheetCfg, session, displayContext);
    		sheet.display();
    		setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    	}

    	reportMonitoringSequenceSheets(configXslx.getMonitoringSequenceSheets(), displayContext);
    	
    	reportMonitoringSheets(configXslx.getMonitoringSheets(), displayContext);

    	if (isDisplayable(this.configXslx.getActionDashboardSheet())){
    		ConfigActionDashboardSheet sheetCfg = this.configXslx.getActionDashboardSheet();
    		// display action dashboard
    		logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
    		this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName());
    		sheet = new ActionDashboardSheet(sheetCfg, session, displayContext);
    		sheet.display();
    		setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    	}    	

    	if (isDisplayable(this.configXslx.getMonitoringRulesSheet())){
    		ConfigMonitoringRulesSheet sheetCfg = this.configXslx.getMonitoringRulesSheet();
    		logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
    		this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName());
    		sheet = new MonitoringRulesSheet(sheetCfg, session, displayContext);
    		sheet.display();
    		setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    	}
    	
    	if (isDisplayable(this.configXslx.getMonitoringStickersSheet())){
    		ConfigMonitoringStickersSheet sheetCfg = this.configXslx.getMonitoringStickersSheet();
    		logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
    		this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName());
    		sheet = new MonitoringStickersSheet(sheetCfg, session, displayContext);
    		sheet.display();
    		setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    	}
    	
    	reportAnalysisPatternsSheets(configXslx.getAnalysisPatternsSheets(), displayContext);
    	
    	// Always displayed
    	if (this.configXslx.getAboutSheet() != null){
    		ConfigAboutSheet sheetCfg = this.configXslx.getAboutSheet();
        	// display about
        	logger.info(LOG_GENERATION_TRACE, AboutSheet.SHEET_NAME);
        	// this.eventDispatcher.fireReportStatusEvent(AboutSheet.SHEET_NAME); // about generation is fast operation
        	sheet = new AboutSheet(sheetCfg, session, displayContext);
        	sheet.display();
        	setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    	}
    	
    	// Always displayed (cannot be disabled through formats)
    	// Must be last to display other sheet items count
    	if (configXslx.getNavigationMenuSheet() != null){
    		ConfigNavigationMenuSheet sheetCfg = this.configXslx.getNavigationMenuSheet();
        	logger.info(LOG_GENERATION_TRACE, NavigationMenuSheet.SHEET_NAME);
    		// this.eventDispatcher.fireReportStatusEvent(NavigationMenuSheet.SHEET_NAME); // menu generation is fast operation
    		sheet = new NavigationMenuSheet(sheetCfg, session, displayContext);
    		sheet.display();
    		setSheetOrder(NavigationMenuSheet.SHEET_NAME, sheetCfg.getIndex());
    	}
    	
    	createCrossSheetActionLinks(displayContext);
    	createCrossSheetDateLinks(displayContext);
    }

	private boolean isDisplayable(ConfigSheet sheetCfg) {
		return sheetCfg != null && sheetCfg.isEnabled();
	}

	private void createCrossSheetDateLinks(DisplayContext displayContext) {
		if (!setup.isHeaderDateLinkEnabled())
			return;
		
		LinkedListMultimap<String, CellReference> dateLinksMultimap;
		dateLinksMultimap = displayContext.getCellRefRepository().getDateRefMultiMap();
		
		for (String dateKey : dateLinksMultimap.keySet()){
			List<CellReference> cellRefs = dateLinksMultimap.get(dateKey);
			createCrossSheetLinks(cellRefs);
		}
	}

	private void createCrossSheetActionLinks(DisplayContext displayContext) {
		if (!setup.isActionLinkEnabled())
			return;
		
		for (ThreadDump dump : session.getDumps()){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = session.getActionHistory().get(timestamp);
			
			for(ThreadAction action : actions){
				List<CellReference> cellRefs = displayContext.getCellRefRepository().getActionRef(action.getStrId());
				createCrossSheetLinks(cellRefs);
			}
		}
	}
	
	private void createCrossSheetLinks(List<CellReference> cellRefs) {
		if (cellRefs== null || cellRefs.size() <= 1)
			return; // cell is not displayed or displayed only once
		
		Iterator<CellReference> iter = cellRefs.iterator();
		CellReference start = iter.next();
		CellReference source = start; 
		while(iter.hasNext()){
			CellReference target = iter.next();
			createCrossSheetLink(source, target);
			source = target;
		}
		createCrossSheetLink(source, start);
	}

	private void createCrossSheetLink(CellReference source, CellReference target) {
        CreationHelper factory = this.workbook.getCreationHelper();
        Hyperlink link = factory.createHyperlink(HyperlinkType.DOCUMENT);
        link.setAddress(target.formatAsString());
        Sheet sheet = this.workbook.getSheet(source.getSheetName());
        Row row = sheet.getRow(source.getRow());
        Cell cell = row.getCell(source.getCol());
        if (cell == null)
        	cell = row.createCell(source.getCol());
        cell.setHyperlink(link);
	}

	private void setSheetOrder(String sheetName, int index) {
		String sheetToRemoveName = DUMMY_SHEET_PREFIX + index;
		
		// Remove the corresponding dummy sheet first
		int pos = index;
		Sheet sheet = this.workbook.getSheet(sheetToRemoveName);
		if (sheet != null){
			pos = this.workbook.getSheetIndex(sheet);
			this.workbook.removeSheetAt(pos);
		}
		
		workbook.setSheetOrder(sheetName, pos);
		
		if (logger.isDebugEnabled())
			logSheetReplacement(sheetToRemoveName, sheetName, pos);
	}

	private void logSheetReplacement(String sheetToRemoveName, String sheetName, int pos) {
		Sheet sheet;
		logger.debug("Removing sheet " + sheetToRemoveName + " at position : " + pos);
		logger.debug("Adding sheet " + sheetName + " at position : " + pos);
		logger.debug("Sheet summary : ");
		for(int i=0; i<this.workbook.getNumberOfSheets(); i++){
			sheet = this.workbook.getSheetAt(i);
			logger.debug("  - "+ i +" sheet : " + sheet.getSheetName());
		}
	}

	private void validateSheetConfigurations() {
    	if (this.configXslx.getProcessCardSheet() != null && this.session.getProcessCard() == null){
    		ConfigProcessCardSheet sheetCfg = this.configXslx.getProcessCardSheet();
    		sheetCfg.disable();
    		logger.info(LOG_DISABLING_TRACE + sheetCfg.getName() + ". Reason : absence of process card");
    	}
    	
    	if (this.configXslx.getProcessJarsSheet() != null && this.session.getProcessJars() == null){
    		ConfigProcessJarsSheet sheetCfg = this.configXslx.getProcessJarsSheet();
    		sheetCfg.disable();
    		logger.info(LOG_DISABLING_TRACE + sheetCfg.getName() + ". Reason : absence of process jars data");
    	}
    	
    	if (this.configXslx.getProcessModulesSheet() != null && this.session.getProcessModules() == null){
    		ConfigProcessModulesSheet sheetCfg = this.configXslx.getProcessModulesSheet();
    		sheetCfg.disable();
    		logger.info(LOG_DISABLING_TRACE + sheetCfg.getName() + ". Reason : absence of process modules data");
    	}
    	
    	if (this.configXslx.getJVMFlagsSheet() != null && this.session.getJVMFlags() == null){
    		ConfigJVMFlagsSheet sheetCfg = this.configXslx.getJVMFlagsSheet();
    		sheetCfg.disable();
    		logger.info(LOG_DISABLING_TRACE + sheetCfg.getName() + ". Reason : absence of JVM flags");
    	}
    	
    	for (ConfigSheet sheetCfg : configXslx.getOrderedSheets()){
    		validateSheetConfigurationFormat(sheetCfg);
    	}
	}

	private void validateSheetConfigurationFormat(ConfigSheet sheetCfg) {
		if (sheetCfg.getSupportedFormats().isEmpty() || !sheetCfg.isEnabled())
			return;
		
		// conditional sheet. Check against parser format short name
		List<String> supportedParsers = sheetCfg.getSupportedFormats();
		if (!supportedParsers.contains(session.getFormatShortName())){
			logger.info(LOG_DISABLING_TRACE + sheetCfg.getName() + ". Reason : " + session.getFormatShortName() + " format not supported");
			sheetCfg.disable();
		}
	}

	private void reportSequenceSheets(List<ConfigSequenceSheet> sequenceSheets, DisplayContext displayContext) throws JzrException{
		for(ConfigSequenceSheet sequenceSheet : sequenceSheets){
			String sheetName = sequenceSheet.getName();
			
			// check if displayable
			if (sequenceSheet.isEnabled()){
				logger.info(LOG_GENERATION_TRACE, sheetName);
				this.eventDispatcher.fireReportStatusEvent(sheetName);
				JeyzerSheet sheet = new TaskSequenceSheet(sequenceSheet, session, displayContext);
				sheet.display();
				setSheetOrder(sheetName, sequenceSheet.getIndex());
			}
		}
    }
	
	private void reportGroupSequenceSheets(List<ConfigGroupSequenceSheet> sequenceSheets, DisplayContext displayContext) throws JzrException{
		for(ConfigGroupSequenceSheet sequenceSheet : sequenceSheets){
			String sheetName = sequenceSheet.getName();
			
			// check if displayable
			if (sequenceSheet.isEnabled()){
				logger.info(LOG_GENERATION_TRACE, sheetName);
				this.eventDispatcher.fireReportStatusEvent(sheetName);
				JeyzerSheet sheet = new TaskGroupSequenceSheet(sequenceSheet, session, displayContext);
				sheet.display();
				setSheetOrder(sheetName, sequenceSheet.getIndex());
			}
		}
    }
	
	private void reportMonitoringSheets(List<ConfigMonitoringSheet> monitoringSheets, DisplayContext displayContext) throws JzrException {
		JeyzerSheet sheet;
		
		for (ConfigMonitoringSheet sheetCfg : monitoringSheets){
			if (sheetCfg.isEnabled()) {
				// display monitoring events
				logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
				this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName());
				if (sheetCfg instanceof ConfigEventJournalSheet)
					sheet = new EventJournalSheet(sheetCfg, session, displayContext);
				else
					sheet = new MonitoringSheet(sheetCfg, session, displayContext);
				sheet.display();
				setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());				
			}
		}
	}
	
	private void reportMonitoringSequenceSheets(List<ConfigMonitoringSequenceSheet> monitoringSequenceSheets, DisplayContext displayContext) throws JzrException {
		JeyzerSheet sheet;
		
		for (ConfigMonitoringSequenceSheet sheetCfg : monitoringSequenceSheets){
			if (sheetCfg.isEnabled()) {
				// display monitoring events
				logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
				this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName());
				sheet = new MonitoringSequenceSheet(sheetCfg, session, displayContext);
				sheet.display();
				setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());				
			}
		}
	}

	private void reportAnalysisPatternsSheets(List<ConfigAnalysisPatternsSheet> analysisPatternsSheets, DisplayContext displayContext) throws JzrException {
		JeyzerSheet sheet;
		
    	for (ConfigAnalysisPatternsSheet sheetCfg : analysisPatternsSheets){
    		if (sheetCfg.isEnabled()) {
        		// display analysis patterns
        		logger.info(LOG_GENERATION_TRACE, sheetCfg.getName());
        		this.eventDispatcher.fireReportStatusEvent(sheetCfg.getName());
        		sheet = new AnalysisPatternsSheet(sheetCfg, tdPatternsCfg, session, displayContext);
        		sheet.display();
        		setSheetOrder(sheetCfg.getName(), sheetCfg.getIndex());
    		}
    	}
	}
}
