package com.blueodin.wifiexporter.exceptions;


public class IllegalNetworkType extends WifiExporterError {
	private static final long serialVersionUID = -2249243222630212282L;

	public IllegalNetworkType(String LogTag, String detailMessage) {
		super(LogTag, detailMessage);
	}
}