package org.jeyzer.analyzer.config.analysis;

import java.util.Arrays;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;

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

public class ConfigStackSorting {
	
	public enum StackSortingKey { RECORDING, THREAD_ID, THREAD_NAME }

	public static final String JZRA_THREAD_STACK_SORTING = "sorting";
	public static final String JZRA_THREAD_STACK_SORTING_KEY = "key";
	
	private StackSortingKey key = null;
	
	public ConfigStackSorting(Element stackNode) throws JzrInitializationException {
		this.key = loadStackSortingKey(stackNode);
	}
	
	public StackSortingKey getStackSortingKey(StackSortingKey defaultKey) {
		return key != null ? key : defaultKey != null ? defaultKey : StackSortingKey.RECORDING;
	}
	
	public static StackSortingKey loadStackSortingKey(Element stackNode) throws JzrInitializationException {
		Element sortingStackNode = ConfigUtil.getFirstChildNode(stackNode, JZRA_THREAD_STACK_SORTING);
		if (sortingStackNode == null)
			return null;
		
		String value = ConfigUtil.getAttributeValue(sortingStackNode,JZRA_THREAD_STACK_SORTING_KEY);
		try {
			return StackSortingKey.valueOf(value.trim().toUpperCase());
		}catch(IllegalArgumentException ex) {
			throw new JzrInitializationException("Stack sorting key : " + value + " is not recognized. "
				+ "Must be one of those values :" + Arrays.toString(StackSortingKey.values()).toLowerCase());
		}
	}
}
