package com.cisco.pxgrid.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is the request for getting the access secret between
 * the client and the peer node. 
 * This is part of pxGrid Authentication.
 * 
 * @since 2.0
 */
@XmlRootElement
public class AccessSecretRequest {
	private String peerNodeName;
	
	public String getPeerNodeName() {
		return peerNodeName;
	}
	public void setPeerNodeName(String peerNodeName) {
		this.peerNodeName = peerNodeName;
	}
}
