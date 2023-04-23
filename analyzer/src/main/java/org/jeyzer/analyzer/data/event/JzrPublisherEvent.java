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

import com.google.common.primitives.Longs;

public class JzrPublisherEvent {
	
	public static final String PUBLISHER_SOURCE = "Jeyzer Publisher";
	
	public static final String LEVEL_INFO = "I";
	public static final String LEVEL_WARNING = "W";
	public static final String LEVEL_CRITICAL = "C";
	
	private final String code; 
	private final String name;
	private final Level level; 
	private final SubLevel sublevel;
	private final Date start;
	private final short trust;
	private final Date snapshotStart;
	
	private Date snapshotEnd;
	
	public JzrPublisherEvent(String code, String name, String level, String sublevel, String start, Date snapshotStart) {
		this.code = code.intern();
		this.name = name.intern();
		this.level = (level!= null) ? Level.getLevelFromCapital(level.charAt(0)) : Level.UNKNOWN;
		this.sublevel = SubLevel.getSubLevel(sublevel);
		Long time = Longs.tryParse(start);
		this.start = (time == null || time<=0) ? null : new Date(time);
		this.trust = 100;
		this.snapshotStart = snapshotStart;
		this.snapshotEnd = snapshotStart; // start with that value, will be updated
	}

	public String getSource() {
		return PUBLISHER_SOURCE;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public Level getLevel() {
		return level;
	}

	public SubLevel getSublevel() {
		return sublevel;
	}

	public Date getStart() {
		return start;
	}

	public short getTrust() {
		return trust;
	}
	
	public Date getSnapshotStart() {
		return snapshotStart;
	}

	public Date getSnapshotEnd() {
		return snapshotEnd;
	}

	public void updateEnd(Date snapshotEnd) {
		this.snapshotEnd = snapshotEnd;
	}
	
	public boolean isValid() {
		//mandatory fields
		if (this.code.isEmpty())
			return false;
		if (this.name.isEmpty())
			return false;
		if (Level.UNKNOWN.equals(this.level) || Level.ERROR.equals(this.level))
			return false;
		if (SubLevel.INVALID.equals(this.sublevel) || SubLevel.NOT_SET.equals(this.sublevel))
			return false;
		if (this.start == null)
			return false;
		return true;
	}
}
