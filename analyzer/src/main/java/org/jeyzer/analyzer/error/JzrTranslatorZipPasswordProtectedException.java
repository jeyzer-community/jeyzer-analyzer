package org.jeyzer.analyzer.error;

public class JzrTranslatorZipPasswordProtectedException extends JzrTranslatorException {

	/**
	 * Translation recording zip file is password encrypted (and we do not have the password)
	 * Error message is made public (for example through the web UI)
	 * 
	 * @param public message
	 */
	private static final long serialVersionUID = 1964783867035220534L;

	public JzrTranslatorZipPasswordProtectedException(String message) {
		super(message);
	}	
	
}
