package com.blueodin.wifiexporter.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

public class WigleDbManager {
	private static final String TAG = "WigleDbManager";
	private String mDatabaseFilename;
	
	private HashMap<String, List<WigleLocation>> mLocationMap = new HashMap<String, List<WigleLocation>>();
	private HashMap<String, WigleNetwork> mNetworkMap = new HashMap<String, WigleNetwork>();
	private List<WigleLocation> mLocations = new ArrayList<WigleLocation>();
	
	public WigleDbManager() {
		this(Environment.getExternalStorageDirectory().getAbsolutePath() + "/wiglewifi/wiglewifi.sqlite");
	}
	
	public WigleDbManager(String filename) {
		mDatabaseFilename = filename;
		parseDatabase();
	}
	
	public HashMap<String, List<WigleLocation>> getLocationMap() {
		return mLocationMap;
	}
	
	public HashMap<String, WigleNetwork> getNetworkMap() {
		return mNetworkMap;
	}
	
	public List<WigleLocation> getLocations() {
		return mLocations;
	}
	
	public List<WigleLocation> getLocations(WigleNetwork network) {
		return getLocations(network.bssid);
	}
	
	public List<WigleLocation> getLocations(String bssid) {
		if(!mLocationMap.containsKey(bssid))
			return new ArrayList<WigleLocation>();
		
		return mLocationMap.get(bssid);
	}
	
	public List<WigleNetwork> getNetworks() {
		return new ArrayList<WigleNetwork>(mNetworkMap.values());
	}
	
	public WigleNetwork getNetwork(String bssid) {
		if(!mNetworkMap.containsKey(bssid))
			return null;
		
		return mNetworkMap.get(bssid);
	}
	
	public boolean parseDatabase() {
		if(!parseLocations())
			return false;
		
		if(!parseNetworks())
			return false;
		
		return true;
	}
	
	private boolean parseNetworks() {
		SQLiteDatabase wigleDatabase = null;
		Cursor data = null;
		
		mNetworkMap.clear();
		
		try {
			wigleDatabase = SQLiteDatabase.openDatabase(mDatabaseFilename, null, SQLiteDatabase.OPEN_READONLY);
			
			data = wigleDatabase.query("network", WigleNetwork.NetworkColumns.getColumnNames(), "ssid != ''", null, null, null, WigleNetwork.NetworkColumns.lasttime + " DESC");
			
			if(!data.moveToFirst()) {
				Log.e(TAG, "Error reading the 'network' table from the WigleWifi database.");
				return false;
			}
			
			while(!data.isAfterLast()) {
				WigleNetwork net = new WigleNetwork(data);
				mNetworkMap.put(net.bssid, net);
				
				if(mLocationMap.containsKey(net.bssid))
					net.updateAverageLevel(mLocationMap.get(net.bssid));
				
				data.moveToNext();
			}
		} finally {
			if(data != null)
				data.close();
			
			if(wigleDatabase != null)
				wigleDatabase.close();
		}
		
		Log.i(TAG, String.format("Parsed %d networks from the database", mNetworkMap.size()));
		return true;
	}
	
	private boolean parseLocations() {
		SQLiteDatabase wigleDatabase = null;
		Cursor data = null;
		
		mLocations.clear();
		mLocationMap.clear();
		
		try {
			wigleDatabase = SQLiteDatabase.openDatabase(mDatabaseFilename, null, SQLiteDatabase.OPEN_READONLY);
			
			data = wigleDatabase.query("location", WigleLocation.LocationColumns.getColumnNames(), null, null, null, null, WigleLocation.LocationColumns.time + " DESC");
			
			while(!data.moveToFirst()) {
				Log.e(TAG, "Error reading the 'location' table from WigleWifi database.");
				return false;
			}
			
			WigleLocation location;
			
			while(!data.isAfterLast()) {
				location = new WigleLocation(data);
				mLocations.add(location);
				
				if(!mLocationMap.containsKey(location.bssid))
					mLocationMap.put(location.bssid, new ArrayList<WigleLocation>());
				
				mLocationMap.get(location.bssid).add(location);
				
				data.moveToNext();
			}
		} finally {
			if(data != null)
				data.close();
			
			if(wigleDatabase != null)
				wigleDatabase.close();
		}
		
		Log.i(TAG, String.format("Parsed %d locations from the database", mLocations.size()));
		
		return true;
	}
}
