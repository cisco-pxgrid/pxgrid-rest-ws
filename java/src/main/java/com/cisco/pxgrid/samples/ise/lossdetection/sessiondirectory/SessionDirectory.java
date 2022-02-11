package com.cisco.pxgrid.samples.ise.lossdetection.sessiondirectory;

import java.time.OffsetDateTime;

import org.json.simple.JSONObject;

public class SessionDirectory {
	private String mac;
	private String userName;
	private String state;
	private String sessionId;
	private OffsetDateTime timestamp;
	
	public String getMac() {
		return mac;
	}

	public OffsetDateTime getTimestamp() {
		return timestamp;
	}
	
	@Override
	public String toString() {
		return "SessionDirectory [mac=" + mac + ", userName=" + userName + ", state=" + state + ", sessionId="
				+ sessionId + ", timestamp=" + timestamp + "]";
	}

	public SessionDirectory(JSONObject session) {
		String time =  (String)session.get("timestamp");
		this.timestamp = OffsetDateTime.parse(time);
		this.mac = (String)session.get("callingStationId");
		this.userName = (String)session.get("userName");
		this.state = (String)session.get("state");
		this.sessionId = (String)session.get("auditSessionId");
	}
	
	public OffsetDateTime getTimeStamp() {
		return this.timestamp;
	}
	

	
}
