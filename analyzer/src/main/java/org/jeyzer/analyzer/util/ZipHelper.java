package org.jeyzer.analyzer.util;

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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.error.JzrTranslatorException;
import org.jeyzer.analyzer.error.JzrTranslatorLimitViolationException;
import org.jeyzer.analyzer.error.JzrTranslatorZipInvalidFileException;
import org.jeyzer.analyzer.error.JzrTranslatorZipPasswordProtectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipHelper {

	private static final Logger logger = LoggerFactory.getLogger(ZipHelper.class);

	public static final String ZIP_EXTENSION = ".zip";
	public static final String GZIP_EXTENSION = ".gz";
	public static final String TAR_EXTENSION = ".tar";

	private static final int BUFFER = 2048;
	
	private static final String MAC_OS_HIDDEN_DIR = "__MACOSX/";

	public static boolean isCompressedFile(String filename) {
		return isZipFile(filename) ? true : isGzipFile(filename);
	}

	public static boolean isZipFile(String filename) {
		return filename != null && filename.toLowerCase().endsWith(ZIP_EXTENSION);
	}

	public static boolean isGzipFile(String filename) {
		return filename != null && filename.toLowerCase().endsWith(GZIP_EXTENSION);
	}
	
	public static boolean isTarFile(File file) {
		if (file == null)
			return false;
		if (file.getName().toLowerCase().endsWith(TAR_EXTENSION))
			return true;
		
		try (
				final InputStream is = new FileInputStream(file);
				TarArchiveInputStream debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", is);
			) 
		{
			// try to read it as a tar file
			debInputStream.getNextEntry();
		}
		catch (Exception ex) {
			return false;
		}
		return true;
	}
	
	public static List<String> listContent(String filename, ZipParams params) {
		List<String> fileNames = new ArrayList<>();

		if (isZipFile(filename)) {
			try {
				try (ZipFile zipFile = new ZipFile(filename)) {
				    Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
				    while (zipEntries.hasMoreElements()) {
				        ZipEntry entry =  zipEntries.nextElement();
				        if (!entry.isDirectory() && !isMacOSHiddenArchiveFile(entry.getName()))
				        	fileNames.add(entry.getName());
				    }
				}
			} catch (Exception ex) {
				logger.warn("Failed to list the content of the file " + filename, ex);
				return fileNames;
			}
		} else if (isGzipFile(filename)) {
			File gzipFile = new File(filename);
			File tarFile = new File(params.getOuputDirectory(), gzipFile.getName().substring(0, gzipFile.getName().length() - 3));
			String outputFolder = params.getOuputDirectory();
			try {
				fileNames = listTar(tarFile);
			} catch (Exception ex) {
				logger.warn("Failed to list the file " + filename + " in the output folder " + outputFolder, ex);
				return fileNames;
			}
			finally {
				// always remove the intermediary tar file
				if (tarFile.exists() && !tarFile.delete())
					logger.warn("Failed to delete the intermediary tar file {}", tarFile.getName());
			}
		}

		return fileNames;
	}

	private static List<String> listTar(File tarFile) throws IOException, ArchiveException {
		List<String> fileNames = new ArrayList<>();
		
		try (
				final InputStream is = new FileInputStream(tarFile);
				TarArchiveInputStream debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", is);
			) 
		{
			ArchiveEntry entry = debInputStream.getNextEntry();
			while (entry != null) {
			  entry = debInputStream.getNextEntry();
			  if (!entry.isDirectory())
				  fileNames.add(entry.getName());
			}
		}

		return fileNames;
	}

	public static List<File> uncompress(String filePath, ZipParams params) throws JzrTranslatorException {
		List<File> uncompressedFiles = null;
		String outputFolder = params.getOuputDirectory();
		String fileName = new File(filePath).getName();

		if (isZipFile(filePath)) {
			try {
				uncompressedFiles = unzip(filePath, params);
			} catch (JzrTranslatorException e) {
				throw e;
			} catch (Exception ex) {
				logger.warn("Failed to unzip the file " + filePath + " in the output folder " + outputFolder, ex);
				throw new JzrTranslatorException("Failed to unzip the file " + fileName, ex);
			}
		} else if (isGzipFile(filePath)) {
			boolean tarDetected = false;
			File gzipFile = new File(filePath);
			File tarFile = new File(params.getOuputDirectory(), gzipFile.getName().substring(0, gzipFile.getName().length() - 3));
			try {
				unGzip(gzipFile, tarFile, params);
				
				tarDetected = isTarFile(tarFile);
				if (tarDetected)
					uncompressedFiles = unTar(tarFile, params);
				else {
					uncompressedFiles = new LinkedList<>();
					uncompressedFiles.add(tarFile);
				}
			} catch (JzrTranslatorException e) {
				throw e;
			} catch (Exception ex) {
				logger.warn("Failed to untar/ungzip the file " + filePath + " in the output folder " + outputFolder, ex);
				throw new JzrTranslatorException("Failed to untar/ungzip the file " + fileName + ". Make sure that the provided file is a valid tar gz file.", ex);
			}
			finally {
				// always remove the intermediary tar file
				if (tarDetected && tarFile.exists())
					if (!tarFile.delete())
						logger.warn("Failed to delete the intermediary tar file " + tarFile.getName());
			}
		}

		return uncompressedFiles;
	}
	
	public static void cleanUncompressedDirectory(List<File> unzippedFiles, String lastFilePath, ZipParams params, String type) {
		if (!params.areTranslatedFilesKept()) {			
			// delete the intermediary files
			for (File file : unzippedFiles) {
				if (file.exists()) 
					if (!file.delete())
						logger.warn("Failed to delete the " + type + " file : " + file.getPath());
			}
			unzippedFiles.clear();
			
			// delete also the last processed file (in case of unzip/untar error)
			if (lastFilePath != null) {
				File lastFile = new File(lastFilePath);
				if (lastFile.exists())
					if (!lastFile.delete())
						logger.warn("Failed to delete the last " + type + " file : " + lastFile.getPath());
			}
			
			// delete any remaining empty directory inside the output directory
			File outputDirectory = new File(params.getOuputDirectory());
		    File[] allContents = outputDirectory.listFiles();
		    if (allContents != null) {
		        for (File directoryToDelete : allContents) {
		        	if (directoryToDelete.isDirectory())
		        		deleteDirectory(directoryToDelete, type);
		        }
		    }
		}
	}

	private static List<File> unzip(String zipFilePath, ZipParams params) throws IOException, JzrException {
		List<File> unzippedFiles = new LinkedList<>();
		long uncompressedTotalSize = 0;
		int uncompressedFilesCount = 0;
		String filePath = null;

		File destDir = new File(params.getOuputDirectory());
		if (!destDir.exists())
			if (!(destDir.mkdir()))
				throw new JzrException("Failed to create the zip output directory : " + destDir.getPath());

		validateFileSize(zipFilePath, params, "Zip");
		
		try (
				FileInputStream fileStream = new FileInputStream(zipFilePath);
				ZipInputStream zipIn = new ZipInputStream(fileStream);
			) 
		{
			logger.info("Unzipping zip file : " + SystemHelper.sanitizePathSeparators(zipFilePath));
			logger.info("    into directory : " + destDir.getAbsoluteFile());

			ZipEntry entry = null;
			try{
				entry = zipIn.getNextEntry();
			}
			catch (ZipException ex) {
				// password protected zip file
				if ("encrypted ZIP entry not supported".equals(ex.getMessage())){
					logger.warn("Failed to open the zip file : it is password encrypted");
					throw new JzrTranslatorZipPasswordProtectedException("Failed to open the zip file : it is password encrypted");				
				}
				else {
					// re-throw
					throw ex;
				}
			}
			// iterates over entries in the zip file
			
			// Invalid zip file. 
			//  Example : rar file renamed as zip file
			if (entry == null)
				throw new JzrTranslatorZipInvalidFileException("Zip format is invalid. Please use a standard zip tool.");
			
			while (entry != null) {
				String fileName = buildFileName(entry.getName());
				validateUncompressedFilename(fileName, ".", "Zip");
				filePath = params.getOuputDirectory() + File.separator + fileName;
				
				if (!entry.isDirectory()) {
					
					if (isMacOSHiddenArchiveFile(fileName)) {
						logger.info("Unzipping zip file - Mac hidden archive entry file detected and ignored : " + fileName);
						closeZipEntry(zipIn);
						entry = zipIn.getNextEntry();
						continue;
					}
					
					if (entry.getSize() == 0) {
						logger.info("Unzipping zip file - Empty entry file detected and ignored : " + fileName);
						closeZipEntry(zipIn);
						entry = zipIn.getNextEntry();
						continue;
					}
					
					// if parent is directory, need to create the dir anyway..
					File parent = (new File(filePath)).getParentFile();
					if (!parent.exists())
						if (!parent.mkdirs())
							throw new JzrTranslatorException("Failed to create the directory " + parent.getAbsolutePath());
					
					// if the entry is a file, extract it
					logger.info("Unzipping zip file - create file : " + fileName);
					uncompressedTotalSize = extractFile(zipIn, filePath, uncompressedTotalSize, params);

					if (uncompressedTotalSize + BUFFER > params.getUncompressedSizeLimitInBytes())
						throw new JzrTranslatorLimitViolationException("Zip file content size is larger than the authorized limit of " +  params.getUncompressedSizeLimit() + " Mb");

					uncompressedFilesCount++;
					if (uncompressedFilesCount > params.getUncompressedFilesLimit())
						throw new JzrTranslatorLimitViolationException("Number of files to decompress is larger than the authorized limit of " + params.getUncompressedFilesLimit() + " files");

					// keep last modified time
					File extractedFile = new File(filePath);
					if (!extractedFile.setLastModified(entry.getTime()))
						logger.warn("Failed to set the last modified date on the extracted file " + extractedFile.getName());

					unzippedFiles.add(extractedFile);
				} else {
					// if the entry is a directory, make the directory
					File dir = new File(filePath);
					logger.info("Unzipping zip file - create directory : " + dir.getAbsolutePath());
					dir.mkdirs();
				}

				closeZipEntry(zipIn);
				entry = zipIn.getNextEntry();
			}
		}
		catch (Exception ex) {
			// Required as the unzippedFiles is not returned.
			cleanUncompressedDirectory(unzippedFiles, filePath, params, "unzipped");
			
			// re-throw
			throw ex;
		}
		
		return unzippedFiles;
	}

	private static void unGzip(File gzipFile, File tarFile, ZipParams params) throws FileNotFoundException, IOException, JzrTranslatorException {
		validateFileSize(gzipFile.getPath(), params, "Gzip");

		final File outputDir = new File(params.getOuputDirectory());
		if (!outputDir.exists())
			if (!(outputDir.mkdir()))
				throw new JzrTranslatorException("Failed to create the gzip output directory : " + outputDir.getPath());

		try (
				FileInputStream fis = new FileInputStream(gzipFile);
				GZIPInputStream gzipIn = new GZIPInputStream(fis);
			) 
		{
			logger.info("Unzipping gzip file : " + gzipFile.getAbsoluteFile());
			
			long uncompressedTotalSize = 0;
			uncompressedTotalSize = extractFile(gzipIn, tarFile.getPath(), uncompressedTotalSize, params);

			if (uncompressedTotalSize + BUFFER > params.getUncompressedSizeLimitInBytes())
				throw new JzrTranslatorLimitViolationException("Zip file content size is larger than the authorized limit of " +  params.getUncompressedSizeLimit() + " Mb");
		}
	}

	private static List<File> unTar(final File inputFile, ZipParams params) throws FileNotFoundException, IOException, ArchiveException, JzrTranslatorException {
		List<File> untaredFiles = new LinkedList<>();
		String outputFilePath = null;

		try (
				final InputStream is = new FileInputStream(inputFile);
				TarArchiveInputStream debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", is);
			) 
		{
			final File outputDir = new File(params.getOuputDirectory());
			logger.info("Untar file : " + inputFile.getAbsoluteFile());
			logger.info("    into directory : " + outputDir.getAbsoluteFile());

			TarArchiveEntry entry = null;
			int uncompressedFilesCount = 0;
			long uncompressedTotalSize = 0;
			while ((entry = (TarArchiveEntry) debInputStream.getNextEntry()) != null) {
				String fileName = buildFileName(entry.getName());
				validateUncompressedFilename(fileName, ".", "Tar");
				
				File outputFile = new File(outputDir, fileName);
				outputFilePath = outputFile.getPath();
						
				if (entry.isDirectory()) {
					logger.info("Untar file - create directory : " + outputFile.getAbsolutePath());
					if (!outputFile.exists())
						if (!outputFile.mkdirs())
							throw new JzrTranslatorException("Failed to create the directory " + outputFile.getAbsolutePath());
				} else {
					if (entry.getSize() == 0) {
						logger.info("Untar file - Empty entry file detected and ignored : " + fileName);
						continue;
					}
					logger.info("Untar file - create file : " + outputFile.getAbsolutePath());
					// if tar contains directories
					if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs())
							throw new JzrTranslatorException("Failed to create the directory " + outputFile.getAbsolutePath());
					try (
							OutputStream outputFileStream = new FileOutputStream(outputFile);
						) 
					{
						IOUtils.copy(debInputStream, outputFileStream);
					}
					
					uncompressedTotalSize += outputFile.length();
					if (uncompressedTotalSize > params.getUncompressedSizeLimitInBytes())
						throw new JzrTranslatorLimitViolationException("Tar file content size is larger than the authorized limit of " +  params.getUncompressedSizeLimit() + " Mb");

					uncompressedFilesCount++;
					if (uncompressedFilesCount > params.getUncompressedFilesLimit())
						throw new JzrTranslatorLimitViolationException("Number of files to decompress is larger than the authorized limit of " + params.getUncompressedFilesLimit() + " files");

					// Keep last modified by
					if (!outputFile.setLastModified(entry.getLastModifiedDate().getTime()))
						logger.warn("Failed to set the last modified date on the extracted file " + outputFile.getName());
					
					untaredFiles.add(outputFile);
				}
			}
		}
		catch (Exception ex) {
			// Required as the untaredFiles is not returned.
			cleanUncompressedDirectory(untaredFiles, outputFilePath, params, "untarred");
			
			// re-throw
			throw ex;
		}

		return untaredFiles;
	}
	
	private static void validateFileSize(String zipFilePath, ZipParams params, String type) throws JzrTranslatorLimitViolationException {
		File file = new File(zipFilePath);
		if (file.length() > params.getFileSizeLimitInBytes())
			throw new JzrTranslatorLimitViolationException(type + " file size is exceeding the authorized limit of " + params.getFileSizeLimit() + " Mb");
	}

	private static void validateUncompressedFilename(String filename, String workDir, String type) throws IOException, JzrTranslatorLimitViolationException {
		// Security check
		File f = new File(filename);
		String canonicalPath = f.getCanonicalPath();

		File iD = new File(workDir);
		String canonicalID = iD.getCanonicalPath();

		if (!canonicalPath.startsWith(canonicalID))
			throw new JzrTranslatorLimitViolationException(type + " file contains files outside of the extraction target directory.");
	}

	private static long extractFile(InflaterInputStream zipIn, String filePath, long total, ZipParams params) throws IOException {
		try (
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
			) 
		{
			byte[] bytesIn = new byte[BUFFER];
			int read = 0;
			while (total + BUFFER <= params.getUncompressedSizeLimitInBytes() && (read = zipIn.read(bytesIn, 0, BUFFER)) != -1) {
				bos.write(bytesIn, 0, read);
				total += read;
			}
		}
		return total;
	}
	
	private static void deleteDirectory(File directoryToBeDeleted, String type) {
	    File[] allContents = directoryToBeDeleted.listFiles();
	    if (allContents != null) {
	        for (File dir : allContents) {
	        	if (dir.isDirectory())
	        		deleteDirectory(dir, type);
	        }
	    }
	    if (!directoryToBeDeleted.delete()) // only if directory is empty in Java
	    	logger.warn("Failed to delete the " + type + " directory : " + directoryToBeDeleted.getPath());
	}
	
	private static boolean isMacOSHiddenArchiveFile(String fileName) {
		return fileName.startsWith(MAC_OS_HIDDEN_DIR);
	}

	private static String buildFileName(String name) {
		if (SystemHelper.isWindows())
			// support the ':' char used on Unix as part of date formats
			// Char ':' is forbidden on Windows, so let's replace it
			return name.replace(":", "-");
		else
			return name;
	}
	
	private static void closeZipEntry(ZipInputStream zipIn) throws JzrException, IOException {
		try {
			zipIn.closeEntry();
		} catch (ZipException e) {
			// Case : "java.util.zip.ZipException: invalid entry CRC (expected 0x0 but found.."
			logger.warn("Unzipping zip file - error on zip close entry : " + e.getMessage());
			throw new JzrException("Zip file is invalid. Please zip the thread dumps with different zip tool and retry.");
		}
	}
}
