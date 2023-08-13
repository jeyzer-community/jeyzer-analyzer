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







import java.util.Date;
import java.util.List;
import java.util.SortedMap;

import org.jeyzer.analyzer.rule.Rule;


public interface ThreadStack {

	public static interface ThreadStackHandler{
		public List<String> getCodeLines(); 		 // natural read order (stack top read  last)
		public List<String> getReversedCodeLines();  // technical access order (stack top read first)
		public StackText getText();
		public StackText getJzrFilteredText();
		public ThreadStack getThreadStack();
	}

	// Thread daemon
	public enum DAEMON { TRUE, FALSE, NOT_SET }

	// Stack freeze
	public enum STACK_CODE_STATE { UNKNOWN, RUNNING, FREEZE_BEGIN, FREEZE_MIDDLE, FREEZE_END }
	
	// Thread priority not available
	public static final int PRIORITY_NOT_AVAILABLE = -1;
	
	public static final String OPER_TO_BE_IDENTIFIED = "(OTBI)";
	public static final String FUNC_TO_BE_IDENTIFIED = "(ATBI)";
	public static final String EXECUTOR_TO_BE_IDENTIFIED = "(ETBI)";
	
	public static final String CONTENTION_TYPE_CODE_EXEC = "Code execution";
	public static final String CONTENTION_TYPE_CODE_LOCKED = "Code locked";
	
	public static final String PACKED_STACK = " Stack packed";
	
	public static final String VIRTUAL_THREAD_UNPARK_HEADER = "VirtualThread-unparker";
	
	public ThreadStackHandler getStackHandler();
	
	public String getHeader();
	
	public String getName();
	
	public String getFilePath();

	public String getID();
	
	public ThreadState getState();
	
	public int getInstanceCount();
	
	public boolean hasUniqueInstance();
	
	public boolean isSuspended();
	
	public DAEMON getDaemon();
	
	public int getPriority();
	
	public int getDepthLength();
	
	public int getLockCount();
	
	public String getLockName();
	
	public String getLockClassName();
	
	public ThreadStack getLockingThread();
	
	public Date getTimeStamp();

	public String getPrincipalOperation();
	
	public List<String> getOperationTags();
	
	public SortedMap<Integer, String> getSourceLocalizedOperationTagsInRange(int start, int end);
	
	public SortedMap<Integer, String> getSourceLocalizedOperationTags();
	
	public List<String> getFunctionTags();
	
	public String getPrincipalTag();
	
	public SortedMap<Integer, String> getSourceLocalizedFunctionTagsInRange(int start, int end);
	
	public SortedMap<Integer, String> getSourceLocalizedFunctionTags();
	
	public String getPrincipalContentionType();
	
	public List<String> getContentionTypeTags();
	
	public String getDefaultContentionType();
	
	public SortedMap<Integer, String> getSourceLocalizedContentionTypeTagsInRange(int start, int end);
	
	public SortedMap<Integer, String> getSourceLocalizedContentionTypeTags();
	
	public List<ThreadStack> getLockedThreads();
	
	public String getExecutor();
	
	public boolean isOfInterest();
	
	public boolean isInDeadlock();
	
	public boolean isBlocked();

	public boolean isWaiting();
	
	public boolean isTimedWaiting();
	
	public boolean isRunning();
	
	public boolean isVirtual();
	
	public boolean isCarrying();
	
	public boolean isUFO();
	
	public boolean isATBI();
	
	public boolean isOTBI();
	
	public boolean isETBI();
	
	public void addFunctionTag(String value);
	
	public void addFunctionTagIndexed(int lineMatch, String name);

	public void addOperationTag(String value);
	
	public void addOperationTagIndexed(int lineMatch, String name);
	
	public boolean addContentionTypeTag(String value);
	
	public void addContentionTypeTagIndexed(int lineMatch, String name);
	
	public void pack();
	
	public void setInDeadlock(boolean value);
	
	public void setInterest(boolean interest);
	
	public void setExecutor(String executor);
	
	public List<String> getOwnedLocks();
	
	public boolean hasOwnedLocks();
	
	public List<String> getBiasedLocks();
	
	public boolean hasBiasedLocks();

	public void setLockCount(int synchroCount);
	
	public void setLockingThread(ThreadStack stack);
	
	public void addLockedThread(ThreadStack stack);
	
	public void setCodeLockName(String codeLockedName);
	
	public String getCodeLockName();
	
	public void setCodeLocked(boolean codeLocked);
	
	public boolean isCodeLocked();
	
	public boolean isLocked();
	
	public void applyRules(List<Rule> rules);
	
	public void setStackCodeState(STACK_CODE_STATE freezeState);
	
	public boolean isFrozenStackCode();
	
	public STACK_CODE_STATE getStackCodeState();
	
	public boolean isCPURunnable();
	
	public void setCPURunnable(boolean cpuRunnable);
	
	public ThreadStackCPUInfo getCpuInfo();
	
	public ThreadStackMemoryInfo getMemoryInfo();
	
	public ThreadStackJeyzerMXInfo getThreadStackJeyzerMXInfo();

}
