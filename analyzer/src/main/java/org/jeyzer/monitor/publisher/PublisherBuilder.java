package org.jeyzer.monitor.publisher;

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



import java.util.ArrayList;
import java.util.List;

import org.jeyzer.monitor.JeyzerMonitor;
import org.jeyzer.monitor.config.ConfigMonitor;
import org.jeyzer.monitor.config.publisher.ConfigMailPublisher;
import org.jeyzer.monitor.config.publisher.ConfigPublisher;
import org.jeyzer.monitor.config.publisher.ConfigSoundPublisher;
import org.jeyzer.monitor.config.publisher.ConfigWebPublisher;
import org.jeyzer.monitor.config.publisher.jira.ConfigJiraPublisher;
import org.jeyzer.monitor.publisher.jira.JiraPublisher;


public class PublisherBuilder {
	
	private static final PublisherBuilder builder = new PublisherBuilder();
	
	private PublisherBuilder(){}
	
	public static PublisherBuilder newInstance(){
		return builder;
	}
	
	public List<Publisher> buildPublishers(ConfigMonitor conf){
		List<Publisher> publishers = new ArrayList<>();
		
		@SuppressWarnings("unchecked")
		List<ConfigPublisher> publisherCfgs = (List<ConfigPublisher>)conf.getValue(ConfigMonitor.JZRM_PUBLISHERS);
		
		for (ConfigPublisher publisherCfg : publisherCfgs){
			Publisher publisher;
			
			if (ConfigMailPublisher.NAME.equals(publisherCfg.getName())){
				publisher = new MailPublisher((ConfigMailPublisher)publisherCfg);
				publishers.add(publisher);
			}
			else if (ConfigSoundPublisher.NAME.equals(publisherCfg.getName())){
				publisher = new SoundPublisher((ConfigSoundPublisher)publisherCfg);
				publishers.add(publisher);
			}			
			else if (ConfigWebPublisher.NAME.equals(publisherCfg.getName())){
				publisher = new WebPublisher((ConfigWebPublisher)publisherCfg);
				publishers.add(publisher);
			}
			else if (ConfigJiraPublisher.NAME.equals(publisherCfg.getName())){
				publisher = new JiraPublisher((ConfigJiraPublisher)publisherCfg);
				publishers.add(publisher);
			}
			else{
				JeyzerMonitor.logger.error("Publisher not found for format : " + publisherCfg.getName());
				System.exit(-1);
			}
		}		
		
		return publishers;
	}
	
}
