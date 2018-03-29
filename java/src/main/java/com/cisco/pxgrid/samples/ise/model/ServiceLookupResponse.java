package com.cisco.pxgrid.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The response corresponding to ServiceLookupRequest
 * 
 * @since 2.0
 */
@XmlRootElement
public class ServiceLookupResponse {
	private Service[] services;
	
	public Service[] getServices() {
		return services;
	}
	
	public void setServices(Service[] services) {
		this.services = services;
	}
}
