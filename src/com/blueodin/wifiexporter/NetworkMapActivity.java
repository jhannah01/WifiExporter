package com.blueodin.wifiexporter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.blueodin.wifiexporter.fragments.NetworkListFragment;
import com.blueodin.wifiexporter.types.LocationMapMarker;
import com.blueodin.wifiexporter.types.WigleDbManager;
import com.blueodin.wifiexporter.types.WigleLocation;
import com.blueodin.wifiexporter.types.WigleNetwork;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NetworkMapActivity extends Activity implements
		NetworkListFragment.Callbacks, WifiExporterApplication.OnWigleDbUpdate,
		OnMarkerClickListener, OnSharedPreferenceChangeListener, InfoWindowAdapter {
	private GoogleMap mGoogleMap;
	private HashMap<String, Marker> mNetworkMarkers = new HashMap<String, Marker>();
	private NetworkListFragment mNetworkListFragment;
	private WifiExporterApplication mApplication;

	private List<LocationMapMarker> mLocationMarkers = new ArrayList<LocationMapMarker>();
	private SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
		PreferenceManager.setDefaultValues(this, R.xml.pref_maps, false);
		
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		setContentView(R.layout.activity_network_map);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		mNetworkListFragment = (NetworkListFragment) getFragmentManager()
				.findFragmentById(R.id.network_list_fragment);
		mNetworkListFragment.setActivateOnItemClick(true);

		mApplication = (WifiExporterApplication) getApplication();
		
		mGoogleMap = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.map)).getMap();
		
		mGoogleMap.setMapType(SettingsActivity.MapsPreferenceFragment.getMapType(mSharedPreferences));
		mGoogleMap.setMyLocationEnabled(SettingsActivity.MapsPreferenceFragment.useMyLocation(mSharedPreferences));
				
		addNetworkMarkers(mApplication.getNetworks(), false);
		
		centerOnMyLocation();
		
		mGoogleMap.setOnMarkerClickListener(this);
		mGoogleMap.setInfoWindowAdapter(this);
	}
	
	private void centerOnMyLocation() {
		Location location = mGoogleMap.getMyLocation();
		if (location == null) {
			LocationManager locationManager = ((LocationManager)getSystemService(Context.LOCATION_SERVICE));
			location = locationManager
					.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		}

		if (location != null) {
			mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(
					location.getLatitude(), location.getLongitude())));
			mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(13), 500, null);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mApplication.registerWigleDbUpdateCallback(this);
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mApplication.unregisterWigleDbUpdateCallback(this);
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
	}

	private void clearLocationMarkers() {
		for (LocationMapMarker locationMarker : mLocationMarkers)
			locationMarker.remove();
		mLocationMarkers.clear();
	}

	private void addNetworkMarkers(List<WigleNetwork> networks, boolean moveCamera) {
		LatLngBounds.Builder markerBounds = LatLngBounds.builder();

		for (WigleNetwork network : networks) {
			LatLng position = new LatLng(network.lastlat, network.lastlon);

			Marker marker = mGoogleMap.addMarker(new MarkerOptions()
					.icon(BitmapDescriptorFactory.defaultMarker(network.getMarkerIcon()))
					.snippet(network.toString())
					.title(network.bssid)
					.position(position));

			mNetworkMarkers.put(network.bssid, marker);
			markerBounds.include(position);
		}
		
		if(moveCamera) {
			mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(markerBounds.build(), 50));
			mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(14), 1500, null);
		}
	}

	@Override
	public void onNetworkSelected(WigleNetwork network) {
		mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
				network.lastlat, network.lastlon), 13));
		
		Marker marker = mNetworkMarkers.get(network.bssid);
		marker.showInfoWindow();
		onMarkerClick(marker);
	}

	@Override
	public void wigleDbUpdate(WigleDbManager manager) {
		HashMap<String,WigleNetwork> networkMap = mApplication.getNetworkMap();

		if (mLocationMarkers.size() > 0) {
			if (!networkMap.containsKey(mLocationMarkers.get(0).getLocation().bssid))
				clearLocationMarkers();
		}

		if (mNetworkMarkers.size() < 1)
			return;

		for (String bssid : mNetworkMarkers.keySet()) {
			if (!networkMap.containsKey(bssid)) {
				mNetworkMarkers.get(bssid).remove();
				mNetworkMarkers.remove(bssid);
			}
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if(!marker.getTitle().matches("([0-9a-fA-F]{2}:?){6}"))
			return false;
		
		if(mLocationMarkers.size() > 0) {
			String lastBSSID = mLocationMarkers.get(0).getLocation().bssid;
			
			clearLocationMarkers();
			
			if(lastBSSID.equals(marker.getTitle()))
				return false;
		}
		
		LatLngBounds.Builder markerBounds = LatLngBounds.builder();

		for (WigleLocation location : mApplication.getLocationMap().get(
				marker.getTitle())) {
			LocationMapMarker locationMarker = new LocationMapMarker(location,
					mGoogleMap, getResources(), true, mNetworkMarkers.get(
							marker.getTitle()).getPosition(), true);
			markerBounds.include(locationMarker.getPosition());
			mLocationMarkers.add(locationMarker);
		}

		mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 13), 1000, null);
		
		return false;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals(SettingsActivity.MapsPreferenceFragment.KEY_PREF_MAP_TYPE))
			mGoogleMap.setMapType(SettingsActivity.MapsPreferenceFragment.getMapType(sharedPreferences));
		else if(key.equals(SettingsActivity.MapsPreferenceFragment.KEY_PREF_USE_MY_LOCATION))
			mGoogleMap.setMyLocationEnabled(SettingsActivity.MapsPreferenceFragment.useMyLocation(sharedPreferences));
	}

	@Override
	public View getInfoContents(Marker marker) {
		String markerTitle = marker.getTitle();
		String title = markerTitle;
		WigleNetwork network;
		View infoView = getLayoutInflater().inflate(R.layout.info_window, null);
		
		if(markerTitle.matches("([0-9a-fA-F]{2}:?){6}")) {			
			network = mApplication.getNetwork(markerTitle);
			title = (network.ssid.isEmpty() ? network.bssid + " [No SSID]" : network.ssid);
		} else if (markerTitle.matches("(([0-9a-fA-F]{2}:?){6})-(\\d+)")) {
			String bssid = markerTitle.substring(0, markerTitle.indexOf('-'));
			long time = Long.parseLong(markerTitle.substring(markerTitle.indexOf('-')+1));
			
			network = mApplication.getNetwork(bssid);
			title = (network.ssid.isEmpty() ? network.bssid + " [No SSID]" : network.ssid);
			
			for(WigleLocation location : mApplication.getLocations(bssid)) {
				if(location.time == time) {
					title = String.format("%s : %d dBm [%d]", title, location.level, location.accuracy);
					break;
				}
			}
		}
		
		((TextView) infoView.findViewById(R.id.info_title)).setText(title);
		((TextView) infoView.findViewById(R.id.info_snippet)).setText(marker.getSnippet());
		
		return infoView;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		return null;
	}
}