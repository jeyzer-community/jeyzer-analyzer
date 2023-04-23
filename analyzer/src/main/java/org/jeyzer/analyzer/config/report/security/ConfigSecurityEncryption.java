package org.jeyzer.analyzer.config.report.security;

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




import org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.HashAlgorithm;

public class ConfigSecurityEncryption {
	
	public static final String BINARY_RC4 = "BINARY_RC4";
	public static final String AES_256    = "AES_256";

	public static ConfigSecurityEncryption getConfigSecurityEncryption(String type) {
		if (BINARY_RC4.equalsIgnoreCase(type.trim()))
			return new ConfigSecurityEncryption("Standard rc4", EncryptionMode.binaryRC4, CipherAlgorithm.rc4, HashAlgorithm.sha1);
		else if (AES_256.equalsIgnoreCase(type.trim()))
			return new ConfigSecurityEncryption("AES 256", EncryptionMode.agile, CipherAlgorithm.aes256, HashAlgorithm.sha1);
		return null;
	}

	private String label;
	private EncryptionMode encryptionMode;
	private CipherAlgorithm cipherAlgorithm;
	private HashAlgorithm hashAlgorithm;
	
	private ConfigSecurityEncryption(String label, EncryptionMode encryptionMode, CipherAlgorithm cipherAlgorithm, HashAlgorithm hashAlgorithm){
		this.label = label;
		this.encryptionMode = encryptionMode;
		this.cipherAlgorithm = cipherAlgorithm;
		this.hashAlgorithm = hashAlgorithm;
	}
	
	public String getLabel(){
		return this.label;
	}

	public EncryptionMode getEncryptionMode() {
		return encryptionMode;
	}

	public CipherAlgorithm getCipherAlgorithm() {
		return cipherAlgorithm;
	}
	
	public HashAlgorithm getHashAlgorithm() {
		return hashAlgorithm;
	}
}
