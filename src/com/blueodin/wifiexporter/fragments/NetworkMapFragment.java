package com.blueodin.wifiexporter.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.blueodin.wifiexporter.R;
import com.blueodin.wifiexporter.SettingsActivity;
import com.blueodin.wifiexporter.WifiExporterApplication;
import com.blueodin.wifiexporter.types.LocationMapMarker;
import com.blueodin.wifiexporter.types.WigleLocation;
import com.blueodin.wifiexporter.types.WigleNetwork;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NetworkMapFragment extends MapFragment implements
	GoogleMap.InfoWindowAdapter, GoogleMap.OnMarkerClickListener, 
	SharedPreferences.OnSharedPreferenceChangeListener {
	public static final String ARG_NETWORK_BSSID = "network";
	private WifiExporterApplication mApplication;

	private HashMap<String, WigleNetwork> mNetworksMap = new HashMap<String, WigleNetwork>();
	private HashMap<String, List<WigleLocation>> mLocationsMap = new HashMap<String, List<WigleLocation>>();

	private HashMap<String, Marker> mNetworkMarkersMap = new HashMap<String, Marker>();
	private HashMap<String, List<LocationMapMarker>> mLocationMarkersMap = new HashMap<String, List<LocationMapMarker>>();

	private GoogleMap mGoogleMap;
	private SharedPreferences mSharedPreferences;
	private int mMapType = GoogleMap.MAP_TYPE_NORMAL;

	public NetworkMapFragment() {
	}

	public static NetworkMapFragment buildFragment(WigleNetwork network) {
		return buildFragment(network.bssid);
	}

	public static NetworkMapFragment buildFragment(String bssid) {
		Bundle args = new Bundle();
		args.putString(ARG_NETWORK_BSSID, bssid);
		NetworkMapFragment f = new NetworkMapFragment();
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mApplication = ((WifiExporterApplication) getActivity()
				.getApplication());

		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mMapType = SettingsActivity.MapsPreferenceFragment.getMapType(mSharedPreferences); 
		
		Bundle args = getArguments();
		if ((args != null) && (args.containsKey(ARG_NETWORK_BSSID))) {
			String bssid = args.getString(ARG_NETWORK_BSSID);
			addNetwork(mApplication.getNetwork(bssid));
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
		
		if(mNetworkMarkersMap.size() > 0)
			recenterMap();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mGoogleMap = getMap();
		mGoogleMap.setIndoorEnabled(true);
		
		mGoogleMap.setMapType(mMapType);
		mGoogleMap.setMyLocationEnabled(SettingsActivity.MapsPreferenceFragment.useMyLocation(mSharedPreferences));
				
		mGoogleMap.setInfoWindowAdapter(this);
		
		centerOnMyLocation();
	}

	private void addNetworkMarkers() {
		//clearNetworkMarkers();

		for (WigleNetwork network : mNetworksMap.values())
			addNetworkMarkers(network);

		//recenterMap();
	}
	
	private void addNetworkMarkers(String bssid) {
		addNetworkMarkers(mNetworksMap.get(bssid));
	}
	
	private void addNetworkMarkers(WigleNetwork network) {
		Marker marker = mGoogleMap.addMarker(new MarkerOptions()
		.icon(BitmapDescriptorFactory.defaultMarker(network
				.getMarkerIcon()))
				.snippet(network.toString())
				.title(network.bssid)
				.position(new LatLng(network.lastlat, network.lastlon)));

		mNetworkMarkersMap.put(network.bssid, marker);
		
		addLocationMarkers(network.bssid);
	}
	
	private void addLocationMarkers(String bssid) {
		clearLocationMarkers(bssid);
		
		if(!mLocationMarkersMap.containsKey(bssid))
			mLocationMarkersMap.put(bssid, new ArrayList<LocationMapMarker>());
		
		for (WigleLocation location : mLocationsMap.get(bssid)) {
			LocationMapMarker locationMarker = new LocationMapMarker(location, mGoogleMap, getResources(), 
					true, mNetworkMarkersMap.get(bssid).getPosition(), true);
			mLocationMarkersMap.get(bssid).add(locationMarker);
		}
	}

	private void clearNetworkMarkers() {
		clearLocationMarkers();
		
		for (Marker marker : mNetworkMarkersMap.values())
			marker.remove();
		
		mNetworkMarkersMap.clear();
	}
	
	private void clearNetworkMarkers(String bssid) {
		clearLocationMarkers(bssid);
		
		if(mNetworkMarkersMap.containsKey(bssid))
			mNetworkMarkersMap.get(bssid).remove();
	}
	
	private void clearLocationMarkers() {
		for (List<LocationMapMarker> markers : mLocationMarkersMap.values()) {
			for(LocationMapMarker marker : markers)
				marker.remove();
		}
		
		mLocationMarkersMap.clear();
	}
	
	private void clearLocationMarkers(String bssid) {
		if(mLocationMarkersMap.containsKey(bssid)) {
			List<LocationMapMarker> locationMarkers = mLocationMarkersMap.get(bssid);
			
			for(LocationMapMarker marker : locationMarkers)
				marker.remove();
			
			locationMarkers.clear();
		}
	}


	public void centerOnMyLocation() {
		Location location = mGoogleMap.getMyLocation();
		if (location == null) {
			LocationManager locationManager = ((LocationManager) getActivity()
					.getSystemService(Context.LOCATION_SERVICE));
			location = locationManager
					.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		}

		if (location != null) {
			mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(
					location.getLatitude(), location.getLongitude())));
			mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(12), 500, null);
		}
	}

	public void recenterMap() {
		if (mNetworkMarkersMap.size() < 1) {
			centerOnMyLocation();
			return;
		}

		final LatLngBounds.Builder markerBounds = LatLngBounds.builder();

		for (Marker marker : mNetworkMarkersMap.values())
			markerBounds.include(marker.getPosition());

		try {
			mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
					markerBounds.build(), 250), 500, null);
		} catch (IllegalStateException ex) {
			final View mapView = this.getView();
			if (mapView.getViewTreeObserver().isAlive()) {
				mapView.getViewTreeObserver().addOnGlobalLayoutListener(
						new ViewTreeObserver.OnGlobalLayoutListener() {
							@SuppressWarnings("deprecation")
							@SuppressLint("NewApi")
							@Override
							public void onGlobalLayout() {
								if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
									mapView.getViewTreeObserver()
											.removeGlobalOnLayoutListener(this);
								else
									mapView.getViewTreeObserver()
											.removeOnGlobalLayoutListener(this);

								mGoogleMap.animateCamera(CameraUpdateFactory
										.newLatLngBounds(markerBounds.build(),
												250), 500, null);
							}
						});
			}
		}
	}

	public void addNetwork(WigleNetwork network) {
		addNetwork(network.bssid);
	}

	public void addNetwork(String bssid) {
		mNetworksMap.put(bssid, mApplication.getNetwork(bssid));
		mLocationsMap.put(bssid, mApplication.getLocations(bssid));

		addNetworkMarkers(bssid);
		
		recenterMap();
	}

	public void addAllNetworks() {
		clearNetworkMarkers();

		mNetworksMap = mApplication.getNetworkMap();
		mLocationsMap = mApplication.getLocationMap();

		addNetworkMarkers();
		
		recenterMap();
	}

	public void removeNetwork(WigleNetwork network) {
		removeNetwork(network.bssid);
	}

	public void removeNetwork(String bssid) {
		clearNetworkMarkers(bssid);

		if (mLocationsMap.containsKey(bssid))
			mLocationsMap.remove(bssid);
		
		if (mNetworksMap.containsKey(bssid))
			mNetworksMap.remove(bssid);
	}
	
	public void removeAllNetworks() {
		clearNetworkMarkers();
		
		mLocationsMap.clear();
		mNetworkMarkersMap.clear();
	}
	
	public void setMapType(int mapType) {
		mGoogleMap.setMapType(mapType);
	}
	
	public void setUseMyLocation(boolean value) {
		mGoogleMap.setMyLocationEnabled(value);
	}
	
	@Override
	public View getInfoContents(Marker marker) {
		View infoView = ((LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.info_window, null);
		String markerTitle = marker.getTitle();
		String title = markerTitle;
		WigleNetwork network;
		
		if(markerTitle.matches("([0-9a-fA-F]{2}:?){6}")) {			
			network = mApplication.getNetwork(markerTitle);
			title = (network.ssid.isEmpty() ? network.bssid + " [No SSID]" : network.ssid);
		} else if (markerTitle.matches("(([0-9a-fA-F]{2}:?){6})-(\\d+)")) {
			String bssid = markerTitle.substring(0, markerTitle.indexOf('-'));
			long time = Long.parseLong(markerTitle.substring(markerTitle.indexOf('-')+1));
			
			network = mApplication.getNetwork(bssid);
			title = (network.ssid.isEmpty() ? network.bssid + " [No SSID]" : network.ssid);
			
			for(WigleLocation location : mLocationsMap.get(bssid)) {
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

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals(SettingsActivity.MapsPreferenceFragment.KEY_PREF_MAP_TYPE))
			setMapType(SettingsActivity.MapsPreferenceFragment.getMapType(sharedPreferences));
		else if(key.equals(SettingsActivity.MapsPreferenceFragment.KEY_PREF_USE_MY_LOCATION))
			setUseMyLocation(SettingsActivity.MapsPreferenceFragment.useMyLocation(sharedPreferences));
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		/*if(!marker.getTitle().matches("([0-9a-fA-F]{2}:?){6}"))
			return false;
		
		String bssid = marker.getTitle();
		
		if(mLocationMarkersMap.containsKey(bssid) && (mLocationMarkersMap.get(bssid).size() > 0)) {
			clearLocationMarkers(bssid);
			return true;
		}
		
		addLocationMarkers(bssid);
		
		recenterMap();
		*/
		return false;
	}
}