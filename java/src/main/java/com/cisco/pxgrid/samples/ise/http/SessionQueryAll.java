package com.cisco.pxgrid.samples.ise.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.time.OffsetDateTime;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.stream.XMLStreamException;

import com.cisco.pxgrid.model.AccountState;
import com.cisco.pxgrid.model.Service;

/**
 * Demonstrates how to use query multiple sessions from ISE
 */
public class SessionQueryAll {
	private static void downloadUsingAccessSecret(SampleConfiguration config) throws IOException, XMLStreamException, ParseException {
		OffsetDateTime startTime = SampleHelper.promptDate("Enter start time (ex. '2015-01-31T13:00:00-07:00' or <enter> for no start time): ");

		
		PxgridControl https = new PxgridControl(config);
		Service[] services = https.lookupService("com.cisco.ise.session");
		if (services == null || services.length == 0)
			System.out.println("Service unavailabe");
		
		for (Service service : services) {
			String url = service.getProperties().get("restBaseURL") + "/getSessions";
			Console.log("REST URL is " + url);
			
			String secret = https.getAccessSecret(service.getNodeName());
		
			HttpsURLConnection httpsConn = SampleHelper.createHttpsURLConnection(
					url, config.getUserName(), secret, config.getSSLContext().getSocketFactory());
			httpsConn.setRequestMethod("POST");
			httpsConn.setRequestProperty("user", config.getUserName());
			httpsConn.setRequestProperty("Content-Type", "application/json");
			httpsConn.setRequestProperty("Accept", "application/json");
			httpsConn.setDoInput(true);
			httpsConn.setDoOutput(true);
		        
			OutputStreamWriter osw = new OutputStreamWriter(httpsConn.getOutputStream());
			if (startTime != null) {
				osw.write("{\"startTimestamp\":\"" + startTime.toString() + "\"}");
			}
			else {
				osw.write("{}");
			}
			osw.flush();
			
			try (BufferedInputStream in = new BufferedInputStream(httpsConn.getInputStream())) {
				if(httpsConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
		        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        	byte[] buffer = new byte[64];
		        	int bytesRead = -1;
		        	while((bytesRead = in.read(buffer, 0, buffer.length)) != -1)
		        		baos.write(buffer, 0, bytesRead);
		        		String sessionStr = new String(baos.toByteArray(), Charset.forName("UTF-8"));
		        		System.out.println(sessionStr);
	        	}
			} catch (IOException e) {
	        	e.printStackTrace();
			}
		}
	}

	public static void main(String [] args) throws Exception {
		SampleConfiguration config = new SampleConfiguration();
		PxgridControl control = new PxgridControl(config);
		
		while (control.accountActivate() != AccountState.ENABLED)
			Thread.sleep(60000);
		System.out.println("pxGrid controller version=" + control.getControllerVersion());

		downloadUsingAccessSecret(config);
	}
}
