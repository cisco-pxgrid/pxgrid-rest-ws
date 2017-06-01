package com.cisco.pxgrid.samples.ise.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import com.cisco.pxgrid.model.AccountState;
import com.cisco.pxgrid.model.Service;

/**
 * Demonstrates how to download all identity groups for active sessions from ISE
 */
public class HttpsIdentityGroupDownload {
	
	private static void downloadIdentityGroups(SampleConfiguration config) throws IOException {
		PxgridControl pxgrid = new PxgridControl(config);
		Service[] services = pxgrid.lookupService("com.cisco.ise.session");
		if (services == null || services.length == 0)
			System.out.println("Service unavailabe");
		
		for (Service service : services) {
			String url = service.getProperties().get("restBaseUrl") + "/getUserGroups";
			Console.log("REST URL is " + url);
			
			String secret = pxgrid.getAccessSecret(service.getNodeName());
			
	    	HttpsURLConnection httpsConn = SampleHelper.createHttpsURLConnection(
	    			url, config.getUserName(), secret, config.getSSLContext().getSocketFactory());
	    	httpsConn.setRequestMethod("POST");
	    	httpsConn.setRequestProperty("Content-Type", "application/json");
	    	httpsConn.setRequestProperty("Accept", "application/json");
	    	httpsConn.setDoInput(true);
	    	httpsConn.setDoOutput(false);
	    	
	    	SampleHelper.printInputStream(httpsConn);
		}
	}

	public static void main(String [] args) throws Exception {
		SampleConfiguration config = new SampleConfiguration();
		PxgridControl control = new PxgridControl(config);

		while (control.accountActivate() != AccountState.ENABLED)
			Thread.sleep(60000);
		System.out.println("pxGrid controller version=" + control.getControllerVersion());

		downloadIdentityGroups(config);
	}
}
