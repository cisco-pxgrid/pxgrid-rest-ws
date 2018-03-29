package com.cisco.pxgrid.samples.ise.model;

/**
 * @since 2.0
 */
public enum AccountState {
	/**
	 * The internal state when the account is created, but never connected. 
	 */
	INIT,
	
	/**
	 * The state when the account is pending for approval from administrator
	 */
	PENDING,
	
	/**
	 * The state where all pxGrid control functionalities are available.
	 */
	ENABLED,
	
	/**
	 * A disabled account. Administrator can disable or enable the account.
	 */
	DISABLED;
}
