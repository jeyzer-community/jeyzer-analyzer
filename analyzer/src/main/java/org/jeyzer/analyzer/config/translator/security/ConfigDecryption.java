package org.jeyzer.analyzer.config.translator.security;

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
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.config.translator.ConfigTranslator;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class ConfigDecryption extends ConfigTranslator{

	private static final Logger logger = LoggerFactory.getLogger(ConfigDecryption.class);

	public static final String TYPE_NAME = "jzr_security";
	
	public static final String MODE_DYNAMIC = "dynamic";
	public static final String MODE_STATIC = "static";
	
	// AES encryption algorithm 
	// Stronger algorithm is "AES/GCM/NoPadding" but requires the "IV" cipher parameter which is random bytes sequence. 
	//  The "IV" must also be used in the decryption and therefore passed in the pay load or stored in the configuration.
	public static final String ENCRYPTION_ALGORITHM = "AES";
	
	// AES key algorithm 
	public static final String KEY_ALGORITHM = "AES";
	
	// RSA local encryption algorithm (signature like security : private to public)
	//   OAEP is not supported by definition in signature approach	
	public static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";
	
	// RSA encryption algorithm used to decrypt the AES key in dynamic mode 
	//   OAEP padding makes it the strongest (encryption like security : public to private)
	public static final String RSA_OAEP_ALGORITHM = "RSA/ECB/OAEPPadding";
	
	public static final String KEY_TYPE = "RSA";
	
	private static final String JZRA_DECRYPTION = "decryption";
	private static final String JZRA_MODE = "mode";
	private static final String JZRA_KEEP_FILES = "keep_files";
	private static final String JZRA_DIRECTORY = "directory";
	private static final String JZRA_STATIC = "static";
	private static final String JZRA_ENCRYPTED_KEY_FILE = "encrypted_key_file";
	private static final String JZRA_DYNAMIC = "dynamic";
	private static final String JZRA_MASTER_PRIVATE_KEY_FILE = "master_private_key_file";
	
	private static final String DECRYPTION_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDo32co2tbUGr12vjhM2BJo2MqrOaXoXgY9T4mRxWhDFOimSPD+dHusyPcKg9NO12YeIX7pl8f5Y/pjdRXGKp5hQdsEAynOGLjGf89x6p4lqRtSoJZNzYFypCVllQNVbysVcxKA7/h66XqtXbNJw42apAFVRmEBhdzvXDSz1UY9uwIDAQAB";
	
	private byte[] masterPrivateKey;
	private SecretKey encryptionKey;
	private String mode;

	public ConfigDecryption(Element translatorNode, String configFilePath, String threadDumpDirectory) throws JzrInitializationException {
		super(TYPE_NAME, configFilePath, true); // always abort on error
		if (translatorNode == null)
			return;
		
		loadSecurity(translatorNode, threadDumpDirectory);
	}
	
	public byte[] getMasterPrivateKey() {
		return masterPrivateKey;
	}

	public SecretKey getEncryptionKey() {
		return encryptionKey;
	}

	public boolean isEncryptionKeyPublished() {
		return MODE_DYNAMIC.equals(mode);
	}

	private void loadSecurity(Element translatorNode, String threadDumpDirectory) throws JzrInitializationException {

		Element decryptionNode = ConfigUtil.getFirstChildNode(translatorNode, JZRA_DECRYPTION);
		if (decryptionNode == null)
			throw new JzrInitializationException("Invalid security file " + this.configFilePath + ". Decryption node not found.");
						
		loadMode(decryptionNode);
		loadKeepFiles(decryptionNode);
		loadDecryptionDirectoryPath(decryptionNode, threadDumpDirectory);
		
		if (isEncryptionKeyPublished())
			loadMasterPrivateKey(decryptionNode);
		else
			loadEncryptionKeyFile(decryptionNode);
		
		this.enabled = true;
	}
	
	private void loadDecryptionDirectoryPath(Element encryptionNode, String threadDumpDirectory) throws JzrInitializationException{
		String dirPath = ConfigUtil.getAttributeValue(encryptionNode, JZRA_DIRECTORY);

		if (dirPath == null || dirPath.isEmpty())
			throw new JzrInitializationException("Invalid security file " + configFilePath + ". Encryption " + JZRA_DIRECTORY + " attribute not found.");
		
		dirPath = dirPath.replace('\\', '/');
		if (dirPath.equals(threadDumpDirectory.replace('\\', '/')))
			throw new JzrInitializationException("Failed to decrypt. Decryption directory cannot be equal to thread dump directory : " + dirPath);
		
		this.outputDir = dirPath;
	}

	private void loadKeepFiles(Element encryptionNode) {
		String value = ConfigUtil.getAttributeValue(encryptionNode, JZRA_KEEP_FILES);
		if (value == null || value.isEmpty()){
			logger.warn("Security file is missing the " + JZRA_KEEP_FILES + " attribute. Decrypted files will be discarded.");
		}
		this.keepTranslatedFiles = Boolean.valueOf(value);
	}

	private void loadMode(Element encryptionNode) throws JzrInitializationException {
		String value = ConfigUtil.getAttributeValue(encryptionNode, JZRA_MODE);
		if (value == null || value.isEmpty())
			throw new JzrInitializationException("Invalid security file " + configFilePath + ". Encryption mode not found.");

		if (!MODE_DYNAMIC.equalsIgnoreCase(value) && !MODE_STATIC.equalsIgnoreCase(value))
			throw new JzrInitializationException("Invalid security file " + configFilePath + ". Encryption mode is invalid : " + value);
		mode = value.toLowerCase();
	}

	private void loadEncryptionKeyFile(Element encryptionNode) throws JzrInitializationException {
		Element staticNode = ConfigUtil.getFirstChildNode(encryptionNode, JZRA_STATIC);
		if (staticNode == null)
			throw new JzrInitializationException("Invalid security file " + this.configFilePath + ". Static node not found.");
		
		String keyPath = ConfigUtil.getAttributeValue(staticNode, JZRA_ENCRYPTED_KEY_FILE);
		if (keyPath == null || keyPath.isEmpty())
			throw new JzrInitializationException("Invalid security file " + this.configFilePath + ". Encryption key path not found.");
		
		File file = new File(keyPath);
		if (!file.isFile() || !file.exists())
			throw new JzrInitializationException("Invalid encryption key file path : " + keyPath);

		String encodedEncryptedAESKey;
		try (
				FileReader fr = new FileReader(file);
				BufferedReader buffer = new BufferedReader(fr);
			)
		{
			encodedEncryptedAESKey = buffer.readLine();
		} catch (IOException ex) {
			throw new JzrInitializationException("Failed to read the encryption key file : " + keyPath, ex);
		}
		
		byte[] decodedAESKey = decryptAESKey(encodedEncryptedAESKey);
		
		this.encryptionKey = new SecretKeySpec(decodedAESKey, KEY_ALGORITHM);
	}

	private byte[] decryptAESKey(String encodedEncryptedAESKey) throws JzrInitializationException {
		byte[] decodedEncryptedAESKey = Base64.getDecoder().decode(encodedEncryptedAESKey);
		byte[] decodedRSAKey = Base64.getDecoder().decode(DECRYPTION_PUBLIC_KEY);
		
        Cipher cipher;
		try {
			cipher = Cipher.getInstance(RSA_ALGORITHM);
		} catch (GeneralSecurityException ex) {
			throw new JzrInitializationException("Failed to instanciate the security configuration for the AES key decryption due to invalid RSA encryption algorithm : " + RSA_ALGORITHM, ex);
		}
		
        PublicKey publicKey;
		try {
			publicKey = KeyFactory.getInstance(KEY_TYPE).generatePublic(new X509EncodedKeySpec(decodedRSAKey));
		} catch (InvalidKeySpecException ex) {
			throw new JzrInitializationException("Failed to instanciate the security configuration for the AES key decryption due to invalid RSA key spec. " + ex.getMessage(), ex);
		} catch (NoSuchAlgorithmException ex) {
			throw new JzrInitializationException("Failed to instanciate the security configuration for the AES key decryption due to invalid RSA alggorithm : " + RSA_ALGORITHM + ". " + ex.getMessage(), ex);
		}
        
    	try {
			cipher.init(Cipher.PRIVATE_KEY, publicKey);
		} catch (InvalidKeyException ex) {
			throw new JzrInitializationException("Failed to instanciate the security configuration for the AES key decryption due to invalid RSA key. " + ex.getMessage(), ex);
		}
    	
    	byte[] decryptedAESKey;
		try {
			decryptedAESKey = cipher.doFinal(decodedEncryptedAESKey);
		} catch (GeneralSecurityException ex) {
			throw new JzrInitializationException("Failed to decrypt the AES key. " + ex.getMessage(), ex);
		}

		return decryptedAESKey;
	}

	private void loadMasterPrivateKey(Element securityNode) throws JzrInitializationException {
		Element dynamicNode = ConfigUtil.getFirstChildNode(securityNode, JZRA_DYNAMIC);
		if (dynamicNode == null)
			throw new JzrInitializationException("Invalid security file " + this.configFilePath + ". Dynamic node not found.");
		
		String keyPath = ConfigUtil.getAttributeValue(dynamicNode, JZRA_MASTER_PRIVATE_KEY_FILE);
		if (keyPath == null || keyPath.isEmpty())
			throw new JzrInitializationException("Invalid security file " + this.configFilePath + ". Master private key path not found.");
		
		File file = new File(keyPath);
		if (!file.isFile() || !file.exists())
			throw new JzrInitializationException("Invalid master private key file : " + keyPath);

		List<String> chunks;
		try {
			chunks = readKeyFileChunks(file);
		} catch (IOException ex) {
			throw new JzrInitializationException("Failed to read the master private key file : " + keyPath, ex);
		}
		
		this.masterPrivateKey = decryptRSAKey(chunks);
	}
	
	private List<String> readKeyFileChunks(File file) throws IOException{
		List<String> chunks = new ArrayList<>();
		
		try (
				FileReader fr = new FileReader(file);
				BufferedReader reader = new BufferedReader(fr);
			)
		{
			String line = reader.readLine();
			while(line != null){
				chunks.add(line);
				line = reader.readLine();
			}			
		}
		
		return chunks;
	}
	
	private byte[] decryptRSAKey(List<String> chunks) throws JzrInitializationException {
		PublicKey jeyzerPubKey = getJeyzerMasterPublicKey();
		byte[] loadedKey = new byte[0];
		int globalpos = 0;
		for (String chunk : chunks){
			byte[] bytes = Base64.getDecoder().decode(chunk);
			
			Cipher cipher;
			try {
				cipher = Cipher.getInstance(RSA_ALGORITHM);
			} catch (GeneralSecurityException ex) {
				throw new JzrInitializationException("Failed to instanciate the security configuration for the dynamic RSA private key decryption due to invalid RSA encryption algorithm : " + RSA_ALGORITHM, ex);
			}
			try {
				cipher.init(Cipher.DECRYPT_MODE, jeyzerPubKey);
			} catch (InvalidKeyException ex) {
				throw new JzrInitializationException("Failed to instanciate the security configuration for the dynamic RSA private key decryption due to invalid RSA key. " + ex.getMessage(), ex);
			}
			byte[] decryptedLoadedKey;
			try {
				decryptedLoadedKey = cipher.doFinal(bytes);
			} catch (GeneralSecurityException ex) {
				throw new JzrInitializationException("Failed to decrypt the dynamic RSA private key. " + ex.getMessage(), ex);
			}
			
			loadedKey = Arrays.copyOf(loadedKey, loadedKey.length + decryptedLoadedKey.length);
			for (byte element : decryptedLoadedKey){
				loadedKey[globalpos] = element;
				globalpos++;
			}
		}
		return loadedKey;
	}
	
	private PublicKey getJeyzerMasterPublicKey() throws JzrInitializationException {
        PublicKey publicKey;
        
		byte[] decodedRSAKey = Base64.getDecoder().decode(DECRYPTION_PUBLIC_KEY);
		try {
			publicKey = KeyFactory.getInstance(KEY_TYPE).generatePublic(new X509EncodedKeySpec(decodedRSAKey));
		} catch (InvalidKeySpecException ex) {
			throw new JzrInitializationException("Failed to instanciate the security configuration for the dynamic RSA private key decryption due to invalid RSA key spec. " + ex.getMessage(), ex);
		} catch (NoSuchAlgorithmException ex) {
			throw new JzrInitializationException("Failed to instanciate the security configuration for the dynamic RSA private key decryption due to invalid RSA alggorithm : " + RSA_ALGORITHM + ". " + ex.getMessage(), ex);
		}
		return publicKey;
	}
}
