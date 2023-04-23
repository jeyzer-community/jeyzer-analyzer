package org.jeyzer.analyzer.data.action;

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







import java.util.Comparator;

public final class ActionGraphSectionComparator {
	
	private ActionGraphSectionComparator(){
	}

	public static class TreeFunctionAndOperationSizeComparator implements Comparator<ActionGraphSection>{
		@Override
		public int compare(ActionGraphSection e1, ActionGraphSection e2) {
			int e1PossibleNodes = e1.getTreeFunctions().size() + e1.getTreeOperations().size();
			int e2PossibleNodes = e2.getTreeFunctions().size() + e2.getTreeOperations().size();
			
			if (e1PossibleNodes == e2PossibleNodes)
				// if equal, filter on action size
				return e2.getStackCount() > e1.getStackCount() ? 1 : e2.getStackCount()==e1.getStackCount() ? 0 : -1;
				
			return e2PossibleNodes > e1PossibleNodes ? 1 : -1;
		}
	}
	
	public static class StackCountComparable implements Comparator<ActionGraphSection>{
		 
	    @Override
	    public int compare(ActionGraphSection s1, ActionGraphSection s2) {
	        return s1.getStackCount()<s2.getStackCount() ? 1 : s1.getStackCount()==s2.getStackCount() ? 0 : -1;
	    }
	}

}
