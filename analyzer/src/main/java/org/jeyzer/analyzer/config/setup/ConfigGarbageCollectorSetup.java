package org.jeyzer.analyzer.config.setup;

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







import java.util.ArrayList;
import java.util.List;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigGarbageCollectorSetup {

	private static final Logger logger = LoggerFactory.getLogger(ConfigPoolMemorySetup.class);	
	
	private static final String JZRA_GARBAGE_COLLECTOR = "garbage_collector";
	private static final String JZRA_CATEGORY = "category";
	private static final String JZRA_NAME = "name";

	private static final String GARBAGE_COLLECTOR_CATEGORY_OLD = "old";
	private static final String GARBAGE_COLLECTOR_CATEGORY_YOUNG = "young";
	
	private List<String> youngGCNames = new ArrayList<>(4);
	private List<String> oldGCNames = new ArrayList<>(4);
	
	public ConfigGarbageCollectorSetup(Element gcsNode) {
		loadMemoryPoolNames(gcsNode);
	}

	private void loadMemoryPoolNames(Element node) {
		String category;
		String name;
	
		NodeList gcNodes = node.getElementsByTagName(JZRA_GARBAGE_COLLECTOR);
		
		for (int i=0; i<gcNodes.getLength(); i++){
			Element gcNode = (Element)gcNodes.item(i);
			
			category = ConfigUtil.getAttributeValue(gcNode,JZRA_CATEGORY);
			name = ConfigUtil.getAttributeValue(gcNode,JZRA_NAME);
		
			if (category!= null && name != null){
				if (GARBAGE_COLLECTOR_CATEGORY_OLD.equals(category))
					this.oldGCNames.add(name);
				else if (GARBAGE_COLLECTOR_CATEGORY_YOUNG.equals(category))
					this.youngGCNames.add(name);
				else
					logger.warn("Unrecognized garbage collector category " + category + " for pool name " + name);
			}
		}
	}

	public List<String> getYoungGCNames() {
		return youngGCNames;
	}

	public List<String> getOldGCNames() {
		return oldGCNames;
	}

}
