package com.github.thorbenkuck.netcom2.services;

import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.net.InetAddress;

public final class DiscoveryProcessingRequest {

	private final Header header;
	private int port;
	private InetAddress inetAddress;
	private String hubName;
	private boolean valid;

	public DiscoveryProcessingRequest(Header header) {
		this.header = header;
	}

	public void setPort(int port) {
		NetCom2Utils.parameterNotNull(port);
		this.port = port;
	}

	public void setAddress(InetAddress inetAddress) {
		NetCom2Utils.parameterNotNull(inetAddress);
		this.inetAddress = inetAddress;
	}

	public void setHubName(String hubName) {
		NetCom2Utils.parameterNotNull(hubName);
		this.hubName = hubName;
	}

	public boolean isValid() {
		return valid;
	}

	public void invalidate() {
		valid = false;
	}

	public Header header() {
		return header;
	}

	ServiceHubLocation toServiceHubLocation() {
		return new ServiceHubLocation(port, inetAddress, hubName);
	}
}