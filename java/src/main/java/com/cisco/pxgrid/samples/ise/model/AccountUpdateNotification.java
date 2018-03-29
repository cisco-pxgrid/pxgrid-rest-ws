package com.cisco.pxgrid.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This notification is sent when there is a change with the account.
 * 
 * @since 2.0
 */
@XmlRootElement
public class AccountUpdateNotification {
	private AccountState accountState;

	public AccountState getAccountState() {
		return accountState;
	}

	public void setAccountState(AccountState accountState) {
		this.accountState = accountState;
	}
}
