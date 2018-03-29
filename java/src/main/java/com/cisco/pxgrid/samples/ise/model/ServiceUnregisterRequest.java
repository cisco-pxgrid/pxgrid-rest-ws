package com.cisco.pxgrid.samples.ise.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The request to unregister a service from the controller.
 * 
 * @since 2.0
 */
@XmlRootElement
public class ServiceUnregisterRequest {
	private String name;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
