package com.cisco.pxgrid.samples.ise;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.pxgrid.samples.ise.SampleHelper.OffsetDateTimeAdapter;
import com.cisco.pxgrid.samples.ise.model.AccountState;
import com.cisco.pxgrid.samples.ise.model.Service;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

/**
 * Demonstrates how to query all sessions and stream process for ISE Session Directory service
 */
public class SessionQueryAll {
	private static Logger logger = LoggerFactory.getLogger(SessionQueryAll.class);
	
	private static class SessionQueryRequest {
		OffsetDateTime startTimestamp;
	}

	private static void query(SampleConfiguration config) throws Exception {
		OffsetDateTime startTimestamp = SampleHelper.promptDate("Enter start time (ex. '2015-01-31T13:00:00-07:00' or <enter> for no start time): ");
		
		PxgridControl pxgrid = new PxgridControl(config);
		
		// pxGrid ServiceLookup for session service
		Service[] services = pxgrid.serviceLookup("com.cisco.ise.session");
		if (services == null || services.length == 0) {
			logger.warn("Service unavailabe");
			return;
		}
		
		// Use first service
		Service service = services[0];
		String url = service.getProperties().get("restBaseUrl") + "/getSessions";
		logger.info("url={}", url);
		
		// pxGrid AccesssSecret for the node
		String secret = pxgrid.getAccessSecret(service.getNodeName());

		SessionQueryRequest request = new SessionQueryRequest();
		request.startTimestamp = startTimestamp;
		
		HttpsURLConnection https = SampleHelper.createHttpsURLConnection(url, config.getNodeName(), secret, config.getSSLContext().getSocketFactory());
		postAndStreamPrint(https, request);
	}
	
	public static void postAndStreamPrint(HttpsURLConnection httpsConn, Object postObject) throws IOException {
		Gson gson = new GsonBuilder().registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter()).create();
		httpsConn.setRequestMethod("POST");
		httpsConn.setRequestProperty("Content-Type", "application/json");
		httpsConn.setRequestProperty("Accept", "application/json");
		httpsConn.setDoInput(true);
		httpsConn.setDoOutput(true);
		OutputStreamWriter osw = new OutputStreamWriter(httpsConn.getOutputStream());
		osw.write(gson.toJson(postObject));
		osw.flush();

		int status = httpsConn.getResponseCode();
		logger.info("Response status={}", status);
		
		if (status < HttpURLConnection.HTTP_BAD_REQUEST) {
			try (InputStream in = httpsConn.getInputStream()) {
				JsonReader jreader = new JsonReader(new InputStreamReader(in));
				jreader.beginObject();
				String name = jreader.nextName();
				if ("sessions".equals(name)) {
					int count = 0;
					jreader.beginArray();
					while (jreader.hasNext()) {
						Session session = gson.fromJson(jreader, Session.class);
						System.out.println("session=" + session);
						count++;
					}
					System.out.println("count=" + count);
				}
			}
		} else {
			try (InputStream in = httpsConn.getErrorStream()) {
				String content = IOUtils.toString(in, StandardCharsets.UTF_8);
				System.out.println("Content: " + content);
			}
		}
	}

	public static void main(String [] args) throws Exception {
		// Parse arguments
		SampleConfiguration config = new SampleConfiguration();
		try {
			config.parse(args);
		} catch (ParseException e) {
			config.printHelp("SessionQueryAll");
			System.exit(1);
		}

		// AccountActivate
		PxgridControl control = new PxgridControl(config);
		while (control.accountActivate() != AccountState.ENABLED)
			Thread.sleep(60000);
		logger.info("pxGrid controller version={}", control.getControllerVersion());

		query(config);
	}
}
