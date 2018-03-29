package com.cisco.pxgrid.samples.ise;

import java.util.concurrent.atomic.AtomicInteger;

public class StompSubscription {
	public static interface Handler {
		void handle(StompFrame message);
	}

	private static AtomicInteger currentSubscriptionID = new AtomicInteger();
	private String id = Integer.toString(currentSubscriptionID.getAndIncrement());
	private String topic;
	private Handler handler;

	public StompSubscription(String topic, Handler handler) {
		this.topic = topic;
		this.handler = handler;
	}

	public String getId() {
		return id;
	}

	public String getTopic() {
		return topic;
	}

	public Handler getHandler() {
		return handler;
	}

	public StompFrame getSubscribeMessage() {
		StompFrame message = new StompFrame();
		message.setCommand(StompFrame.Command.SUBSCRIBE);
		message.setHeader("destination", topic);
		message.setHeader("id", id);
		return message;
	}
}