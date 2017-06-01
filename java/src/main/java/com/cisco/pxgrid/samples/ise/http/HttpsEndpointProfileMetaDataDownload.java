package com.cisco.pxgrid.samples.ise.http;

import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import com.cisco.pxgrid.model.AccountState;
import com.cisco.pxgrid.model.Service;

/**
 * Demonstrates how to download all endpoint profile policies from ISE
 */
public class HttpsEndpointProfileMetaDataDownload {
	public static void main(String [] args) throws Exception {
		SampleConfiguration config = new SampleConfiguration();
		PxgridControl https = new PxgridControl(config);
		
		while (https.accountActivate() != AccountState.ENABLED) {
			Thread.sleep(60000);
		}
		
		System.out.println("pxGrid controller version=" + https.getControllerVersion());
		
		// Loop around in case of more than one service. 
		Service[] services = https.lookupService("com.cisco.ise.config.profiler");
		
		if (services == null || services.length == 0) {
			System.out.println("Service unavailabe");
			return;
		}
		
		Service service = services[0];
		Map<String, String> map = service.getProperties();
		String url = map.get("restBaseUrl") + "/getProfiles";
		
		System.out.println("Going to URL:" + url);
		String secret = https.getAccessSecret(service.getNodeName());
		
		System.out.println("GOT secret ...");
		
		HttpsURLConnection connection = SampleHelper.createHttpsURLConnection(url, config.getUserName(), 
				secret, config.getSSLContext().getSocketFactory());
		
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("Accept", "application/json");
		connection.setDoInput(true);
		connection.setDoOutput(true);

		SampleHelper.printInputStream(connection);
	}
}
