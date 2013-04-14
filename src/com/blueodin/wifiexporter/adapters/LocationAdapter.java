package com.blueodin.wifiexporter.adapters;

import java.util.ArrayList;
import java.util.List;

import com.blueodin.wifiexporter.R;
import com.blueodin.wifiexporter.types.WigleLocation;
import com.blueodin.wifiexporter.types.WigleNetwork;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LocationAdapter extends ArrayAdapter<WigleLocation> {
	private LayoutInflater mInflater;

	public LocationAdapter(Context context) {
		this(context, new ArrayList<WigleLocation>());
	}
	
	public LocationAdapter(Context context, List<WigleLocation> locations) {
		super(context, R.layout.simple_location_row);
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		addAll(locations);
	}
	
	public void update(List<WigleLocation> locations) {
		clear();
		addAll(locations);
		notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		WigleLocation location = getItem(position);
		
		View rootView = mInflater.inflate(R.layout.simple_location_row, parent, false);
		((ImageView)rootView.findViewById(R.id.image_location_signal_icon)).setImageResource(location.getSignalIcon(getContext()));
		((TextView)rootView.findViewById(R.id.text_location_bssid)).setText(location.bssid);
		((TextView)rootView.findViewById(R.id.text_location_level)).setText(String.format("%d dBm", location.level));
		((TextView)rootView.findViewById(R.id.text_location_time)).setText(location.getFormattedTime());
		((TextView)rootView.findViewById(R.id.text_location_altitude)).setText(String.format("%d m", location.altitude));
		((TextView)rootView.findViewById(R.id.text_location_accuracy)).setText(String.format("%d m", location.accuracy));
		
		return rootView;
	}
}
