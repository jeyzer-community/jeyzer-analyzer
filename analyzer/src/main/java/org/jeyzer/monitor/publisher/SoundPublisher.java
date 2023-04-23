package org.jeyzer.monitor.publisher;

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







import java.util.List;
import java.util.Map;

import javax.sound.sampled.LineUnavailableException;

import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.monitor.config.publisher.ConfigSoundPublisher;
import org.jeyzer.monitor.engine.event.MonitorEvent;
import org.jeyzer.monitor.engine.event.info.Level;
import org.jeyzer.monitor.util.SoundUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoundPublisher implements Publisher {

	public static final Logger logger = LoggerFactory.getLogger(SoundPublisher.class);
	
	private ConfigSoundPublisher publisherCfg;
	
	public SoundPublisher(ConfigSoundPublisher publisherCfg) {
		this.publisherCfg = publisherCfg;
	}

	@Override
	public void publish(JzrMonitorSession session, List<MonitorEvent> events, Map<String, List<String>> publisherPaths) {
		if (!this.publisherCfg.isEnabled())
			return;
		
		logger.info("Generating sound in case of warnings (short beep) or critical events (long beep).");
		emitBeeps(events);
	}

	private void emitBeeps(List<MonitorEvent> events){
		
		try {
			for (MonitorEvent event : events) {
				if (Level.WARNING.equals(event.getLevel())) {
					// Warning
					SoundUtils.tone(400, 500);
				}
				else if (Level.ERROR.equals(event.getLevel())) {
					emitErrorBeep();
				}
				else if (Level.CRITICAL.equals(event.getLevel())) {
					emitCriticalBeep();
				}
			}
		} catch (LineUnavailableException e) {
			logger.error("Failed to generate beep.", e);
		}
	}


	private void emitCriticalBeep() throws LineUnavailableException {
		int increase = 3000;
		for (int i = 0; i < 5; i++){
			SoundUtils.tone(increase, 200);
			SoundUtils.tone(400,500);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			increase += 500;
		}
	}

	private void emitErrorBeep() throws LineUnavailableException {
		// Error
		for (int i = 0; i < 20; i++){
			SoundUtils.tone(800, 800);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}	
}
