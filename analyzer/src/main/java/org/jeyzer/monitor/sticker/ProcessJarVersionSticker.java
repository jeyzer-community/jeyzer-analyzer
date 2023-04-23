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

import org.jeyzer.analyzer.data.jar.ProcessJarVersion;
import org.jeyzer.analyzer.data.jar.ProcessJars;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.sticker.ConfigProcessJarVersionSticker;
import org.jeyzer.monitor.config.sticker.ConfigSticker;

public class ProcessJarVersionSticker extends Sticker {

	public static final String STICKER_NAME = "process jar version";
	
	public ProcessJarVersionSticker(ConfigSticker stickerCfg) {
		super(stickerCfg);
	}

	@Override
	public boolean match(JzrSession session, boolean negative) {
		if (this.cachedValue == null){
			ProcessJars processJars = session.getProcessJars();

			if (processJars != null){
				ProcessJarVersion processJar = processJars.getProcessJarVersion((((ConfigProcessJarVersionSticker)this.stickerCfg).getJarName()));			
				this.cachedValue = (processJar != null && processJar.getJarVersion() != null) ?
						((ConfigProcessJarVersionSticker)this.stickerCfg).getPattern().matcher(processJar.getJarVersion()).find() :
						false;
			}
			else{
				return negative ? !this.stickerCfg.isLazyAppliance() : this.stickerCfg.isLazyAppliance();
			}
		}

		return negative ? !this.cachedValue : this.cachedValue;
	}
}
