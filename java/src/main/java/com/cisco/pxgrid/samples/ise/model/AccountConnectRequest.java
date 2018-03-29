package com.cisco.pxgrid.samples.ise.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The AccountConnectRequest is used by client to provide description
 * and the desired groups to participate. 
 * 
 * This is required for new account to perform at least once.
 * The new account in INIT state will move to PENDING state.
 * If password authentication was used, password will not be changeable anymore.
 * 
 * @since 2.0
 * @deprecated
 */
@XmlRootElement
public class AccountConnectRequest {
	private String description;
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Deprecated
	private String[] groups;
	@Deprecated
	public String[] getGroups() {
		return groups;
	}
	@Deprecated
	public void setGroups(String[] groups) {
		this.groups = groups;
	}
}
