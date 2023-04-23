package org.jeyzer.service.action.id;

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



import java.util.concurrent.atomic.AtomicInteger;

/**
 * Oneshot id generator, valid for one analysis
 * (Not designed to be used in a Monitor context like the Action Id Monitor generator one)
 */
public class StackGroupActionIdGenerator {

	private final AtomicInteger idCounter = new AtomicInteger(0);	
	
	public int getStackGroupActionId() {
		return idCounter.incrementAndGet();
	}
	
}
