package com.cisco.pxgrid.samples.ise;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSession;
import javax.websocket.Session;

import org.apache.commons.cli.ParseException;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.client.SslEngineConfigurator;
import org.glassfish.tyrus.client.auth.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.pxgrid.samples.ise.model.AccountState;
import com.cisco.pxgrid.samples.ise.model.Service;
import com.cisco.pxgrid.samples.ise.model.ServiceRegisterResponse;

/**
 * Demonstrate how to create a custom service that publishes data
 * 
 * The flow of the application is as follows:
 * 1. Parse arguments for configurations
 * 2. Activate Account. This will then require ISE Admin to approve this new node.
 * 3. pxGrid ServiceRegister to register the new custom service
 * 4. Schedule periodic pxGrid ServiceReregister to signify the service is still alive
 * 5. pxGrid ServiceLookup for ISE pubsub service
 * 6. pxGrid get AccessSecret for the ISE pubsub node
 * 7. Establish WebSocket connection with the ISE pubsub node
 * 8. Establish STOMP connection for pubsub messaging
 * 9. Schedule periodic publish of data
 * 10. Wait for keyboard input for stopping the application
 */
public class CustomServiceProvider {
	private static Logger logger = LoggerFactory.getLogger(CustomServiceProvider.class);

	public static void main(String[] args) throws Exception {
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

		// Parse arguments
		SampleConfiguration config = new SampleConfiguration();
		try {
			config.parse(args);
		} catch (ParseException e) {
			config.printHelp("CustomServiceProvider");
			System.exit(1);
		}

		// AccountActivate
		PxgridControl control = new PxgridControl(config);
		while (control.accountActivate() != AccountState.ENABLED) {
			Thread.sleep(60000);
		}
		logger.info("pxGrid controller version={}", control.getControllerVersion());

		// pxGrid ServiceRegister
		Map<String, String> sessionProperties = new HashMap<>();
		sessionProperties.put("wsPubsubService", "com.cisco.ise.pubsub");
		sessionProperties.put("customTopic", "/topic/com.example.custom");
		ServiceRegisterResponse response = control.serviceRegister("com.example.custom", sessionProperties);
		String registrationId = response.getId();
		long reregisterTimeMillis = response.getReregisterTimeMillis();

		// Schedule pxGrid ServiceReregister
		executor.scheduleWithFixedDelay(() -> {
			try {
				control.serviceReregister(registrationId);
			} catch (IOException e) {
				logger.error("Reregister failure");
			}
		}, reregisterTimeMillis, reregisterTimeMillis, TimeUnit.MILLISECONDS);

		// pxGrid ServiceLookup for pubsub service
		Service[] services = control.serviceLookup("com.cisco.ise.pubsub");
		if (services.length == 0) {
			logger.info("Pubsub service unavailabe");
			return;
		}
		// Use first service
		Service wsPubsubService = services[0];
		String wsURL = wsPubsubService.getProperties().get("wsUrl");
		logger.info("wsUrl={}", wsURL);

		// pxGrid AccessSecret
		String secret = control.getAccessSecret(wsPubsubService.getNodeName());

		// Setup WebSocket client
		ClientManager client = ClientManager.createClient();
		SslEngineConfigurator sslEngineConfigurator = new SslEngineConfigurator(config.getSSLContext());
		// Ignore hostname verification
		sslEngineConfigurator.setHostnameVerifier((String hostname, SSLSession session) -> true);
		client.getProperties().put(ClientProperties.SSL_ENGINE_CONFIGURATOR, sslEngineConfigurator);
		client.getProperties().put(ClientProperties.CREDENTIALS,
				new Credentials(config.getNodeName(), secret.getBytes()));

		// WebSocket connect
		StompPubsubClientEndpoint endpoint = new StompPubsubClientEndpoint();
		URI uri = new URI(wsURL);
		Session session = client.connectToServer(endpoint, uri);

		// STOMP connect
		endpoint.connect(uri.getHost());

		// STOMP send periodically
		executor.scheduleWithFixedDelay(() -> {
			try {
				endpoint.publish("/topic/com.example.custom", "custom data".getBytes());
			} catch (IOException e) {
				logger.error("Publish failure");
			}
		}, 0, 5, TimeUnit.SECONDS);

		SampleHelper.prompt("press <enter> to disconnect...");

		// pxGrid ServerUnregister
		control.unregisterService(registrationId);
		
		// Stop executor
		executor.shutdown();
		executor.awaitTermination(5, TimeUnit.SECONDS);

		// STOMP disconnect
		endpoint.disconnect("ID-123");
		// Wait for disconnect receipt
		Thread.sleep(3000);

		// Websocket close
		session.close();
	}
}
