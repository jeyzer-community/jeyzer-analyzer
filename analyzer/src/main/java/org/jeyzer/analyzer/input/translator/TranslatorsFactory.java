package org.jeyzer.analyzer.input.translator;

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
import java.util.List;

import org.jeyzer.analyzer.config.translator.ConfigTranslator;
import org.jeyzer.analyzer.config.translator.compression.ConfigDecompression;
import org.jeyzer.analyzer.config.translator.jfr.ConfigJFRDecompression;
import org.jeyzer.analyzer.config.translator.obfuscation.ConfigDeobfuscation;
import org.jeyzer.analyzer.config.translator.security.ConfigDecryption;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrTranslatorException;
import org.jeyzer.analyzer.input.translator.compression.CompressionTranslator;
import org.jeyzer.analyzer.input.translator.jfr.JFRTranslator;
import org.jeyzer.analyzer.input.translator.obfuscation.DeobfuscationTranslator;
import org.jeyzer.analyzer.input.translator.obfuscation.DeobfuscationTranslatorFactory;
import org.jeyzer.analyzer.input.translator.security.SecurityTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TranslatorsFactory {

	// logger
	private static final Logger logger = LoggerFactory.getLogger(TranslatorsFactory.class);	
	
	private TranslatorsFactory() {
	}
		
	public static List<Translator> createTranslators(ConfigTranslator cfg, TranslateData inputData) throws JzrTranslatorException, JzrInitializationException{
		List<Translator> translators = new ArrayList<>();
			
		try {
			if (CompressionTranslator.NAME.equals(cfg.getType())){
				Translator translator = new CompressionTranslator((ConfigDecompression)cfg);
				translators.add(translator);
			}
			else if (SecurityTranslator.NAME.equals(cfg.getType())){
				Translator translator = new SecurityTranslator((ConfigDecryption)cfg, inputData);
				translators.add(translator);
			}
			else if(DeobfuscationTranslator.NAME.equals(cfg.getType())){
				// Several translators can be chained here
				List<DeobfuscationTranslator> deobsfuscators = DeobfuscationTranslatorFactory.getThreadDumpDeobfuscators((ConfigDeobfuscation)cfg, inputData);
				for (DeobfuscationTranslator translator : deobsfuscators){
					translators.add(translator);
				}
			}
			else if (JFRTranslator.NAME.equals(cfg.getType())){
				Translator translator = new JFRTranslator((ConfigJFRDecompression)cfg);
				translators.add(translator);
			}
			else{
				throw new JzrInitializationException("No translator implementation found for the translator type : " + cfg.getType());
			}
			} catch (JzrInitializationException ex) {
				if (cfg.isAbortOnError())
					throw ex;
				else 
					logger.error("Failed to create (initialization) the translator of type : " + cfg.getType() + ". Translator is skipped.", ex);
			} catch (JzrTranslatorException ex) {
				if (cfg.isAbortOnError())
					throw ex;
				else 
					logger.error("Failed to create (instanciation) the translator of type : " + cfg.getType() + ". Translator is skipped.", ex);
			}
		
		return translators;
	}
}
