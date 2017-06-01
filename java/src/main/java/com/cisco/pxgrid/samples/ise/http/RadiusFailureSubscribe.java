package com.cisco.pxgrid.samples.ise.http;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.websocket.MessageHandler;
import javax.websocket.PongMessage;
import javax.websocket.Session;

import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.client.auth.Credentials;

import com.cisco.pxgrid.model.AccountState;
import com.cisco.pxgrid.model.Service;

/**
 * Demonstrates how to subscribe using REST/WS
 */
public class RadiusFailureSubscribe {
	// Subscribe handler class
	private static class SubsribeDataHandler implements StompSubscription.Handler {
		@Override
		public void handle(StompFrame message) {
			try {
				String content = new String(message.getContent());
				Console.log("Received message " + content);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String [] args) throws Exception {
		// Read environment for config
		SampleConfiguration config = new SampleConfiguration();
		PxgridControl control = new PxgridControl(config);
		
		// AccountActivate
		while (control.accountActivate() != AccountState.ENABLED) {
			Thread.sleep(60000);
		}
		Console.log("pxGrid controller version=" + control.getControllerVersion());

		// Session ServiceLookup
		Service[] services = control.lookupService("com.cisco.ise.radius");
		if (services.length == 0) {
			Console.log("Authentication Failure service unavailabe");
			return;
		}
		
		Console.log("Starting the consumer thread");
		Consumer consumer = new Consumer(config);
		consumer.init(services, control);
		
		WSConnWatchdog watchdog = new WSConnWatchdog(consumer);
		consumer.setWatchdog(watchdog);
		consumer.subscribe();

		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleWithFixedDelay(watchdog, 30, 30, TimeUnit.SECONDS);
		
		Console.log("!!! Press enter to terminate the program anytime !!!");
		System.in.read();
		executor.shutdown();
		consumer.shutdown();
	}
	
	static class Consumer {
		SampleConfiguration config = null;
		volatile int currentIndex = 0;
		volatile Session session = null;
		WSConnWatchdog watchdog = null;
		String[] secrets = null;
		String[] wsUrls = null;
		String topic = null;
		
		public Consumer(SampleConfiguration conf) {
			this.config = conf;
			Console.log("Created Consumer object");
		}
		
		void setWatchdog(WSConnWatchdog dog) {
			this.watchdog = dog;
		}
		
		void init(Service[] services, PxgridControl control) {
			try {
				Console.log("Susbriber init() started");
				Service sessionService = services[0];
				String wsPubsubServiceName = sessionService.getProperties().get("wsPubsubService");
				this.topic = sessionService.getProperties().get("failureTopic");
				Console.log("wsPubsubServiceName=" + wsPubsubServiceName + " Topic Name=" + topic);
				
				// Pubsub ServiceLookup
				Service[] pubsubServices = control.lookupService(wsPubsubServiceName);
				this.secrets = new String[pubsubServices.length];
				this.wsUrls = new String[pubsubServices.length];
				
				if (pubsubServices.length == 0) {
					Console.log("Pubsub service unavailabe");
					return;
				} else {
					Console.log("Number of pubsub services " + pubsubServices.length);
					for (int i = 0; i < pubsubServices.length; i++) {
						Service service = pubsubServices[i];
						Console.log("Found pubsub service at " + service.getNodeName() );
						secrets[i] = control.getAccessSecret(
										service.getNodeName());
						wsUrls[i] = service.getProperties().get("WSURL");
					}
				}
				Console.log("Susbcriber init() done");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public synchronized void subscribe() {
			try {
				Console.log("###############################################");
				Console.log("Starting the Websocket connection process....");
	
				// WebSocket config
				ClientManager client = ClientManager.createClient();
				SSLEngineConfigurator sslEngineConfigurator = new SSLEngineConfigurator(config.getSSLContext());
				client.getProperties().put(ClientProperties.SSL_ENGINE_CONFIGURATOR, sslEngineConfigurator);
				int index = secrets.length == 0 ? 0 : (currentIndex++ % secrets.length);
				client.getProperties().put(ClientProperties.CREDENTIALS,
						new Credentials(config.getUserName(), secrets[index].getBytes()));
	
				// WebSocket connect
				StompPubsubClientEndpoint endpoint = new StompPubsubClientEndpoint();
				Console.log("Trying to connect to websocket at " + wsUrls[index]);
				this.session = client.connectToServer(endpoint, new URI(wsUrls[index]));
				this.watchdog.setPongHandler();
				Console.log("Websocket connection created status=" + session.isOpen() + " for " + session.getRequestURI());
				
				// Subscribe
				Console.log("Subscribing to topic " + topic);
				StompSubscription subscription = new StompSubscription(topic, new SubsribeDataHandler());
				endpoint.subscribe(subscription);				
			} catch (Exception e) {
				Console.log(e.toString());
				e.printStackTrace();
			}
		}
		
		public Session getSession() {
			return this.session;
		}
		
		public void shutdown() {
			if(session != null) {
				Console.log("Shutting down the subscriber...");
				try {
					session.close();
				} catch (Exception e) {
					Console.log(e.toString());
					e.printStackTrace();
				}
			}
		}
	}
	
	static class WSConnWatchdog implements Runnable {
		static final long MAX_WAIT_TIME = 30*1000L;
		volatile Consumer consumer = null;
		volatile boolean pingSent = false;
		volatile long timePingSent = 0;
		
		public WSConnWatchdog(Consumer c) {
			this.consumer = c;
		}
		
		public void setPongHandler() {
			Session session = consumer.getSession();
			session.addMessageHandler(new MessageHandler.Whole<PongMessage>() {
				@Override
				public void onMessage(PongMessage pongMessage) {
					StringBuffer pong = new StringBuffer();
					pong.append("Received Pong response: Yes ")
						.append(new String(pongMessage.getApplicationData().array()))
						.append(" Over session ")
						.append(session.getId());
					Console.log(pong.toString());
					
					//enable to send the next ping
					WSConnWatchdog.this.pingSent = false;	
				}
			});
		}
		
		public void run() {
			try {
				Session session = consumer.getSession();
				if(session != null) {
					if(!session.isOpen()) {
						Console.log("Websocket connection is closed, will re-connect...");
						//re-subscribe, possibly with the next available server
						consumer.subscribe();
						pingSent = false;
						return;
					} 
					
					if(!pingSent) {
						Console.log("Sending the periodic PING message over websocket - I am here, are you there ?");
						String pingString = "I am here, are you there ?";
						ByteBuffer pingData = ByteBuffer.allocate(pingString.getBytes().length);
						pingData.put(pingString.getBytes()).flip();
						session.getBasicRemote().sendPing(pingData);
						pingSent = true;
						timePingSent = System.currentTimeMillis();
					} else {
						if(System.currentTimeMillis() - timePingSent >= MAX_WAIT_TIME) {
							Console.log("Time to receive Pong exceeded!");
							Console.log("Will re-initialize the websocket connection");
							
							//re-subscribe, possibly with the next available server
							consumer.subscribe();
							return;
						} else {
							Console.log("Ping already sent, waiting for Pong to be received");
						}
					}
				}
			} catch (Exception e) {
				Console.log(e.toString());
				e.printStackTrace();
			}
		}
	}
}
