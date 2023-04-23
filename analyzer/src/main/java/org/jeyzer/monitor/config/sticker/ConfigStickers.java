package org.jeyzer.monitor.config.sticker;

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


import java.util.LinkedList;
import java.util.List;

import org.jeyzer.analyzer.config.ConfigDynamicLoading;
import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.data.location.JzrResourceLocation;
import org.jeyzer.analyzer.data.location.MultipleJzrResourceLocation;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.util.SystemHelper;
import org.jeyzer.monitor.sticker.AnalyzerSticker;
import org.jeyzer.monitor.sticker.ProcessCommandLinePropertySticker;
import org.jeyzer.monitor.sticker.ProcessJarVersionSticker;
import org.jeyzer.monitor.sticker.ProcessModuleVersionSticker;
import org.jeyzer.monitor.sticker.PropertyCardSticker;
import org.jeyzer.monitor.sticker.RuleBlockerSticker;
import org.jeyzer.monitor.util.MonitorHelper;
import org.jeyzer.service.location.JzrLocationResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConfigStickers {

	private static final Logger logger = LoggerFactory.getLogger(ConfigStickers.class);	
	
	public static final String JZRM_STICKERS = "stickers";
	private static final String JZRM_IGNORE = "ignore";
	private static final String JZRM_GROUP = "group";
	private static final String JZRM_STICKER_SET = "sticker_set";
	private static final String JZRM_STICKER_SETS = "sticker_sets";
	private static final String JZRM_FILE = "file";
	private static final String JZRM_FILES = "files";
	private static final String JZRM_LIST = "list";
	private static final String JZRM_DYNAMIC_STICKER_SETS = "dynamic_sticker_sets";
	
	private static final String DEFAULT_GROUP = "";
	
	private List<ConfigSticker> stickers = new LinkedList<>();
	private boolean ignoreStickers;
	
	private ConfigDynamicLoading dynamicStickersLoadingCfg;
	
	public ConfigStickers(Element stickersNode, JzrLocationResolver pathResolver) throws JzrInitializationException {
		this(stickersNode, pathResolver, false);
	}
	
	public ConfigStickers(Element stickersNode, JzrLocationResolver pathResolver, boolean forceStickers) throws JzrInitializationException {
		if(stickersNode == null){
			this.ignoreStickers = true;
			return; // no stickers	
		}
		
		String group = loadStickerGroupName(stickersNode);
		
		if (forceStickers){
			loadStickers(stickersNode, group, pathResolver, false);
			this.ignoreStickers = false;
		} else {
			this.ignoreStickers = Boolean.valueOf(ConfigUtil.getAttributeValue(stickersNode, JZRM_IGNORE));
			if (!this.ignoreStickers)
				loadStickers(stickersNode, group, pathResolver, false);
		}
		
		this.dynamicStickersLoadingCfg = new ConfigDynamicLoading(ConfigUtil.getFirstChildNode(stickersNode, JZRM_DYNAMIC_STICKER_SETS));
	}
	
	public void loadDynamicStickers(List<String> paths, JzrLocationResolver jzrLocationResolver) {
		for (String path : paths) {
			Element stickersRootNode;
			try {
				stickersRootNode = loadStickersConfigurationFile(path);
				String group = loadStickerGroupName(stickersRootNode);
				loadStickers(stickersRootNode, group, jzrLocationResolver, true);
			} catch (JzrInitializationException ex) {
				logger.warn("Failed to load the dynamic stickers " 
						+ (SystemHelper.isRemoteProtocol(path) ? path :  SystemHelper.sanitizePathSeparators(path)) 
						+ ". Initialization error is : " + ex.getMessage());
			}
		}
	}
	
	public List<ConfigSticker> getStickers(boolean dynamic) {
		List<ConfigSticker> filteredStickers = new ArrayList<ConfigSticker>();
		for (ConfigSticker sticker : this.stickers) {
			if (dynamic) {
				if (sticker.isDynamic())
					filteredStickers.add(sticker);
			}
			else {
				if (!sticker.isDynamic())
					filteredStickers.add(sticker);
			}
		}
		return filteredStickers;
	}
	
	public boolean areStickersIgnored(){
		return this.ignoreStickers;
	}

	public ConfigDynamicLoading getDynamicStickersLoadingCfg() {
		return dynamicStickersLoadingCfg; // can be null
	}

	private void loadStickers(Element stickersNode, String group, JzrLocationResolver pathResolver, boolean dynamic) throws JzrInitializationException {
		NodeList stickersNodes = stickersNode.getElementsByTagName(ConfigSticker.JZRM_STICKER);

		for (int i=0; i<stickersNodes.getLength(); i++){
			Element stickerNode = (Element)stickersNodes.item(i);
			ConfigSticker stickerCfg;
			
			String type = ConfigUtil.getAttributeValue(stickerNode,ConfigSticker.JZRM_TYPE);
			if (PropertyCardSticker.STICKER_NAME.equals(type)
					|| ProcessCommandLinePropertySticker.STICKER_NAME.equals(type)){
				stickerCfg = new ConfigPropertyCardSticker(stickerNode, group, dynamic);
				stickers.add(stickerCfg);
			}
			else if (ProcessJarVersionSticker.STICKER_NAME.equals(type)){
				stickerCfg = new ConfigProcessJarVersionSticker(stickerNode, group, dynamic);
				stickers.add(stickerCfg);
			}else if (ProcessModuleVersionSticker.STICKER_NAME.equals(type)){
				stickerCfg = new ConfigProcessModuleVersionSticker(stickerNode, group, dynamic);
				stickers.add(stickerCfg);
			}else{
				logger.warn("Failed to load sticker. Sticker type unknown : " + type);
			}
		}
		
		loadStickerSets(stickersNode, group, pathResolver, dynamic);
	}

	private void loadStickerSets(Element stickersNode, String parentGroup, JzrLocationResolver pathResolver, boolean dynamic) throws JzrInitializationException {
		NodeList nodeList = stickersNode.getChildNodes();
		
		for(int j=0; j<nodeList.getLength(); j++){
			Node node = nodeList.item(j);
			if (node.getNodeType() == Node.ELEMENT_NODE 
					&& JZRM_STICKER_SET.equals(((Element)node).getTagName())){
				Element stickerSetNode = (Element)node;
				if (!stickerSetNode.getAttribute(JZRM_FILE).isEmpty())
					loadStickerFileSet(stickerSetNode, parentGroup, pathResolver, dynamic);
				else if (!stickerSetNode.getAttribute(JZRM_LIST).isEmpty())
					loadStickerListSet(stickerSetNode, parentGroup, dynamic);
			}
			else if (node.getNodeType() == Node.ELEMENT_NODE 
					&& JZRM_STICKER_SETS.equals(((Element)node).getTagName())){
				Element stickerSetsNode = (Element)node;
				String location = ConfigUtil.getAttributeValue(stickerSetsNode,JZRM_FILES);
				if (pathResolver == null) {
					logger.warn("Trying to load stickers from a repository location, probably within the Jeyzer setup, which is not allowed and des not make sense. Please review the configuration. Location is : " + location);
					return;
				}
				if (location != null && !location.isEmpty()) {
					List<JzrResourceLocation> locations = new ArrayList<>(1);
					locations.add(new MultipleJzrResourceLocation(location));
					try {
						for (String path : pathResolver.resolveStickerLocations(locations)) {
							Element stickersRootNode = loadStickersConfigurationFile(path);
							String group = (DEFAULT_GROUP.equals(parentGroup) ? "" : parentGroup + ".")
									+ loadStickerGroupName(stickersRootNode);
							loadStickers(stickersRootNode, group, pathResolver, dynamic);
						}
					}
					catch(Exception ex){
						throw new JzrInitializationException("Stickers configuration loading failed. Failed to resolve the sticker locations.", ex);
					}
				}
			}
		}
	}

	private void loadStickerListSet(Element stickerSetNode, String parentGroup, boolean dynamic) throws JzrInitializationException {
		String values = ConfigUtil.getAttributeValue(stickerSetNode,JZRM_LIST);		
		if (values != null && !values.isEmpty()){
			String group = (DEFAULT_GROUP.equals(parentGroup) ? "" : parentGroup + ".") 
							+ loadStickerGroupName(stickerSetNode);
			
			List<String> stickerNames = MonitorHelper.parseStrings(values);
			for (String stickerName : stickerNames){
				if (stickerName.startsWith(RuleBlockerSticker.STICKER_PREFIX))
					stickers.add(new ConfigSticker(stickerName, RuleBlockerSticker.STICKER_NAME, group, dynamic));
				else
					stickers.add(new ConfigSticker(stickerName, AnalyzerSticker.STICKER_NAME, group, dynamic));
			}
		}
	}

	private void loadStickerFileSet(Element stickerSetNode, String parentGroup, JzrLocationResolver pathResolver, boolean dynamic) throws JzrInitializationException {
		String location = ConfigUtil.getAttributeValue(stickerSetNode,JZRM_FILE);		
		if (location != null && !location.isEmpty()){
			Element stickersRootNode = loadStickersConfigurationFile(location);
			String group = (DEFAULT_GROUP.equals(parentGroup) ? "" : parentGroup + ".")
							+ loadStickerGroupName(stickersRootNode);
			loadStickers(stickersRootNode, group, pathResolver, dynamic);
		}
	}

	private String loadStickerGroupName(Element stickersNode) {
		String group = ConfigUtil.getAttributeValue(stickersNode, JZRM_GROUP);
		if (group == null || group.isEmpty())
			group = DEFAULT_GROUP;
		return group;
	}
	
	private Element loadStickersConfigurationFile(String path) throws JzrInitializationException {
		Document doc;
		
		logger.info("Loading stickers from file : " + SystemHelper.sanitizePathSeparators(path));
		
		try {
			doc = ConfigUtil.loadXMLFile(path);
			if (doc == null){
				logger.error("Failed to open the stickers configuration resource using path : " + path);
				throw new JzrInitializationException("Failed to open the stickers configuration resource using path : " + path);
			}
		} catch (Exception e) {
			logger.error("Failed to open the stickers configuration resource using path : " + path, e);
			throw new JzrInitializationException("Failed to open the stickers configuration resource using path : " + path, e);
		}
		
		NodeList stickerNodes = doc.getElementsByTagName(JZRM_STICKERS);
		if (stickerNodes == null){
			logger.error("Stickers configuration " + path + " is invalid.");
			throw new JzrInitializationException("Stickers configuration " + path + " is invalid.");
		}
		return (Element)stickerNodes.item(0);
	}
}
