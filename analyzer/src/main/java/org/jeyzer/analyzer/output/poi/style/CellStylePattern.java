package org.jeyzer.analyzer.output.poi.style;

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



import org.apache.poi.ss.usermodel.BorderStyle;






import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

public class CellStylePattern {

	// Pattern fields
	private HorizontalAlignment alignment;
	private BorderStyle borderBottom;
	private BorderStyle borderLeft;
	private BorderStyle borderRight;
	private BorderStyle borderTop;
	private short borderBottomColor;
	private short borderLeftColor;
	private short borderRightColor;
	private short borderTopColor;
	private XSSFColor borderBottomColorRGB;
	private XSSFColor borderLeftColorRGB;
	private XSSFColor borderRightColorRGB;
	private XSSFColor borderTopColorRGB;
	private short dataFormat;
	private String dataFormatString;
	private short fillBackgroundColor;
	private short fillForegroundColor;
	private XSSFColor fillBackgroundColorRGB;
	private XSSFColor fillForegroundColorRGB;
	private FillPatternType fillPattern;
	private int fontIndex;
	private boolean hidden;
	private short indention;
	private boolean locked;
	private short rotation;
	private boolean shrinkToFit;
	private VerticalAlignment verticalAlignment;
	private boolean wrapText;
	
	public CellStylePattern(CellStyle seedStyle){
		alignment = seedStyle.getAlignment();
		borderBottom = seedStyle.getBorderBottom();
		borderLeft = seedStyle.getBorderLeft();
		borderRight = seedStyle.getBorderRight();
		borderTop= seedStyle.getBorderTop();
		borderBottomColor = seedStyle.getBottomBorderColor();
		borderLeftColor = seedStyle.getLeftBorderColor();
		borderRightColor = seedStyle.getRightBorderColor();
		borderTopColor = seedStyle.getTopBorderColor();
		dataFormat = seedStyle.getDataFormat();
		dataFormatString = seedStyle.getDataFormatString();
		fillBackgroundColor = seedStyle.getFillBackgroundColor();
		fillForegroundColor = seedStyle.getFillForegroundColor();
		fillPattern = seedStyle.getFillPattern();
		fontIndex = seedStyle.getFontIndexAsInt();
		hidden= seedStyle.getHidden();
		indention = seedStyle.getIndention();
		locked = seedStyle.getLocked();
		rotation = seedStyle.getRotation();
		shrinkToFit = seedStyle.getShrinkToFit();
		verticalAlignment = seedStyle.getVerticalAlignment();
		wrapText = seedStyle.getWrapText();
		
		XSSFCellStyle xssfSeedStyle = (XSSFCellStyle) seedStyle; 
		borderBottomColorRGB = xssfSeedStyle.getBottomBorderXSSFColor();
		borderLeftColorRGB = xssfSeedStyle.getLeftBorderXSSFColor();
		borderRightColorRGB = xssfSeedStyle.getRightBorderXSSFColor();
		borderTopColorRGB = xssfSeedStyle.getTopBorderXSSFColor();
		fillBackgroundColorRGB = xssfSeedStyle.getFillBackgroundXSSFColor();
		fillForegroundColorRGB = xssfSeedStyle.getFillForegroundXSSFColor();
	}

	public void setAlignment(HorizontalAlignment alignment) {
		this.alignment = alignment;
	}

	public void setBorderBottom(BorderStyle borderBottom) {
		this.borderBottom = borderBottom;
	}

	public void setBorderLeft(BorderStyle borderLeft) {
		this.borderLeft = borderLeft;
	}

	public void setBorderRight(BorderStyle borderRight) {
		this.borderRight = borderRight;
	}

	public void setBorderTop(BorderStyle borderTop) {
		this.borderTop = borderTop;
	}

	public void setBottomBorderColor(short borderBottomColor) {
		this.borderBottomColor = borderBottomColor;
	}

	public void setLeftBorderColor(short borderLeftColor) {
		this.borderLeftColor = borderLeftColor;
	}

	public void setRightBorderColor(short borderRightColor) {
		this.borderRightColor = borderRightColor;
	}

	public void setTopBorderColor(short borderTopColor) {
		this.borderTopColor = borderTopColor;
	}

	public void setDataFormat(short dataFormat) {
		this.dataFormat = dataFormat;
	}

	public void setDataFormatString(String dataFormatString) {
		this.dataFormatString = dataFormatString;
	}

	public void setFillBackgroundColor(short fillBackgroundColor) {
		this.fillBackgroundColor = fillBackgroundColor;
	}

	public void setFillForegroundColor(short fillForegroundColor) {
		this.fillForegroundColor = fillForegroundColor;
	}

	public void setFillPattern(FillPatternType fillPattern) {
		this.fillPattern = fillPattern;
	}

	public void setFontIndex(int fontIndex) {
		this.fontIndex = fontIndex;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public void setIndention(short indention) {
		this.indention = indention;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public void setRotation(short rotation) {
		this.rotation = rotation;
	}

	public void setShrinkToFit(boolean shrinkToFit) {
		this.shrinkToFit = shrinkToFit;
	}

	public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
		this.verticalAlignment = verticalAlignment;
	}

	public void setWrapText(boolean wrapText) {
		this.wrapText = wrapText;
	}

	public void setBottomBorderColorRGB(XSSFColor borderBottomColorRGB) {
		this.borderBottomColorRGB = borderBottomColorRGB;
	}

	public void setLeftBorderColorRGB(XSSFColor borderLeftColorRGB) {
		this.borderLeftColorRGB = borderLeftColorRGB;
	}

	public void setRightBorderColorRGB(XSSFColor borderRightColorRGB) {
		this.borderRightColorRGB = borderRightColorRGB;
	}

	public void setTopBorderColorRGB(XSSFColor borderTopColorRGB) {
		this.borderTopColorRGB = borderTopColorRGB;
	}

	public void setFillBackgroundColorRGB(XSSFColor fillBackgroundColorRGB) {
		this.fillBackgroundColorRGB = fillBackgroundColorRGB;
	}

	public void setFillForegroundColorRGB(XSSFColor fillForegroundColorRGB) {
		this.fillForegroundColorRGB = fillForegroundColorRGB;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alignment == null) ? 0 : alignment.hashCode());
		result = prime * result + ((borderBottom == null) ? 0 : borderBottom.hashCode());
		result = prime * result + borderBottomColor;
		result = prime * result + ((borderBottomColorRGB == null) ? 0 : borderBottomColorRGB.hashCode());
		result = prime * result + ((borderLeft == null) ? 0 : borderLeft.hashCode());
		result = prime * result + borderLeftColor;
		result = prime * result + ((borderLeftColorRGB == null) ? 0 : borderLeftColorRGB.hashCode());
		result = prime * result + ((borderRight == null) ? 0 : borderRight.hashCode());
		result = prime * result + borderRightColor;
		result = prime * result + ((borderRightColorRGB == null) ? 0 : borderRightColorRGB.hashCode());
		result = prime * result + ((borderTop == null) ? 0 : borderTop.hashCode());
		result = prime * result + borderTopColor;
		result = prime * result + ((borderTopColorRGB == null) ? 0 : borderTopColorRGB.hashCode());
		result = prime * result + dataFormat;
		result = prime * result + ((dataFormatString == null) ? 0 : dataFormatString.hashCode());
		result = prime * result + fillBackgroundColor;
		result = prime * result + ((fillBackgroundColorRGB == null) ? 0 : fillBackgroundColorRGB.hashCode());
		result = prime * result + fillForegroundColor;
		result = prime * result + ((fillForegroundColorRGB == null) ? 0 : fillForegroundColorRGB.hashCode());
		result = prime * result + ((fillPattern == null) ? 0 : fillPattern.hashCode());
		result = prime * result + fontIndex;
		result = prime * result + (hidden ? 1231 : 1237);
		result = prime * result + indention;
		result = prime * result + (locked ? 1231 : 1237);
		result = prime * result + rotation;
		result = prime * result + (shrinkToFit ? 1231 : 1237);
		result = prime * result + ((verticalAlignment == null) ? 0 : verticalAlignment.hashCode());
		result = prime * result + (wrapText ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CellStylePattern other = (CellStylePattern) obj;
		if (alignment != other.alignment)
			return false;
		if (borderBottom != other.borderBottom)
			return false;
		if (borderBottomColor != other.borderBottomColor)
			return false;
		if (borderBottomColorRGB == null) {
			if (other.borderBottomColorRGB != null)
				return false;
		} else if (!borderBottomColorRGB.equals(other.borderBottomColorRGB))
			return false;
		if (borderLeft != other.borderLeft)
			return false;
		if (borderLeftColor != other.borderLeftColor)
			return false;
		if (borderLeftColorRGB == null) {
			if (other.borderLeftColorRGB != null)
				return false;
		} else if (!borderLeftColorRGB.equals(other.borderLeftColorRGB))
			return false;
		if (borderRight != other.borderRight)
			return false;
		if (borderRightColor != other.borderRightColor)
			return false;
		if (borderRightColorRGB == null) {
			if (other.borderRightColorRGB != null)
				return false;
		} else if (!borderRightColorRGB.equals(other.borderRightColorRGB))
			return false;
		if (borderTop != other.borderTop)
			return false;
		if (borderTopColor != other.borderTopColor)
			return false;
		if (borderTopColorRGB == null) {
			if (other.borderTopColorRGB != null)
				return false;
		} else if (!borderTopColorRGB.equals(other.borderTopColorRGB))
			return false;
		if (dataFormat != other.dataFormat)
			return false;
		if (dataFormatString == null) {
			if (other.dataFormatString != null)
				return false;
		} else if (!dataFormatString.equals(other.dataFormatString))
			return false;
		if (fillBackgroundColor != other.fillBackgroundColor)
			return false;
		if (fillBackgroundColorRGB == null) {
			if (other.fillBackgroundColorRGB != null)
				return false;
		} else if (!fillBackgroundColorRGB.equals(other.fillBackgroundColorRGB))
			return false;
		if (fillForegroundColor != other.fillForegroundColor)
			return false;
		if (fillForegroundColorRGB == null) {
			if (other.fillForegroundColorRGB != null)
				return false;
		} else if (!fillForegroundColorRGB.equals(other.fillForegroundColorRGB))
			return false;
		if (fillPattern != other.fillPattern)
			return false;
		if (fontIndex != other.fontIndex)
			return false;
		if (hidden != other.hidden)
			return false;
		if (indention != other.indention)
			return false;
		if (locked != other.locked)
			return false;
		if (rotation != other.rotation)
			return false;
		if (shrinkToFit != other.shrinkToFit)
			return false;
		if (verticalAlignment != other.verticalAlignment)
			return false;
		if (wrapText != other.wrapText)
			return false;
		return true;
	}
	
}
