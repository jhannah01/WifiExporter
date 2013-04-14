package com.blueodin.wifiexporter;

import android.app.Application;

import com.blueodin.wifiexporter.types.WigleDbManager;
import com.blueodin.wifiexporter.types.WigleLocation;
import com.blueodin.wifiexporter.types.WigleNetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WifiExporterApplication extends Application {
	private WigleDbManager mWigleDbManager;
	private List<OnWigleDbUpdate> mOnUpdateCallbacks = new ArrayList<OnWigleDbUpdate>();
	
	public interface OnWigleDbUpdate {
		public void wigleDbUpdate(WigleDbManager manager);
	}
	
	public WifiExporterApplication() {
		super();
		mWigleDbManager = new WigleDbManager();
	}
	
	public List<WigleNetwork> getNetworks() {
		return mWigleDbManager.getNetworks();
	}
	
	public WigleNetwork getNetwork(String bssid) {
		return mWigleDbManager.getNetwork(bssid);
	}
	
	public HashMap<String, WigleNetwork> getNetworkMap() {
		return mWigleDbManager.getNetworkMap();
	}
	
	public List<WigleLocation> getLocations() {
		return mWigleDbManager.getLocations();
	}
	
	public List<WigleLocation> getLocations(String bssid) {
		return mWigleDbManager.getLocations(bssid);
	}
	
	public List<WigleLocation> getLocations(WigleNetwork network) {
		return mWigleDbManager.getLocations(network);
	}

	public HashMap<String, List<WigleLocation>> getLocationMap() {
		return mWigleDbManager.getLocationMap();
	}
	
	public void parseWigleDb() {
		mWigleDbManager.parseDatabase();
		
		for(OnWigleDbUpdate callback : mOnUpdateCallbacks)
			callback.wigleDbUpdate(mWigleDbManager);
	}

	public void registerWigleDbUpdateCallback(OnWigleDbUpdate callback) {
		this.mOnUpdateCallbacks.add(callback);
	}
	
	public void unregisterWigleDbUpdateCallback(OnWigleDbUpdate callback) {
		mOnUpdateCallbacks.remove(callback);
	}
}