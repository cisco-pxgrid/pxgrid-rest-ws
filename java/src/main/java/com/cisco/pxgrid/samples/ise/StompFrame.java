package com.cisco.pxgrid.samples.ise.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * This follows STOMP 1.2 specification to parse and generate STOMP frames:
 *   https://stomp.github.io/stomp-specification-1.2.html
 * 
 * This single class is self-sufficient handle all STOMP frames.
 * 
 * Note for WebSocket:
 * If input comes as WebSocket text type, (WS RFC says Text is UTF-8)
 * server side handling code like Spring TextMessage may convert the bytes to String as UTF-8
 * which maybe the wrong encoding as STOMP frame itself can use other encoding.
 *   e.g. A particular encoding may have bytes: FF FF FF FF FF FF FF FF FF FF... 10, that is completely out of range for Unicode.
 * Unless STOMP body is also UTF-8, STOMP frame must be sent as binary
 * 
 * @author Alan Lei
 */
public class StompFrame {
	public enum Command {
		CONNECT, STOMP, CONNECTED, SEND, SUBSCRIBE, UNSUBSCRIBE, ACK, NACK,
		BEGIN, COMMIT, ABORT, DISCONNECT, MESSAGE, RECEIPT, ERROR;
		
		private static Map<String, Command> mapOfStringToCommand = new HashMap<>();
		static {
			for (Command command : Command.values()) {
				mapOfStringToCommand.put(command.name(), command);
			}
		}
		
		public static Command get(String value) {
			return mapOfStringToCommand.get(value);
		}
	}
	
	private Command command;
	private Map<String, String> headers = new HashMap<>();
	private byte[] content;
	private final static int MAX_BUFFER_SIZE = 1024;
	
	public Command getCommand() {
		return command;
	}
	
	public void setCommand(Command command) {
		this.command = command;
	}
	
	public String getHeader(String name) {
		return headers.get(name);
	}
	
	public void setHeader(String name, String value) {
		headers.put(name, value);
	}

	public Map<String, String> getHeaders() {
		return headers;
	}
	
	public byte[] getContent() {
		return content;
	}
	
	public void setContent(byte[] content) {
		this.content = content;
	}
	
	public void write(OutputStream out) throws IOException {
		out.write(command.name().getBytes());
		out.write('\n');
		for (String name : headers.keySet()) {
			out.write(name.getBytes());
			out.write(':');
			out.write(headers.get(name).getBytes());
			out.write('\n');
		}
		out.write('\n');
		if (content != null) {
			out.write(content);
		}
		out.write(0);
	}
	
	private static String readLine(InputStream in) throws IOException, ParseException {
		byte[] line = new byte[MAX_BUFFER_SIZE];
		int index = 0;
		while (index < MAX_BUFFER_SIZE) {
			int b = in.read();
			if (b != -1) {
				if (b == '\n') {
					return new String(line, 0, index);			
				}
				if (b != '\r') {
					line[index] = (byte)b;
					index++;
				}
			}
			else {
				// No line found
				return null;
			}
		}
		throw new ParseException("Line too long", MAX_BUFFER_SIZE);
	}
	
	/*
	 * Using InputStream instead of Reader because
	 * content-length is octet count instead of character count
	 */
	public static StompFrame parse(InputStream reader) throws IOException, ParseException {
		StompFrame stomp = new StompFrame();
		
		// Read Command
		String line = readLine(reader);
		Command command = Command.get(line);
		if (command == null) throw new ParseException("Unknown command: " + line, 0);
		stomp.setCommand(command);
		
		// Read Headers
		int contentLength = -1;
		while ((line = readLine(reader)) != null) {
			if (line.equals("")) break;
			int colon = line.indexOf(':');
			String name = line.substring(0, colon);
			String value = line.substring(colon + 1);
			stomp.setHeader(name, value);
			if (name.equals("content-length")) {
				contentLength = Integer.parseInt(value);
			}
		}
		
		// Read Content
		if (contentLength != -1) {
			// content-length is in octets
			byte[] content = new byte[contentLength];
			reader.read(content);
			stomp.setContent(content);
			if (reader.read() != 0) {
				throw new ParseException("Byte after STOMP Body not NULL", -1);
			}
		}
		else {
			// No content-length. Look for ending NULL byte.
			byte[] buffer = new byte[MAX_BUFFER_SIZE];
			int length = 0;
			while (length < MAX_BUFFER_SIZE) {
				int b = reader.read();
				if (b == -1) {
					throw new ParseException("Premature end of stream", -1);
				}
				if (b == 0) {
					if (length > 0) {
						byte[] content = new byte[length];
						System.arraycopy(buffer, 0, content, 0, length);
						stomp.setContent(content);
					}
					// More EOLs may follow, but ignored.
					return stomp;
				}
				buffer[length] = (byte)b;
				length++;
			}
			throw new ParseException("Frame too long", -1);
		}
		return stomp;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("command=" + command);
		sb.append(", headers={");
		for (String name : headers.keySet()) {
			sb.append("'" + name + "':");
			sb.append("'" + headers.get(name) + "',");
		}
		sb.append("}");
		sb.append(", content.length=" + content.length);
		return sb.toString();
	}
}
