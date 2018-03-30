package com.cisco.pxgrid.samples.ise.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The request to reregister a service on the controller.
 * 
 * @since 2.0
 */
@XmlRootElement
public class ServiceReregisterRequest {
	private String id;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
