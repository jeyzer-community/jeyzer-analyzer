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
import java.util.TreeMap;

import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStackJeyzerMXInfo;

public class ThreadStackGroupAction extends AbstractAction {
	
	private TreeMap<Date,List<ThreadStack>> stacksPerTimeStamp = new TreeMap<>();
	
	private List<List<ThreadStack>> stacksGroups = null; // cached
	
	public ThreadStackGroupAction(ThreadStack stack, Date timestamp, int id){
		super(id, stack.isVirtual());
		List<ThreadStack> stacks = new ArrayList<>();
		stacks.add(stack);
		this.stacksPerTimeStamp.put(timestamp, stacks);
	}
	
	@Override
	public String getName() {
		return this.stacksPerTimeStamp.firstEntry().getValue().get(0).getName();
	}
	
	@Override
	public Date getStartDate(){
		return this.stacksPerTimeStamp.firstKey();
	}
	
	@Override
	public Date getEndDate(){
		return this.stacksPerTimeStamp.lastKey();
	}
		
	@Override
	public String getExecutor(){
		return this.stacksPerTimeStamp.firstEntry().getValue().get(0).getExecutor();
	}
	
	@Override
	public int size(){
		return this.stacksPerTimeStamp.size();
	}
	
	@Override
	public int getStackSize(){
		int stackSize = 0;
		for (List<ThreadStack> stacks : this.stacksPerTimeStamp.values()) {
			// Should be : stacks.size() for ThreadStack
			//             stacks.get(0).getInstancecount() for VirtualThreadStack 
			for (ThreadStack stack : stacks)
				stackSize += stack.getInstanceCount();
		}
		return stackSize;
	}
	
	@Override
	public Set<String> getDistinctFunctionTags(){
		Set<String> distinctFunctions = new HashSet<>();
		distinctFunctions.addAll(this.stacksPerTimeStamp.firstEntry().getValue().get(0).getFunctionTags());
		return distinctFunctions;
	}

	@Override
	public Set<String> getDistinctOperationTags(){
		Set<String> distinctOperations = new HashSet<>();
		distinctOperations.addAll(this.stacksPerTimeStamp.firstEntry().getValue().get(0).getOperationTags());
		return distinctOperations;
	}
	
	@Override
	public String getOperationTags(int pos){
		StringBuilder b = new StringBuilder(30);
		boolean start = true;
		
		List<String> tags = this.stacksPerTimeStamp.firstEntry().getValue().get(0).getOperationTags();
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
		Set<String> contentionTypes = new HashSet<>();
		contentionTypes.addAll(this.stacksPerTimeStamp.firstEntry().getValue().get(0).getContentionTypeTags());
		return contentionTypes;
	}
	
	@Override
	public String getContentionTypeTags(int pos){
		StringBuilder b = new StringBuilder(30);
		boolean start = true;
		
		ThreadStack stack = this.stacksPerTimeStamp.firstEntry().getValue().get(0);
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
		return this.stacksPerTimeStamp.firstEntry().getValue().get(0);
	}

	@Override
	public String getPrincipalContentionType(int pos){
		return this.stacksPerTimeStamp.firstEntry().getValue().get(0).getPrincipalContentionType();
	}
	
	@Override
	public String getPrincipalOperation(int pos){
		return this.stacksPerTimeStamp.firstEntry().getValue().get(0).getPrincipalOperation();
	}
	
	@Override
	public String getPrincipalFunction(int pos){
		return this.stacksPerTimeStamp.firstEntry().getValue().get(0).getPrincipalTag();
	}
	
	@Override
	public String getPrincipalCompositeFunction(){
		return this.stacksPerTimeStamp.firstEntry().getValue().get(0).getPrincipalTag();
	}

	@Override
	public String getPrincipalCompositeOperation(){
		return this.stacksPerTimeStamp.firstEntry().getValue().get(0).getPrincipalOperation();
	}
	
	@Override
	public String getPrincipalCompositeContentionType(){
		return this.stacksPerTimeStamp.firstEntry().getValue().get(0).getPrincipalContentionType();
	}
	
	@Override
	public String getFunctionTags(int pos){
		StringBuilder b = new StringBuilder(30);
		boolean start = true;
		
		List<String> tags = this.stacksPerTimeStamp.firstEntry().getValue().get(0).getFunctionTags();
		for (String tag : tags){
			b.append(start? "" : " - ");
			b.append(tag);
			start = false;
		}
		
		if (tags.isEmpty())
			b.append(ThreadStack.FUNC_TO_BE_IDENTIFIED);
		
		return b.toString();
	}
	
	@Override
	public String toString(){
		StringBuilder b = new StringBuilder(2000);
		
		b.append("Stack group action " + CR);
		b.append("================================================= " + CR);
		b.append(" - Start : " + this.getStartDate() + CR);
		b.append(" - End   : " + this.getEndDate() + CR);
		b.append(this.stacksPerTimeStamp.firstEntry().getValue().get(0));
		
		return b.toString();
	}
	
	public static class ThreadStackGroupActionComparator implements Comparator<ThreadStackGroupAction> {

	    @Override
	    public int compare(ThreadStackGroupAction action1, ThreadStackGroupAction action2) {
	        if (action2.getId() < action1.getId()) {
	            return 1;
	        } else {
	            return -1; // return 0 is not expected as Action Ids must be unique
	        }
	    }
	}

	@Override
	public long getCpuTime() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getCpuUsageMax() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getCpuUsage() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getApplicativeCpuActivityUsageMax() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getAllocatedMemory() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getApplicativeMemoryActivityUsageMax() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getThreadId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getPriority() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ThreadStackJeyzerMXInfo getThreadStackJeyzerMXInfo() {
		throw new UnsupportedOperationException();
	}
	
	public int getGroupSize(int pos) {
		if (stacksGroups == null)
			this.stacksGroups = new ArrayList<>(this.stacksPerTimeStamp.values());
		
		// Should be : stacksGroups.get(pos).size() for ThreadStack
		//             sum of stacksGroups.get(pos).getInstancecount() for VirtualThreadStack 
		int stackGroupSize = 0;
		for (ThreadStack stack : stacksGroups.get(pos))
			stackGroupSize += stack.getInstanceCount();
		return stackGroupSize;
	}
	
	public void updateStackGroupAction(Date timestamp, ThreadStack stack){
		List<ThreadStack> stacks = this.stacksPerTimeStamp.computeIfAbsent(timestamp, x-> new ArrayList<>());
		stacks.add(stack);
	}
}
