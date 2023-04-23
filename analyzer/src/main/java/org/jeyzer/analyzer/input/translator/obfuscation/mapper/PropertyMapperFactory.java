package org.jeyzer.analyzer.input.translator.obfuscation.mapper;

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






import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.config.translator.obfuscation.ConfigDeobfuscation;
import org.jeyzer.analyzer.config.translator.obfuscation.ConfigPropertyCardMapper;
import org.jeyzer.analyzer.data.ProcessCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyMapperFactory {

	// logger
	private static final Logger logger = LoggerFactory.getLogger(PropertyMapperFactory.class);
	
	private PropertyMapperFactory(){
	}
	
	public static Map<String, PropertyMapper> getCardPropertyMappers(ConfigDeobfuscation deobfuscationCfg, ProcessCard processCard) {
		Map<String, PropertyMapper> mappers = new HashMap<>();
		
		if (processCard == null){
			if (!deobfuscationCfg.getPropertyMapperConfigurations().isEmpty())
				logger.warn("No process card found although property mappers are defined.");
			return mappers; // nothing to map
		}
		
		for (ConfigPropertyCardMapper propertyMapperConfiguration : deobfuscationCfg.getPropertyMapperConfigurations()){
			CardPropertyMapper propertymapper = new CardPropertyMapper(propertyMapperConfiguration, processCard);
			if (propertymapper.isValid())
				mappers.put(propertyMapperConfiguration.getName(), propertymapper);
		}
		
		return mappers;
	}
	
	public static PropertyMapper chainMappers(List<String> propertyMappers, Map<String, PropertyMapper> mapperMap) {
		// Has at least 1 element. Check performed previously
		Iterator<String> propertyMapperIter = propertyMappers.iterator();		
		String mapperId = propertyMapperIter.next();
		PropertyMapper firstMapper = mapperMap.get(mapperId); // could be null
		if (firstMapper == null){
			logger.warn("No property mapper found for the mapper id : " + mapperId);
			return null;
		}

		PropertyMapper currentMapper = firstMapper;
		while (propertyMapperIter.hasNext()){
			mapperId = propertyMapperIter.next();
			PropertyMapper nextMapper = mapperMap.get(mapperId);
			if (nextMapper == null){
				logger.warn("No property mapper found for the mapper id : " + mapperId);
				return null;				
			}
			currentMapper.setNextMapper(nextMapper);
			currentMapper = nextMapper;
		}
		
		// add the last one
		currentMapper.setNextMapper(new LastPropertyMapper());

		return firstMapper;
	}
	
}
