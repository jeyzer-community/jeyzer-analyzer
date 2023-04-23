package org.jeyzer.analyzer.util;

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

import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeyzer.analyzer.data.ProcessCard;
import org.jeyzer.analyzer.data.jar.ProcessJars;
import org.jeyzer.analyzer.data.module.ProcessModules;
import org.jeyzer.analyzer.session.JzrSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyHelper {

	public static final Logger logger = LoggerFactory.getLogger(PropertyHelper.class);
	
	public static final String PROCESS_CARD_TOKEN = "@@";
	public static final String PROCESS_JARS_TOKEN = "%%";
	public static final String PROCESS_MODULES_TOKEN = "##";
	
	public static final String PROPERTY_NAME_PATTERN = "(?<property>[A-Za-z0-9-_.]+)";
	
	public static final Pattern processCardPattern = Pattern.compile(PROCESS_CARD_TOKEN + PROPERTY_NAME_PATTERN + PROCESS_CARD_TOKEN);
	public static final Pattern processJarsPattern = Pattern.compile(PROCESS_JARS_TOKEN + PROPERTY_NAME_PATTERN + PROCESS_JARS_TOKEN);
	public static final Pattern processModulesPattern = Pattern.compile(PROCESS_MODULES_TOKEN + PROPERTY_NAME_PATTERN + PROCESS_MODULES_TOKEN);
	
	private PropertyHelper(){
	}
	
	public static String expandRecordingProperties(String text, String name, JzrSession session) {
		String result = null;
		
		if (text == null)
			return result;

		// expand process card properties
		result = PropertyHelper.expandProcessCardProperties(text, session.getProcessCard());
		if (result.contains(PropertyHelper.PROCESS_CARD_TOKEN))
			logger.warn("Input text " + name + " is not properly expanded. It still contains property card tokens (" + PropertyHelper.PROCESS_CARD_TOKEN + ").");
		
		// expand process jar versions
		result = PropertyHelper.expandProcessJarVersions(result, session.getProcessJars());
		if (result.contains(PropertyHelper.PROCESS_JARS_TOKEN))
			logger.warn("Input text " + name + " is not properly expanded. It still contains process jar tokens (" + PropertyHelper.PROCESS_JARS_TOKEN + ").");
		
		// expand process module versions
		result = PropertyHelper.expandProcessModuleVersions(result, session.getProcessModules());
		if (result.contains(PropertyHelper.PROCESS_MODULES_TOKEN))
			logger.warn("Input text " + name + " is not properly expanded. It still contains process module tokens (" + PropertyHelper.PROCESS_MODULES_TOKEN + ").");
		
		return result;
	}
	
	public static String expandProcessCardProperties(String text, ProcessCard processCard) {
		if (processCard == null)
			return text;
		return replaceTokens(
					text,
					processCardPattern,
					match -> processCard.getValue(match.group("property")).getValue());
	}
	
	public static String expandProcessJarVersions(String text, ProcessJars processJars) {
		if (processJars == null)
			return text;
		return replaceTokens(
					text,
					processJarsPattern,
					match -> processJars.getProcessJarVersion(match.group("property")).getJarVersion());
	}
	
	public static String expandProcessModuleVersions(String text, ProcessModules processModules) {
		if (processModules == null)
			return text;
		return replaceTokens(
					text,
					processModulesPattern,
					match -> processModules.getProcessModule(match.group("property")).getVersion());
	}
	
	public static String expandProperties(String text, Map<String, String> map, Pattern pattern) {
		if (map == null)
			return text;
		return replaceTokens(
					text,
					pattern,
					match -> map.get(match.group("property")));
	}
	
	public static String expandProperties(String text, Properties props, Pattern pattern) {
		if (props == null)
			return text;
		return replaceTokens(
					text,
					pattern,
					match -> (String)props.get(match.group("property")));
	}
	
    /**
     * Replace all the tokens in an input using the algorithm provided for each
     * @param original original string
     * @param tokenPattern the pattern to match with
     * @param converter the conversion to apply
     * @return the substituted string
     */
    public static String replaceTokens(String original, Pattern tokenPattern,
                                       Function<Matcher, String> converter) {
        int lastIndex = 0;
        StringBuilder output = new StringBuilder();
        Matcher matcher = tokenPattern.matcher(original);
        while (matcher.find()) {
            output.append(original, lastIndex, matcher.start())
                    .append(converter.apply(matcher));

            lastIndex = matcher.end();
        }
        if (lastIndex < original.length()) {
            output.append(original, lastIndex, original.length());
        }
        return output.toString();
    }
    
//    public static final void main(String[] args) {
//    	Map<String, String> props = new HashMap<>();
//    	props.put("jzr.jar.jeyzer-demo.jar.Implementation-ArtifactId", "Hello");
//    	props.put("first-name", "John");
//    	
////    	String text = "He said : @@intro@@ world, my name is @@first-name@@";
//    	String text = "@@jzr.jar.jeyzer-demo.jar.Implementation-ArtifactId@@";
//    	String result = expandProperties(text, props);
//    	System.out.print(result);
//    }
}
