package com.cisco.pxgrid.samples.ise.http;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import com.cisco.pxgrid.model.AccountState;
import com.cisco.pxgrid.model.Service;

public class SystemPerformanceQuery {
	private static void getPerformances(SampleConfiguration config) throws IOException, XMLStreamException {
		PxgridControl https = new PxgridControl(config);
		Service[] services = https.lookupService("com.cisco.ise.system");
		if (services == null || services.length == 0) {
			Console.log("Service unavailabe");
			return;
		}
		
		String url = services[0].getProperties().get("restBaseUrl") + "/getPerformances";
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
		
		getPerformances(config);
	}
}
