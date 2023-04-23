package org.jeyzer.analyzer.output.poi;

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







import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Set;

import org.apache.poi.ss.util.CellReference;
import org.jeyzer.analyzer.data.Action;

import com.google.common.collect.LinkedListMultimap;

public class CellRefRepository {
	
	private Map<String, Map<String, CellReference>> sheetCellRefs = new LinkedHashMap<String, Map<String, CellReference>>();
	private Map<String, Map<Date, CellReference>> sheetColumnRefs = new LinkedHashMap<String, Map<Date, CellReference>>();
	private LinkedListMultimap<String, CellReference> dateRefs = LinkedListMultimap.create();
	private LinkedListMultimap<String, CellReference> actionRefs;

	private int initialCellRefSize;  // Performance optimization : create maps immediately with the right size. 
	
	public CellRefRepository(int actionSize, int actionsStackSize) {
		actionRefs = LinkedListMultimap.create(actionSize);
		initialCellRefSize = (int) Math.ceil(actionsStackSize / 0.75);
	}

	public Set<String> getSheetTypes(){
		return sheetColumnRefs.keySet();
	}
	
	public Set<String> getActionSheetTypes(){
		return sheetCellRefs.keySet();
	}	
	
	public void addCellRef(String linkType, Action action, Date timestamp, CellReference ref){
		Map<String, CellReference> cellRefs = sheetCellRefs.computeIfAbsent(
				linkType, 
				x -> new HashMap<String, CellReference>(initialCellRefSize));
		cellRefs.put(action.getName() + timestamp, ref);
	}	
	
	public void addCellRef(String linkType, String eventId, CellReference ref){
		Map<String, CellReference> cellRefs = sheetCellRefs.computeIfAbsent(
				linkType, 
				x -> new HashMap<String, CellReference>(initialCellRefSize));
		cellRefs.put(eventId, ref);
	}

	public void addColumnRef(String linkType, Date date, CellReference ref){
		Map<Date, CellReference> columnRefs = sheetColumnRefs.computeIfAbsent(
				linkType, 
				x -> new HashMap<Date, CellReference>());
		columnRefs.put(date, ref);
	}
	
	public CellReference getRefColumn(String linkType, Date date){
		Map<Date, CellReference> columnRefs = sheetColumnRefs.get(linkType);
		
		if (columnRefs == null){
			return null;
		}
		
		return columnRefs.get(date);
	}

	public CellReference getCellRef(String linkType, Action action){
		return getCellRef(linkType, getKey(action));
	}
	
	public CellReference getCellRef(String linkType, String threadId, Date startDate){
		return getCellRef(linkType, threadId + startDate.toString());
	}
	
	public CellReference getCellRef(String linkType, String key){
		Map<String, CellReference> cellRefs = sheetCellRefs.get(linkType);
		
		if (cellRefs == null){
			return null;
		}
		
		return cellRefs.get(key);
	}
	
	private String getKey(Action action) {
		return action.getName() + action.getStartDate();
	}
	
	public void addActionRef(String id, CellReference ref){
		this.actionRefs.put(id, ref);
	}

	public List<CellReference> getActionRef(String id){
		return this.actionRefs.get(id);
	}

	public void addDateRef(String time, CellReference ref) {
		this.dateRefs.put(time, ref);
	}
	
	public LinkedListMultimap<String, CellReference> getDateRefMultiMap(){
		return this.dateRefs;
	}
	
}
