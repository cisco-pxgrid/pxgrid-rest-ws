package com.cisco.pxgrid.samples.ise.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The response corresponding to the AccountCreateRequest
 * 
 * @since 1.0
 */
@XmlRootElement
public class AccountCreateResponse {
	private String nodeName;
	private String password;

	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	@Deprecated
	private String userName;
	@Deprecated
	public String getUserName() {
		return userName;
	}
	@Deprecated
	public void setUserName(String userName) {
		this.userName = userName;
	}

}
