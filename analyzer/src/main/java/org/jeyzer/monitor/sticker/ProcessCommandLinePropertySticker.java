package org.jeyzer.monitor.sticker;

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




import org.jeyzer.analyzer.data.ProcessCommandLine;
import org.jeyzer.analyzer.data.ProcessCommandLine.CommandLineProperty;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.sticker.ConfigPropertyCardSticker;
import org.jeyzer.monitor.config.sticker.ConfigSticker;

public class ProcessCommandLinePropertySticker extends Sticker {

	public static final String STICKER_NAME = "process command line property";
	
	public ProcessCommandLinePropertySticker(ConfigSticker stickerCfg) {
		super(stickerCfg);
	}

	@Override
	public boolean match(JzrSession session, boolean negative) {
		if (this.cachedValue == null){
			if (session.getProcessCard() == null)
				return negative ? !this.stickerCfg.isLazyAppliance() : this.stickerCfg.isLazyAppliance();
			
			ProcessCommandLine commandLine = session.getProcessCommandLine(); // not null
			CommandLineProperty property = commandLine.getValue(((ConfigPropertyCardSticker)this.stickerCfg).getProperty());
			this.cachedValue = property != null ?
					((ConfigPropertyCardSticker)this.stickerCfg).getPattern().matcher(property.getValue()).find() :
					false;
		}

		return negative ? !this.cachedValue : this.cachedValue;
	}
}
