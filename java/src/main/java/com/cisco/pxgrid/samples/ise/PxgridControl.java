package com.cisco.pxgrid.samples.ise;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Base64;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.pxgrid.samples.ise.model.AccessSecretRequest;
import com.cisco.pxgrid.samples.ise.model.AccessSecretResponse;
import com.cisco.pxgrid.samples.ise.model.AccountActivateRequest;
import com.cisco.pxgrid.samples.ise.model.AccountActivateResponse;
import com.cisco.pxgrid.samples.ise.model.AccountCreateRequest;
import com.cisco.pxgrid.samples.ise.model.AccountCreateResponse;
import com.cisco.pxgrid.samples.ise.model.AccountState;
import com.cisco.pxgrid.samples.ise.model.Authorization;
import com.cisco.pxgrid.samples.ise.model.AuthorizationRequest;
import com.cisco.pxgrid.samples.ise.model.AuthorizationResponse;
import com.cisco.pxgrid.samples.ise.model.Service;
import com.cisco.pxgrid.samples.ise.model.ServiceLookupRequest;
import com.cisco.pxgrid.samples.ise.model.ServiceLookupResponse;
import com.cisco.pxgrid.samples.ise.model.ServiceRegisterRequest;
import com.cisco.pxgrid.samples.ise.model.ServiceRegisterResponse;
import com.cisco.pxgrid.samples.ise.model.ServiceReregisterRequest;
import com.cisco.pxgrid.samples.ise.model.ServiceReregisterResponse;
import com.cisco.pxgrid.samples.ise.model.ServiceUnregisterRequest;
import com.cisco.pxgrid.samples.ise.model.ServiceUnregisterResponse;
import com.google.gson.Gson;

/**
 * Using HTTPS for pxGrid control
 */
public class PxgridControl {
	private static Logger logger = LoggerFactory.getLogger(PxgridControl.class);
	private SampleConfiguration config;
	private String controllerVersion;

	public PxgridControl(SampleConfiguration config) {
		this.config = config;
	}

	private <T> T sendRequest(HttpsURLConnection https, Object request, Class<T> responseClass) throws IOException {
		https.setRequestProperty("Content-Type", "application/json");
		https.setRequestProperty("Accept", "application/json");

		Gson gson = new Gson();
		
		// Getting urlSuffix for logging purpose
		String path = https.getURL().getPath();
		String urlSuffix = path.substring(path.lastIndexOf('/') + 1);
		logger.info("{} request={}", urlSuffix, gson.toJson(request));
		
		OutputStreamWriter out = new OutputStreamWriter(https.getOutputStream());
		gson.toJson(request, out);
		out.flush();
		InputStreamReader in = new InputStreamReader(https.getInputStream());
		T response = gson.fromJson(in, responseClass);
		
		logger.info("{} response={}", urlSuffix, gson.toJson(response));

		return response;
	}

	private HttpsURLConnection getHttpsURLConnection(String urlSuffix) throws IOException {
		String url = "https://" + config.getHostnames()[0] + ":8910/pxgrid/control/" + urlSuffix;
		URL conn = new URL(url);
		HttpsURLConnection https = (HttpsURLConnection) conn.openConnection();

		// SSL and Auth
		https.setSSLSocketFactory(config.getSSLContext().getSocketFactory());

		https.setRequestMethod("POST");

		String userPassword = config.getNodeName() + ":" + config.getPassword();
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
	public String accountCreate() throws IOException {
		HttpsURLConnection https = getHttpsURLConnection("AccountCreate");
		AccountCreateRequest request = new AccountCreateRequest();
		request.setNodeName(config.getNodeName());
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

	public ServiceRegisterResponse serviceRegister(String name, Map<String, String> properties) throws IOException {
		HttpsURLConnection https = getHttpsURLConnection("ServiceRegister");
		ServiceRegisterRequest request = new ServiceRegisterRequest();
		request.setName(name);
		request.setProperties(properties);
		return sendRequest(https, request, ServiceRegisterResponse.class);
	}
	
	public void serviceReregister(String id) throws IOException {
		HttpsURLConnection https = getHttpsURLConnection("ServiceReregister");
		ServiceReregisterRequest request = new ServiceReregisterRequest();
		request.setId(id);
		sendRequest(https, request, ServiceReregisterResponse.class);
	}
	
	public void unregisterService(String id) throws IOException {
		HttpsURLConnection https = getHttpsURLConnection("ServiceUnregister");
		ServiceUnregisterRequest request = new ServiceUnregisterRequest();
		request.setId(id);
		sendRequest(https, request, ServiceUnregisterResponse.class);
	}

	public Service[] serviceLookup(String name) throws IOException {
		HttpsURLConnection https = getHttpsURLConnection("ServiceLookup");
		ServiceLookupRequest request = new ServiceLookupRequest();
		request.setName(name);
		ServiceLookupResponse response = sendRequest(https, request, ServiceLookupResponse.class);
		return response.getServices();
	}

	public String getAccessSecret(String peerNodeName) throws IOException {
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
