package org.jeyzer.analyzer.input.translator.obfuscation;

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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.config.ConfigThreadLocal;
import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.config.translator.obfuscation.ConfigDeobfuscation;
import org.jeyzer.analyzer.config.translator.obfuscation.ConfigDeobfuscatorConfiguration;
import org.jeyzer.analyzer.config.translator.obfuscation.ConfigDeobfuscatorPlugin;
import org.jeyzer.analyzer.data.ProcessCard;
import org.jeyzer.analyzer.data.jar.ProcessJars;
import org.jeyzer.analyzer.data.module.ProcessModules;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrTranslatorException;
import org.jeyzer.analyzer.input.translator.TranslateData;
import org.jeyzer.analyzer.input.translator.obfuscation.mapper.ProcessJarVersionMapper;
import org.jeyzer.analyzer.input.translator.obfuscation.mapper.ProcessModuleVersionMapper;
import org.jeyzer.analyzer.input.translator.obfuscation.mapper.PropertyMapper;
import org.jeyzer.analyzer.input.translator.obfuscation.mapper.PropertyMapperFactory;
import org.jeyzer.analyzer.input.translator.obfuscation.plugin.ProguardPluginTranslator;
import org.jeyzer.analyzer.input.translator.obfuscation.plugin.RetraceAltPluginTranslator;
import org.jeyzer.analyzer.util.SystemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeobfuscationTranslatorFactory {

	// logger
	private static final Logger logger = LoggerFactory.getLogger(DeobfuscationTranslatorFactory.class);

	private DeobfuscationTranslatorFactory(){
	}
	
	// factory method
	public static List<DeobfuscationTranslator> getThreadDumpDeobfuscators(ConfigDeobfuscation deobfuscationCfg, TranslateData initialData) throws JzrTranslatorException, JzrInitializationException {
		List<DeobfuscationTranslator> deobuscators = new ArrayList<DeobfuscationTranslator>(deobfuscationCfg.getDeobfuscatorPluginConfigurations().size());
		String dirPath = deobfuscationCfg.getOuputDirectory();
		
		File deobsDir = new File(dirPath);
		if (!deobsDir.exists() && !deobsDir.mkdirs())
			throw new JzrTranslatorException("Deobfuscation directory creation failed. Directory to create is : " + dirPath);

		ProcessCard processCard = ProcessCard.loadProcessCard(initialData.getProcessCard()); // can be null
		ProcessJars processJars = ProcessJars.loadProcessJars(initialData.getProcessJarPaths()); // can be null
		ProcessModules processModules = ProcessModules.loadProcessModules(initialData.getProcessModules()); // can be null
		Map<String, PropertyMapper> mappers = PropertyMapperFactory.getCardPropertyMappers(deobfuscationCfg, processCard);

		for (ConfigDeobfuscatorPlugin conf : deobfuscationCfg.getDeobfuscatorPluginConfigurations()){
			String type = conf.getType();
			
			try{
				List<String> configPaths = buildPluginConfigPaths(conf.getDeobfuscatorConfiguration(), mappers, processCard, processJars, processModules);
				configPaths = validateConfigPaths(configPaths, conf.isFailOnconfigNotFound(), type);
				
				short refNumber = 0;
				for (String configPath : configPaths){
					logger.info("Creating obfuscator " + type + " with configuration plugin file : " + SystemHelper.sanitizePathSeparators(configPath));
					// list here all possible deobfuscator implementations
					logger.info("Creating obfuscator " + type + " with configuration plugin file : " + SystemHelper.sanitizePathSeparators(configPath));
					// list here all possible deobfuscator implementations
					if(RetraceAltPluginTranslator.DEOBFUSCATOR_TYPE.equals(type)){
						deobuscators.add(
								new RetraceAltPluginTranslator(
										conf.getId(),
										refNumber++,
										deobsDir,
										configPath,
										conf.isAbortOnError(),
										deobfuscationCfg.areTranslatedFilesKept()
										)
								);
						continue;
					}
					else if(ProguardPluginTranslator.DEOBFUSCATOR_TYPE.equals(type)){
						deobuscators.add(
								new ProguardPluginTranslator(
										conf.getId(),
										refNumber++,
										deobsDir,
										configPath,
										conf.isAbortOnError(),
										deobfuscationCfg.areTranslatedFilesKept()
										)
								);
						continue;
					}
				}

				if (deobuscators.isEmpty()){
					logger.error("No deobfuscator found for plugin type : {}", type);
					throw new JzrTranslatorException("No deobfuscator found for plugin type : " + type);
				}
				
			}catch(JzrInitializationException ex){
				if (conf.isAbortOnError())
					throw ex;
				else
					logger.warn("Deobfuscator plugin creation (initialization) failed for : " + type + ". Abort on error is disabled : deobfuscation will not happen for this plugin.");
			}catch(JzrTranslatorException ex){
				if (conf.isAbortOnError())
					throw ex;
				else
					logger.warn("Deobfuscator plugin creation (instanciation) failed for : " + type + ". Abort on error is disabled : deobfuscation will not happen for this plugin.");
			}
			
		}

		return deobuscators;
	}

	private static List<String> validateConfigPaths(List<String> configPaths, boolean failOnconfigNotFound, String type) throws JzrInitializationException {
		List<String> resultPaths = new ArrayList<>();
		List<String> errorPaths = new ArrayList<>();
		
		for (String config : configPaths){
			if (ConfigUtil.isValidURI(config))
				validateURI(config, resultPaths, errorPaths);
			else
				validateFile(config, resultPaths, errorPaths);
		}
		
		if (!errorPaths.isEmpty()){
			for (String errorPath : errorPaths)
				logger.warn("Obfuscator " + type + " configuration plugin file not found : " + errorPath);
			if (failOnconfigNotFound)
				throw new JzrInitializationException("Obfuscator " + type + " configuration plugin file(s) not found");
		}
		
		return resultPaths;
	}

	private static List<String> buildPluginConfigPaths(ConfigDeobfuscatorConfiguration deobfuscatorConfiguration, Map<String, PropertyMapper> mapperMap, ProcessCard processCard, ProcessJars processJars, ProcessModules processModules) {
		List<String> configPaths = new ArrayList<>(1);
		List<String> templatePaths = deobfuscatorConfiguration.getConfigurationPaths();

		for (String templatePath : templatePaths){
			String templateId = templatePath; 
			
			// resolve any variable, using also the process process card
			if (processCard != null)
				ConfigThreadLocal.put(processCard.getProperties());
			templatePath = ConfigUtil.resolveValue(templatePath);
			if (processCard != null)
				ConfigThreadLocal.remove(processCard.getProperties());
			
			if (templatePath.contains(ProcessJarVersionMapper.PROCESS_JAR_TOKEN))
				templatePath = ProcessJarVersionMapper.mapVersion(templatePath, processJars);
			
			if (templatePath.contains(ProcessModuleVersionMapper.PROCESS_MODULE_TOKEN))
				templatePath = ProcessModuleVersionMapper.mapVersion(templatePath, processModules);
			
			if (!templatePath.contains(PropertyMapper.PROPERTY_TOKEN)){
				// not a template path
				configPaths.add(templatePath);
				continue;
			}
			
			List<String> propertyMapperIds = deobfuscatorConfiguration.getPropertyMappers(templateId);
			if (propertyMapperIds == null || propertyMapperIds.isEmpty()){
				logger.warn("No property mapper ids defined in the deobfuscator plugin although the configuration path is a template. Path is : " + templatePath);
				configPaths.add(templatePath);
				continue;
			}
			
			if (mapperMap.isEmpty()){
				logger.warn("No valid property mappers found although the configuration path is a template. Path is : " + templatePath);
				configPaths.add(templatePath);
				continue;
			}
			
			PropertyMapper mapper = PropertyMapperFactory.chainMappers(propertyMapperIds, mapperMap);
			if (mapper == null){
				logger.warn("No property mapper found for this list of mapper ids : " 
								+ propertyMapperIds.toString() 
								+ " for the template path : " + templatePath);
				configPaths.add(templatePath);
				continue;
			}
			
			mapper.resolveProperties(templatePath, configPaths);
		}
		
		return configPaths;
	}
	
	private static void validateFile(String config, List<String> resultPaths, List<String> errorPaths) {
		if (new File(config).exists())
			resultPaths.add(config);
        else
        	errorPaths.add(config);
	}

	private static void validateURI(String config, List<String> resultPaths, List<String> errorPaths) {
        try {
            URL url = new URL(config);
            url.openStream();
        }catch(Exception ex){
        	errorPaths.add(config);
        	return;
        }
        resultPaths.add(config);
	}
}
