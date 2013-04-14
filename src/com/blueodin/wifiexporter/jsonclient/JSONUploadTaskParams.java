package com.blueodin.wifiexporter.jsonclient;

import android.content.SharedPreferences;

import com.blueodin.wifiexporter.SettingsActivity;
import com.blueodin.wifiexporter.WifiExporterApplication;
import com.blueodin.wifiexporter.types.WigleLocation;
import com.blueodin.wifiexporter.types.WigleNetwork;

import java.util.List;

public class JSONUploadTaskParams {
	private final String mServiceUri;
	private final List<WigleNetwork> mNetworks;
	private final List<WigleLocation> mLocations;

	public static JSONUploadTaskParams buildParams(
			SharedPreferences sharedPrefs, WifiExporterApplication application) {
		return new JSONUploadTaskParams(
				SettingsActivity.GeneralPreferenceFragment.getServiceUri(
						sharedPrefs, application.getResources()),
				application.getNetworks(), application.getLocations());
	}

	public JSONUploadTaskParams(String serviceUri, List<WigleNetwork> networks,
			List<WigleLocation> locations) {
		mServiceUri = serviceUri;
		mNetworks = networks;
		mLocations = locations;
	}

	public String getServiceUri() {
		return mServiceUri;
	}

	public List<WigleNetwork> getNetworks() {
		return mNetworks;
	}

	public List<WigleLocation> getLocations() {
		return mLocations;
	}
}