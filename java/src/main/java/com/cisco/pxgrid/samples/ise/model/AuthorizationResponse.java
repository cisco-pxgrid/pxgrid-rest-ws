package com.cisco.pxgrid.samples.ise.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The response corresponding to the AuthorizationRequest
 * 
 * @since 2.0
 */
@XmlRootElement
public class AuthorizationResponse {
	private Authorization authorization;

	public Authorization getAuthorization() {
		return authorization;
	}
	public void setAuthorization(Authorization authorization) {
		this.authorization = authorization;
	}
}
