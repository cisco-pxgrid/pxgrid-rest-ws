package com.cisco.pxgrid.samples.ise;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.pxgrid.samples.ise.StompSubscription.Handler;

@ClientEndpoint
public class StompPubsubClientEndpoint extends Endpoint {
	private static Logger logger = LoggerFactory.getLogger(StompPubsubClientEndpoint.class);

	private volatile Session session;
	private Map<String, StompSubscription> mapOfIdToSubscription = new ConcurrentHashMap<>();
	
	public void connect(String hostname) throws IOException {
    	logger.info("STOMP CONNECT host=" + hostname);
		StompFrame message = new StompFrame();
    	message.setCommand(StompFrame.Command.CONNECT);
    	message.setHeader("accept-version", "1.2");
    	message.setHeader("host", hostname);
    	send(message);
	}
	
	public void disconnect(String receipt) throws IOException {
		logger.info("STOMP DISCONNECT receipt=" + receipt);
		StompFrame message = new StompFrame();
    	message.setCommand(StompFrame.Command.DISCONNECT);
    	if (receipt != null) {
    		message.setHeader("receipt", receipt);
    	}
    	send(message);
	}
	
	public void subscribe(StompSubscription subscription) throws IOException {
		logger.info("STOMP SUBSCRIBE topic=" + subscription.getTopic());
		mapOfIdToSubscription.put(subscription.getId(), subscription);
		if (session != null) {
			StompFrame message = subscription.getSubscribeMessage();
	    	send(message);
		}
	}

	public void publish(String topic, byte[] content) throws IOException {
		logger.info("STOMP SEND topic=" + topic);
		StompFrame message = new StompFrame();
    	message.setCommand(StompFrame.Command.SEND);
    	message.setHeader("destination", topic);
    	message.setHeader("content-length", Integer.toString(content.length));
    	message.setContent(content);
    	send(message);
	}
	
	private void send(StompFrame message) throws IOException {
		if (session != null) {
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    	message.write(baos);
	    	// Send as binary
	    	session.getBasicRemote().sendBinary(ByteBuffer.wrap(baos.toByteArray()));
		}
	}
	
	public void waitForOpen() throws InterruptedException {
		synchronized (this) {
			while (session == null) {
				this.wait();
			}
		}
	}
	
	private void onStompMessage(StompFrame stomp) {
		switch (stomp.getCommand()) {
		case CONNECTED:
			String version = stomp.getHeader("version");
			logger.info("STOMP CONNECTED version={}", version);
			break;
		case RECEIPT:
			String receiptId = stomp.getHeader("receipt-id");
			logger.info("STOMP RECEIPT id={}", receiptId);
			break;
		case MESSAGE:
			String id = stomp.getHeader("subscription");
			StompSubscription subscription = mapOfIdToSubscription.get(id);
			Handler handler = subscription.getHandler();
			if (handler != null) {
				handler.handle(stomp);
			}
			break;
		case ERROR:
			// Server will close connect on ERROR according to STOMP specification
			logger.info("STOMP ERROR stomp={}", stomp);
			break;
		default:
			// Ignore others
			break;
		}
	}
	
	private class TextHandler implements MessageHandler.Whole<String> {
		@Override
		public void onMessage(String message) {
        	try {
        		StompFrame stomp = StompFrame.parse(new ByteArrayInputStream(message.getBytes()));
        		onStompMessage(stomp);
        	} catch (IOException | ParseException e) {
        		logger.error("onMessage", e);
        	}
		}
	}

	private class BinaryHandler implements MessageHandler.Whole<InputStream> {
		@Override
		public void onMessage(InputStream in) {
        	try {
				StompFrame stomp = StompFrame.parse(in);
				onStompMessage(stomp);
			} catch (IOException | ParseException e) {
        		logger.error("onMessage", e);
			}
		}
	}
	
	@Override
	public void onOpen(Session session, EndpointConfig cfg) {
		logger.info("WS onOpen");
		this.session = session;
		try {
	    	session.addMessageHandler(new TextHandler());
	    	session.addMessageHandler(new BinaryHandler());

        	for (StompSubscription subscription : mapOfIdToSubscription.values()) {
    			StompFrame message = subscription.getSubscribeMessage();
					send(message);
			}
		} catch (IOException e) {
			logger.error("onOpen", e);
		}
		synchronized (this) {
			this.notifyAll();
		}
	}

	@Override
	public void onClose(Session session, CloseReason closeReason) {
		logger.info("WS onClose closeReason code={} phrase={}", closeReason.getCloseCode(), closeReason.getReasonPhrase());
		this.session = null;
	}
	
	@Override
	public void onError(Session session, Throwable thr) {
		logger.info("WS onError thr={}", thr.getMessage());
		this.session = null;
	}
}
