package org.jeyzer.analyzer.data.event;

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

import org.jeyzer.monitor.engine.event.info.Level;
import org.jeyzer.monitor.engine.event.info.SubLevel;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public class ExternalEvent {
	
	public static final String SCOPE_GLOBAL = "G";  // session
	public static final String SCOPE_SYSTEM = "S";
	public static final String SCOPE_ACTION = "A";
	
	public static final String LEVEL_INFO = "I";
	public static final String LEVEL_WARNING = "W";
	public static final String LEVEL_CRITICAL = "C";
	
	private final String source; 
	private final String service; 
	private final String type; 
	private final String code; 
	private final String name; 
	private final String narrative;
	private final Level level; 
	private final SubLevel sublevel; 
	private final String scope; 
	private final String id; 
	private final String message;
	private final String ticket;
	private final Date start; 
	private final String threadId; 
	private final short trust; 
	private final boolean oneshot;
	private final Date snapshotStart;
	
	private Date end;
	private Date snapshotEnd;
	private boolean applicativeRuleHit;
	
	public ExternalEvent(String source, String service, String type, String code, String name, String narrative,
			String level, String sublevel, String scope, String id, String message, String ticket, String start, String end,
			String threadId, String trust, String oneshotValue, Date snapshotStart) {
		this.source = source.intern();
		this.service = service.intern();
		this.type = type.intern();
		this.code = code.intern();
		this.name = name.intern();
		this.narrative = narrative.intern();
		this.level = (level!= null) ? Level.getLevelFromCapital(level.charAt(0)) : Level.UNKNOWN;
		this.sublevel = SubLevel.getSubLevel(sublevel);
		this.scope = scope.intern();
		this.id = id.intern();
		this.message = message.intern();
		this.ticket = ticket;
		Long time = Longs.tryParse(start);
		this.start = (time == null || time<=0) ? null : new Date(time);
		time = Longs.tryParse(end);
		this.end = (time == null || time<=0) ? null : new Date(time);
		this.threadId = threadId.intern();
		Integer value = Ints.tryParse(trust);
		this.trust = (value == null || value<0 || value >100) ? -1 : value.shortValue();
		this.oneshot = Boolean.parseBoolean(oneshotValue);
		this.snapshotStart = snapshotStart;
		this.snapshotEnd = snapshotStart; // start with that value, will be updated
	}

	public String getSource() {
		return source;
	}

	public String getService() {
		return service;
	}

	public String getType() {
		return type;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getNarrative() {
		return narrative;
	}

	public Level getLevel() {
		return level;
	}

	public SubLevel getSublevel() {
		return sublevel;
	}

	public String getScope() {
		return scope;
	}
	
	public boolean isActionEvent() {
		return ExternalEvent.SCOPE_ACTION.equals(scope);
	}

	public String getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}
	
	public String getTicket() {
		return ticket; // can be null
	}

	public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end; // can be null if event created on very last snapshot
	}

	public String getThreadId() {
		return threadId;
	}

	public short getTrust() {
		return trust;
	}

	public boolean isOneshot() {
		return oneshot;
	}
	
	public Date getSnapshotStart() {
		return snapshotStart;
	}

	public Date getSnapshotEnd() {
		return snapshotEnd;
	}

	public void updateEnd(Date end, Date snapshotEnd) {
		this.end = end;
		this.snapshotEnd = snapshotEnd;
	}
	
	public boolean isValid() {
		//mandatory fields
		if (this.code.isEmpty())
			return false;
		if (this.name.isEmpty())
			return false;
		if (this.id.isEmpty())
			return false;
		if (Level.UNKNOWN.equals(this.level) || Level.ERROR.equals(this.level))
			return false;
		if (SubLevel.INVALID.equals(this.sublevel) || SubLevel.NOT_SET.equals(this.sublevel))
			return false;
		if (!SCOPE_GLOBAL.equals(this.scope) 
				&& !SCOPE_SYSTEM.equals(this.scope)
				&& !SCOPE_ACTION.equals(this.scope))
			return false;
		if (this.start == null)
			return false;
		if (SCOPE_ACTION.equals(this.scope) && this.threadId.isEmpty())
			return false;
		if (this.trust == -1)
			return false;
		return true;
	}
	
	public void hitApplicativeRule() {
		this.applicativeRuleHit = true;
	}

	public boolean hasApplicativeRuleHit() {
		return applicativeRuleHit;
	}

	public void resetHit() {
		this.applicativeRuleHit = false;  // allows the event to be processed in multiple places(typically in different monitoring sheets)
	}
}
