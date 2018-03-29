package com.cisco.pxgrid.samples.ise.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is for creating password-based account.
 * 
 * @since 1.0
 */
@XmlRootElement
public class AccountCreateRequest {
	private String nodeName;

	public String getNodeName() {
		return nodeName;
	}
	
	/**
	 * This field is for specifying a unique node name to be created.
	 * 
	 * @since 2.0
	 * @param nodeName
	 */
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	@Deprecated
	private String userName;

	@Deprecated
	public String getUserName() {
		return userName;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
