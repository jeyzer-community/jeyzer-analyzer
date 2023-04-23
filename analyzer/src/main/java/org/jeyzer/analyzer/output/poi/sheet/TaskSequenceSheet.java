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



import static org.jeyzer.analyzer.math.FormulaHelper.*;
import static org.jeyzer.analyzer.output.poi.style.CellFonts.FONT_DOUBLE_UNDERLINE_SYMBOL_9;
import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.*;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.*;





import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.jeyzer.analyzer.config.report.ConfigSequenceSheet;
import org.jeyzer.analyzer.config.report.headers.ConfigSheetAbstractHeaders.HEADER_FREEZE_MODE;
import org.jeyzer.analyzer.data.Action;
import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.TimeZoneInfo;
import org.jeyzer.analyzer.error.JzrReportException;
import org.jeyzer.analyzer.output.poi.CellText;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.output.poi.context.SequenceSheetDisplayContext;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.cell.DisplayRule;
import org.jeyzer.analyzer.output.poi.rule.cell.DisplayRuleBuilder;
import org.jeyzer.analyzer.output.poi.rule.header.AbstractNumericDisplayHeaderRule;
import org.jeyzer.analyzer.output.poi.rule.header.Header;
import org.jeyzer.analyzer.output.poi.rule.header.HeaderBuilder;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunctionBuilder;
import org.jeyzer.analyzer.output.poi.rule.header.function.NotSupportedFunction;
import org.jeyzer.analyzer.output.poi.rule.row.RowHeader;
import org.jeyzer.analyzer.output.poi.rule.row.RowHeaderBuilder;
import org.jeyzer.analyzer.output.poi.style.CellFonts;
import org.jeyzer.analyzer.output.stats.CollectedStats;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.analyzer.util.AnalyzerHelper;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTMarker;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTMarkerStyle;
import org.openxmlformats.schemas.drawingml.x2006.chart.STMarkerStyle;

public class TaskSequenceSheet extends JeyzerSheet {

	protected static final String LEGEND = "Legend";
	
	public static final int ACTIVITY_COLUMN_LARGE_WIDTH = 42;
	public static final int ACTIVITY_COLUMN_SHORT_WIDTH = 14;
	
	private ConfigSequenceSheet sheetCfg;
	private Map<Integer,Integer> missingTds = new HashMap<>();
	private Map<String,Cell> headerTitleCells = new HashMap<>();
	
	public TaskSequenceSheet(ConfigSequenceSheet sheetCfg, JzrSession session, DisplayContext displayContext){
		super(session, displayContext);
		this.sheetCfg = sheetCfg;
	}
	
	@Override
	public void display() throws JzrReportException {
		Sheet sheet = createSheet(this.sheetCfg);
		
		HeaderBuilder headerBuilder = HeaderBuilder.newInstance();
		List<Header> headers = headerBuilder.buildHeaders(sheetCfg.getHeaderConfigsSets(), new SheetDisplayContext(displayContext, sheet), session);
		
		HeaderFunctionBuilder functionBuilder = HeaderFunctionBuilder.newInstance();
		Map<String,HeaderFunction> functionsPerHeader = functionBuilder.buildHeaderFunctions(sheetCfg.getHeaderConfigsSets(), headers, session);
		List<HeaderFunction> modelFunctions = functionBuilder.buildHeaderModelFunctions(sheetCfg.getHeaderConfigsSets(), headers, session);
		
		RowHeaderBuilder rowHeaderBuilder = RowHeaderBuilder.newInstance();
		List<RowHeader> rowHeaders = rowHeaderBuilder.buildRowHeaders(sheetCfg.getRowHeadersConfig(), new SheetDisplayContext(displayContext, sheet), session);
		
		DisplayRuleBuilder displayBuilder = DisplayRuleBuilder.newInstance();
		CollectedStats statsModel = new CollectedStats(session.getActionsSize(), session.getActionsStackSize());
		int tdPeriod = session.getThreadDumpPeriod();
		List<DisplayRule> rules = displayBuilder.buildRules(sheetCfg.getDisplayConfigs(), new SequenceSheetDisplayContext(displayContext, sheet, statsModel, tdPeriod), session);
		
		prepareSheet(sheet, getRowFreezeHeaderSize(rowHeaders), getFreezeHeaderSize(headers, rules));

		int columnOffset = getHeaderRowOffset(rowHeaders, modelFunctions);
		
		reportTaskSequenceHeader(sheet, rowHeaders, modelFunctions, rules.isEmpty(), columnOffset);

		if (!headers.isEmpty())
			fillDynamicHeaders(sheet, headers, functionsPerHeader, columnOffset);
		
		if (sheetCfg.isLinkable())
			prepareColumnLinks(columnOffset);
		
		if (!rules.isEmpty())
			fillData(sheet, rules, rowHeaders, headers.size(), columnOffset);
		
		updateTimeHeaderCells(sheet, rules, headers.size(), columnOffset);

		if (!headers.isEmpty() && !rules.isEmpty())
			groupDynamicHeaders(sheet, headers);
		
		int line = fillLegend(sheet, rules, rowHeaders, headers.size(), columnOffset);
		
		line = fillStats(sheet, rules, line, columnOffset);
		
		if (!headers.isEmpty()){
			line = adjustChartsStartLine(rules, headers, line);
			displayCharts(sheet, headers, columnOffset, line);
		}
		
		addMenuLink(sheet, sheetCfg, STYLE_THEME_TOP_BAR_JEYZER_TITLE, 0, 0);
		
		this.itemsCount = rules.isEmpty() ? headers.size() : session.getActionsSize();
		close(this.sheetCfg);
	}

	private int getHeaderRowOffset(List<RowHeader> rowHeaders, List<HeaderFunction> modelFunctions) {
		if (modelFunctions.isEmpty())
			return rowHeaders.size();
		if (rowHeaders.size() > modelFunctions.size() + 2)
			return rowHeaders.size();
		else
			return modelFunctions.size() + 2;
	}

	private int adjustChartsStartLine(List<DisplayRule> rules, List<Header> headers, int line) {
		int lastActionLine = getLastActionLine(rules, headers.size());
		if (lastActionLine > line)
			return lastActionLine;
		else
			return line;
	}

	private int getLastActionLine(List<DisplayRule> rules, int headerOffset) {
		return 2 + ((!rules.isEmpty()) ? this.session.getActionsSize() : 0)  + headerOffset;
	}

	private int fillStats(Sheet sheet, List<DisplayRule> rules, int line, int columnOffset) {
    	int startPos, startLine;
    	
    	if (!hasStatsToDisplay(rules))
    		return line;
    	
    	line = line + 3;
    	
    	// set row position, must be empty
    	startPos = columnOffset + 3;
    	startLine = line;
		
    	line = fillStatsHeader(sheet, startPos, line);
    	
		for (DisplayRule rule : rules){
    		line = rule.displayStats(line, startPos);
    	}
		
   		// add borders
   		addFrameBorders(sheet, startLine-1, line, startPos-1, startPos+8);
    	
    	return line;
	}

	private int fillStatsHeader(Sheet sheet, int startPos, int line) {
		addLegendSectionLabel(sheet, line, startPos-1, "Appearance"); // style set in addFrameBorders 
		addLegendSectionLabel(sheet, line, startPos, "Type", STYLE_CELL_CENTERED_SMALL);
		addLegendSectionLabel(sheet, line, startPos+1, "Action %", STYLE_CELL_CENTERED_SMALL);
		addLegendSectionLabel(sheet, line, startPos+2, "Action count", STYLE_CELL_CENTERED_SMALL);
		addLegendSectionLabel(sheet, line, startPos+3, "Global %", STYLE_CELL_CENTERED_SMALL);
		addLegendSectionLabel(sheet, line, startPos+4, "Global count", STYLE_CELL_CENTERED_SMALL);
		addLegendSectionLabel(sheet, line, startPos+5, "Type", STYLE_CELL_CENTERED_SMALL);

		line++;
		addLegendSectionLabel(sheet, line, startPos-1, "Stats"); // style set in addFrameBorders
		
		return line;
	}

	private void displayCharts(Sheet sheet, List<Header> headers, int rowHeaderSize, int line) {
		List<ConfigChart> chartConfigs = this.sheetCfg.getChartConfigs();
		
		if (chartConfigs.isEmpty())
			return;
		
		final int CHART_HEIGHT = 20;
		final int CHART_HEIGHT_SEP = 3;
		
		int anchorPosX1 = (rowHeaderSize>2) ? rowHeaderSize-2 : 0;
		int anchorPosY1 = line + CHART_HEIGHT_SEP;
		int anchorPosX2 = anchorPosX1 + this.missingTds.size() + 2; // 2 is size of left legend
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
		final int HEADER_START = 2;
		final int DATA_RANGE_X1 = rowHeaderSize;
		
		int y1, y2;
		int x1 = DATA_RANGE_X1;
		int x2 = DATA_RANGE_X1 + this.missingTds.size() - 1;
		
        // data source X axis (dates)
        ChartDataSource<Number> xs = DataSources.fromNumericCellRange(sheet, new CellRangeAddress(1, 1, x1, x2));		
		
        for (String serie : chartConfig.getSeries()) {
        	int pos = HEADER_START;
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
	
	private int getFreezeHeaderSize(List<Header> headers, List<DisplayRule> rules) {
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

	private int getRowFreezeHeaderSize(List<RowHeader> rowHeaders) {
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
	
	private void reportTaskSequenceHeader(Sheet sheet, List<RowHeader> rowHeaders, List<HeaderFunction> functions, boolean headersOnly, int headerLabelsColumnSize){
		Row row0, row1;
		
		// On row 0, just display date every 14 cells
		row0 = sheet.createRow(0);
		
		for (int i=1; i<headerLabelsColumnSize; i++){
			addHeaderCell(row0, i, "", STYLE_THEME_TOP_BAR);
		}

		// On row 1, display thread dump time stamps
		row1 = sheet.createRow(1);
		
		int columnPos = 0;
		Cell cell;
		for (RowHeader rowHeader : rowHeaders){
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
		
		int missingTdCount = 0;
		int columnWidth = session.getDumps().size() <= 4 ? ACTIVITY_COLUMN_LARGE_WIDTH : ACTIVITY_COLUMN_SHORT_WIDTH;
		
		for (ThreadDump dump : session.getDumps()){
			if (dump.isRestart()){ // Advanced JMX only
				// Proceed with restart : treat it as hiatus but display it differently
				sheet.setColumnWidth(columnPos, 12*256);
				addCell(row0, columnPos, "RESTART", STYLE_RESTART_HEADER); // restart cell
				// calculate start date
				long startTime = dump.getTimestamp().getTime() - dump.getProcessUpTime();
				addCell(row1, columnPos, formatDate(new Date(startTime), TIME_DISPLAY_FORMAT), STYLE_RESTART_HEADER); // restart cell
				missingTdCount++;
				missingTds.put(Integer.valueOf(columnPos), Integer.valueOf(missingTdCount)); // keep track. Will be used as extra offset for lower rows
				columnPos++;
			}
			else if (dump.hasHiatusBefore()){
				// Proceed with hiatus
				long difftime = convertToMinutes(dump.getTimeSlice());  // from nano sec to min
				String unit = " mn)";
				if (difftime == 0){
					// downgrade granularity from min to sec
					difftime = convertToSeconds(dump.getTimeSlice());  // from nano sec to sec
					unit = " sec)";
				}
				
				sheet.setColumnWidth(columnPos, 12*256);
				fillDateHeaderCell(row0, columnPos, dump, headerLabelsColumnSize, true); // date + time zone + up time (adv jmx) or empty cell
				addCell(row1, columnPos, "Hiatus (" + difftime + unit, STYLE_MISSING_TD_HEADER); // hiatus cell
				missingTdCount++;
				missingTds.put(Integer.valueOf(columnPos), Integer.valueOf(missingTdCount)); // keep track. Will be used as extra offset for lower rows
				columnPos++;
			}
			sheet.setColumnWidth(columnPos, columnWidth*256);
			fillDateHeaderCell(row0, columnPos, dump, headerLabelsColumnSize, false);  // date + time zone + up time (adv jmx) or empty cell
			addHeaderCell(row1, columnPos, formatDate(dump.getTimestamp(), TIME_DISPLAY_FORMAT), STYLE_THEME_HEADER_TIME); // time stamp cell
			missingTds.put(Integer.valueOf(columnPos), Integer.valueOf(missingTdCount));
			
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
			int missingtdOffset = 0;
			Row row = sheet.createRow(linePos);
			
			fillHeaderTitleSection(sheet, row, header, columnOffset, headerNamePos);
			
			List<Cell> cells = new ArrayList<>();
			List<ThreadDump> dumps = session.getDumps();
			
			for (int j=0; j< dumps.size(); j++){
				// get offset
				missingtdOffset = this.missingTds.get(Integer.valueOf(missingtdOffset + j + columnOffset)).intValue();
				int colIndex = j + columnOffset + missingtdOffset;
				Cell  cell = row.createCell(colIndex);
				cell.setCellStyle(getStyle(header.getStyle()));
				
				cells.add(cell);
			}
			
			HeaderFunction function = functionsPerHeader.get(header.getName());
			if (function == null)
				function = new NotSupportedFunction(null);
			
			header.apply(dumps, cells, function);
		
			linePos++;
			
			fillMissingDumps(row, columnOffset);
			
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
				if (header.getComment()!= null){
					int deep = AnalyzerHelper.countLines(header.getComment()) + 1; // add one extra line in case 1 line is wrapped
					int length = AnalyzerHelper.countParagraphMaxLength(header.getComment());
					if (length > 0)
						addComment(sheet, cell, header.getComment(), deep, length / CellText.COMMENT_COLUMN_CHARS_SIZE);
				}
				this.headerTitleCells.put(header.getName(), cell); // used by charts
			}
			else{
				addHeaderCell(row, columnPos, "", STYLE_THEME_DYNAMIC_HEADER);
			}
			columnPos++;
		}
	}

	private void fillData(Sheet sheet, List<DisplayRule> rules, List<RowHeader> rowHeaders, int rowOffset, int columnOffset){
		int offset = 0;
		Row row;
		Cell cell;
		List<Cell> cells;
    	
		int linePos= 2 + rowOffset; // start on second row + # headers
		int missingtdOffset = this.missingTds.get(Integer.valueOf(offset + columnOffset)).intValue();
		
		for (ThreadDump dump : session.getDumps()){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = session.getActionHistory().get(timestamp);
			
			missingtdOffset = this.missingTds.get(Integer.valueOf(offset + columnOffset + missingtdOffset)).intValue();
			
			boolean firstAction = true;
			for(ThreadAction action : actions){

				row = sheet.createRow(linePos);
				
				cells = new ArrayList<Cell>();
				
				int missingtdOffsetForAction = missingtdOffset;
				for (int j=0; j< action.size(); j++){
					// get extra offset
					missingtdOffsetForAction = this.missingTds.get(Integer.valueOf(offset + j + columnOffset + missingtdOffsetForAction)).intValue();
					
					int colIndex = offset + j + columnOffset + missingtdOffsetForAction;
					cell = row.createCell(colIndex);
					cells.add(cell);
					// reference all the stack cells
					prepareCellLinks(row, colIndex, action, action.getThreadStack(j).getTimeStamp(), firstAction);
				}
				
				for (DisplayRule rule : rules)
					rule.apply((Action)action, cells);
				
				linePos++;
				
				fillMissingDumps(row, columnOffset);
				firstAction = false;
				
				// write row headers (ex : thread id, thread name..)
				// do it last as we need to access action links just previously created
				fillRowHeaderData(rowHeaders, action, row, columnOffset);
				
				// register action link
				registerActionLink(action, row, offset, columnOffset, missingtdOffset);
			}
			offset++;
		}

		// if some data was written, add filter on row headers
		if (linePos != 2 + rowOffset){
			sheet.setAutoFilter(new CellRangeAddress(1, linePos-1, 0, rowHeaders.size()-1));
		}
    }

	private void updateTimeHeaderCells(Sheet sheet, List<DisplayRule> rules, int rowOffset, int columnOffset){
		int offset = 0;
		Row row;
		Cell cell;
		
		if (!this.displayContext.getSetupManager().isHeaderActionLinkEnabled() 
				&& !this.displayContext.getSetupManager().isHeaderActionHighlightEnabled())
			return;
    	
		int missingtdOffset = this.missingTds.get(Integer.valueOf(offset + columnOffset)).intValue();
		row = sheet.getRow(1);
		
		for (ThreadDump dump : session.getDumps()){
			if (dump.hasStartingActions()){
				// header time stamp must be updated with link to first action (if actions are displayed)
				missingtdOffset = this.missingTds.get(Integer.valueOf(offset + columnOffset + missingtdOffset)).intValue();
				cell = row.getCell(offset + columnOffset + missingtdOffset);
				
				if (this.displayContext.getSetupManager().isHeaderActionHighlightEnabled())
					cell.setCellStyle(this.getThemeStyle(STYLE_THEME_HEADER_TIME_ACTION_START));
				
				if (!rules.isEmpty() && this.displayContext.getSetupManager().isHeaderActionLinkEnabled()){
					CellReference cellref = this.displayContext.getCellRefRepository().getRefColumn(sheetCfg.getLinkType(), dump.getTimestamp());
					addDocumentHyperLink(cell, cellref.formatAsString());
				}
			}
			else if (!dump.getWorkingThreads().isEmpty() && this.displayContext.getSetupManager().isHeaderActionHighlightEnabled()){
				missingtdOffset = this.missingTds.get(Integer.valueOf(offset + columnOffset + missingtdOffset)).intValue();
				cell = row.getCell(offset + columnOffset + missingtdOffset);
				cell.setCellStyle(this.getThemeStyle(STYLE_THEME_HEADER_TIME_ACTION));
			}
			offset++;
		}
    }	
	
    private void registerActionLink(ThreadAction action, Row row, int offset, int rowHeaderSize, int missingtdOffset) {
		if (!this.displayContext.getSetupManager().isActionLinkEnabled())
			return;
		
		int missingtdOffsetForAction = this.missingTds.get(Integer.valueOf(offset + rowHeaderSize + missingtdOffset)).intValue();
		int colIndex = offset + rowHeaderSize + missingtdOffsetForAction;
		// action first cell
		CellReference cellref = new CellReference(this.sheetCfg.getName(), row.getRowNum(), colIndex-1, true, true);
		this.displayContext.getCellRefRepository().addActionRef(action.getStrId(), cellref);
	}
    
	
    private void registerDateLink(Date date, Row row, int i) {
		if (!this.displayContext.getSetupManager().isHeaderDateLinkEnabled())
			return;
    	
		CellReference cellref = new CellReference(this.sheetCfg.getName(), row.getRowNum(), i, true, true);
		this.displayContext.getCellRefRepository().addDateRef(Long.toString(date.getTime()), cellref);
	}

	private void fillRowHeaderData(List<RowHeader> rowHeaders, ThreadAction action, Row row, int columnOffset) {
    	Cell cell;
		int columnPos = 0;
		
		for (RowHeader rowHeader : rowHeaders){
			cell = addEmptyRowHeaderCell(row, columnPos);
			rowHeader.apply(action, cell);
			if (rowHeader.hasActionLink()){
				CellReference cellRef = this.displayContext.getCellRefRepository().getCellRef(sheetCfg.getLinkType(), action);
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

	private void prepareCellLinks(Row row, int colIndex, ThreadAction action, Date timestamp, boolean firstAction) {
		if (!this.sheetCfg.isLinkable())
			return;
		
		// first cell, let's get the Excel reference (ex : 'Sheet1'!G21) and update the action
		CellReference cellref = new CellReference(this.sheetCfg.getName(), row.getRowNum(), colIndex, true, true);
		this.displayContext.getCellRefRepository().addCellRef(sheetCfg.getLinkType(), action, timestamp, cellref);
		
		if (firstAction) // also update the column link
			this.displayContext.getCellRefRepository().addColumnRef(sheetCfg.getLinkType(), timestamp, cellref);
	}

	private void prepareColumnLinks(int columnOffset){
		int offset = 0;
		boolean linkable = this.sheetCfg.isLinkable();
    	
		if (!linkable)
			return;
		
		int linePos= 2;
		int missingtdOffset = this.missingTds.get(Integer.valueOf(offset + columnOffset)).intValue();
		
		for (ThreadDump dump : session.getDumps()){
			Date timestamp = dump.getTimestamp();
			
			missingtdOffset = this.missingTds.get(Integer.valueOf(offset + columnOffset + missingtdOffset)).intValue();
			
			// column ref (ex : 'Sheet1'!G2) 
			CellReference cellref = new CellReference(this.sheetCfg.getName(), linePos, offset + columnOffset + missingtdOffset, true, true);
			this.displayContext.getCellRefRepository().addColumnRef(sheetCfg.getLinkType(), timestamp, cellref);
			
			offset++;
		}
    }

    private int fillLegend(Sheet sheet, List<DisplayRule> rules, List<RowHeader> rowHeaders, int headerOffset, int columnOffset){
    	Row row;
    	Cell cell0, cell1, cell2, cell3, cell4, cell5, cell6, cell7, cell8, cell9, cell10, cell11;
    	int line;
    	int startPos;
		boolean hasRowHeaderLegend = hasRowHeaderLegend(rowHeaders);
		boolean hasCellLegend = hasCellLegend(rules);
		boolean hasCellStats = hasStatsToDisplay(rules);
    	
    	if (!hasCellLegend && !hasRowHeaderLegend)
    		return this.getLastActionLine(rules, headerOffset);
    	
    	// set row position, must be empty
    	line = 10 + headerOffset;
    	startPos = columnOffset + 3;
    	int delay = 0;
    	while (true){
    	   	row = sheet.getRow(line);
    	   	if (row == null){
    	   		row = sheet.createRow(line); // either row is null
    	   		break;
    	   	}
    	   	cell0 = row.getCell(startPos-3);  // or row contains null cells
    	   	cell1 = row.getCell(startPos-2);
    	   	cell2 = row.getCell(startPos-1);
        	cell3 = row.getCell(startPos);
        	cell4 = row.getCell(startPos+1);
        	cell5 = row.getCell(startPos+2);
        	cell6 = row.getCell(startPos+3);
        	cell7 = row.getCell(startPos+4);
        	cell8 = hasCellStats ? row.getCell(startPos+5) : null; // stats section is larger than legend one
        	cell9 = hasCellStats ? row.getCell(startPos+6) : null;
        	cell10 = hasCellStats ? row.getCell(startPos+7) : null;
        	cell11 = hasCellStats ? row.getCell(startPos+8) : null;
        	if (cell0 == null && cell1 == null && cell2 == null 
        			&& cell3 == null && cell4 == null && cell5 == null 
        			&& cell6 == null && cell7 == null && cell8 == null
        			&& cell9 == null && cell10 == null && cell11 == null 
        			&& delay>=3)
	        	break;
        	line++;
        	delay++;
    	}
    	
    	// legend size
		sheet.setColumnWidth(startPos, 14*256);

    	// fill legend for display rules
		line = line + 3;
		int startLine = line;
		if (hasCellLegend){
    		addLegendSectionLabel(sheet, line, startPos-1, "Task");
	    	for (DisplayRule rule : rules){
	    		line = rule.displayLegend(line, startPos);  // line is incremented if legend is displayed by the rule
	    	}
		}

		if (hasCellLegend && hasRowHeaderLegend)
			line++; // add separator line with next section
		
		if (hasRowHeaderLegend){
	    	// fill legend for row header rules
	    	addLegendSectionLabel(sheet, line, startPos-1, "Row Header");
	    	for (RowHeader rule : rowHeaders){
	    		line = rule.displayLegend(line, startPos);
	    	}
		}
    	
   		// add borders
   		addFrameBorders(sheet, startLine-1, line, startPos-1, startPos+4);
    	
   		// add legend link in top bar
   		addLegendLink(sheet, startLine, startPos);
    	
    	return line;
    }
    
    private void addLegendLink(Sheet sheet, int startLine, int startPos) {
		Row row = getValidRow(sheet, 0);
		Cell cell = row.createCell(1);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_TOP_BAR_JEYZER_LEGEND));
		cell.setCellValue(LEGEND);
		
		CellReference cellref = new CellReference(
				startLine-3, startPos-3, true, true);  // Refer the Legend section
		addDocumentHyperLink(cell, cellref.formatAsString());
	}

	private boolean hasRowHeaderLegend(List<RowHeader> rowHeaders) {
    	for (RowHeader rule : rowHeaders){
			if (rule.hasLegend()){
				return true;
			}
    	}
		return false;
	}

	private boolean hasCellLegend(List<DisplayRule> rules) {
		for (DisplayRule rule : rules){
			if (rule.hasLegend()){
				return true;
			}
		}
		return false;
	}
	
	private boolean hasStatsToDisplay(List<DisplayRule> rules) {
		for (DisplayRule rule : rules){
			if (rule.hasStatsToDisplay()){
				return true;
			}
		}
		return false;
	}

	private void addFrameBorders(Sheet sheet, int startLine, int endLine, int startPos, int endPos){
    	Row row;
    	Cell cell;

    	// top cells
        row = getValidRow(sheet, startLine);
    	for (int i=startPos; i<endPos+1; i++){
    		cell = getValidCell(row, i);
    	   	if (i==startPos)
    	   		cell.setCellStyle(getStyle(STYLE_FRAME_TOP_LEFT));
    	   	else if (i==endPos)
    	   		cell.setCellStyle(getStyle(STYLE_FRAME_TOP_RIGHT));
    	   	else 
    	   		cell.setCellStyle(getStyle(STYLE_FRAME_TOP));
    	}	   	
        
    	// left and right cells
    	for (int j=startLine+1; j<endLine; j++){
    		row = getValidRow(sheet, j);

    	   	cell = getValidCell(row, startPos);
    		cell.setCellStyle(getStyle(STYLE_FRAME_LEFT));

    		cell = getValidCell(row, endPos);
    		cell.setCellStyle(getStyle(STYLE_FRAME_RIGHT));
    	}
    	
    	// bottom cells
    	row = getValidRow(sheet, endLine);
    	for (int i=startPos; i<endPos+1; i++){
    	   	cell = getValidCell(row, i);
    	   	if (i==startPos)
    	   		cell.setCellStyle(getStyle(STYLE_FRAME_BOTTOM_LEFT));
    	   	else if (i==endPos)
    	   		cell.setCellStyle(getStyle(STYLE_FRAME_BOTTOM_RIGHT));
    	   	else 
    	   		cell.setCellStyle(getStyle(STYLE_FRAME_BOTTOM));
    	}
    	
    }
    
	private void addLegendSectionLabel(Sheet sheet, int line, int pos, String title, String style) {
		Row row = getValidRow(sheet, line);
		addCell(row, pos, title, style);
	}
	
	private void addLegendSectionLabel(Sheet sheet, int line, int pos, String title) {
		Row row = getValidRow(sheet, line);
		addCell(row, pos, title);
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

	private void fillMissingDumps(Row row, int rowHeaderSize){
		int i=rowHeaderSize;
		for (ThreadDump dump : session.getDumps()){
			if (dump.isRestart()){
				Cell cell = addEmptyCell(row, i, STYLE_RESTART_COLUMN);
				cell.setCellComment(null); // reset any comment
				i++;
			}
			else if (dump.hasHiatusBefore()){
				Cell cell = addEmptyCell(row, i, STYLE_MISSING_TD_CELL);
				cell.setCellComment(null); // reset any comment
				i++;
			}
			i++;
		}
    }
}
