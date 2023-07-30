package org.jeyzer.analyzer.data;

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



import static org.jeyzer.analyzer.math.FormulaHelper.DOUBLE_TO_LONG_NA;
import static org.jeyzer.analyzer.util.SystemHelper.CR;
import static org.jeyzer.analyzer.data.stack.ThreadStack.VIRTUAL_THREAD_UNPARK_HEADER;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jeyzer.analyzer.config.analysis.ConfigStackSorting.StackSortingKey;
import org.jeyzer.analyzer.data.event.ExternalEvent;
import org.jeyzer.analyzer.data.event.JzrPublisherEvent;
import org.jeyzer.analyzer.data.gc.GarbageCollection;
import org.jeyzer.analyzer.data.memory.MemoryPools;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStack.ThreadStackHandler;
import org.jeyzer.analyzer.data.stack.ThreadStackCPUInfo;
import org.jeyzer.analyzer.data.stack.ThreadStackMemoryInfo;
import org.jeyzer.analyzer.data.tag.ContentionTypeTag;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.rule.Rule;
import org.jeyzer.analyzer.setup.CPURunnableContentionTypesManager;
import org.jeyzer.service.action.id.ActionIdGenerator;
import org.jeyzer.service.action.id.StackGroupActionIdGenerator;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;


public class ThreadDump {
	
	private String filePath;
	private String fileName;
	private Date timestamp;
	private long fileSize;	
	private long fileWriteTime = -1;
	private long fileWriteSize = -1;
	
	// capture time in ms
	private long captureTime = -1;
	
	// collected
	private double processCpu = -1;  // percentage
	private long   processUpTime = -1; // ms
	private double operatingSystemCpu = -1;  // percentage
	
	private long  processOpenFDCount = -1;
	private int   processOpenFDUsage = -1; // percentage, computed
	
	// collected physical memory
	private long freeMemory = -1;
	private long totalMemory = -1;
	
	// computed based on stack cpu times 
	private double processComputedCpuUsage = 0;      // percentage
	private double processComputedSystemUsage = 0;   // percentage
	private double processComputedUserUsage = 0;     // percentage
	private double applicativeCpuActivityUsage = 0;  // percentage

	// time slice since last thread dump. Can be zero. Capture time ignored
	private long previousTimeSlice = 0; // nanosec
	private long nextTimeSlice = 0; // nanosec
	
	// timestamp of the previous thread and next thread
	private Date previousTimestamp;
	@SuppressWarnings("unused")
	private Date nextTimestamp;
	
	// exclusive with hiatus, advanced mode only
	private boolean restart = false;
	
	// exclusive with restart, capture time taken into account
	private boolean prevHiatus = false;
	
	private long totalCPUTime = 0; // nanosec
	private long totalUserTime = 0; // nanosec
	private long totalSystemTime = 0; // nanosec

	private long totalComputedMemory = -1; // bytes
	private double applicativeMemoryActivityUsage = -1;  // percentage

	private int cpuRunnableThreadsCount = 0;
	
	private Boolean hasVirtualThreads = null;
	
	private List<ThreadStack> stacks = new ArrayList<>();
	private Map<String, ThreadStack> stacksPerId = new HashMap<>();

	// stacks of interest
	private List<ThreadStack> workingThreads = null;
	private boolean hasStartingActions = false;
	private boolean hasStartingThreadStackGroupActions = false;
	
	private GarbageCollection garbageCollection = new GarbageCollection();
	// heap, non heap and all zone memory pools 
	private MemoryPools memoryPools = new MemoryPools();
	
	private DiskSpaces diskSpaces = new DiskSpaces();
	
	// collected 
	private int objectPendingFinalizationCount = -1;

	// Jeyzer MX bean context parameters
	private Map<String,String> jHContextParams = new HashMap<>();
	
	// MX bean parameters
	private Map<String,String> mxBeanParams = new HashMap<>();
	
	private List<String> deadlockTexts = new ArrayList<>();
	
	private List<ExternalEvent> extEvents = new ArrayList<>();
	private List<JzrPublisherEvent> pubEvents = new ArrayList<>();
	
	public ThreadDump(File file, Date timestamp){
		this.filePath = file.getAbsolutePath();
		this.fileName = file.getName();
		this.fileSize = file.length();
		this.timestamp = timestamp;
	}

	public ThreadStack addStack(ThreadStack stack){
		stacks.add(stack);
		stacksPerId.put(stack.getID(), stack);
		return stack;
	}
	
	public GarbageCollection getGarbageCollection(){
		return this.garbageCollection;
	}
	
	public MemoryPools getMemoryPools(){
		return this.memoryPools;
	}	
	
	public DiskSpaces getDiskSpaces() {
		return diskSpaces;
	}

	public Multiset<Tag> getPrincipalContentionTypes() {
		Multiset<Tag> principalContentionTypeTags = HashMultiset.create();
		
		for (ThreadStack stack : this.workingThreads){
			principalContentionTypeTags.add(
					new ContentionTypeTag(stack.getPrincipalContentionType())
					);
		}
		
		return principalContentionTypeTags;
	}
	
	public void applyRules(List<Rule> rules){
		// apply rules to ourselves
		for (Rule rule : rules){
			rule.apply(this);
		}
		
		// apply rules to stacks
		for (ThreadStack stack : this.stacks){
			stack.applyRules(rules);
		}
	}
	
	public void updateActions(Map<String,ThreadAction> openActions, Map<Date,Set<ThreadAction>> actionHistory, ActionIdGenerator actionIdGenerator){
		Map<String,ThreadAction> remainingActions = new HashMap<>(openActions);
		
		// for each working thread..
		List<ThreadStack> wts = this.getWorkingThreads();
		
		// ..get related action
		for (ThreadStack stack : wts){
			String actionId = buildId(stack);
			ThreadAction ta = openActions.get(actionId);
			if (ta == null || isRestart()){
				// not found or restart, create new one
				ThreadAction action = new ThreadAction(
						stack, 
						actionIdGenerator.getActionId(stack)
						);
				action.setMinStartDate(previousTimestamp);
				openActions.put(actionId, action);
				if (isRestart() && remainingActions.containsKey(actionId)){
					// close any previous action with same id
					closeAction(actionHistory, remainingActions, actionId);
					remainingActions.remove(actionId);
				}
				hasStartingActions = true;
			}
			else{
				ta.updateAction(stack);
				remainingActions.remove(actionId);
			}
		}
		
		Set<String> remainingIds = remainingActions.keySet();
		// process the closed actions
		for (String rid : remainingIds){
			closeAction(actionHistory, remainingActions, rid);
			openActions.remove(rid);
		}
	}
	
	private void closeAction(Map<Date, Set<ThreadAction>> actionHistory,
			Map<String, ThreadAction> remainingActions, String actionId) {
		ThreadAction actionToArchive = remainingActions.get(actionId);
		actionToArchive.setMaxEndDate(timestamp);
		Set<ThreadAction> archivedActions = actionHistory.get(actionToArchive.getStartDate());
		archivedActions.add(actionToArchive);
	}
	
	private void closeThreadStackGroupAction(Map<Date, Set<ThreadStackGroupAction>> actionHistory, Map<ThreadStackHandler, ThreadStackGroupAction> remainingActions, ThreadStackHandler actionId) {
		ThreadStackGroupAction actionToArchive = remainingActions.get(actionId);
		actionToArchive.setMaxEndDate(timestamp);
		Set<ThreadStackGroupAction> archivedActions = actionHistory.get(actionToArchive.getStartDate());
		archivedActions.add(actionToArchive);
	}

	private String buildId(ThreadStack stack) {
		String id;
		
		id = stack.getID();
		if (stack.getThreadStackJeyzerMXInfo() != null && !stack.getThreadStackJeyzerMXInfo().getJzrId().isEmpty())
			id = id + "@" + stack.getThreadStackJeyzerMXInfo().getJzrId();
		
		return id;
	}

	public List<ThreadStack> getWorkingThreads(){
		if (this.workingThreads != null)
			return this.workingThreads;
		
		this.workingThreads = new ArrayList<>();
		for(ThreadStack stack : this.stacks){
			if (stack.isOfInterest())
				this.workingThreads.add(stack);
		}
		
		return this.workingThreads;
	}
	
	public List<ThreadStack> getThreads(){
		return this.stacks;
	}	

	public ThreadStack getStack(String id){
		return this.stacksPerId.get(id);
	}	
	
	public List<ThreadStack> getDiscardedThreads(){
		List<ThreadStack> dt = new ArrayList<>();
		
		for(ThreadStack stack : this.stacks){
			if (!stack.isOfInterest())
				dt.add(stack);
		}
		
		return dt;
	}	
	
	/**
	 * Get the number of threads (active and non active) in the dump
	 */
	public int size(){
		return this.stacks.size();
	}		
	
	public List<ThreadStack> getUFOThreads(){
		List<ThreadStack> wt = getWorkingThreads();
		List<ThreadStack> ufot = new ArrayList<>();
		
		for (ThreadStack ts : wt){
			if (ts.isUFO()){
				ufot.add(ts);
			}
		}
		
		return ufot;
	}	
	
	@Override
	public String toString(){
		StringBuilder b = new StringBuilder(100);
		
		b.append(CR);
		b.append("-------------------------------------------------------------" + CR);
		b.append(" Dump : " + this.fileName + CR);
		b.append("-------------------------------------------------------------" + CR);
		b.append(" - Timestamp  : " + this.timestamp + CR);
		b.append(" - File size  : " + this.fileSize + CR);
		b.append(" - File path  : " + this.filePath + CR);
//		b.append(" - Thread Stacks  : \n");
//		for (ThreadStack stack : this.stacks){
//			stack.dump();
//		}	
		
		return b.toString();
	}
	
	public void pack(){
		for(ThreadStack stack : this.stacks){
			if (!stack.isOfInterest())
				stack.pack();
		}
	}
	
	public Date getTimestamp(){
		return this.timestamp;
	}
	
	public String getFileName(){
		return this.fileName;
	}
	
	public String getFilePath(){
		return this.filePath;
	}
	
	public long getFileSize(){
		return this.fileSize;
	}
	
	public long getWriteTime() {
		return this.fileWriteTime;
	}
	
	public long getWriteSize() {
		return this.fileWriteSize;
	}
	
	public long getCaptureTime() {
		return captureTime;
	}

	public void setCaptureTime(long captureTime) {
		this.captureTime = captureTime;
	}

	public int getObjectPendingFinalizationCount() {
		return this.objectPendingFinalizationCount;
	}	
	
	public void setObjectPendingFinalizationCount(int objectPendingFinalizationCount) {
		this.objectPendingFinalizationCount = objectPendingFinalizationCount;
	}

	public static class ThreadDumpComparator implements Comparator<ThreadDump>{
		 
	    @Override
	    public int compare(ThreadDump td1, ThreadDump td2) {
	        return td1.getTimestamp().compareTo(td2.getTimestamp());
	    }
	}

	public void updateCPUData(ThreadDump previousTd) {
		ThreadStack previousStack = null;
		this.totalCPUTime = 0;
		this.totalUserTime = 0;
		this.totalSystemTime = 0;
		this.processComputedCpuUsage = 0;
		this.processComputedSystemUsage = 0;
		this.processComputedUserUsage = 0;
		
		// if first thread dump, do not compute
		if (previousTd != null){
			// collect stack times
			for (ThreadStack stack : this.stacks){
				String id = stack.getID();
				previousStack = previousTd.getStack(id); // can be null
				ThreadStackCPUInfo stackCPUInfo = stack.getCpuInfo();
	 			this.totalCPUTime += stackCPUInfo.updateCPUData(previousStack, this.previousTimeSlice);
	 			this.totalSystemTime += stackCPUInfo.updateSystemData(previousStack, this.previousTimeSlice);
	 			this.totalUserTime += stackCPUInfo.updateUserData(previousStack, this.previousTimeSlice);
			}
			if (this.previousTimeSlice > 0){ 
				this.processComputedCpuUsage = FormulaHelper.percent(totalCPUTime, previousTimeSlice);
				this.processComputedSystemUsage = FormulaHelper.percent(totalSystemTime, previousTimeSlice);
				this.processComputedUserUsage = FormulaHelper.percent(totalUserTime, previousTimeSlice);
			}
		}else{
			// NA case
			this.processComputedCpuUsage = -1;
			this.processComputedSystemUsage = -1;
			this.processComputedUserUsage = -1;
		}
	}

	public double getProcessCpu() {
		return processCpu;
	}

	public long getProcessUpTime() {
		return processUpTime;
	}
	
	public long getProcessOpenFileDescriptorCount() {
		return this.processOpenFDCount;
	}
	
	public int getProcessOpenFileDescriptorUsage() {
		return this.processOpenFDUsage;
	}
	
	public double getOperatingSystemCpu() {
		return operatingSystemCpu;
	}
	
	public double getProcessComputedCPUUsage() {
		return processComputedCpuUsage;
	}

	public double getProcessComputedSystemUsage() {
		return processComputedSystemUsage;
	}	
	
	public double getProcessComputedUserUsage() {
		return processComputedUserUsage;
	}

	public double getApplicativeCpuActivityUsage() {
		return applicativeCpuActivityUsage;
	}	
	
	public long getTotalCPUTime() {
		return totalCPUTime;
	}

	public long getTotalUserTime() {
		return totalUserTime;
	}
	
	public long getTotalSystemTime() {
		return totalSystemTime;
	}
	
	public long getTotalComputedMemory() {
		return totalComputedMemory;
	}

	public double getApplicativeMemoryActivityUsage() {
		return applicativeMemoryActivityUsage;
	}

	public void updateApplicativeActivityUsage(long cpuTimePeak, long timeSlicePeak) {
		double ratio = (double) this.previousTimeSlice / timeSlicePeak;
		
		final double epsilon = 1E-10;
		// zero equality test with doubles, in order to test that processComputedCpuUsage is -1
		if(this.processComputedCpuUsage + 1 < epsilon){  
			// NA case
			applicativeCpuActivityUsage = -1;
		}
		else if (totalCPUTime > 0){
			long adjTotalCPUTime = (long) (this.totalCPUTime / ratio);  // acceptable precision loss
			applicativeCpuActivityUsage = FormulaHelper.percent(adjTotalCPUTime, cpuTimePeak);
		}
		else{
			applicativeCpuActivityUsage = 0;
		}

		// compute stack time percentages
		for (ThreadStack stack : this.stacks){
			ThreadStackCPUInfo stackCPUInfo = stack.getCpuInfo();
			stackCPUInfo.updateApplicativeActivityUsage(cpuTimePeak, ratio);
		}
 	}

	public void setProcessCPU(double processCpu) {
		this.processCpu = processCpu;
		if (Double.doubleToRawLongBits(this.processCpu) != DOUBLE_TO_LONG_NA)
			this.processCpu *= 100;
	}
	
	public void setProcessUpTime(long processUpTime) {
		this.processUpTime = processUpTime;
	}
	
	public void setProcessOpenFileDescriptorCount(Long processOpenFDCount) {
		this.processOpenFDCount = processOpenFDCount;
	}
	
	public void setProcessOpenFileDescriptorUsage(int processOpenFDUsage) {
		this.processOpenFDUsage = processOpenFDUsage;
	}
	
	public void setWriteTime(long writeTime) {
		this.fileWriteTime = writeTime;
	}
	
	public void setWriteSize(long writeSize) {
		this.fileWriteSize = writeSize;
	}

	public void setOperatingSystemCPU(double systemCpu) {
		this.operatingSystemCpu = systemCpu;
		if (Double.doubleToRawLongBits(this.operatingSystemCpu) != DOUBLE_TO_LONG_NA)
			this.operatingSystemCpu *= 100;
	}

	public long getTimeSlice() {
		return this.previousTimeSlice;
	}

	public long getNextTimeSlice() {
		return this.nextTimeSlice;
	}
	
	/*
	 * System memory
	 */
	
	public void setSystemPhysicalFreeMemory(long freeMemory) {
		this.freeMemory = freeMemory;
	}	

	public void setSystemPhysicalTotalMemory(long totalMemory) {
		this.totalMemory = totalMemory;
	}	

	public long getSystemPhysicalFreeMemory() {
		return this.freeMemory;
	}	

	public long getSystemPhysicalUsedMemory() {
		if (this.totalMemory == -1 || this.freeMemory == -1)
			return -1;
		return this.totalMemory - this.freeMemory;
	}

	public double getSystemPhysicalUsedMemoryPercentage() {
		if (this.totalMemory == -1 || this.freeMemory == -1 || this.totalMemory == 0)
			return -1;

		return FormulaHelper.percent(getSystemPhysicalUsedMemory(), this.totalMemory);
	}	
	
	public long getSystemPhysicalTotalMemory() {
		return this.totalMemory;
	}

	/*
	 * Stack memory
	 */
	
	public void updateMemoryData(ThreadDump previousTd) {
		ThreadStack previousStack = null;
		this.totalComputedMemory = 0;
		
		// collect stack memory
		// if first thread dump, do not compute
		if (previousTd != null){
			for (ThreadStack stack : this.stacks){
				String id = stack.getID();
				previousStack = previousTd.getStack(id); // can be null
				ThreadStackMemoryInfo stackMemoryInfo = stack.getMemoryInfo();
	 			this.totalComputedMemory += stackMemoryInfo.updateMemoryData(this.memoryPools, previousStack, totalMemory);
			}
		}
		else{
			this.totalComputedMemory = -1;
		}
	}
	
	public void sortStacks(StackSortingKey stackSortingKey) {
		if (stackSortingKey == null || StackSortingKey.RECORDING.equals(stackSortingKey))
			return; // keep it as it is
		
		Comparator<ThreadStack> comparator = null;
		if (StackSortingKey.THREAD_ID.equals(stackSortingKey))
			comparator = (ThreadStack st1, ThreadStack st2) -> st1.getID().compareTo(st2.getID());
		else if (StackSortingKey.THREAD_NAME.equals(stackSortingKey))
			comparator = (ThreadStack st1, ThreadStack st2) -> st1.getName().compareTo(st2.getName());
		
		if (comparator != null)
			this.stacks.sort(comparator);
	}

	public void updateApplicativeMemoryActivityUsage(long memoryPeak, long timeSlicePeak) {
		double ratio = (double) this.previousTimeSlice / timeSlicePeak;
		
		if(this.totalComputedMemory == -1){
			// NA case
			applicativeMemoryActivityUsage = -1;
		}
		else if (totalComputedMemory > 0){
			long adjTotalMemory = (long) (this.totalComputedMemory / ratio);  // acceptable precision loss
			applicativeMemoryActivityUsage = FormulaHelper.percent(adjTotalMemory, memoryPeak);
		}
		else{
			applicativeMemoryActivityUsage = 0;
		}

		// compute stack time percentages
		for (ThreadStack stack : this.stacks){
			ThreadStackMemoryInfo stackMemoryInfo = stack.getMemoryInfo();
			stackMemoryInfo.updateApplicativeActivityUsage(memoryPeak, ratio);
		}
	}

	public void updateTimestamps(Date previousTimestamp, Date nextTimestamp) {
		if (previousTimestamp != null){
			this.previousTimestamp = previousTimestamp;
			this.previousTimeSlice = this.getTimestamp().getTime() - previousTimestamp.getTime();
			this.previousTimeSlice = previousTimeSlice * 1000000L; // time slice in nanosec may differ from thread dump period or may correspond to hiatus
		}
		else{
			this.previousTimestamp = this.timestamp;
		}
		if (nextTimestamp != null){
			this.nextTimestamp = nextTimestamp;
			this.nextTimeSlice = nextTimestamp.getTime() - this.getTimestamp().getTime();
			this.nextTimeSlice = nextTimeSlice * 1000000L; // time slice in nanosec may differ from thread dump period or may correspond to hiatus
		}
		else{
			this.nextTimestamp = this.timestamp;
		}
	}

	public void updateGarbageCollectionData(ThreadDump previous) {
		this.garbageCollection.updateGarbageCollectionData(previous, this.getTimeSlice());
	}

	public void addDeadLock(String deadlockText) {
		deadlockTexts.add(deadlockText);
	}
	
	public List<String> getDeadLockTexts() {
		return deadlockTexts;
	}
	
	public int hasDeadLock() {
		// Todo : clarify the number of locks.
		
		// With jstack, we have this info thanks to the DeadLock text
		if (!deadlockTexts.isEmpty())
			return deadlockTexts.size(); // gives the exact number 
		
		// With the agent, we have only the deadlock candidate info
		for(ThreadStack stack : this.stacks){
			if (stack.isInDeadlock())
				return 1; // cannot give the exact number at this stage
		}
		
		return 0;
	}
	
	public int hasSuspendedThreads() {
		int count = 0;
		for(ThreadStack stack : this.stacks){
			if (stack.isSuspended())
				count++;
		}
		
		return count;
	}
	
	public boolean hasVirtualThreadPresence() {
		if (hasVirtualThreads())
			return true;
		
		// check otherwise if we have carrier threads
		for (ThreadStack stack : this.stacks) {
			if (stack.isCarrying())
				return true;
		}
		
		// At last, check if we have a VirtualThread-unparker thread
		for (ThreadStack stack : this.stacks) {
			if (VIRTUAL_THREAD_UNPARK_HEADER.equals(stack.getName()))
				return true;
		}
		
		return false;
	}

	public boolean hasVirtualThreads() {
		if (this.hasVirtualThreads != null)
			return this.hasVirtualThreads;
		
		for (ThreadStack stack : this.stacks) {
			if (stack.isVirtual()) {
				this.hasVirtualThreads = Boolean.TRUE;
				return true;
			}
		}
		
		this.hasVirtualThreads = Boolean.FALSE;
		return this.hasVirtualThreads;
	}	
	
	public void addJeyzerMXContextParam(String param, String value) {
		jHContextParams.put(param, value);
	}
	
	public Map<String,String> getJeyzerMXContextParams(){
		return jHContextParams;
	}
	
	public void addJMXBeanParam(String param, String value) {
		mxBeanParams.put(param, value);
	}
	
	public Map<String,String> getJMXBeanParams(){
		return mxBeanParams;
	}	

	public List<ExternalEvent> getExternalEvents() {
		return extEvents;
	}
	
	public void addExternalEvent(ExternalEvent event) {
		extEvents.add(event);
	}
	
	public void clearExternalEvents() {
		extEvents.clear();
	}

	public List<JzrPublisherEvent> getPublisherEvents() {
		return pubEvents;
	}
	
	public void addPublisherEvent(JzrPublisherEvent event) {
		pubEvents.add(event);
	}
	
	public void clearPublisherEvents() {
		pubEvents.clear();
	}

	public void setRestart(boolean restart) {
		this.restart = restart;
	}

	public boolean isRestart() {
		return restart;
	}

	public boolean hasStartingActions() {
		return this.hasStartingActions;
	}
	
	public boolean hasStartingStackGroupActions() {
		return this.hasStartingThreadStackGroupActions;
	}

	public void setHiatus(boolean hiatus) {
		this.prevHiatus = hiatus;
	}
	
	public boolean hasHiatusBefore() {
		return prevHiatus;
	}

	public void updateCPURunnable(CPURunnableContentionTypesManager cpuRunnableContentionTypesManager) {
		for (ThreadStack stack : getWorkingThreads()){
			boolean cpuRunnable = cpuRunnableContentionTypesManager.isCPURunnable(stack.getPrincipalContentionType());
			stack.setCPURunnable(cpuRunnable);
			if (stack.isCPURunnable())
				cpuRunnableThreadsCount++;
		}
	}
	
	public int getCPURunnableThreadsCount(){
		return this.cpuRunnableThreadsCount;
	}

	public void generateStackGroupActions(Map<ThreadStackHandler, ThreadStackGroupAction> openStackGroupActions, Map<Date, Set<ThreadStackGroupAction>> stackGroupActionHistory, StackGroupActionIdGenerator idGenerator) {
		Map<ThreadStackHandler,ThreadStackGroupAction> remainingStackGroupActions = new HashMap<>(openStackGroupActions);
		
		// for each working thread..
		List<ThreadStack> wts = this.getWorkingThreads();
		
		// ..get related stack group action
		for (ThreadStack stack : wts){
			ThreadStackHandler stackHashId = stack.getStackHandler();
			ThreadStackGroupAction tsga = openStackGroupActions.get(stackHashId);
			if (tsga == null || isRestart()){
				// not found or restart, create new one
				ThreadStackGroupAction threadStackGroupAction = new ThreadStackGroupAction(
						stack, 
						this.timestamp,
						idGenerator.getStackGroupActionId()
						);
				threadStackGroupAction.setMinStartDate(previousTimestamp);
				openStackGroupActions.put(stackHashId, threadStackGroupAction);
				if (isRestart() && remainingStackGroupActions.containsKey(stackHashId)){
					// close any previous stack group action with same thread stack
					closeThreadStackGroupAction(stackGroupActionHistory, remainingStackGroupActions, stackHashId);
					remainingStackGroupActions.remove(stackHashId);
				}
				hasStartingThreadStackGroupActions = true;
			}
			else{
				tsga.updateStackGroupAction(this.timestamp, stack);
				remainingStackGroupActions.remove(stackHashId);
			}
		}
		
		Set<ThreadStackHandler> remainingIds = remainingStackGroupActions.keySet();
		// process the closed stack group actions
		for (ThreadStackHandler rid : remainingIds){
			closeThreadStackGroupAction(stackGroupActionHistory, remainingStackGroupActions, rid);
			openStackGroupActions.remove(rid);
		}
	}
}
