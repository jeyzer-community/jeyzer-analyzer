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




import org.jeyzer.analyzer.data.ProcessCard;
import org.jeyzer.analyzer.data.ProcessCard.ProcessCardProperty;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.sticker.ConfigPropertyCardSticker;
import org.jeyzer.monitor.config.sticker.ConfigSticker;

public class PropertyCardSticker extends Sticker {

	public static final String STICKER_NAME = "property card";
	
	public PropertyCardSticker(ConfigSticker stickerCfg) {
		super(stickerCfg);
	}

	@Override
	public boolean match(JzrSession session, boolean negative) {
		if (this.cachedValue == null){
			ProcessCard card = session.getProcessCard();
			if (card != null){
				ProcessCardProperty entry = card.getValue(((ConfigPropertyCardSticker)this.stickerCfg).getProperty());			
				this.cachedValue = entry != null ?
						((ConfigPropertyCardSticker)this.stickerCfg).getPattern().matcher(entry.getValue()).find() :
						false;				
			}
			else{
				return negative ? !this.stickerCfg.isLazyAppliance() : this.stickerCfg.isLazyAppliance();
			}
		}

		return negative ? !this.cachedValue : this.cachedValue;
	}
}
