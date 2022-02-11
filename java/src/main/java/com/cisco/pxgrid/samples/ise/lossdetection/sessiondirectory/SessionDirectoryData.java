package com.cisco.pxgrid.samples.ise.lossdetection.sessiondirectory;

import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionDirectoryData {
	private static Logger logger = LoggerFactory.getLogger(SessionDirectoryData.class);
	private static Map<String, SessionDirectory> sessionMap = new LinkedHashMap<>();
	private static OffsetDateTime timestamp  = OffsetDateTime.MIN;;
	
	public static OffsetDateTime getTimestamp() {
		return timestamp;
	}

	// Merge the new entry into the database. Retain the entry with latest
	// time stamp
	public static synchronized void mergeEntry(SessionDirectory session) {
		SessionDirectory dbSession = sessionMap.get(session.getMac());
		if (dbSession != null) {
			if (dbSession.getTimestamp().compareTo(session.getTimestamp()) < 0) {
				logger.info("Incoming msg is later than existing one in DB. Updating");
				sessionMap.put(session.getMac(), session);
			} else {
				logger.info("New msg is older than the existing one in DB. Ignoring");
			}
		} else {
			logger.info("New entry. Add to DB");
			sessionMap.put(session.getMac(), session);
		}
	}
	
	
	// Update the Rx time with the time stamp in the incoming message
	public static synchronized void updateRxTime(OffsetDateTime timeStamp) {
		if(timestamp.compareTo(timeStamp) < 0) {
			timestamp = timeStamp;
		}
	}
	
 	public static synchronized void printSessionDirectoryData() {
 		Iterator<?> sdIterator = sessionMap.entrySet().iterator();
 		int i=0;
		while(sdIterator.hasNext()) {
			Entry<String, SessionDirectory> entry = (Entry<String, SessionDirectory>) sdIterator.next();
			SessionDirectory sd = entry.getValue() ;
			logger.info("Entry - {}", i++);
			logger.info(sd.toString());
		}
	}
 	
 	public static void clearCache() {
 		sessionMap = new LinkedHashMap<>();
 	}
}
