package org.jeyzer.monitor.engine.event;

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







import java.util.List;

import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.stack.StackText;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStack.ThreadStackHandler;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.Scope;

public abstract class MonitorTaskEvent extends MonitorEvent {

	protected String threadName;
	protected String threadId;
	protected String function;
	protected String functionPrincipal;
	protected int actionId;

	// real duration, used only on actions
	protected long realDuration;
	
	protected String jhId; // may be null
	
	protected StackText stackText; // may be null

	public MonitorTaskEvent(String eventName, MonitorEventInfo info, ThreadAction action){
		super(eventName, info);
		this.threadName = action.getName();
		this.threadId = action.getThreadId();
		this.actionId = action.getId();
		
		if (action.getThreadStackJeyzerMXInfo() != null && !action.getThreadStackJeyzerMXInfo().getJzrId().isEmpty())
			this.jhId = action.getThreadStackJeyzerMXInfo().getJzrId();
		
		this.function = action.getCompositeFunction();
		if (this.function == null || this.function.isEmpty()){
			this.function = "ATBI";
		}
		
		this.functionPrincipal = action.getPrincipalCompositeFunction();
		
		this.realDuration = Scope.STACK.equals(this.info.getScope())? 0 : 1; // Influences the duration computation
	}
	
	public MonitorTaskEvent(String eventName, MonitorEventInfo info, ThreadAction action, ThreadStackHandler stack){
		this(eventName, info, action);
		this.stackText = stack.getJzrFilteredText();
	}
	
	@Override
	public String getNameExtraInfo(){
		return getPrincipalFunction();
	}
	
	@Override
	public List<String> getPrintableParameters(){

		List<String> params = getPrintableParameters(
				this.function, 
				this.threadName,
				getPrintableEndDate(),
				getPrintableDuration()
				);
		
		addPrintableExtraParameters(params);
		
		return params;
	}
	
	public abstract void updateContext(ThreadStack stack);

	@Override
	public String dump() {
		StringBuilder msg = new StringBuilder(400);
		dumpHeader(msg, this.name);
		
		// task specifics
		msg.append("ACTION : " + this.function + "\n");
		msg.append("THREAD : " + this.threadId + " / " + this.threadName + "\n");
		
		dumpExtraParameters(msg);
		dumpFooter(msg);
		return msg.toString();
	}

	public boolean hasStackText() {
		return stackText != null;
	}
	
	public StackText getStackText() {
		return stackText;
	}

	public String getThreadName() {
		return threadName;
	}

	public String getThreadId() {
		return threadId;
	}

	public String getJhId() {
		return jhId;
	}

	public int getActionId() {
		return actionId;
	}
	
	public String getPrincipalFunction() {
		return functionPrincipal;
	}
	
	public void updateTime(long time){
		this.realDuration += time;
	}
	
	public void forceTime(long time){
		this.realDuration = time;
	}
	
	/**
	 * Duration in seconds
	 */
	@Override
	public long getDuration(){
		return this.realDuration != 0 ? 
				this.realDuration/1000 : // action case
				(super.getDuration());   // stack case, or action case with percentage
	}
	
	@Override
	public boolean equalsIgnoringCategory(MonitorEvent candidate) {
		if (!(candidate instanceof MonitorTaskEvent))
			return false;
		
		if (this.threadId.equals(((MonitorTaskEvent)candidate).getThreadId()))
			return super.equalsIgnoringCategory(candidate);
		
		return false;
	}
	
}
