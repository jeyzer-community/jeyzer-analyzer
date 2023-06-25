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


import static org.jeyzer.analyzer.output.poi.style.CellFonts.FONT_DOUBLE_UNDERLINE_SYMBOL_9;
import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.*;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.charts.AxisCrosses;
import org.apache.poi.ss.usermodel.charts.AxisPosition;
import org.apache.poi.ss.usermodel.charts.ChartAxis;
import org.apache.poi.ss.usermodel.charts.ChartDataSource;
import org.apache.poi.ss.usermodel.charts.ChartLegend;
import org.apache.poi.ss.usermodel.charts.DataSources;
import org.apache.poi.ss.usermodel.charts.LegendPosition;
import org.apache.poi.ss.usermodel.charts.LineChartSeries;
import org.apache.poi.ss.usermodel.charts.ValueAxis;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.charts.XSSFLineChartData;
import org.jeyzer.analyzer.config.report.ConfigChart;
import org.jeyzer.analyzer.config.report.ConfigMonitoringSequenceSheet;
import org.jeyzer.analyzer.config.report.ConfigMonitoringSheet;
import org.jeyzer.analyzer.config.report.ConfigSheet;
import org.jeyzer.analyzer.config.report.headers.ConfigSheetAbstractHeaders.HEADER_FREEZE_MODE;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.TimeZoneInfo;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrMonitorException;
import org.jeyzer.analyzer.output.poi.CellText;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.output.poi.context.SequenceSheetDisplayContext;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.context.DisplayContext.MenuItemsContext;
import org.jeyzer.analyzer.output.poi.rule.header.AbstractNumericDisplayHeaderRule;
import org.jeyzer.analyzer.output.poi.rule.header.Header;
import org.jeyzer.analyzer.output.poi.rule.header.HeaderBuilder;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunctionBuilder;
import org.jeyzer.analyzer.output.poi.rule.header.function.NotSupportedFunction;
import org.jeyzer.analyzer.output.poi.rule.monitor.cell.MonitorDisplayRule;
import org.jeyzer.analyzer.output.poi.rule.monitor.cell.MonitorDisplayRuleBuilder;
import org.jeyzer.analyzer.output.poi.rule.monitor.row.MonitorRowHeader;
import org.jeyzer.analyzer.output.poi.rule.monitor.row.MonitorRowHeaderBuilder;
import org.jeyzer.analyzer.output.poi.style.CellFonts;
import org.jeyzer.analyzer.output.stats.CollectedStats;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.analyzer.util.AnalyzerHelper;
import org.jeyzer.monitor.engine.event.MonitorEvent;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.Level;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;
import org.jeyzer.monitor.sticker.Sticker;
import org.jeyzer.monitor.util.MonitorHelper;

import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTMarker;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTMarkerStyle;
import org.openxmlformats.schemas.drawingml.x2006.chart.STMarkerStyle;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

public class MonitoringSequenceSheet extends JeyzerSheet {
	
	public static final int ACTIVITY_COLUMN_LARGE_WIDTH = 32;
	public static final int ACTIVITY_COLUMN_SHORT_WIDTH = 14;
	
	private ConfigMonitoringSequenceSheet sheetCfg;
	private Map<String,Cell> headerTitleCells = new HashMap<>();
	private boolean criticalAlert = false;
		
	public MonitoringSequenceSheet(ConfigMonitoringSequenceSheet sheetCfg, JzrSession session, DisplayContext displayContext){
		super(session, displayContext);
		this.sheetCfg = sheetCfg;
	}
	
	@Override
	public void display() {
		List<MonitorEvent> events;

		try{
			events = buildEvents();
		}
		catch(JzrException ex){
			logger.error("Failed to generate the " + sheetCfg.getName() + " sheet.", ex);
			return;
		}
		Multimap<Date, MonitorEvent> eventHistory = getEventHistory(events);
		
		Sheet sheet = createSheet(this.sheetCfg);
		
		HeaderBuilder headerBuilder = HeaderBuilder.newInstance();
		List<Header> headers = headerBuilder.buildHeaders(sheetCfg.getHeaderConfigsSets(), new SheetDisplayContext(displayContext, sheet), session);
		
		HeaderFunctionBuilder functionBuilder = HeaderFunctionBuilder.newInstance();
		Map<String,HeaderFunction> functionsPerHeader = functionBuilder.buildHeaderFunctions(sheetCfg.getHeaderConfigsSets(), headers, session);
		List<HeaderFunction> modelFunctions = functionBuilder.buildHeaderModelFunctions(sheetCfg.getHeaderConfigsSets(), headers, session);
		
		MonitorRowHeaderBuilder rowHeaderBuilder = MonitorRowHeaderBuilder.newInstance();
		List<MonitorRowHeader> rowHeaders = rowHeaderBuilder.buildRowHeaders(sheetCfg.getRowHeadersConfig(), new SheetDisplayContext(displayContext, sheet), session);
		
		MonitorDisplayRuleBuilder displayBuilder = MonitorDisplayRuleBuilder.newInstance();
		CollectedStats statsModel = new CollectedStats(session.getActionsSize(), session.getActionsStackSize());
		int tdPeriod = session.getThreadDumpPeriod();
		List<MonitorDisplayRule> rules = displayBuilder.buildRules(sheetCfg.getDisplayConfigs(), new SequenceSheetDisplayContext(displayContext, sheet, statsModel, tdPeriod));
		
		prepareSheet(sheet, getRowFreezeHeaderSize(rowHeaders), getFreezeHeaderSize(headers, rules));

		int columnOffset = getHeaderRowOffset(rowHeaders, modelFunctions);
		
		reportTaskSequenceHeader(sheet, rowHeaders, modelFunctions, rules.isEmpty(), columnOffset);
		
		if (!headers.isEmpty())
			fillDynamicHeaders(sheet, headers, functionsPerHeader, columnOffset);
		
		if (sheetCfg.isLinkable())
			prepareColumnLinks(columnOffset);
		
		if (!rules.isEmpty())
			fillData(sheet, rules, eventHistory, rowHeaders, headers.size(), columnOffset);
		
		updateTimeHeaderCells(sheet, rules, eventHistory, headers.size(), columnOffset);

		if (!headers.isEmpty() && !rules.isEmpty())
			groupDynamicHeaders(sheet, headers);
		
		int line = headers.size() + events.size();
		if (!headers.isEmpty()){
			line = adjustChartsStartLine(rules, headers, line);
			displayCharts(sheet, headers, columnOffset, line);
		}
		
		addMenuLink(sheet, sheetCfg, STYLE_THEME_TOP_BAR_JEYZER_TITLE, 0, 0);
		
		if (criticalAlert && sheetCfg.getMonitoringConfig().getCriticalColor() != null)
    		((XSSFSheet)sheet).setTabColor(sheetCfg.getMonitoringConfig().getCriticalColor());
		
		this.itemsCount = rules.isEmpty() ? headers.size() : events.size();
		close(this.sheetCfg);
	}
	
	@Override
	protected void close(ConfigSheet cfgSheet){
		super.close(cfgSheet);
		
		if (criticalAlert && sheetCfg.getMonitoringConfig().getCriticalColor() != null) {
			// Update the menu item with the criticality
			MenuItemsContext menuItem = this.displayContext.getMenuItems().get(cfgSheet.getName());
			menuItem.setCriticalColor(sheetCfg.getMonitoringConfig().getCriticalColor());
		}
	}

	private Multimap<Date, MonitorEvent> getEventHistory(List<MonitorEvent> events) {
		ListMultimap<Date, MonitorEvent> history = ArrayListMultimap.create();
		
		for (MonitorEvent event : events)
			history.put(event.getStartDate(), event);
		
		return history;
	}

	private int getHeaderRowOffset(List<MonitorRowHeader> rowHeaders, List<HeaderFunction> modelFunctions) {
		if (modelFunctions.isEmpty())
			return rowHeaders.size();
		if (rowHeaders.size() > modelFunctions.size() + 2)
			return rowHeaders.size();
		else
			return modelFunctions.size() + 2;
	}

	private int adjustChartsStartLine(List<MonitorDisplayRule> rules, List<Header> headers, int line) {
		int lastActionLine = getLastEventLine(rules, headers.size());
		if (lastActionLine > line)
			return lastActionLine;
		else
			return line;
	}

	private int getLastEventLine(List<MonitorDisplayRule> rules, int headerOffset) {
		return 2 + ((!rules.isEmpty()) ? this.session.getActionsSize() : 0)  + headerOffset;
	}

	private void displayCharts(Sheet sheet, List<Header> headers, int rowHeaderSize, int line) {
		List<ConfigChart> chartConfigs = this.sheetCfg.getChartConfigs();
		
		if (chartConfigs.isEmpty())
			return;
		
		final int CHART_HEIGHT = 20;
		final int CHART_HEIGHT_SEP = 3;
		
		int anchorPosX1 = (rowHeaderSize>2) ? rowHeaderSize-2 : 0;
		int anchorPosY1 = line + CHART_HEIGHT_SEP;
		int anchorPosX2 = anchorPosX1 + 2; // 2 is size of left legend
		anchorPosX2 = (anchorPosX2 - anchorPosX1) > EXCEL_CHART_MAX_LENGTH ? EXCEL_CHART_MAX_LENGTH : anchorPosX2;
		int anchorPosY2 = anchorPosY1 + CHART_HEIGHT;

		for (ConfigChart chartConfig : chartConfigs){

			if (!isValidSerieFound(chartConfig, headers))
				continue;
			
	        XSSFDrawing drawing = ((XSSFSheet)sheet).createDrawingPatriarch();
	        XSSFClientAnchor anchor = drawing.createAnchor(
	        		0, 0, 0, 0, // this is dx and dy coordnates within the cells defined with anchorPos
	        		anchorPosX1, anchorPosY1, anchorPosX2, anchorPosY2);

	        XSSFChart chart = drawing.createChart(anchor);
	        ChartLegend legend = chart.getOrCreateLegend();
	        legend.setPosition(LegendPosition.LEFT);
	        
	        XSSFLineChartData lineChartData = null;
    		// LineChartSeries values are equally displayed on X axis (time/tick based)
    		// ScatterChartSeries allows to display data distributions (cloud) : X values are taken into account
        	lineChartData = chart.getChartDataFactory().createLineChartData();
	        
	        // Use a category axis for the bottom axis.
	        ChartAxis bottomAxis = chart.getChartAxisFactory().createCategoryAxis(AxisPosition.BOTTOM);
	        ValueAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);
	        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
	        if (chartConfig.getYAxisMax() != null)
	        	leftAxis.setMaximum(chartConfig.getYAxisMax());
	        
	        addSeries(sheet, headers, chartConfig, lineChartData, rowHeaderSize);

	        // display data
        	chart.plot(lineChartData, bottomAxis, leftAxis);

        	customizeChartRendering(chart);

	        // display title and bottom bar
	        displayChartBottomBar(sheet, chartConfig, anchorPosX1, anchorPosX2, anchorPosY2, headers);
	        updateHeaderTitles(anchorPosX1,anchorPosY2, chartConfig);
        	
	        anchorPosY1 = anchorPosY2 + CHART_HEIGHT_SEP;
	        anchorPosY2 = anchorPosY1 + CHART_HEIGHT;
	        
	        this.graphItemsCount++;
		}
		
	}

	private void updateHeaderTitles(int x, int y, ConfigChart chartConfig) {
		for (String serie : chartConfig.getSeries()){
			// find header cell
			Cell cell = this.headerTitleCells.get(serie);
			if (cell == null)
				continue;

			// add suffix symbol
			String headerName = cell.getStringCellValue() + " ";
			String symbol = getChartSymbol();
			
			XSSFRichTextString newHeaderName = new XSSFRichTextString(headerName);
			newHeaderName.append(symbol, this.getConstantXSSFont(CellFonts.FONT_SYMBOL_BOLD_10));
			
			cell.setCellValue(newHeaderName);
			
			// add chart link
			CellReference chartCellRef = new CellReference(
					this.sheetCfg.getName(), 
					y,
					x,
					true, 
					true);
			addDocumentHyperLink(cell, chartCellRef.formatAsString());
		}
	}

	private String getChartSymbol() {
		int index = (this.graphItemsCount >= CellText.getFontSymbolCharsLength()) ? this.graphItemsCount % CellText.getFontSymbolCharsLength() : this.graphItemsCount;
		return CellText.getFontSymbolChar(index);
	}

	private void displayChartBottomBar(Sheet sheet, ConfigChart chartConfig, int xStart, int xEnd, int line, List<Header> headers) {
		Row row = getValidRow(sheet, line);

		boolean found = false;
		String topHeader = null;
        for (Header header : headers) {
        	for (String serie : chartConfig.getSeries()) {
	        	if (header.getName().equals(serie)){
	        		topHeader = header.getName();
	        		found = true;
	        		break;
	        	}
	        }
        	if (found)
        		break;
        }
		
        Cell topCell = null;
        if (topHeader != null)
        	topCell = this.headerTitleCells.get(topHeader);
		
		// link to top header cell
		CellReference parentCellRef = new CellReference(
				this.sheetCfg.getName(), 
				topCell != null ? topCell.getRowIndex() : 0,
				topCell != null ? topCell.getColumnIndex() : 0,
				true, 
				true);
		addCell(row, xStart, CellText.FONT_SYMBOL_UP_ARROW, parentCellRef, STYLE_CELL_GREY_SHADOW, this.getConstantFont(FONT_DOUBLE_UNDERLINE_SYMBOL_9));
		
		// title (optional)
		if (chartConfig.getTitle() != null){
			XSSFRichTextString title = new XSSFRichTextString(getChartSymbol());
			title.applyFont(this.getConstantXSSFont(CellFonts.FONT_SYMBOL_BOLD_11));
			title.append("  " + chartConfig.getTitle(), this.getConstantXSSFont(CellFonts.FONT_11));
			addCell(row, xStart+1, title, STYLE_CELL_GREY_SHADOW);
		}
		else{
			String symbolTitle = getChartSymbol();
			addCell(row, xStart+1, symbolTitle, STYLE_CELL_GREY_SHADOW, this.getConstantXSSFont(CellFonts.FONT_SYMBOL_BOLD_11));
		}
		
		for (int pos=xStart+2 ; pos<xEnd; pos++){
			addEmptyCell(row, pos, STYLE_CELL_GREY_SHADOW);
		}
	}

	private boolean isValidSerieFound(ConfigChart chartConfig, List<Header> headers) {
		// validate that at least one serie is valid
        for (String serie : chartConfig.getSeries()) {
	        for (Header header : headers) {
	        	if (header.getName().equals(serie)){
	        		return true;
	        	}
	        }
        }
        
        if (chartConfig.getTitle() != null)
        	logger.warn("Chart " + chartConfig.getTitle() + " cannot be displayed : no valid series found.");

		return false;
	}

	private void addSeries(Sheet sheet, List<Header> headers, ConfigChart chartConfig, XSSFLineChartData lineChartData, int rowHeaderSize) {
		int y1, y2;
		int x1 = rowHeaderSize;
		int x2 = rowHeaderSize - 1;
		x2 = (x2 - x1) > EXCEL_CHART_MAX_LENGTH ? EXCEL_CHART_MAX_LENGTH : x2;
		
        // data source X axis (dates)
        ChartDataSource<Number> xs = DataSources.fromNumericCellRange(sheet, new CellRangeAddress(1, 1, x1, x2));		
		
        for (String serie : chartConfig.getSeries()) {
        	int pos = 2; // header start
	        for (Header header : headers) {
	        	if (header.getName().equals(serie)){
	        		y1 = y2 = pos;
	        		// data source Y axis (numbers)
	        		ChartDataSource<Number> ys = DataSources.fromNumericCellRange(sheet, new CellRangeAddress(y1, y2, x1, x2));
        			LineChartSeries chartSerie = lineChartData.addSeries(xs, ys);
        			chartSerie.setTitle(header.getDisplayName());
	        	}
	        	pos++;
	        }
        }
	}

	private void customizeChartRendering(XSSFChart chart) {
    	// update the line rendering
        for (int i = 0; i<chart.getCTChart().getPlotArea().getLineChartArray(0).sizeOfSerArray(); i++ ){
	        	CTLineSer s = chart.getCTChart().getPlotArea().getLineChartArray(0).getSerArray(i);
	        	CTBoolean smooth = s.getSmooth();
	        	if (smooth == null)
	        		smooth = s.addNewSmooth(); 
	        	smooth.setVal(this.displayContext.getSetupManager().isGraphSoftLine());
    	}
    	
    	// update the dot rendering
	    String dotStyle = this.displayContext.getSetupManager().getGraphDotStyle();
    	if (dotStyle != null){
    		STMarkerStyle.Enum style = STMarkerStyle.Enum.forString(dotStyle.toLowerCase());
    		if (style == null)
    			//default
    			style = STMarkerStyle.NONE;
    		
	        for (int i = 0; i<chart.getCTChart().getPlotArea().getLineChartArray(0).sizeOfSerArray(); i++ ){
	        	CTLineSer s = chart.getCTChart().getPlotArea().getLineChartArray(0).getSerArray(i);
	        	CTMarker marker = s.getMarker();
	        	if (marker == null)
	        		marker = s.addNewMarker();
	        	CTMarkerStyle markerStyle =  marker.getSymbol();
	        	if (markerStyle == null)
	        		markerStyle = marker.addNewSymbol();
	        	markerStyle.setVal(style);
	        }
    	}
	}

	private void prepareSheet(Sheet sheet, int freezeRowHeaderSize, int freezeHeaderSize){
   		sheet.createFreezePane(freezeRowHeaderSize, freezeHeaderSize);
        sheet.setAutobreaks(true);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);
    }
	
	private int getFreezeHeaderSize(List<Header> headers, List<MonitorDisplayRule> rules) {
		if (headers.isEmpty())
			// no headers to display
			return 2;
		
		if (this.sheetCfg.getChartConfigs().isEmpty() && rules.isEmpty())
			// only headers are displayed, allow browsing it
			return 2;
		
		HEADER_FREEZE_MODE freezeMode = this.sheetCfg.getHeaderConfigsSets().getHeaderConfigs(session.getFormatShortName()).getFreezeMode();
		if (HEADER_FREEZE_MODE.FREEZE.equals(freezeMode)){
			// force freeze as per sheet configuration
    		// freeze first 2 rows + number of headers to display
			return 2 + headers.size();
		}
		if (HEADER_FREEZE_MODE.UNFREEZE.equals(freezeMode)){
			// force unfreeze as per sheet configuration
			return 2;
		}
		
		// let the system decide
		int threshold = this.displayContext.getSetupManager().getHeaderUnfreezePaneThreshold();
    	if (headers.size() >= threshold)
    		// large list of headers, let's allow its browsing
    		// not nice when coupled with charts : you see one or the other
    		return 2;
    	else
    		// interesting for small header lists and chart display to correlate both
    		// freeze first 2 rows + number of headers to display
    		return 2 + headers.size();
	}

	private int getRowFreezeHeaderSize(List<MonitorRowHeader> rowHeaders) {
		HEADER_FREEZE_MODE freezeMode = this.sheetCfg.getRowHeadersConfig().getFreezeMode();
		if (HEADER_FREEZE_MODE.FREEZE.equals(freezeMode)){
			// force freeze as per sheet configuration
    		// freeze number of row headers to display
			return rowHeaders.size();
		}
		if (HEADER_FREEZE_MODE.UNFREEZE.equals(freezeMode)){
			// force unfreeze as per sheet configuration
			return 0;
		}
		
		// let the system decide
		int threshold = this.displayContext.getSetupManager().getRowHeaderUnfreezePaneThreshold();
    	if (rowHeaders.size() >= threshold)
    		// large list of row headers, let's allow its browsing
    		return 0;
    	else
    		// freeze number of row headers to display
    		return rowHeaders.size();
	}
	
	private void reportTaskSequenceHeader(Sheet sheet, List<MonitorRowHeader> rowHeaders, List<HeaderFunction> functions, boolean headersOnly, int headerLabelsColumnSize){
		Row row0, row1;
		
		// On row 0, just display date every 14 cells
		row0 = sheet.createRow(0);
		
		for (int i=1; i<headerLabelsColumnSize; i++){
			addHeaderCell(row0, i, "", STYLE_THEME_TOP_BAR);
		}

		// On row 1, display recording time stamps
		row1 = sheet.createRow(1);
		
		int columnPos = 0;
		Cell cell;
		for (MonitorRowHeader rowHeader : rowHeaders){
			sheet.setColumnWidth(columnPos, rowHeader.getColumnWidth());
			cell = addHeaderCell(row1, columnPos, headersOnly? columnPos==rowHeaders.size()-1? "Measurement":"" :rowHeader.getDisplayName());
			if (!headersOnly && rowHeader.getComment() != null) {
				int deep = AnalyzerHelper.countLines(rowHeader.getComment());
				int length = AnalyzerHelper.countParagraphMaxLength(rowHeader.getComment());
				if (length > 0)
					addComment(sheet, cell, rowHeader.getComment(), deep, length / CellText.COMMENT_COLUMN_CHARS_SIZE);
			}
			columnPos++;
		}
		
		boolean doubleLabel = false;
		int columnFunctionPos = 2;
		// Set the header function names, append to row header names if any
		for (HeaderFunction function : functions){
			cell = row1.getCell(columnFunctionPos);
			if (cell != null)
				// update cell text
				if (cell.getStringCellValue() == null || cell.getStringCellValue().isEmpty())
					cell.setCellValue(function.getDisplayName());
				else{
					String crLf = Character.toString((char)13) + Character.toString((char)10);
					XSSFRichTextString richText = new XSSFRichTextString(function.getDisplayName() + crLf);
				    XSSFFont cellFont = this.styles.getCellFonts().getFont(CellFonts.FONT_6);
				    richText.append("--/--" + crLf, cellFont);
				    richText.append(cell.getStringCellValue());
				    cell.setCellValue(richText);
				    doubleLabel = true;
				}
			else
				// no row header here
				addHeaderCell(row1, columnFunctionPos, function.getDisplayName());
			
			columnFunctionPos++;
		}
		
		if (doubleLabel)
			row1.setHeightInPoints(40);
		
		// adjust column pos if more functions than row headers
		if (columnFunctionPos > columnPos)
			columnPos = columnFunctionPos;
		
		int columnWidth = session.getDumps().size() <= 4 ? ACTIVITY_COLUMN_LARGE_WIDTH : ACTIVITY_COLUMN_SHORT_WIDTH;
		
		for (ThreadDump dump : session.getDumps()){
			sheet.setColumnWidth(columnPos, columnWidth*256);
			fillDateHeaderCell(row0, columnPos, dump, headerLabelsColumnSize, false);  // date + time zone + up time (adv jmx) or empty cell
			addHeaderCell(row1, columnPos, formatDate(dump.getTimestamp(), TIME_DISPLAY_FORMAT), STYLE_THEME_HEADER_TIME); // time stamp cell
			
			columnPos++;
		}

	}

	private void groupDynamicHeaders(Sheet sheet, List<Header> headers) {
		sheet.groupRow(2, headers.size() + 1);
		sheet.setRowGroupCollapsed(2, false);
    }
	
    private void fillDynamicHeaders(Sheet sheet, List<Header> headers, Map<String, HeaderFunction> functionsPerHeader, int columnOffset) {
		int linePos=2; // start on second row
		int headerNamePos = this.sheetCfg.getHeaderConfigsSets().getHeaderConfigs(session.getFormatShortName()).getHeaderPos(columnOffset-1);
		
		for(Header header : headers){
			Row row = sheet.createRow(linePos);
			
			fillHeaderTitleSection(sheet, row, header, columnOffset, headerNamePos);
			
			List<Cell> cells = new ArrayList<>();
			List<ThreadDump> dumps = session.getDumps();
			
			for (int j=0; j< dumps.size(); j++){
				int colIndex = j + columnOffset;
				Cell  cell = row.createCell(colIndex);
				cell.setCellStyle(getStyle(header.getStyle()));
				
				cells.add(cell);
			}
			
			HeaderFunction function = functionsPerHeader.get(header.getName());
			if (function == null)
				function = new NotSupportedFunction(null);
			
			header.apply(dumps, cells, function);
		
			linePos++;
			
			if (functionsPerHeader.get(header.getName()) != null)
				fillFunctions(row, function, header);
		}
	}	
	
    private void fillFunctions(Row row, HeaderFunction function, Header header) {
    	List<Cell> cells = new ArrayList<>();
    	
    	for (int i=2; i<function.size()+2; i++){
    		Cell cell;
    		
    		if (header instanceof AbstractNumericDisplayHeaderRule){
    			if (((AbstractNumericDisplayHeaderRule)header).isPercentageBased())
    				cell = addHeaderCell(row, i, "", STYLE_THEME_DYNAMIC_HEADER_NUMBER_1_DECIMAL);
    			else
    				cell = addHeaderCell(row, i, "", STYLE_THEME_DYNAMIC_HEADER_NUMBER);
    			
    		}
    		else
    			cell = addHeaderCell(row, i, "", STYLE_THEME_DYNAMIC_HEADER); // empty one
    		cells.add(cell);
    	}

		function.display(workbook, cells.iterator(), header, this.displayContext.getCellStyles());
	}

	private void fillHeaderTitleSection(Sheet sheet, Row row, Header header, int rowHeaderSize, int headerNamePos) {
		int columnPos = 0;
		Cell cell;
    	
		for (int i=0; i<rowHeaderSize; i++){
			if (i == headerNamePos){
				cell = addHeaderCell(row, columnPos, header.getDisplayName(), STYLE_THEME_DYNAMIC_HEADER);
				if (header.getComment() != null){
					int deep = AnalyzerHelper.countLines(header.getComment()) + 1; // add one extra line in case 1 line is wrapped
					int maxLength = AnalyzerHelper.countParagraphMaxLength(header.getComment());
					if (maxLength > 0)
						addComment(sheet, cell, header.getComment(), deep, maxLength / CellText.COMMENT_COLUMN_CHARS_SIZE);
				}
				this.headerTitleCells.put(header.getName(), cell); // used by charts
			}
			else{
				addHeaderCell(row, columnPos, "", STYLE_THEME_DYNAMIC_HEADER);
			}
			columnPos++;
		}
	}

	private void fillData(Sheet sheet, List<MonitorDisplayRule> rules, Multimap<Date, MonitorEvent> eventHistory, List<MonitorRowHeader> rowHeaders, int rowOffset, int columnOffset){
		int offset = 0;
		Row row;
		Cell cell;
		List<Cell> cells;
    	
		int linePos= 2 + rowOffset; // start on second row + # headers
		
		for (ThreadDump dump : session.getDumps()){
			Date timestamp = dump.getTimestamp();
			Collection<MonitorEvent> events = eventHistory.get(timestamp);
			
			boolean firstEvent = true;
			for(MonitorEvent event : events){

				if (!criticalAlert && Level.CRITICAL.equals(event.getLevel()))
					criticalAlert = true;
				
				row = sheet.createRow(linePos);
				
				cells = new ArrayList<Cell>();
				
				int eventSize = getEventSize(event);
				
				for (int j=0; j< eventSize; j++){
					int colIndex = offset + j + columnOffset;
					cell = row.createCell(colIndex);
					cells.add(cell);
					prepareCellLinks(row, colIndex, event, timestamp, j==0, firstEvent);
				}
				
				for (MonitorDisplayRule rule : rules)
					rule.apply(event, cells);
				
				linePos++;
				
				firstEvent = false;
				
				// write row headers (ex : thread id, thread name..)
				// do it last as we need to access event links just previously created
				fillRowHeaderData(rowHeaders, event, row, columnOffset);
			}
			offset++;
		}

		// if some data was written, add filter on row headers
		if (linePos != 2 + rowOffset){
			sheet.setAutoFilter(new CellRangeAddress(1, linePos-1, 0, rowHeaders.size()-1));
		}
    }

	private void updateTimeHeaderCells(Sheet sheet, List<MonitorDisplayRule> rules, Multimap<Date, MonitorEvent> eventHistory, int rowOffset, int columnOffset){
		int offset = 0;
		Row row;
		Cell cell;
		
		row = sheet.getRow(1);
		
		for (ThreadDump dump : session.getDumps()){
			if (!eventHistory.get(dump.getTimestamp()).isEmpty()){
				// header time stamp must be updated with link to first event (if events are displayed)
				cell = row.getCell(offset + columnOffset);
				
				if (this.displayContext.getSetupManager().isHeaderActionHighlightEnabled())
					cell.setCellStyle(this.getThemeStyle(STYLE_THEME_HEADER_TIME_ACTION_START));
				
				if (!rules.isEmpty() && this.displayContext.getSetupManager().isHeaderActionLinkEnabled()){
					CellReference cellref = this.displayContext.getCellRefRepository().getRefColumn(sheetCfg.getLinkType(), dump.getTimestamp());
					addDocumentHyperLink(cell, cellref.formatAsString());
				}
			}
			offset++;
		}
    }	
	
    private int getEventSize(MonitorEvent event) {
    	int size = 0;
    	
		// not nice
    	boolean started = false;
    	int pos = -1;
		for (ThreadDump td : session.getDumps()){
			pos++;
			
			if (event.getStartDate().equals(td.getTimestamp())){
				// in progress event
				if (event.isInProgress())
					return session.getDumps().size() - pos;
				
				started = true;
			}
			
			if (started)
				size++;
			
			if (event.getEndDate().equals(td.getTimestamp())){
				return size;
			}
		}
		
		return size; // not expected
	}

	private void registerDateLink(Date date, Row row, int i) {
		if (!this.displayContext.getSetupManager().isHeaderDateLinkEnabled())
			return;
    	
		CellReference cellref = new CellReference(this.sheetCfg.getName(), row.getRowNum(), i, true, true);
		this.displayContext.getCellRefRepository().addDateRef(Long.toString(date.getTime()), cellref);
	}

	private void fillRowHeaderData(List<MonitorRowHeader> rowHeaders, MonitorEvent event, Row row, int columnOffset) {
    	Cell cell;
		int columnPos = 0;
		
		for (MonitorRowHeader rowHeader : rowHeaders){
			cell = addEmptyRowHeaderCell(row, columnPos);
			rowHeader.apply(event, cell);
			if (rowHeader.hasEventLink()){
				CellReference cellRef = this.displayContext.getCellRefRepository().getCellRef(sheetCfg.getLinkType(), event.getId());
				addDocumentHyperLink(cell, cellRef.formatAsString());
			}
				
			columnPos++;
		}

		// fill extra cells if more functions
		while(columnPos < columnOffset){
			cell = addEmptyRowHeaderCell(row, columnPos);
			columnPos++;
		}

	}

	private void prepareCellLinks(Row row, int colIndex, MonitorEvent event, Date timestamp, boolean firstEventCell, boolean firstEvent) {
		if (firstEventCell && this.sheetCfg.isLinkable()){
			// first cell, let's get the Excel reference (ex : 'Sheet1'!G21) and update the event
			CellReference cellref = new CellReference(this.sheetCfg.getName(), row.getRowNum(), colIndex, true, true);
			this.displayContext.getCellRefRepository().addCellRef(sheetCfg.getLinkType(), event.getId(), cellref);
			if (firstEvent) // also update the column link
				this.displayContext.getCellRefRepository().addColumnRef(sheetCfg.getLinkType(), timestamp, cellref);
		}
	}

	private void prepareColumnLinks(int columnOffset){
		int offset = 0;
		boolean linkable = this.sheetCfg.isLinkable();
    	
		if (!linkable)
			return;
		
		int linePos= 2;
		
		for (ThreadDump dump : session.getDumps()){
			Date timestamp = dump.getTimestamp();
			
			// column ref (ex : 'Sheet1'!G2) 
			CellReference cellref = new CellReference(this.sheetCfg.getName(), linePos, offset + columnOffset, true, true);
			this.displayContext.getCellRefRepository().addColumnRef(sheetCfg.getLinkType(), timestamp, cellref);
			
			offset++;
		}
    }	    

	private void fillDateHeaderCell(Row row, int i, ThreadDump dump, int headerLabelsColumnSize, boolean hiatus){
		int pos = (i-headerLabelsColumnSize) % 14;
		Cell cell = row.createCell(i);
		if (pos == 0){
    		cell.setCellStyle(getThemeStyle(STYLE_THEME_TOP_BAR));
    		cell.setCellValue(formatDate(dump.getTimestamp(), DAY_DISPLAY_FORMAT));
    		registerDateLink(dump.getTimestamp(), row, i);
		}else if (pos == 1 && !this.session.getDisplayTimeZoneInfo().isUnknown()){
    		cell.setCellStyle(getThemeStyle(STYLE_THEME_TOP_BAR));
    		cell.setCellValue(formatTimeZoneInfo(this.session.getDisplayTimeZoneInfo()));
		}else if (!hiatus && pos == 6 && dump.getProcessUpTime()!=-1){
    		cell.setCellStyle(getThemeStyle(STYLE_THEME_TOP_BAR_SMALL));
    		cell.setCellValue(formatUpTime(dump.getProcessUpTime()));
		}else{
			cell.setCellStyle(getThemeStyle(STYLE_THEME_TOP_BAR));
		}	
	}

	private String formatUpTime(long processUpTimeMs) {
        long processUpTimeSec = processUpTimeMs / 1000L; // convert to sec

        return String.format("Up %dd %02d:%02d",
        		processUpTimeSec / 86400 ,  // day
				(processUpTimeSec % 86400) / 3600 ,  // hour
				(processUpTimeSec % 3600) / 60);  // mn 
	}

	private XSSFRichTextString formatTimeZoneInfo(TimeZoneInfo timeZoneInfo) {
    	String value = timeZoneInfo.getZoneAbbreviation() + " " + timeZoneInfo.getDisplayOrigin(); 
    	XSSFRichTextString richText = new XSSFRichTextString(value); 
    	
        Font cellFont = workbook.createFont();
        cellFont.setFontHeightInPoints((short)11);

        Font smallCellFont = workbook.createFont();
        smallCellFont.setFontHeightInPoints((short)8);
        
    	richText.applyFont(0, timeZoneInfo.getZoneAbbreviation().length(), cellFont);
    	richText.applyFont(timeZoneInfo.getZoneAbbreviation().length()+1, value.length(), smallCellFont);
    	
		return richText;
	}
	
	private List<MonitorEvent> buildEvents() throws JzrInitializationException, JzrMonitorException {
		ConfigMonitoringSheet monitorConfig = this.sheetCfg.getMonitoringConfig();
		
		Map<String, Sticker> stickers = this.displayContext.getMonitoringRepository().getStickers(
				monitorConfig.getConfigStickers(), 
				this.session,
				this.sheetCfg.getMonitoringConfig().getJzrLocationResolver(),
				this.displayContext.getSetupManager().getMonitorSetupManager());
		
		Multimap<String, MonitorTaskEvent> taskEvents = this.displayContext.getMonitoringRepository().getTaskEvents(monitorConfig);
		if (taskEvents == null){
			List<MonitorTaskRule> taskRules = this.displayContext.getMonitoringRepository().getTaskRules(monitorConfig, this.session);
			taskEvents = LinkedListMultimap.create();
			session.applyMonitorStickers(taskRules, stickers);
			session.applyMonitorTaskRules(taskRules, taskEvents, monitorConfig.getConfigMonitorRules().getApplicativeRuleManager().getApplicativeTaskRuleFilter());
			this.displayContext.getMonitoringRepository().addTaskEvents(monitorConfig, taskEvents);
		}
		
		Multimap<String, MonitorSessionEvent> sessionEvents = this.displayContext.getMonitoringRepository().getSessionEvents(monitorConfig);
		if (sessionEvents == null){
			List<MonitorSessionRule> sessionRules = this.displayContext.getMonitoringRepository().getSessionRules(monitorConfig, this.session);
			sessionEvents = LinkedListMultimap.create();
			session.applyMonitorStickers(sessionRules, stickers);
			session.applyMonitorSessionRules(sessionRules, sessionEvents, monitorConfig.getConfigMonitorRules().getApplicativeRuleManager().getApplicativeSessionRuleFilter(), monitorConfig.getConfigMonitorRules().isPublisherRulesAllowed());
			this.displayContext.getMonitoringRepository().addSessionEvents(monitorConfig, sessionEvents);
		}
		
		Multimap<String, MonitorSystemEvent> systemEvents = this.displayContext.getMonitoringRepository().getSystemEvents(this.sheetCfg.getMonitoringConfig());
		if (systemEvents == null){
			List<MonitorSystemRule> systemRules = this.displayContext.getMonitoringRepository().getSystemRules(monitorConfig, this.session);
			systemEvents = LinkedListMultimap.create();
			session.applyMonitorStickers(systemRules, stickers);
			session.applyMonitorSystemRules(systemRules, systemEvents, monitorConfig.getConfigMonitorRules().getApplicativeRuleManager().getApplicativeSystemRuleFilter());
			this.displayContext.getMonitoringRepository().addSystemEvents(monitorConfig, systemEvents);
		}

		updateHighestEvent(taskEvents, sessionEvents, systemEvents);
		
		return MonitorHelper.buildElectedEventSortedListForReport(
				taskEvents, 
				sessionEvents, 
				systemEvents, 
				monitorConfig.isGroupSorting(),
				monitorConfig.hasDuplicateEventCleanup());
	}
	
	private void updateHighestEvent(
			Multimap<String, MonitorTaskEvent> taskEvents,
			Multimap<String, MonitorSessionEvent> sessionEvents,
			Multimap<String, MonitorSystemEvent> systemEvents) {
		parseEvents(Arrays.asList(taskEvents.values().toArray(new MonitorEvent[0])));
		parseEvents(Arrays.asList(sessionEvents.values().toArray(new MonitorEvent[0])));
		parseEvents(Arrays.asList(systemEvents.values().toArray(new MonitorEvent[0])));
	}
	
	private void parseEvents(Collection<MonitorEvent> events) {
		if (Level.CRITICAL.equals(this.session.getHighestEventCategory()))
			return; // maximum already reached
		
		for (MonitorEvent event : events){
			if (event.isElected() && event.getLevel().isMoreCritical(this.session.getHighestEventCategory()) > 0){
				this.session.setHighestEventCategory(event.getLevel());
			}
		}
	}
}
