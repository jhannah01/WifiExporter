package com.blueodin.wifiexporter.types;

import android.content.res.Resources;
import android.graphics.Color;

import com.blueodin.wifiexporter.R.color;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class LocationMapMarker {
	private WigleLocation mLocation;

	private LatLng mPosition;
	private Circle mCircle;
	private Marker mMarker;
	private Polyline mLocationLine;

	public LocationMapMarker(WigleLocation location, GoogleMap googleMap,
			Resources resources, boolean markerVisible, LatLng networkPosition,
			boolean lineVisible) {
		mLocation = location;
		
		mPosition = new LatLng(location.lat, location.lon);

		mCircle = googleMap.addCircle(new CircleOptions().center(mPosition)
				.radius(Math.max(1, (100 + location.level)*5))
				.fillColor(resources.getColor(color.network_circle))
				.strokeWidth(0.8f)
				.strokeColor(resources.getColor(color.network_circle_stroke))
				.visible(markerVisible));

		mMarker = googleMap.addMarker(new MarkerOptions()
				.position(mPosition)
				.title(location.bssid + "-" + location.time)
				.snippet(location.toString())
				.visible(markerVisible)
				.icon(BitmapDescriptorFactory.fromAsset(location
						.getMapMarkerIcon())));

		mLocationLine = googleMap.addPolyline(new PolylineOptions()
				.add(networkPosition)
				.add(mPosition)
				.color(Color.argb(85, 5, 5, 5))
				.width(5)
				.zIndex(1.5f)
				.visible(lineVisible));
	}

	public WigleLocation getLocation() {
		return mLocation;
	}

	public LatLng getPosition() {
		return mPosition;
	}

	public Circle getCircle() {
		return mCircle;
	}

	public Marker getMarker() {
		return mMarker;
	}

	public void remove() {
		mLocationLine.remove();
		mCircle.remove();
		mMarker.remove();
	}

	public void setLineVisiblity(boolean visible) {
		mLocationLine.setVisible(visible);
	}

	public void setMarkerVisiblity(boolean visible) {
		mCircle.setVisible(visible);
		mMarker.setVisible(visible);

	}

	public void setVisiblity(boolean visible) {
		setMarkerVisiblity(visible);
		setLineVisiblity(visible);
	}

	public boolean isMarkerVisible() {
		return mCircle.isVisible();
	}

	public boolean isLineVisible() {
		return mLocationLine.isVisible();
	}
}
