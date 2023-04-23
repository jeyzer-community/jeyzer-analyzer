package org.jeyzer.analyzer.error;

import java.util.Set;

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

public class JzrTranslatorRecordingSnapshotNotFoundException  extends JzrTranslatorException{

	private static final long serialVersionUID = -8509151289887818552L;

	private Set<String> supportedFileFormats;
	
	/**
	 * Translation recording snapshot not found exception, due to human error
	 * Error message is made public (for example through the web UI)
	 */
	public JzrTranslatorRecordingSnapshotNotFoundException(String message, Set<String> supportedFileFormats) {
		super(message);
		this.supportedFileFormats = supportedFileFormats;
	}

	public Set<String> getSupportedFileFormats() {
		return supportedFileFormats;
	}
}
