package com.cisco.pxgrid.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is the request to determine if the request node has
 * permission to access the operation of the service.
 * 
 * This is part of pxGrid Authorization.
 * 
 * @since 2.0
 */
@XmlRootElement
public class AuthorizationRequest {
	private String requestNodeName;
	private String serviceName;
	private String serviceOperation;
	
	public String getRequestNodeName() {
		return requestNodeName;
	}
	public void setRequestNodeName(String requestNodeName) {
		this.requestNodeName = requestNodeName;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getServiceOperation() {
		return serviceOperation;
	}
	public void setServiceOperation(String serviceOperation) {
		this.serviceOperation = serviceOperation;
	}
}
