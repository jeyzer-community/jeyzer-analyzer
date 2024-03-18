package org.jeyzer.monitor.engine.event.info;

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
import java.util.concurrent.atomic.AtomicLong;

public class MonitorEventInfo {
	
	// If MAX is reached, will go to MIN value. Unlikely to happen.
	private static AtomicLong extIdCount = new AtomicLong(0);
	
	// Event identification, unique within the Jeyzer Monitor scope
	private final String id;                                    // Technical id, built from rule, time stamp and context info - used in the event retrieval
	private long extId = -1L;  	// External id suitable for display, initiated when the event gets elected
	
	private final String ref;
	private final String rank;
	private final Level level;
	private final SubLevel subLevel;
	private final Scope scope;
	protected final Date start;
	
	private String message;
	protected Date end;
	private String ticket; // can be null

	public MonitorEventInfo(String id, String ref, Scope scope, Level level, SubLevel subLevel, Date start, Date end, String message, String ticket){
		this.id = id;
		this.ref = ref;
		this.level = level;
		this.subLevel = subLevel;
		this.rank = buildRank();
		this.scope = scope;
		this.start = start;
		this.end = end;
		this.message = message;
		this.ticket = ticket;
	}

	public String getId(){
		return id;
	}
	
	public String getExtId(){
		return String.valueOf(extId);
	}
	
	public void initiateExtId() {
		this.extId = extIdCount.incrementAndGet();
	}
	
	public String getRef(){
		return ref;
	}
	
	public String getMessage(){
		return message;
	}
	
	public Level getLevel(){
		return this.level;
	}
	
	public SubLevel getSubLevel(){
		return this.subLevel;
	}
	
	public String getRank(){
		return this.rank;
	}
	
	public Scope getScope(){
		return this.scope;
	}
	
	public Date getStartDate(){
		return this.start;
	}
	
	public Date getEndDate(){
		return this.end;
	}
	
	public void updateEnd(Date date) {
		this.end = date;
	}
	
	public void updateMessage(String message) {
		this.message = message;
	}
	
	public String getTicket() {
		return ticket;
	}
	
	public boolean hasTicket() {
		return ticket!= null && !ticket.isEmpty();
	}
	
	private String buildRank() {
		StringBuilder rank = new StringBuilder();
		rank.append(level.getCapital());
		rank.append(subLevel.value());
		return rank.toString();
	}
}
