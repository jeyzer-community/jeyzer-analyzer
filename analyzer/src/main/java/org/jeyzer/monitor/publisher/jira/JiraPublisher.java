package org.jeyzer.monitor.publisher.jira;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.error.JzrMonitorException;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.publisher.jira.ConfigJiraPublisher;
import org.jeyzer.monitor.engine.event.MonitorEvent;
import org.jeyzer.monitor.publisher.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JiraPublisher implements Publisher {

	public static final Logger logger = LoggerFactory.getLogger(JiraPublisher.class);
	
	private ConfigJiraPublisher publisherCfg;
	
	public JiraPublisher(ConfigJiraPublisher publisherCfg) {
		this.publisherCfg = publisherCfg;
	}

	@Override
	public void publish(JzrMonitorSession monitorSession, List<MonitorEvent> events, Map<String, List<String>> publisherPaths) throws JzrMonitorException {
		if (!publisherCfg.isEnabled())
			return;
		
		if (!(monitorSession instanceof JzrSession))
			return; // We do not publish errors on JIRA

		// Preliminary checks
		JzrSession session = (JzrSession)monitorSession;
		checkProcessCardAvailability(session);
		checkProcessJarsAvailability(session);
		
		// Discard the events not matching the thresholds 
		//   and discard the already published ones
		List<MonitorEvent> candidates = filterEvents(events);
		if (candidates.isEmpty())
			return;
		
		// group the events to get 1 Jira item per group
		Map<String, List<MonitorEvent>> eventGroups = groupEvents(candidates);
		
		logger.info("Publishing the important monitoring events in Jira.");
		
		JiraClient client = new JiraClient(publisherCfg);
		for (List<MonitorEvent> eventsToPublish : eventGroups.values()) {
			// Process only the first event of each group
			// Note that the templates can still access the other events from the group
			MonitorEvent firstEventInGroup = eventsToPublish.get(0);
			String ticketId = firstEventInGroup.getTicket(); // can be null

			if (firstEventInGroup.hasTicket() && publisherCfg.isUpdateActionActive()) {
				logger.info("Updating the JIRA item " + ticketId + " for the event " + firstEventInGroup.getRef() + " / "+ firstEventInGroup.getName());
				
				// Jira item update - attachment
				if (publisherCfg.getUpdateActionCfg().isAttachmentEnabled())
					// attach only once the report
					publishAttachments(publisherPaths, ticketId, client);

				// Jira item update - comment
				JiraCommentRequest request = new JiraCommentRequest(
						eventsToPublish, 
						session, 
						publisherCfg.getUpdateActionCfg());
				client.commentItem(request);
			}
			else if (!firstEventInGroup.hasTicket() && publisherCfg.isCreateActionActive()) {
				// Jira item creation
				logger.info("Creating the JIRA item for the event " + firstEventInGroup.getRef() + " / "+ firstEventInGroup.getName());

				JiraCreationRequest request = new JiraCreationRequest(
						eventsToPublish, 
						session,
						publisherCfg.getCreateActionCfg());
				ticketId = client.createItem(request);
				if (ticketId == null)
					continue;
				
				// add the attachments to the Jira item
				publishAttachments(publisherPaths, ticketId, client);
			}
		}
	}

	private Map<String, List<MonitorEvent>> groupEvents(List<MonitorEvent> candidates) {
		Map<String, List<MonitorEvent>> groups = new HashMap<>();
		
		for (MonitorEvent event : candidates) {
			String eventName = event.getRef();
			List<MonitorEvent> group = groups.computeIfAbsent(eventName, p -> new ArrayList<>());
			group.add(event);
		}
		
		return groups;
	}

	private void checkProcessJarsAvailability(JzrSession session) {
		if (session.getProcessJars() == null)
			logger.warn("Process Jars info is not available : any process jars based field will be set to empty value.");
	}

	private void checkProcessCardAvailability(JzrSession session) {
		if (session.getProcessCard() == null)
			logger.warn("Process card is not available : any process card based field will be set to empty value.");		
	}

	private void publishAttachments(Map<String, List<String>> publisherPaths, String ticketId, JiraClient client) {
		logger.info("Adding attachment(s) to the JIRA item : " + ticketId);
		
		List<String> attachmentPaths = publisherPaths.get(publisherCfg.getName());
		if (attachmentPaths == null || attachmentPaths.isEmpty()) {
			logger.warn("No attachment provided : please review the Monitor configuration.");
			return;
		}
		
		for (String path : attachmentPaths) {
			logger.info("Adding the attachment : " + path);
			JiraAttachRequest request = new JiraAttachRequest(ticketId, path);
			client.attachItem(request);
		}
	}

	private List<MonitorEvent> filterEvents(List<MonitorEvent> events) {
		List<MonitorEvent> candidates = new ArrayList<>();
		for (MonitorEvent event : events) {
			// filter already published events
			if (event.isPublished())
				continue;
			
			// keep events with required level and sub level (ex: C5, C10..)
			String detailedLevel = event.getLevel().getCapital() + event.getSubLevel().toString();
			if (publisherCfg.getThresholds().contains(detailedLevel))
				candidates.add(event);
		}
		return candidates;
	}
}
