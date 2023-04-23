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




import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.*;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_TOP_BAR_JEYZER_TITLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.jeyzer.analyzer.config.report.ConfigMonitoringRulesSheet;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.output.poi.context.MonitoringRepository;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.info.Scope;
import org.jeyzer.monitor.engine.rule.MonitorRule;
import org.jeyzer.monitor.engine.rule.threshold.MonitorTaskThreshold;
import org.jeyzer.monitor.engine.rule.threshold.MonitorThreshold;

public class MonitoringRulesSheet extends JeyzerSheet {

	private ConfigMonitoringRulesSheet sheetCfg;
	
	public MonitoringRulesSheet(ConfigMonitoringRulesSheet config, JzrSession session, DisplayContext displayContext) {
		super(session, displayContext);
		this.sheetCfg = config;
	}

	@Override
	public void display() throws JzrException {
    	int linePos = 1;
    	int rowPos=0;
    	Sheet sheet = createSheet(this.sheetCfg);
    	
    	if (this.sheetCfg.isRuleRefListingKey()) {
    		// prepare the list of rules, indexed by rule reference
    		Map<String, MonitorRule> rules = new HashMap<>();
    		Map<String, Set<String>> sheetsPerRule = new HashMap<>();
    		int maxSheets = fillRuleAndSheetNames(rules, sheetsPerRule);
    		
        	rowPos = displayHeaders(sheet, linePos, rowPos, maxSheets);
        	linePos++;
        	linePos = displayRulesPerRuleRef(sheet, linePos, new ArrayList<>(rules.values()), sheetsPerRule, maxSheets);
        	
    		sheet.createFreezePane(4, 2); // left and top, add rule ref
    	}
    	else {
        	rowPos = displayHeaders(sheet, linePos, rowPos, -1);
        	linePos++;
        	linePos = displayRulesPerSheet(sheet, linePos);	
    		sheet.createFreezePane(3, 2); // left and top
    	}
		
		sheet.setAutoFilter(new CellRangeAddress(1, linePos-1, 0, rowPos - 1));
		
    	addTopBar(sheet, rowPos);
    	addMenuLink(sheet, sheetCfg, STYLE_THEME_TOP_BAR_JEYZER_TITLE, 0, 0);
    	
    	close(this.sheetCfg);
	}

	private int fillRuleAndSheetNames(Map<String, MonitorRule> rules, Map<String, Set<String>> sheetsPerRule) {
		int maxSheets = 1;

		MonitoringRepository repository = this.displayContext.getMonitoringRepository();
		for (String ruleSetId : repository.getRuleSetIds()){
			List<MonitorRule> candidates = repository.getRules(ruleSetId);
			Collection<String> sheetNameCandidates = repository.getSheetNames(ruleSetId);
			
			for (MonitorRule candidate: candidates) {
				rules.putIfAbsent(candidate.getRef(), candidate);
				Set<String> sheets = sheetsPerRule.get(candidate.getRef());
				if (sheets == null) {
					sheets = new TreeSet<>();
					sheetsPerRule.put(candidate.getRef(), sheets);
				}
				for (String sheetName : sheetNameCandidates)
					sheets.add(sheetName);
				
				// keep track of the max number of sheets for the display
				if (sheets.size() > maxSheets)
					maxSheets = sheets.size();
			}
		}
		
		return maxSheets;
	}

	private int displayRulesPerSheet(Sheet sheet, int linePos) {
		MonitoringRepository repository = this.displayContext.getMonitoringRepository();
		
		for (String ruleSetId : repository.getRuleSetIds()){
			List<MonitorRule> rules = repository.getRules(ruleSetId);
			Collection<String> sheetNames = repository.getSheetNames(ruleSetId);
			
			// sort per rule id, within the sheet
			rules.sort(new MonitorRule.MonitorRuleComparable());
			
			for (MonitorRule rule : rules){
				// sort per threshold id
				List<MonitorThreshold> thresholds = rule.getThresholds();
				thresholds.sort(new MonitorThreshold.MonitorThresholdComparable());
				for (MonitorThreshold threshold : thresholds){
					displayThreshold(sheet, linePos++, sheetNames, rule, threshold, -1);
					this.itemsCount++;
				}
			}
		}
		
		return linePos;
	}

	private void displayThreshold(Sheet sheet, int linePos, Collection<String> sheetNames, MonitorRule rule, MonitorThreshold threshold, int maxSheets) {
		int rowPos = 0;
		Row row = sheet.createRow(linePos);
		
		// hit, give link to first available sheet or sticker disabled rule, grey cell
		displayHit(row, rowPos++, sheetNames.iterator().next(), rule, threshold);

    	if (this.sheetCfg.isRuleRefListingKey())
    		// rule and threshold ref
    		addCell(row, rowPos++, rule.getRef() + threshold.getRef(), STYLE_CELL_CENTERED_WRAPPED);
		
		// rule name
		addCell(row, rowPos++, rule.getName(), STYLE_CELL_CENTERED_WRAPPED);
    	
		// level
		addCell(row, rowPos++, threshold.getLevel().toString(), STYLE_CELL_CENTERED);
		
		// sub level
		addCell(row, rowPos++, threshold.getSubLevel().value(), STYLE_CELL_CENTERED, HorizontalAlignment.CENTER);
		
		// scope
		addCell(row, rowPos++, threshold.getScope().toString(), STYLE_CELL_CENTERED);
		
		// match type
		addCell(row, rowPos++, threshold.getMatchType().toString(), STYLE_CELL_CENTERED);
		
		// rule condition description
		addCell(row, rowPos++, rule.getConditionDescription(), STYLE_CELL_SMALL_TEXT_WRAPPED);
		
		// advanced
		addCell(row, rowPos++, rule.isAdvancedMonitoringBased()? "Y" : "N", STYLE_CELL_CENTERED);
		
		// value, pattern, signal..
		addCell(row, rowPos++, threshold.getDisplayCondition(), STYLE_CELL_SMALL_TEXT_WRAPPED);
		
		// count
		displayCount(row, rowPos++, threshold);
			
		// time
		displayTime(row, rowPos++, threshold);

		// percentage
		displayPercentage(row, rowPos++, threshold);
		
		// function
		displayFunction(row, rowPos++, threshold);

		// message
		addCell(row, rowPos++, threshold.getMessage(), STYLE_CELL_SMALL_TEXT_WRAPPED);
		
		// narrative
		addCell(row, rowPos++, rule.getNarrative(), STYLE_CELL_SMALL_TEXT_WRAPPED);
		
		// sticker disabled
		addCell(row, rowPos++, rule.isEnabled() ? "Y":"N", STYLE_CELL_CENTERED);
		
		// trust factor
		displayTrustFactor(row, rowPos++, threshold);
		
		// stickers
		displayStickers(row, rowPos++, rule);
		
		// sheet
    	if (this.sheetCfg.isRuleRefListingKey()) {
    		int count = 0;
    		Iterator<String> sheetNameIter = sheetNames.iterator();
    		while(sheetNameIter.hasNext()) {
        		displaySheetName(row, rowPos++, sheetNameIter.next());
        		count++;
    		}
    		for (int i=count; i<maxSheets; i++)
    			addEmptyCell(row, rowPos++); // fill the rest
    	} else {
    		displaySheetNames(row, rowPos++, sheetNames);
    	}
		
		// rule group
		addCell(row, rowPos++, rule.getGroup(), STYLE_CELL_CENTERED);
		
		// rule source
		addCell(row, rowPos++, rule.getSource(), STYLE_CELL_CENTERED);
		
		// rule loading
		addCell(row, rowPos++, rule.isDynamic() ? "Dynamic" : "Static", STYLE_CELL_CENTERED);
		
    	if (this.sheetCfg.isSheetListingKey())
    		// rule and threshold ref
    		addCell(row, rowPos++, rule.getRef() + threshold.getRef(), STYLE_CELL_CENTERED_WRAPPED);
		
		// rule ticket
		addCell(row, rowPos++, rule.getTicket(), STYLE_CELL_CENTERED_WRAPPED);
		
		// rule id, for debug purposes
		addCell(row, rowPos++, rule.getId(), STYLE_CELL_CENTERED, HorizontalAlignment.CENTER);
	}

	private void displayStickers(Row row, int rowPos, MonitorRule rule) {
		List<String> stickers = rule.getStickerRefs();
		String value = stickers == null ? "" : stickers.isEmpty() ? "" : stickers.toString().substring(1, stickers.toString().length()-1);
		addCell(row, rowPos, value, STYLE_CELL_SMALL_TEXT_WRAPPED);
	}

	private void displayPercentage(Row row, int rowPos, MonitorThreshold threshold) {
		if (threshold.isPercentageBased()){
			MonitorTaskThreshold taskThreshold = (MonitorTaskThreshold)threshold;
			addCell(row, rowPos, taskThreshold.getPercentageInAction(), STYLE_CELL_CENTERED, HorizontalAlignment.CENTER);
		}
		else{
			addEmptyCell(row, rowPos);
		}
	}

	private void displaySheetNames(Row row, int rowPos, Collection<String> sheetNames) {
		Iterator<String> iter = sheetNames.iterator(); 
		String value = iter.next();
		while (iter.hasNext()){
			value += "\n" + iter.next();
		}
		Cell cell = addCell(row, rowPos, value, STYLE_CELL_CENTERED_WRAPPED);
		// give access to the first available sheet
		CellReference cellRef = new CellReference(sheetNames.iterator().next(), 0, 0, true, true);
		addDocumentHyperLink(cell, cellRef.formatAsString());
	}
	
	private void displaySheetName(Row row, int rowPos, String sheetName) {
		Cell cell = addCell(row, rowPos, sheetName, STYLE_CELL_CENTERED_WRAPPED);
		// give access to the sheet
		CellReference cellRef = new CellReference(sheetName, 0, 0, true, true);
		addDocumentHyperLink(cell, cellRef.formatAsString());
	}

	private void displayTime(Row row, int rowPos, MonitorThreshold threshold) {
		if (threshold.getTime() != -1)
			addCell(row, rowPos, threshold.getTime(), STYLE_CELL_CENTERED, HorizontalAlignment.CENTER);
		else
			addEmptyCell(row, rowPos);
	}

	private void displayCount(Row row, int rowPos, MonitorThreshold threshold) {
		if (threshold.getCount() != -1)
			addCell(row, rowPos, threshold.getCount(), STYLE_CELL_CENTERED, HorizontalAlignment.CENTER);
		else
			addEmptyCell(row, rowPos);
	}

	private void displayHit(Row row, int rowPos, String sheetName, MonitorRule rule, MonitorThreshold threshold) {
		if (threshold.getHit() > 0){
			Cell cell = addCell(row, rowPos, (long)threshold.getHit(), STYLE_CELL_GREEN_BACKGROUND_CENTERED, HorizontalAlignment.CENTER);
			// add link to sheet
			CellReference cellRef = new CellReference(sheetName, 0, 0, true, true);
			addDocumentHyperLink(cell, cellRef.formatAsString());
		}
		else{
			addCell(row, rowPos, 0, rule.isEnabled() ? STYLE_CELL_CENTERED  : STYLE_CELL_GREY_SHADOW, HorizontalAlignment.CENTER);
		}
	}

	private void displayTrustFactor(Row row, int rowPos, MonitorThreshold threshold) {
		if (threshold.isFullyTrustable())
			addCell(row, rowPos, threshold.getTrustFactor(), STYLE_CELL_CENTERED , HorizontalAlignment.CENTER);
		else
			addCell(row, rowPos, threshold.getTrustFactor(), STYLE_CELL_ORANGE_BACKGROUND_CENTERED , HorizontalAlignment.CENTER);
	}

	private void displayFunction(Row row, int rowPos, MonitorThreshold threshold) {
		if (Scope.ACTION.equals(threshold.getScope())
				|| Scope.STACK.equals(threshold.getScope())){
			MonitorTaskThreshold taskThreshold = (MonitorTaskThreshold)threshold;
			if (taskThreshold.hasFunction())
				addCell(row, rowPos, taskThreshold.getFunction(), STYLE_CELL_CENTERED_WRAPPED);
			else
				addEmptyCell(row, rowPos);
		}
		else
			addEmptyCell(row, rowPos);
	}
	
	private int displayRulesPerRuleRef(Sheet sheet, int linePos, List<MonitorRule> rules, Map<String, Set<String>> sheetsPerRule, int maxSheets) {
		// sort per rule ref
		rules.sort(new MonitorRule.MonitorRuleRefComparable());
		
		for (MonitorRule rule : rules){
			List<MonitorThreshold> thresholds = rule.getThresholds();
			// sort per threshold id
			thresholds.sort(new MonitorThreshold.MonitorThresholdComparable());
			for (MonitorThreshold threshold : thresholds){
				displayThreshold(sheet, linePos++, sheetsPerRule.get(rule.getRef()), rule, threshold, maxSheets);
				this.itemsCount++;
			}
		}
		
		return linePos;
	}

	private int displayHeaders(Sheet sheet, int linePos, int rowPos, int maxSheets) {
    	// Header
    	Row row = sheet.createRow(linePos++);

    	sheet.setColumnWidth(rowPos, 8*256);
    	addHeaderCell(row, rowPos++, "Hit");
    	
    	if (this.sheetCfg.isRuleRefListingKey()) {
        	sheet.setColumnWidth(rowPos, 18*256);
        	addHeaderCell(row, rowPos++, "Rule ref");
    	}
    	
    	sheet.setColumnWidth(rowPos, 30*256);
    	addHeaderCell(row, rowPos++, "Rule name");

    	sheet.setColumnWidth(rowPos, 11*256);
    	addHeaderCell(row, rowPos++, "Level");
    	
    	sheet.setColumnWidth(rowPos, 8*256);
    	addHeaderCell(row, rowPos++, "SL");
    	
    	sheet.setColumnWidth(rowPos, 12*256);
    	addHeaderCell(row, rowPos++, "Scope");
    	
    	sheet.setColumnWidth(rowPos, 16*256);
    	addHeaderCell(row, rowPos++, "Match type");
    	
    	sheet.setColumnWidth(rowPos, 50*256);
    	addHeaderCell(row, rowPos++, "Condition");
    	
    	sheet.setColumnWidth(rowPos, 10*256);
    	addHeaderCell(row, rowPos++, "Adv");
    	
    	sheet.setColumnWidth(rowPos, 26*256);
    	addHeaderCell(row, rowPos++, "Value / Pattern / Custom");

    	sheet.setColumnWidth(rowPos, 9*256);
    	addHeaderCell(row, rowPos++, "Count");
    	
    	sheet.setColumnWidth(rowPos, 9*256);
    	addHeaderCell(row, rowPos++, "Time");

    	sheet.setColumnWidth(rowPos, 9*256);
    	addHeaderCell(row, rowPos++, "Percent");
    	
    	sheet.setColumnWidth(rowPos, 25*256);
    	addHeaderCell(row, rowPos++, "Function");

    	sheet.setColumnWidth(rowPos, 60*256);
    	addHeaderCell(row, rowPos++, "Message");
    	
    	sheet.setColumnWidth(rowPos, 80*256);
    	addHeaderCell(row, rowPos++, "Rule narrative");
    	
    	sheet.setColumnWidth(rowPos, 8*256);
    	addHeaderCell(row, rowPos++, "Active");
    	
    	sheet.setColumnWidth(rowPos, 9*256);
    	addHeaderCell(row, rowPos++, "Trust");
    	
    	sheet.setColumnWidth(rowPos, 25*256);
    	addHeaderCell(row, rowPos++, "Stickers");

    	if (this.sheetCfg.isRuleRefListingKey()) {
        	for (int i=1; i<maxSheets+1; i++) {
            	sheet.setColumnWidth(rowPos, 30*256);
            	addHeaderCell(row, rowPos++, "Sheet " + i);
            }
    	}
    	else {
        	sheet.setColumnWidth(rowPos, 30*256);
        	addHeaderCell(row, rowPos++, "Sheet");
    	}

    	sheet.setColumnWidth(rowPos, 30*256);
    	addHeaderCell(row, rowPos++, "Rule group");
    	
    	sheet.setColumnWidth(rowPos, 30*256);
    	addHeaderCell(row, rowPos++, "Rule source");
    	
    	sheet.setColumnWidth(rowPos, 13*256);
    	addHeaderCell(row, rowPos++, "Loading");
    	
    	if (this.sheetCfg.isSheetListingKey()) {
        	sheet.setColumnWidth(rowPos, 18*256);
        	addHeaderCell(row, rowPos++, "Rule ref");
    	}
    	
    	sheet.setColumnWidth(rowPos, 11*256);
    	addHeaderCell(row, rowPos++, "Rule ticket");
    	
    	sheet.setColumnWidth(rowPos, 11*256);
    	addHeaderCell(row, rowPos++, "Rule id");
    	
		return rowPos;
	}
}
