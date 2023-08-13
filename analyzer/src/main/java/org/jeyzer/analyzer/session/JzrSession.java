package org.jeyzer.analyzer.session;

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



import static org.jeyzer.analyzer.math.FormulaHelper.*;
import static org.jeyzer.analyzer.util.SystemHelper.CR;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jeyzer.analyzer.config.ConfigAnalyzer;
import org.jeyzer.analyzer.config.analysis.ConfigStackSorting.StackSortingKey;
import org.jeyzer.analyzer.config.ConfigState;
import org.jeyzer.analyzer.config.ConfigThreadLocal;
import org.jeyzer.analyzer.config.translator.ConfigTranslator;
import org.jeyzer.analyzer.config.translator.obfuscation.ConfigDeobfuscation;
import org.jeyzer.analyzer.data.AnalysisContext;
import org.jeyzer.analyzer.data.AnalysisPatternsStats;
import org.jeyzer.analyzer.data.ProcessCard;
import org.jeyzer.analyzer.data.ProcessCommandLine;
import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.ThreadStackGroupAction;
import org.jeyzer.analyzer.data.TimeZoneInfo;
import org.jeyzer.analyzer.data.ProcessCard.ProcessCardProperty;
import org.jeyzer.analyzer.data.TimeZoneInfo.TimeZoneOrigin;
import org.jeyzer.analyzer.data.event.ExternalEvent;
import org.jeyzer.analyzer.data.event.JzrPublisherEvent;
import org.jeyzer.analyzer.data.flags.JVMFlags;
import org.jeyzer.analyzer.data.jar.ProcessJars;
import org.jeyzer.analyzer.data.module.ProcessModules;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStack.ThreadStackHandler;
import org.jeyzer.analyzer.data.tag.ContentionTypeTag;
import org.jeyzer.analyzer.data.tag.FunctionTag;
import org.jeyzer.analyzer.data.tag.OperationTag;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.error.JzrEmptyRecordingException;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.error.JzrExecutionException;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrMasterProfileRedirectException;
import org.jeyzer.analyzer.error.JzrMonitorException;
import org.jeyzer.analyzer.error.JzrNoThreadDumpFileFound;
import org.jeyzer.analyzer.error.JzrTranslatorException;
import org.jeyzer.analyzer.input.translator.TranslateData;
import org.jeyzer.analyzer.input.translator.Translator;
import org.jeyzer.analyzer.input.translator.TranslatorsFactory;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.output.ReportDescriptor;
import org.jeyzer.analyzer.parser.JMXStackParser;
import org.jeyzer.analyzer.parser.ThreadDumpParser;
import org.jeyzer.analyzer.parser.ThreadDumpParserFactory;
import org.jeyzer.analyzer.parser.io.ThreadDumpFileDateComparator;
import org.jeyzer.analyzer.parser.io.ThreadDumpFileDateHelper;
import org.jeyzer.analyzer.parser.io.SnapshotFileNameFilter;
import org.jeyzer.analyzer.rule.Rule;
import org.jeyzer.analyzer.setup.JzrSetupManager;
import org.jeyzer.analyzer.status.JeyzerStatusEvent;
import org.jeyzer.analyzer.status.JeyzerStatusEventDispatcher;
import org.jeyzer.analyzer.util.SystemHelper;
import org.jeyzer.analyzer.util.TimeZoneInfoHelper;
import org.jeyzer.monitor.JeyzerMonitor;
import org.jeyzer.monitor.config.applicative.MonitorApplicativeRuleFilter;
import org.jeyzer.monitor.engine.event.MonitorAnalyzerEvent;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.Level;
import org.jeyzer.monitor.engine.rule.MonitorAnalyzerRule;
import org.jeyzer.monitor.engine.rule.MonitorRule;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;
import org.jeyzer.monitor.impl.rule.MonitorExternalRuleBuilder;
import org.jeyzer.monitor.impl.rule.MonitorPublisherRuleBuilder;
import org.jeyzer.monitor.sticker.RuleBlockerSticker;
import org.jeyzer.monitor.sticker.Sticker;
import org.jeyzer.monitor.util.MonitorHelper;
import org.jeyzer.profile.master.MasterProfile;
import org.jeyzer.profile.master.MasterRepository;
import org.jeyzer.service.action.id.ActionIdGenerator;
import org.jeyzer.service.action.id.StackGroupActionIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;


public class JzrSession implements JzrMonitorSession {

	private static final Logger logger = LoggerFactory.getLogger(JzrSession.class);	
	
	public static final String NA = "Not available";
	
	public static final String UFO_FILE = "ufo-stacks.txt";
	public static final String UFO_ZIP_FILE = "ufo-stacks.zip";
	public static final String ACTION_FILE = "actions.txt";
	public static final String IGNORED_STACKS_FILE = "ignored-stacks.txt";
	public static final String IGNORED_STACKS_ZIP_FILE = "ignored-stacks.zip";
	public static final String FILE_ZIP_EXTENSION = ".zip";	
	public static final String FILE_GZ_EXTENSION = ".tar.gz";
	public static final String UFO_ORDER_BY_FILE = "file";
	public static final String UFO_ORDER_BY_FREQUENCY = "frequency";
	
	public static final AtomicLong nextId = new AtomicLong(0);
	private static final String PROPERTY_JEYZER_ANALYSIS_ID = "JEYZER_ANALYSIS_ID";
	private String id; // unique id
	
	private String origin; // Jeyzer Web Analyzer, Jeyzer Analyzer, Jeyzer Monitor, Jeyzer Monitor Console.. 
	
	private boolean discoveryModeEnabled;
	
	private String recordingDirPath;
	private String recordingFileName; // optional
	private boolean ufoStacksFileGenerationEnabled;
	private String ufoOutputDirPath;
	private String ufoZipFile;
	private String ufoOrderBy;
	
	private String description;
	private String issuer;
	
	private boolean ignoredStacksGenerationEnabled;
	private String ignoredStacksOutputDir;
	
	private StackSortingKey stackSortingKey;
	
	private List<Translator> translators = new ArrayList<>();
	
	private String applicationType;
	private String applicationId;
	private String format;
	private String formatShortName;

	private int tdPeriod = -1;
	private Boolean validTDConfiguredPeriod = null;
	
	private int tdDetectedPeriod = -1;
	private Boolean validTDDetectedPeriod = null;
	
	private Date sinceDate = null;
	
	private TimeZoneInfo displayTimeZoneInfo;
	private TimeZoneInfo recordingTimeZoneInfo;
	
	private boolean recordingTimeZoneUserSpecified;
	private String recordingTimeZoneId; // can be null
	private boolean displayTimeZoneUserSpecified;
	private String displayTimeZoneId; // can be null
	
	private Date startDate;
	private Date endDate;
	private List<ThreadDump> dumps = new ArrayList<>();
	private Map<Date, ThreadDump> dumpHistory = new HashMap<>();
	
	private int actionTotal = 0; //computed
	private int actionStackTotal = 0; //computed, includes the virtual thread stacks, used for data manipulation (stats..)
	private int actionStackTotalNotVirtualExpanded = 0; //computed, virtual thread stacks represented only once, used for display
	private int stackMinimumSize = -1; //computed
	
	private ProcessCard processCard = null;	
	private ProcessCommandLine processCommandLine = null;
	
	private ProcessJars processJars = null;
	private ProcessModules processModules = null;
	private JVMFlags jvmFlags = null;
	
	private boolean moduleSupport;
	
	// Carrier (and possibly virtual) threads
	private boolean virtualThreadPresence;
	// Virtual threads
	private boolean virtualThreadsAvailable;
	
	private String midiFilePath;

	private long jumpLimit = -1;
	
	private Map<String,ThreadAction> openActions = new HashMap<>();
	private Map<Date,Set<ThreadAction>> actionHistory = new HashMap<>();

	private Map<Date,Set<ThreadStackGroupAction>> stackGroupActionHistory = null;
	
	private Multiset<Tag> functionSets = null;
	private Multiset<Tag> principalFunctionSets = null;
	private Multiset<Tag> operationSets = null;
	private Multiset<Tag> contentionTypeSets = null;
	private Multiset<String> executorSets = null;
	
	private AnalysisPatternsStats analysisPatternStats  = new AnalysisPatternsStats();
	
	private Multimap<String, Tag> contentionTypeSetPerFunctionPrincipal = null;
	private Multimap<String, Tag> operationSetPerFunctionPrincipal = null;
	private Multimap<String, Tag> functionSetPerFunctionPrincipal = null;
	private Multimap<String, ThreadAction> actionsPerFunctionPrincipal = null;
	
	private Multimap<String, Tag> contentionTypeSetPerExecutor = null;
	private Multimap<String, Tag> operationSetPerExecutor = null;
	private Multimap<String, Tag> functionSetPerExecutor = null;
	private Multimap<String, ThreadAction> actionsPerExecutor = null;
	
	private List<MonitorTaskRule> monitorApplicativeTaskRules = null;
	private List<MonitorSessionRule> monitorApplicativeSessionRules = null;
	private List<MonitorSystemRule> monitorApplicativeSystemRules = null;
	private List<MonitorSessionRule> monitorPublisherRules = null;
	
	private ThreadDumpParser parser;
	
	private JzrSetupManager setupMgr;
	private JeyzerStatusEventDispatcher eventDispatcher;
	
	private MasterRepository masterRepository; // can be null
	private boolean profileDiscoveryEnabled;
	private String redirectedFromProfile; // can be null
	
	private Level highestEventCategory = Level.UNKNOWN;  // lowest 
	
	private ConfigDeobfuscation deobfuscationCfg;
	
	private ConfigState analyzerConfigurationState;
	
	public JzrSession(
			JzrSetupManager setupMgr, 
			ConfigAnalyzer analyzerCfg, 
			Date sinceDate,
			JeyzerStatusEventDispatcher eventDispatcher,
			AnalysisContext analysisContext
		)
	{
		this.origin= analysisContext.getOrigin();
		this.id = loadId();
		
		this.setupMgr = setupMgr;
		this.eventDispatcher = eventDispatcher;
		this.masterRepository = analysisContext.getMasterRepository();
		this.profileDiscoveryEnabled = analyzerCfg.getProfileDiscoveryCfg().isRedirectEnabled();
		this.redirectedFromProfile = analysisContext.getRedirectFrom();
		this.applicationId = analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_APPLICATION_ID);
		this.applicationType = analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_APPLICATION_TYPE);
		this.analyzerConfigurationState = analyzerCfg.getState();
		this.recordingDirPath = analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_JEYZER_RECORD_DIRECTORY).replace('\\', '/');
		this.recordingFileName = analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_JEYZER_RECORD_FILE);
		this.description = analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_DESCRIPTION);
		this.issuer = analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_ISSUER);
		this.tdPeriod = analyzerCfg.getThreadDumpPeriod();
		this.sinceDate =  (sinceDate != null? sinceDate:SystemHelper.getUnixEpochDate());  // if not provided = all files
		
		this.discoveryModeEnabled = Boolean.parseBoolean(analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_DISCOVERY_MODE_ENABLED));
		
		this.ufoOutputDirPath = analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_UFO + ConfigAnalyzer.JZRA_UFO_DIRECTORY).replace('\\', '/');
		this.ufoZipFile = this.ufoOutputDirPath + "/" + UFO_ZIP_FILE;
		this.ufoStacksFileGenerationEnabled = Boolean.valueOf(analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_UFO + ConfigAnalyzer.JZRA_UFO_THREAD_STACK_GENERATION_ENABLED));
		this.ufoOrderBy = analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_UFO + ConfigAnalyzer.JZRA_UFO_ORDER_BY).replace('\\', '/');
		
		this.ignoredStacksGenerationEnabled = Boolean.valueOf(analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_THREAD_STACK_GENERATION_ENABLED));
		this.ignoredStacksOutputDir = analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_THREAD_STACK_OUTPUT_DIR);
		
		this.stackSortingKey = analyzerCfg.getStackSortingCfg().getStackSortingKey(setupMgr.getDefaultStackSortingKey());

		this.recordingTimeZoneUserSpecified = Boolean.valueOf(analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_RECORDING_TIME_ZONE_USER_SPECIFIED));
		this.recordingTimeZoneId = analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_RECORDING_TIME_ZONE);
		this.displayTimeZoneUserSpecified = Boolean.valueOf(analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_DISPLAY_TIME_ZONE_USER_SPECIFIED));
		this.displayTimeZoneId = analyzerCfg.getParamValue(ConfigAnalyzer.JZRA_DISPLAY_TIME_ZONE);
	}

	@Override
	public List<ThreadDump> getDumps(){
		return this.dumps;
	}
	
	public ThreadDump getDump(Date date){
		return this.dumpHistory.get(date);
	}

	public ProcessCard getProcessCard() {
		return processCard;  // can be null
	}
	
	public ProcessCommandLine getProcessCommandLine() {
		return processCommandLine;
	}	

	public ProcessJars getProcessJars() {
		return processJars; // can be null
	}
	
	public ProcessModules getProcessModules() {
		return processModules; // can be null
	}
	
	public JVMFlags getJVMFlags() {
		return jvmFlags; // can be null
	}

	public List<Translator> getTranslators() {
		return translators;
	}
	
	public Translator lookupTranslator(String type) {
		for (Translator translator : translators)
			if (translator.getConfiguration().getType().equals(type)){
				return translator;
			}
		return null;
	}

	public String getMidiFilePath() {
		return midiFilePath;
	}

	public void setMidiFilePath(String midiFilePath) {
		this.midiFilePath = midiFilePath;
	}	

	public Map<Date,Set<ThreadAction>> getActionHistory(){
		return this.actionHistory;
	}
	
	public int getActionsSize(){
		return this.actionTotal;
	}

	public int getActionsStackSize(){
		return this.actionStackTotal;
	}

	public int getActionsStackSize(boolean expandVirtual){
		if (expandVirtual)
			return getActionsStackSize();
		else
			return this.actionStackTotalNotVirtualExpanded;
	}	
	
	public Map<Date,Set<ThreadStackGroupAction>> getThreadStackGroupActionHistory(){
		if (this.stackGroupActionHistory != null)
			return this.stackGroupActionHistory;

		this.stackGroupActionHistory = new HashMap<>(this.actionHistory.size());
		Map<ThreadStackHandler,ThreadStackGroupAction> openStackGroupActions = new HashMap<>();
		StackGroupActionIdGenerator idGenerator = new StackGroupActionIdGenerator();
		
		for (ThreadDump dump : this.dumps){
			this.stackGroupActionHistory.put(dump.getTimestamp(), new TreeSet<ThreadStackGroupAction>(new ThreadStackGroupAction.ThreadStackGroupActionComparator()));
			dump.generateStackGroupActions(openStackGroupActions, this.stackGroupActionHistory, idGenerator);
		}
		
		// close remaining actions
		Set<ThreadStackHandler> stackIds = openStackGroupActions.keySet();
		for (ThreadStackHandler stackId : stackIds){
			ThreadStackGroupAction ta = openStackGroupActions.get(stackId);
			ta.setMaxEndDate(this.endDate);
			Set<ThreadStackGroupAction> archivedActions =  this.stackGroupActionHistory.get(ta.getStartDate());
			archivedActions.add(ta);
		}
		openStackGroupActions.clear();
		
		return this.stackGroupActionHistory;
	}
	
	public String getOrigin() {
		return this.origin;
	}
	
	@Override
	public Date getStartDate(){
		return this.startDate;
	}

	@Override
	public Date getEndDate(){
		return this.endDate;
	}	

	@Override
	public TimeZoneInfo getDisplayTimeZoneInfo() {
		return displayTimeZoneInfo;
	}
	
	public TimeZoneInfo getRecordingTimeZoneInfo() {
		return recordingTimeZoneInfo;
	}

	public String getApplicationType() {
		return applicationType;
	}

	public String getApplicationId() {
		return applicationId;
	}
	
	public String getId() {
		return id;
	}
	
	public ConfigState getConfigurationState() {
		return analyzerConfigurationState;
	}
	
	public String getDescription() {
		if (this.description == null || this.description.isEmpty())
			return NA;
		return description;
	}
	
	public String getIssuer() {
		if (this.issuer == null || this.issuer.isEmpty())
			return NA;
		return issuer;
	}	
	
	public String getFormat() {
		return format;
	}

	public String getFormatShortName() {
		return formatShortName;
	}
	
	public Level getHighestEventCategory() {
		return highestEventCategory;
	}

	public void setHighestEventCategory(Level highestEventCategory) {
		this.highestEventCategory = highestEventCategory;
	}

	public String getThreadDumpDirectory(){
		return this.recordingDirPath;
	}
	
	public String getRecordingFileName(){
		return this.recordingFileName;
	}
	
	public boolean isRecordingFileProvided(){
		return this.recordingFileName != null && !this.recordingFileName.isEmpty();
	}

	@Override
	public int getThreadDumpPeriod(){
		return (this.tdPeriod == -1)? this.tdDetectedPeriod : this.tdPeriod; 
	}

	public int getConfiguredThreadDumpPeriod(){
		return this.tdPeriod;
	}
	
	public Boolean isValidConfiguredThreadDumpPeriod(){
		// may be null
		return this.validTDConfiguredPeriod != null ? this.validTDConfiguredPeriod : false;
	}
	
	public int getDetectedThreadDumpPeriod(){
		return this.tdDetectedPeriod;
	}

	public Boolean isValidDetectedThreadDumpPeriod(){
		return this.validTDDetectedPeriod;
	}
	
	public boolean isDiscoveryModeEnabled(){
		return this.discoveryModeEnabled;
	}
	
	public boolean isMasterProfileDiscoveryEnabled() {
		return this.profileDiscoveryEnabled;
	}

	public boolean isMasterProfileRedirected() {
		return this.redirectedFromProfile != null;
	}
	
	public String getMasterProfileRedirectedFrom() {
		return this.redirectedFromProfile; // can be null
	}
	
	public boolean isMasterRepositoryAvailable() {
		return this.masterRepository != null;
	}
	
	public boolean isJavaModuleSupported(){
		return this.moduleSupport;
	}

	public boolean isUFOStacksFileGenerationEnabled() {
		return this.ufoStacksFileGenerationEnabled;
	}
	
	public String getUFOFilePath(){
		return this.ufoZipFile;
	}
	
	public boolean isIgnoredStacksFileGenerationEnabled(){
		return this.ignoredStacksGenerationEnabled;
	}
	
	public String getIgnoredStacksFilePath(){
		return ignoredStacksGenerationEnabled ? this.ignoredStacksOutputDir + "/" + IGNORED_STACKS_ZIP_FILE : null;
	}
	
	public long getThreadDumpPeriodJumpLimit(){
		if (jumpLimit ==-1){
			long period = getThreadDumpPeriod()*1000L;
			if (period*0.3>1000)
				this.jumpLimit = (long)(period*1.4); // take some extra point
			else
				this.jumpLimit = period + 1000;  // minimum is period + 1 sec
		}
		return jumpLimit;
	}
	
	
	public ConfigDeobfuscation getConfigTDDeobfuscation(){
		return this.deobfuscationCfg;
	}

	public boolean isCPUInfoAvailable(){
		return parser.isCPUMeasurementUsed();
	}
	
	public boolean isMemoryInfoAvailable(){
		return parser.isMemoryMeasurementUsed();
	}

	public boolean isBiasedInfoAvailable(){
		return parser.isBiasedLockUsed();
	}

	public boolean areVirtualThreadVariationCountersAvailable(){
		return parser.areVirtualThreadVariationCountersUsed();
	}

	public boolean hasVirtualThreadPresence() {
		return this.virtualThreadPresence;
	}
	
	public boolean hasVirtualThreads() {
		return this.virtualThreadsAvailable;
	}

	public boolean isJeyzerMXInfoAvailable(){
		return parser.isJeyzerMXUsed();
	}
	
	public boolean isPostMortemAnalysis(){
		return SystemHelper.getUnixEpochDate().equals(this.sinceDate);
	}
	
	public boolean isProcessUpTimeMeasurementUsed(){
		return parser.isProcessUpTimeMeasurementUsed();
	}

	public void build(ConfigAnalyzer analyzerCfg) throws JzrInitializationException, JzrExecutionException{
		logger.info("Loading session for analysis id : " + this.id);
		
		SnapshotFileNameFilter filter = new SnapshotFileNameFilter(analyzerCfg.getSnapshotFilePatterns());
		TranslateData inputData = initializeInputData(filter, analyzerCfg.getTranslatorInputFileExtensions());
		
		// apply the translators
		TranslateData outputData = inputData;		
		for (ConfigTranslator translatorCfg : analyzerCfg.getTranslators()){
			List<Translator> transls = TranslatorsFactory.createTranslators(translatorCfg, inputData);
			this.translators.addAll(transls);
			for (Translator translator : transls){
				this.eventDispatcher.fireStatusEvent(translator.getStatusEventState());
				outputData = translator.translate(inputData, filter, this.sinceDate);
				inputData = outputData;
			}
		}

		// Optional, usually for generic profiles
		redirectProfile(outputData.getTDs());
		
		// detect the thread dump period 
		detectThreadDumpPeriod(outputData.getTDs(), filter);
		
		// load the process card (optional) and process command line
		processCard = ProcessCard.loadProcessCard(outputData.getProcessCard()); // can be null
		processCommandLine = new ProcessCommandLine(processCard);
		
		processJars = ProcessJars.loadProcessJars(outputData.getProcessJarPaths()); // can be null
		processModules = ProcessModules.loadProcessModules(outputData.getProcessModules()); // can be null
		
		jvmFlags = JVMFlags.loadJVMFlags(outputData.getJVMFlags()); // can be null
		if (jvmFlags != null && processCard != null)
			processCard.addProperties(jvmFlags.getProperties()); // prefixed props
		
		this.eventDispatcher.fireStatusEvent(JeyzerStatusEvent.STATE.PARSING);

		// get first thread dump. If none, throw exception
		File candidate = getFirstSnapshotFile(outputData.getDirectory(), outputData.getTDs());		
		
		// create the thread dump parser
		parser = ThreadDumpParserFactory.getThreadDumpParser(this.setupMgr, candidate);  // can be obfuscated, not an issue
		this.format = parser.getFormatName();
		this.formatShortName = parser.getFormatShortName(); 
		
		// parse the files
		dumps = parser.parseThreadDumpFiles(outputData.getTDs(), filter, this.sinceDate);
		
		if (dumps.isEmpty()){
			logger.error("No thread dump file found in : " + outputData.getDirectory().getPath());
			throw new JzrNoThreadDumpFileFound("No thread dump file found in : " + outputData.getDirectory().getPath());
		}

		// sort dumps by date
		Collections.sort(dumps, new ThreadDump.ThreadDumpComparator());
		
		// Sort stacks in dumps
		for (ThreadDump dump : dumps)
			dump.sortStacks(this.stackSortingKey);
		
		// fill thread dump history
		for(ThreadDump dump : dumps)
			this.dumpHistory.put(dump.getTimestamp(), dump);

		// set pre and post time slices (capture time ignored)
		updateThreadDumpTimestamps();
		
		computeTimeZones(filter);
		
		// inject the GC names in the process card
		addGCNames();
		
		// handle the virtual thread presence
		detectVirtualThreadSupport();
		
		this.startDate = dumps.get(0).getTimestamp();
		this.endDate = dumps.get(dumps.size()-1).getTimestamp();
	}

	private void addGCNames() {
		if (processCard == null || !parser.isGarbageCollectionMeasurementUsed())
			return;
		
		String oldName = null;
		String youngName = null;
		for (ThreadDump td : dumps) {
			if (td.getGarbageCollection() == null)
				continue;

			// check old
			if (td.getGarbageCollection().getOldGarbageCollectorInfo() != null && oldName == null)
				oldName = td.getGarbageCollection().getOldGarbageCollectorInfo().getName();
			
			// check young
			if (td.getGarbageCollection().getYoungGarbageCollectorInfo() != null && youngName == null)
				youngName = td.getGarbageCollection().getYoungGarbageCollectorInfo().getName();
			
			if (oldName != null && youngName != null)
				break;
		}

		Properties props = new Properties();
		if (oldName != null)
			props.put(ProcessCard.GC_OLD_NAME, oldName);
		if (youngName != null)
			props.put(ProcessCard.GC_YOUNG_NAME, youngName);
		processCard.addProperties(props);
	}
	
	private void detectVirtualThreadSupport() {
		// optimize
		if (parser.hasVirtualThreadSupport()) {
			for (ThreadDump dump : this.dumps) {
				if (dump.hasVirtualThreadPresence()) {
					// 1. set the virtual thread presence
					virtualThreadPresence = true;
					break;
				}
			}
		}

		// 2. determine if we have virtual threads in dumps
		if (virtualThreadPresence) {
			for (ThreadDump dump : this.dumps) {
				if (dump.hasVirtualThreads()) {
					virtualThreadsAvailable = true;
					break;
				}
			}
		}
		
		// 3. Enrich the property card for monitoring rules
		if (processCard != null) {
			Properties props = new Properties();
			
			props.put(ProcessCard.VIRTUAL_THREAD_PRESENCE, Boolean.toString(virtualThreadPresence));
			
			if (virtualThreadPresence)
				props.put(ProcessCard.VIRTUAL_THREAD_CARRIERS_ONLY, Boolean.toString(!virtualThreadsAvailable));
			
			processCard.addProperties(props);
		}
	}

	private void redirectProfile(File[] tDs) throws JzrMasterProfileRedirectException {
		if (tDs.length == 0 || !this.profileDiscoveryEnabled || this.masterRepository == null)
			return;
		
		List<MasterProfile> masterProfiles = this.masterRepository.getMasterProfiles(this.applicationType);
		if (masterProfiles.isEmpty())
			return;
		
		for(MasterProfile profile : masterProfiles) {
			for (String pattern : profile.getRedirectionPatterns()) {
				// scan only the first 5 TDs
				for (int i=0; i<tDs.length; i++) {
					try (
							FileReader fr = new FileReader(tDs[i]);
							BufferedReader reader = new BufferedReader(fr);
						)
					{
						String line;
						while ((line = reader.readLine()) != null) {
							if (line.contains(pattern))
								throw new JzrMasterProfileRedirectException(profile.getType());
						}
					} catch (IOException e) {
						logger.warn("Redirect profile failed to read the snapshot file : {}", tDs[i].getAbsolutePath());
					}
				}
			}
		}
	}

	private void computeTimeZones(SnapshotFileNameFilter filter) {
		logger.info("Time zones :");
		computeRecordingTimeZone(filter);
		computeDisplayTimeZone();
		logger.info(" - Analyzer local time zone : {}", new GregorianCalendar().getTimeZone().getID());
	}

	private void computeRecordingTimeZone(SnapshotFileNameFilter filter) {
		// get the time zone origin. At this stage, empty files have been discarded. 
		this.recordingTimeZoneInfo = TimeZoneInfoHelper.getTimeZoneInfo(filter, dumps.get(0).getFileName());
		
		if (this.recordingTimeZoneInfo.isUnknown() && this.recordingTimeZoneId != null) {
			// get it from the user or from the configuration
			if (this.recordingTimeZoneUserSpecified)
				this.recordingTimeZoneInfo = new TimeZoneInfo(TimeZoneOrigin.USER_SELECTED, recordingTimeZoneId);
			else
				this.recordingTimeZoneInfo = new TimeZoneInfo(TimeZoneOrigin.PROFILE, recordingTimeZoneId);
		}
		
		logger.info(" - Recording time zone and origin : {} / {}", this.recordingTimeZoneInfo.getZoneAbbreviation(), this.recordingTimeZoneInfo.getDisplayOrigin());
	}
	
	private void computeDisplayTimeZone() {
		if (this.displayTimeZoneId != null) {
			// get it from the user or from the configuration
			if (this.displayTimeZoneUserSpecified)
				this.displayTimeZoneInfo = new TimeZoneInfo(TimeZoneOrigin.USER_SELECTED, displayTimeZoneId);
			else
				this.displayTimeZoneInfo = new TimeZoneInfo(TimeZoneOrigin.PROFILE, displayTimeZoneId);
		}
		else {
			// defaulting to the recording one
			this.displayTimeZoneInfo = this.recordingTimeZoneInfo;
		}

		// And update the thread local time zone in MonitorHelper, otherwise keep local one
		if (!this.displayTimeZoneInfo.isUnknown())
			MonitorHelper.setMonitoredProcessTimeZone(displayTimeZoneInfo.getZone());
		
		logger.info(" - Display time zone and origin : {} / {}", this.displayTimeZoneInfo.getZoneAbbreviation(), this.displayTimeZoneInfo.getDisplayOrigin());
	}

	private TranslateData initializeInputData(SnapshotFileNameFilter filter, List<String> supportedInputExtensions) throws JzrInitializationException, JzrTranslatorException, JzrNoThreadDumpFileFound, JzrEmptyRecordingException {
		if (isRecordingFileProvided()){
			// check translator support
			checkTranslatorSupport(this.recordingFileName, supportedInputExtensions);
			
			// zip or gzip file provided
			checkRecording(this.recordingDirPath, this.recordingFileName);
			
			return new TranslateData(
					null,
					null,
					null,
					null,
					null,
					new File(this.recordingDirPath + File.separator + this.recordingFileName)
					);
		}else{
			// thread dump directory provided
			checkRecordingDirectory(this.recordingDirPath);
			
			File recordingDirectory = new File(this.recordingDirPath);
			return new TranslateData(
					listFiles(recordingDirectory, filter),
					getFile(recordingDirectory, ProcessCard.PROCESS_CARD_FILE_NAME),
					getFile(recordingDirectory, ProcessJars.PROCESS_JAR_PATHS_FILE_NAME),
					getFile(recordingDirectory, ProcessModules.PROCESS_MODULES_FILE_NAME),
					getFile(recordingDirectory, JVMFlags.JVM_FLAGS_FILE_NAME),
					recordingDirectory
					);
		}
	}

	private File getFile(File dir, String fileName) {
		File file = new File(dir + File.separator + fileName);
		if (file.exists() && file.isFile())
			return file;
		else
			return null;
	}

	private File getFirstSnapshotFile(File dir, File[] files) throws JzrNoThreadDumpFileFound {
		for (File file : files){
			if (file.length() == 0 
					|| file.getName().endsWith(FILE_ZIP_EXTENSION) 
					|| file.getName().endsWith(FILE_GZ_EXTENSION))
				continue;
			return file; 
		}
		
		noSnapshotsDetected(dir.getPath(), null);
		return null; // will never reach this point
	}

	private void noSnapshotsDetected(String path, Set<String> supportedFormats) throws JzrNoThreadDumpFileFound {
		List<String> monitorOrigins = Arrays.asList("Jeyzer Monitor", "Jeyzer Monitor Console");
		// prefer a warning log trace in a monitoring session
		if (monitorOrigins.contains(this.origin))
			logger.warn("No recording snapshot file found in : {}", path);
		else
			logger.error("No recording snapshot file found in : {}", path);
		throw new JzrNoThreadDumpFileFound("No recording snapshot file found in : " + path, supportedFormats);
	}

	private File[] listFiles(File dir, SnapshotFileNameFilter filter) throws JzrNoThreadDumpFileFound {
		File[] files = null;
		
		files =	dir.listFiles(filter);
		
		if (files == null || files.length == 0)
			noSnapshotsDetected(dir.getPath(), filter.getSupportedFileFormats());

		// sort files by date (date provided by the filter) 
		Arrays.sort(files, new ThreadDumpFileDateComparator(filter));
		
		return files;
	}

	private void checkRecordingDirectory(String path) throws JzrInitializationException{
		if (path == null || path.isEmpty()){
			logger.error("Recording directory is mandatory. Please set it.");
			throw new JzrInitializationException("Recording directory is mandatory. Please set it."); 
		}
		
		File dir = new File(path);
		
		if (!dir.exists()){
			logger.error("Recording directory not found : {}", dir.getPath());
			throw new JzrInitializationException("Recording directory not found : " + dir.getPath()); 
		}
		
		if (!dir.isDirectory()){
			logger.error("Recording directory is not a directory but a file : {}", dir.getPath());
			throw new JzrInitializationException("Recording directory is not a directory but a file : " + dir.getPath()); 
		}
	}
	
	private void checkRecording(String path, String fileName) throws JzrInitializationException, JzrEmptyRecordingException {
		checkRecordingDirectory(path);
		
		File file = new File(path + File.separator + fileName);
		
		if (file.length() == 0){
			logger.error("Recording file is empty : {}", file.getPath());
			throw new JzrEmptyRecordingException("Recording file is empty: " + file.getPath()); 
		}
		
		if (!file.exists()){
			logger.error("Recording file not found : {}", file.getPath());
			throw new JzrInitializationException("Recording file not found : " + file.getPath()); 
		}
		
		if (file.isDirectory()){
			logger.error("Recording is not a file but a directory : {}", file.getPath());
			throw new JzrInitializationException("Recording is not a file but a directory : " + file.getPath());
		}
	}
	
	private void checkTranslatorSupport(String fileName, List<String> supportedInputExtensions) throws JzrTranslatorException {
		for (String extension : supportedInputExtensions) {
			if (fileName.toLowerCase().endsWith(extension)) {
				return;
			}
		}
		
		logger.error("Recording extension is not supported by any translator : {}", fileName);
		throw new JzrTranslatorException("the recording file extension is not recognized by Jeyzer. Please make sure that Jeyzer supports this file format. Otherwise ask the administrator to check the analysis profile translators.");
	}

	private void updateThreadDumpTimestamps() {
		ThreadDump td;
		Date previousTimestamp = null;
		Date nextTimestamp = null;
		int size = this.dumps.size();
		int next;
		
		for (int i=0; i<size; i++){
			next = i + 1;
			nextTimestamp = (next < size) ? this.dumps.get(next).getTimestamp() : null;
			td = this.dumps.get(i);
			td.updateTimestamps(previousTimestamp, nextTimestamp);
			previousTimestamp = td.getTimestamp(); 
		}
	}
	
	@Override
	public void applyMonitorStickers(List<? extends MonitorRule> rules, Map<String, Sticker> stickers) {
		for (MonitorRule rule : rules){
			// Check first if the rule is disabled with a rule blocker sticker
			// Rule blocker sticker gets priority on any other sticker
			Sticker blockerCandidate = stickers.get(RuleBlockerSticker.STICKER_PREFIX + rule.getRef());
			if (blockerCandidate != null) {
				rule.disable();
				logger.info("Rule \""+ rule.getGroup() + "." + rule.getName() + "\" " + rule.getRef() + " disabled by the rule blocker sticker " + blockerCandidate.getConfigSticker().getFullName() + ".");
				continue;
			}
			
			if (rule.getStickerRefs() == null)
				continue;
			
			for (String stickerRef : rule.getStickerRefs()){
				boolean negative = stickerRef.startsWith("!");
				if (negative)
					stickerRef = stickerRef.substring(1);
				
				Sticker sticker = stickers.get(stickerRef);
				if (sticker != null){
					if (!sticker.match(this, negative)){
						rule.disable(); // not matching
						logger.info("Rule \""+ rule.getGroup() + "." + rule.getName() + "\" " + rule.getRef() + " disabled by not matching the sticker " + sticker.getConfigSticker().getGroup() + "." + sticker.getConfigSticker().getName());
					}
				}
				else {
					if (!negative){ // if negative and sticker not there, success is implicitly assumed
						rule.disable(); // sticker not found although it is expected in the ambient environment : disable it
						logger.info("Rule \"" + rule.getGroup() + "." + rule.getName() + "\" " + rule.getRef() + " disabled by not hitting the sticker " + stickerRef);
					}
				}
			}
		}
	}

	public void applyRules(List<Rule> rules){		
		logger.info("Applying rules");
		
		// Could apply multi-threading here, with care
		for (ThreadDump td : this.dumps){
			td.applyRules(rules);
		}
		
		analysisPatternStats.feed(rules);
	}
	
	@Override
	public void applyMonitorTaskRules(
			List<org.jeyzer.monitor.engine.rule.MonitorTaskRule> taskRules,
			Multimap<String, MonitorTaskEvent> events, 
			MonitorApplicativeRuleFilter appRuleFilter) throws JzrMonitorException {
		
		// Few rules may need to access the process card, as session is not accessible, use config thread local
		if (processCard != null)
			ConfigThreadLocal.put(processCard.getProperties());

		// 1 - add applicative event rules
		if (this.isJeyzerMXInfoAvailable() && appRuleFilter.rulesAllowed()) {
			List<MonitorTaskRule> rules = new ArrayList<>(this.monitorApplicativeTaskRules);
			rules.retainAll(appRuleFilter.filter(this.monitorApplicativeTaskRules));
			taskRules.addAll(rules);
		}
		
		// 2 - filter advanced monitoring rules if advanced monitoring not detected
		if (!this.isCPUInfoAvailable()){ // no CPU = no advanced monitoring
			List<org.jeyzer.monitor.engine.rule.MonitorTaskRule> rulesToRemove = new ArrayList<>();
			for (org.jeyzer.monitor.engine.rule.MonitorTaskRule rule : taskRules){
				if (rule.isAdvancedMonitoringBased())
					rulesToRemove.add(rule);
			}
			taskRules.removeAll(rulesToRemove);
		}

		// 3 - apply candidacy
		logger.info("Applying Monitoring " + taskRules.size() + " task rules");
		for (org.jeyzer.monitor.engine.rule.MonitorTaskRule rule : taskRules){
			// apply rules on actions
			for (ThreadDump dump : this.getDumps()){
				Date timestamp = dump.getTimestamp();
				Set<ThreadAction> actions = this.getActionHistory().get(timestamp);
			
				for(ThreadAction action : actions){
					rule.applyCandidacy(this, action, events);
				}
			}
		}
		
		// 4 - apply elected events
		for (org.jeyzer.monitor.engine.rule.MonitorTaskRule rule : taskRules){
			// apply rules on actions
			for (ThreadDump dump : this.getDumps()){
				Date timestamp = dump.getTimestamp();
				Set<ThreadAction> actions = this.getActionHistory().get(timestamp);
				for(ThreadAction action : actions){
					rule.applyElection(this, action, events);
				}
			}
		}
		
		if (processCard != null)
			ConfigThreadLocal.remove(processCard.getProperties());
	}	
	
	@Override
	public void applyMonitorSessionRules(
			List<org.jeyzer.monitor.engine.rule.MonitorSessionRule> sessionRules,
			Multimap<String, MonitorSessionEvent> events, 
			MonitorApplicativeRuleFilter appRuleFilter, 
			boolean includePublisherRules) throws JzrMonitorException {
		
		// Few rules may need to access the process card, as session is not accessible, use config thread local
		if (processCard != null)
			ConfigThreadLocal.put(processCard.getProperties());
		
		// 1 - add applicative and publisher event rules
		if (this.isJeyzerMXInfoAvailable()){
			if (appRuleFilter.rulesAllowed()) {
				List<MonitorSessionRule> rules = new ArrayList<>(this.monitorApplicativeSessionRules);
				rules.retainAll(appRuleFilter.filter(this.monitorApplicativeSessionRules));
				sessionRules.addAll(rules);				
			}
			if (includePublisherRules)
				sessionRules.addAll(this.monitorPublisherRules);
		}
		
		// 2 - filter advanced monitoring rules if advanced monitoring not detected
		if (!this.isCPUInfoAvailable()){ // no CPU = no advanced monitoring
			List<org.jeyzer.monitor.engine.rule.MonitorSessionRule> rulesToRemove = new ArrayList<>();
			for (org.jeyzer.monitor.engine.rule.MonitorSessionRule rule : sessionRules){
				if (rule.isAdvancedMonitoringBased())
					rulesToRemove.add(rule);
			}
			sessionRules.removeAll(rulesToRemove);
		}
		
		// 3 - create candidate events
		logger.info("Applying Monitoring " + sessionRules.size() + " session rules");
		for (org.jeyzer.monitor.engine.rule.MonitorSessionRule rule : sessionRules){
			// apply rules to ourselves
			rule.applyCandidacy(this, events);
		}
		
		// 4 - apply elected events
		for (org.jeyzer.monitor.engine.rule.MonitorSessionRule rule : sessionRules){
			// apply rules to ourselves
			rule.applyElection(this, events);
		}
		
		if (processCard != null)
			ConfigThreadLocal.remove(processCard.getProperties());
	}
	
	@Override
	public void applyMonitorSystemRules(
			List<org.jeyzer.monitor.engine.rule.MonitorSystemRule> systemRules,
			Multimap<String, MonitorSystemEvent> events,
			MonitorApplicativeRuleFilter appRuleFilter) throws JzrMonitorException {
		
		// 1 - add applicative event rules
		if (this.isJeyzerMXInfoAvailable() && appRuleFilter.rulesAllowed()) {
			List<MonitorSystemRule> rules = new ArrayList<>(this.monitorApplicativeSystemRules);
			rules.retainAll(appRuleFilter.filter(this.monitorApplicativeSystemRules));
			systemRules.addAll(rules);
		}
		
		logger.info("Applying Monitoring " + systemRules.size() + " system rules");
		// 2 - create candidate events
		for (org.jeyzer.monitor.engine.rule.MonitorSystemRule rule : systemRules){
			// apply rules to ourselves
			rule.applyCandidacy(this, events);
		}
		
		// 3 - apply elected events
		for (org.jeyzer.monitor.engine.rule.MonitorSystemRule rule : systemRules){
			// apply rules to ourselves
			rule.applyElection(this, events);
		}
	}
	
	public void updateActions(ActionIdGenerator actionIdGenerator){
		logger.info("Updating actions");

		// Set restart (in advanced mode). Required to open/close the actions
		computeRestart();
		
		computeHiatus();
		
		computeJavaModuleUsage();
		
		for (ThreadDump td : this.dumps){
			this.actionHistory.put(td.getTimestamp(), new TreeSet<ThreadAction>(new ThreadAction.ThreadActionComparator()));

			td.updateActions(this.openActions, this.actionHistory, actionIdGenerator);
			
			actionIdGenerator.analysisInitClose();
		}
		
		// close remaining actions
		Set<String> ids = this.openActions.keySet();
		for (String id : ids){
			ThreadAction ta = this.openActions.get(id);
			ta.setMaxEndDate(this.endDate);
			Set<ThreadAction> archivedActions =  this.actionHistory.get(ta.getStartDate());
			archivedActions.add(ta);
		}
		this.openActions.clear();
		
		// flag frozen stacks and compute totals
		for (ThreadDump dump : this.getDumps()){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = this.getActionHistory().get(timestamp);
			
			this.actionTotal += actions.size();
			for(ThreadAction action : actions){
				action.flagFrozenStacks();
				this.actionStackTotal += action.getStackSize();
				this.actionStackTotalNotVirtualExpanded += action.size();
			}
		}
		
		computeRunnableCPUState();
		
		// compute CPU percentage and diff
		if (parser.isCPUMeasurementUsed())
			computeCPU();
		
		// compute memory percentage and diff
		if (parser.isMemoryMeasurementUsed())
			computeMemory();
		
		// compute garbage collection percentage and diff
		if (parser.isGarbageCollectionMeasurementUsed())
			computeGarbageCollection();

		// compute open file descriptor usage percentage
		if (parser.isOpenFileDescriptorMeasurementUsed())
			computeOpenFileDescriptor();
		
		// compute virtual threads
		if (parser.hasVirtualThreadSupport())
			computeVirtualThreads();
		
		// process applicative events
		processExternalEvents();
		
		// process publisher events
		processPublisherEvents();
		
		// shift write info
		if (parser.isDiskWriteTimeMeasurementUsed())
			shiftWriteInfo();
		
		// pack useless stacks to free memory
		for (ThreadDump td : this.dumps){
			td.pack();
		}
		System.gc();
	}

	private void computeVirtualThreads() {
		// variation counters
		if (this.parser.areVirtualThreadVariationCountersUsed()) {
			ThreadDump prevTd = null;
			for (ThreadDump td : this.dumps){
				td.updateVirtualThreadVariationCounters(prevTd);
				prevTd = td;
			}
		}
		
		int cpuCores = getAvailableProcessors();
		if (this.parser.hasVirtualThreadSupport()) {
			for (ThreadDump td : this.dumps){
				td.updateVirtualThreadMountedCounters(cpuCores);
			}
		}
		
		if (this.parser.hasVirtualThreadStackSupport()) {
			for (ThreadDump td : this.dumps){
				td.updateVirtualThreadStackCounters();
			}			
		}
	}
	
	private int getAvailableProcessors() {
		if (this.processCard == null) {
			return -1;
		}

		// System CPUs (can be x2 cores)
		ProcessCardProperty property = processCard.getValue(ProcessCard.AVAILABLE_PROCESSORS);
    	if (property != null) {
        	String value = property.getValue();
    		if (value != null && !value.isEmpty()) {
    			Integer parsedValue = Ints.tryParse(value);
    			if (parsedValue != null)
    				return parsedValue;	    		
    		}
		}		
		
    	// system cores
    	property = processCard.getValue(ProcessCard.JFR_AVAILABLE_PROCESSORS);
    	if (property != null) {
        	String value = property.getValue();
    		if (value != null && !value.isEmpty()) {
    			Integer parsedValue = Ints.tryParse(value);
    			if (parsedValue != null)
    				return parsedValue;	    		
    		}
		}
		
		return -1;
	}
	

	private void processExternalEvents() {
		if (!this.isJeyzerMXInfoAvailable())
			return;
		
		List<ExternalEvent> extEvents = finalizeExternalEvents(dumps);
		
		MonitorExternalRuleBuilder extBuilder = new MonitorExternalRuleBuilder();
		extBuilder.createExternalRules(extEvents);
		this.monitorApplicativeTaskRules = extBuilder.getMonitorApplicativeTaskRules();
		this.monitorApplicativeSessionRules = extBuilder.getMonitorApplicativeSessionRules();
		this.monitorApplicativeSystemRules = extBuilder.getMonitorApplicativeSystemRules();
	}
	
	private void processPublisherEvents() {
		if (!this.isJeyzerMXInfoAvailable())
			return;
		
		List<JzrPublisherEvent> pubEvents = new ArrayList<>();
		for (ThreadDump dump : dumps) {
			pubEvents.addAll(dump.getPublisherEvents());
			dump.clearPublisherEvents();
		}
		
		MonitorPublisherRuleBuilder pubBuilder = new MonitorPublisherRuleBuilder();
		this.monitorPublisherRules = pubBuilder.createPublisherRules(pubEvents);
	}

	private List<ExternalEvent> finalizeExternalEvents(List<ThreadDump> dumps) {
		Map<String, ExternalEvent> extEvents = new HashMap<>();
		List<ExternalEvent> finalExtEvents = new ArrayList<>();
		
		for (ThreadDump dump : dumps) {
			for (ExternalEvent candidate : dump.getExternalEvents()) {
				
				if (candidate.isOneshot()) {
					finalExtEvents.add(candidate); //immediately add to final list
					continue;
				}
				
				ExternalEvent event = extEvents.get(candidate.getId());
				if (event == null) {
					// add candidate
					extEvents.put(candidate.getId(), candidate);
				}
				else {
					if (event.getEnd()!= null)
						logger.warn("External event finalization : event closed more than once by the application. Event is : " + event.getId());
					event.updateEnd(candidate.getEnd(), dump.getTimestamp());
				}
			}
			
			dump.clearExternalEvents();
		}
		
		finalExtEvents.addAll(extEvents.values());
		
		return finalExtEvents;
	}

	private void shiftWriteInfo() {
		ThreadDump prev = null;
		
		// shift back the values
		for (ThreadDump dump : this.dumps){
			if (prev != null){
				prev.setWriteTime(dump.getWriteTime());
				prev.setWriteSize(dump.getWriteSize());
			}
			prev = dump;
		}
		
		// reset last one
		prev.setWriteTime(-1);
		prev.setWriteSize(-1);
	}

	private void computeRunnableCPUState() {
		for (ThreadDump dump : this.getDumps()){
			dump.updateCPURunnable(this.setupMgr.getCPURunnableContentionTypesManager());
		}
	}

	private void computeRestart() {
		ThreadDump prev = null;
		for (ThreadDump dump : this.dumps){
			if (prev != null){
				boolean restart = dump.getProcessUpTime() != -1 && dump.getProcessUpTime() < prev.getProcessUpTime();
				dump.setRestart(restart);
			}
			prev = dump;
		}
	}
	
	private void computeHiatus() {
		ThreadDump prev = null;
		for (ThreadDump dump : this.dumps){
			if (prev != null && !dump.isRestart()){
				// exclude the capture time if any
				long captureTime = 0;
				if (prev.getCaptureTime() != -1)
					captureTime = prev.getCaptureTime() / 2;
				if (dump.getCaptureTime() != -1)
					captureTime += dump.getCaptureTime() / 2;
				
				long diff = dump.getTimestamp().getTime() - prev.getTimestamp().getTime() - captureTime;
				boolean hiatus = diff > this.getThreadDumpPeriodJumpLimit();
				dump.setHiatus(hiatus);
			}
			prev = dump;
		}
	}
	
	private void computeJavaModuleUsage() {
		ThreadDump dump = this.getDumps().get(0);
		if (dump != null) {
			int i = 0;
			while(i < dump.getThreads().size()) {
				ThreadStackHandler handler = dump.getThreads().get(i).getStackHandler();
				if (!handler.getCodeLines().isEmpty()) {
					String line = handler.getCodeLines().get(0);
					if (line != null) {
						this.moduleSupport = line.contains("@");
						break;
					}
				}
				i++;
			}
		}
	}
	
	private void computeOpenFileDescriptor() {
		if (processCard == null)
			return;
		
    	ProcessCardProperty property = processCard.getValue(ProcessCard.FILE_DESCRIPTOR_MAX);
    	if (property == null)
    		return;
    	
    	String value = property.getValue();
		if (value == null || value.isEmpty())
			return;
		
		Long fdMax = Longs.tryParse(value);
		if (fdMax == null || fdMax == -1 || fdMax == 0)
			return;
		
		for (ThreadDump dump : this.getDumps()){
			long fdCount = dump.getProcessOpenFileDescriptorCount();
			if (fdCount == -1)
				continue;
		
	    	int fdUsagePercent = FormulaHelper.percentRound(fdCount, fdMax);
			dump.setProcessOpenFileDescriptorUsage(fdUsagePercent);
		}
	}

	private void computeGarbageCollection() {
		ThreadDump previous = null;
		double gcTimePercentPeak = 0;
		String peakDumpFileName = "";

		// update the diff and get the max gc time percent
		for (ThreadDump dump : this.getDumps()){
			dump.updateGarbageCollectionData(previous);
			if (dump.getGarbageCollection().getGcTimePercent()> gcTimePercentPeak){
				gcTimePercentPeak = dump.getGarbageCollection().getGcTimePercent();
				peakDumpFileName = dump.getFileName();
			}
			previous = dump; 
		}
		
		logger.info("Peak garbage collection dump file : {}", peakDumpFileName);
		logger.info("Peak garbage collection           : {} ms", gcTimePercentPeak);
	}

	private void computeMemory() {
		ThreadDump previous = null;
		long memoryPeak = 0;
		long timeSlicePeak = 0;
		String peakDumpFileName = "";
		
		for (ThreadDump dump : this.getDumps()){
			// calculate diff between 2 stacks using their id
			// set it on 2nd stack
			// Collect memory diff total time and set it on each TD
			dump.updateMemoryData(previous);
			
			if (dump.getTotalComputedMemory()> memoryPeak){
				memoryPeak = dump.getTotalComputedMemory();
				peakDumpFileName = dump.getFileName();
				timeSlicePeak = dump.getTimeSlice();
			}
			previous = dump; 
		}

		for (ThreadDump dump : this.getDumps()){
			dump.updateApplicativeMemoryActivityUsage(memoryPeak, timeSlicePeak);
		}
		
		logger.info("Peak memory dump file : {}", peakDumpFileName);
		logger.info("Peak memory           : {} mb", convertToMb(memoryPeak));
		
		// set memory figures on actions
		int pos = 0;
		for (ThreadDump dump : this.getDumps()){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = this.getActionHistory().get(timestamp);
		
			for(ThreadAction action : actions){
				// we must get the memory remainder on the stack following the end of the action to be accurate 
				ThreadStack postActionStack = getPostActionStack(action, pos);
				action.updateMemoryFigures(postActionStack);
			}
			pos++;
		}
	}

	private void computeCPU() {
		ThreadDump previous = null;
		double cpuUsagePeak = 0;
		long cpuTimePeak = 0;
		long timeSlicePeak = 0;
		String peakDumpFileName = "";
		
		for (ThreadDump dump : this.getDumps()){
			// calculate diff between 2 stacks using their id
			// set it on 2nd stack
			// Collect CPU diff total time and set it on each dump
			dump.updateCPUData(previous);
			
			if (dump.getProcessComputedCPUUsage()> cpuUsagePeak){
				cpuUsagePeak = dump.getProcessComputedCPUUsage();
				cpuTimePeak = dump.getTotalCPUTime();
				peakDumpFileName = dump.getFileName();
				timeSlicePeak = dump.getTimeSlice();
			}
			previous = dump; 
		}

		for (ThreadDump dump : this.getDumps()){
			dump.updateApplicativeActivityUsage(cpuTimePeak, timeSlicePeak);
		}
		
		logger.info("Peak CPU dump file : {}", peakDumpFileName);
		logger.info("Peak CPU usage     : {} %", cpuUsagePeak);
		logger.info("Peak CPU time      : {} ms", cpuTimePeak / 1000000L);
		
		// set cpu figures on actions 
		int pos = 0;
		Date prevTimeStamp = null;
		for (ThreadDump dump : this.getDumps()){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = this.getActionHistory().get(timestamp);
		
			for(ThreadAction action : actions){
				// we must get the CPU remainder on the stack following the end of the action to be accurate 
				ThreadStack postActionStack = getPostActionStack(action, pos);
				action.updateCPUFigures(postActionStack, prevTimeStamp);
			}
			prevTimeStamp = timestamp; 
			pos++;
		}
	}
	
	private ThreadStack getPostActionStack(ThreadAction action, int pos){
		ThreadStack postStack;
		ThreadDump postDump;

		// access the thread dump which follows the action end
		int index = pos + action.size();
		if (index>=this.dumps.size()) // action is finishing at the end of the session
			return null;
		
		postDump = this.dumps.get(index);
		
		// access the related stack using thread id
		postStack = postDump.getStack(action.getThreadId());
		
		// thread may have died
		if (postStack == null)
			return null;
		
		// if missing period, ignore it as post action time range may be not CPU representative anymore
		if (postDump.hasHiatusBefore()){
			return null;
		}
		
		return postStack;
	}
	
	@SuppressWarnings("unused")
	private ThreadStack getPreviousActionStack(ThreadAction action, int pos){
		ThreadStack firstActionStack, prevStack;
		ThreadDump prevDump, actionFirstDump;

		// access the thread dump which precedes the action start
		int index = pos - 1;
		if (index < 0) // action is starting at the beginning of the session
			return null;
		
		prevDump = dumps.get(index);
		actionFirstDump = dumps.get(pos); 
		
		// access the related stack using thread id
		prevStack = prevDump.getStack(action.getThreadId());
		
		// thread may have not existed
		if (prevStack == null)
			return null;
		
		// if missing period, ignore it as post action time range may be not CPU representative anymore
		if (actionFirstDump.hasHiatusBefore()){
			return null;
		}
		
		return prevStack;
	}
	
	public void setStackMinimumSize(int stackMinimumSize) {
		this.stackMinimumSize = stackMinimumSize;
	}
	
	public int getStackMinimumSize() {
		return this.stackMinimumSize;
	}

	public void updateLocks(){
		logger.info("Updating locks");
		
		for (ThreadDump td : this.dumps){
			for (ThreadStack ts : td.getWorkingThreads()){
				if (ts.isBlocked() && !ts.getLockName().isEmpty()){
					ThreadStack lt = resolveLockingThread(td, ts);
					if (lt != null){
						ts.setLockingThread(lt);
						lt.addLockedThread(ts);
					}
					else{
						logger.warn("Lock owner thread not found for lock : " + ts.getLockName() + " in file " + td.getFileName());
					}
				}
			}
		}

		for (ThreadDump td : this.dumps){
			updateDeadLocks(td);
		}
	}

	private void updateDeadLocks(ThreadDump td) {
		List<ThreadStack> dlCandidates = getDeadlockCandidates(td);
		
		// IMPORTANT : works only with jdk6+ as we manipulate here lock ids
		//   We assume that getDeadlockCandidates has filtered out the jdk5 case
		if (dlCandidates.isEmpty())
			return;
		
		// each deadlock cycle is a set of stacks
		List<List<ThreadStack>> dlCycles = detectDeadLocks(dlCandidates);
		
		// no deadlock
		if (dlCycles.isEmpty())
			return;

		// process deadlock cycles : store it
		
		// process deadlock cycles : build deadlock string
		if (td.getDeadLockTexts().isEmpty()){
			for (List<ThreadStack> dlCycle : dlCycles){
				td.addDeadLock(buildDeadLockText(dlCycle));
			}
		}else{
			// check that what we found is ok
			if (td.getDeadLockTexts().size() != dlCycles.size())
				logger.warn("Deadlock detection cycle fails : " + dlCycles.size() + " deadlock cycles were identified although it's not the case in the recording snapshot ("+ td.getDeadLockTexts().size() + " deadlock cycles) . Please review the results and inputs." );
		}
		
		// process deadlock cycles : update the stacks
		if (!this.parser.isLockCycleDetectionUsed()){
			setDeadLocks(dlCycles);
		}else{
			// check that what we found is ok
			checkDeadLockDetectionCorrectness(dlCycles);
		}
	}

	private void setDeadLocks(List<List<ThreadStack>> dlCycles) {
		for (List<ThreadStack> dlCycle : dlCycles){
			for (ThreadStack dlStack : dlCycle){
				dlStack.setInDeadlock(true);
			}
		}
	}

	private void checkDeadLockDetectionCorrectness(List<List<ThreadStack>> dlCycles) {
		for (List<ThreadStack> dlCycle : dlCycles){
			for (ThreadStack dlStack : dlCycle){
				if (!dlStack.isInDeadlock())
					logger.warn("Deadlock detection fails : " + dlStack.getName() + " has been identified as participating to a deadlock although it's not the case in the recording snapshot. Please review the results and inputs." );
			}
		}
	}

	private List<List<ThreadStack>> detectDeadLocks(List<ThreadStack> dlCandidates) {
		List<List<ThreadStack>> dlCycles = new ArrayList<>();
		
		for (ThreadStack stack : dlCandidates){
			if (alreadyProcessedLock(dlCycles, stack))
				continue;
			
			List<ThreadStack> dlCycleCandidate = new ArrayList<>();
			
			// last visited stack will have to be blocked by one of the lock of current stack.getOwnedLocks()
			if (detectCycle(stack, dlCandidates, dlCycleCandidate, stack.getLockName(), stack.getOwnedLocks())){
				dlCycleCandidate.add(stack);
				dlCycles.add(dlCycleCandidate);
			}
			
		}

		return dlCycles;
	}

	private String buildDeadLockText(List<ThreadStack> dlCycle) {
		StringBuilder dlText = new StringBuilder();
		
		dlText.append("Detected one Java-level deadlock:\n");
		dlText.append("=============================\n");
		for (ThreadStack dlStack : dlCycle){
			dlText.append("Thread \"");
			dlText.append(dlStack.getName());
			dlText.append(" -- ");
			dlText.append(dlStack.getID());
			dlText.append("\"\n");
			dlText.append("  waiting to lock : ");
			dlText.append(dlStack.getLockName()!=null?dlStack.getLockName():"not found");
			dlText.append("\n  which is held by : \"");
			dlText.append(dlStack.getLockingThread()!=null? dlStack.getLockingThread().getName() +" -- "+ dlStack.getLockingThread().getID() +"\"":"not found");
			dlText.append("\n");
		}

		return dlText.toString();
	}

	private boolean detectCycle(
			ThreadStack stack,
			List<ThreadStack> dlCandidates, 
			List<ThreadStack> dlCycleCandidate,
			String lockName, 
			final List<String> originalOwnedLocks) {

		for (ThreadStack candidate : dlCandidates){
			if (candidate == stack)
				continue; // don't process ourselves
			
			if (candidate.getOwnedLocks().contains(lockName)){
				if (originalOwnedLocks.contains(candidate.getLockName())){
					// dead lock found !!
					dlCycleCandidate.add(candidate);
					return true;
				}
				else if (detectCycle(candidate, dlCandidates, dlCycleCandidate, candidate.getLockName(), originalOwnedLocks)){
					dlCycleCandidate.add(candidate);
					return true;
				}
			}
		}
		
		return false;
	}

	private boolean alreadyProcessedLock(List<List<ThreadStack>> dlCycles, ThreadStack stack) {

		if (dlCycles.isEmpty())
			return false;
		
		for (List<ThreadStack> stacks : dlCycles){
			for (ThreadStack st : stacks){
				if (stack.getID().equals(st.getID()))
					return true;
			}
		}
		
		return false;
	}

	private List<ThreadStack> getDeadlockCandidates(ThreadDump td) {
		List<ThreadStack> dlCandidates = new ArrayList<>(); 
		
		if (this.parser.isDeadlockUsed()){
			// deadlock info is available on stack (in deadlock participant attribute) and possibly on dump (e.g. deadlock text for jstack)
			for (ThreadStack ts : td.getWorkingThreads()){
				if (ts.isInDeadlock())
					dlCandidates.add(ts);
			}
			
			if (!JMXStackParser.FORMAT_SHORT_NAME.equals(this.parser.getFormatShortName()))
				return dlCandidates;

			// JMX case exception : deadlock may not be always printed
			if (!dlCandidates.isEmpty())
				return dlCandidates;
		}
		
		// case deadlock info not present (or JMX case exception) 
		//  we have lock info but no clue about deadlocks
		for (ThreadStack ts : td.getWorkingThreads()){
			// locked and owns locks = deadlock candidate
			// ts.hasOwnedLocks() will be always false for Jstack15Parser
			if (ts.getLockingThread() != null && ts.hasOwnedLocks())
				dlCandidates.add(ts);
		}
		
		return dlCandidates;
	}

	private ThreadStack resolveLockingThread(ThreadDump td, ThreadStack lockedT){
		
		for (ThreadStack ts : td.getWorkingThreads()){
			if (ts == lockedT)
				continue;
			if (parser.isLockIdUsed()){
				// jdk 1.6+ case : lock id (jstack) or lock name (jmx) is used
				for (String lock : ts.getOwnedLocks()){
					if (lock.equals(lockedT.getLockName())){
						return ts;
					}
				}
			}
			else 
			{
				// jdk 1.5 case	: thread name is used
				if (lockedT.getLockName().contains(ts.getName())){
					return ts;
				}
			}
		}
		return null;
	}
	
	public void dumpWorkingThreads(){
		
		logger.debug("----------------");
		logger.debug("Working threads :");
		logger.debug("----------------");
		
		for (ThreadDump td : this.dumps){
			List<ThreadStack> stacks = td.getWorkingThreads();
			logger.debug(td.toString());
			for (ThreadStack ts : stacks)
				logger.debug(ts.toString());
		}
	}	
	
	public void dumpUFOThreads(ReportDescriptor desc){
		if (!this.ufoStacksFileGenerationEnabled)
			return;
		
		if (UFO_ORDER_BY_FILE.equals(this.ufoOrderBy))
			dumpUFOThreadsOrderedPerFile(desc);
		else if (UFO_ORDER_BY_FREQUENCY.equals(this.ufoOrderBy))
			dumpUFOThreadsOrderedPerFrequency(desc);
		else
			logger.warn("Unrecognized UFO order by method : " 
					+ ((ufoOrderBy!=null)? ufoOrderBy :"Not set"));
	}	
	
	private void dumpUFOThreadsOrderedPerFrequency(ReportDescriptor desc) {
		String zipPath= this.ufoZipFile;

		Multiset<ThreadStackHandler> stacks = this.getUFOStackSet();
		if (stacks.isEmpty()){
			logger.info("UFO stack file generation skipped : no UFO stacks found.");
			return;
		}
		
		try {
			SystemHelper.createDirectory(this.ufoOutputDirPath);
		} catch (JzrException ex) {
			logger.error("Failed to create the directory " + SystemHelper.sanitizePathSeparators(this.ufoOutputDirPath), ex);
			return;
		}
		
		try (
				FileOutputStream fos = new FileOutputStream(zipPath);
				ZipOutputStream out = new ZipOutputStream(fos);
			)
		{
			ZipEntry entry = new ZipEntry(UFO_FILE);
			out.putNextEntry(entry);
			
			out.write(("UFO stacks : " + CR + CR).getBytes());
			for (ThreadStackHandler stack : Multisets.copyHighestCountFirst(stacks).elementSet()) {
				out.write(("Stack count : " + stacks.count(stack)+ CR).getBytes());
				out.write(("Stack sample : " + CR).getBytes());
				out.write(stack.getThreadStack().toString().getBytes());
				out.write(CR.getBytes());
			}
			
			desc.setUfoStackFilePath(zipPath);
			desc.setUfoStackFileName(UFO_ZIP_FILE);
		}catch (Exception e){
			logger.error("Failed to write UFO stacks into file " + SystemHelper.sanitizePathSeparators(zipPath), e);
			return;
		}
		
		logger.info("UFO file created : " + SystemHelper.sanitizePathSeparators(this.ufoZipFile));
	}

	private void dumpUFOThreadsOrderedPerFile(ReportDescriptor desc) {
		String zipPath= this.ufoZipFile;

		if (this.getUFOStackList().isEmpty()){
			logger.info("UFO stack file generation skipped : no UFO stacks found.");
			return;
		}
		
		try {
			SystemHelper.createDirectory(this.ufoOutputDirPath);
		} catch (JzrException ex) {
			logger.error("Failed to create the directory " + SystemHelper.sanitizePathSeparators(this.ufoOutputDirPath), ex);
			return;
		}
		
		try (
				FileOutputStream fos = new FileOutputStream(zipPath);
				ZipOutputStream out = new ZipOutputStream(fos);
			)
		{
			ZipEntry entry = new ZipEntry(UFO_FILE);
			out.putNextEntry(entry);
			
			out.write(("UFO stacks : " + CR).getBytes());
			for (ThreadDump dump : this.dumps){
				List<ThreadStack> stacks = dump.getUFOThreads();
				for (ThreadStack stack : stacks){
					out.write(dump.toString().getBytes());
					out.write(stack.toString().getBytes());
				}
			}
			
			desc.setUfoStackFilePath(zipPath);
			desc.setUfoStackFileName(UFO_ZIP_FILE);
		}catch (Exception e){
			logger.error("Failed to write UFO stacks into file " + SystemHelper.sanitizePathSeparators(zipPath), e);
			return;
		}		
		
		logger.info("UFO file created : " + SystemHelper.sanitizePathSeparators(this.ufoZipFile));
	}

	private void detectThreadDumpPeriod(File[] files, SnapshotFileNameFilter filter){

		if (!(files.length > 1)){
			logger.warn("Unable to detect the recording snapshot period : only one recording snapshot file was found.");
			this.tdDetectedPeriod = -1;
			return;
		}

		List<Integer> intervals = getThreadDumpIntervals(files, filter);
		
		// get mode(s)
		List<Integer> candidates = calculateMode(intervals);
		
		// select middle one
		int size = candidates.size();
		int modeIdx =  ((size % 2) == 0) ? size / 2 : Math.round(size / 2);

		int mode = candidates.get(modeIdx);

		logger.info("Recording snapshot period detected   : {} sec", mode);
		this.tdDetectedPeriod = mode;
		
		validateDetectedThreadDumpPeriod(intervals, mode);
		validateConfiguredThreadDumpPeriod();
	}
	
	private List<Integer> getThreadDumpIntervals(File[] files, SnapshotFileNameFilter filter){
		List<Integer> intervals = new ArrayList<>(files.length-1);
		Date prev = null;
		Date next = null;

		prev = ThreadDumpFileDateHelper.getFileDate(filter, files[0]);
		for (int i=1; i<files.length; i++){
			next = ThreadDumpFileDateHelper.getFileDate(filter, files[i]);
			
			if (!prev.equals(next)){
				float value = (next.getTime() - prev.getTime())/1000f;
				int interval = Math.round(value);
				intervals.add(Integer.valueOf(interval));
			}
			else {
				logger.warn("Recording snapshot period detection : found 2 files with same time stamp : {} and {}", files[i], files[i-1]);
				intervals.add(Integer.valueOf(0));
			}

			prev = next;
		}
		
		return intervals;
	} 
	
	private void validateDetectedThreadDumpPeriod(List<Integer> intervals, final int mode){
		int closedTomodeCount = 0;
		Map<Integer, Integer> distribution = getDistribution(intervals);

		for (int i=mode-5; i<mode+5 ; i++){
			Integer count = distribution.get(i);
			if (count != null)
				closedTomodeCount = closedTomodeCount + count;
		}
		
		// accept if distribution of intervals around mode is larger than 60%
		this.validTDDetectedPeriod = (float)closedTomodeCount / (float)intervals.size() > 0.6;
		
		if (!this.validTDDetectedPeriod)
			logger.warn("Recording snapshot period detection : calculated period not accurate as time intervals between 2 recording snapshots is not consistent.");
	}
	
	private void validateConfiguredThreadDumpPeriod(){

		logger.info("Recording snapshot period configured : {}", this.tdPeriod != -1?  this.tdPeriod + " sec" : "not set");
		
		// valid if configured and detected values differ no more than 5 sec
		this.validTDConfiguredPeriod = 
				(this.tdPeriod != -1 || this.tdDetectedPeriod != -1) 
				&& (Math.abs(this.tdPeriod - this.tdDetectedPeriod) < 5);
		
		if (this.tdPeriod != -1 && !this.validTDConfiguredPeriod)
			logger.warn("Recording snapshot period configured ({} sec) differs from detected one ({} sec).", 
					this.tdPeriod, this.tdDetectedPeriod);
	}
	
	public void dumpIgnoredStacks(){
		if (!this.isIgnoredStacksFileGenerationEnabled())
			return;
		
		String zipPath= getIgnoredStacksFilePath();
		ZipOutputStream out = null;
		
		try{
			out = new ZipOutputStream(new FileOutputStream(zipPath));
			ZipEntry entry = new ZipEntry(IGNORED_STACKS_FILE);
			out.putNextEntry(entry);
			
			for (ThreadDump dump : this.dumps){
				
				out.write("------------------------------------------------------------------------------------\n".getBytes());
				out.write(dump.getFileName().getBytes());
				out.write("\n".getBytes());
				out.write("------------------------------------------------------------------------------------\n\n".getBytes());
				
				List<ThreadStack> stacks = dump.getDiscardedThreads();
				for (ThreadStack stack : stacks){
					out.write(stack.getStackHandler().getText().getText().getBytes());
					out.write("\n".getBytes());
				}
			}
		}catch (Exception e){
			logger.error("Failed to write ignored stacks into file " + SystemHelper.sanitizePathSeparators(zipPath), e);
		}finally{
			if (out!=null)
				try {
					out.close();
				} catch (IOException e) {
					logger.error("Failed to close ignored stacks into file " + SystemHelper.sanitizePathSeparators(zipPath), e);
				}
		}
		
		logger.info("Ignored stacks file created : " + SystemHelper.sanitizePathSeparators(zipPath));
	}
	
	public Multiset<ThreadStackHandler> getStackSet(){
		Multiset<ThreadStackHandler> stacks = HashMultiset.create();
		
		for (ThreadDump dump : this.dumps){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = this.actionHistory.get(timestamp);
			
			for(ThreadAction action : actions){
				for (int i=0; i<action.size(); i++) {
					ThreadStack stack = action.getThreadStack(i);
					for (int j=0; j<stack.getInstanceCount(); j++)
						stacks.add(stack.getStackHandler()); // yes we inject as much as needed for virtual threads (otherwise 1 for standard)
				}
			}
		}
		
		return stacks;
	}
	
	private Multiset<ThreadStackHandler> getUFOStackSet(){
		Multiset<ThreadStackHandler> stacks = HashMultiset.create();
		
		for (ThreadDump dump : this.dumps){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = this.actionHistory.get(timestamp);
			
			for(ThreadAction action : actions){
				for (int i=0; i<action.size(); i++){
					ThreadStack stack = action.getThreadStack(i);
					if (stack.isUFO()) {
						for (int j=0; j<action.getThreadStack(i).getInstanceCount(); j++)
							stacks.add(action.getThreadStack(i).getStackHandler()); // yes we inject as much as needed for virtual threads (otherwise 1 for standard)						
					}
				}
			}
		}
		
		return stacks;
	}
	
	private List<ThreadStack> getUFOStackList(){
		List<ThreadStack> stacks = new ArrayList<>();
		
		for (ThreadDump dump : this.dumps){
			stacks.addAll(dump.getUFOThreads());
		}
		
		return stacks;
	}	

	public Multiset<Tag> getFunctionSet(){
		if (this.functionSets != null)
			return functionSets;
		
		this.functionSets = HashMultiset.create();
		
		for (ThreadDump dump : this.dumps){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = this.actionHistory.get(timestamp);
			
			for(ThreadAction action : actions){
				fillFunctionSet(action);
			}
		}
		
		return this.functionSets;
	}
	
	private void fillFunctionSet(ThreadAction action) {
		for (int i=0; i<action.size(); i++){
			ThreadStack stack = action.getThreadStack(i);
			for (int j=0; j<stack.getInstanceCount(); j++) { // do it as much as needed for virtual threads
				if (stack.isATBI()){
					// include ATBI
					this.functionSets.add(FunctionTag.ATBI_TAG);
				}

				else{
					for(String functionTag : stack.getFunctionTags()){
						this.functionSets.add(new FunctionTag(functionTag));
					}
				}				
			}
		}
	}
	
	public Multiset<Tag> getPrincipalFunctionSet(){
		if (this.principalFunctionSets != null)
			return principalFunctionSets;
		
		this.principalFunctionSets = HashMultiset.create();
		
		for (ThreadDump dump : this.dumps){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = this.actionHistory.get(timestamp);
			
			for(ThreadAction action : actions){
				fillPrincipalFunctionSet(action);
			}
		}
		
		return this.principalFunctionSets;
	}

	private void fillPrincipalFunctionSet(ThreadAction action) {
		for (int i=0; i<action.size(); i++){
			FunctionTag principalTag = new FunctionTag(action.getThreadStack(i).getPrincipalTag());
			this.principalFunctionSets.add(principalTag);
		}
	}

	public Multiset<Tag> getOperationSet(){
		if (this.operationSets != null)
			return operationSets;
		
		this.operationSets = HashMultiset.create();
		
		for (ThreadDump dump : this.dumps){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = this.actionHistory.get(timestamp);
			
			for(ThreadAction action : actions){
				fillOperationSet(action);
			}
		}
		
		return this.operationSets;
	}
	
	private void fillOperationSet(ThreadAction action) {
		for (int i=0; i<action.size(); i++){
			ThreadStack stack = action.getThreadStack(i);
			for (int j=0; j<stack.getInstanceCount(); j++) { // do it as much as needed for virtual threads
				if (stack.isOTBI()){
					this.operationSets.add(OperationTag.OTBI_TAG);
				}
				else{
					for(String operationTag : stack.getOperationTags()){
						this.operationSets.add(new OperationTag(operationTag));
					}
				}
			}
		}
	}
	
	public Multiset<Tag> getContentionTypeSet(){
		if (this.contentionTypeSets != null)
			return contentionTypeSets;
		
		this.contentionTypeSets = HashMultiset.create();
		
		for (ThreadDump dump : this.dumps){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = this.actionHistory.get(timestamp);
			
			for(ThreadAction action : actions){
				fillContentionTypeSet(action);
			}
		}
		
		return this.contentionTypeSets;
	}
	
	private void fillContentionTypeSet(ThreadAction action) {
		for (int i=0; i<action.size(); i++){
			ThreadStack stack = action.getThreadStack(i);
			for (int j=0; j<stack.getInstanceCount(); j++) { // do it as much as needed for virtual threads
				for(String contentionTypeTag : stack.getContentionTypeTags()){
					this.contentionTypeSets.add(new ContentionTypeTag(contentionTypeTag));
				}
			}
		}
	}
	
	public Multiset<Tag> getPrincipalContentionTypeSet(){
		Multiset<Tag> principalContentionTypeSets = HashMultiset.create();
		
		for (ThreadDump dump : this.dumps){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = this.actionHistory.get(timestamp);
			
			for(ThreadAction action : actions){
				for (int i=0; i<action.size(); i++){
					principalContentionTypeSets.add(
							new ContentionTypeTag(
									action.getThreadStack(i).getPrincipalContentionType())
							);
				}
			}
		}
		
		return principalContentionTypeSets;
	}
	
	public AnalysisPatternsStats getAnalysisPatternsStats() {
		return analysisPatternStats;
	}

	public Multimap<String, Tag> getContentionTypeSetPerFunctionPrincipal() {
		if (this.contentionTypeSetPerFunctionPrincipal != null)
			return this.contentionTypeSetPerFunctionPrincipal;
		
		this.contentionTypeSetPerFunctionPrincipal = ArrayListMultimap.create();
		
		for (ThreadDump dump : this.dumps){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = this.actionHistory.get(timestamp);
			
			for(ThreadAction action : actions){
				fillContentionTypeSetPerFunctionPrincipal(this.contentionTypeSetPerFunctionPrincipal, action);
			}
		}
		
		return this.contentionTypeSetPerFunctionPrincipal;
	}
	
	private void fillContentionTypeSetPerFunctionPrincipal(Multimap<String, Tag> contentionTypes, ThreadAction action) {
		String principal = action.getPrincipalCompositeFunction();
		for (int i=0; i<action.size(); i++){
			ThreadStack stack = action.getThreadStack(i);
			for(String contentionTypeTag : stack.getContentionTypeTags()){
				for (int j=0; j<stack.getInstanceCount(); j++) // include virtual threads
					contentionTypes.put(
							principal, 
							new ContentionTypeTag(contentionTypeTag)
							);
			}
		}
	}
	
	public Multimap<String, Tag> getOperationSetPerFunctionPrincipal() {
		if (this.operationSetPerFunctionPrincipal != null)
			return this.operationSetPerFunctionPrincipal;
		
		this.operationSetPerFunctionPrincipal = ArrayListMultimap.create();
		
		for (ThreadDump dump : this.dumps){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = this.actionHistory.get(timestamp);
			
			for(ThreadAction action : actions){
				fillOperationSetPerFunctionPrincipal(this.operationSetPerFunctionPrincipal, action);
			}
		}
		
		return this.operationSetPerFunctionPrincipal;
	}
	
	private void fillOperationSetPerFunctionPrincipal(Multimap<String, Tag> operations, ThreadAction action) {
		String principal = action.getPrincipalCompositeFunction();
		for (int i=0; i<action.size(); i++){
			ThreadStack stack = action.getThreadStack(i);
			for(String operationTag : stack.getOperationTags()){
				for (int j=0; j<stack.getInstanceCount(); j++)
					operations.put(
							principal, 
							new OperationTag(operationTag)
							);
			}
		}
	}

	public Multimap<String, Tag> getFunctionSetPerFunctionPrincipal() {
		if (this.functionSetPerFunctionPrincipal != null)
			return this.functionSetPerFunctionPrincipal;
		
		this.functionSetPerFunctionPrincipal = ArrayListMultimap.create();
		
		for (ThreadDump dump : this.dumps){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = this.actionHistory.get(timestamp);
			
			for(ThreadAction action : actions){
				fillFunctionSetPerFunctionPrincipal(this.functionSetPerFunctionPrincipal, action);
			}
		}
		
		return this.functionSetPerFunctionPrincipal;
	}
	
	private void fillFunctionSetPerFunctionPrincipal(Multimap<String, Tag> functions, ThreadAction action) {
		String principal = action.getPrincipalCompositeFunction();
		for (int i=0; i<action.size(); i++){
			ThreadStack stack = action.getThreadStack(i);
			for(String functionTag : stack.getFunctionTags()){
				for (int j=0; j<stack.getInstanceCount(); j++) // include virtual threads
					functions.put(
							principal, 
							new FunctionTag(functionTag)
							);
			}
		}
	}
	
	public Multimap<String, Tag> getContentionTypeSetPerExecutor() {
		if (this.contentionTypeSetPerExecutor != null)
			return this.contentionTypeSetPerExecutor;
		
		this.contentionTypeSetPerExecutor = ArrayListMultimap.create();
		
		for (ThreadDump dump : this.dumps){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = this.actionHistory.get(timestamp);
			
			for(ThreadAction action : actions){
				fillContentionTypeSetPerExecutor(this.contentionTypeSetPerExecutor, action);
			}
		}
		
		return this.contentionTypeSetPerExecutor;
	}

	private void fillContentionTypeSetPerExecutor(Multimap<String, Tag> contentionTypes, ThreadAction action) {
		String executor = action.getExecutor();
		for (int i=0; i<action.size(); i++){
			ThreadStack stack = action.getThreadStack(i);
			for(String contentionTypeTag : stack.getContentionTypeTags()){
				for (int j=0; j<stack.getInstanceCount(); j++)
					contentionTypes.put(
							executor, 
							new ContentionTypeTag(contentionTypeTag)
							); // add as much as needed for virtual threads for virtual threads
			}
		}
	}
	
	public Multimap<String, Tag> getOperationSetPerExecutor() {
		if (this.operationSetPerExecutor != null)
			return this.operationSetPerExecutor;
		
		this.operationSetPerExecutor = ArrayListMultimap.create();
		
		for (ThreadDump dump : this.dumps){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = this.actionHistory.get(timestamp);
			
			for(ThreadAction action : actions){
				fillOperationSetPerExecutor(this.operationSetPerExecutor, action);
			}
		}
		
		return this.operationSetPerExecutor;
	}

	
	private void fillOperationSetPerExecutor(Multimap<String, Tag> operations, ThreadAction action) {
		String executor = action.getExecutor();
		for (int i=0; i<action.size(); i++){
			ThreadStack stack = action.getThreadStack(i);
			for(String operationTag : stack.getOperationTags()){
				for (int j=0; j<stack.getInstanceCount(); j++)
					operations.put(
							executor, 
							new OperationTag(operationTag)
							); // add as much as needed for virtual threads
			}
		}
	}
	
	public Multimap<String, Tag> getFunctionSetPerExecutor() {
		if (this.functionSetPerExecutor != null)
			return this.functionSetPerExecutor;
		
		this.functionSetPerExecutor = ArrayListMultimap.create();
		
		for (ThreadDump dump : this.dumps){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = this.actionHistory.get(timestamp);
			
			for(ThreadAction action : actions){
				fillFunctionSetPerExecutor(this.functionSetPerExecutor, action);
			}
		}
		
		return this.functionSetPerExecutor;
	}
	
	private void fillFunctionSetPerExecutor(Multimap<String, Tag> functions, ThreadAction action) {
		String executor = action.getExecutor();
		for (int i=0; i<action.size(); i++){
			ThreadStack stack = action.getThreadStack(i);
			for(String functionTag : stack.getFunctionTags()){
				for (int j=0; j<stack.getInstanceCount(); j++)
					functions.put(
							executor, 
							new FunctionTag(functionTag)
							); // add as much as needed for virtual threads
			}
		}
	}

	public Multimap<String, ThreadAction> getActionSetPerFunctionPrincipal() {
		if (this.actionsPerFunctionPrincipal != null)
			return this.actionsPerFunctionPrincipal;
		
		this.actionsPerFunctionPrincipal = ArrayListMultimap.create();
		
		for (ThreadDump dump : this.dumps){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = this.actionHistory.get(timestamp);
			
			for(ThreadAction action : actions){
				String principal = action.getPrincipalCompositeFunction();
				this.actionsPerFunctionPrincipal.put(
								principal, 
								action
								);
			}
		}
		
		return this.actionsPerFunctionPrincipal;
	}

	public Multimap<String, ThreadAction> getActionSetPerExecutor() {
		if (this.actionsPerExecutor != null)
			return this.actionsPerExecutor;
		
		this.actionsPerExecutor = ArrayListMultimap.create();
		
		for (ThreadDump dump : this.dumps){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = this.actionHistory.get(timestamp);
			
			for(ThreadAction action : actions){
				String executor = action.getExecutor();
				this.actionsPerExecutor.put(
								executor, 
								action
								);
			}
		}
		
		return this.actionsPerExecutor;
	}
	
	public Multiset<String> getExecutorSet(){
		if (this.executorSets != null)
			return executorSets;
		
		this.executorSets = HashMultiset.create();
		
		for (ThreadDump dump : this.dumps){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = this.actionHistory.get(timestamp);
			
			for(ThreadAction action : actions){
				fillExecutorSet(action);
			}
		}
		
		return this.executorSets;
	}
	
	private void fillExecutorSet(ThreadAction action) {
		for (int i=0; i<action.size(); i++){
			if (action.getThreadStack(i).isETBI()){
				this.executorSets.add(ThreadStack.EXECUTOR_TO_BE_IDENTIFIED);
			}
			else{
				this.executorSets.add(action.getThreadStack(i).getExecutor());
			}
		}
	}
	
	private String loadId() {
		String id = ConfigThreadLocal.get(PROPERTY_JEYZER_ANALYSIS_ID);
		if (id == null) {
			String timestamp = Long.toString(System.currentTimeMillis());
			// Remove the 1st (century) and last 3 ms digits
			String reducedTimestamp = timestamp.substring(1, timestamp.length() - 3);
			Long value = Long.valueOf(reducedTimestamp);
			id = Long.toHexString(value) + "-i" + nextId.incrementAndGet(); // Note the -i variant 
		}
		return id;
	}
	
	@Override
	public void close() {
		for (Translator translator : this.translators)
			translator.close();
	}

	@Override
	public void applyMonitorAnalyzerRules(List<MonitorAnalyzerRule> rules, Multimap<String, MonitorAnalyzerEvent> events) throws JzrMonitorException {
		for (MonitorAnalyzerRule rule : rules){
			// apply rules to ourselves
			rule.applyCandidacy(this, events);
		}
		JeyzerMonitor.logger.info("Number of candidate analyzer monitoring events : " + events.size());
		
		// 2 - apply elected events
		for (MonitorAnalyzerRule rule : rules){
			// apply rules to ourselves
			rule.applyElection(this, events);
		}
	}
}
