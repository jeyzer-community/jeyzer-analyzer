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

public class ConfigPoolMemorySetup {

	private static final Logger logger = LoggerFactory.getLogger(ConfigPoolMemorySetup.class);	
	
	private static final String JZRA_MEMORY_POOL = "memory_pool";
	private static final String JZRA_CATEGORY = "category";
	private static final String JZRA_NAME = "name";

	private static final String MEMORY_POOL_CATEGORY_OLD = "old";
	private static final String MEMORY_POOL_CATEGORY_YOUNG = "young";
	
	private List<String> youngPoolNames = new ArrayList<>(2);
	private List<String> oldPoolNames = new ArrayList<>(2);
	
	
	public ConfigPoolMemorySetup(Element memoryPoolNode) {
		loadMemoryPoolNames(memoryPoolNode);
	}

	private void loadMemoryPoolNames(Element node) {
		String category;
		String name;
	
		NodeList memPoolNodes = node.getElementsByTagName(JZRA_MEMORY_POOL);
		
		for (int i=0; i<memPoolNodes.getLength(); i++){
			Element memPoolNode = (Element)memPoolNodes.item(i);
			
			category = ConfigUtil.getAttributeValue(memPoolNode,JZRA_CATEGORY);
			name = ConfigUtil.getAttributeValue(memPoolNode,JZRA_NAME);
		
			if (category!= null && name != null){
				if (MEMORY_POOL_CATEGORY_OLD.equals(category))
					this.oldPoolNames.add(name);
				else if (MEMORY_POOL_CATEGORY_YOUNG.equals(category))
					this.youngPoolNames.add(name);
				else
					logger.warn("Unrecognized memory pool category " + category + " for pool name " + name);
			}
		}
	}

	public List<String> getYoungPoolNames() {
		return youngPoolNames;
	}

	public List<String> getOldPoolNames() {
		return oldPoolNames;
	}		
		
}


