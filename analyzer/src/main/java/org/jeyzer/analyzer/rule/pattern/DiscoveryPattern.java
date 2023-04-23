package org.jeyzer.analyzer.rule.pattern;

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



import static org.jeyzer.analyzer.rule.pattern.Matcher.NO_LINE_MATCH;





import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeyzer.analyzer.error.JzrInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class DiscoveryPattern extends org.jeyzer.analyzer.rule.pattern.Pattern{

	protected static final Logger logger = LoggerFactory.getLogger(DiscoveryPattern.class);
	
	public enum FOCUS { 
		CLASS("class"), 
		METHOD("method"), 
		BOTH("both");
		
		private String focus;
		
		FOCUS(String focus){
			this.focus = focus;
		}
		
		@Override
		public String toString(){
			return focus;
		}
		
	}
	
	private static final String GETTER_PREFIX = "get";
	private static final String SETTER_PREFIX = "set";
	
	private String discoveryName;
	private java.util.regex.Pattern regex; // optional
	private FOCUS focus;
	
	Map<String, String> discoveries = new HashMap<>();

	// temporary values
	protected String currentName;
	
	public DiscoveryPattern(String discoveryName, FOCUS focus, String source){
		super(source);
		this.discoveryName = capitalize(discoveryName);
		this.focus = focus;
	}

	public DiscoveryPattern(String discoveryName, FOCUS focus, String regex, String source){
		super(source);
		this.discoveryName = capitalize(discoveryName);
		if (regex != null && !regex.isEmpty())
			this.regex = Pattern.compile(regex);
		this.focus = focus;
	}

	@Override
	public void updatePriority(int p) throws JzrInitializationException {
		// do nothing
	}

	@Override
	public List<String> getPatterns() {
		return new ArrayList<String>(this.discoveries.keySet());  //code lines
	}
	
	@Override
	public int getLineMatch() {
		return lineMatch;
	}
	
	@Override
	public String getName() {
		return this.currentName; // be careful : this is the last matching name
	}
	
	@Override
	public String getDisplayName() {
		return discoveryName 
				+ "   Focus=" + this.focus.toString() 
				+ " Regex=" + (this.regex != null ? this.regex.pattern():"None");
	}
	
	@Override
	public String getType() {
		return null; // not supported
	}
	
	public String getFocus(){
		return this.focus.toString();
	}
	
	public String getRegex(){
		return this.regex.pattern();
	}
	
	@Override
	public boolean matchPattern(List<String> lines, int linesCount){
		this.lineMatch = NO_LINE_MATCH;
		int indexLine = lines.size()-1;
		
		if (lines == null || lines.isEmpty())
			return false;

		// do it in reverse order as function/operation discovery must be read from the top
		final List<String> reversedLines = Lists.reverse(lines);
		
		// compare with 1 line patterns
		for (String line : reversedLines){
			int pos = line.indexOf(discoveryName);
			if (pos != -1 && matchRegex(line)){
				currentName = discoveries.get(line);
				if (currentName == null){
					currentName = computeName(line, pos);
					if (currentName == null){
						return false;
					}
					discoveries.put(line, new String(currentName)); // copy	
				}

				this.lineMatch = linesCount - indexLine -1; // must be read from stack top
				this.hitCount++;
				return true;
			}
			indexLine--;
		}
		
		return false;
	}
	
	@Override
	public boolean isLowLevelPattern(){
		return false;
	}
	
	private String computeName(String line, int discoveryPos) {
		// discoveryName start at pos
		
		// example :
		//  discoveryName = Balloon 
		//  focus= both
		//  line  = org.balloon.Manager.loadBalloons();
		//  computed name = Load balloons 

		//  line  = org.balloon.BalloonManager.exit();
		//  computed name = Balloon manager exit

		//  line  = org.balloon.Field$BalloonManager.exitField();
		//  computed name = Balloon manager exit field

		//  focus= method
		//  line  = org.balloon.Field$BalloonManager.hitBalloon();
		//  computed name = Hit balloon
		
		// Excluded :
		// inner class
		//  line  = org.balloon.BalloonManager$Player.exit();
		// accessors
		//  line  = org.balloon.Manager.getBalloon();
		//  line  = org.balloon.Manager.setBalloon();
		
		// find all occurrences
		List<Integer> discoveryOccurences = new ArrayList<>(2);
		while(discoveryPos != -1){
			discoveryOccurences.add(discoveryPos);
			discoveryPos = line.indexOf(discoveryName, discoveryPos + discoveryName.length());
		}
		
		for(int discoveryCandidatePos : discoveryOccurences){
			String result = computeCandidate(line, discoveryCandidatePos);
			if (result != null)
				return result;
		}
		
		return null; // not found
	}
	
	private String computeCandidate(String line, int discoveryCandidatePos) {
		// 1. find end separator : parenthesis or dot

		boolean methodFound = true;
		int endParenthesisPos = line.indexOf('(', discoveryCandidatePos); // method
		int endDotPos = line.indexOf('.', discoveryCandidatePos); // class
		int endDollarPos = line.indexOf('$', discoveryCandidatePos); // inner class
		
		if (endParenthesisPos == -1)
			return null; // ignore source code case : org.balloon.BalloonManager$Player.exit(BalloonManager.java)
		
		if (endDollarPos != -1 && (endDollarPos < endParenthesisPos ||  endDollarPos < endDotPos))
			return null; // ignore case : org.balloon.BalloonManager$Player.exit(); 
		
		if (endDotPos != -1 && endParenthesisPos != -1 && endDotPos < endParenthesisPos){
			methodFound = false;
		}
		
		String methodName = null;
		String className = null;
		String lineBegin;
		
		if (methodFound){
			if (focus == FOCUS.CLASS)
				return null;
			// find method name
			lineBegin = line.substring(0, discoveryCandidatePos);
			int startMethodPos = lineBegin.lastIndexOf('.');
			if (startMethodPos == -1)
				return null; // weird
			
			// we have the method name, ignore accessors
			methodName = line.substring(startMethodPos+1, endParenthesisPos); 
			if (methodName.startsWith(GETTER_PREFIX) || methodName.startsWith(SETTER_PREFIX))
				return null;
			
			// process the class name
			if (focus == FOCUS.BOTH){
				int classStartPos = getClassStartPos(line.substring(0, startMethodPos));
				className = line.substring(classStartPos, startMethodPos);
			}
		}
		else{
			if (focus == FOCUS.METHOD)
				return null;
			
			// process the class name
			int classStartPos = getClassStartPos(line.substring(0, discoveryCandidatePos));
			if (classStartPos==-1)
				return null;
			className = line.substring(classStartPos, endDotPos);
			
			if (focus == FOCUS.BOTH){
				// process the method name
				methodName = line.substring(endDotPos+1, endParenthesisPos);
			}
		}
		
		// we have the class name, so create final name
		String computedName;
		if (focus == FOCUS.BOTH){
			computedName = className + " " + methodName;
		}
		else if (focus == FOCUS.METHOD){
			computedName = methodName;
		}
		else{
			computedName = className;
		}
		computedName = insertSpaces(computedName);
		computedName = computedName.toLowerCase();
		computedName = capitalize(computedName);

		if (logger.isDebugEnabled())
			logger.debug("Dynamic pattern found : {}", computedName);
		
		return computedName;
	}

	private int getClassStartPos(String line){
		int startDotPos = line.lastIndexOf('.'); 
		int startDollarPos = line.lastIndexOf('$'); 
		
		if (startDotPos == -1 && startDollarPos == -1)
			return -1;
		
		return startDotPos > startDollarPos ? startDotPos+1 : startDollarPos+1;
	}

	private String insertSpaces(String value) {
		int index = 1;
		if (value == null || value.isEmpty())
			return value;
		
		while(true){
			if (index == value.length())
				break;
			char letter = value.charAt(index);
			if (Character.isUpperCase(letter)){
				value = value.substring(0, index) + " " + value.substring(index);
				index++;
			}
			index++;
		}
		
		return value;
	}

	private boolean matchRegex(String value) {
		if (regex == null)
			return true;
		
		Matcher matcher = regex.matcher(value);
		if (matcher.find())
			return true;
		
		return false;
	}

	private String capitalize(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	public static void main(String[] args) {
		
		List<String> lines = new ArrayList<>();
		lines.add("at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)");
		lines.add("at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1093)");
		lines.add("sun.rmi.transport.tcp.TCPTransport$AcceptLoop.executeAcceptLoop(TCPTransport.java:400)");
		lines.add("at sun.rmi.transport.tcp.TCPTransport$AcceptLoop.executeAcceptLoop(TCPTransport.java:400)");
		lines.add("at com.sun.jmx.mbeanserver.MBeanSupport.invokePotatoes(MBeanSupport.java:252)");
		lines.add("at javax.management.StandardMBean.invokePotatoes(StandardMBean.java:405)");
		lines.add("at javax.management.BalloonManager.loadBalloons(BalloonManager.java:405)");
		

		DiscoveryPattern pattern = new DiscoveryPattern("queue", FOCUS.BOTH, "test"); // class
		pattern.matchPattern(lines, lines.size()); // Delayed work queue take

		pattern = new DiscoveryPattern("Transport", FOCUS.BOTH, "test"); // inner class - excluded
		pattern.matchPattern(lines, lines.size()); // Accept loop execute accept loop
			
		pattern = new DiscoveryPattern("loop", FOCUS.BOTH, "test"); // method
		pattern.matchPattern(lines, lines.size()); // nothing
		
		pattern = new DiscoveryPattern("potatoes", FOCUS.BOTH, "\\Qcom.sun\\E.*", "test"); // 1 method out of 2
		pattern.matchPattern(lines, lines.size()); // M bean support invoke potatoes
		
		pattern = new DiscoveryPattern("balloon", FOCUS.METHOD, "javax.*", "test"); // 1 method and not the class
		pattern.matchPattern(lines, lines.size()); // Load balloons
		
		pattern = new DiscoveryPattern("balloon", FOCUS.CLASS, "javax.*", "test"); // 1 class and not the method
		pattern.matchPattern(lines, lines.size()); // Balloon Manager
		
		pattern = new DiscoveryPattern("balloon", FOCUS.BOTH, "javax.*", "test"); // class and method
		pattern.matchPattern(lines, lines.size()); // Balloon Manager load balloons
		
		pattern = new DiscoveryPattern("invoke", FOCUS.BOTH, "test"); // class and method
		pattern.matchPattern(lines, lines.size()); // nothing (would work with loadInvoke. Capital letter as first character is mandatory)
	}
	
}
