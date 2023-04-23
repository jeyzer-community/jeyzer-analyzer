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




import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrSecurityInvalidPasswordException;
import org.jeyzer.analyzer.util.SystemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigSecurity {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigSecurity.class);

	private static final int MINIMAL_PWD_SIZE = 8;	
	
	private static final String JZRR_SECURITY_FILE = "security_file";
	private static final String JZRR_SECURITY = "security";
	private static final String JZRR_PASSWORD = "password";
	private static final String JZRR_MODE = "mode";
	private static final String JZRR_VALUE = "value";
	private static final String JZRR_ENCRYPTION = "encryption";
	private static final String JZRR_ALGORITHM = "algorithm";
	
	/*
	 * WARNING : Duplicate exists in Jeyzer Web project as SecMode enum
	 * Duplicated to not expose the whole security layer of current package after obfuscation.
	 */
	public static final String NONE = "none";
	public static final String EXTERNAL_OPTIONAL = "external optional";
	public static final String EXTERNAL_MANDATORY = "external mandatory";
	public static final String INTERNAL_MANDATORY = "internal mandatory";
	
	
	private ConfigSecurityEncryption encryption;
	
	private String mode = NONE;
	private String pwd;
	
	// setup constructor
	public ConfigSecurity(Element node) throws JzrInitializationException {
		String securityFile = ConfigUtil.getAttributeValue(node, JZRR_SECURITY_FILE);
		if (securityFile == null)
			throw new JzrInitializationException("Default setup report security file must be provided.");
		logger.info("Loading the setup report security configuration from file : " + SystemHelper.sanitizePathSeparators(securityFile));
		loadConfiguration(securityFile);
	}
	
	// standard constructor
	public ConfigSecurity(Element node, ConfigSecurity parent) throws JzrInitializationException {
		String securityFile = ConfigUtil.getAttributeValue(node, JZRR_SECURITY_FILE);
		if (securityFile == null || securityFile.isEmpty()){
			// take setup values
			logger.info("Report security configuration inherited from global setup.");
			this.mode = parent.getSecurityMode();
			this.pwd = parent.getInternalPassword();
			this.encryption = parent.getEncryption();
			return;
		}
		logger.info("Report security configuration read from file : " + SystemHelper.sanitizePathSeparators(securityFile));	
		loadConfiguration(securityFile);
	}
	
	public String getSecurityMode(){
		return this.mode;
	}
	
	public ConfigSecurityEncryption getEncryption() {
		return encryption;
	}
	
	public String getPassword() throws JzrSecurityInvalidPasswordException{
		if (!isValid())
			throw new JzrSecurityInvalidPasswordException("Password is not set.");
		validatePasswordSize();
		return pwd;
	}

	public boolean isValid(){
		switch(mode)
		{
			case EXTERNAL_MANDATORY:
			case INTERNAL_MANDATORY:
				return pwd != null;
			case EXTERNAL_OPTIONAL:
			case NONE:
				return true;
			default:
				return false;
		}
	}
	
	public boolean isReportSecured(){
		return EXTERNAL_MANDATORY.equals(mode)
			|| INTERNAL_MANDATORY.equals(mode)
			|| (EXTERNAL_OPTIONAL.equals(mode) && pwd != null  && !pwd.isEmpty());
	}

	private String getInternalPassword(){
		return pwd;
	}
	
	private void loadConfiguration(String path) throws JzrInitializationException {
		Element securityNode = loadSecurityConfigurationFile(path);
		loadSecurityMode(securityNode);
		if (!NONE.equals(this.mode))
			loadEncryption(securityNode);
	}
	
	private void loadEncryption(Element securityNode) throws JzrInitializationException {
		Element encryptionNode = ConfigUtil.getFirstChildNode(securityNode, JZRR_ENCRYPTION);
		if (encryptionNode == null)
			throw new JzrInitializationException("Report security is defined without any encryption section.");
		
		String algorithmValue = ConfigUtil.getAttributeValue(encryptionNode, JZRR_ALGORITHM);
		if (algorithmValue == null || algorithmValue.isEmpty())
			throw new JzrInitializationException("Report security is defined without any encryption algorithm.");
		
		this.encryption = ConfigSecurityEncryption.getConfigSecurityEncryption(algorithmValue);
		if (this.encryption == null)
			throw new JzrInitializationException("Report security is defined with invalid encryption algorithm : " + algorithmValue);
	}

	private void loadSecurityMode(Element securityNode) throws JzrInitializationException {
		Element passwordNode = ConfigUtil.getFirstChildNode(securityNode, JZRR_PASSWORD);
		if (passwordNode == null)
			throw new JzrInitializationException("Report security is defined without any password section.");
		
		String modeValue = ConfigUtil.getAttributeValue(passwordNode, JZRR_MODE);
		if (modeValue == null || modeValue.isEmpty())
			throw new JzrInitializationException("Report security is defined without any password mode.");
		
		if (NONE.equalsIgnoreCase(modeValue.trim()))
			this.mode = NONE;
		else if (EXTERNAL_OPTIONAL.equalsIgnoreCase(modeValue.trim()))
			this.mode = EXTERNAL_OPTIONAL;
		else if (EXTERNAL_MANDATORY.equalsIgnoreCase(modeValue.trim()))
			this.mode = EXTERNAL_MANDATORY;
		else if (INTERNAL_MANDATORY.equalsIgnoreCase(modeValue.trim()))
			this.mode = INTERNAL_MANDATORY;
		else
			throw new JzrInitializationException("Report security is defined with invalid password mode : " + modeValue);
		
		// Let's load the password
		if (!NONE.equals(this.mode)){
			this.pwd = ConfigUtil.getAttributeValue(passwordNode, JZRR_VALUE);
			
			// password must be specified in the mandatory cases
			if (EXTERNAL_MANDATORY.equals(this.mode) || INTERNAL_MANDATORY.equals(this.mode)){
				if (this.pwd == null|| this.pwd.isEmpty())
					throw new JzrInitializationException("Report security is expecting password value in " + this.mode + " password mode.");
			}
			
			// password must be validated (when not optional/empty)
			if (!(EXTERNAL_OPTIONAL.equals(this.mode) && this.pwd.isEmpty()))
			{
				try{
					validatePasswordSize();
				}catch(JzrSecurityInvalidPasswordException ex){
					throw new JzrInitializationException("Report security password value is invalid.", ex);
				}
			}
		}
	}

	private void validatePasswordSize() throws JzrSecurityInvalidPasswordException {
		if (EXTERNAL_MANDATORY.equals(this.mode) && this.pwd.length() < MINIMAL_PWD_SIZE)
			throw new JzrSecurityInvalidPasswordException("Password must contain at least " + MINIMAL_PWD_SIZE + " characters.");
		else if (EXTERNAL_OPTIONAL.equals(this.mode) && this.pwd.length() > 0 && this.pwd.length() < MINIMAL_PWD_SIZE)
			throw new JzrSecurityInvalidPasswordException("When it is set, optional password must contain at least " + MINIMAL_PWD_SIZE + " characters.");
	}

	private Element loadSecurityConfigurationFile(String path) throws JzrInitializationException {
		Document doc;
		
		try {
			doc = ConfigUtil.loadXMLFile(path);
			if (doc == null){
				logger.error("Failed to open the report security configuration resource using path : " + path);
				throw new JzrInitializationException("Failed to open the report security configuration resource using path : " + path);
			}
		} catch (JzrInitializationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Failed to open the report security configuration resource using path : " + path, e);
			throw new JzrInitializationException("Failed to open the report security configuration resource using path : " + path, e);
		}
		
		NodeList securityNodes = doc.getElementsByTagName(JZRR_SECURITY);
		if (securityNodes == null){
			logger.error("Report security configuration " + path + " is invalid.");
			throw new JzrInitializationException("Report security configuration " + path + " is invalid.");
		}
		return (Element)securityNodes.item(0);
	}
}
