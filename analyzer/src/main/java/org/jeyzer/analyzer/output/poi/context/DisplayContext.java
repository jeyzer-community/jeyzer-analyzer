package org.jeyzer.analyzer.output.poi.context;

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
import java.util.Map;
import java.util.Random;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture;
import org.jeyzer.analyzer.output.poi.CellRefRepository;
import org.jeyzer.analyzer.output.poi.style.CellStyles;
import org.jeyzer.analyzer.setup.JzrSetupManager;

public class DisplayContext{
	
	private Workbook wb;
	private CellStyles styles;
	private CellRefRepository cellRefRepository;
	private JzrSetupManager setup;
	private Map<String,MenuItemsContext> menuItems = new HashMap<>();
	private MonitoringRepository monitoringRepository;
	private Map<String, ExcelGraphPicture> graphPictureRepository;
	private String magicWord;
	private Random random = new Random();
	
	// Important : must be called only once for a report generation
	public DisplayContext(Workbook wb, CellStyles styles, JzrSetupManager setup, CellRefRepository cellRefRepository){
		this.wb = wb;
		this.setup = setup;
		this.styles = styles;
		this.cellRefRepository = cellRefRepository;
		this.monitoringRepository = new MonitoringRepository();
		this.graphPictureRepository = new HashMap<String, ExcelGraphPicture>();
		this.magicWord = generateMagicWord();
	}
	
	public DisplayContext(DisplayContext context){
		this.wb = context.getWorkbook();
		this.styles = context.getCellStyles();
		this.cellRefRepository = context.getCellRefRepository();
		this.menuItems = context.getMenuItems();
		this.setup = context.getSetupManager();
		this.monitoringRepository = context.getMonitoringRepository();
		this.graphPictureRepository = context.getGraphPictureRepository();
		this.magicWord = context.getMagicWord();
	}
	
   	public Workbook getWorkbook() {
			return wb;
		}

	public CellStyles getCellStyles() {
		return styles;
	}

	public JzrSetupManager getSetupManager() {
		return setup;
	}
	
	public CellRefRepository getCellRefRepository(){
		return this.cellRefRepository;
	}
	
	public MonitoringRepository getMonitoringRepository() {
		return monitoringRepository;
	}

	public void registerMenuItems(String sheetName, MenuItemsContext ctx){
		this.menuItems.put(sheetName, ctx);
	}
	
	public Map<String,MenuItemsContext> getMenuItems(){
		return this.menuItems; 
	}
	
	public Map<String, ExcelGraphPicture> getGraphPictureRepository() {
		return graphPictureRepository;
	}
	
	public String getMagicWord() {
		return this.magicWord;
	}
	
	private String generateMagicWord() {
		// Strings are converted to bytes through https://onlineutf8tools.com/convert-utf8-to-bytes
		// ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890
		String magicWordSalt = new String(hexStringToByteArray("4142434445464748494a4b4c4d4e4f505152535455565758595a31323334353637383930"));
        StringBuilder salt = new StringBuilder();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (random.nextFloat() * magicWordSalt.length());
            salt.append(magicWordSalt.charAt(index));
        }
        return salt.toString();
    }
	
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

	public static class MenuItemsContext {
		
		public static final int NO_ITEMS = -1;
		public static final int ITEMS_NA = -2;
		
		private int itemsCount;
		private int graphItemsCount;
		private XSSFColor criticalColor;
		
		public MenuItemsContext (int itemsCount, int graphItemsCount){
			this.itemsCount = itemsCount;
			this.graphItemsCount = graphItemsCount;
		}

		public int getItemsCount() {
			return itemsCount;
		}

		public int getGraphItemsCount() {
			return graphItemsCount;
		}

		public void setCriticalColor(XSSFColor criticalColor) {
			this.criticalColor = criticalColor;
		}
		
		public XSSFColor getCriticalColor() {
			return this.criticalColor; // can be null
		}
		
		public boolean hasCriticalItems() {
			return this.criticalColor != null;
		}
	}
}
