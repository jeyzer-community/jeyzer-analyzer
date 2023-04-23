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




import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.sticker.ConfigSticker;
import org.jeyzer.monitor.config.sticker.ConfigStickers;
import org.jeyzer.service.location.JzrLocationResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StickerBuilder {
	
	public static final Logger logger = LoggerFactory.getLogger(StickerBuilder.class);	

	private Map<String, Sticker> stickers = new HashMap<>();
	private List<Sticker> stickerList = new ArrayList<>();
	
	private boolean ignoreStickers;
	private boolean dynamicLoaded;
	
	private ConfigStickers stickersCfg;
	
	public StickerBuilder(ConfigStickers stickersCfg){
		ignoreStickers = stickersCfg.areStickersIgnored();
		if (!ignoreStickers){
			buildStickers(stickersCfg, false);
			buildStickerList(stickersCfg, false);
		}
		this.stickersCfg = stickersCfg;
	}
	
	public void addStickerList(List<Sticker> extraStickers){
		if (ignoreStickers)
			return;
		this.stickerList.addAll(extraStickers);
	}

	public void addStickers(Map<String, Sticker> extraStickers){
		if (ignoreStickers)
			return;

		List<Sticker> candidates = new ArrayList<>();
		for (Sticker extraSticker : extraStickers.values()){
			if (detectDuplicate(extraSticker.getConfigSticker()))
				continue;
			candidates.add(extraSticker);
		}
		
		for (Sticker extraSticker : candidates){
			stickers.put(extraSticker.getConfigSticker().getName(), extraSticker);
			// put long name as well
			if (!extraSticker.getConfigSticker().getFullName().equals(extraSticker.getConfigSticker().getName()))
				stickers.put(extraSticker.getConfigSticker().getFullName(), extraSticker);
		}
	}
	
	public void loadDynamicStickers(JzrSession session, JzrLocationResolver resolver) {
		if (ignoreStickers 
				|| dynamicLoaded 
				|| (this.stickersCfg.getDynamicStickersLoadingCfg() != null && !this.stickersCfg.getDynamicStickersLoadingCfg().isDynamicLoadingActive()))
			return;
		
		logger.info("Loading the dynamic monitoring stickers");
		List<String> paths = resolver.resolveDynamicStickersLocations(
				session.getProcessJars(),
				session.getProcessModules(),
				this.stickersCfg.getDynamicStickersLoadingCfg().isDeclaredRepositoryOnly()
				);
		this.stickersCfg.loadDynamicStickers(paths, resolver);
		
		buildStickers(this.stickersCfg, true);
		buildStickerList(this.stickersCfg, true);
		dynamicLoaded = true;
	}
	
	public Map<String, Sticker> getStickers() {
		return stickers;
	}

	public List<Sticker> getStickerList() {
		return stickerList;
	}

	private void buildStickerList(ConfigStickers stickersCfg, boolean dynamic) {
		for (ConfigSticker stickerCfg : stickersCfg.getStickers(dynamic)){
			Sticker sticker = buildSticker(stickerCfg);
			if (sticker != null)
				stickerList.add(sticker);
		}
	}

	private void buildStickers(ConfigStickers stickersCfg, boolean dynamic) {
		for (ConfigSticker stickerCfg : stickersCfg.getStickers(dynamic)){
			if (detectDuplicate(stickerCfg))
				continue;
			
			Sticker sticker = buildSticker(stickerCfg);
			if (sticker != null){
				stickers.put(stickerCfg.getName(), sticker);
				// put long name as well
				if (!stickerCfg.getFullName().equals(stickerCfg.getName()))
					stickers.put(stickerCfg.getFullName(), sticker);				
			}
		}
	}
	
	private Sticker buildSticker(ConfigSticker stickerCfg) {
		String type = stickerCfg.getType();
		
		if (AnalyzerSticker.STICKER_NAME.equals(type))
			return new AnalyzerSticker(stickerCfg);
		else if (RuleBlockerSticker.STICKER_NAME.equals(type))
			return new RuleBlockerSticker(stickerCfg);
		else if (PropertyCardSticker.STICKER_NAME.equals(type))
			return new PropertyCardSticker(stickerCfg);
		else if (ProcessCommandLinePropertySticker.STICKER_NAME.equals(type))
			return new ProcessCommandLinePropertySticker(stickerCfg);
		else if (ProcessJarVersionSticker.STICKER_NAME.equals(type))
			return new ProcessJarVersionSticker(stickerCfg);
		else if (ProcessModuleVersionSticker.STICKER_NAME.equals(type))
			return new ProcessModuleVersionSticker(stickerCfg);
		
		return null;
	}
	
	private boolean detectDuplicate(ConfigSticker stickerCfg) {
		Sticker original = this.stickers.get(stickerCfg.getName());
		if (original != null){
			logger.warn("Sticker already exist with the same name : " + stickerCfg.getName() + " in group : " + original.getConfigSticker().getGroup());
			logger.warn("Current sticker belongs to group : " + stickerCfg.getGroup() + ". Current sticker will be ignored.");
			return true;
		}
		
		original = this.stickers.get(stickerCfg.getFullName());
		if (original != null){
			logger.warn("Sticker already exist with the same full name : " + stickerCfg.getFullName());
			return true;
		}
		
		return false;
	}
}
