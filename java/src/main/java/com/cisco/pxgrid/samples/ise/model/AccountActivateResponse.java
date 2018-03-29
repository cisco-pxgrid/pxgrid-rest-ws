package com.cisco.pxgrid.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The response for the corresponding AccountActivateRequest.
 * It returns the account state and the version of the pxGrid Controller.
 * 
 * @since 2.0
 */
@XmlRootElement
public class AccountActivateResponse {
	private AccountState accountState;
	private String version;
	
	public AccountState getAccountState() {
		return accountState;
	}
	public void setAccountState(AccountState accountState) {
		this.accountState = accountState;
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
}
