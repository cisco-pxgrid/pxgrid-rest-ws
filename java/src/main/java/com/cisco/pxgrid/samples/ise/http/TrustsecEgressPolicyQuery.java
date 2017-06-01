package com.cisco.pxgrid.samples.ise.http;

import java.io.IOException;
import java.time.OffsetDateTime;

import com.cisco.pxgrid.model.AccountState;
import com.cisco.pxgrid.model.Service;

public class TrustsecEgressPolicyQuery {
	
	private static void query(SampleConfiguration config) throws IOException {
		String matrix = SampleHelper.prompt("Enter id (or <enter> for all matrices):");
		GetEgressPoliciesRequest request = new GetEgressPoliciesRequest();
		request.setId(matrix);
		
		PxgridControl pxgrid = new PxgridControl(config);
		Service[] services = pxgrid.lookupService("com.cisco.ise.config.trustsec");
		if (services == null || services.length == 0) {
			System.out.println("Service unavailabe");
			return;
		}
		Service service = services[0];
		String url = service.getProperties().get("restBaseUrl") + "/getEgressPolicies";
		Console.log("REST URL is " + url);
		String secret = pxgrid.getAccessSecret(service.getNodeName());
		SampleHelper.postObjectAndPrint(url, config.getUserName(), secret, config.getSSLContext().getSocketFactory(), request);
		
		url = service.getProperties().get("restBaseUrl") + "/getEgressMatrices";
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

	private static class GetEgressPoliciesRequest {
		private String id;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
	}

}
