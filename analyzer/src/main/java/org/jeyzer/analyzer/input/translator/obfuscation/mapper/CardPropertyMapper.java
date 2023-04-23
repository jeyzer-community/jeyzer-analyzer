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






import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeyzer.analyzer.config.translator.obfuscation.ConfigPropertyCardMapper;
import org.jeyzer.analyzer.config.translator.obfuscation.ConfigPropertyCardMapper.PropertyMapping;
import org.jeyzer.analyzer.data.ProcessCard;
import org.jeyzer.analyzer.data.ProcessCard.ProcessCardProperty;

public class CardPropertyMapper implements PropertyMapper{
	
	// pattern and value to set
	private List<Map<Pattern,String>> propertyGroups = new ArrayList<>();
	private PropertyMapper nextMapper;
	
	public CardPropertyMapper(ConfigPropertyCardMapper propertyMapperConfiguration, ProcessCard processCard){
		buildProperties(propertyMapperConfiguration, processCard);
	}

	@Override
	public void setNextMapper(PropertyMapper nextMapper) {
		this.nextMapper = nextMapper;
	}
	
	@Override
	public boolean isValid(){
		return !propertyGroups.isEmpty();
	}
	
	@Override
	public void resolveProperties(String templatePath, List<String> configPaths) {
		if (!templatePath.contains(PROPERTY_TOKEN)){
			configPaths.add(templatePath);
			return;
		}

		for (Map<Pattern,String> propertyGroup : propertyGroups){
			String pathToResolve = templatePath;
			for (Pattern pattern : propertyGroup.keySet()){
				Matcher matcher = pattern.matcher(pathToResolve);
				pathToResolve = matcher.replaceAll(propertyGroup.get(pattern));
			}
			this.nextMapper.resolveProperties(pathToResolve, configPaths);
		}
	}
	
	private void buildProperties(ConfigPropertyCardMapper propertyMapperConfiguration, ProcessCard processCard) {
		Pattern sourcePattern = propertyMapperConfiguration.getPropertySourceNamePattern();
		
		List<ProcessCardProperty> processCardProps = processCard.getValues(sourcePattern);
		for(ProcessCardProperty property : processCardProps){
			// Find the interesting part of the card properties and store it in map
			//   assuming we have all the mapped properties
			// Examples :
			//  <property name="module" pattern="build.version-(.*)" scope="property-name"/>
			//  <property name="version" pattern="(.*)" scope="property-value"/>

			List<PropertyMapping> mappings = propertyMapperConfiguration.getPropertyMappings();
			List<String> candidateValues = new ArrayList<>(mappings.size());
			for (PropertyMapping mapping : mappings){
				Pattern pattern = mapping.getPattern();
				Matcher matcher = mapping.isValueScope() ? pattern.matcher(property.getValue()) : pattern.matcher(property.getName());
				if (matcher.matches() && matcher.groupCount() == 1){
					candidateValues.add(matcher.group(0));
				}
			}
			
			if (mappings.size() == candidateValues.size()){
				// all mappings were hit : keep this set
				int i = 0;
				Map<Pattern, String> propertyGroup = new HashMap<>(mappings.size());
				for (PropertyMapping mapping : mappings){
					propertyGroup.put(
							Pattern.compile(PROPERTY_TOKEN + mapping.getName() + PROPERTY_TOKEN),
							candidateValues.get(i++));
				}
				propertyGroups.add(propertyGroup);
			}
		}
	}
	
}
