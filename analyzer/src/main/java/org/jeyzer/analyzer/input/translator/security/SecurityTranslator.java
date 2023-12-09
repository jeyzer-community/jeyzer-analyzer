package org.jeyzer.analyzer.input.translator.security;

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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.PSource.PSpecified;

import org.jeyzer.analyzer.config.translator.ConfigTranslator;
import org.jeyzer.analyzer.config.translator.security.ConfigDecryption;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.error.JzrTranslatorException;
import org.jeyzer.analyzer.input.translator.TranslateData;
import org.jeyzer.analyzer.input.translator.Translator;
import org.jeyzer.analyzer.input.translator.worker.TDTranslator;
import org.jeyzer.analyzer.input.translator.worker.TDTranslatorWorker;
import org.jeyzer.analyzer.input.translator.worker.TDTranslatorWorkerFactory;
import org.jeyzer.analyzer.parser.io.ThreadDumpFileDateComparator;
import org.jeyzer.analyzer.parser.io.ThreadDumpFileDateHelper;
import org.jeyzer.analyzer.parser.io.SnapshotFileNameFilter;
import org.jeyzer.analyzer.status.JeyzerStatusEvent;
import org.jeyzer.analyzer.util.SystemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityTranslator implements Translator, TDTranslator {

	private static final Logger logger = LoggerFactory.getLogger(SecurityTranslator.class);	
	
	public static final String PUBLISHED_ENCRYPTED_AES_KEY_FILE = "jzr-recording.key";

	public static final String NAME = "jzr_security";
	private static final short PRIORITY = 200;	
	
	protected Map<String, Throwable> decryptErrors = Collections.synchronizedMap(new HashMap<String, Throwable>());
	
	protected List<File> decryptedFiles = Collections.synchronizedList(new ArrayList<File>());
	protected File ouputProcessCard = null;
	protected File ouputProcessJarPaths = null;
	protected File ouputProcessModules = null;
	protected File outputJvmFlags = null;
	
	private ConfigDecryption translateCfg;
	private SecretKey encryptionKey;
	
	public SecurityTranslator(ConfigDecryption cfg, TranslateData inputData) throws JzrTranslatorException {
		this.translateCfg = cfg;
		if (!translateCfg.isEnabled())
			return;
		
		if (this.translateCfg.isEncryptionKeyPublished()){
			byte[] encryptedKey = loadEncryptedKey(inputData);
			encryptionKey = decryptAESKey(encryptedKey);
		}
		else{
			encryptionKey = cfg.getEncryptionKey();
		}
	}

	@Override
	public boolean accept(TranslateData input) throws JzrTranslatorException {
		return translateCfg.isEnabled();
	}	
	
	@Override
	public TranslateData translate(TranslateData inputData, SnapshotFileNameFilter filter, Date sinceDate) throws JzrTranslatorException {
		logger.info("Executing security translator");
						
		// create the output directory
		try {
			SystemHelper.createDirectory(this.translateCfg.getOuputDirectory());
		} catch (JzrException ex) {
			throw new JzrTranslatorException("Failed to create the directory : " + this.translateCfg.getOuputDirectory()); 
		}
		
		long startTime = 0;
		if (logger.isDebugEnabled())
			startTime = System.currentTimeMillis();
		
		// decrypt the process card
		if (inputData.getProcessCard() != null){
			logger.info("Decrypting process card file : {}", inputData.getProcessCard().getAbsolutePath());
			ouputProcessCard = decryptFile(inputData.getProcessCard());
		}
		
		// decrypt the process jar paths
		if (inputData.getProcessJarPaths() != null){
			logger.info("Decrypting process jar paths file : {}", inputData.getProcessJarPaths().getAbsolutePath());
			ouputProcessJarPaths = decryptFile(inputData.getProcessJarPaths());
		}
		
		// decrypt the process modules
		if (inputData.getProcessModules() != null){
			logger.info("Decrypting process modules file : {}", inputData.getProcessModules().getAbsolutePath());
			ouputProcessModules = decryptFile(inputData.getProcessModules());
		}
		
		// decrypt the JVM flags (but not yet supported)
		if (inputData.getJVMFlags() != null){
			logger.info("Decrypting jvm flags file : {}", inputData.getJVMFlags().getAbsolutePath());
			outputJvmFlags = decryptFile(inputData.getJVMFlags());
		}

		decryptTDFiles(inputData.getTDs(), filter, sinceDate);
		
		if (logger.isDebugEnabled()){
			long endTime = System.currentTimeMillis();
			logger.debug("Decryption took {} ms", endTime-startTime);
		}
		
		// sort files by date (date provided by the filter)
		this.decryptedFiles.sort(new ThreadDumpFileDateComparator(filter));
		
		TranslateData outputData = new TranslateData(
				this.decryptedFiles.toArray(new File[this.decryptedFiles.size()]),
				ouputProcessCard,
				ouputProcessJarPaths,
				ouputProcessModules,
				outputJvmFlags,
				new File(this.translateCfg.getOuputDirectory())
				);
		
		return outputData;
	}

	@Override
	public void translateTDFile(File encryptedFile) {
		logger.info("Decrypting recording file : {}", encryptedFile.getName());
		
		try {
			File decryptedFile = decryptFile(encryptedFile);
			decryptedFiles.add(decryptedFile);
		} catch (JzrTranslatorException ex) {
			this.decryptErrors.put(encryptedFile.getName(), ex.getCause());
		} catch (Exception ex) {
			this.decryptErrors.put(encryptedFile.getName(), ex);
		}
	}

	private File decryptFile(File encryptedFile) throws JzrTranslatorException{
    	Cipher aesCipher;
		try {
			aesCipher = Cipher.getInstance(ConfigDecryption.ENCRYPTION_ALGORITHM);
	    	aesCipher.init(Cipher.DECRYPT_MODE, encryptionKey);
		} catch (GeneralSecurityException ex) {
			throw new JzrTranslatorException(ex);
		}
		
        File targetFile = new File(this.translateCfg.getOuputDirectory() + File.separator + encryptedFile.getName());
		try (
		        FileInputStream fis = new FileInputStream(encryptedFile);
		        CipherInputStream cis = new CipherInputStream(fis, aesCipher);
		        GZIPInputStream gzip = new GZIPInputStream(cis);
				InputStreamReader isr = new InputStreamReader(gzip, "UTF-8");
		        BufferedReader reader = new BufferedReader(isr);
				
				FileWriter fileWriter = new FileWriter(targetFile, false);
		        BufferedWriter writer = new BufferedWriter(fileWriter);
			)
		{
		    String line;
		    while ((line = reader.readLine()) != null) {
		    	writer.write(line);
		    	writer.write(SystemHelper.CR);
		    }
		} catch (IOException ex) {
			if (targetFile.exists() && !targetFile.delete())
				logger.warn("Failed to delete the decrypted file upon decryption error : " + targetFile.getName());
			throw new JzrTranslatorException(ex);
		}
		
		if (!targetFile.setLastModified(encryptedFile.lastModified()))
			logger.warn("Failed to change the last modified timestamp on the decrypted file " + targetFile.getName());
		
		return targetFile;
	}

	@Override
	public void close() {		
		if (translateCfg.isEnabled() && !translateCfg.areTranslatedFilesKept() && decryptedFiles != null){	
			logger.info("Cleaning up the decrypted recording directory : " + translateCfg.getOuputDirectory());
			for (File f : decryptedFiles){
				if (!f.delete())
					logger.warn("Failed to delete decrypted file : " + f.getName());
			}
			decryptedFiles = null;
			
			// delete the original files
			if (ouputProcessCard != null && !ouputProcessCard.delete())
				logger.warn("Failed to delete the decrypted process card : " + ouputProcessCard.getName());
			
			if (ouputProcessJarPaths != null && !ouputProcessJarPaths.delete())
				logger.warn("Failed to delete the decrypted process jar paths : " + ouputProcessJarPaths.getName());
			
			if (ouputProcessModules != null && !ouputProcessModules.delete())
				logger.warn("Failed to delete the decrypted process modules : " + ouputProcessModules.getName());
		}
	}
	
	@Override
	public JeyzerStatusEvent.STATE getStatusEventState() {
		return JeyzerStatusEvent.STATE.DECRYPTION;
	}

	@Override
	public short getPriority() {
		return PRIORITY;
	}
	
	@Override
	public ConfigTranslator getConfiguration() {
		return this.translateCfg;
	}
	
	@Override
	public boolean isEnabled() {
		return this.translateCfg.isEnabled();
	}
	
	private SecretKey decryptAESKey(byte[] decodedAESKey) throws JzrTranslatorException {
        Cipher cipher;
		try {
			cipher = Cipher.getInstance(ConfigDecryption.RSA_OAEP_ALGORITHM);
		} catch (GeneralSecurityException ex) {
			throw new JzrTranslatorException("Failed to instanciate the security translator for the AES key decryption due to invalid RSA encryption algorithm : " + ConfigDecryption.RSA_OAEP_ALGORITHM, ex);
		}
		
        PrivateKey pk;
		try {
			pk = KeyFactory.getInstance(ConfigDecryption.KEY_TYPE).generatePrivate(
					new PKCS8EncodedKeySpec(this.translateCfg.getMasterPrivateKey()));
		} catch (InvalidKeySpecException ex) {
			throw new JzrTranslatorException("Failed to instanciate the security translator for the AES key decryption due to invalid RSA key spec. " + ex.getMessage(), ex);
		} catch (NoSuchAlgorithmException ex) {
			throw new JzrTranslatorException("Failed to instanciate the security translator for the AES key decryption due to invalid RSA algorithm : " + ConfigDecryption.RSA_OAEP_ALGORITHM + ". " + ex.getMessage(), ex);
		}
		
		OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-1"), PSpecified.DEFAULT);
		
    	try {
			cipher.init(Cipher.DECRYPT_MODE, pk, oaepParams);
		} catch (InvalidKeyException ex) {
			throw new JzrTranslatorException("Failed to instanciate the security translator for the AES key decryption due to invalid RSA key. " + ex.getMessage(), ex);
		} catch (InvalidAlgorithmParameterException ex) {
			throw new JzrTranslatorException("Failed to instanciate the security translator for the AES key decryption due to invalid RSA algorithm. " + ex.getMessage(), ex);
		}
    	
    	byte[] decryptedKey;
		try {
			decryptedKey = cipher.doFinal(decodedAESKey);
		} catch (GeneralSecurityException ex) {
			throw new JzrTranslatorException("Failed to decrypt the AES key. " + ex.getMessage(), ex);
		}
        
        SecretKey encryptionKey = new SecretKeySpec(decryptedKey , 0, decryptedKey .length, ConfigDecryption.KEY_ALGORITHM);
        
        return encryptionKey;
	}
	
	private File getKeyFile(File dir) {
		File keyFile = new File(dir + File.separator + SecurityTranslator.PUBLISHED_ENCRYPTED_AES_KEY_FILE);
		if (keyFile.exists() && keyFile.isFile())
			return keyFile;
		else
			return null;
	}

	private byte[] loadEncryptedKey(TranslateData inputData) throws JzrTranslatorException {
		File keyFile = getKeyFile(inputData.getDirectory());		
		if (keyFile == null)
			throw new JzrTranslatorException("recording encryption key file is missing. Please make sure it is provided with the recording.");
		
        String encodedAESKey = null;
        byte[] decodedAESKey = null;
        
        try (
                FileInputStream fis = new FileInputStream(keyFile);
                GZIPInputStream gzip = new GZIPInputStream(fis);
        		InputStreamReader isr = new InputStreamReader(gzip);
                BufferedReader buf = new BufferedReader(isr);
        	)
        {
			encodedAESKey = buf.readLine();
			decodedAESKey = Base64.getDecoder().decode(encodedAESKey);
		} catch (IllegalArgumentException ex) {
			throw new JzrTranslatorException("Failed to read the recording encrypted AES key file : encoded key is invalid", ex);
		} catch (IOException ex) {
			throw new JzrTranslatorException("Failed to read the recording encrypted AES key file.", ex);
		}
		
		return decodedAESKey;
	}
	
	private void decryptTDFiles(File[] files, SnapshotFileNameFilter filter, Date sinceDate) throws JzrTranslatorException {
		try{			
			int cpuCount = Runtime.getRuntime().availableProcessors();
			ExecutorService executor = Executors.newFixedThreadPool(cpuCount, 
					new TDTranslatorWorkerFactory(NAME));
			
			// empty the lists
			decryptErrors.clear();
			
			for (int i=0; i < files.length; i++){
				File file = files[i];
				
				// skip files before sinceDate
				Date date = ThreadDumpFileDateHelper.getFileDate(filter, file);
				if (date.compareTo(sinceDate)<=0)
					continue;
				
				// ignore empty files, will be processed as missing thread dumps
				if (file.length() == 0){
					logger.warn("Recording encrypted file is empty : {}", file.getName());
					continue;
				}
				
				// process in parallel the thread dump decryption
				Runnable worker = new TDTranslatorWorker(this, file);
				executor.execute(worker);
			}
			executor.shutdown();
			
			// wait for thread termination
			while (!executor.isTerminated()){}
			
			if (!decryptErrors.isEmpty()){
				// todo : improve the error handling. Return all decryption errors ?
				
				// Log all decryption errors
				Set<Entry<String, Throwable>> entries = decryptErrors.entrySet();
				Iterator<Entry<String, Throwable>> iter = entries.iterator();
				while(iter.hasNext()){
					Entry<String, Throwable> entry = iter.next();
					logger.warn("Decryption failed for file : " + entry.getKey() + ". Error is :" + entry.getValue().getMessage());
				}
				
				// for now return just the first one
				// And <BR> is super crap. Any workaround ?  \n doesn't work and not printed
				Map.Entry<String, Throwable> error = decryptErrors.entrySet().iterator().next();
				throw new JzrTranslatorException("Decryption of file " 
					+ error.getKey() 
					+ " failed.<BR>Error is : " 
					+ error.getValue().getMessage(), 
					error.getValue()); 
			}
			
		}finally{
			// release resources
			decryptErrors.clear();
		}
	}
}
