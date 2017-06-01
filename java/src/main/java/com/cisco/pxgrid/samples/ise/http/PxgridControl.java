package com.cisco.pxgrid.samples.ise.http;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Base64;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import com.cisco.pxgrid.model.AccessSecretRequest;
import com.cisco.pxgrid.model.AccessSecretResponse;
import com.cisco.pxgrid.model.AccountActivateRequest;
import com.cisco.pxgrid.model.AccountActivateResponse;
import com.cisco.pxgrid.model.AccountCreateRequest;
import com.cisco.pxgrid.model.AccountCreateResponse;
import com.cisco.pxgrid.model.AccountState;
import com.cisco.pxgrid.model.Authorization;
import com.cisco.pxgrid.model.AuthorizationRequest;
import com.cisco.pxgrid.model.AuthorizationResponse;
import com.cisco.pxgrid.model.Service;
import com.cisco.pxgrid.model.ServiceLookupRequest;
import com.cisco.pxgrid.model.ServiceLookupResponse;
import com.cisco.pxgrid.model.ServiceRegisterRequest;
import com.cisco.pxgrid.model.ServiceRegisterResponse;
import com.google.gson.Gson;

/**
 * Using HTTPS for pxGrid control
 */
public class PxgridControl {
	private SampleConfiguration config;
    private String controllerVersion;
    
    public PxgridControl(SampleConfiguration config) {
    	this.config = config;
	}
    
	private <T> T sendRequest(HttpsURLConnection https, Object request, Class<T> responseClass) throws IOException {
		https.setRequestProperty("Content-Type", "application/json");
		https.setRequestProperty("Accept", "application/json");

		Gson gson = new Gson();

		OutputStreamWriter out = new OutputStreamWriter(https.getOutputStream());
		Console.log("Request is: " + gson.toJson(request));
		gson.toJson(request, out);
		out.flush();

    	InputStreamReader in = new InputStreamReader(https.getInputStream());
    	T response = gson.fromJson(in, responseClass);
    	Console.log("Response is: " + gson.toJson(response));

    	return response;
	}
	
    private HttpsURLConnection getHttpsURLConnection(String urlSuffix) throws IOException {
		String url = "https://" + config.getHostnames()[0] + ":8910/pxgrid/control/" + urlSuffix;
		URL conn = new URL(url);
		HttpsURLConnection https = (HttpsURLConnection) conn.openConnection();
		
		// SSL and Auth
		https.setSSLSocketFactory(config.getSSLContext().getSocketFactory());
		
		https.setRequestMethod("POST");

		// TODO To be removed later
		https.setRequestProperty("user", config.getUserName());

		String userPassword = config.getUserName() + ":" + config.getPassword();
		String encoded = Base64.getEncoder().encodeToString(userPassword.getBytes());
		https.setRequestProperty("Authorization", "Basic " + encoded);

		https.setDoInput(true);
		https.setDoOutput(true);
		
		return https;
    }

	   
    /**
     * Create new account
     * 
     * @return password
     */
    public String AccountCreate() throws IOException {
    	HttpsURLConnection https = getHttpsURLConnection("AccountCreate");
		AccountCreateRequest request = new AccountCreateRequest();
		request.setNodeName(config.getUserName());
		AccountCreateResponse response = sendRequest(https, request, AccountCreateResponse.class);
		return response.getPassword();
    }
    
    public AccountState accountActivate() throws IOException {
		HttpsURLConnection https = getHttpsURLConnection("AccountActivate");
		AccountActivateRequest request = new AccountActivateRequest();
		request.setDescription(config.getDescription());
		AccountActivateResponse response = sendRequest(https, request, AccountActivateResponse.class);
		controllerVersion = response.getVersion();
		return response.getAccountState();
    }
	
	public void registerService(String name, Map<String, String> properties) throws IOException {
		HttpsURLConnection https = getHttpsURLConnection("ServiceRegister");
		ServiceRegisterRequest request = new ServiceRegisterRequest();
		request.setName(name);
		request.setProperties(properties);
		sendRequest(https, request, ServiceRegisterResponse.class);
	}

	public Service[] lookupService(String name) throws IOException {
		HttpsURLConnection https = getHttpsURLConnection("ServiceLookup");
		ServiceLookupRequest request = new ServiceLookupRequest();
		request.setName(name);
		ServiceLookupResponse response = sendRequest(https, request, ServiceLookupResponse.class);
		return response.getServices();
	}

	public String getAccessSecret(String peerNodeName) throws IOException  {
		HttpsURLConnection https = getHttpsURLConnection("AccessSecret");
		AccessSecretRequest request = new AccessSecretRequest();
		request.setPeerNodeName(peerNodeName);
		AccessSecretResponse response = sendRequest(https, request, AccessSecretResponse.class);
		return response.getSecret();
	}

	public boolean isAuthorized(String requestNodeName, String serviceName, String operation) throws IOException {
		HttpsURLConnection https = getHttpsURLConnection("Authorization");
		AuthorizationRequest request = new AuthorizationRequest();
		request.setRequestNodeName(requestNodeName);
		request.setServiceName(serviceName);
		
		request.setServiceOperation(operation);
		
		AuthorizationResponse response = sendRequest(https, request, AuthorizationResponse.class);
		return (response.getAuthorization() == Authorization.PERMIT);
	}
	
	public String getControllerVersion() {
		return controllerVersion;
	}
}

