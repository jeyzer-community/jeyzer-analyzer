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

import org.jeyzer.analyzer.data.module.ProcessModule;
import org.jeyzer.analyzer.data.module.ProcessModules;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.sticker.ConfigProcessModuleVersionSticker;
import org.jeyzer.monitor.config.sticker.ConfigSticker;

public class ProcessModuleVersionSticker extends Sticker {

	public static final String STICKER_NAME = "process module version";
	
	public ProcessModuleVersionSticker(ConfigSticker stickerCfg) {
		super(stickerCfg);
	}

	@Override
	public boolean match(JzrSession session, boolean negative) {
		if (this.cachedValue == null){
			ProcessModules processModules = session.getProcessModules();

			if (processModules != null){
				ProcessModule module = processModules.getProcessModule((((ConfigProcessModuleVersionSticker)this.stickerCfg).getModuleName()));			
				this.cachedValue = (module != null && module.getVersion() != null) ?
						((ConfigProcessModuleVersionSticker)this.stickerCfg).getPattern().matcher(module.getVersion()).find() :
						false;
			}
			else{
				return negative ? !this.stickerCfg.isLazyAppliance() : this.stickerCfg.isLazyAppliance();
			}
		}

		return negative ? !this.cachedValue : this.cachedValue;
	}
}
