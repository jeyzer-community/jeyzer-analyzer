package org.jeyzer.analyzer.data.stack;

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

import static org.jeyzer.analyzer.util.SystemHelper.CR;
import static org.jeyzer.analyzer.util.SystemHelper.JSON_EXTENSION;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jeyzer.analyzer.rule.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractThreadStack implements ThreadStack {

	private static final Logger logger = LoggerFactory.getLogger(AbstractThreadStack.class);	
	
	private static final String IBM_STACK_END_TAG = "NULL";
	
	private static final String JZ_PREFIX = "\tJz>\t";
	private static final String JZ_EQUALS = "\t";

	private static final String LOG_TRACE_NOT_SET = "NOT SET";	
	
	private ThreadStackHandler stackHandler;
	
	private String header;
	private String id;   // unique
	private String name; // not unique !!
	private ThreadState state;
	private boolean suspended;

	private String lockName;
	private String lockClassName; // not null
	private Date timestamp;
	
	protected DAEMON daemon = DAEMON.NOT_SET;
	protected int priority = ThreadStack.PRIORITY_NOT_AVAILABLE;
	
	private List<String> codeLines;
	protected StackText stackText;
	
	private int depthLength;
	private int lockCount;
	private boolean ofInterest;
	
	protected STACK_CODE_STATE stackCodeState;
	private boolean cpuRunnable;

	protected ThreadStackCPUInfo cpuInfo = null;
	protected ThreadStackMemoryInfo memoryInfo = null;
	protected ThreadStackJeyzerMXInfo jeyzerMXInfo = null;

	private ThreadStack lockingThread = null;
	private List<ThreadStack> lockedThreads = new ArrayList<>();
	private List<String> ownedlocks;
	protected List<String> biasedLocks = null;
	private boolean deadlock;
	private boolean codeLocked;
	private String codeLockedName;
	
	private List<String> functions = new ArrayList<>();
	// Important, function index is set : min = top of the stack, max = stack bottom
	// ps : code lines are stored as found, i.e in reversed order
	private NavigableMap<Integer, String> functionsIndexed = new TreeMap<>();

	private List<String> operations = new ArrayList<>();
	// Important, operation index is set : min = top of the stack, max = stack bottom
	// ps : code lines are stored as found, i.e in reversed order
	private NavigableMap<Integer, String> operationsIndexed = new TreeMap<>();

	private List<String> contentionTypes = new ArrayList<>();
	// Important, contention type index is set : min = top of the stack, max = stack bottom
	// ps : code lines are stored as found, i.e in reversed order
	private NavigableMap<Integer, String> contentionTypesIndexed = new TreeMap<>();
	
	private String executor;
	
	protected int filePos;
	protected String fileName;

	public AbstractThreadStack(String header, String id, String name, ThreadState state, int filePos, String fileName, Date timestamp, List<String> codeLines) {
		this.header = header;
		this.name = name;
		this.id = id;
		this.state = state;
		
		this.filePos = filePos;
		this.fileName = fileName;
		
		this.ofInterest = true;
		this.timestamp = timestamp;

		this.codeLines = codeLines;
		this.depthLength = codeLines.size();
		
		this.stackCodeState = STACK_CODE_STATE.UNKNOWN;
		
		this.stackHandler = new ThreadStackHandlerImpl(this);
	}	
	
	public AbstractThreadStack(String header, String name, String id, ThreadState state, boolean suspended, int filePos, String fileName, Date timestamp, 
			List<String> codeLines, String lockName, String lockClassName, List<String> ownedlocks, boolean deadlock){
		this(header, id, name, state, filePos, fileName, timestamp, codeLines);

		this.suspended = suspended;
		
		this.lockName = lockName;
		this.lockClassName = lockClassName;
		this.ownedlocks = new ArrayList<>(ownedlocks);
		this.deadlock = deadlock;
	}
	
	@Override
	public ThreadStackHandler getStackHandler(){
		return this.stackHandler;
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getFilePath(){
		return this.fileName;
	}	
	
	@Override
	public int getDepthLength() {
		return this.depthLength;
	}

	@Override
	public int getLockCount() {
		return this.lockCount;
	}

	@Override
	public boolean isOfInterest() {
		return this.ofInterest;
	}
	
	@Override
	public boolean isInDeadlock() {
		return this.deadlock;
	}	

	@Override
	public void pack(){
		this.codeLines.clear();
		this.codeLines.add(PACKED_STACK);
		this.stackText = null;
	}

	@Override
	public boolean isLocked(){
		return isCodeLocked() || isBlocked();
	}
	
	@Override
	public boolean isCodeLocked(){
		return this.codeLocked;
	}
	
	@Override
	public boolean isBlocked(){
		return this.state.isBlocked();
	}

	@Override
	public boolean isWaiting(){
		return this.state.isWaiting();
	}
	
	@Override
	public boolean isTimedWaiting(){
		return this.state.isTimedWaiting();
	}

	@Override
	public boolean isRunning(){
		return this.state.isRunning();
	}	
	
	@Override
	public boolean isCarrying() {
		return ThreadState.CARRYING_VIRTUAL_THREAD.equals(this.state);
	}
	
	@Override
	public boolean isUFO(){
		return this.isOTBI() 
				|| this.isATBI() 
				|| this.isETBI();
	}
	
	@Override
	public boolean isATBI(){
		return this.getFunctionTags().isEmpty();
	}
	
	@Override
	public boolean isOTBI(){
		return this.getOperationTags().isEmpty();
	}

	@Override
	public boolean isETBI(){
		return this.executor == null;
	}
	
	@Override
	public String getHeader() {
		return this.header;
	}
	
	public boolean hasHeader() {
		return !this.isVirtual();
	}

	@Override
	public String getID() {
		return this.id;
	}

	@Override
	public ThreadState getState() {
		return this.state;
	}

	@Override
	public boolean isSuspended() {
		return this.suspended;
	}
	
	@Override
	public DAEMON getDaemon() {
		return this.daemon;
	}

	@Override
	public int getPriority() {
		return this.priority;
	}
	
	@Override
	public String getLockName(){
		return this.lockName;
	}
	
	@Override
	public String getLockClassName(){
		return this.lockClassName;
	}
	
	@Override
	public Date getTimeStamp(){
		return this.timestamp;
	}
	
	@Override
	public void setLockingThread(ThreadStack stack){
		this.lockingThread = stack;
	}
	
	@Override
	public List<String> getOwnedLocks(){
		return this.ownedlocks;
	}
	
	@Override
	public boolean hasOwnedLocks(){
		return !this.ownedlocks.isEmpty();
	}
	
	@Override
	public List<String> getBiasedLocks(){
		return (this.biasedLocks != null)? this.biasedLocks : new ArrayList<>(0);
	}
	
	@Override
	public boolean hasBiasedLocks(){
		return biasedLocks != null && !this.biasedLocks.isEmpty();
	}
	
	@Override
	public void addLockedThread(ThreadStack stack){
		if (!this.lockedThreads.contains(stack))
			this.lockedThreads.add(stack);
	}
	
	@Override
	public void setCodeLocked(boolean codeLocked){
		this.codeLocked = codeLocked;
	}
	
	@Override
	public String getCodeLockName(){
		return this.codeLockedName;
	}
	
	@Override
	public void setCodeLockName(String codeLockedName){
		this.codeLockedName = codeLockedName;
	}
	
	@Override
	public void setInterest(boolean interest){
		this.ofInterest = interest;
	}
	
	@Override
	public void setInDeadlock(boolean value){
		this.deadlock = value;
	}
	
	@Override
	public void setStackCodeState(STACK_CODE_STATE freezeState){
		this.stackCodeState = freezeState;
	}
	
	@Override
	public boolean isFrozenStackCode(){
		return stackCodeState.equals(STACK_CODE_STATE.FREEZE_BEGIN) 
				|| stackCodeState.equals(STACK_CODE_STATE.FREEZE_MIDDLE)
				|| stackCodeState.equals(STACK_CODE_STATE.FREEZE_END);
	}
	
	@Override
	public STACK_CODE_STATE getStackCodeState(){
		return this.stackCodeState;
	}
	
	@Override
	public ThreadStackCPUInfo getCpuInfo() {
		return cpuInfo; // may be null
	}

	@Override
	public ThreadStackMemoryInfo getMemoryInfo() {
		return memoryInfo; // may be null
	}
	
	@Override
	public ThreadStackJeyzerMXInfo getThreadStackJeyzerMXInfo() {
		return jeyzerMXInfo; // may be null
	}
	
	@Override
	public void addFunctionTag(String value){
		this.functions.add(value);
	}
	
	@Override
	public void addFunctionTagIndexed(int lineMatch, String name) {
		this.functionsIndexed.put(lineMatch, name);
	}

	@Override
	public void addOperationTag(String value){
		this.operations.add(value);
	}
	
	@Override
	public void addOperationTagIndexed(int lineMatch, String name) {
		this.operationsIndexed.put(lineMatch, name);
	}
	
	@Override
	public List<String> getOperationTags(){
		return this.operations;
	}
	
	@Override
	public SortedMap<Integer, String> getSourceLocalizedOperationTagsInRange(int start, int end) {
		return this.operationsIndexed.subMap(start, end); // start inclusive, end exclusive
	}
	
	@Override
	public SortedMap<Integer, String> getSourceLocalizedOperationTags(){
		return this.operationsIndexed; // start inclusive, end exclusive
	}
	
	@Override
	public String getPrincipalOperation(){
		if (this.operations.isEmpty())
			return ThreadStack.OPER_TO_BE_IDENTIFIED;
		else
			return this.operations.get(0);
	}	

	@Override
	public String getPrincipalTag(){
		if (this.functions.isEmpty())
			return ThreadStack.FUNC_TO_BE_IDENTIFIED;
		else
			return this.functions.get(0);
	}
	
	@Override
	public List<String> getFunctionTags(){
		return this.functions;
	}
	
	@Override
	public SortedMap<Integer, String> getSourceLocalizedFunctionTagsInRange(int start, int end) {
		return this.functionsIndexed.subMap(start, end); // start inclusive, end exclusive
	}
	
	@Override
	public SortedMap<Integer, String> getSourceLocalizedFunctionTags() {
		return this.functionsIndexed;
	}
	
	@Override
	public boolean addContentionTypeTag(String value){
		if (this.contentionTypes.contains(value))
			return false; // duplicates are excluded
		this.contentionTypes.add(value);
		return true;
	}
	
	@Override
	public void addContentionTypeTagIndexed(int lineMatch, String name) {
		this.contentionTypesIndexed.put(lineMatch, name);
	}
	
	@Override
	public String getPrincipalContentionType(){
		if (!this.contentionTypes.isEmpty())
			return this.contentionTypes.get(0);
		else 
			return getDefaultContentionType();
	}
	
	@Override
	public List<String> getContentionTypeTags(){
		return this.contentionTypes;
	}
	
	@Override
	public String getDefaultContentionType(){
		if (this.isCodeLocked())
			return CONTENTION_TYPE_CODE_LOCKED;  // Usually, declared as operation type as Synchro
		else if (this.state.isRunning() || (this.isVirtual() && !(this.state.isTimedWaiting() || this.state.isWaiting())))
			return CONTENTION_TYPE_CODE_EXEC;
		else
			return this.state.getDislayName();  // blocked, waiting, timed_wait, unknown
	}
	
	@Override
	public SortedMap<Integer, String> getSourceLocalizedContentionTypeTagsInRange(int start, int end) {
		return this.contentionTypesIndexed.subMap(start, end); // start inclusive, end exclusive
	}
	
	@Override
	public SortedMap<Integer, String> getSourceLocalizedContentionTypeTags() {
		return this.contentionTypesIndexed;
	}
	
	@Override
	public List<ThreadStack> getLockedThreads(){
		return this.lockedThreads;
	}
	
	@Override
	public ThreadStack getLockingThread(){
		return this.lockingThread;
	}
	
	@Override
	public void setExecutor(String executor){
		this.executor = executor;
	}
	
	@Override
	public void setCPURunnable(boolean runnableCPU) {
		this.cpuRunnable = runnableCPU;
	}
	
	@Override
	public boolean isCPURunnable() {
		return this.cpuRunnable;
	}

	@Override
	public String getExecutor(){
		if (this.executor == null)
			return EXECUTOR_TO_BE_IDENTIFIED;
		else
			return this.executor;
	}
	
	@Override
	public void applyRules(List<Rule> rules){
		for (Rule rule : rules){
			rule.apply(this);
		}
	}
	
	// jdk 1.6+
	@Override
	public void setLockCount(int synchroCount){
		this.lockCount = synchroCount;
	}
	
	@Override
	public String toString(){
		StringBuilder b = new StringBuilder(3000);
		
		b.append("*************************************************************" + CR);
		b.append("Thread : " + this.name + CR);
		b.append(" - ID              : " + this.id + CR);
		b.append(" - Timestamp       : " + this.timestamp + CR);
		b.append(" - State           : " + this.state + CR);
		b.append(" - Virtual         : " + (this.isVirtual() ? "yes" : "no") + CR);
		if (this.isVirtual()) {
			b.append(" - Instance count  : " + this.getInstanceCount() + CR);			
		}
		b.append(" - Working         : " + (this.ofInterest ? "yes" : "no") + CR);
		b.append(" - Header          : " + this.header + CR);
		b.append(" - File pos        : " + this.filePos + CR);
		b.append(" - Depth           : " + this.depthLength + CR);
		b.append(" - Lock count      : " + this.lockCount + CR);
		b.append(" - Lock name       : " + this.lockName + CR);
		b.append(" - Lock class name : " + this.lockClassName + CR);
		if (this.cpuInfo != null){
			b.append(this.cpuInfo.toString());
		}
		if (this.memoryInfo != null){
			b.append(this.memoryInfo.toString());
		}
		b.append(" - Executor   : " + (this.executor == null ? LOG_TRACE_NOT_SET:this.executor) + CR);
		b.append(" - Functions  : " + (this.functions.isEmpty() ? LOG_TRACE_NOT_SET:"") + CR);
		for (String function : this.functions){
			b.append("    - " + function + CR);
		}
		b.append(" - Operations : " + (this.operations.isEmpty() ? LOG_TRACE_NOT_SET:"") + CR);
		for (String operation : this.operations){
			b.append("    - " + operation + CR);
		}
		b.append(" - Contentions : " + (this.contentionTypes.isEmpty() ? LOG_TRACE_NOT_SET:"") + CR);
		for (String contentionType : this.contentionTypes){
			b.append("    - " + contentionType + CR);
		}
		b.append(" - Code lines (" + this.codeLines.size()  + ") : " + CR);
		for (String line : this.codeLines){
			b.append("    - " + line + CR);
		}
		if (this.codeLines.size() < 20) {
			b.append(" - Exclude candidate : " + CR);
			b.append("    - ");
			Iterator<String> methodNamesIter = this.getMethodNames().iterator();
			while (methodNamesIter.hasNext()){
				String methodName = methodNamesIter.next();
				int posSlash = methodName.indexOf('/'); // remove any module prefix such as java.base@11.0.6/
				if (posSlash != -1)
					methodName = methodName.substring(posSlash+1);
				b.append(methodName);
				if (methodNamesIter.hasNext())
					b.append(';');
			}
			b.append(CR);
		}
		
		return b.toString();
	}
	
	/*
	/  Inner methods
	*/

	List<String> getCodeLines() {
		return this.codeLines;
	}
	
	StackText getStackText(){
		return this.readStackText(false);
	}
	
	StackText getStackTextWithoutEmptyJzrValues(){
		return this.readStackText(true);
	}

	private List<String> getMethodNames(){
		List<String> methodNames = new ArrayList<>(this.codeLines.size());
		
		if (this.codeLines.isEmpty())
			return methodNames;
		
		if (this.codeLines.get(0).contains("at ")) { 
			// standard case
			for (String line : this.codeLines) {
				int atPos = line.indexOf("at ");
				int parenthesisPos = line.indexOf('(');
				if (atPos != -1 && parenthesisPos != -1 && parenthesisPos > atPos)
					methodNames.add(line.substring(atPos+3, parenthesisPos));
			}
		}
		else if (this.codeLines.get(0).contains("      ")) { 
			// Jcmd txt case
			for (String line : this.codeLines) {
				int parenthesisPos = line.indexOf('(');
				if (parenthesisPos != -1)
					methodNames.add(line.substring(6, parenthesisPos));
			}
		}
		else {
			// default (and Jcmd JSON case)
			for (String line : this.codeLines) {
				int parenthesisPos = line.indexOf('(');
				if (parenthesisPos != -1)
					methodNames.add(line.substring(0, parenthesisPos));
			}
		}

		return methodNames;
	}
	
	private StackText readStackText(boolean excludeJzrEmptyValues){
		if (this.stackText != null)
			return this.stackText;
		
		// If JSON extension is not there, file pos will be negative : interpret it as json
		if (this.fileName.endsWith(JSON_EXTENSION) || this.filePos == -1) {
			// JCMD JSON format includes only code lines, 
			// so the stack text can be directly set with the stack code lines
			this.stackText = buildJsonStackText();
			return this.stackText;
		}
			
		File file = new File(this.fileName);
		StackText st = new StackText();
						
		String line = "s";
		int pos = 0;
	
		try (
				FileReader fr = new FileReader(file);
				BufferedReader reader = new BufferedReader(fr);
			)
		{
			while(pos != this.filePos){
				reader.readLine();
				pos++;
			}
			
			while(true){
				line = reader.readLine();
				if (line.isEmpty() || line.startsWith(IBM_STACK_END_TAG))
					break;
				if (excludeJzrEmptyValues && line.startsWith(JZ_PREFIX)) {
					if (line.endsWith(JZ_EQUALS))
						continue; // ignore
					else
						line = line.replace('\t', ' '); // cosmetic for Excel as it ignores the tabulations in cell comments
				}
				st.appendLine(line);
			}
		}catch(FileNotFoundException ex){
			logger.error("Failed to open file : " + file.getPath(), ex);
			return st;
		}catch(Exception e){
			logger.error("Failed to read file : " + file.getPath(), e);
		}
		
		this.stackText = st;
		
		return st;
	}
	
	private StackText buildJsonStackText() {
		StackText st = new StackText();
		
		for (String line : this.codeLines) {
			st.appendLine(' ' + line); // add cosmetic space for Excel
		}
		
		return st;
	}

	/*
	 * Provides an equal method focused on the stack content
	 */
	public static class ThreadStackHandlerImpl implements ThreadStackHandler{
		
		private AbstractThreadStack stack;
		
		ThreadStackHandlerImpl(AbstractThreadStack stack){
			this.stack = stack;
		}
		
		@Override
		public boolean equals(Object obj) {
			// same object
			if (this == obj)
				return true;
			
			if (!(obj instanceof AbstractThreadStack.ThreadStackHandlerImpl))
				return false;
			
			// comparison
			AbstractThreadStack.ThreadStackHandlerImpl otherStack = (AbstractThreadStack.ThreadStackHandlerImpl)obj;
			List<String> stackLines = this.stack.getCodeLines();
			
			if (otherStack.getCodeLines() == null)
				return false;
			
			List<String> otherLines = otherStack.getCodeLines();
			if (stackLines.size() != otherLines.size())
				return false;
			
			// compare the 2 stacks
			ListIterator<String> iter = otherLines.listIterator();
			for (String codeLine : stackLines){
				if (iter.hasNext()){
					String line = iter.next();
					if (codeLine.equals(line))
						continue;
					else
						return false;
				}
				else {
					return false;
				}
			}
			return true;
		}

		@Override
		public int hashCode() {
			  List<String> stackLines = this.stack.getCodeLines();
			  int hash = 6;
			  int index = 0;
			  
			  for (String line : stackLines){
				  hash += 32 * line.hashCode();
				  index++;
				  if (index > 10)
					  break;
			  }
			  
			  return hash;
		}
		
		@Override
		public ThreadStack getThreadStack(){
			return this.stack;
		}

		@Override
		public List<String> getCodeLines(){
			return stack.getCodeLines();
		}
		
		@Override
		public StackText getText(){
			return stack.getStackText();
		}

		@Override
		public StackText getJzrFilteredText(){
			return stack.getStackTextWithoutEmptyJzrValues();
		}		
		
		@Override
		public List<String> getReversedCodeLines() {
			List<String> reversedList = new ArrayList<>(stack.getCodeLines());
			Collections.reverse(reversedList);
			return reversedList;
		}
	}
}
