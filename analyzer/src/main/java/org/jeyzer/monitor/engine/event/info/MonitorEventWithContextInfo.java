package org.jeyzer.monitor.engine.event.info;

import java.util.Date;
import java.util.Map;

public class MonitorEventWithContextInfo extends MonitorEventInfo{

	private Map<String, Object> context;
	
	public MonitorEventWithContextInfo(String id, String ref, Scope scope, Level level, SubLevel subLevel, Date start,
			Date end, String message, String ticket, Map<String, Object> context) {
		super(id, ref, scope, level, subLevel, start, end, message, ticket);
		this.context = context;
	}
	
	public Map<String, Object> getContext(){
		return this.context;
	}

}
