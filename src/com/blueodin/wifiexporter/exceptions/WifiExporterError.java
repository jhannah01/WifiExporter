package com.blueodin.wifiexporter.exceptions;

import android.util.Log;

public class WifiExporterError extends Exception {
	private static final long serialVersionUID = -8384855427177024630L;

	public WifiExporterError(String LogTag, String detailMessage) {
		super(detailMessage);
		Log.e(LogTag, detailMessage);
	}
}