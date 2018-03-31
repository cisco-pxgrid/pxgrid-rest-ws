package com.cisco.pxgrid.samples.ise;

import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.pxgrid.samples.ise.model.AccountState;
import com.cisco.pxgrid.samples.ise.model.Service;

/**
 * Demonstrates how to query session by IP from ISE Session Directory service
 */
public class SessionQueryByIP {
	private static Logger logger = LoggerFactory.getLogger(SessionQueryByIP.class);

	private static void query(SampleConfiguration config, String ip) throws IOException {
		PxgridControl pxgrid = new PxgridControl(config);
		
		// pxGrid ServiceLookup for session service
		Service[] services = pxgrid.serviceLookup("com.cisco.ise.session");
		if (services == null || services.length == 0) {
			System.out.println("Service unavailabe");
			return;
		}
		
		// Use first service
		Service service = services[0];
		String url = service.getProperties().get("restBaseUrl") + "/getSessionByIpAddress";
		logger.info("url={}", url);
		
		// pxGrid AccessSecret for the node
		String secret = pxgrid.getAccessSecret(service.getNodeName());
		
		String postData = "{\"ipAddress\":\"" + ip + "\"}";
		SampleHelper.postStringAndPrint(url, config.getNodeName(), secret, config.getSSLContext().getSocketFactory(), postData);
	}

	public static void main(String [] args) throws Exception {
		// Parse arguments
		SampleConfiguration config = new SampleConfiguration();
		try {
			config.parse(args);
		} catch (ParseException e) {
			config.printHelp("SessionQueryByIP");
			System.exit(1);
		}

		// AccountActivate
		PxgridControl pxgrid = new PxgridControl(config);
		while (pxgrid.accountActivate() != AccountState.ENABLED)
			Thread.sleep(60000);
		logger.info("pxGrid controller version={}", pxgrid.getControllerVersion());

		while (true) {
			String ip = SampleHelper.prompt("IP address (or <enter> to disconnect): ");
			if (ip == null)	break;
			query(config, ip);
		}
	}
}
