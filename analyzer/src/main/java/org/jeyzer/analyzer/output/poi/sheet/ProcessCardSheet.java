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





import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jeyzer.analyzer.config.report.ConfigProcessCardSheet;
import org.jeyzer.analyzer.config.report.ConfigProcessCardSheet.ConfigCategory;
import org.jeyzer.analyzer.data.ProcessCard;
import org.jeyzer.analyzer.data.ProcessCard.ProcessCardProperty;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.session.JzrSession;

public class ProcessCardSheet extends JeyzerSheet {
	
	private ConfigProcessCardSheet sheetCfg;
	
	public ProcessCardSheet(ConfigProcessCardSheet processCardSheetConfig,
			JzrSession session, DisplayContext displayContext) {
		super(session, displayContext);
		this.sheetCfg = processCardSheetConfig;
	}

	@Override
	public void display() {
		
    	ProcessCard processCard = this.session.getProcessCard();
    	
    	Sheet sheet = createSheet(this.sheetCfg);
    	sheet.setColumnWidth(0, 3*256);
    	sheet.setColumnWidth(1, 42*256);
    	sheet.setColumnWidth(2, 84*256);
    	sheet.setColumnWidth(3, 100*256);
    	
    	int rowLine = 1;
    	
    	for (ConfigCategory configCategory : sheetCfg.getCategories()){
    		rowLine += displayCategory(configCategory, sheet, processCard, rowLine);
    	}

    	sheet.createFreezePane(0, 1);
    	addMainFrame(sheet, sheetCfg, rowLine++, 6);
    	
    	close(this.sheetCfg);
	}

	private int displayCategory(ConfigCategory configCategory, Sheet sheet, ProcessCard processCard, int rowLine) {
		Map<Object, String> fields = configCategory.getFields();
		List<String> names = new ArrayList<>(fields.size());
		List<String> values = new ArrayList<>(fields.size());
		List<String> properties = new ArrayList<>(fields.size());
		int count = 0;
		
		for (Object propertyKey : fields.keySet()){
			ProcessCardProperty property = processCard.getValue(propertyKey);
			if (property != null){
				values.add(property.getValue());
				names.add(fields.get(propertyKey));
				properties.add(property.getName());
			}
		}
		
		if (values.isEmpty() || isCategoryWithEmptyValues(values, configCategory.getName()))
			return count;  // nothing to log in this category
		
		displayCategoryHeader(sheet, configCategory.getName(), configCategory.getColor(), rowLine + count);
		count++;
		
		for (int i=0; i<values.size(); i++){
			displayCategoryField(
					sheet, 
					names.get(i),
					values.get(i),
					properties.get(i),
					rowLine + count
					);
			count++;
			itemsCount++;
		}
		
		return count;
	}

	private void displayCategoryField(Sheet sheet, String name, String value, String property, int rowLine) {
		Row row = sheet.createRow(rowLine);
		addCell(row, 1, name, getCellStylePlainReference(rowLine));
		// JEYZ-73 : if value is too large, trim it
		addCell(row, 2, secureCellDisplayValue(value), getCellStyleWrappedPlainReference(rowLine));
		addCell(row, 3, property, getCellStylePlainReference(rowLine));
		addEmptyCell(row, 4, getCellStylePlainReference(rowLine));
		addEmptyCell(row, 5, getCellStylePlainReference(rowLine));
	}

	private void displayCategoryHeader(Sheet sheet, String name, String color, int rowLine) {
    	Row row = sheet.createRow(rowLine);
    	short height = 600;
    	row.setHeight(height);
		addCell(row, 1, name, STYLE_CATEGORY_HEADER, color);
		addEmptyCell(row, 2, STYLE_CATEGORY_HEADER, color);
		addEmptyCell(row, 3, STYLE_CATEGORY_HEADER, color);
		addEmptyCell(row, 4, STYLE_CATEGORY_HEADER, color);
		addEmptyCell(row, 5, STYLE_CATEGORY_HEADER, color);
	}

	private boolean isCategoryWithEmptyValues(List<String> values, String category) {
		for (int i=0; i<values.size(); i++){
			if (! ProcessCard.UNAVAILABLE_VALUE.equals(values.get(i)))
					return false;
		}
		logger.info("Category section display skipped : the " + category + " category contains only unavailable values");
		return true;
	}
}
