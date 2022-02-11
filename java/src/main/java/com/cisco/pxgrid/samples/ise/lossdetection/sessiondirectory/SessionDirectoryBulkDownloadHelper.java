package com.cisco.pxgrid.samples.ise.lossdetection.sessiondirectory;

import java.io.IOException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.pxgrid.samples.ise.PxgridControl;
import com.cisco.pxgrid.samples.ise.SampleConfiguration;
import com.cisco.pxgrid.samples.ise.lossdetection.sessiondirectory.SessionDirectoryRESTDownload;
import com.cisco.pxgrid.samples.ise.model.Service;

public class SessionDirectoryBulkDownloadHelper {

	private static Logger logger = LoggerFactory.getLogger(SessionDirectoryBulkDownloadHelper.class);
	private HttpsURLConnection httpsConn;
	private String url;
	private String secret;
	private static SessionDirectoryBulkDownloadHelper blkDwnldHelper = null;
	private SampleConfiguration config;
	private OffsetDateTime timestamp;
	Thread blkDwnldThread;

	
	public void setTimestamp(OffsetDateTime timestamp) {
		this.timestamp = timestamp;
	}
	public void setConfig(SampleConfiguration config) {
		this.config = config;
	}
	
	private SessionDirectoryBulkDownloadHelper() {
		
	}
	
	private void assignUrlSecret() {
		PxgridControl https = new PxgridControl(config);

		// pxGrid ServiceLookup for session service
		Service[] services;
		try {
			services = https.serviceLookup("com.cisco.ise.session");
			if (services == null || services.length == 0) {
				logger.warn("Service unavailabe");
				return;
			}

			// Use first service
			Service service = services[0];
			url = service.getProperties().get("restBaseUrl") + "/getSessions";
			logger.info("url={}", url);
			secret = https.getAccessSecret(service.getNodeName());
		} catch (IOException e) {
			logger.error("Exception while fetching access secret ", e);
		}
	}
	
	private SessionDirectoryRESTDownload blkDwnld;
	
	public static SessionDirectoryBulkDownloadHelper getInstance() {
		if (blkDwnldHelper == null) {
			blkDwnldHelper = new SessionDirectoryBulkDownloadHelper();
		}
		return blkDwnldHelper;
	}
	
	
	private static HttpsURLConnection createHttpsURLConnection(String url, String user, String password,
			SSLSocketFactory sslSocketFactory) throws IOException {
		URL conn = new URL(url);
		HttpsURLConnection https = (HttpsURLConnection) conn.openConnection();
		https.setDoOutput(true);
		https.setSSLSocketFactory(sslSocketFactory);
		String userPassword = user + ":" + password;
		String encoded = Base64.getEncoder().encodeToString(userPassword.getBytes());
		https.setRequestProperty("Authorization", "Basic " + encoded);
		https.setRequestProperty("Content-Type", "application/json");
		return https;
	} 
	
	public void bulkDownloadData() {

		// While canceling existing bulk download and starting a new 
		// download, the time stamp should be of the the bulk download that was
		// in progress
		if (blkDwnldThread != null && blkDwnldThread.isAlive()) {
			blkDwnldThread.interrupt();
			try {
				blkDwnldThread.join();
				httpsConn.disconnect();
			} catch (InterruptedException e) {
				logger.error("Interrupted Exception = ", e);
			}
		}
		assignUrlSecret();
		try {
			httpsConn = createHttpsURLConnection(url, config.getNodeName(), secret, config.getSSLContext().getSocketFactory());
			blkDwnldThread = new Thread(new SessionDirectoryRESTDownload(httpsConn, timestamp));
			blkDwnldThread.start();
		} catch (IOException e) {
			logger.error("Exception during bulkdownload ", e);
		}
	}
}
