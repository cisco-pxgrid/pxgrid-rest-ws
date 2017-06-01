package com.cisco.pxgrid.samples.ise.http;

import java.io.IOException;
import java.text.ParseException;
import java.time.OffsetDateTime;

import com.cisco.pxgrid.model.AccountState;
import com.cisco.pxgrid.model.Service;

/**
 * Sample Java client program to get health of ISE nodes. 
 */
public class SystemHealthQuery {
	private static void download(SampleConfiguration config) throws IOException, ParseException {
		String nodeName = SampleHelper.prompt("Enter node name (or <enter> for all nodes):");
		OffsetDateTime startTimestamp = SampleHelper.promptDate("Enter start time (ex. '2015-01-31T13:00:00-07:00' or <enter> for one hour earlier): ");
		
		PxgridControl https = new PxgridControl(config);
		Service[] services = https.lookupService("com.cisco.ise.system");
		if (services == null || services.length == 0) {
			Console.log("Service unavailabe");
			return;
		}
		
		Service service = services[0];
		String url = service.getProperties().get("restBaseUrl") + "/getHealths";
		Console.log("REST URL is " + url);
		String secret = https.getAccessSecret(service.getNodeName());
		
		GetHealthsRequest request = new GetHealthsRequest();
		request.setNodeName(nodeName);
		request.setStartTimestamp(startTimestamp);
		SampleHelper.postObjectAndPrint(url, config.getUserName(), secret, config.getSSLContext().getSocketFactory(), request);
	}

	private static class GetHealthsRequest {
		private String nodeName;
		private OffsetDateTime startTimestamp;
		public String getNodeName() {
			return nodeName;
		}
		public void setNodeName(String nodeName) {
			this.nodeName = nodeName;
		}
		public OffsetDateTime getStartTimestamp() {
			return startTimestamp;
		}
		public void setStartTimestamp(OffsetDateTime startTimestamp) {
			this.startTimestamp = startTimestamp;
		}
	}

	public static void main(String [] args) throws Exception {
		SampleConfiguration config = new SampleConfiguration();
		PxgridControl control = new PxgridControl(config);
		while (control.accountActivate() != AccountState.ENABLED) {
			Thread.sleep(60000);
		}
		Console.log("pxGrid controller version=" + control.getControllerVersion());

		download(config);
	}
}