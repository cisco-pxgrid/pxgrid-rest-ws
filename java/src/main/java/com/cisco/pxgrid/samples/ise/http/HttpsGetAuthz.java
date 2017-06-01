package com.cisco.pxgrid.samples.ise.http;

import com.cisco.pxgrid.model.AccountState;

/**
 * Demonstrates how to get Authz
 */
public class HttpsGetAuthz {

	public static void main(String [] args) throws Exception {
		// Read environment for config
		SampleConfiguration config = new SampleConfiguration();
		
		PxgridControl https = new PxgridControl(config);
		
		// AccountActivate
		while (https.accountActivate() != AccountState.ENABLED) {
			Thread.sleep(60000);
		}
		System.out.println("pxGrid controller version=" + https.getControllerVersion());

		boolean isPermitted = https.isAuthorized("user1", "SessionDirectory", "subscribe");
		System.out.println("isPermitted=" + isPermitted);

	}
}
