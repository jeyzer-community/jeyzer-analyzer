package org.jeyzer.analyzer.output.poi.sheet;

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



import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.*;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.*;

import java.util.Date;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.jeyzer.analyzer.config.ConfigAnalyzer;
import org.jeyzer.analyzer.config.report.ConfigSessionDetailsSheet;
import org.jeyzer.analyzer.config.translator.compression.ConfigDecompression;
import org.jeyzer.analyzer.config.translator.obfuscation.ConfigDeobfuscation;
import org.jeyzer.analyzer.config.translator.security.ConfigDecryption;
import org.jeyzer.analyzer.data.AnalysisPatternsStats;
import org.jeyzer.analyzer.data.ProcessCard;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.TimeZoneInfo;
import org.jeyzer.analyzer.data.ProcessCard.ProcessCardProperty;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStack.ThreadStackHandler;
import org.jeyzer.analyzer.data.tag.FunctionTag;
import org.jeyzer.analyzer.data.tag.OperationTag;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.input.translator.Translator;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.output.ReportHelper;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.analyzer.util.SystemHelper;

import com.google.common.collect.Multiset;

public class SessionDetailsSheet extends JeyzerSheet {

	public static final String SHEET_NAME = "Session details";
	
	public static final String DISABLED_VALUE = "Disabled";
	public static final String NOT_SET_VALUE = "-1";
	
	public static final short DETAILS_ROW_HEIGHT = 600;
	
	public static final String JEYZER_RECORDER_VERSION  = "jzr.recorder.version";
	public static final String JEYZER_PUBLISHER_VERSION = "jzr.publisher.version";
	public static final String JEYZER_AGENT_VERSION = "jzr.agent.version";
	
	public static final String JEYZER_RECORDER_LOG_LEVEL = "jzr.recorder.log.level";
	public static final String JEYZER_RECORDER_LOG_RELOADABLE = "jzr.recorder.log.reloadable";

	public static final String JEYZER_RECORDER_LOG_FILE_PATH = "jzr.recorder.log.file.path";
	public static final String JEYZER_RECORDER_LOG_FILE_ACTIVE = "jzr.recorder.log.file.active";
	public static final String JEYZER_RECORDER_LOG_FILE_LEVEL = "jzr.recorder.log.file.level";
	
	public static final String JEYZER_RECORDER_LOG_CONSOLE_ACTIVE = "jzr.recorder.log.console.active";
	public static final String JEYZER_RECORDER_LOG_CONSOLE_LEVEL = "jzr.recorder.log.console.level";
	
	private ConfigSessionDetailsSheet sheetCfg;
	
	public SessionDetailsSheet(ConfigSessionDetailsSheet sheetCfg, JzrSession session, DisplayContext displayContext) {
		super(session, displayContext);
		this.sheetCfg = sheetCfg;
	}

	@Override
	public void display() {
    	int linePos = 2;
		
    	Sheet sheet = createSheet(this.sheetCfg);
    	prepareSheet(sheet);
    	
    	sheet.setColumnWidth(0, 3*256);
    	sheet.setColumnWidth(1, 64*256);
    	sheet.setColumnWidth(2, 96*256);
    	sheet.setColumnWidth(3, 255*256);
    	
    	// ----------------------
    	// Session details title
    	// ----------------------
    	displayTitle(sheet);

    	// ----------------------
    	// Session parameters
    	// ----------------------
		int offset = linePos;
		linePos = displayProfileSection(sheet, linePos);		
		linePos = displayRecordingSection(sheet, linePos);
		linePos = displayAnalysisSection(sheet, linePos);
		linePos = displayReportSection(sheet, linePos);
		linePos = displayStorageSection(sheet, linePos);
		linePos = displayRecorderLogSection(sheet, linePos);
		linePos = displayVersionsSection(sheet, linePos);
		
		// Minus number of sections with title. First one doesn't have
		this.itemsCount = linePos - offset - 4;

    	// ----------------------
    	// Bottom page
    	// ----------------------
		linePos = displayBottom(sheet, linePos);
    	
    	addMainFrame(sheet, sheetCfg, linePos, 4);
    	
    	sheet.createFreezePane(0, 2);
    	
    	close(this.sheetCfg);
	}

	private void displayTitle(Sheet sheet) {
    	Row row = sheet.createRow(1);
    	row.setHeight(DETAILS_ROW_HEIGHT);
    	
    	Cell cell = row.createCell(1);
		cell.setCellValue("Session details");
		cell.setCellStyle(getThemeStyle(STYLE_THEME_SESSION_TITLE));
    	
		cell = row.createCell(2);
    	cell.setCellStyle(getThemeStyle(STYLE_THEME_SESSION));
    	
		cell = row.createCell(3);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_SESSION));
	}

	private int displayProfileSection(Sheet sheet, int linePos) {
		addParam(sheet, linePos++, "Report name", this.session.getApplicationId());
    	addTextParam(sheet, linePos++, "Issue description", this.session.getDescription());
    	if (!session.isMasterProfileRedirected())
    		addParam(sheet, linePos++, "Master profile", this.session.getApplicationType());
    	else {
	    	String comment = "This JZR report is the result of a master profile redirection.\n"
	    			+ "Redirection is usually initiated by a generic profile (" + session.getMasterProfileRedirectedFrom() + " here) and is targeting a production profile (" + this.session.getApplicationType() + " here).";
	    	addCommentedParam(sheet, linePos++, "Redirected master profile", this.session.getApplicationType(), STYLE_SESSION_WARNING_CELL, comment);
    	}
    	addParam(sheet, linePos++, "Profile state", this.session.getConfigurationState().getDisplayValue());
		if (session.isMasterProfileRedirected()) {
	    	addParam(sheet, linePos++, "Original master profile", session.getMasterProfileRedirectedFrom());
		}
		
		return linePos;
	}

	private int displayRecordingSection(Sheet sheet, int linePos) {
		linePos = displaySectionHeader("Recording", sheet, linePos);

    	addParam(sheet, linePos++, "Duration", ReportHelper.getPrintableDuration(this.session.getEndDate().getTime() - this.session.getStartDate().getTime()));
    	addParam(sheet, linePos++, "Start date", formatRecordingDate(this.session.getStartDate(), DATE_TIME_DISPLAY_FORMAT));
    	addParam(sheet, linePos++, "End date", formatRecordingDate(this.session.getEndDate(), DATE_TIME_DISPLAY_FORMAT));
    	addParam(sheet, linePos++, "Time zone and source", formatTimeZoneInfo(session.getRecordingTimeZoneInfo()));
    	
    	int size = this.session.getDumps().size();
    	addNumericParam(sheet, linePos++, "Recording snapshots", size, size == 0? STYLE_SESSION_WARNING_CELL : STYLE_SESSION_CELL);

    	int ctd = this.session.getConfiguredThreadDumpPeriod();
    	int dtp  = this.session.getDetectedThreadDumpPeriod();
    	// both values specified are really different : period setting problem
    	Boolean periodSetIssue = ctd!=-1 && !session.isValidConfiguredThreadDumpPeriod();
    	String style = (periodSetIssue != null && periodSetIssue) ?  STYLE_SESSION_WARNING_CELL : STYLE_SESSION_CELL;
    	String comment = periodSetIssue ? "Configured recording period doesn't match the detected period. \n"
    			+ "Please make sure that configured period is correct. \n"
    			+ "Otherwise fix it an re-run the analysis." : null;
    	addCommentedParam(sheet, linePos++, "Recording configured period", ctd != -1 ? Long.toString(ctd) + " sec" : "Not set", style, comment);
    	
    	Boolean isValidDetectedTDPeriod = session.isValidDetectedThreadDumpPeriod();
    	style = (isValidDetectedTDPeriod != null && dtp != -1 && !isValidDetectedTDPeriod) ?  STYLE_SESSION_WARNING_CELL : STYLE_SESSION_CELL;
    	comment = (isValidDetectedTDPeriod != null && dtp != -1 && !isValidDetectedTDPeriod) ? "Detected recording period is not accurate. \n"
    			+ "Please make sure recording snapshots are taken at regular interval." : null;
    	addCommentedParam(sheet, linePos++, "Recording detected period", dtp != -1 ? Long.toString(dtp) + " sec" : DISABLED_VALUE, style, comment);
    	
    	addParam(sheet, linePos++, "Recording format", this.session.getFormat());
    	
    	if (this.session.isRecordingFileProvided())
        	addParam(sheet, linePos++, "Recording file", this.session.getRecordingFileName());
    	
    	Translator decryptTranslator = session.lookupTranslator(ConfigDecryption.TYPE_NAME);
    	addParam(sheet, linePos++, "Secured recording", decryptTranslator != null && decryptTranslator.isEnabled() ? decryptTranslator.getConfiguration().areTranslatedFilesKept() ? "Yes, decrypted files kept" : "Yes, decrypted files not kept" : "No");
    	if (decryptTranslator != null && decryptTranslator.isEnabled())
    		addParam(sheet, linePos++, "Decryption mode", ((ConfigDecryption)decryptTranslator.getConfiguration()).isEncryptionKeyPublished() ? "Dynamic" : "Static");
		
    	Translator deobTranslator = session.lookupTranslator(ConfigDeobfuscation.TYPE_NAME);    	
    	addParam(sheet, linePos++, "Deobfuscation", deobTranslator != null && deobTranslator.isEnabled() ? deobTranslator.getConfiguration().areTranslatedFilesKept() ? "Yes, deobfuscated files kept" : "Yes, deobfuscated files not kept" : "No");
    	
		return linePos;
	}
	
	private int displayAnalysisSection(Sheet sheet, int linePos) {
		linePos = displaySectionHeader("Analysis", sheet, linePos);
		
		// display it only in Web analysis case (master repository available) and in a non redirection case (a generic profile typically)
		if (!session.isMasterProfileRedirected() && session.isMasterRepositoryAvailable()) 
			addParam(sheet, linePos++, "Master profile discovery", session.isMasterProfileDiscoveryEnabled() ? "active" : "inactive");

		// Matching shared profiles
		AnalysisPatternsStats analysisPatternsStats = this.session.getAnalysisPatternsStats();
		StringBuilder text = new StringBuilder();
		Iterator<String> namesIter = analysisPatternsStats.getProfileNames(false).iterator();
		while (namesIter.hasNext()) {
			text.append(namesIter.next());
			if (namesIter.hasNext())
				text.append("\n");
		}
		if (text.length() == 0)
			text.append("(None)");
		addTextParam(sheet, linePos++, "Matching shared profiles", text.toString());
		
    	int size = this.session.getActionsSize();
    	addNumericParam(sheet, linePos++, "Detected actions", size, size > 0? STYLE_SESSION_OK_CELL : STYLE_SESSION_WARNING_CELL);
    	    	
    	if (this.session.isProcessUpTimeMeasurementUsed()){
        	size = getApplicativeRestartCount();
        	addNumericParam(sheet, linePos++, "Applicative restarts", size, size > 0? STYLE_SESSION_WARNING_CELL : STYLE_SESSION_CELL);
    	}
    	
    	size = getMissingDumpsCount();
    	addNumericParam(sheet, linePos++, "Recording hiatus", size, size > 0? STYLE_SESSION_WARNING_CELL : STYLE_SESSION_CELL);
    	
    	size = this.session.getActionsStackSize();
    	addNumericParam(sheet, linePos++, "Detected stacks", size, STYLE_SESSION_CELL);

    	// Virtual threads
    	String comment ="Virtual threads are available in standard in Java 21 and as experimental feature since Java 17.";
    	addCommentedParam(sheet, linePos++, "Virtual threads presence", session.hasVirtualThreadPresence() ? "yes" : "no", STYLE_SESSION_CELL, comment);

    	if (session.hasVirtualThreadPresence() && !session.hasVirtualThreads()) {
        	comment ="Virtual threads are detected but not visible in the recording (only its carrier threads are). \n"
        			+ "You must use the Jcmd command to view it in a JZR report.";
        	addCommentedParam(sheet, linePos++, "Virtual threads visible", session.hasVirtualThreads() ? "yes" : "no", session.hasVirtualThreads() ? STYLE_SESSION_CELL : STYLE_SESSION_WARNING_CELL, comment);
    	}
    	
    	size = this.session.getActionsStackSize();
    	
    	Multiset<ThreadStackHandler> stacks = session.getStackSet();
    	int percent = getIdentifiedActionPercentage(stacks);
    	addNumericParam(sheet, linePos++, "Action identification %", percent, percent < 30? STYLE_SESSION_WARNING_CELL : STYLE_SESSION_OK_CELL);
    	
    	percent = getIdentifiedOperationPercentage(stacks);
    	addNumericParam(sheet, linePos++, "Operation identification %", percent, percent < 30? STYLE_SESSION_WARNING_CELL : STYLE_SESSION_OK_CELL);
    	
    	percent = getIdentifiedExecutorPercentage(stacks);
    	addNumericParam(sheet, linePos++, "Executor identification %", percent, percent < 30? STYLE_SESSION_WARNING_CELL : STYLE_SESSION_OK_CELL);
    	
    	Multiset<Tag> functionSets = this.session.getFunctionSet();
    	percent = FormulaHelper.percentRound(functionSets.count(FunctionTag.ATBI_TAG), size);
    	addNumericParam(sheet, linePos++, "ATBI global presence %", percent, percent > 20? STYLE_SESSION_WARNING_CELL : STYLE_SESSION_OK_CELL);
    	
    	Multiset<Tag> operationSets = this.session.getOperationSet();
    	percent = FormulaHelper.percentRound(operationSets.count(OperationTag.OTBI_TAG), size);
    	addNumericParam(sheet, linePos++, "OTBI global presence %", percent, percent > 20? STYLE_SESSION_WARNING_CELL : STYLE_SESSION_OK_CELL);

    	Multiset<String> executorSets = this.session.getExecutorSet();
    	percent = FormulaHelper.percentRound(executorSets.count(ThreadStack.EXECUTOR_TO_BE_IDENTIFIED), size);
    	addNumericParam(sheet, linePos++, "ETBI global presence %", percent, percent > 20? STYLE_SESSION_WARNING_CELL : STYLE_SESSION_OK_CELL);
		
    	addNumericParam(sheet, linePos++, "Interest stack minimum size", this.session.getStackMinimumSize(), this.session.getStackMinimumSize() > 1000 ? STYLE_SESSION_WARNING_CELL : STYLE_SESSION_OK_CELL);
    	
    	size = 0;
    	for (ThreadDump dump : session.getDumps())
    		size += dump.size();
	    percent = FormulaHelper.percentRound(stacks.size(), size);
	    if (percent == 0)
	    	percent = stacks.size() > 0 ? 1 : 0; // adjust to 1 if some identified stacks do exist
	    addNumericParam(sheet, linePos++, "Interest stack %", percent, percent < 5 ? STYLE_SESSION_WARNING_CELL : STYLE_SESSION_OK_CELL);
    	
		return linePos;
	}

	private int displayReportSection(Sheet sheet, int linePos) {
		linePos = displaySectionHeader("Report", sheet, linePos);
		
		addParam(sheet, linePos++, "JZR report id", this.session.getId());
    	addParam(sheet, linePos++, "JZR report start date", formatDate(this.session.getStartDate(), DATE_TIME_DISPLAY_FORMAT));
    	addParam(sheet, linePos++, "JZR report end date", formatDate(this.session.getEndDate(), DATE_TIME_DISPLAY_FORMAT));
    	addParam(sheet, linePos++, "JZR report time zone and source", formatTimeZoneInfo(session.getDisplayTimeZoneInfo()));
    	addParam(sheet, linePos++, "JZR report generation", (new Date()).toString());
    	addParam(sheet, linePos++, "JZR report issuer", this.session.getIssuer());
    	addParam(sheet, linePos++, "JZR report type", this.session.isPostMortemAnalysis() ? "Post Mortem" : "Runtime");
    	addParam(sheet, linePos++, "JZR report origin", this.session.getOrigin());
		
		return linePos;
	}
	
	private int displayStorageSection(Sheet sheet, int linePos) {
		if (!sheetCfg.arePathsExposed())
			return linePos;
		
		linePos = displaySectionHeader("Storage", sheet, linePos);
		
		String tdDir = this.session.getThreadDumpDirectory();		
    	addParam(sheet, linePos++, "Recording directory", SystemHelper.sanitizePathSeparators(tdDir));
		
    	if (this.session.isRecordingFileProvided()){
        	Translator uncompressTranslator = session.lookupTranslator(ConfigDecompression.TYPE_NAME);
        	if (uncompressTranslator != null && uncompressTranslator.isEnabled() && uncompressTranslator.getConfiguration().areTranslatedFilesKept())
        		addParam(sheet, linePos++, "Uncompress directory", SystemHelper.sanitizePathSeparators(uncompressTranslator.getConfiguration().getOuputDirectory()));        	
    	}
    	
    	Translator decryptTranslator = session.lookupTranslator(ConfigDecryption.TYPE_NAME);
    	if (decryptTranslator != null && decryptTranslator.isEnabled() && decryptTranslator.getConfiguration().areTranslatedFilesKept())
    		addParam(sheet, linePos++, "Decryption directory", SystemHelper.sanitizePathSeparators(decryptTranslator.getConfiguration().getOuputDirectory()));
    	
    	Translator deobTranslator = session.lookupTranslator(ConfigDeobfuscation.TYPE_NAME);    	
    	if (deobTranslator != null && deobTranslator.isEnabled() && deobTranslator.getConfiguration().areTranslatedFilesKept())
    		addParam(sheet, linePos++, "Deobfuscation directory", SystemHelper.sanitizePathSeparators(deobTranslator.getConfiguration().getOuputDirectory()));
    	
		String ufoFile = getUFOFilePath();
    	addParam(sheet, linePos++, "Action UFO file", ufoFile);

    	if (session.getMidiFilePath() != null){
    		addParam(sheet, linePos++, "MIDI file path", session.getMidiFilePath());
    	}
    	
		String ignoredStacksFile = getIgnoredStacksFilePath();
    	addParam(sheet, linePos++, "Ignored stacks file", ignoredStacksFile);
    	
		return linePos;
	}
	
	private int displayRecorderLogSection(Sheet sheet, int linePos) {
		if (!isLogInfoAvailable())
			return linePos;
		
		linePos = displaySectionHeader("Recorder log", sheet, linePos);

		addParam(sheet, linePos++, "Log level", readPropertyCardValue(JEYZER_RECORDER_LOG_LEVEL));
		addParam(sheet, linePos++, "Reload active", readPropertyCardValue(JEYZER_RECORDER_LOG_RELOADABLE));
		
		addParam(sheet, linePos++, "Log file active", readPropertyCardValue(JEYZER_RECORDER_LOG_FILE_ACTIVE));
		addParam(sheet, linePos++, "Log file", readPropertyCardValue(JEYZER_RECORDER_LOG_FILE_PATH));
		addParam(sheet, linePos++, "Log file level", readPropertyCardValue(JEYZER_RECORDER_LOG_FILE_LEVEL));
		
		addParam(sheet, linePos++, "Log console active", readPropertyCardValue(JEYZER_RECORDER_LOG_CONSOLE_ACTIVE));
		addParam(sheet, linePos++, "Log console level", readPropertyCardValue(JEYZER_RECORDER_LOG_CONSOLE_LEVEL));
		
		return linePos;
	}

	private int displayVersionsSection(Sheet sheet, int linePos) {
		linePos = displaySectionHeader("Versions", sheet, linePos);
		
    	addParam(sheet, linePos++, "Jeyzer Analyzer", System.getProperty(ConfigAnalyzer.JEYZER_ANALYZER_VERSION));
    	addParam(sheet, linePos++, "Jeyzer Recorder", readPropertyCardValue(JEYZER_RECORDER_VERSION));
    	addParam(sheet, linePos++, "Jeyzer Publisher", readPropertyCardValue(JEYZER_PUBLISHER_VERSION));
    	addParam(sheet, linePos++, "Jeyzer Agent", readPropertyCardValue(JEYZER_AGENT_VERSION));
		
    	return linePos;
	}
	
	private String readPropertyCardValue(String field) {
    	ProcessCard processCard = this.session.getProcessCard();
    	if (processCard != null) {
        	ProcessCardProperty property = processCard.getValue(field);
        	if (property == null)
        		return "Not available - Property not found in the process card";
    		return NOT_SET_VALUE.equals(property.getValue()) ? "Unused / inactive / not set" : property.getValue();
    	}
    	else {
    		return "Not available - No process card";
    	}
	}
	
	private int displayBottom(Sheet sheet, int linePos) {
    	Row bottomRow = sheet.createRow(linePos++);
    	bottomRow.setHeightInPoints(300);
		Cell cell = bottomRow.createCell(1);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_SESSION));
		cell = bottomRow.createCell(2);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_SESSION));
		cell = bottomRow.createCell(3);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_SESSION));
		return linePos;
	}

	private int displaySectionHeader(String title, Sheet sheet, int linePos) {
    	Row row = sheet.createRow(linePos++);
    	row.setHeight(DETAILS_ROW_HEIGHT);
    	
    	Cell cell = row.createCell(1);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_SESSION_HEADER));
		
		cell = row.createCell(2);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_SESSION_HEADER));
		cell.setCellValue(title);
		
		cell = row.createCell(3);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_SESSION_HEADER));

		return linePos;
	}

	private String getIgnoredStacksFilePath() {
		String ignoredStacksFile;

		if (this.session.isIgnoredStacksFileGenerationEnabled()){
			ignoredStacksFile = this.session.getIgnoredStacksFilePath();
			ignoredStacksFile = SystemHelper.sanitizePathSeparators(ignoredStacksFile);
		}
		else{
			ignoredStacksFile = DISABLED_VALUE;
		}

		return ignoredStacksFile;
	}

	private String getUFOFilePath() {
		String ufoFile;
		
		if (this.session.isUFOStacksFileGenerationEnabled()){
			ufoFile = this.session.getUFOFilePath();
			ufoFile = SystemHelper.sanitizePathSeparators(ufoFile);
		}
		else{
			ufoFile = DISABLED_VALUE;
		}

		return ufoFile;
	}

	private int getIdentifiedActionPercentage(Multiset<ThreadStackHandler> stacks){
    	int identifiedFunctionCount = 0;
    	for (ThreadStackHandler stack : stacks.elementSet()) {
    		if (!ThreadStack.FUNC_TO_BE_IDENTIFIED.equals(stack.getThreadStack().getPrincipalTag()))
    			identifiedFunctionCount++;
    	}
    	return FormulaHelper.percentRound(identifiedFunctionCount, stacks.elementSet().size());
	}
	
	private int getIdentifiedOperationPercentage(Multiset<ThreadStackHandler> stacks){
    	int identifiedOperationCount = 0;
    	for (ThreadStackHandler stack : stacks.elementSet()) {
    		if (!ThreadStack.OPER_TO_BE_IDENTIFIED.equals(stack.getThreadStack().getPrincipalOperation()))
    			identifiedOperationCount++;
    	}
    	return FormulaHelper.percentRound(identifiedOperationCount, stacks.elementSet().size());
	}
	
	private int getIdentifiedExecutorPercentage(Multiset<ThreadStackHandler> stacks){
    	int identifiedExecutorCount = 0;
    	for (ThreadStackHandler stack : stacks.elementSet()) {
    		if (!ThreadStack.EXECUTOR_TO_BE_IDENTIFIED.equals(stack.getThreadStack().getExecutor()))
    			identifiedExecutorCount++;
    	}
    	return FormulaHelper.percentRound(identifiedExecutorCount, stacks.elementSet().size());
	}
	
    private XSSFRichTextString formatTimeZoneInfo(TimeZoneInfo timeZoneInfo) {
    	if (timeZoneInfo.isUnknown())
    		return new XSSFRichTextString("Unknown");
    	
    	String value = timeZoneInfo.getZoneAbbreviation() + " " + timeZoneInfo.getDisplayOrigin(); 
    	XSSFRichTextString richText = new XSSFRichTextString(value); 
    	
        Font sessionCellFont = workbook.createFont();
        sessionCellFont.setFontHeightInPoints((short)12);

        Font sessionSmallCellFont = workbook.createFont();
        sessionSmallCellFont.setFontHeightInPoints((short)9);
        
    	richText.applyFont(0, timeZoneInfo.getZoneAbbreviation().length(), sessionCellFont);
    	richText.applyFont(timeZoneInfo.getZoneAbbreviation().length()+1, value.length(), sessionSmallCellFont);
    	
		return richText;
	}

	private void addParam(Sheet sheet, int linePos, String name, String param){
    	Row row = sheet.createRow(linePos);
    	row.setHeight(DETAILS_ROW_HEIGHT);
		addSessionHeader(row, name);
		addCell(row, 2, param, STYLE_SESSION_CELL);
		
		Cell cell = row.createCell(3);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_SESSION));
    }

	private void addParam(Sheet sheet, int linePos, String name, XSSFRichTextString param){
    	Row row = sheet.createRow(linePos);
    	row.setHeight(DETAILS_ROW_HEIGHT);
		addSessionHeader(row, name);
		addCell(row, 2, param, STYLE_SESSION_CELL);
		
		Cell cell = row.createCell(3);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_SESSION));
    }
    
    private void addCommentedParam(Sheet sheet, int linePos, String name, String param, String style, String comment){
    	Row row = sheet.createRow(linePos);
    	short height = 600;
    	row.setHeight(height);
		addSessionHeader(row, name);
		Cell cell = addCell(row, 2, param, style);
		if (comment != null)
			addComment(sheet, cell, comment, linePos, 2);
		
		cell = row.createCell(3);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_SESSION));
    }

    private void addTextParam(Sheet sheet, int linePos, String name, String param){
    	Row row = sheet.createRow(linePos);
    	if (!param.contains("\n"))
    		row.setHeight(DETAILS_ROW_HEIGHT);	
    	addSessionHeader(row, name);
		addCell(row, 2, param, STYLE_SESSION_CELL_TEXT);
		
		Cell cell = row.createCell(3);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_SESSION));
    }    
    
    private void addNumericParam(Sheet sheet, int linePos, String name, int param, String style){
    	Row row = sheet.createRow(linePos);
    	row.setHeight(DETAILS_ROW_HEIGHT);
    	addSessionHeader(row, name);

    	Cell cell = addCell(row, 2, param, style);
		cell.getCellStyle().setAlignment(HorizontalAlignment.CENTER); // re-align

		cell = row.createCell(3);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_SESSION));
    }

    private void addSessionHeader(Row row, String name) {
		Cell cell = row.createCell(1);
		cell.setCellValue(name);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_SESSION));
	}
    
    private int getMissingDumpsCount(){
    	int count = 0;
		for (ThreadDump dump : session.getDumps()){
			if (dump.hasHiatusBefore()){
				count++;
			}
		}
		return count;
    }
    
    private int getApplicativeRestartCount(){
    	int count = 0;
		for (ThreadDump dump : session.getDumps()){
			if (dump.isRestart()){
				count++;
			}
		}
		return count;
    }
    
	private boolean isLogInfoAvailable() {
    	ProcessCard processCard = this.session.getProcessCard();
    	if (processCard != null) {
        	ProcessCardProperty property = processCard.getValue(JEYZER_RECORDER_LOG_FILE_PATH);
    		return property != null && property.getValue()!=null && !property.getValue().isEmpty();
    	}
    	else {
    		return false;
    	}
	}
}
