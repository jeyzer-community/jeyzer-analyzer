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

public interface ZipParams {
	
	public String getOuputDirectory();
	
	public int getFileSizeLimit();
	
	public long getFileSizeLimitInBytes();

	public int getUncompressedSizeLimit();
	
	public long getUncompressedSizeLimitInBytes();

	public long getUncompressedFilesLimit();
	
	public boolean areTranslatedFilesKept();

}
