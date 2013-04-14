package com.blueodin.wifiexporter.adapters;

import java.util.ArrayList;
import java.util.List;

import com.blueodin.wifiexporter.R;
import com.blueodin.wifiexporter.types.WigleNetwork;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NetworkAdapter extends ArrayAdapter<WigleNetwork> {
	private LayoutInflater mInflater;

	public NetworkAdapter(Context context) {
		this(context, new ArrayList<WigleNetwork>());
	}
	
	public NetworkAdapter(Context context, List<WigleNetwork> networks) {
		super(context, R.layout.simple_network_row);
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		addAll(networks);
	}
	
	public void update(List<WigleNetwork> networks) {
		clear();
		addAll(networks);
		notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		WigleNetwork network = getItem(position);
		View rootView = mInflater.inflate(R.layout.simple_network_row, parent, false);
		
		((ImageView)rootView.findViewById(R.id.network_security)).setImageResource(network.getSecurityIcon());
		((TextView)rootView.findViewById(R.id.network_bssid)).setText(network.bssid);
		((TextView)rootView.findViewById(R.id.network_ssid)).setText(network.ssid.isEmpty() ? "N/A" : network.ssid);
		((TextView)rootView.findViewById(R.id.network_lasttime)).setText(network.getFormattedLastTime());
		((TextView)rootView.findViewById(R.id.network_avglevel)).setText(String.format("%d dBm", network.getAverageLevel()));
		
		return rootView;
	}
}
