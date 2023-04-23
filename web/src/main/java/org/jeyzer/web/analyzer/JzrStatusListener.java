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


import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.server.Command;

import org.jeyzer.analyzer.status.JeyzerStatusListener;

import com.vaadin.flow.component.UI;

public class JzrStatusListener implements JeyzerStatusListener{

	private transient ProgressBar progressBar;
	private transient Label progressStatus;
	private UI ui;
	
	public JzrStatusListener(ProgressBar progressBar, Label progressStatus){
		this.progressBar = progressBar;
		this.progressStatus = progressStatus;
	}
	
	public void setUI(UI ui) {
		this.ui = ui;
	}
	
	@Override
	public void updateStatus(final String status, final float progress) {
		ui.access(new Command() {
			
			private static final long serialVersionUID = -4761891226177996498L;

			@Override
			public void execute() {
				if (progress > progressBar.getMax())
					progressBar.setValue(progressBar.getMax());
				else
					progressBar.setValue(progress);
            	progressStatus.setText(status);
			}
		});
	}
	
}
