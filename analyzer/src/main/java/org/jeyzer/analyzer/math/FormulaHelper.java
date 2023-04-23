package org.jeyzer.analyzer.math;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormulaHelper {

	public static final int BYTES_IN_1_KB = 1024;
	public static final int BYTES_IN_1_MB = 1048586;
	public static final int BYTES_IN_1_GB = 1073741824;
	
	public static final long NANOSEC_IN_1_MIN = 60000000000L;
	public static final long NANOSEC_IN_1_SEC = 1000000000L;
	public static final long NANOSEC_IN_1_MS = 1000000L;
	
	public static final long DOUBLE_TO_LONG_NA = Double.doubleToRawLongBits(-1); // -4616189618054758400L
	public static final long DOUBLE_TO_LONG_ONE = Double.doubleToRawLongBits(1); // 4607182418800017408L
	public static final long DOUBLE_TO_LONG_MINUS_TWO = Double.doubleToRawLongBits(-2); // -4611686018427387904L
	
	private FormulaHelper(){
	}
	
	public static long convertToKb(long byteValue){
		return Math.round((float)byteValue / BYTES_IN_1_KB);
	}
	
	public static long convertToMb(long byteValue){
		return Math.round((float)byteValue / BYTES_IN_1_MB);
	}

	public static double convertToMb(double byteValue) {
		return byteValue / BYTES_IN_1_MB;
	}
	
	public static long convertToGb(long byteValue){
		return Math.round((float)byteValue / BYTES_IN_1_GB);
	}
	
	public static long convertToMinutes(long nanosecValue){
		return Math.round((float)nanosecValue / NANOSEC_IN_1_MIN);
	}
	
	public static long convertToSeconds(long nanosecValue){
		return Math.round((float)nanosecValue / NANOSEC_IN_1_SEC);
	}
	
	public static long convertToNanoseconds(Float msValue){
		return Math.round(msValue * NANOSEC_IN_1_MS);
	}
	
	public static String humanReadableByteCountBin(long bytes) {
	    long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
	    if (absB < 1024) {
	        return bytes + " B";
	    }
	    long value = absB;
	    CharacterIterator ci = new StringCharacterIterator("KMGTPE");
	    for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
	        value >>= 10;
	        ci.next();
	    }
	    value *= Long.signum(bytes);
	    return String.format("%.1f %cb", value / 1024.0, ci.current());
	}
	
	public static List<Integer> calculateMode(List<Integer> values) {
		final List<Integer> modes = new ArrayList<>();
		final Map<Integer, Integer> countMap = new HashMap<>();

		int max = -1;

		for (Integer value : values) {
			int count = 0;
			int n = value;

			if (countMap.containsKey(n)) {
				count = countMap.get(n) + 1;
			} else {
				count = 1;
			}

			countMap.put(n, count);

			if (count > max) {
				max = count;
			}
		}

		for (final Map.Entry<Integer, Integer> tuple : countMap.entrySet()) {
			if (tuple.getValue() == max) {
				modes.add(tuple.getKey());
			}
		}

		return modes;
	}
	
	public static Map<Integer, Integer> getDistribution(List<Integer> values) {
		final Map<Integer, Integer> countMap = new HashMap<>();

		for (Integer value : values) {
			int count = 0;
			int n = value;

			if (countMap.containsKey(n)) {
				count = countMap.get(n) + 1;
			} else {
				count = 1;
			}

			countMap.put(n, count);
		}

		return countMap;
	}
	
	public static double calculateStandardDeviation(List<Long> values, long average) {
		return Math.sqrt(calculateVariance(values, average));
	}	
	
	public static double calculateStandardDeviation(List<Integer> values, int average) {
		return Math.sqrt(calculateVariance(values, average));
	}

    public static double calculateVariance(List<Integer> values, int average) {
        double sum = 0.0;
        
        for (Integer value : values) {
            sum += Math.pow((double)value - average, 2);
        }
        
        return sum / values.size();
    }
    
	public static double calculateStandardDeviation(List<Double> values, double average) {
		return Math.sqrt(calculateVariance(values, average));
	}

    public static double calculateVariance(List<Double> values, double average) {
        double sum = 0.0;
        
        for (Double value : values) {
            sum += Math.pow((double)value - average, 2);
        }
        
        return sum / values.size();
    }
    
    public static double calculateVariance(List<Long> values, long average) {
        double sum = 0.0;
        
        for (Long value : values) {
            sum += Math.pow((double)value - average, 2);
        }
        
        return sum / values.size();
    }

	public static int percentRound(long numerator, long denominator) {
		return (int) Math.round(percent(numerator, denominator));
	}
    
	public static double percent(long numerator, long denominator) {
		if (numerator == 0)
			return 0;
		
		// should not happen. But let's protect
		if (denominator == 0)
			return 0;

		double percent = (double) numerator / denominator;
		percent *= 100;
		return percent;
	}
    
	public static Double percentNull(long numerator, long denominator) {
		if (denominator == 0)
			return null;
		
		return percent(numerator, denominator);
	}
	
}
