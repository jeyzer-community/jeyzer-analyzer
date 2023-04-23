package org.jeyzer.profile.master;

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



public class JzrProfileSecurity {
	
	private SecMode mode;
	
	public JzrProfileSecurity(SecMode mode){
		this.mode= mode;
	}

	public boolean isPasswordMandatory(){
		return SecMode.EXTERNAL_MANDATORY.equals(mode);
	}

	public boolean isPasswordOptional(){
		return SecMode.EXTERNAL_OPTIONAL.equals(mode);
	}
	
	public boolean isPasswordFree(){
		return !isPasswordOptional() && !isPasswordMandatory();
	}
	
}
