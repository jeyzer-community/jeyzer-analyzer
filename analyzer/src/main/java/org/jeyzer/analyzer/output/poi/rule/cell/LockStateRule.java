package org.jeyzer.analyzer.output.poi.rule.cell;

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







import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.data.Action;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadState;
import org.jeyzer.analyzer.output.poi.CellColor;
import org.jeyzer.analyzer.output.poi.context.SequenceSheetDisplayContext;
import org.jeyzer.analyzer.output.stats.Stats;


public class LockStateRule extends AbstractCellDisplayRule implements DisplayRule{

	public static final String RULE_NAME = "lock_state";
	
	public static final String LOCK_OWNER_FIELD = "lock_owner";
	public static final String LOCK_OWNER_AND_LOCKED_FIELD = "lock_owner_and_locked";
	public static final String LOCK_DEADLOCK_FIELD = "deadlock";
	public static final String CODE_LOCKED_FIELD = "code_locked";
	public static final String SUSPENDED_FIELD = "suspended";
	
	private static final String LOCK_OWNER_DISPLAY = "LOCK OWNER";
	private static final String WAITING_TO_LOCK_DISPLAY = "waiting to lock ";
	
	public static final String LOCK_OWNER_AND_LOCKED_DEFAULT_COLOR = "VIOLET";
	public static final String LOCK_DEADLOCK_DEFAULT_COLOR = "RED";
	public static final String CODE_LOCKED_DEFAULT_COLOR = "RGB-255-145-71";
	public static final String SUSPENDED_DEFAULT_COLOR = "RGB-246-26-26"; // red flash, must be processed like deadlock
	
	private Object lockOwnerColor;
	private Object lockOwnerAndLockedColor;
	private Object deadlockColor;
	private Object codeLockedColor;
	private Object suspendedColor;

	protected Stats lockOwnerStats = null; // Extra stats appearance for lock owner	
	protected Stats lockOwnerAndLockedStats = null; // Extra stats appearance for lock owner and locked
	protected Stats deadlockStats = null; // Extra stats appearance for deadlock activity
	protected Stats codeLockedStats = null; // Extra stats appearance for code locked activity
	protected Stats suspendedStats = null; // Extra stats appearance for suspended activity

	public LockStateRule(ConfigDisplay displayCfg, SequenceSheetDisplayContext context){
		super(displayCfg, context);
		
		this.lockOwnerColor = CellColor.buildColor((String)displayCfg.getValue(LOCK_OWNER_FIELD));
		
		this.lockOwnerAndLockedColor =  displayCfg.getValue(LOCK_OWNER_AND_LOCKED_FIELD, LOCK_OWNER_AND_LOCKED_DEFAULT_COLOR);
		lockOwnerAndLockedColor = CellColor.buildColor((String)lockOwnerAndLockedColor);
		
		this.deadlockColor =  displayCfg.getValue(LOCK_DEADLOCK_FIELD, LOCK_DEADLOCK_DEFAULT_COLOR);
		deadlockColor = CellColor.buildColor((String)deadlockColor);
		
		this.codeLockedColor = displayCfg.getValue(CODE_LOCKED_FIELD, CODE_LOCKED_DEFAULT_COLOR);
		codeLockedColor = CellColor.buildColor((String)codeLockedColor);
		
		this.suspendedColor = displayCfg.getValue(SUSPENDED_FIELD, SUSPENDED_DEFAULT_COLOR);
		suspendedColor = CellColor.buildColor((String)suspendedColor);
		
		this.lockOwnerStats = duplicateStats();
		this.lockOwnerAndLockedStats = duplicateStats();
		this.deadlockStats = duplicateStats();
		this.codeLockedStats = duplicateStats();
		this.suspendedStats = duplicateStats();
	}
	
	@Override
	public boolean apply(Action action, List<Cell> cells) {
		Cell cell;
		ThreadStack ts;
		String state;
		
		boolean actionOwnerHit = false;
		boolean actionLockedHit = false;
		boolean actionOwnerAndLockedHit = false;
		boolean actionDeadlockHit = false;
		boolean actionCodeLockedHit = false;
		boolean suspendedHit = false;
		
		// cell value
		for (int j=0; j< action.size(); j++){
			state = null;
			cell = cells.get(j);
			ts = action.getThreadStack(j);
			
			boolean locked = ts.isBlocked();
			boolean codeLocked = ts.isCodeLocked();
			boolean locking = !ts.getLockedThreads().isEmpty();

			if (ts.isInDeadlock()){
				state = getLockingLabel(ts) + " // " + getLockedLabel(ts);
				setColorForeground(cell, this.deadlockColor);
				actionDeadlockHit = hitStats(this.deadlockStats, actionDeadlockHit, ts.getInstanceCount());
			}
			else if (locked && locking){
				state = getLockingLabel(ts) + " // " + getLockedLabel(ts);
				setColorForeground(cell, this.lockOwnerAndLockedColor);
				actionOwnerAndLockedHit = hitStats(this.lockOwnerAndLockedStats, actionOwnerAndLockedHit, ts.getInstanceCount());
			}
			else if (locked){
				state = getLockedLabel(ts);
				setColorForeground(cell);
				actionLockedHit = hitStats(this.stats, actionLockedHit, ts.getInstanceCount());
			}
			else if (locking){
				state = getLockingLabel(ts);
				setColorForeground(cell, this.lockOwnerColor);
				actionOwnerHit = hitStats(this.lockOwnerStats, actionOwnerHit, ts.getInstanceCount());
			}
			else if (codeLocked){
				state = "ADV LOCKED (" + ts.getCodeLockName() + ")";
				setColorForeground(cell, this.codeLockedColor);
				actionCodeLockedHit = hitStats(this.codeLockedStats, actionCodeLockedHit, ts.getInstanceCount());
			}
			else if(ts.isSuspended()) {
				state = "SUSPENDED";
				setColorForeground(cell, this.suspendedColor);
				suspendedHit = hitStats(this.suspendedStats, suspendedHit, ts.getInstanceCount());
			}
			
			if (state != null)
				setValue(cell, state);
			
			// Add wait for class
			if (ts.isBlocked()) {
				String waitForClass = WAITING_TO_LOCK_DISPLAY + ts.getLockClassName();
				appendSmallValue(cell, waitForClass);
			}
		}
		
		return true;
	}

	@Override
	public int displayLegend(int line, int pos) {
		
		line = displayLegend("Thread waiting for java lock release", ThreadState.BLOCKED.name(), true, line, pos);
		
		// override color (hack)
		Object colorBackup = this.color;
		
		this.color = this.lockOwnerColor;
		line = displayLegend("Thread owning java lock", LOCK_OWNER_DISPLAY, true, line, pos);
		
		this.color = this.lockOwnerAndLockedColor;
		line = displayLegend("Thread owning java lock, waiting for java lock release", ThreadState.BLOCKED + "/" + LOCK_OWNER_DISPLAY, true, line, pos);
		
		this.color = this.deadlockColor;
		line = displayLegend("Deadlock", ThreadState.BLOCKED + "/" + LOCK_OWNER_DISPLAY, true, line, pos);

		this.color = this.codeLockedColor;
		line = displayLegend("Thread waiting for advanced lock release", "ADV LOCKED (Lock class)", true, line, pos);
		
		this.color = this.suspendedColor;
		line = displayLegend("Thread suspended on debug breakpoint", "SUSPENDED", true, line, pos);
		
		this.color = colorBackup;
		
		return line;
	}	

	@Override
	public int displayStats(int line, int pos) {
		line = super.displayStats(this.stats,"Thread waiting for java lock release", ThreadState.BLOCKED.name(), true, line, pos);
		
		Object colorBackup = this.color;

		this.color = this.lockOwnerColor;
		line = super.displayStats(this.lockOwnerStats,"Thread owning java lock", LOCK_OWNER_DISPLAY, true, line, pos);
		
		this.color = this.lockOwnerAndLockedColor;
		line = super.displayStats(this.lockOwnerAndLockedStats,"Thread owning java lock, waiting for java lock release", ThreadState.BLOCKED + "/" + LOCK_OWNER_DISPLAY, true, line, pos);
		
		this.color = this.deadlockColor;
		line = super.displayStats(this.deadlockStats,"Deadlock", ThreadState.BLOCKED + "/" + LOCK_OWNER_DISPLAY, true, line, pos);

		this.color = this.codeLockedColor;
		line = super.displayStats(this.codeLockedStats,"Thread waiting for advanced lock release", "ADV LOCKED (Lock class)", true, line, pos);
		
		this.color = this.suspendedColor;
		line = super.displayStats(this.suspendedStats,"Thread suspended on debug breakpoint", "SUSPENDED", true, line, pos);
		
		this.color = colorBackup;
		
		return line;
	}	
	
	@Override
	public String getName() {
		return RULE_NAME;
	}
	
	private String getLockedLabel(ThreadStack ts){
		return ThreadState.BLOCKED + "(" 
				+ (ts.getLockingThread() != null ? ts.getLockingThread().getID():"Locking Thread Not Detected") 
				+ ")";
	}
	
	private String getLockingLabel(ThreadStack ts){
		StringBuilder ownedLocksLabel = new StringBuilder();
		if (ts.getLockedThreads().isEmpty()){
			ownedLocksLabel.append("Not available");
		}
		else {
			Iterator<ThreadStack> iter =  ts.getLockedThreads().iterator();
			while (iter.hasNext()){
				ownedLocksLabel.append(iter.next().getID());
				if (iter.hasNext())
					ownedLocksLabel.append(" : ");
			}
		}
		
		return LOCK_OWNER_DISPLAY + "(" 
				+ ts.getID()
				+ " -> "
				// on deadlock cases, locking thread can sometimes not been printed immediately. Seen in JMX case.
				+ ownedLocksLabel.toString() 
				+ ")";
	}
	
	@Override
	public boolean hasLegend() {
		return true;
	}
	
	@Override
	public boolean hasStats() {
		return true;
	}
}
