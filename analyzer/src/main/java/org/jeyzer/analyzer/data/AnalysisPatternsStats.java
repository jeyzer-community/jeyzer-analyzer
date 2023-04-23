package org.jeyzer.analyzer.data;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 - 2023 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jeyzer.analyzer.rule.PresetPatternRule;
import org.jeyzer.analyzer.rule.Rule;
import org.jeyzer.analyzer.rule.pattern.PresetPattern;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class AnalysisPatternsStats {

	private Multimap<String, PresetPattern> patterns = ArrayListMultimap.create();
	
	public void feed(List<Rule> rules) {
		for (Rule rule : rules)
			if (rule instanceof PresetPatternRule)
				processPresetPatternRule((PresetPatternRule) rule);
	}
	
	public List<String> getProfileNames(boolean includeNative) {
		List<String> names = new ArrayList<>(this.patterns.keySet());
		if (!includeNative) {
			// exclude low level profiles
			names.remove("Java");
			names.remove("Jeyzer");
		}
		Collections.sort(names);
		return names;
	}

	private void processPresetPatternRule(PresetPatternRule rule) {
		PresetPattern pattern = rule.getPattern();		
		if (pattern.hasHit())
			patterns.put(pattern.getSource(), pattern);
	}
}
