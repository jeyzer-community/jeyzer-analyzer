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
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;

import org.jeyzer.analyzer.error.JzrDeobfuscationPluginException;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.input.translator.obfuscation.DeobfuscationTranslator;

import proguard.obfuscate.MappingReader;
import proguard.retrace.FrameInfo;
import proguard.retrace.FramePattern;
import proguard.retrace.FrameRemapper;

public class ProguardPluginTranslator extends DeobfuscationTranslator {
	
	// added (?:.*?\\-\\s+%c\\.%m.*(?:line=(%l)).*) to handle the hung stack format
	public static final String STACK_TRACE_EXPRESSION = "(?:.*?\\bat\\s+%c\\.%m\\s*\\(%s(?::%l)?\\)\\s*)|(?:(?:.*?[:\"]\\s+)?%c(?::.*)?)|(?:.*?\\-\\s+%c\\.%m.*(?:line=(%l)).*)";	
	
	public static final String DEOBFUSCATOR_TYPE = "proguard-retrace";
	public static final String OBFUSCATOR_NAME = "proguard";
	public static final String OBFUSCATOR_CLASS = "proguard.retrace.FrameInfo";
	
	private File mappingFile;
	
	public ProguardPluginTranslator(String id, short refNumber, File deobsDir, String config, boolean abortOnError, boolean filesKept) throws JzrInitializationException {
		super(id, refNumber, deobsDir, config, abortOnError, filesKept);
		mappingFile = new File(this.config);
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
		// Open the input stack trace. We're always using the UTF-8
		// character encoding
		
		try (
				// input
				FileInputStream fis = new FileInputStream(stackTraceFile);
				InputStreamReader isr = new InputStreamReader(fis);
				BufferedReader br = new BufferedReader(isr);
				LineNumberReader reader = new LineNumberReader(br);
			)
		{
			proguardDeobuscate(stackTraceFile, deobfuscatedFile, reader);
		} catch (FileNotFoundException e) {
			throw new JzrDeobfuscationPluginException("Failed to read stack trace file : " + stackTraceFile.getAbsolutePath(), e);
		} catch (IOException e) {
			throw new JzrDeobfuscationPluginException("Failed to deobfuscate the stack trace file : " + stackTraceFile.getAbsolutePath(), e);
		}
	}

	@Override
	protected void checkConfigurationFile() throws JzrInitializationException {
		// Do nothing but could check the validity of the Proguard file
	}

	private void proguardDeobuscate(File stackTraceFile, File deobfuscatedFile, LineNumberReader reader) throws JzrDeobfuscationPluginException, IOException {
		try (
				// output
				FileOutputStream fos = new FileOutputStream(deobfuscatedFile);
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				PrintWriter writer = new PrintWriter(osw);					
			)
		{
			retrace(reader, writer);
		} catch (FileNotFoundException e) {
			throw new JzrDeobfuscationPluginException("Failed to open the deobfuscated target file : " + stackTraceFile.getAbsolutePath(), e);
		}
	}
	
	/**
     * De-obfuscates a given stack trace.
     * @param stackTraceReader a reader for the obfuscated stack trace.
     * @param stackTraceWriter a writer for the de-obfuscated stack trace.
     */
    private void retrace(LineNumberReader stackTraceReader,
                        PrintWriter      stackTraceWriter) throws IOException
    {
        // Create a pattern for stack frames.
        FramePattern pattern = new FramePattern(STACK_TRACE_EXPRESSION, false);

        // Create a remapper.
        FrameRemapper mapper = new FrameRemapper();

        // Read the mapping file.
        MappingReader mappingReader = new MappingReader(mappingFile);
        mappingReader.pump(mapper);

        // Read and process the lines of the stack trace.
        while (true)
        {
            // Read a line.
            String obfuscatedLine = stackTraceReader.readLine();
            if (obfuscatedLine == null)
            {
                break;
            }

            // Try to match it against the regular expression.
            FrameInfo obfuscatedFrame = pattern.parse(obfuscatedLine);
            if (obfuscatedFrame != null)
            {
                // Transform the obfuscated frame back to one or more
                // original frames.
                Iterator retracedFrames =
                    mapper.transform(obfuscatedFrame).iterator();

                String previousLine = null;

                while (retracedFrames.hasNext())
                {
                    // Retrieve the next retraced frame.
                    FrameInfo retracedFrame =
                        (FrameInfo)retracedFrames.next();

                    // Format the retraced line.
                    String retracedLine =
                        pattern.format(obfuscatedLine, retracedFrame);

                    // Clear the common first part of ambiguous alternative
                    // retraced lines, to present a cleaner list of
                    // alternatives.
                    String trimmedLine =
                        previousLine != null &&
                        obfuscatedFrame.getLineNumber() == 0 ?
                            trim(retracedLine, previousLine) :
                            retracedLine;

                    // Print out the retraced line.
                    if (trimmedLine != null)
                    {
                        stackTraceWriter.println(trimmedLine);
                    }

                    previousLine = retracedLine;
                }
            }
            else
            {
                // Print out the original line.
                stackTraceWriter.println(obfuscatedLine);
            }
        }

        stackTraceWriter.flush();
    }

    /**
     * Returns the first given string, with any leading characters that it has
     * in common with the second string replaced by spaces.
     */
    private String trim(String string1, String string2)
    {
        StringBuffer line = new StringBuffer(string1);

        // Find the common part.
        int trimEnd = firstNonCommonIndex(string1, string2);
        if (trimEnd == string1.length())
        {
            return null;
        }

        // Don't clear the last identifier characters.
        trimEnd = lastNonIdentifierIndex(string1, trimEnd) + 1;

        // Clear the common characters.
        for (int index = 0; index < trimEnd; index++)
        {
            if (!Character.isWhitespace(string1.charAt(index)))
            {
                line.setCharAt(index, ' ');
            }
        }

        return line.toString();
    }


    /**
     * Returns the index of the first character that is not the same in both
     * given strings.
     */
    private int firstNonCommonIndex(String string1, String string2)
    {
        int index = 0;
        while (index < string1.length() &&
               index < string2.length() &&
               string1.charAt(index) == string2.charAt(index))
        {
            index++;
        }

        return index;
    }


    /**
     * Returns the index of the last character that is not an identifier
     * character in the given string, at or before the given index.
     */
    private int lastNonIdentifierIndex(String line, int index)
    {
        while (index >= 0 &&
               Character.isJavaIdentifierPart(line.charAt(index)))
        {
            index--;
        }

        return index;
    }
}
