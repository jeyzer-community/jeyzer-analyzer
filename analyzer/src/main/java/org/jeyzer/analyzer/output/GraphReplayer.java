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




import static org.jeyzer.analyzer.math.FormulaHelper.*;

import org.jeyzer.analyzer.config.analysis.ConfigReplay;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.output.graph.motion.FunctionGraphPlayer;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.analyzer.util.AnalyzerHelper;
import org.jeyzer.monitor.util.MonitorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphReplayer {
	
	private static final Logger logger = LoggerFactory.getLogger(GraphReplayer.class);
	
	private JzrSession session;	
	private ConfigReplay replayCfg;
	private String applicationId;
	
	public GraphReplayer(ConfigReplay replayCfg, JzrSession session, String applicationId){
		this.session = session;
		this.replayCfg = replayCfg;
		this.applicationId = applicationId;
	}
	
	public void replay() throws JzrInitializationException{
		logger.info("Replaying session");
		
		FunctionGraphPlayer functionPlayer = new FunctionGraphPlayer(
				replayCfg.getFunctionGraphPlayerCfg(), 
				applicationId, 
				session.getThreadDumpPeriod());
		
		if (session.getDumps().isEmpty()){
			functionPlayer.displayNoThreadDump();
			return;
		}
		
		ThreadDump prev = session.getDumps().get(0);
		for (ThreadDump dump : session.getDumps()){
			if (dump.isRestart()){
				displayRestart(functionPlayer, dump);
			}
			else if (dump.hasHiatusBefore()){
				displayHiatus(functionPlayer, dump, prev);
			}

			functionPlayer.play(dump);
		
			prev = dump;
			try {
				Thread.sleep(this.replayCfg.getRefreshPeriod().toMillis());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
		functionPlayer.displayMessage("The END.");
	}

	private void displayHiatus(FunctionGraphPlayer player, ThreadDump dump, ThreadDump prev) {
		int missingCount = AnalyzerHelper.getMissingFilesCount(prev.getTimestamp(), dump.getTimestamp(), session.getThreadDumpPeriod());
		long hiatusTime = convertToMinutes(dump.getTimeSlice()); // from nano sec to mn
		if (hiatusTime >0)
			player.displayNoThreadDump(
					missingCount 
					+ " missing thread dump"
					+ (missingCount == 1 ? " (" : "s (")
					+ hiatusTime 
					+ " mn hiatus)");
		else{
			hiatusTime = convertToSeconds(dump.getTimeSlice()); // from nano sec to sec
			player.displayNoThreadDump(
					missingCount 
					+ " missing thread dump"
					+ (missingCount == 1 ? " (" : "s (")
					+ hiatusTime 
					+ " sec hiatus)");
		}
		try {
			Thread.sleep(Math.round(this.replayCfg.getRefreshPeriod().toMillis() * 1.5));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
	}

	private void displayRestart(FunctionGraphPlayer player, ThreadDump dump) {
		long hiatusTime = convertToMinutes(dump.getTimeSlice()); // from nano sec to mn
		String restartDate = MonitorHelper.formatDate(dump.getTimestamp());
		if (hiatusTime >0)
			player.displayRestart("Application restart at " + restartDate + " ("+ hiatusTime + " mn hiatus)");
		else{
			hiatusTime = convertToSeconds(dump.getTimeSlice()); // from nano sec to sec
			player.displayRestart("Application restart at " + restartDate + " (" + hiatusTime + " sec hiatus)");
		}
		try {
			Thread.sleep(this.replayCfg.getRefreshPeriod().toMillis() * 2);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
	}
}
