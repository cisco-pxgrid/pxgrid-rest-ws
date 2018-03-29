package com.cisco.pxgrid.model;

import java.util.Map;

/**
 * @since 2.0
 */
public class Service {
	private String name;
	private String nodeName;
	private Map<String, String> properties;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
}

