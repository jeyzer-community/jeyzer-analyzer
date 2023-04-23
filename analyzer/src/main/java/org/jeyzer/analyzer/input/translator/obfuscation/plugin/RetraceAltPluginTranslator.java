package org.jeyzer.analyzer.input.translator.obfuscation.plugin;

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







import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.jeyzer.analyzer.error.JzrDeobfuscationPluginException;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.input.translator.obfuscation.DeobfuscationTranslator;

import com.github.artyomcool.retrace.Retrace;

public class RetraceAltPluginTranslator extends DeobfuscationTranslator {
	
	// added (?:.*?\\-\\s+%c\\.%m.*(?:line=(%l)).*) to handle the hung stack format
//	public static final String STACK_TRACE_EXPRESSION = "(?:.*?\\bat\\s+%c\\.%m\\s*\\(%s(?::%l)?\\)\\s*)|(?:(?:.*?[:\"]\\s+)?%c(?::.*)?)|(?:.*?\\-\\s+%c\\.%m.*(?:line=(%l)).*)";	
	
	public static final String DEOBFUSCATOR_TYPE = "retrace-alt";
	public static final String OBFUSCATOR_NAME = "retrace-alt";
	public static final String OBFUSCATOR_CLASS = "com.github.artyomcool.retrace.Retrace";
	
	private Retrace retrace;
	
	public RetraceAltPluginTranslator(String id, short refNumber, File deobsDir, String config, boolean abortOnError, boolean filesKept) throws JzrInitializationException {
		super(id, refNumber, deobsDir, config, abortOnError, filesKept);
		// load it once as it is stateless
		try {
			retrace = createRetrace();
		} catch (IOException ex) {
			throw new JzrInitializationException("Failed to read mapping file : " + this.config, ex);
		}
	}
	
	@Override
	public String getType(){
		return DEOBFUSCATOR_TYPE;
	}
	
	@Override
	public String getName(){
		return OBFUSCATOR_NAME;
	}
	
	@Override
	public String getClassName(){
		return OBFUSCATOR_CLASS;
	}
	
	@Override
	protected void deobfuscateFile(File stackTraceFile, File deobfuscatedFile) throws JzrDeobfuscationPluginException {
		try (
				// input
				FileInputStream fis = new FileInputStream(stackTraceFile);
				InputStreamReader isr = new InputStreamReader(fis);
				BufferedReader br = new BufferedReader(isr);
			)
		{
			String result = retrace.stackTrace(br);
			writeResult(result, deobfuscatedFile);
		} catch (FileNotFoundException e) {
			throw new JzrDeobfuscationPluginException("Failed to read stack trace file : " + stackTraceFile.getAbsolutePath(), e);
		} catch (IOException e) {
			throw new JzrDeobfuscationPluginException("Failed to deobfuscate the stack trace file : " + stackTraceFile.getAbsolutePath(), e);
		}
	}

	private void writeResult(String result, File deobfuscatedFile) throws JzrDeobfuscationPluginException, IOException {
		try (
				// output
				FileOutputStream fos = new FileOutputStream(deobfuscatedFile);
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				PrintWriter writer = new PrintWriter(osw);
			)
		{
			writer.print(result);
		} catch (FileNotFoundException e) {
			throw new JzrDeobfuscationPluginException("Failed to open the deobfuscated target file : " + deobfuscatedFile.getAbsolutePath(), e);
		}
	}

	@Override
	protected void checkConfigurationFile() throws JzrInitializationException {
		// Nothing to do here
	}
	
	private Retrace createRetrace() throws IOException {
		try (
				FileInputStream fis = new FileInputStream(this.config);
				InputStreamReader isr = new InputStreamReader(fis);
				BufferedReader br = new BufferedReader(isr);
			)
		{
			return new Retrace(br);
		}
	}
}
