package com.cisco.pxgrid.samples.ise.http;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.pxgrid.model.AccountState;
import com.cisco.pxgrid.model.Service;

/**
 * Demonstrates how to query a session using IP address
 */
public class SessionQueryByIP {
	private static Logger logger = LoggerFactory.getLogger(SessionQueryByIP.class);

	private static void query(SampleConfiguration config, String ip) throws IOException {
		PxgridControl pxgrid = new PxgridControl(config);
		Service[] services = pxgrid.lookupService("com.cisco.ise.session");
		if (services == null || services.length == 0) {
			System.out.println("Service unavailabe");
			return;
		}
		Service service = services[0];
		String url = service.getProperties().get("restBaseUrl") + "/getSessionByIpAddress";
		logger.info("url={}", url);
		String secret = pxgrid.getAccessSecret(service.getNodeName());
		String postData = "{\"ipAddress\":\"" + ip + "\"}";
		SampleHelper.postStringAndPrint(url, config.getNodeName(), secret, config.getSSLContext().getSocketFactory(), postData);
	}
	

	public static void main(String [] args) throws Exception {
		SampleConfiguration config = new SampleConfiguration();
		PxgridControl pxgrid = new PxgridControl(config);
		
		while (pxgrid.accountActivate() != AccountState.ENABLED)
			Thread.sleep(60000);
		logger.info("pxGrid controller version=" + pxgrid.getControllerVersion());

		while (true) {
			String ip = SampleHelper.prompt("IP address (or <enter> to disconnect): ");
			if (ip == null)	break;
			query(config, ip);
		}
	}
}
