package org.jeyzer.analyzer.output.poi.rule.header;

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







import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeader;
import org.jeyzer.analyzer.data.gc.GarbageCollectorInfo;
import org.jeyzer.analyzer.data.gc.GarbageCollectorMemoryPool;
import org.jeyzer.analyzer.output.poi.CellColor;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.style.CellFonts;

public abstract class AbstractGarbageCollectionMemoryRule extends AbstractGarbageCollectionRule{

	public enum GC_EXECUTION_STATE { NA, EXECUTED, NOT_EXECUTED }
	
	public static class GCExecutionValue{
		
		private GC_EXECUTION_STATE state = GC_EXECUTION_STATE.NA;
		private boolean displayNonExecutionValue = true;
		private Long value = null;  // NA
		
		public GCExecutionValue(boolean display) {
			this.displayNonExecutionValue = display; 
		}
		
		public long getDisplayableValue(){
			return GC_EXECUTION_STATE.NA.equals(state)? -1 : value;
		}

		public Long getValue(){
			return value;
		}
		
		public void setValue(long value){
			this.value = value;
		}
		
		public GC_EXECUTION_STATE getState(){
			return state;
		}
		
		public void setState(GC_EXECUTION_STATE state){
			this.state = state;
		}
		
	} 
	
	public static class GCExecutionPercentValue{
		
		private GC_EXECUTION_STATE state = GC_EXECUTION_STATE.NA;
		private boolean displayNonExecutionValue = true;
		private Double value = null;  // NA
		
		public GCExecutionPercentValue(boolean display) {
			this.displayNonExecutionValue = display; 
		}
		
		public Double getValue(){
			return value;
		}
		
		public double getDisplayableValue(){
			return GC_EXECUTION_STATE.NA.equals(state)? -1 : value;
		}
		
		public void setValue(double value){
			this.value = value;
		}
		
		public GC_EXECUTION_STATE getState(){
			return state;
		}
		
		public void setState(GC_EXECUTION_STATE state){
			this.state = state;
		}
	}
	
	private static final String GC_NOT_EXECUTED_DISPLAY = "No exec";
	private static final Object GC_NOT_EXECUTED_COLOR =  CellColor.buildColor("RGB-231-230-230");

	// possibles values : all or <pool name>
	private static final String DISPLAY_POOL = "pool";
	protected static final String DISPLAY_POOL_ALL = "all";
	protected static final String DISPLAY_POOL_OLD = "old";
	protected static final String DISPLAY_POOL_YOUNG = "young";

	protected String pool;
	
	public AbstractGarbageCollectionMemoryRule(ConfigSheetHeader headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
		
		this.pool = (String)headerCfg.getValue(DISPLAY_POOL);
	}
	
	protected abstract long getMemoryPoolValue(GarbageCollectorMemoryPool memPool);
	
	protected abstract double getMemoryPoolPercentValue(GarbageCollectorMemoryPool memPool);
	
	protected GCExecutionValue buildGCExecutionValue(GarbageCollectorInfo gcInfo, long value, boolean display) {
		GCExecutionValue result = new GCExecutionValue(display); 

		if (gcInfo == null || !gcInfo.isValid())
			return result;
		
		// not executed
		if (!gcInfo.isFresh() || !gcInfo.isStarted()){
			result.state = GC_EXECUTION_STATE.NOT_EXECUTED;
			accessValue(gcInfo, result, value);
		}
		else {
			result.state = GC_EXECUTION_STATE.EXECUTED;
			accessValue(gcInfo, result, value);
		}
		
		return result;
	}
	

	private void accessValue(GarbageCollectorInfo gcInfo, GCExecutionValue result, long value) {
		if (DISPLAY_POOL_ALL.equals(pool))
			result.setValue(value);
		else{
			GarbageCollectorMemoryPool memPool = accessGarbageCollectorMemoryPool(gcInfo, pool);
			if (memPool != null)
				result.setValue(getMemoryPoolValue(memPool));
			else
				result.state = GC_EXECUTION_STATE.NA; // revert state to NA
		}
	}

	protected GCExecutionPercentValue buildGCExecutionPercentValue(GarbageCollectorInfo gcInfo, double value, boolean display) {
		GCExecutionPercentValue result = new GCExecutionPercentValue(display); 

		if (gcInfo == null || !gcInfo.isValid())
			return result;

		// not executed
		if (!gcInfo.isFresh() || !gcInfo.isStarted()){
			result.state = GC_EXECUTION_STATE.NOT_EXECUTED;
			accessPercentValue(gcInfo, result, value);
		}
		else {
			result.state = GC_EXECUTION_STATE.EXECUTED;
			accessPercentValue(gcInfo, result, value);
		}

		return result;
	}	

	private void accessPercentValue(GarbageCollectorInfo gcInfo, GCExecutionPercentValue result, double value) {
		if (DISPLAY_POOL_ALL.equals(pool))
			result.setValue(value);
		else{
			GarbageCollectorMemoryPool memPool = accessGarbageCollectorMemoryPool(gcInfo, pool);
			if (memPool != null)
				result.setValue(getMemoryPoolPercentValue(memPool));
			else
				result.state = GC_EXECUTION_STATE.NA; // revert state to NA
		}
	}
	
	private GarbageCollectorMemoryPool accessGarbageCollectorMemoryPool(GarbageCollectorInfo gcInfo, String pool) {
		if (DISPLAY_POOL_OLD.equals(pool))
			return gcInfo.getOldGarbageCollectorMemoryPool();
		else if (DISPLAY_POOL_YOUNG.equals(pool))
			return gcInfo.getYoungGarbageCollectorMemoryPool();
		else 
			return gcInfo.getGarbageCollectorMemoryPool(pool);
	}
	
	protected String displayCommentPoolAndGCName(){
		if (DISPLAY_POOL_ALL.equals(pool) && DISPLAY_GC_LAST.endsWith(gc))
			return " all memory pools for the " + gc + " garbage collector last execution.\n";
		else if (DISPLAY_POOL_ALL.equals(pool))
			return " all memory pools managed by the " + gc + " garbage collector.\n";
		else
			return " the " + pool + " memory pool managed by the " + gc + " garbage collector.\n";
	}
	
	protected String getNameSuffix(){
		return "-" + gc + "-" + pool;
	}
	
	protected void setValue(Cell cell, GCExecutionValue value, HeaderFunction function){
		if (GC_EXECUTION_STATE.NOT_EXECUTED.equals(value.state)){
			if (value.displayNonExecutionValue)
				super.setValue(cell, value.value);
			else{
				cell.setCellValue(GC_NOT_EXECUTED_DISPLAY);
				
				CellStyle newStyle = getAmendedStyles().amendStyleWithFont(
						this.context.getWorkbook(), 
						cell.getCellStyle(), 
						getFont(CellFonts.FONT_10)
						);
				
				cell.setCellStyle(newStyle);
			}
		} 
		else {
			super.setValue(cell, value.value);
			function.apply(value.value, cell);
		}
	}
	
	protected void setValue(Cell cell, GCExecutionPercentValue percentValue, HeaderFunction function){
		if (GC_EXECUTION_STATE.NOT_EXECUTED.equals(percentValue.state)){
			if (percentValue.displayNonExecutionValue)
				super.setValue(cell, percentValue.getValue());
			else{
				cell.setCellValue(GC_NOT_EXECUTED_DISPLAY);

				CellStyle newStyle = getAmendedStyles().amendStyleWithFont(
						this.context.getWorkbook(), 
						cell.getCellStyle(), 
						getFont(CellFonts.FONT_10)
						);
				
				cell.setCellStyle(newStyle);
			}
		} 
		else {
			super.setValue(cell, percentValue.getValue());
			if (function != null)
				function.apply(percentValue.getValue(), cell);
		}
	}

	protected void setValueBasedColorForeground(Cell cell, GCExecutionPercentValue percentValue, double prevValue){
		if (GC_EXECUTION_STATE.NOT_EXECUTED.equals(percentValue.state))
			setColorForeground(
					cell,
					GC_NOT_EXECUTED_COLOR);
		else
			super.setValueBasedColorForeground(
					cell, 
					percentValue.getDisplayableValue(), 
					prevValue, 
					GC_EXECUTION_STATE.NA.equals(percentValue.state));
	}	
	
	protected void setValueBasedColorForeground(Cell cell, GCExecutionValue value, double prevValue){
		if (GC_EXECUTION_STATE.NOT_EXECUTED.equals(value.state))
			setColorForeground(
					cell,
					GC_NOT_EXECUTED_COLOR);
		else
			super.setValueBasedColorForeground(
					cell, 
					value.getDisplayableValue(),
					prevValue, 
					GC_EXECUTION_STATE.NA.equals(value.state));
	}
	
}
