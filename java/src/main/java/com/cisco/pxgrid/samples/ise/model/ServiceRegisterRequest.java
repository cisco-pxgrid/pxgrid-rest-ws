package com.cisco.pxgrid.samples.ise.model;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @since 2.0
 */
@XmlRootElement
public class ServiceRegisterRequest {
	private String name;
	private Map<String, String> properties;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
}
