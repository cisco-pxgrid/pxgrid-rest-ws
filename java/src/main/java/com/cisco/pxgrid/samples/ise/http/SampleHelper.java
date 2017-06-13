package com.cisco.pxgrid.samples.ise.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class SampleHelper {
	public static HttpsURLConnection createHttpsURLConnection(String url, String user, String password, SSLSocketFactory sslSocketFactory) throws IOException {
		URL conn = new URL(url);
		HttpsURLConnection https = (HttpsURLConnection) conn.openConnection();
		https.setSSLSocketFactory(sslSocketFactory);
		String userPassword = user + ":" + password;
		String encoded = Base64.getEncoder().encodeToString(userPassword.getBytes());
		https.setRequestProperty("Authorization", "Basic " + encoded);
		return https;
	}

	public static String prompt(String msg) {
        System.out.print(msg);
        @SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
        String value = scanner.nextLine();
        if ("".equals(value)) return null;
        return value;
    }
    
    public static OffsetDateTime promptDate(String msg) throws ParseException {
    	String value = prompt(msg);
    	if (value == null) return null;
    	return OffsetDateTime.parse(value);
	}
	
	public static void postObjectAndPrint(String url, String user, String password, SSLSocketFactory ssl, Object postObject) throws IOException {
		Gson gson = new GsonBuilder()
	            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
	            .create();
		postStringAndPrint(url, user, password, ssl, gson.toJson(postObject));
	}
	
	public static void postStringAndPrint(String url, String user, String password, SSLSocketFactory ssl, String postData) throws IOException {
		HttpsURLConnection httpsConn = SampleHelper.createHttpsURLConnection(url, user, password, ssl);
    	httpsConn.setRequestMethod("POST");
    	httpsConn.setRequestProperty("Content-Type", "application/json");
    	httpsConn.setRequestProperty("Accept", "application/json");
    	httpsConn.setDoInput(true);
    	httpsConn.setDoOutput(true);
        
		OutputStreamWriter osw = new OutputStreamWriter(httpsConn.getOutputStream());
		osw.write(postData);
		osw.flush();

		int status = httpsConn.getResponseCode();
    	System.out.println("Response status: " + status);

		if (status < HttpURLConnection.HTTP_BAD_REQUEST) {
			try (InputStream in = httpsConn.getInputStream()) {
				String content = IOUtils.toString(in, StandardCharsets.UTF_8);
	        	System.out.println("Content: "  + content);
	        }
		}
		else {
			try (InputStream in = httpsConn.getErrorStream()) {
				String content = IOUtils.toString(in, StandardCharsets.UTF_8);
	        	System.out.println("Content: "  + content);
	        }
		}
	}
	
	private static class OffsetDateTimeAdapter extends TypeAdapter<OffsetDateTime> {
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		@Override
		public void write(JsonWriter out, OffsetDateTime value) throws IOException {
			if (value == null) {
				out.nullValue();
				return;
			}
			out.value(formatter.format(value));
		}

		@Override
		public OffsetDateTime read(JsonReader in) throws IOException {
			if (in.peek() == JsonToken.NULL) {
				in.nextNull();
				return null;
			}
			return formatter.parse(in.nextString(), OffsetDateTime::from);
		}
	}
}
