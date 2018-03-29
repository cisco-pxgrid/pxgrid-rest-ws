package com.cisco.pxgrid.samples.ise.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This used by client to activate account.
 * 
 * This is required for new account to perform at least once.
 * The new account in INIT state will move to PENDING state.
 * If password authentication was used, password will not be changeable anymore.
 * 
 * @since 2.0
 */
@XmlRootElement
public class AccountActivateRequest {
	private String description;
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
