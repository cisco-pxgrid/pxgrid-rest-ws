package com.cisco.pxgrid.samples.ise;

import java.net.URI;

import javax.net.ssl.SSLSession;

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
 * Demonstrates how to subscribe to ISE SessionDirectory
 */
public class SessionSubscribe {
	private static Logger logger = LoggerFactory.getLogger(SessionSubscribe.class);

	// Subscribe handler class
	private static class SessionHandler implements StompSubscription.Handler {
		@Override
		public void handle(StompFrame message) {
			logger.info("Content={}", new String(message.getContent()));
		}
	}

	public static void main(String[] args) throws Exception {
		// Parse arguments
		SampleConfiguration config = new SampleConfiguration();
		try {
			config.parse(args);
		} catch (ParseException e) {
			config.printHelp("SessionSubscribe");
			System.exit(1);
		}
		
		// AccountActivate
		PxgridControl control = new PxgridControl(config);
		while (control.accountActivate() != AccountState.ENABLED) {
			Thread.sleep(60000);
		}
		logger.info("pxGrid controller version={}", control.getControllerVersion());

		// pxGrid ServiceLookup for session service
		Service[] services = control.serviceLookup("com.cisco.ise.session");
		if (services.length == 0) {
			logger.info("Session service unavailabe");
			return;
		}
		
		// Use first service. Note that ServiceLookup randomize ordering of services
		Service sessionService = services[0];
		String wsPubsubServiceName = sessionService.getProperties().get("wsPubsubService");
		String sessionTopic = sessionService.getProperties().get("sessionTopic");
		logger.info("wsPubsubServiceName={} sessionTopic={}", wsPubsubServiceName, sessionTopic);
		
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

		// pxGrid get AccessSecret
		String secret = control.getAccessSecret(wsPubsubService.getNodeName());


		// WebSocket config
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
		javax.websocket.Session session = client.connectToServer(endpoint, uri);

		// STOMP connect
		endpoint.connect(uri.getHost());
		
		// Subscribe
		StompSubscription subscription = new StompSubscription(sessionTopic, new SessionHandler());
		endpoint.subscribe(subscription);

		// Give time for connection to establish before prompt
		Thread.sleep(1000);
		SampleHelper.prompt("press <enter> to disconnect...");

		// STOMP disconnect
		endpoint.disconnect("ID-123");
		// Wait for disconnect receipt
		Thread.sleep(3000);
		
		// Websocket close
		session.close();
	}
}
