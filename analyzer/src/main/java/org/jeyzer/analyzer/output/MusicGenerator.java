package org.jeyzer.analyzer.output;

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







import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.jeyzer.analyzer.config.ConfigReport;
import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.output.jmusic.MozartPhraseList;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.analyzer.util.SystemHelper;

import jm.music.data.CPhrase;
import jm.music.data.Part;
import jm.music.data.Score;
import jm.music.tools.Mod;
import jm.util.Write;

public class MusicGenerator {

	private static final String JZRA_DISABLE_EASTER_EGG = "${JZRA_DISABLE_EASTER_EGG}"; 
	private final GregorianCalendar aprilFoolsDay = new GregorianCalendar(2000,Calendar.APRIL,1);
	private final GregorianCalendar christmasDay = new GregorianCalendar(2000,Calendar.DECEMBER,25);
	private final GregorianCalendar musicPartyDay = new GregorianCalendar(2000,Calendar.JUNE,21);
	
	private static final String AUDIO_ENABLED = "enabled";
	private static final String AUDIO_PATH_DIR = "output_directory";
	private static final String AUDIO_FILE_NAME = "output_file_prefix";
	private static final String AUDIO_FILE_EXTENSION = ".mid";
	
	private Map<String, Integer> instruments = new HashMap<>();
	
	private String filePath;
	private String fileName;
	private boolean enabled;
	
	public MusicGenerator(ConfigDisplay configAudio) {
		this.enabled = isEnabled(Boolean.parseBoolean((String)configAudio.getValue(AUDIO_ENABLED)));
		this.fileName = (String)configAudio.getValue(AUDIO_FILE_NAME) + AUDIO_FILE_EXTENSION;
		this.filePath = SystemHelper.sanitizePathSeparators(
				(String)configAudio.getValue(AUDIO_PATH_DIR) 
				+ File.separatorChar 
				+ fileName
				);
	}

	public MusicGenerator(String outputDirectory, String applicationId) {
		this.enabled = isEnabled(false);
		this.fileName = applicationId + "-music" + AUDIO_FILE_EXTENSION;
		this.filePath = SystemHelper.sanitizePathSeparators(
				outputDirectory 
				+ File.separatorChar 
				+ fileName
				);
	}

	public MusicGenerator() {
		this.enabled = false;
	}
	
	private boolean isEnabled(boolean audioEnabled) {
		if (audioEnabled)
			return true;
		
		// check if easter egg is disabled
		String easterEggDisabled = ConfigUtil.resolveVariable(JZRA_DISABLE_EASTER_EGG, false);  //no warning if missing
		if (Boolean.parseBoolean(easterEggDisabled)) // if variable is set to "true"
			return false;
		
		// try easter egg 
		return isEasterEggTime();
	}

	public void createSong(JzrSession session){
		if(!enabled)
			return;
		
		MozartPhraseList mpl = new MozartPhraseList();
		
		//Create the data objects we want to use
		Score score = new Score("MozartDiceGame");
		score.setTempo(120.0);

		int offset = 0;
		for (ThreadDump dump : session.getDumps()){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = session.getActionHistory().get(timestamp);
			
			for(ThreadAction action : actions){
				createPart(mpl, score, action.getName(), action.size(), offset);
			}
			
			offset++;
		}
		
		Mod.transpose(score, 12);
		
		// create a MIDI file of the score
		Write.midi(score, filePath);
		session.setMidiFilePath(filePath);
		
//		Play.midi(score);
	}

	private void createPart(MozartPhraseList mpl, Score score, String name, int actionSize, int offset) {
		int channel = 0;
		int randNum;
		int table;
		
		String key = name.substring(0, name.length()> 4? 4 : name.length());
		Integer instId = instruments.get(key);
		if (instId == null){
			instId = ThreadLocalRandom.current().nextInt(0, 127);
			instruments.put(key, instId);
		}
		
		Part part = new Part("Inst"+ instId, instId, channel);
		channel++;
		if (channel == 17)
			channel = 0;
		CPhrase nextBar = new CPhrase(0);
		table = ThreadLocalRandom.current().nextInt(0, 1);
		
		int i = ThreadLocalRandom.current().nextInt(0, 7);
		for (int k=0; k<actionSize; k++){
			randNum = rollDice(2);
			
			nextBar = table == 0 ? mpl.getBarArray()[i][randNum-2].copy() : mpl.getBarArray2()[i][randNum-2].copy();
			part.addCPhrase(nextBar);
			nextBar.setStartTime((float)((k*3)+(offset))); // seems sec
			i++;
			if(i==8)
				i=0;
		}
		score.add(part);
	}

	private boolean isSameDay(GregorianCalendar calendar1, GregorianCalendar calendar2) {
        boolean sameMonth = calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH);
        boolean sameDay = calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH);
        return sameDay && sameMonth;
    }

	private boolean isEasterEggTime() {
		GregorianCalendar today = new GregorianCalendar();

		// Easter egg
		return isSameDay(today, aprilFoolsDay) 
			||isSameDay(today, christmasDay)
			||isSameDay(today, musicPartyDay);
	}
	
	public void fillReportDescriptor(ReportDescriptor desc){
		if(!enabled)
			return;
		
		desc.setMusicFileName(fileName);
		desc.setMusicFilePath(filePath);
	}	

	//method which automates the rolling of a specified number of die
	static int rollDice(int dieNum){
		int roll = 0;
		for (int i=0; i<dieNum; i++){
			roll += (ThreadLocalRandom.current().nextInt(1,6));
		}
		return roll;
	}
	
	
	public static MusicGenerator buildMusicGenerator(ConfigReport tdReportCfg, String defaultApplicationId){
		if (tdReportCfg.getConfigAudio() != null){
			// To enable it in standard, add the following configuration block 
			//  in the report configuration file within the <report> section :
			// 		<audio_midi 
			//     		enabled="${JEYZER_ANALYZER_GENERATE_PROCESS_MUSIC}" 
			//     		output_directory="${JEYZER_OUTPUT_DIR}/analysis" 
			//     		output_file_prefix="${JEYZER_TARGET_NAME}-music"
			//  	/>
			return new MusicGenerator(tdReportCfg.getConfigAudio());
		}
		else if (tdReportCfg.getConfigXSLX() != null)
		{
			// Potential configuration in case of Easter egg
			// Easter egg won't happen if JZRA_DISABLE_EASTER_EGG=true
			return new MusicGenerator(
						tdReportCfg.getConfigXSLX().getOutputDirectory(),
						defaultApplicationId
						);
		}
		else {
			// Default, disabled. Should not happen
			return new MusicGenerator();
		}
	}
}
