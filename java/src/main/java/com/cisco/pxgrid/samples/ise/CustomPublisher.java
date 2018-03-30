package com.cisco.pxgrid.samples.ise;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
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
 * Sample custom service that publishes data
 */
public class CustomPublisher {
	private static Logger logger = LoggerFactory.getLogger(CustomPublisher.class);

	public static void main(String[] args) throws Exception {
		// Parse arguments
		SampleConfiguration config = new SampleConfiguration();
		try {
			config.parse(args);
		} catch (ParseException e) {
			config.printHelp("CustomPublisher");
			System.exit(1);
		}

		// AccountActivate
		PxgridControl control = new PxgridControl(config);
		while (control.accountActivate() != AccountState.ENABLED) {
			Thread.sleep(60000);
		}
		logger.info("pxGrid controller version={}", control.getControllerVersion());

		// pxGrid ServiceRegistration
		Map<String, String> sessionProperties = new HashMap<>();
		sessionProperties.put("wsPubsubService", "com.cisco.ise.pubsub");
		sessionProperties.put("customTopic", "/topic/com.example.custom");
		ServiceRegisterResponse response = control.registerService("com.example.custom", sessionProperties);
		String registrationId = response.getId();
		long reregisterTimeMillis = response.getReregisterTimeMillis();

		// Schedule pxGrid ServiceReregistration
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		ScheduledFuture<?> reregisterHandle = executor.scheduleWithFixedDelay(() -> {
			try {
				control.reregisterService(registrationId);
			} catch (IOException e) {
				logger.error("Reregister failure");
			}
		}, reregisterTimeMillis, reregisterTimeMillis, TimeUnit.MILLISECONDS);

		// pxGrid ServiceLookup for pubsub service
		Service[] services = control.lookupService("com.cisco.ise.pubsub");
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

		// get URI, connect pxGrid client to the pxGrid server so that we can publish to
		// dynamic service
		URI uri = new URI(wsURL);
		Session session = client.connectToServer(endpoint, uri);

		// STOMP connect
		endpoint.connect(uri.getHost());

		// Give time for connection to establish before prompt
		Thread.sleep(1000);
		SampleHelper.prompt("press <enter> to publish...");

		endpoint.publish("/topic/com.example.custom", "custom data".getBytes());

		SampleHelper.prompt("press <enter> to disconnect...");

		// Stop reregistration
		reregisterHandle.cancel(true);
		executor.shutdown();

		// STOMP disconnect
		endpoint.disconnect("ID-123");
		// Wait for disconnect receipt
		Thread.sleep(3000);

		session.close();
	}
}
