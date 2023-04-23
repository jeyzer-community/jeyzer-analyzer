package org.jeyzer.analyzer.config.translator.compression;

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




import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.config.translator.ConfigTranslator;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.util.ZipHelper;
import org.jeyzer.analyzer.util.ZipParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class ConfigDecompression extends ConfigTranslator implements ZipParams {

	private static final Logger logger = LoggerFactory.getLogger(ConfigDecompression.class);

	public static final String TYPE_NAME = "compression";
	
	private static final String JZRA_COMPRESSION = "uncompress";
	private static final String JZRA_KEEP_FILES = "keep_files";
	private static final String JZRA_DIRECTORY = "directory";
	
	// limits
	private static final String JZRA_LIMITS = "limits";
	private static final String JZRA_FILE_SIZE_LIMIT = "file_size_limit";
	private static final String JZRA_UNCOMPRESSED_SIZE_LIMIT = "uncompressed_size_limit";
	private static final String JZRA_UNCOMPRESSED_FILES_LIMIT = "uncompressed_files_limit";

	private static final int DEFAULT_FILE_SIZE_LIMIT = 100; // Mb
	private static final int DEFAULT_UNCOMPRESSED_SIZE_LIMIT = 1500; // Mb
	private static final int DEFAULT_UNCOMPRESSED_FILES_LIMIT = 14400;  // 5 days of recording with 30 sec period
	
	private int fileSizeLimit = DEFAULT_FILE_SIZE_LIMIT;
	private int uncompressedSizeLimit = DEFAULT_UNCOMPRESSED_SIZE_LIMIT;
	private long uncompressedFilesLimit = DEFAULT_UNCOMPRESSED_FILES_LIMIT;
	
	public ConfigDecompression(Element translatorNode, String configFilePath, String threadDumpDirectory) throws JzrInitializationException {
		super(TYPE_NAME, configFilePath, true); // always abort on error
		if (translatorNode == null)
			return;
		
		supportedInputFileExtensions.add(ZipHelper.ZIP_EXTENSION);
		supportedInputFileExtensions.add(ZipHelper.GZIP_EXTENSION);
		
		loadCompression(translatorNode, threadDumpDirectory);
	}
	
	public long getFileSizeLimitInBytes() {
		return (long)fileSizeLimit * FormulaHelper.BYTES_IN_1_MB;
	}
	
	public int getFileSizeLimit() {
		return fileSizeLimit;
	}

	public long getUncompressedSizeLimitInBytes() {
		return (long)uncompressedSizeLimit * FormulaHelper.BYTES_IN_1_MB;
	}
	
	public int getUncompressedSizeLimit() {
		return uncompressedSizeLimit;
	}

	public long getUncompressedFilesLimit() {
		return uncompressedFilesLimit;
	}

	private void loadCompression(Element translatorNode, String threadDumpDirectory) throws JzrInitializationException {

		Element compressionNode = ConfigUtil.getFirstChildNode(translatorNode, JZRA_COMPRESSION);
		if (compressionNode == null)
			throw new JzrInitializationException("Invalid compression file " + this.configFilePath + ". Compression node not found.");
						
		loadUncompressDirectoryPath(compressionNode, threadDumpDirectory);
		loadKeepFiles(compressionNode);
		loadLimits(compressionNode);
		
		this.enabled = true;
	}
	
	private void loadLimits(Element compressionNode) {
		Element limitsNode = ConfigUtil.getFirstChildNode(compressionNode, JZRA_LIMITS);
		if (limitsNode == null) {
			logger.info("Uncompress limits not set. Defaulting on default limit values.");
			return;
		}
		
		String value = ConfigUtil.getAttributeValue(limitsNode, JZRA_FILE_SIZE_LIMIT);
		if (value == null || value.isEmpty() || value.startsWith("${")){
			logger.info("Uncompress file size limit not set. Defaulting on value : " + DEFAULT_FILE_SIZE_LIMIT + " Mb");
		}
		else {
			try{
				this.fileSizeLimit = Integer.parseInt(value);
			}catch(java.lang.NumberFormatException e){
				logger.warn("Uncompress file size limit " + value + " is invalid. Defaulting on value : " + DEFAULT_FILE_SIZE_LIMIT + " Mb");
			}
		}
		if (this.fileSizeLimit <= 0)
			logger.warn("Uncompress file size limit " + value + " is negative. Defaulting on value : " + DEFAULT_FILE_SIZE_LIMIT + " Mb");
		
		value = ConfigUtil.getAttributeValue(limitsNode, JZRA_UNCOMPRESSED_SIZE_LIMIT);
		if (value == null || value.isEmpty() || value.startsWith("${")){
			logger.info("Uncompress size limit not set. Defaulting on value : " + DEFAULT_UNCOMPRESSED_SIZE_LIMIT + " Mb");
		}
		else {
			try{
				this.uncompressedSizeLimit = Integer.parseInt(value);
			}catch(java.lang.NumberFormatException e){
				logger.warn("Uncompress size limit " + value + " is invalid. Defaulting on value : " + DEFAULT_UNCOMPRESSED_SIZE_LIMIT + " Mb");
			}
		}
		if (this.uncompressedSizeLimit <= 0)
			logger.warn("Uncompress size limit " + value + " is negative. Defaulting on value : " + DEFAULT_UNCOMPRESSED_SIZE_LIMIT + " Mb");
		
		value = ConfigUtil.getAttributeValue(limitsNode, JZRA_UNCOMPRESSED_FILES_LIMIT);
		if (value == null || value.isEmpty() || value.startsWith("${")){
			logger.info("Uncompress files limit not set. Defaulting on value : " + DEFAULT_UNCOMPRESSED_FILES_LIMIT + " files");
		}
		else {
			try{
				this.uncompressedFilesLimit = Integer.parseInt(value);
			}catch(java.lang.NumberFormatException e){
				logger.warn("Uncompress files limit " + value + " is invalid. Defaulting on value : " + DEFAULT_UNCOMPRESSED_FILES_LIMIT + " files");
			}
		}
		if (this.uncompressedFilesLimit <= 0)
			logger.warn("Uncompress files limit " + value + " is negative. Defaulting on value : " + DEFAULT_UNCOMPRESSED_FILES_LIMIT + " files");
	}

	private void loadUncompressDirectoryPath(Element compressionNode, String threadDumpDirectory) throws JzrInitializationException{
		String dirPath = ConfigUtil.getAttributeValue(compressionNode, JZRA_DIRECTORY);

		if (dirPath == null || dirPath.isEmpty())
			throw new JzrInitializationException("Invalid compression file " + configFilePath + ". Compression " + JZRA_DIRECTORY + " attribute not found.");
		
		dirPath = dirPath.replace('\\', '/');
		if (dirPath.equals(threadDumpDirectory.replace('\\', '/')))
			throw new JzrInitializationException("Failed to decompress the recording. Decompress directory cannot be equal to thread dump directory : " + dirPath);
		
		this.outputDir = dirPath;
	}

	private void loadKeepFiles(Element compressionNode) {
		String value = ConfigUtil.getAttributeValue(compressionNode, JZRA_KEEP_FILES);
		if (value == null || value.isEmpty()){
			logger.warn("Compression file is missing the " + JZRA_KEEP_FILES + " attribute. Decompressed files will be discarded.");
		}
		this.keepTranslatedFiles = Boolean.valueOf(value);
	}
}
