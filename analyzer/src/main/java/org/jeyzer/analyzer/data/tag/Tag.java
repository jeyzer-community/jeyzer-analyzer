package org.jeyzer.analyzer.data.tag;

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







public abstract class Tag implements Comparable<Tag>{

	private String name;
	
	public Tag(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	@Override
	public int hashCode(){
		return getTypeName().hashCode() + this.name.hashCode();
	}
	

	@Override
	public boolean equals(Object obj){
		if (!(obj instanceof Tag))
			return false;
		
		Tag tag = (Tag) obj;
		
		return this.name.equals(tag.getName()) && this.getTypeName().equals(tag.getTypeName());
	}
	
	public abstract String getTypeName();
	
	
	@Override
	public int compareTo(Tag t) {
		return getName().compareTo(t.getName());
	}	
}
