package com.cisco.pxgrid.samples.ise.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This request is used to lookup information about a service.
 * 
 * @since 2.0
 */
@XmlRootElement
public class ServiceLookupRequest {
	private String name;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
