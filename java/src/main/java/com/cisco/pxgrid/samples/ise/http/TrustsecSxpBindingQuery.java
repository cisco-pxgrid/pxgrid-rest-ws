package com.cisco.pxgrid.samples.ise.http;

import java.io.IOException;

import com.cisco.pxgrid.model.AccountState;
import com.cisco.pxgrid.model.Service;

public class TrustsecSxpBindingQuery {
	private static void query(SampleConfiguration config) throws IOException {
		PxgridControl pxgrid = new PxgridControl(config);
		Service[] services = pxgrid.lookupService("com.cisco.ise.sxp");
		if (services == null || services.length == 0) {
			System.out.println("Service unavailabe");
			return;
		}
		Service service = services[0];
		String url = service.getProperties().get("restBaseUrl") + "/getBindings";
		Console.log("REST URL is " + url);
		String secret = pxgrid.getAccessSecret(service.getNodeName());
		SampleHelper.postObjectAndPrint(url, config.getUserName(), secret, config.getSSLContext().getSocketFactory(), "{}");
	}
	
	public static void main(String [] args) throws Exception {
		SampleConfiguration config = new SampleConfiguration();
		PxgridControl pxgrid = new PxgridControl(config);
		while (pxgrid.accountActivate() != AccountState.ENABLED) {
			Thread.sleep(60000);
		}
		Console.log("pxGrid controller version=" + pxgrid.getControllerVersion());

		query(config);
	}
}
