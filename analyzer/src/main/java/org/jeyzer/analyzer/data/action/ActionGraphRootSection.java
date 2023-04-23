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







import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.util.AnalyzerHelper;

public class ActionGraphRootSection extends ActionGraphSection {
	
	private ThreadAction action = null; // representative action in case root section is used for global action usage 
	private int actionCount = 1;
	
	public ActionGraphRootSection(ThreadStack stack, ThreadAction action, boolean statesIncluded) {
		super(stack, 0, statesIncluded);
		this.action = action;
	}
	
	@Override
	public boolean acceptNewStack(ThreadStack stack){
		List<String> stackLines = stack.getStackHandler().getReversedCodeLines().subList(start, stack.getStackHandler().getCodeLines().size());
		List<String> sectionLines = this.stacks.get(0).getStackHandler().getReversedCodeLines().subList(start, end);
		
		if (!sectionLines.isEmpty()){
			// check the first line
			String firstLine = sectionLines.get(0);
			String firstLinetoMatch = stackLines.get(0);
			if (!firstLine.equals(firstLinetoMatch)){
				// special case : first line is different but function and executor are identical : needs to split and fork. start = 0, end = 0
				splitAndForkSection(stack, -1); // no line to match so -1. Children will start at 0.
				this.stacks.add(stack);
				return true;
			}
			else {
				return super.acceptNewStack(stack);
			}
			
		}
		else{
			// must have children
			for (ActionGraphSection section : this.children){
				if (section.acceptNewStack(stack)){
					// child section has accepted it. Perfect.
					this.stacks.add(stack);
					return true;
				}
			}
			// section not found : need to create new section
			createSection(stack, 0); // no line to match so end = 0. Child will start at 0.
			this.stacks.add(stack);
			return true;
		}
	}

	@Override
	public String getFunction() {
		if (actionCount == 1)
			return action.getPrincipalCompositeFunction();
		else
			return AnalyzerHelper.getPrincipalCompositeFunction(this.stacks);
	}
	
	@Override
	public Set<String> getAllFunctions() {
		Set<String> functions = new HashSet<>(); 

		for (ThreadStack stack : this.stacks){
			for (String function : stack.getFunctionTags()){
				if (!functions.contains(function)){
					functions.add(function);
				}
			}
		}
		return functions;
	}
	

	@Override
	public String getExecutor() {
		return action.getExecutor();
	}
	
	@Override
	public String getOperation() {
		if (actionCount == 1)
			return action.getPrincipalCompositeOperation();
		else
			return AnalyzerHelper.getPrincipalCompositeOperation(this.stacks);
	}
	
	@Override
	public String getContentionType() {
		if (actionCount == 1)
			return action.getPrincipalCompositeContentionType();
		else{
			return AnalyzerHelper.getPrincipalCompositeContentionType(this.stacks);
		}
	}

	@Override
	public Set<String> getAllOperations() {
		Set<String> operations = new HashSet<>(); 
		
		for (ThreadStack stack : this.stacks){
			for (String operation : stack.getOperationTags()){
				if (!operations.contains(operation)){
					operations.add(operation);
				}
			}
		}
		return operations;
	}
	
	@Override
	public Set<String> getAllContentionTypes() {
		Set<String> contentionTypes = new HashSet<>(); 
		
		for (ThreadStack stack : this.stacks){
			for (String contentionType : stack.getContentionTypeTags()){
				if (!contentionTypes.contains(contentionType)){
					contentionTypes.add(contentionType);
				}
			}
		}
		return contentionTypes;
	}	
	
	public void updateActionCount(){
		actionCount++;
	}

	public int getActionCount() {
		return actionCount;
	}
}
