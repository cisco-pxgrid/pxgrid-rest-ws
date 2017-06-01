package com.cisco.pxgrid.samples.ise.http;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.net.ssl.HttpsURLConnection;

import com.cisco.pxgrid.model.AccountState;
import com.cisco.pxgrid.model.Service;

/**
 * Demonstrates how to query a session using IP address
 */
public class HttpsIdentityGroupQueryByUser {

	private static void getIdentityGroupByUser(SampleConfiguration config, String userName) throws IOException {
		PxgridControl pxgrid = new PxgridControl(config);
		Service[] services = pxgrid.lookupService("com.cisco.ise.session");
		if (services == null || services.length == 0)
			System.out.println("Service unavailabe");
		
		for (Service service : services) {
			String url = service.getProperties().get("restBaseUrl") + "/getUserGroupByUserName";
			Console.log("REST URL is " + url);
			
			String secret = pxgrid.getAccessSecret(service.getNodeName());
			
	    	HttpsURLConnection httpsConn = SampleHelper.createHttpsURLConnection(
	    			url, config.getUserName(), secret, config.getSSLContext().getSocketFactory());
	    	httpsConn.setRequestMethod("POST");
	    	httpsConn.setRequestProperty("user", config.getUserName());
	    	httpsConn.setRequestProperty("Content-Type", "application/json");
	    	httpsConn.setRequestProperty("Accept", "application/json");
	    	httpsConn.setDoInput(true);
	    	httpsConn.setDoOutput(true);
	    	
			OutputStreamWriter osw = new OutputStreamWriter(httpsConn.getOutputStream());
			osw.write("{\"userName\":\"" + userName + "\"}");
			osw.flush();
			
	        SampleHelper.printInputStream(httpsConn);
		}
	}

	public static void main(String [] args) throws Exception {
		SampleConfiguration config = new SampleConfiguration();
		PxgridControl pxgrid = new PxgridControl(config);
		
		while (pxgrid.accountActivate() != AccountState.ENABLED)
			Thread.sleep(60000);
		System.out.println("pxGrid controller version=" + pxgrid.getControllerVersion());

		while (true) {
			String userName = SampleHelper.prompt("User name (or <enter> to disconnect): ");
			if (userName == null)
				break;
			
			getIdentityGroupByUser(config, userName);
		}
	}
}
