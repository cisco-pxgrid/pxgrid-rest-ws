package com.cisco.pxgrid.samples.ise.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The corresponding response for AccessSecretRequest.
 * The returned secret will be used for subsequence authentication with the provider.
 * 
 * @since 2.0
 */
@XmlRootElement
public class AccessSecretResponse {
	private String secret;
	
	public String getSecret() {
		return secret;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}
}
