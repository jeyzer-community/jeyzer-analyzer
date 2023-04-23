package org.jeyzer.analyzer.config.setup;

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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.config.repository.ConfigRepository;
import org.jeyzer.analyzer.config.repository.ConfigRepositoryCache;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigRepositorySetup {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigRepositorySetup.class);
	
	private static final String JZRA_CACHE = "cache";
	private static final String JZRA_DIRECTORIES = "directories";
	private static final String JZRA_REPOSITORIES = "repositories";
	private static final String JZRA_REPOSITORY = "repository";

	private Map<String, ConfigRepository> repositoryConfigs = new HashMap<>();

	private ConfigRepositoryCache repositoryCacheCfg;
	
	public ConfigRepositorySetup(Element repositorySetupsNode) throws JzrInitializationException {
		Element cacheNode = ConfigUtil.getFirstChildNode(repositorySetupsNode, JZRA_CACHE);
		repositoryCacheCfg = new ConfigRepositoryCache(cacheNode);

		List<String> repositoryDirectories = loadRepositoryDirectories(repositorySetupsNode);
		scanRepositoryDirectories(repositoryDirectories);
	}

	private void scanRepositoryDirectories(List<String> repositoryDirectories) throws JzrInitializationException {
		for (String repositoryDirectory : repositoryDirectories){
			File repoDir = new File(repositoryDirectory);
			if (!repoDir.isDirectory()){
				logger.warn("Skipping the repository directory : " + repoDir.getPath() + ". Repository directory is invalid or not found.");
				continue;
			}
			
			File[] repoFiles = repoDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".xml");
				}
			});
			
			if (repoFiles == null || repoFiles.length == 0){
				logger.warn("No repository file found in : " + repoDir.getPath());
				continue;
			}
			
			for (File repoFile : repoFiles){
				loadRepositoryFile(repoFile);
			}
		}
	}

	private void loadRepositoryFile(File repoFile) throws JzrInitializationException {
		Document doc = ConfigUtil.loadDOM(repoFile);
		logger.info("Loading repository config file : " + repoFile.getPath());
		
		NodeList nodes = doc.getElementsByTagName(JZRA_REPOSITORIES);
		Element repositoriesNode = (Element)nodes.item(0);
		
		NodeList repositoryNodes = repositoriesNode.getElementsByTagName(JZRA_REPOSITORY);
		
		if (repositoryNodes.getLength() == 0){
			logger.warn("Skipping the repository file : " + repoFile.getPath() + ". File doesn't contain any repository description.");
			return;
		}
		
		for (int i=0; i<repositoryNodes.getLength(); i++){
			ConfigRepository repo = new ConfigRepository((Element)repositoryNodes.item(i));
			logger.info("Loading repository with id : " + repo.getId());
			
			if (repositoryConfigs.containsKey(repo.getId())){
				logger.error("Repository id " + repo.getId() + " loaded from file : " + repoFile.getPath() + " is already defined. Repository ids must be unique.");
				continue;
			}

			this.repositoryConfigs.put(repo.getId(), repo);
		}
	}

	private List<String> loadRepositoryDirectories(Element repositorySetupsNode) {
		List<String> repositoryDirectories = new ArrayList<>(2);
		
		String directories = ConfigUtil.getAttributeValue(repositorySetupsNode, JZRA_DIRECTORIES); // resolve any variable
		if (directories != null && !directories.isEmpty()){
			StringTokenizer st = new StringTokenizer(directories, ";");
			while (st.hasMoreTokens()){
				String directory = st.nextToken().trim();
				if (!directory.isEmpty())
					repositoryDirectories.add(directory);
			}
		}
		
		return repositoryDirectories;
	}

	public ConfigRepositoryCache getRepositoryCacheConfig() {
		return repositoryCacheCfg;
	}
	
	public Map<String, ConfigRepository> getRepositoryConfigs() {
		return repositoryConfigs;
	}
}
