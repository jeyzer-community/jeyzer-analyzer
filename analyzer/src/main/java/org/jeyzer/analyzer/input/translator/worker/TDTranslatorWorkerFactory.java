package org.jeyzer.analyzer.input.translator.worker;

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




import java.util.concurrent.ThreadFactory;

public class TDTranslatorWorkerFactory implements ThreadFactory{

	// guarded by class instance
	private static int nextId = 0;
	
	private static int getNextId() {
		synchronized (TDTranslatorWorkerFactory.class) {
			return ++nextId;
		}
	}	
	
	private String name;
	
	public TDTranslatorWorkerFactory(String name) {
		this.name = name;
	}
	
	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r);
		t.setName("TD " + name + " translator #" + getNextId());
		return t;
	}
}
