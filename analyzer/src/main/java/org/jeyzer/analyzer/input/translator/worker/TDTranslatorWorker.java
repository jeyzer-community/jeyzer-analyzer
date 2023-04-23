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




import java.io.File;

public class TDTranslatorWorker implements Runnable{

	private TDTranslator translator;
	private File file;
	
	public TDTranslatorWorker(TDTranslator translator, File file){
		this.translator = translator;
		this.file = file;
	}
	
	@Override
	public void run() {
		translator.translateTDFile(file);
	}

}
