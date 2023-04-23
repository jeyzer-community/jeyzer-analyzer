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



import static org.jeyzer.analyzer.util.SystemHelper.CR;





import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStackCPUInfo;
import org.jeyzer.analyzer.data.stack.ThreadStackJeyzerMXInfo;
import org.jeyzer.analyzer.data.stack.ThreadStackMemoryInfo;
import org.jeyzer.analyzer.data.stack.ThreadStack.STACK_CODE_STATE;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.util.AnalyzerHelper;

public class ThreadAction extends AbstractAction {
	
	private List<ThreadStack> stacks = new ArrayList<>();
	private String principalFunction = null;
	private String principalOperation = null;
	private String principalContentionType = null;
	
	private Set<String> distinctFunctions = null; // cached
	private Set<String> distinctOperations = null; // cached
	private Set<String> distinctContentionTypes = null; // cached
	private ThreadStackJeyzerMXInfo jeyzerMXInfo = null; // cached
	
	// cpu times
	private long cpuTime = 0;
	private double cpuUsageMax = -1;  // percentage
	private double cpuUsage = -1;     // percentage
	private double appCpuActivityUsageMax = -1; // percentage

	// memory figures
	private long allocatedMemory = -1;
	private double applicativeMemoryActivityUsageMax = -1; // percentage
	
	public ThreadAction(ThreadStack stack, int id){
		super(id);
		this.stacks.add(stack);
	}
	
	public void updateAction(ThreadStack stack){
		this.stacks.add(stack);
	}
	
	@Override
	public Date getStartDate(){
		return this.stacks.get(0).getTimeStamp();
	}
	
	@Override
	public Date getEndDate(){
		return this.stacks.get(this.stacks.size()-1).getTimeStamp();
	}
	
	@Override
	public long getCpuTime() {
		return this.cpuTime;
	}

	@Override
	public double getCpuUsageMax() {
		return this.cpuUsageMax;
	}

	@Override
	public double getCpuUsage() {
		return this.cpuUsage;
	}

	@Override
	public double getApplicativeCpuActivityUsageMax() {
		return this.appCpuActivityUsageMax;
	}

	@Override
	public long getAllocatedMemory() {
		return allocatedMemory;
	}

	@Override
	public double getApplicativeMemoryActivityUsageMax() {
		return applicativeMemoryActivityUsageMax;
	}
	
	@Override
	public String getExecutor(){
		return this.stacks.get(0).getExecutor();
	}	
	
	@Override
	public String getThreadId(){
		return this.stacks.get(0).getID();
	}

	@Override
	public String getName(){
		return this.stacks.get(0).getName();
	}
	
	@Override
	public int getPriority(){
		return this.stacks.get(0).getPriority();
	}
	
	@Override
	public ThreadStackJeyzerMXInfo getThreadStackJeyzerMXInfo(){
		if (jeyzerMXInfo!= null)
			return jeyzerMXInfo;
		
		// return the first available 
		for (ThreadStack stack : this.stacks){
			if (stack.getThreadStackJeyzerMXInfo()!= null){
				jeyzerMXInfo = stack.getThreadStackJeyzerMXInfo(); 
				return jeyzerMXInfo; 
			}
		}
		
		return jeyzerMXInfo;
	}
	
	@Override
	public int size(){
		return this.stacks.size();
	}
	
	@Override
	public Set<String> getDistinctFunctionTags(){
		if (distinctFunctions != null)
			return distinctFunctions;
		
		distinctFunctions = new HashSet<>();
		for (ThreadStack stack : this.stacks){
			List<String> functions = stack.getFunctionTags();
			for (String function : functions)
				if (!distinctFunctions.contains(function))
					distinctFunctions.add(function);
		}
		
		return distinctFunctions;
	}

	@Override
	public Set<String> getDistinctOperationTags(){
		if (distinctOperations != null)
			return distinctOperations;
		
		distinctOperations = new HashSet<>();
		for (ThreadStack stack : this.stacks){
			List<String> operations = stack.getOperationTags();
			for (String operation : operations)
				if (!distinctOperations.contains(operation))
					distinctOperations.add(operation);
		}
		
		return distinctOperations;
	}
	
	@Override
	public String getOperationTags(int pos){
		StringBuilder b = new StringBuilder(30);
		boolean start = true;
		
		ThreadStack stack = this.stacks.get(pos);
		List<String> tags = stack.getOperationTags();
		for (String tag : tags){
			b.append(start? "" : " - ");
			b.append(tag);
			start = false;
		}
		
		if (tags.isEmpty())
			b.append(ThreadStack.OPER_TO_BE_IDENTIFIED);
		
		return b.toString();
	}
	
	@Override
	public Set<String> getDistinctContentionTypeTags(){
		if (distinctContentionTypes != null)
			return distinctContentionTypes;
		
		distinctContentionTypes = new HashSet<>();
		for (ThreadStack stack : this.stacks){
			List<String> distinctTypes = stack.getContentionTypeTags();
			for (String distinctType : distinctTypes)
				if (!distinctContentionTypes.contains(distinctType))
					distinctContentionTypes.add(distinctType);
		}
		
		return distinctContentionTypes;
	}
	
	@Override
	public String getContentionTypeTags(int pos){
		StringBuilder b = new StringBuilder(30);
		boolean start = true;
		
		ThreadStack stack = this.stacks.get(pos);
		List<String> tags = stack.getContentionTypeTags();
		for (String tag : tags){
			b.append(start? "" : " - ");
			b.append(tag);
			start = false;
		}
		
		if (tags.isEmpty())
			b.append(stack.getDefaultContentionType());
		
		return b.toString();
	}
	
	@Override
	public ThreadStack getThreadStack(int pos){
		return this.stacks.get(pos);
	}

	@Override
	public String getPrincipalContentionType(int pos){
		return this.stacks.get(pos).getPrincipalContentionType();
	}
	
	@Override
	public String getPrincipalOperation(int pos){
		return this.stacks.get(pos).getPrincipalOperation();
	}
	
	@Override
	public String getPrincipalFunction(int pos){
		return this.stacks.get(pos).getPrincipalTag();
	}
	
	@Override
	public String getPrincipalCompositeFunction(){
		if (principalFunction != null)
			return principalFunction;
	    
	    principalFunction = AnalyzerHelper.getPrincipalCompositeFunction(this.stacks);
	    
		return principalFunction;
	}

	@Override
	public String getPrincipalCompositeOperation(){
		if (principalOperation != null)
			return principalOperation;
		
		principalOperation = AnalyzerHelper.getPrincipalCompositeOperation(this.stacks);
	
		return principalOperation;
	}
	
	@Override
	public String getPrincipalCompositeContentionType(){
		if (principalContentionType != null)
			return principalContentionType;
		
		principalContentionType = AnalyzerHelper.getPrincipalCompositeContentionType(this.stacks);
		
		return principalContentionType;
	}
	
	@Override
	public String getFunctionTags(int pos){
		StringBuilder b = new StringBuilder(30);
		boolean start = true;
		
		ThreadStack stack = this.stacks.get(pos);
		List<String> tags = stack.getFunctionTags();
		for (String tag : tags){
			b.append(start? "" : " - ");
			b.append(tag);
			start = false;
		}
		
		if (tags.isEmpty())
			b.append(ThreadStack.FUNC_TO_BE_IDENTIFIED);
		
		return b.toString();
	}
	
	public void flagFrozenStacks(){
		ThreadStack prev = null;
		int i = 1;
		int size = stacks.size();
		
		for (ThreadStack stack : stacks){
			if (prev == null){
				// first stack
				stack.setStackCodeState(STACK_CODE_STATE.RUNNING);
				prev = stack;
				i++;
				continue;
			}
			
			STACK_CODE_STATE prevCodeState = prev.getStackCodeState();
			
			if (stack.getStackHandler().equals(prev.getStackHandler())){
				if (STACK_CODE_STATE.RUNNING.equals(prevCodeState)){
					// begin freeze section
					prev.setStackCodeState(STACK_CODE_STATE.FREEZE_BEGIN);
				}
				if (i == size){
					// last element of the action, end reached : close the freeze section
					stack.setStackCodeState(STACK_CODE_STATE.FREEZE_END);
				}
				else{
					// middle element
					stack.setStackCodeState(STACK_CODE_STATE.FREEZE_MIDDLE);
				}
			}
			else {
				if (STACK_CODE_STATE.FREEZE_MIDDLE.equals(prevCodeState) || STACK_CODE_STATE.FREEZE_BEGIN.equals(prevCodeState)){
					// close previous freeze section
					prev.setStackCodeState(STACK_CODE_STATE.FREEZE_END);
				}
				
				// running stack
				stack.setStackCodeState(STACK_CODE_STATE.RUNNING);
			}
			
			prev = stack;
			i++;
		}
	}
	
	@Override
	public String toString(){
		StringBuilder b = new StringBuilder(2000);
		
		b.append("Action " + CR);
		b.append("================================================= " + CR);
		b.append(" - Start : " + this.getStartDate() + CR);
		b.append(" - End   : " + this.getEndDate() + CR);
		
		for (ThreadStack stack : stacks){
			b.append(stack);
		}
		
		return b.toString();
	}

	public void updateCPUFigures(ThreadStack postActionStack, Date prevTimeStamp) {
		double stackAppActivityUsage = 0;
		double stackCpuUsage = 0;
		long endDate = this.getEndDate().getTime();
		
		for (ThreadStack stack : stacks){
			ThreadStackCPUInfo stackCPUInfo = stack.getCpuInfo();
			this.cpuTime += stackCPUInfo.getCpuTime();
			stackCpuUsage = stackCPUInfo.getCpuUsage();
			if (stackCpuUsage > this.cpuUsageMax)
				this.cpuUsageMax = stackCpuUsage;
			stackAppActivityUsage = stackCPUInfo.getApplicativeActivityUsage();
			if (stackAppActivityUsage > this.appCpuActivityUsageMax)
				this.appCpuActivityUsageMax = stackAppActivityUsage;
		}
		
		// consider post action stack to add its remaining CPU
		if (postActionStack != null){
			ThreadStackCPUInfo stackCPUInfo = postActionStack.getCpuInfo();
			this.cpuTime += stackCPUInfo.getCpuTime();
			stackCpuUsage = stackCPUInfo.getCpuUsage();
			if (stackCpuUsage > this.cpuUsageMax)
				this.cpuUsageMax = stackCpuUsage;
			stackAppActivityUsage = stackCPUInfo.getApplicativeActivityUsage();
			if (stackAppActivityUsage > this.appCpuActivityUsageMax)
				this.appCpuActivityUsageMax = stackAppActivityUsage;
			endDate = postActionStack.getTimeStamp().getTime();
		}

		// take into account the timestamp of the pre action thread dump 
		// if action is starting on first thread dump, prevTimeStamp = null
		long startDate = (prevTimeStamp != null) ? prevTimeStamp.getTime() : this.getStartDate().getTime();   

		long time = (endDate - startDate) * 1000000L; // convert to nanosec
		this.cpuUsage = FormulaHelper.percent(this.cpuTime, time);
	}

	public void updateMemoryFigures(ThreadStack postActionStack) {
		double appActivityUsage = 0;  // percentage
		
		for (ThreadStack stack : stacks){
			ThreadStackMemoryInfo stackMemoryInfo = stack.getMemoryInfo();
			if (stackMemoryInfo.getAllocatedMemory() != -1){
				if (this.allocatedMemory == -1)
					this.allocatedMemory = 0; // reset to normal value
				this.allocatedMemory += stackMemoryInfo.getAllocatedMemory();
			}
			appActivityUsage = stackMemoryInfo.getApplicativeActivityUsage();
			if (appActivityUsage > this.applicativeMemoryActivityUsageMax)
				this.applicativeMemoryActivityUsageMax = appActivityUsage;
		}

		// consider post action stack to add its remaining memory
		if (postActionStack != null){
			ThreadStackMemoryInfo stackMemoryInfo = postActionStack.getMemoryInfo();
			if (stackMemoryInfo.getAllocatedMemory() != -1){
				if (this.allocatedMemory == -1)
					this.allocatedMemory = 0; // reset to normal value
				this.allocatedMemory += stackMemoryInfo.getAllocatedMemory();
			}
			appActivityUsage = stackMemoryInfo.getApplicativeActivityUsage();
			if (appActivityUsage > this.applicativeMemoryActivityUsageMax)
				this.applicativeMemoryActivityUsageMax = appActivityUsage;
		}
	}
	
	public static class ThreadActionComparator implements Comparator<ThreadAction> {

	    @Override
	    public int compare(ThreadAction action1, ThreadAction action2) {
	        if (action2.getId() < action1.getId()) {
	            return 1;
	        } else {
	            return -1; // return 0 is not expected as Action Ids must be unique
	        }
	    }
	}
}
