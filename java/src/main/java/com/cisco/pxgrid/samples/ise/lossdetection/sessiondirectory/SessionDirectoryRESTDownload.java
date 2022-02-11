package com.cisco.pxgrid.samples.ise.lossdetection.sessiondirectory;

import java.io.IOException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.pxgrid.samples.ise.SampleConfiguration;
import com.cisco.pxgrid.samples.ise.SampleHelper;
import com.cisco.pxgrid.samples.ise.SessionQueryAll;

public class SessionDirectoryRESTDownload implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(SessionDirectoryRESTDownload.class);
	private OffsetDateTime timestamp;
	private SampleConfiguration config;
	private HttpsURLConnection httpsConn;

	public SessionDirectoryRESTDownload(HttpsURLConnection httpsConn, OffsetDateTime timestamp) {
		this.httpsConn = httpsConn;
		this.config = config;
	}

	private static class SessionQueryRequest {
		OffsetDateTime startTimestamp;
	}

	@Override
	public void run() {

		try {
			SessionQueryRequest request = new SessionQueryRequest();
			request.startTimestamp = timestamp;
			String content = SampleHelper.getRestResponse(httpsConn, request);
			JSONParser parser = new JSONParser();
			JSONObject test;

			test = (JSONObject)parser.parse(content);
			JSONArray sessions = (JSONArray)test.get("sessions");
			SessionDirectoryJsonObjectsProcessor processor = new SessionDirectoryJsonObjectsProcessor();
			processor.processSessionObjects(sessions);
		} catch (ParseException e) {
			logger.error("Exception", e);
		} catch (IOException e1) {
			logger.error("Exception", e1);
		} finally {
			httpsConn.disconnect();
			logger.info("Close httpsconnection");
		}
	}

}
