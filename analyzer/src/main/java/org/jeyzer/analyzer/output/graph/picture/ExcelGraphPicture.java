package org.jeyzer.analyzer.output.graph.picture;

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
import org.jeyzer.analyzer.config.graph.ConfigGraphResolution;

public class ExcelGraphPicture {
	
	public enum GraphType { ACTION_GRAPH, ACTION_GRAPH_SINGLE, CONTENTION_GRAPH, CONTENTION_GRAPH_SINGLE }
	
	private static final int NOT_INDEXED = -1;
	
	private String picturePath;
	private Cell parentLinkCell;
	private int width = 2560; // default
	private int height = 2048; // default
	private int index = NOT_INDEXED; // Excel picture index
	
	public ExcelGraphPicture(String picturePath, ConfigGraphResolution resolution){
		this.picturePath = picturePath;
		this.width = resolution.getWidth();
		this.height = resolution.getHeight();
	}

	public String getPicturePath() {
		return picturePath;
	}

	public void setParentLinkCell(Cell parentLinkCell) {
		this.parentLinkCell = parentLinkCell;
	}
	
	public Cell getParentLinkCell() {
		return this.parentLinkCell;
	}
	
	public int getExcelWidth() {
		return width;
	}

	public int getExcelHeight() {
		return height;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public boolean hasIndex(){
		return index != NOT_INDEXED; 
	}
	
}
