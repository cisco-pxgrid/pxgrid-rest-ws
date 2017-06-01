package com.cisco.pxgrid.samples.ise.http;

import java.io.IOException;

import com.cisco.pxgrid.model.AccountState;
import com.cisco.pxgrid.model.Service;

/**
 * Demonstrates how to query a session using IP address
 */
public class HttpsSessionQueryByIP {
	
	private static void query(SampleConfiguration config, String ip) throws IOException {
		PxgridControl pxgrid = new PxgridControl(config);
		Service[] services = pxgrid.lookupService("com.cisco.ise.session");
		if (services == null || services.length == 0) {
			System.out.println("Service unavailabe");
			return;
		}
		Service service = services[0];
		// TODO change method
		String url = service.getProperties().get("restBaseUrl") + "/getSessionByIpAddress";
		Console.log("REST URL is " + url);
		String secret = pxgrid.getAccessSecret(service.getNodeName());
		String postData = "{\"ipAddress\":\"" + ip + "\"}";
		SampleHelper.postStringAndPrint(url, config.getUserName(), secret, config.getSSLContext().getSocketFactory(), postData);
	}
	

	public static void main(String [] args) throws Exception {
		SampleConfiguration config = new SampleConfiguration();
		PxgridControl pxgrid = new PxgridControl(config);
		
		while (pxgrid.accountActivate() != AccountState.ENABLED)
			Thread.sleep(60000);
		Console.log("pxGrid controller version=" + pxgrid.getControllerVersion());

		while (true) {
			String ip = SampleHelper.prompt("IP address (or <enter> to disconnect): ");
			if (ip == null)	
				break;

			query(config, ip);
		}
	}
}
