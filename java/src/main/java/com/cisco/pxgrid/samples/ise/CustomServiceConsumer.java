package com.cisco.pxgrid.samples.ise;

import java.net.URI;

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

/**
 * Demonstrates how to subscribe a topic from a custom service
 * 
 * The flow of the application is as follows:
 * 1. Parse arguments for configurations
 * 2. Activate Account. This will then require ISE Admin to approve this new node.
 * 3. pxGrid ServiceLookup for the custom service
 * 4. pxGrid ServiceLookup for ISE pubsub service
 * 5. pxGrid get AccessSecret for the ISE pubsub node
 * 6. Establish WebSocket connection with the ISE pubsub node
 * 7. Establish STOMP connection for pubsub messaging
 * 8. Subscribe to the topic in the custom service
 * 9. Wait for keyboard input for stopping the application
 */
public class CustomServiceConsumer {
	private static Logger logger = LoggerFactory.getLogger(CustomServiceProvider.class);

	// Subscribe handler class
	private static class MessageHandler implements StompSubscription.Handler {
		@Override
		public void handle(StompFrame message) {
			System.out.println(new String(message.getContent()));
		}
	}

	public static void main(String [] args) throws Exception {
		// Parse arguments
		SampleConfiguration config = new SampleConfiguration();
		try {
			config.parse(args);
		} catch (ParseException e) {
			config.printHelp("CustomServiceConsumer");
			System.exit(1);
		}
		
		// AccountActivate
		PxgridControl control = new PxgridControl(config);
		while (control.accountActivate() != AccountState.ENABLED) {
			Thread.sleep(60000);
		}
		logger.info("pxGrid controller version={}", control.getControllerVersion());
		
		
		// pxGrid ServiceLookup for custom service
		Service[] services = control.serviceLookup("com.example.custom");
		if (services.length == 0) {
			logger.info("Service unavailabe");
			return;
		}
		
		// Use first service. Note that ServiceLookup randomize ordering of services
		Service customService = services[0];
		String wsPubsubServiceName = customService.getProperties().get("wsPubsubService");
		String customTopic = customService.getProperties().get("customTopic");
		logger.info("wsPubsubServiceName={} sessionTopic={}", wsPubsubServiceName, customTopic);
		
		// pxGrid ServiceLookup for pubsub service
		services = control.serviceLookup(wsPubsubServiceName);
		if (services.length == 0) {
			logger.info("Pubsub service unavailabe");
			return;
		}

		// Use first service
		Service wsPubsubService = services[0];
		String wsURL = wsPubsubService.getProperties().get("wsUrl");
		logger.info("wsUrl={}", wsURL);

		// pxGrid AccessSecret for the pubsub node
		String secret = control.getAccessSecret(wsPubsubService.getNodeName());

		// WebSocket config
		ClientManager client = ClientManager.createClient();
		SslEngineConfigurator sslEngineConfigurator = new SslEngineConfigurator(config.getSSLContext());
		client.getProperties().put(ClientProperties.SSL_ENGINE_CONFIGURATOR, sslEngineConfigurator);
		client.getProperties().put(ClientProperties.CREDENTIALS,
				new Credentials(config.getNodeName(), secret.getBytes()));
		
		// WebSocket connect
		StompPubsubClientEndpoint endpoint = new StompPubsubClientEndpoint();
		URI uri = new URI(wsURL);
		Session session = client.connectToServer(endpoint, uri);

		// STOMP connect
		endpoint.connect(uri.getHost());
		
		// Subscribe
		StompSubscription subscription = new StompSubscription(customTopic, new MessageHandler());
		endpoint.subscribe(subscription);

		SampleHelper.prompt("press <enter> to disconnect...");

		// STOMP disconnect
		endpoint.disconnect("ID-123");
		// Wait for disconnect receipt
		Thread.sleep(3000);
		
		session.close();
	}
}
