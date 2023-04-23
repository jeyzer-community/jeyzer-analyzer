package org.jeyzer.monitor.util;

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





import com.google.common.primitives.Longs;



public final class CommanLineHelper {

	public static final String MAX_HEAP_PARAM = "Xmx";	
	
	private static enum Unit{B, KB, MB, GB, UNKNOWN}
	
	private CommanLineHelper(){
	}
	
	public static long parseHeapSize(String param){
		Unit unit = parseUnit(param);

		String value;
		if (Unit.B.equals(unit))
			value = param.substring(MAX_HEAP_PARAM.length(), param.length());
		else
			value = param.substring(MAX_HEAP_PARAM.length(), param.length()-1);
		
		Long heapSize = Longs.tryParse(value);
		if (heapSize == null)
			return Long.MAX_VALUE;
		
		// expressed value in mb
		return convert(heapSize, unit);
	}

	private static long convert(long heapSize, Unit unit) {
		switch(unit){
			case B:
				return heapSize / 1024L / 1024L;
			case KB:
				return heapSize / 1024L;
			case MB:
				return heapSize;
			case GB:
				return heapSize * 1024L;
			case UNKNOWN:
			default:
				return Long.MAX_VALUE;
		}
	}

	private static Unit parseUnit(String param) {
		String lastLetter = param.substring(param.length()-1);
		
		if (Longs.tryParse(lastLetter) != null)
			return Unit.B; // if no letter, it is in bytes
		
		// that's letter
		if ("k".equals(lastLetter) || "K".equals(lastLetter))
			return Unit.KB;
		else if ("m".equals(lastLetter) || "M".equals(lastLetter))
			return Unit.MB;
		else if ("g".equals(lastLetter) || "G".equals(lastLetter))
			return Unit.GB;
		else
			return Unit.UNKNOWN;
	}
	
}
