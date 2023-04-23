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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.monitor.config.publisher.jira.ConfigJiraPublisher;
import static org.jeyzer.monitor.publisher.jira.JiraCreationRequest.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.rest.client.ApiClient;
import com.atlassian.jira.rest.client.ApiException;
import com.atlassian.jira.rest.client.api.IssueCommentsApi;
import com.atlassian.jira.rest.client.api.IssuePrioritiesApi;
import com.atlassian.jira.rest.client.api.IssuesApi;
import com.atlassian.jira.rest.client.api.MyselfApi;
import com.atlassian.jira.rest.client.api.ProjectComponentsApi;
import com.atlassian.jira.rest.client.api.ProjectVersionsApi;
import com.atlassian.jira.rest.client.api.ProjectsApi;
import com.atlassian.jira.rest.client.api.UserSearchApi;
import com.atlassian.jira.rest.client.model.Component;
import com.atlassian.jira.rest.client.model.CreatedIssue;
import com.atlassian.jira.rest.client.model.IssueTypeDetails;
import com.atlassian.jira.rest.client.model.PageBeanProject;
import com.atlassian.jira.rest.client.model.Priority;
import com.atlassian.jira.rest.client.model.Project;
import com.atlassian.jira.rest.client.model.User;
import com.atlassian.jira.rest.client.model.Version;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * JIRA REST API v3 doc is available at :
 * https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issue-attachments/
 */
public class JiraClient {
	
	public static final Logger logger = LoggerFactory.getLogger(JiraClient.class);

	private ConfigJiraPublisher publisherCfg;
	
	public JiraClient(ConfigJiraPublisher publisherCfg) {
		this.publisherCfg = publisherCfg;
	}

	public void attachItem(JiraAttachRequest request) {	
		// Use the Okhttp instead of the JIRA library		
	    final String address = buildAttachURL(request);
	    final OkHttpClient okHttpClient = new OkHttpClient();
	    final RequestBody formBody = new MultipartBody.Builder()
	            .setType(MultipartBody.FORM)
	            .addFormDataPart(
	            		"file", 
	            		request.getAttachment().getName(), 
	            		RequestBody.create(MediaType.parse("text/plain"), 
	            				request.getAttachment()
	            				)
	            		)
	            .build();
	    final Request restRequest = new Request.Builder().url(address).post(formBody)
	            .addHeader("X-Atlassian-Token", "no-check")
	            .addHeader("Authorization", "Basic " + this.publisherCfg.getSetupCfg().getAuthKey())
	            .build();
	    
	    Response response = null;
	    try {
			response = okHttpClient.newCall(restRequest).execute();
			if (response.code() != 200) {
				logger.error("Failed to attach to the JIRA item " + request.getTicketId() + " the doc " + request.getAttachment().getAbsolutePath());
				logger.error("JIRA server error is " + response.body().toString());
			}
		} catch (IOException ex) {
			logger.error("Failed to attach to the JIRA item " + request.getTicketId() + " the doc " + request.getAttachment().getAbsolutePath() + " due to IO error.", ex);
		}
	    finally {
	    	if (response != null)
	    		try { response.close(); } catch(Exception ex) {}
	    }
	}

	public void commentItem(JiraCommentRequest request) {
		
	    ApiClient apiClient = new ApiClient();
	    apiClient.addDefaultHeader("Authorization", "Basic " + this.publisherCfg.getSetupCfg().getAuthKey());
	    apiClient.addDefaultHeader("Content-Type", "application/json");	    
	    apiClient.setDebugging(this.publisherCfg.getSetupCfg().isDebugEnabled());
	    apiClient.setBasePath(this.publisherCfg.getSetupCfg().getServerUrl());
		
	    IssueCommentsApi commentsApi = new IssueCommentsApi(apiClient);
        Map<String, Object> issueCommentsParams = new HashMap<>();
	    
        Map<String, Object> descField = new HashMap<>();
        descField.put("type", "doc");
        descField.put("version", 1);
        
        Object[] textObjects = new Object[1];
        Map<String, Object> textContentField = new HashMap<>();
        textContentField.put("type", "text");
        textContentField.put("text", request.getComment());
        textObjects[0] = textContentField;
        
        Map<String, Object> paragraphContentField = new HashMap<>();
        paragraphContentField.put("type", "paragraph");
        paragraphContentField.put("content", textObjects);
        Object[] paragraphObjects = new Object[1];
        paragraphObjects[0] = paragraphContentField;
        
        descField.put("content", paragraphObjects);
        
        issueCommentsParams.put("body", descField);
        
	    try {
			commentsApi.addComment(issueCommentsParams, request.getTicketId(), "");
		} catch (ApiException ex) {
			logger.error("Failed to add a comment to the JIRA item " + request.getTicketId(), ex);
		}
	}

	public String createItem(JiraCreationRequest request) {
	    ApiClient apiClient = new ApiClient();
	    apiClient.addDefaultHeader("Authorization", "Basic " + this.publisherCfg.getSetupCfg().getAuthKey());
	    apiClient.addDefaultHeader("Content-Type", "application/json");
	    apiClient.setDebugging(this.publisherCfg.getSetupCfg().isDebugEnabled());
	    apiClient.setBasePath(this.publisherCfg.getSetupCfg().getServerUrl());

	    try {
		    String projectName = this.publisherCfg.getTargets().get(ConfigJiraPublisher.JEYZER_MONITOR_JIRA_TARGET_PROJECT);
	        Project project = accessJiraProject(projectName, apiClient, request);
	        
	        Map<String, Object> fields = new HashMap<>();
	        
	        Map<String, Object> projectField = new HashMap<>();
	        projectField.put("id", project.getId());
	        fields.put(JIRA_PROJECT_FIELD, projectField);
	        
	        String issueTypeId = findIssueType(project, request);
	        Map<String, Object> issueTypeField = new HashMap<>();
	        issueTypeField.put("id", issueTypeId);
	        fields.put(JIRA_ISSUE_TYPE_FIELD, issueTypeField);
	        
	        fields.put(JIRA_SUMMARY_FIELD, request.getSummary());
	        
	        fields.put(JIRA_DESCRIPTION_FIELD, buildParagraphField(request.getDescription()));

	        String accountId = findAccoundId(apiClient, request, project);
	        Map<String, Object> assigneeField = new HashMap<>();
	        assigneeField.put("accountId", accountId);
	        fields.put(JIRA_ASSIGNEE_FIELD, assigneeField);

	        String componentId = accessComponent(project, request, apiClient);
	        if (componentId != null) {
	        	Object[] componentObjects = buildIdArrayField(componentId);
		        fields.put(JIRA_COMPONENTS_FIELD, componentObjects);
	        }
	        
	        String versionId = accessVersion(project, request, apiClient);
	        if (versionId != null) {
	        	Object[] versionObjects = buildIdArrayField(versionId);
		        fields.put(JIRA_VERSIONS_FIELD, versionObjects);
	        }
	        
	        if (request.hasEnvironment())
		        fields.put(JIRA_ENVIRONMENT_FIELD, buildParagraphField(request.getEnvironment()));
	        
	        if (request.hasPriorityPublished()) {
		        String priority = publisherCfg.getPriority(request.getEvent().getLevel().getCapital() + request.getEvent().getSubLevel().toString());
		        if (priority != null) {
		        	String priorityId = accessPriorityId(project, priority, request, apiClient);
		        	if (priorityId!= null) {
				        Map<String, Object> priorityField = new HashMap<>();
				        priorityField.put("id", priorityId);
				        fields.put(JIRA_PRIORITY_FIELD, priorityField);		        		
		        	}
		        }
	        }
	        
	        Map<String, Object> issueCreateParams = new HashMap<>();
	        issueCreateParams.put("fields", fields);

	        IssuesApi issuesApi = new IssuesApi(apiClient);
	        CreatedIssue createdIssue;
			try {
				createdIssue = issuesApi.createIssue(issueCreateParams, true);
			} catch (ApiException ex) {
				logger.error("Failed to create the JIRA item for the event : " + request.getEvent().getName(), ex);
				return null;
			}
			logger.info("JIRA item created : " + createdIssue.getKey() + "  for the event " + request.getEvent().getName());
		    
			return createdIssue.getKey();
			
	    } catch (JiraClientException ex) {
	    	if (ex.getCause() != null)
	    		logger.error(ex.getMessage(), ex.getCause());
	    	else
	    		logger.error(ex.getMessage());
	    	return null;
    	} catch (Exception ex) {
    		logger.error("Failed to create JIRA item due to unexpected error for the event : " + request.getEvent().getName(), ex);
    		return null;
    	}
	}

	private String accessPriorityId(Project project, String priorityName, JiraCreationRequest request, ApiClient apiClient) throws JiraClientException {
        IssuePrioritiesApi prioritiesApi = new IssuePrioritiesApi(apiClient);
        List<Priority> priorities;
		try {
			priorities = prioritiesApi.getPriorities();
		} catch (ApiException ex) {
			throw new JiraClientException(request, "Priority access error", ex);
		}

        for (Priority priority : priorities) {
            if (((String)priority.get("name")).equalsIgnoreCase(priorityName)) 
            	return (String)priority.get("id");
        }
            
         logger.warn("JIRA priority " + priorityName + " not found on the Jira project " + project.getName());
         return null;
	}

	private String accessVersion(Project project, JiraCreationRequest request, ApiClient apiClient) {
        if (!request.hasVersion())
        	return null;
		
        ProjectVersionsApi versionsApi = new ProjectVersionsApi(apiClient);
        List<Version> versions;
		try {
			versions = versionsApi.getProjectVersions(project.getKey(), null);
		} catch (ApiException ex) {
            logger.warn("JIRA version access failed on the Jira project " + project.getName(), ex);
            return null;
		}
        for (Version version : versions) {
            if (version.getName().equalsIgnoreCase(request.getVersion()))
            	return version.getId();
        }
        
        logger.warn("JIRA version " + request.getVersion() + " not found on the Jira project " + project.getName());
        return null;
	}

	private Object[] buildIdArrayField(String idValue) {
        Object[] objects = new Object[1];
        Map<String, Object> idField = new HashMap<>();
        idField.put("id", idValue);
        objects[0] = idField;
		return objects;
	}

	private String accessComponent(Project project, JiraCreationRequest request, ApiClient apiClient) {
		if (!request.hasComponent())
			return null;
		
        ProjectComponentsApi componentApi = new ProjectComponentsApi(apiClient);
        List<Component> components;
		try {
			components = componentApi.getProjectComponents(project.getKey());
		} catch (ApiException ex) {
            logger.warn("JIRA component " + request.getComponent() + " access failed on the Jira project " + project.getName(), ex);
            return null;
		}
        for (Component issueComponent : components) {
            if (issueComponent.getName().equalsIgnoreCase(request.getComponent()))
            	return issueComponent.getId();
        }

        logger.warn("JIRA component " + request.getComponent() + " not found on the Jira project " + project.getName());
		return null;
	}

	private String findIssueType(Project project, JiraCreationRequest request) throws JiraClientException {
        String issueTypeId = null;
        
        String issueType = this.publisherCfg.getTargets().get(ConfigJiraPublisher.JEYZER_MONITOR_JIRA_TARGET_ISSUE_TYPE);
        for (IssueTypeDetails issueTypeDetails : project.getIssueTypes()) {
            if (issueTypeDetails.getName().equalsIgnoreCase(issueType)) {
                issueTypeId = issueTypeDetails.getId();
                break;
            }
        }
        if (issueTypeId == null)
        	throw new JiraClientException(request, "Issue type " + issueType + " not found on project : " + project);

        return issueTypeId;
	}

	private String findAccoundId(ApiClient apiClient, JiraCreationRequest request, Project project) throws JiraClientException {
		if (request.hasAssigneeAccountId())
			return request.getAssignee();
					
    	User user;
    	if (request.hasAssignee())
    		user = accessUser(apiClient, request, project);
    	else
    		user = accessCurrentUser(apiClient, request);
    	
		return user.getAccountId();
	}
	
	private User accessCurrentUser(ApiClient apiClient, JiraCreationRequest request) throws JiraClientException {
	    MyselfApi myselfApi = new MyselfApi(apiClient);
		try {
			return myselfApi.getCurrentUser(null);
		} catch (ApiException ex) {
			throw new JiraClientException(request, "Current user access error", ex);
		}
	}

	private User accessUser(ApiClient apiClient, JiraCreationRequest request, Project project) throws JiraClientException {
        UserSearchApi searchApi = new UserSearchApi(apiClient);
        List<User> users = null;
        try {     	
	        users = searchApi.findBulkAssignableUsers(
	        		project.getKey(), // projectKeys 
	        		request.getAssignee(), // query
	        		null, // username - deprecated 
	        		null, //accountId 
	        		null, //startAt
	        		null // maxResults
	        		);	        	
        } catch(ApiException ex) {
			throw new JiraClientException(request, "User access error using user email " + request.getAssignee(), ex);
        }
        
        if (users.size() > 1)
        	throw new JiraClientException(request, "User access returned more than one user for the user email " + request.getAssignee());
        
        if (users.isEmpty())
        	throw new JiraClientException(request, "User access returned no user for the user email " + request.getAssignee());
        
		return users.get(0);
	}

	private Map<String, Object> buildParagraphField(String text) {
        Map<String, Object> paragraphField = new HashMap<>();
        paragraphField.put("type", "doc");
        paragraphField.put("version", 1);
        
        Object[] textObjects = new Object[1];
        Map<String, Object> textContentField = new HashMap<>();
        textContentField.put("type", "text");
        textContentField.put("text", text);
        textObjects[0] = textContentField;
        
        Map<String, Object> paragraphContentField = new HashMap<>();
        paragraphContentField.put("type", "paragraph");
        paragraphContentField.put("content", textObjects);
        Object[] paragraphObjects = new Object[1];
        paragraphObjects[0] = paragraphContentField;
        
        paragraphField.put("content", paragraphObjects);
        
        return paragraphField;
	}

	private Project accessJiraProject(String projectName, ApiClient apiClient, JiraCreationRequest request) throws JiraClientException {
	    ProjectsApi projectsApi = new ProjectsApi(apiClient);
	    PageBeanProject projects;
		try {
			projects = projectsApi.searchProjects(
			    null, // startAt
			    null, // maxResults
			    null, // orderBy
			    projectName, // query
			    null,  // typeKey
			    null,  // categoryId
			    null,  // searchBy
			    null, // action
			    "issueTypes", // expand issue types
			    null  // status
			    );
		} catch (ApiException ex) {
			throw new JiraClientException(request, "Project API search error", ex);
		}
	            
		if (projects.getValues().isEmpty())
			throw new JiraClientException(request, "JIRA project " + projectName + " not found");
		
        return projects.getValues().get(0);
	}
	
	private String buildAttachURL(JiraAttachRequest request) {
		// URL example : https://fjoubjira.atlassian.net/rest/api/2/issue/JT-1/attachments
		
		StringBuilder url = new StringBuilder();
		url.append(this.publisherCfg.getSetupCfg().getServerUrl());
		url.append("/rest/api/");
		url.append(this.publisherCfg.getSetupCfg().getCloudAPIVersion());
		url.append("/issue/");
		url.append(request.getTicketId());
		url.append("/attachments");
		return url.toString();
	}
}
