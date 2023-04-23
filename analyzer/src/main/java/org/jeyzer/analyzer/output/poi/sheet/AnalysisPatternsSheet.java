package org.jeyzer.analyzer.output.poi.sheet;

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




import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_SECTION_HEADER;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_SECTION_HEADER_BOLD;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jeyzer.analyzer.config.patterns.ConfigAggregatorPatterns;
import org.jeyzer.analyzer.config.patterns.ConfigPatterns;
import org.jeyzer.analyzer.config.patterns.ConfigSinglePatterns;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.config.report.ConfigAnalysisPatternsSheet;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.rule.pattern.DiscoveryPattern;
import org.jeyzer.analyzer.rule.pattern.Pattern;
import org.jeyzer.analyzer.session.JzrSession;

public class AnalysisPatternsSheet extends JeyzerSheet {
	
	private ConfigAnalysisPatternsSheet sheetCfg;
	private ConfigAggregatorPatterns tdPatternsCfg;
	
	public AnalysisPatternsSheet(ConfigAnalysisPatternsSheet config, ConfigPatterns tdPatternsCfg, JzrSession session, DisplayContext displayContext) {
		super(session, displayContext);
		this.sheetCfg = config;
		this.tdPatternsCfg = (ConfigAggregatorPatterns)tdPatternsCfg;
	}

	@Override
	public void display() {
		Sheet sheet = createSheet(this.sheetCfg);
    	sheet.setColumnWidth(0, 3*256);
    	sheet.setColumnWidth(1, 42*256);
    	sheet.setColumnWidth(2, 56*256);
    	sheet.setColumnWidth(3, 8*256);
    	sheet.setColumnWidth(4, 150*256);
    	sheet.setColumnWidth(5, 20*256);
    	
    	int rowLine = 1;
    	
    	displayCategoryHeader(sheet, "Profile Source", "Version", "Static", "", "", rowLine++);
   		rowLine = displayProfileSourceVersions(sheet, rowLine);
   		
   		displayCategoryHeader(sheet, "Content", "Type", "", "", "", rowLine++);
   		rowLine = displayPatternsContent(sheet, rowLine);

   		for(ConfigDisplay displayCfg : sheetCfg.getDisplayConfigs()){
   			String name = displayCfg.getName();
   			
   			if(ConfigAnalysisPatternsSheet.JZRR_EXCLUDES.equals(name)){
   				rowLine= displayExcludes(sheet, rowLine);
   			}
   			else if(ConfigAnalysisPatternsSheet.JZRR_LOCKERS.equals(name)){
   	   	   		displayCategoryHeader(sheet, "Lockers", "Stack pattern", "", "", "", rowLine++);
   	   	   		rowLine = displayPatterns(sheet, ConfigPatterns.JZRA_LOCKER, rowLine);
   			}
   			else if(ConfigAnalysisPatternsSheet.JZRR_FUNCTIONS.equals(name)){
   	   	   		displayCategoryHeader(sheet, "Functions", "Stack pattern", "", "", "", rowLine++);
   	   	   		rowLine = displayPatterns(sheet, ConfigPatterns.JZRA_FUNCTION, rowLine);
   			}
   			else if(ConfigAnalysisPatternsSheet.JZRR_DISCOVERY_FUNCTIONS.equals(name)){
   	   	   		displayCategoryHeader(sheet, "Discovery Functions", "Keywords", "", "", "", rowLine++);
   	   	   		rowLine = displayDiscoveryPatterns(sheet, ConfigPatterns.JZRA_DISCOVERY_FUNCTION, rowLine);
   			}
   			else if(ConfigAnalysisPatternsSheet.JZRR_OPERATIONS.equals(name)){
   	   	   		displayCategoryHeader(sheet, "Operations", "Stack pattern", "", "", "Contention type", rowLine++);
   	   	   		rowLine = displayPatterns(sheet, ConfigPatterns.JZRA_OPERATION, rowLine);
   			}
   			else if(ConfigAnalysisPatternsSheet.JZRR_DISCOVERY_OPERATIONS.equals(name)){
   	   	   		displayCategoryHeader(sheet, "Discovery Operations", "Keywords", "", "", "", rowLine++);
   	   	   		rowLine = displayDiscoveryPatterns(sheet, ConfigPatterns.JZRA_DISCOVERY_OPERATION, rowLine);
   			}
   			else if(ConfigAnalysisPatternsSheet.JZRR_EXECUTORS.equals(name)){
   	   	   		displayCategoryHeader(sheet, "Executors", "Stack pattern", "", "", "", rowLine++);
   	   	   		rowLine = displayPatterns(sheet, ConfigPatterns.JZRA_EXECUTOR, rowLine);
   			}
   		}
   		
    	sheet.createFreezePane(0, 1);
    	addMainFrame(sheet, sheetCfg, rowLine, 6);
    	
    	close(this.sheetCfg);
	}
	
	private int displayExcludes(Sheet sheet, int rowLine) {
	   	displayCategoryHeader(sheet, "Excludes", "Stack size interest", "", "", "", rowLine++);
		Row row = sheet.createRow(rowLine++);

		int stackSizeThreshold = 0;
		String displayName = "Not found";
		ConfigSinglePatterns masterPatterns = this.tdPatternsCfg.getMasterPatterns();
		if (masterPatterns != null){
			stackSizeThreshold = this.session.getStackMinimumSize();
			displayName = masterPatterns.getName();
		}
			
		addCell(row, 1, displayName, getCellStylePlainReference(rowLine));
		addCell(row, 2, "Threshold", getCellStylePlainReference(rowLine));
		addCell(row, 3, stackSizeThreshold, getCellStylePlainReference(rowLine));
		
		addEmptyCell(row, 4, getCellStylePlainReference(rowLine));
		addEmptyCell(row, 5, getCellStylePlainReference(rowLine));
			
	   	displayCategoryHeader(sheet, "", "Stack pattern", "", "", "", rowLine++);
	   	rowLine = displayPatterns(sheet, ConfigPatterns.JZRA_EXCLUDE, rowLine);
	   	
		return rowLine;
	}

	@SuppressWarnings("unchecked")
	private int displayDiscoveryPatterns(Sheet sheet, String type, int rowLine) {
		for (ConfigSinglePatterns patternSet : this.tdPatternsCfg.getPatternSets()){
			Map<String, Pattern> patternMap = (Map<String, Pattern>)patternSet.getValue(type);
			
			Set<String> keys = patternMap.keySet();
			
			if (keys.isEmpty()){
				rowLine = displayEmptyPatterns(sheet, patternSet, rowLine);
				continue;
			}
			
			boolean start = true;
			for (String key : keys){
				boolean discoveryStart = true;
				DiscoveryPattern pattern = (DiscoveryPattern)patternMap.get(key);
				if (pattern.getPatterns().isEmpty()){
					Row row = sheet.createRow(rowLine);
					if (start)
						addCell(row, 1, patternSet.getName(), getCellStylePlainReference(rowLine));
					else
						addEmptyCell(row, 1, getCellStylePlainReference(rowLine));
					addCell(row, 2, pattern.getDisplayName(), getCellStylePlainReference(rowLine));
					addCell(row, 3, "(No match)", getCellStylePlainReference(rowLine));
					addEmptyCell(row, 4, getCellStylePlainReference(rowLine));
					addEmptyCell(row, 5, getCellStylePlainReference(rowLine));
					start = false;
					rowLine++;
					itemsCount++;
					continue;
				}
				for (String code : pattern.getPatterns()){
					Row row = sheet.createRow(rowLine);
					if (start){
						addCell(row, 1, patternSet.getName(), getCellStylePlainReference(rowLine));
						start = false;
					}
					else{
						addEmptyCell(row, 1, getCellStylePlainReference(rowLine));
					}
					
					if (discoveryStart){
			   			addCell(row, 2, pattern.getDisplayName(), getCellStylePlainReference(rowLine));
			   			discoveryStart = false;
					}
					else{
						addEmptyCell(row, 2, getCellStylePlainReference(rowLine));
					}

		   			addCell(row, 3, code, getCellStylePlainReference(rowLine));
		   			addEmptyCell(row, 4, getCellStylePlainReference(rowLine));
		   			addEmptyCell(row, 5, getCellStylePlainReference(rowLine));
					rowLine++;
					itemsCount++;
				}
			}
		}
		
		return rowLine;
	}

	private int displayEmptyPatterns(Sheet sheet, ConfigSinglePatterns patternSet, int rowLine) {
		Row row = sheet.createRow(rowLine);
		
		addCell(row, 1, patternSet.getName(), getCellStylePlainReference(rowLine));
		addCell(row, 2, "(None)", getCellStylePlainReference(rowLine));
		addCell(row, 3, "(None)", getCellStylePlainReference(rowLine));
		addEmptyCell(row, 4, getCellStylePlainReference(rowLine));
		addEmptyCell(row, 5, getCellStylePlainReference(rowLine));
		rowLine++;
		
		return rowLine;
	}

	@SuppressWarnings("unchecked")
	private int displayPatterns(Sheet sheet, String type, int rowLine) {
		for (ConfigSinglePatterns patternSet : this.tdPatternsCfg.getPatternSets()){
			Map<String, Pattern> patternMap;
			if (ConfigPatterns.JZRA_EXCLUDE.equals(type)){
				patternMap = new LinkedHashMap<String, Pattern>();
				patternMap.putAll((Map<String, Pattern>)patternSet.getValue(ConfigPatterns.JZRA_EXCLUDE));
				patternMap.putAll((Map<String, Pattern>)patternSet.getValue(ConfigPatterns.JZRA_EXCLUDE_THREAD_NAME));
			}
			else if (ConfigPatterns.JZRA_EXECUTOR.equals(type)){
				patternMap = new LinkedHashMap<String, Pattern>();
				patternMap.putAll((Map<String, Pattern>)patternSet.getValue(ConfigPatterns.JZRA_EXECUTOR));
				patternMap.putAll((Map<String, Pattern>)patternSet.getValue(ConfigPatterns.JZRA_EXECUTOR_THREAD_NAME));
			}
			else{
				patternMap = (Map<String, Pattern>)patternSet.getValue(type);
			}

			Set<String> keys = patternMap.keySet();
			
			if (keys.isEmpty()){
				rowLine = displayEmptyPatterns(sheet, patternSet, rowLine);
				continue;
			}

			boolean start = true;
			for (String key : keys){
				Pattern pattern = patternMap.get(key);
				for (String code : pattern.getPatterns()){
					Row row = sheet.createRow(rowLine);
					if (start){
						addCell(row, 1, patternSet.getName(), getCellStylePlainReference(rowLine));
						start = false;
					}
					else{
						addEmptyCell(row, 1, getCellStylePlainReference(rowLine));
					}
		   			addCell(row, 2, pattern.getDisplayName(), getCellStylePlainReference(rowLine));
		   			addCell(row, 3, code, getCellStylePlainReference(rowLine));
		   			addEmptyCell(row, 4, getCellStylePlainReference(rowLine));
		   			if (pattern.getType() != null)
		   				addCell(row, 5, pattern.getType(), getCellStylePlainReference(rowLine));
		   			else
		   				addEmptyCell(row, 5, getCellStylePlainReference(rowLine));
					rowLine++;
					itemsCount++;
				}
			}
		}
		
		return rowLine;
	}

	private int displayPatternsContent(Sheet sheet, int rowLine) {
		for (ConfigSinglePatterns patternSet : this.tdPatternsCfg.getPatternSets()){
			displayPatternContentValue(sheet, patternSet.getName(), "Functions", 
					patternSet.getDistinctPatternCount(ConfigPatterns.JZRA_FUNCTION), rowLine++);
			displayPatternContentValue(sheet, "", "Discovery Functions", 
					patternSet.getDistinctPatternCount(ConfigPatterns.JZRA_DISCOVERY_FUNCTION), rowLine++);
			displayPatternContentValue(sheet, "", "Operations",
					patternSet.getDistinctPatternCount(ConfigPatterns.JZRA_OPERATION), rowLine++);
			displayPatternContentValue(sheet, "", "Discovery Operations", 
					patternSet.getDistinctPatternCount(ConfigPatterns.JZRA_DISCOVERY_OPERATION), rowLine++);
			displayPatternContentValue(sheet, "", "Lockers", 
					patternSet.getDistinctPatternCount(ConfigPatterns.JZRA_LOCKER), rowLine++);
			displayPatternContentValue(sheet, "", "Executors", 
					patternSet.getDistinctPatternCount(ConfigPatterns.JZRA_EXECUTOR) + patternSet.getDistinctPatternCount(ConfigPatterns.JZRA_EXCLUDE_THREAD_NAME), rowLine++);
			displayPatternContentValue(sheet, "", "Excludes",
					patternSet.getDistinctPatternCount(ConfigPatterns.JZRA_EXCLUDE) + patternSet.getDistinctPatternCount(ConfigPatterns.JZRA_EXCLUDE_THREAD_NAME), rowLine++);
		}
		return rowLine;
	}

	private void displayPatternContentValue(Sheet sheet, String name, String type, int count, int rowLine) {
		Row row = sheet.createRow(rowLine);
		addCell(row, 1, name, getCellStylePlainReference(rowLine));
		addCell(row, 2, type, getCellStylePlainReference(rowLine));
		addCell(row, 3, count, getCellStylePlainReference(rowLine));
		addEmptyCell(row, 4, getCellStylePlainReference(rowLine));
		addEmptyCell(row, 5, getCellStylePlainReference(rowLine));
	}

	private int displayProfileSourceVersions(Sheet sheet, int rowLine) {
		int firstRow = rowLine;
		for (ConfigSinglePatterns patterns : this.tdPatternsCfg.getPatternSets()){
			Row row = sheet.createRow(rowLine);
			addCell(row, 1, patterns.getName(), getCellStylePlainReference(rowLine));
			addCell(row, 2, patterns.getVersion(), getCellStylePlainReference(rowLine));
			addCell(row, 3, !patterns.isDynamic()? "yes":"no", getCellStylePlainReference(rowLine));
			addCell(row, 4, firstRow == rowLine ? "Master profile" : "Shared profile",  getCellStylePlainReference(rowLine));
			addEmptyCell(row, 5, getCellStylePlainReference(rowLine));
			rowLine++;
		}
		return rowLine;
	}

	private void displayCategoryHeader(Sheet sheet, String name1, String name2, String name3, String name4, String name5, int rowLine) {
    	Row row = sheet.createRow(rowLine);
    	short height = 600;
    	row.setHeight(height);
    	
    	addHeaderCell(row, 1, name1, STYLE_THEME_SECTION_HEADER_BOLD);
    	addHeaderCell(row, 2, name2, STYLE_THEME_SECTION_HEADER);
    	addHeaderCell(row, 3, name3, STYLE_THEME_SECTION_HEADER);
    	addHeaderCell(row, 4, name4, STYLE_THEME_SECTION_HEADER);
    	addHeaderCell(row, 5, name5, STYLE_THEME_SECTION_HEADER);
	}

}
