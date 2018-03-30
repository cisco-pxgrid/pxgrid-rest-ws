package com.cisco.pxgrid.samples.ise.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The response corresponding to ServiceRegisterRequest
 * 
 * @since 2.0
 */
@XmlRootElement
public class ServiceRegisterResponse {
	private String id;
	private long reregisterTimeMillis;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getReregisterTimeMillis() {
		return reregisterTimeMillis;
	}

	public void setReregisterTimeMillis(long reregisterTimeMillis) {
		this.reregisterTimeMillis = reregisterTimeMillis;
	}
}
