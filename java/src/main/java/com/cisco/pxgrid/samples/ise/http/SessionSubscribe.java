package com.cisco.pxgrid.samples.ise.http;

import java.net.URI;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.client.SslEngineConfigurator;
import org.glassfish.tyrus.client.auth.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.pxgrid.model.AccountState;
import com.cisco.pxgrid.model.Service;

/**
 * Demonstrates how to subscribe using REST/WS
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
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "SessionSubscribe", config.getOptions());
			System.exit(1);
		}
		
		// AccountActivate
		PxgridControl control = new PxgridControl(config);
		while (control.accountActivate() != AccountState.ENABLED) {
			Thread.sleep(60000);
		}
		logger.info("pxGrid controller version=" + control.getControllerVersion());

		// pxGrid ServiceLookup for session service
		logger.info("Looking up service com.cisco.ise.session");
		Service[] services = control.lookupService("com.cisco.ise.session");
		if (services.length == 0) {
			logger.info("Session service unavailabe");
			return;
		}
		
		// Use first service
		Service sessionService = services[0];
		String wsPubsubServiceName = sessionService.getProperties().get("wsPubsubService");
		String sessionTopic = sessionService.getProperties().get("sessionTopic");
		logger.info("wsPubsubServiceName=" + wsPubsubServiceName + " sessionTopic=" + sessionTopic);
		
		// pxGrid ServiceLookup for pubsub service
		services = control.lookupService(wsPubsubServiceName);
		if (services.length == 0) {
			logger.info("Pubsub service unavailabe");
			return;
		}

		// Use first service
		Service wsPubsubService = services[0];
		String wsURL = wsPubsubService.getProperties().get("wsUrl");
		logger.info("wsUrl=" + wsURL);

		// pxGrid get AccessSecret
		String secret = control.getAccessSecret(wsPubsubService.getNodeName());

		// WebSocket config
		ClientManager client = ClientManager.createClient();
		SslEngineConfigurator sslEngineConfigurator = new SslEngineConfigurator(config.getSSLContext());
		sslEngineConfigurator.setHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
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

		SampleHelper.prompt("press <enter> to disconnect...");

		// STOMP disconnect
		endpoint.disconnect("ID-123");
		// Wait for disconnect receipt
		Thread.sleep(3000);
		
		session.close();
	}
}
