package com.cisco.pxgrid.samples.ise.http;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import com.cisco.pxgrid.model.AccountState;
import com.cisco.pxgrid.model.Service;


/**
 * Sample Java client program to get details for a radius authentication failure. 
 * It shows how to query health of a set of nodes and how to get health of all nodes.
 * 
 * How to run:
 * java -DPXGRID_HOSTNAMES="pxgrid-host.cisco.com" -DPXGRID_USERNAME="healthuser-1" -DPXGRID_PASSWORD="password" \
 * 		-DPXGRID_TRUSTSTORE_FILENAME="/path/to/trusstore.pem" -DPXGRID_TRUSTSTORE_PASSWORD="Cisco123" \
 * 		-DPXGRID_KEYSTORE_FILENAME="/path/to/keystore.pem" -DPXGRID_KEYSTORE_PASSWORD="Cisco123" \
 * 		 
 * @author gajvsing
 *
 */
public class RadiusFailureQuery {

	private static void downloadUsingAccessSecret(SampleConfiguration config ) throws IOException, XMLStreamException {
		PxgridControl https = new PxgridControl(config);
		Service[] services = https.lookupService("com.cisco.ise.radius");
		if (services == null || services.length == 0) {
			Console.log("Service unavailabe");
			return;
		}
		String url = services[0].getProperties().get("restBaseUrl") + "/getFailures";
		Console.log("REST URL is " + url);
		String secret = https.getAccessSecret(services[0].getNodeName());
        
		SampleHelper.postStringAndPrint(url, config.getUserName(), secret, config.getSSLContext().getSocketFactory(), "{}");

	}
	
	public static void main(String [] args) throws Exception {
		SampleConfiguration config = new SampleConfiguration();
		PxgridControl control = new PxgridControl(config);
		while (control.accountActivate() != AccountState.ENABLED) {
			Thread.sleep(60000);
		}
		Console.log("pxGrid controller version=" + control.getControllerVersion());
		
		try {
			downloadUsingAccessSecret(config);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
