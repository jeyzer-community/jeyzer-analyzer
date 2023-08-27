package org.jeyzer.monitor.config.engine;

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







import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.engine.event.info.Level;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public class ConfigMonitorThreshold {

	public static final String JZRM_TYPE = "type";
	private static final String JZRM_LEVEL = "level";
	private static final String JZRM_SUB_LEVEL = "sub_level";
	private static final String JZRM_TRUST_FACTOR = "trust_factor";
	private static final String JZRM_MESSAGE = "message";
	private static final String JZRM_COUNT = "count";
	private static final String JZRM_TIME = "time";
	private static final String JZRM_VALUE = "value";
	private static final String JZRM_PATTERN = "pattern";
	
	public static final String THRESHOLD_ACTION_APPLICATIVE = "action applicative";
	public static final String THRESHOLD_ACTION_SIGNAL = "action signal";
	public static final String THRESHOLD_ACTION_VALUE = "action value";
	public static final String THRESHOLD_ACTION_PATTERN = "action pattern";
	public static final String THRESHOLD_STACK_SIGNAL = "stack signal";
	public static final String THRESHOLD_STACK_SIGNAL_WITH_CONTEXT = "stack signal with context";
	public static final String THRESHOLD_STACK_VALUE = "stack value";
	public static final String THRESHOLD_STACK_PATTERN = "stack pattern";
	public static final String THRESHOLD_SESSION_APPLICATIVE = "session applicative";
	public static final String THRESHOLD_SESSION_PUBLISHER = "session publisher";
	public static final String THRESHOLD_SESSION_DIFF = "session diff";
	public static final String THRESHOLD_SESSION_SIGNAL = "session signal";
	public static final String THRESHOLD_SESSION_PATTERN = "session pattern";	
	public static final String THRESHOLD_SESSION_VALUE = "session value";
	public static final String THRESHOLD_SESSION_CUSTOM = "session custom";
	public static final String THRESHOLD_SESSION_CUSTOM_WITH_CONTEXT = "session custom with context";
	public static final String THRESHOLD_GLOBAL_DIFF = "global diff";
	public static final String THRESHOLD_GLOBAL_SIGNAL = "global signal";
	public static final String THRESHOLD_GLOBAL_PATTERN = "global pattern";
	public static final String THRESHOLD_GLOBAL_VALUE = "global value";
	public static final String THRESHOLD_GLOBAL_CUSTOM = "global custom";
	public static final String THRESHOLD_SYSTEM_APPLICATIVE = "system applicative";
	public static final String THRESHOLD_SYSTEM_PATTERN = "system pattern";
	public static final String THRESHOLD_SYSTEM_VALUE = "system value";
	public static final String THRESHOLD_SYSTEM_SIGNAL = "system signal";
	
	protected static List<String> supportedParameters = new ArrayList<>();
	
	private static List<String> supportedTaskThresholds = new ArrayList<>();
	private static List<String> supportedSessionThresholds = new ArrayList<>();
	private static List<String> supportedSystemThresholds = new ArrayList<>();

	private static final String THRESHOLD_ERROR_PREFIX = "Monitoring rule threshold ";
	
	private static final short DEFAULT_TRUST_FACTOR = 100;
	
	static{
		supportedTaskThresholds.add(THRESHOLD_ACTION_APPLICATIVE);
		supportedTaskThresholds.add(THRESHOLD_ACTION_SIGNAL);
		supportedTaskThresholds.add(THRESHOLD_ACTION_VALUE);
		supportedTaskThresholds.add(THRESHOLD_ACTION_PATTERN);
		supportedTaskThresholds.add(THRESHOLD_STACK_SIGNAL);
		supportedTaskThresholds.add(THRESHOLD_STACK_SIGNAL_WITH_CONTEXT);
		supportedTaskThresholds.add(THRESHOLD_STACK_VALUE);
		supportedTaskThresholds.add(THRESHOLD_STACK_PATTERN);
		
		supportedSessionThresholds.add(THRESHOLD_SESSION_APPLICATIVE);
		supportedSessionThresholds.add(THRESHOLD_SESSION_PUBLISHER);
		supportedSessionThresholds.add(THRESHOLD_SESSION_SIGNAL);
		supportedSessionThresholds.add(THRESHOLD_SESSION_DIFF);
		supportedSessionThresholds.add(THRESHOLD_SESSION_VALUE);
		supportedSessionThresholds.add(THRESHOLD_SESSION_PATTERN);
		supportedSessionThresholds.add(THRESHOLD_SESSION_CUSTOM);
		supportedSessionThresholds.add(THRESHOLD_SESSION_CUSTOM_WITH_CONTEXT);
		supportedSessionThresholds.add(THRESHOLD_GLOBAL_DIFF);
		supportedSessionThresholds.add(THRESHOLD_GLOBAL_SIGNAL);
		supportedSessionThresholds.add(THRESHOLD_GLOBAL_PATTERN);
		supportedSessionThresholds.add(THRESHOLD_GLOBAL_VALUE);
		supportedSessionThresholds.add(THRESHOLD_GLOBAL_CUSTOM);
		
		supportedSystemThresholds.add(THRESHOLD_SYSTEM_APPLICATIVE);
		supportedSystemThresholds.add(THRESHOLD_SYSTEM_PATTERN);
		supportedSystemThresholds.add(THRESHOLD_SYSTEM_VALUE);
		supportedSystemThresholds.add(THRESHOLD_SYSTEM_SIGNAL);
		
		supportedParameters.add(JZRM_TYPE);
		supportedParameters.add(JZRM_LEVEL);
		supportedParameters.add(JZRM_SUB_LEVEL);
		supportedParameters.add(JZRM_MESSAGE);
		supportedParameters.add(JZRM_COUNT);
		supportedParameters.add(JZRM_TIME);
		supportedParameters.add(JZRM_VALUE);
		supportedParameters.add(JZRM_PATTERN);
	}
	
	private final String name;
	private final String ref;
	
	private final String message;
	private final Level level;
	private final SubLevel subLevel;
	private final short trustFactor;
	
	private long time = -1;
	private int count = -1;
	
	private Long value = null;
	private Pattern pattern = null;
	
	private Map<String, String> customParams = new HashMap<>();

	/**
	 * Analyzer threshold constructor
	 */
	public ConfigMonitorThreshold(Element thresholdNode, String name, String ref) throws JzrInitializationException{
		this.name = name;
		this.ref = ref;
		
		this.level = loadLevel(thresholdNode);
		this.subLevel = loadSubLevel(thresholdNode);
		this.trustFactor = loadTrustFactor(thresholdNode);
		this.message = ConfigUtil.getAttributeValue(thresholdNode,JZRM_MESSAGE);
		loadTimeOrCountConstraint(thresholdNode);
		this.value = loadValue(thresholdNode);
		this.pattern = loadPattern(thresholdNode);
		loadCustomAttributes(thresholdNode);
	}

	/**
	 * Analyzer applicative threshold constructor
	 */
	public ConfigMonitorThreshold(String name, String ref, Level level, SubLevel subLevel, String message, short trustFactor){
		this.name = name;
		this.ref = ref;
		this.level = level;
		this.subLevel = subLevel;
		this.trustFactor = trustFactor;
		this.message = message;
	}
	
	/**
	 * Analyzer internal threshold constructor
	 */
	protected ConfigMonitorThreshold(String name, String ref, Level level, String message){
		this.name = name;
		this.ref = ref;
		this.level = level;
		this.subLevel = SubLevel.NOT_SET;
		this.trustFactor = DEFAULT_TRUST_FACTOR;
		this.message = message;
		this.count = 1; // will make election automatic
	}

	public String getName(){
		return name;
	}
	
	public String getRef(){
		return ref;
	}

	public String getMessage() {
		return message;
	}

	public Level getLevel() {
		return level;
	}
	
	public SubLevel getSubLevel() {
		return subLevel;
	}	

	public long getTime() {
		return time;
	}

	public int getCount() {
		return count;
	}
	
	public boolean isTimeBound(){
		return this.time != -1;
	}

	public boolean isValueBound(){
		return this.value != null;
	}	
	
	public long getValue() {
		return (this.value != null)? value : -1;
	}
	
	public Pattern getPattern(){
		return this.pattern; // can be null
	}
	
	public String getCustomParameter(String name){
		return this.customParams.get(name);
	}
	
	public Map<String, String> getCustomParameters(){
		return this.customParams;
	}
	
	private void loadCustomAttributes(Element thresholdNode) {
		NamedNodeMap attributes = thresholdNode.getAttributes();
		for (int i=0; i<attributes.getLength(); i++){
			Attr attr = (Attr)attributes.item(i);
			if (isCustomParam(attr.getName())){
				customParams.put(attr.getName(), ConfigUtil.resolveValue(attr.getNodeValue()));
			}
		}
	}

	private Level loadLevel(Element thresholdNode) throws JzrInitializationException {
		String levelValue = ConfigUtil.getAttributeValue(thresholdNode,JZRM_LEVEL);
		if (levelValue == null || levelValue.isEmpty())
			throw new JzrInitializationException(THRESHOLD_ERROR_PREFIX + this.getName() + " is missing level.");
		Level loadedLevel = Level.getLevel(levelValue);
		if (Level.UNKNOWN.equals(loadedLevel))
			throw new JzrInitializationException(THRESHOLD_ERROR_PREFIX + this.getName() + " has invalid level set : "+ levelValue);
		return loadedLevel;
	}
	
	private SubLevel loadSubLevel(Element thresholdNode) throws JzrInitializationException {
		String subLevelValue = ConfigUtil.getAttributeValue(thresholdNode,JZRM_SUB_LEVEL);
		SubLevel loadedSubLevel = SubLevel.getSubLevel(subLevelValue);
		if (SubLevel.INVALID.equals(loadedSubLevel))
			throw new JzrInitializationException(THRESHOLD_ERROR_PREFIX + this.getName() + " has invalid sub level set : "+ subLevelValue);
		return loadedSubLevel;
	}
	
	private short loadTrustFactor(Element thresholdNode) throws JzrInitializationException {
		String trustFactorValue = ConfigUtil.getAttributeValue(thresholdNode,JZRM_TRUST_FACTOR);
		if (trustFactorValue == null || trustFactorValue.isEmpty())
			return DEFAULT_TRUST_FACTOR;
		
		Integer value = Ints.tryParse(trustFactorValue);
		if (value == null)
			throw new JzrInitializationException(THRESHOLD_ERROR_PREFIX + this.getName() + " has invalid trust factor : "+ trustFactorValue);
		if (value <0 || value > 100)
			throw new JzrInitializationException(THRESHOLD_ERROR_PREFIX + this.getName() + " has trust factor value which is not between 0 and 100. Given value is : "+ value);
		return value.shortValue();
	}

	private Pattern loadPattern(Element thresholdNode) {
		String configPattern = ConfigUtil.getAttributeValue(thresholdNode,JZRM_PATTERN);
		if (configPattern == null || configPattern.isEmpty())
			return null;
		
		return Pattern.compile(configPattern);
	}

	private Long loadValue(Element thresholdNode) {
		String configValue = ConfigUtil.getAttributeValue(thresholdNode,JZRM_VALUE);
		if (configValue == null || configValue.isEmpty())
			return null;
		
		return Longs.tryParse(configValue); // null if invalid 
	}

	private void loadTimeOrCountConstraint(Element ruleNode) throws JzrInitializationException {
		String countStr = ConfigUtil.getAttributeValue(ruleNode,JZRM_COUNT); 
		String timeStr = ConfigUtil.getAttributeValue(ruleNode,JZRM_TIME);
		
		if (!countStr.isEmpty() && !timeStr.isEmpty())
			throw new JzrInitializationException(THRESHOLD_ERROR_PREFIX + this.getName() + " has both time and count attributes set. Only one is accepted.");
		
		if (countStr.isEmpty() && timeStr.isEmpty()){  // not defined
			this.count = 1;
			return;
		}
		
		if (!countStr.isEmpty())
			this.count = loadCountConstraint(countStr);
		else
			this.time = loadTimeConstraint(timeStr);
	}
	
	private long loadTimeConstraint(String timeStr) throws JzrInitializationException {
		Duration duration = ConfigUtil.parseDuration(timeStr);
		
		if (duration == null)
			throw new JzrInitializationException(THRESHOLD_ERROR_PREFIX + this.getName() + " has invalid time attribute : " + timeStr);
		
		return duration.getSeconds();
	}

	private int loadCountConstraint(String countStr) throws JzrInitializationException {
		Integer countValue = Ints.tryParse(countStr);
		if (countValue != null)
			return countValue;
		else
			throw new JzrInitializationException(THRESHOLD_ERROR_PREFIX + this.getName() + " has invalid count attribute : " + countStr);
	}
	

	public static boolean isValidTaskType(String type){
		return supportedTaskThresholds.contains(type);
	}

	public static boolean isValidSessionType(String type){
		return supportedSessionThresholds.contains(type);
	}
	
	public static boolean isValidSystemType(String type){
		return supportedSystemThresholds.contains(type);
	}

	public static boolean isCustomParam(String param){
		return !supportedParameters.contains(param);
	}

	public short getTrustFactor() {
		return trustFactor;
	}
}
