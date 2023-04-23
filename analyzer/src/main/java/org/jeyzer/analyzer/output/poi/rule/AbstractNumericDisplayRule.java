package org.jeyzer.analyzer.output.poi.rule;

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



import static org.jeyzer.analyzer.math.FormulaHelper.DOUBLE_TO_LONG_NA;
import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.*;





import java.text.DecimalFormat;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.output.poi.CellColor;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Ints;

public abstract class AbstractNumericDisplayRule extends AbstractDisplayRule {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractNumericDisplayRule.class);	
	
	private static final String THRESHOLD_NAME = "threshold";
	
	private static final int DEFAULT_PERCENTAGE_THRESHOLD = 70;
	private static final int DEFAULT_NUMBER_THRESHOLD = Integer.MAX_VALUE;

    private static final ThreadLocal< DecimalFormat > formatPercent = new ThreadLocal< DecimalFormat >() {
        @Override
        protected DecimalFormat initialValue() {
            return new DecimalFormat("#.#");
        }
    };
    
    private static final ThreadLocal< DecimalFormat > formatNumber = new ThreadLocal< DecimalFormat >() {
        @Override
        protected DecimalFormat initialValue() {
        	DecimalFormat format = new DecimalFormat("#");
        	format.setGroupingSize(3);
            return format;
        }
    };	
	
	protected int threshold;
	protected Delta delta; // can be null
	
	public AbstractNumericDisplayRule(ConfigDisplay displayCfg,
			SheetDisplayContext context) {
		super(displayCfg, context);
		
		this.threshold = buildThreshold(displayCfg); 
		this.delta = buildDelta(displayCfg);
	}
	
	public abstract boolean isPercentageBased();

    public String formatPercent(double percent) {
        return formatPercent.get().format(percent);
    }

    public String formatNumber(double number) {
        return formatNumber.get().format(number);
    }

	protected void setValue(Cell cell, Double value){
		if (value != null)
			cell.setCellValue(value);
		else
			cell.setCellValue(NOT_AVAILABLE);
	}

	protected void setValue(Cell cell, Long value){
		if (value != null)
			cell.setCellValue(value);
		else
			cell.setCellValue(NOT_AVAILABLE);
	}
	
	protected String getDecimalStyle(){
		return STYLE_CELL_DECIMAL_NUMBER;
	}
	
	protected String getNumberStyle(){
		return STYLE_CELL_NUMBER;
	}
	
	protected void setValueBasedColorForeground(Cell cell, double value, double prevValue, boolean valueNotAvailable){
		if (valueNotAvailable){
			setColorForeground(cell,COLOR_NOT_AVAILABLE);
		}
		else if (value >= this.threshold){
			// threshold reached
			setColorForeground(cell);
		}
		else if (delta != null && Double.doubleToRawLongBits(prevValue) != DOUBLE_TO_LONG_NA){
			// delta reached ?
			if ((value - prevValue) > (this.delta.getIncreaseFactor() * prevValue))
				// increase
				setColorForeground(cell, this.delta.getIncreaseColor());
			else if ((prevValue - value) > (this.delta.getDecreaseFactor() * prevValue)) 
				// decrease
				setColorForeground(cell, this.delta.getDecreaseColor());
		}
	}
    
	private int buildThreshold(ConfigDisplay displayCfg){
		String value = (String)displayCfg.getValue(THRESHOLD_NAME);
		
		if (value == null) // optional
			return defaultThreshold();
		
		Integer threshold = Ints.tryParse(value);
		if (threshold != null){
			return threshold;
		}
		else{
			logger.warn("Failed to read threshold value " + value + " for the " + getName() + " rule. Defaulting to value : " + defaultThreshold());
			return defaultThreshold();
		}
	}
	
	private int defaultThreshold() {
		return this.isPercentageBased() ? DEFAULT_PERCENTAGE_THRESHOLD : DEFAULT_NUMBER_THRESHOLD; 
	}

	private Delta buildDelta(ConfigDisplay displayCfg) {
		ConfigDisplay deltaCfg = displayCfg.getDelta();
		if (deltaCfg == null)
			return null; // optional
		
		Integer factor = 0;
		Delta dl = new Delta();
		String value = (String)deltaCfg.getValue(Delta.INCREASE_FACTOR_FIELD);
		if (value == null){
			logger.warn("Increase factor value not found. Delta ignored for the " + getName() + " header.");
			return null;
		}
		
		factor = Ints.tryParse(value);
		if (factor == null){
			logger.warn("Failed to convert delta increase factor value " + value + " for the " + getName() + " header. Delta ignored.");
			return null;
		}
		dl.setIncreaseFactor((float)factor/100);
		
		value = (String)deltaCfg.getValue(Delta.DECREASE_FACTOR_FIELD);
		if (value == null){
			logger.warn("Decrease factor value not found. Delta ignored for the " + getName() + " header.");
			return null;
		}
		
		factor = Ints.tryParse(value);
		if (factor == null){
			logger.warn("Failed to convert delta decrease factor value " + value + " for the " + getName() + " header. Delta ignored.");
			return null;
		}
		dl.setDecreaseFactor((float)factor/100);
		
		value = (String)deltaCfg.getValue(Delta.INCREASE_COLOR_FIELD);
		if (value != null)
			dl.setIncreaseColor(CellColor.buildColor(value));
		else{
			logger.warn("Increase color value not found. Delta ignored for the " + getName() + " header.");
			return null;
		}
		
		value = (String)deltaCfg.getValue(Delta.DECREASE_COLOR_FIELD);
		if (value != null)
			dl.setDecreaseColor(CellColor.buildColor(value));
		else{
			logger.warn("Decrease color value not found. Delta ignored for the " + getName() + " header.");
			return null;
		}
		
		return dl;
	}
	
}
