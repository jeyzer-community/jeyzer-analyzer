package org.jeyzer.web.analyzer;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Web
 * --
 * Copyright (C) 2020 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */


public class JzrDiscoveryItem {

	private String keyWords = "";
	private String color;
	
	public JzrDiscoveryItem(String keyWords, String color) {
		this.keyWords = keyWords;
		this.color = color;
	}

	public JzrDiscoveryItem() {
	}

	public String getKeyWords() {
		return keyWords;
	}

	public String getColor() {
		return color;
	}

	public void setKeyWords(String keyWords) {
		this.keyWords = keyWords;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
}
