package com.cisco.pxgrid.samples.ise.lossdetection.sessiondirectory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SessionDirectoryJsonObjectsProcessor {
	
	public void clearcache() {
		SessionDirectoryData.clearCache();
	}

	public void processSessionObjects (JSONArray sessions) {
		for (int i=0; i<sessions.size(); i++) {
			JSONObject session = (JSONObject)sessions.get(i);
			SessionDirectory sessionDir = new SessionDirectory(session);
			SessionDirectoryData.mergeEntry(sessionDir);
			SessionDirectoryData.updateRxTime(sessionDir.getTimeStamp());
		}
		SessionDirectoryData.printSessionDirectoryData();
	}

}
