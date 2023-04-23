package org.jeyzer.analyzer.data.action;

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







import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStackCPUInfo;
import org.jeyzer.analyzer.data.stack.ThreadStackMemoryInfo;
import org.jeyzer.analyzer.data.stack.ThreadState;
import org.jeyzer.analyzer.data.tag.ContentionTypeTag;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.math.FormulaHelper;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class ActionGraphSection {

	private static final ActionGraphSectionComparator.StackCountComparable comparator = new ActionGraphSectionComparator.StackCountComparable(); 
	
	private static final int NO_MATCHING_LINE = -1;
	private static final int FULL_MATCH_AND_EXTRA_LINES = -2;
	private static final int EXACT_MATCH_LINES = -3;
	private static final int MATCH_BUT_LESS_LINES = -4;
	
	protected int start; // inclusive
	protected int end;   // exclusive
	protected List<ActionGraphSection> children; // Instantiate only if required
	protected List<ThreadStack> stacks; // instantiated in constructor
	
	protected Multiset<ThreadState> states; // instantiated in constructor
	
	private int cpuUsageAverage = -2; // not initialized
	private int allocatedMemoryAverage = -2; // not initialized
	
	private boolean topChildCached = false;
	private ActionGraphSection topChild = null;
	
	private boolean statesIncluded;
	
	// cached for performance
	private int maxDepth = -1;
	
	public ActionGraphSection(ThreadStack stack, int start, boolean statesIncluded) {
		this.stacks = new ArrayList<ThreadStack>();
		this.statesIncluded = statesIncluded;
		if (statesIncluded){
			this.states  = HashMultiset.create();
			this.states.add(stack.getState());
		}
		setStart(start);
		setEnd(stack.getStackHandler().getCodeLines().size());
		stacks.add(stack);
	}

	// called on split
	public ActionGraphSection(
			List<ActionGraphSection> children, // can be null
			List<ThreadStack> stacks,
			Multiset<ThreadState> states, // can be null
			int start, 
			int end,
			boolean statesIncluded) {
		this.children = children; // keep list reference, parent will nullify its own
		this.statesIncluded = statesIncluded;
		setStart(start);
		setEnd(end);
		this.stacks = new ArrayList<ThreadStack>(stacks); // list copy
		if (statesIncluded)
			this.states  = HashMultiset.create(states); // set copy
	}
	
	private void setEnd(int value){
		this.end = value;
	}
	
	private void setStart(int value){
		this.start = value;
	}	
	
	public int getStackCount(){
		return stacks.size();
	}
	
	public int getCpuUsageAverage(){
		if (cpuUsageAverage != -2)
			return cpuUsageAverage; // cached value
		
		int count = 0;
		long sum = 0;
		
		for (ThreadStack stack : stacks){
			ThreadStackCPUInfo cpuInfo = stack.getCpuInfo();
			if (cpuInfo == null){
				continue; // ignore
			}
			count++;
			sum += cpuInfo.getCpuUsage();
		}
		
		if (count == 0)
			cpuUsageAverage = -1; // NA
		else
			cpuUsageAverage = (int) (sum / count);
		
		return cpuUsageAverage;
	}
	
	public double getCpuUsageStdDeviation(){
		List<Integer> values = new ArrayList<>(this.getStackCount());
		
		for (ThreadStack stack : stacks){
			ThreadStackCPUInfo cpuInfo = stack.getCpuInfo();
			if (cpuInfo != null){
				values.add((int)Math.round(cpuInfo.getCpuUsage()));
			}
		}
		
		if (values.isEmpty())
			return -1; // NA
		
		return FormulaHelper.calculateStandardDeviation(values, getCpuUsageAverage());
	}
	
	public long getCpuTime() {
		long sum = 0;
		
		for (ThreadStack stack : stacks){
			ThreadStackCPUInfo cpuInfo = stack.getCpuInfo();
			if (cpuInfo == null){
				continue; // ignore
			}
			sum += cpuInfo.getCpuTime();
		}
		
		return sum;
	}
	
	public long getAllocatedMemoryAverage(){
		if (allocatedMemoryAverage != -2)
			return allocatedMemoryAverage;
		
		int count = 0;
		long sum = 0;
		
		for (ThreadStack stack : stacks){
			ThreadStackMemoryInfo memoryInfo = stack.getMemoryInfo();
			if (memoryInfo == null){
				continue; // ignore
			}
			count++;
			sum += memoryInfo.getAllocatedMemory();
		}
		
		if (count == 0)
			allocatedMemoryAverage = -1; // NA
		else
			allocatedMemoryAverage = (int) (sum / count); 
		
		return allocatedMemoryAverage;
	}

	public double getAllocatedMemoryStdDeviation(){
		List<Long> values = new ArrayList<>(this.getStackCount());
		
		for (ThreadStack stack : stacks){
			ThreadStackMemoryInfo memoryInfo = stack.getMemoryInfo();
			if (memoryInfo != null){
				values.add(memoryInfo.getAllocatedMemory());
			}
		}
		
		if (values.isEmpty())
			return -1; // NA
		
		return FormulaHelper.calculateStandardDeviation(values, getAllocatedMemoryAverage());
	}	
	
	public int getMaxDepth(){
		if (maxDepth != -1)
			return maxDepth;
		
		int result = 0;
		if (this.hasChildren()){
			for (ActionGraphSection section : children){
				if (section.getMaxDepth()>result)
					result = section.getMaxDepth();
			}
		}
		
		maxDepth = result + 1; // count itself
		
		return maxDepth;
	}

	public boolean acceptNewStack(ThreadStack stack){
		
		int result = match(stack);
		
		switch(result){

			case NO_MATCHING_LINE:	// no common point : not accepted
				// ==========================================
				// Stacks to compare :
				//       Section	Stack to integrate
				//   		| S1			*
				//   		|				*
				//   		|				*
				//   		|				*
				//   						*
				//
				// Section result :  S1 unchanged
				// ==========================================
				return false;
		
			case FULL_MATCH_AND_EXTRA_LINES: // Ok, part of this section. Extra lines be accepted now by one child section
				// ==========================================
				// Stacks to compare :
				//       Section	Stack to integrate
				//   		| S1			|
				//   		|				|
				//   		|				|
				//   		|               |
				//                          *
				//                          *
				//
				// Section result :
				//   			|  S1		
				//   			|
				//   			|
				//   			|
				//               \   Child S2 (*) : new section or existing one
				//                \
				// ==========================================
 				stacks.add(stack);
				
 				if (childAcceptStack(stack))
 					return true;

				// section not found : need to create new section
				createSection(stack, this.end);
				return true;

			case MATCH_BUT_LESS_LINES : // Both stacks are containing the same code but one is shorter. Split the current section in 2 sections 
				// ==========================================
				// Stacks to compare :
				//       Section	Stack to integrate
				//   		| S1			|
				//   		|				|
				//   		|				
				//   		|		
				//
				// Section result :
				//   			|  S1
				//   			|
				//             /    
				//       S1'  /     
				// ==========================================
				int stackEnd = stack.getStackHandler().getCodeLines().size();
				splitSection(stackEnd-1); // last line to match, must be adjusted with -1
				stacks.add(stack);
				
				if (this.statesIncluded)
					states.add(stack.getState());
				
				return true;

				
			case EXACT_MATCH_LINES:
				// ==========================================
				// Stacks to compare :
				//       Section	Stack to integrate
				//   		| S1			|
				//   		|				|
				//   		|				|
				//   		|				|
				// ==========================================
				
				stacks.add(stack);
				
				if (this.statesIncluded)
					states.add(stack.getState());
				
				return true;
				
			default:
				// partial match, must split the current section and fork a new section for the code which is different
				// ==========================================
				// Stacks to compare :
				//       Section	Stack to integrate
				//   		| S1			|
				//   		|				|
				//   		|				*
				//   		|               
				//                          
				//                          
				//
				// Section result :
				//   			|  S1
				//   			|
				//             / \   S2 (*)
				//       S1'  /        
				// ==========================================
				splitAndForkSection(stack, result);
				stacks.add(stack);
				
				return true;
		}
	}

	private boolean childAcceptStack(ThreadStack stack) {
		if (children != null){
			for (ActionGraphSection section : children){
				if (section.acceptNewStack(stack)){
					// child section has accepted it. Perfect.
					return true;
				}
			}
		}
		return false;
	}

	private void splitSection(int lastLineToMatch) {
		// create ending section from existing one (split)
		ActionGraphSection section = new ActionGraphSection(
				this.children,				
				this.stacks, 
				this.states,
				lastLineToMatch+1,  // will be the start point 
				this.end,
				this.statesIncluded
				);
		
		// reset child list and add to it the split result
		this.children = null;
		
		// reset states list
		if (this.statesIncluded)
			this.states.clear();
		
		addChildSection(section);
		
		// adjust current section
		this.setEnd(lastLineToMatch+1);
	}

	protected void createSection(ThreadStack stack, int end) {
		ActionGraphSection section = new ActionGraphSection(stack, end, statesIncluded);  
		addChildSection(section);
	}

	protected void splitAndForkSection(ThreadStack stack, int lastLineToMatch) {
		splitSection(lastLineToMatch);
		
		// create new section (fork)
		createSection(stack, lastLineToMatch+1);
	}

	private void addChildSection(ActionGraphSection section) {
		if (children ==  null) // Instantiate only if required
			children = new ArrayList<ActionGraphSection>(5);
		
		children.add(section);
	}
	
	private int match(ThreadStack stack) {
		List<String> stackLines = stack.getStackHandler().getReversedCodeLines().subList(start, stack.getStackHandler().getCodeLines().size());
		List<String> sectionLines = this.stacks.get(0).getStackHandler().getReversedCodeLines().subList(start, end);

		// check the first line
		String firstLine = sectionLines.get(0);
		String firstLinetoMatch = stackLines.get(0);
		if (!firstLine.equals(firstLinetoMatch))
			return NO_MATCHING_LINE;

		for (int pos = 1; pos<sectionLines.size(); pos++){
			String line = sectionLines.get(pos);
			if (pos == stackLines.size())
				return MATCH_BUT_LESS_LINES; 
			String linetoMatch = stackLines.get(pos);
			if (!line.equals(linetoMatch))
				return start + pos-1; // last line that matches
		}

		if (stackLines.size() == sectionLines.size())
			return EXACT_MATCH_LINES;
			
		// all lines matched
		return FULL_MATCH_AND_EXTRA_LINES;
	}

	public List<String> getCodeLines() {
		int size = stacks.get(0).getStackHandler().getCodeLines().size();
		return this.stacks.get(0).getStackHandler().getCodeLines().subList(size - end, size - start);
	}
	
	public int getSize(){
		return end - start; 
	}

	public boolean hasChildren() {
		return this.children != null;  
	}

	public Collection<ActionGraphSection> getChildren() {
		if (children != null)
			return children;
		else 
			return null;
	}
	
	public String getFunction() {
		// try to get first the main function from current section
		for (ThreadStack stack : this.stacks){
			String preferedFunction = findPreferedFunctionInSection(stack);
			if (preferedFunction != null)
				return preferedFunction;
		}

		// otherwise get it from the most representative child (most frequent child)
		ActionGraphSection child = getTopChild();
		if (child != null)
			return child.getFunction();
		
		return null; 
	}
	
	public List<String> getSourceFunctionTags() {
		int size = this.end - this.start;
		List<String> functionTags = new ArrayList<>(size);
		SortedMap<Integer, String> sourceLocalizedFunctionTags = this.stacks.get(0).getSourceLocalizedFunctionTagsInRange(this.start, this.end);
		int pos = size;
		for (int i = this.end-1; i>=this.start; i--){
			functionTags.add(size-pos, sourceLocalizedFunctionTags.get(i));
			pos--;
		}
		return functionTags;
	}
	
	public boolean isSourceFunctionTagsEmpty() {
		return this.stacks.get(0).getSourceLocalizedFunctionTagsInRange(this.start, this.end).isEmpty();
	}	
	
	public SortedMap<Integer, String> getSourceLocalizedFunctionTags(){
		return this.stacks.get(0).getSourceLocalizedFunctionTagsInRange(this.start, this.end);
	}
	
	private String findPreferedFunctionInSection(ThreadStack stack) {
		List<String> preferredFunctions = stack.getFunctionTags(); // first is preferred
		Collection<String> functions = stack.getSourceLocalizedFunctionTagsInRange(this.start, this.end).values();
		
		if (functions.isEmpty())
			return null;
		
		for (String preferredFunction : preferredFunctions){
			for (String function : functions){
				if (preferredFunction.equals(function)){
					return function;
				}
			}
		}
		return null;
	}

	public Set<String> getAllFunctions() {
		Set<String> functions = new HashSet<>();

		// try to get first all the functions from current section
		for (ThreadStack stack : this.stacks){
			for (String function : stack.getSourceLocalizedFunctionTagsInRange(this.start, this.end).values()){
				if (!functions.contains(function)){
					functions.add(function);
				}
			}
		}
		
		// and return it
		if (!functions.isEmpty())
			return functions;

		// otherwise get it from the most representative child (most frequent child)
		ActionGraphSection child = getTopChild();
		if (child != null)
			return child.getAllFunctions();
		
		return functions; // empty one	
	}	
	
	public String getOperation() {
		// try to get first the main operation from current section
		for (ThreadStack stack : this.stacks){
			String preferedOperation = findPreferedOperationInSection(stack);
			if (preferedOperation != null)
				return preferedOperation;
		}

		// otherwise get it from the most representative child (most frequent child)
		ActionGraphSection child = getTopChild();
		if (child != null)
			return child.getOperation();
		
		return null; 
	}
	
	public List<String> getSourceOperationTags() {
		int size = this.end - this.start;
		List<String> operationTags = new ArrayList<>(size);
		SortedMap<Integer, String> sourceLocalizedOperationTags = this.stacks.get(0).getSourceLocalizedOperationTagsInRange(this.start, this.end);
		int pos = size;
		for (int i = this.end-1; i>=this.start; i--){
			operationTags.add(size-pos, sourceLocalizedOperationTags.get(i));
			pos--;
		}
		return operationTags;
	}
	
	public boolean isSourceOperationTagsEmpty() {
		return this.stacks.get(0).getSourceLocalizedOperationTagsInRange(this.start, this.end).isEmpty();
	}
	
	public SortedMap<Integer, String> getSourceLocalizedOperationTags(){
		return this.stacks.get(0).getSourceLocalizedOperationTagsInRange(this.start, this.end);
	}
	
	public int getThreadStateCount(ThreadState state) {
		return this.states.count(state);
	}
	
	private String findPreferedOperationInSection(ThreadStack stack) {
		List<String> preferredOperations = stack.getOperationTags(); // first is preferred
		Collection<String> operations = stack.getSourceLocalizedOperationTagsInRange(this.start, this.end).values();
		
		if (operations.isEmpty())
			return null;
		
		for (String preferredOperation : preferredOperations){
			for (String operation : operations){
				if (preferredOperation.equals(operation)){
					return operation;
				}
			}
		}
		return null;
	}

	public Set<String> getAllOperations() {
		Set<String> operations = new HashSet<>();

		// try to get first all the operations from current section
		for (ThreadStack stack : this.stacks){
			for (String operation : stack.getSourceLocalizedOperationTagsInRange(this.start, this.end).values()){
				if (!operations.contains(operation)){
					operations.add(operation);
				}
			}
		}
		
		// and return it
		if (!operations.isEmpty())
			return operations;

		// otherwise get it from the most representative child (most frequent child)
		ActionGraphSection child = getTopChild();
		if (child != null)
			return child.getAllOperations();
		
		return operations; // empty one
	}

	public Set<String> getTreeOperations() {
		Set<String> operations = new HashSet<>();

		for (ThreadStack stack : this.stacks){
			for (String operation : stack.getSourceLocalizedOperationTagsInRange(this.start, this.end).values()){
				if (!operations.contains(operation)){
					operations.add(operation);
				}
			}
		}
		
		if (this.hasChildren()){
			for (ActionGraphSection child : children){
				operations.addAll(child.getTreeOperations());
			}
		}
		
		return operations;
	}
	
	public Set<String> getTreeFunctions() {
		Set<String> functions = new HashSet<>();

		for (ThreadStack stack : this.stacks){
			for (String operation : stack.getSourceLocalizedFunctionTagsInRange(this.start, this.end).values()){
				if (!functions.contains(operation)){
					functions.add(operation);
				}
			}
		}
		
		if (this.hasChildren()){
			for (ActionGraphSection child : children){
				functions.addAll(child.getTreeFunctions());
			}
		}
		
		return functions;
	}
	
	public String getContentionType() {
		// try to get first the main contention type from current section
		for (ThreadStack stack : this.stacks){
			String preferedContentionType = findPreferedContentionTypeInSection(stack);
			if (preferedContentionType != null)
				return preferedContentionType;
		}

		// otherwise get it from the most representative child (most frequent child)
		ActionGraphSection child = getTopChild();
		if (child != null)
			return child.getContentionType();
		
		return null;
	}
	
	public Multiset<Tag> getPrincipalContentionTypes() {
		Multiset<Tag> principalContentionTypeTags = HashMultiset.create();
		
		for (ThreadStack stack : this.stacks){
			principalContentionTypeTags.add(
					new ContentionTypeTag(stack.getPrincipalContentionType())
					);
		}
		
		return principalContentionTypeTags;
	}
	
	private String findPreferedContentionTypeInSection(ThreadStack stack) {
		List<String> preferredContentionTypes = stack.getContentionTypeTags(); // first is preferred
		Collection<String> contentionTypes = stack.getSourceLocalizedContentionTypeTagsInRange(this.start, this.end).values();
		
		if (contentionTypes.isEmpty())
			return null;
		
		for (String preferredContentionType : preferredContentionTypes){
			for (String contentionType : contentionTypes){
				if (preferredContentionType.equals(contentionType)){
					return contentionType;
				}
			}
		}
		return null;
	}
	
	public List<String> getSourceContentionTypeTags() {
		int size = this.end - this.start;
		List<String> contentionTypeTags = new ArrayList<>(size);
		SortedMap<Integer, String> sourceLocalizedContentionTypeTags = this.stacks.get(0).getSourceLocalizedContentionTypeTagsInRange(this.start, this.end);
		int pos = size;
		for (int i = this.end-1; i>=this.start; i--){
			contentionTypeTags.add(size-pos, sourceLocalizedContentionTypeTags.get(i));
			pos--;
		}
		return contentionTypeTags;
	}
	
	public Set<String> getAllContentionTypes() {
		Set<String> contentionTypes = new HashSet<>();

		// try to get first all the contention types from current section
		for (ThreadStack stack : this.stacks){
			for (String contentionType : stack.getSourceLocalizedContentionTypeTagsInRange(this.start, this.end).values()){
				if (!contentionTypes.contains(contentionType)){
					contentionTypes.add(contentionType);
				}
			}
		}
		
		// and return it
		if (!contentionTypes.isEmpty())
			return contentionTypes;

		// otherwise get it from the most representative child (most frequent child)
		ActionGraphSection child = getTopChild();
		if (child != null)
			return child.getAllContentionTypes();
		
		return contentionTypes; // empty one
	}
	
	public String getExecutor() {
		return ""; // return empty string
	}
	
	public void sortChildren() {
		if (this.hasChildren()){
			// sort by stack count
			this.children.sort(comparator);
			for (ActionGraphSection child : children){
				child.sortChildren();
			}
		}
	}	
	
	private ActionGraphSection getTopChild(){
		if (topChildCached)
			return this.topChild;
		
		if (hasChildren()){
			int highRank = 0;
			List<ActionGraphSection> sectionCandidates = new ArrayList<>();
			
			for (ActionGraphSection child : children){
				// take the one which has highest rank
				if (child.getStackCount() > highRank){
					highRank = child.getStackCount();
					sectionCandidates.clear();
					sectionCandidates.add(child);
				}
				else if (child.getStackCount() == highRank){
					sectionCandidates.add(child); // other candidate
				}
			}
			
			// must be highest alone, otherwise ignore
			if (sectionCandidates.size() == 1)
				topChild = sectionCandidates.get(0);
		}

		topChildCached = true;
		return topChild; // can be null;
	}

}
