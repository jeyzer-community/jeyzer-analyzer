package org.jeyzer.web.config;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Web
 * --
 * Copyright (C) 2020 - 2021 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import org.jeyzer.analyzer.util.ZipParams;

public class ConfigGzipParams implements ZipParams {

	private static final int BYTES_IN_1_MB = 1048586;
	
	private ConfigWeb configWeb;
	
	public ConfigGzipParams(ConfigWeb configWeb) {
		this.configWeb = configWeb;
	}
	
	@Override
	public String getOuputDirectory() {
		return this.configWeb.getWorkDirectory();
	}

	@Override
	public int getFileSizeLimit() {
		return Integer.MAX_VALUE;
	}

	@Override
	public long getFileSizeLimitInBytes() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int getUncompressedSizeLimit() {
		return this.configWeb.getUploadRecordingUncompressedMaxSize();
	}

	@Override
	public long getUncompressedSizeLimitInBytes() {
		return (long)this.configWeb.getUploadRecordingUncompressedMaxSize() * BYTES_IN_1_MB;
	}

	@Override
	public long getUncompressedFilesLimit() {
		return this.configWeb.getUploadRecordingUncompressedMaxFiles();
	}

	@Override
	public boolean areTranslatedFilesKept() {
		return false;
	}
}
