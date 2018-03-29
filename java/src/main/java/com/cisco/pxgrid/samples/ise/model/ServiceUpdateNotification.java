package com.cisco.pxgrid.samples.ise.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This notification is sent when there is a change with the service.
 * 
 * @since 2.0
 */
@XmlRootElement
public class ServiceUpdateNotification {
	private ServiceState serviceState;
	private Service service;
	
	public ServiceState getServiceState() {
		return serviceState;
	}
	public void setServiceState(ServiceState serviceState) {
		this.serviceState = serviceState;
	}
	public Service getService() {
		return service;
	}
	public void setService(Service service) {
		this.service = service;
	}
}
