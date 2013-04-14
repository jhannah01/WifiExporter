package com.blueodin.wifiexporter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.blueodin.wifiexporter.R;
import com.blueodin.wifiexporter.types.WigleLocation;
import com.blueodin.wifiexporter.types.WigleNetwork;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ExpandableNetworkAdapter extends BaseExpandableListAdapter {
	private LinkedList<Group> mGroups = new LinkedList<Group>();
	private Context mContext;
	
	private class Group {
		private WigleNetwork mNetwork;
		private List<WigleLocation> mLocations;
		
		public Group(WigleNetwork network, List<WigleLocation> locations) {
			this.mNetwork = network;
			this.mLocations = locations;
		}
		
		public WigleNetwork getNetwork() {
			return mNetwork;
		}
		
		public List<WigleLocation> getLocations() {
			return mLocations;
		}
	}
	
	public ExpandableNetworkAdapter(Context context) {
		super();
		mContext = context;
	}
	
	@Override
	public int getGroupCount() {
		return mGroups.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return getGroup(groupPosition).getLocations().size();
	}

	@Override
	public Group getGroup(int groupPosition) {
		return mGroups.get(groupPosition);
	}
	
	public WigleNetwork getNetworkFromGroup(int groupPosition) {
		return getGroup(groupPosition).getNetwork();
	}
	
	public List<WigleLocation> getLocationsFromGroup(int groupPosition) {
		return getGroup(groupPosition).getLocations();
	}

	@Override
	public WigleLocation getChild(int groupPosition, int childPosition) {
		return getGroup(groupPosition).getLocations().get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		WigleNetwork network = getGroup(groupPosition).getNetwork();
		
		View rootView = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.network_row, parent, false);
		
		((ImageView)rootView.findViewById(R.id.network_security)).setImageResource(network.getSecurityIcon());
		((TextView)rootView.findViewById(R.id.network_bssid)).setText(network.bssid);
		((TextView)rootView.findViewById(R.id.network_ssid)).setText(network.ssid);
		((TextView)rootView.findViewById(R.id.network_avglevel)).setText(String.format("%d dBm", network.getAverageLevel()));
		((TextView)rootView.findViewById(R.id.network_location_count)).setText(String.format("%d", getGroup(groupPosition).getLocations().size()));
		
		return rootView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		WigleLocation location = getChild(groupPosition, childPosition);
		
		View rootView = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.location_row, parent, false);
		((TextView)rootView.findViewById(R.id.location_level)).setText(String.format("%d dBm", location.level));
		((TextView)rootView.findViewById(R.id.location_accuracy)).setText(String.format("%d m", location.accuracy));
		((TextView)rootView.findViewById(R.id.location_altitude)).setText(String.format("%d m", location.altitude));
		((TextView)rootView.findViewById(R.id.location_time)).setText(location.getTimestamp());
		
		return rootView;
		
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	public void update(List<WigleNetwork> networks, HashMap<String, List<WigleLocation>> locationMap) {
		mGroups.clear();
		for(WigleNetwork network : networks)
			mGroups.add(new Group(network, locationMap.get(network.bssid)));
		
		notifyDataSetChanged();
	}
}