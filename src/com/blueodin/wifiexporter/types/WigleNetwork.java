package com.blueodin.wifiexporter.types;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.blueodin.wifiexporter.R;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.util.Log;

public class WigleNetwork implements Parcelable {
	private static final String TAG = "WigleNetwork";
	
	public String bssid;
	public String ssid;
	public int frequency;
	public String capabilities;
	public long lasttime;
	public float lastlat;
	public float lastlon;
	public String type;

	private int mAverageLevel = -1;

	public enum NetworkType {
		Wireless, Cellular, Unknown;

		public String getValue() {
			switch (this) {
			case Wireless:
				return "W";
			case Cellular:
				return "C";
			default:
				return "";
			}
		}

		public static NetworkType parseNetworkType(char value) {
			switch (value) {
			case 'W':
				return NetworkType.Wireless;
			case 'C':
				return NetworkType.Cellular;
			default:
				return NetworkType.Unknown;
			}
		}
	}

	public static class NetworkColumns implements BaseColumns {
		public static String bssid = "bssid";
		public static String ssid = "ssid";
		public static String frequency = "frequency";
		public static String capabilities = "capabilities";
		public static String lasttime = "lasttime";
		public static String lastlat = "lastlat";
		public static String lastlon = "lastlon";
		public static String type = "type";
		public static String _ID = NetworkColumns.bssid;
		
		public static String[] getColumnNames() {
			return new String[] {
					bssid,
					ssid,
					frequency,
					capabilities,
					lasttime,
					lastlat,
					lastlon,
					type
			};
		}
	}

	public WigleNetwork(Cursor data) {
		this.bssid = data.getString(data.getColumnIndex(NetworkColumns.bssid));
		this.ssid = data.getString(data.getColumnIndex(NetworkColumns.ssid));
		this.frequency = data.getInt(data
				.getColumnIndex(NetworkColumns.frequency));
		this.capabilities = data.getString(data
				.getColumnIndex(NetworkColumns.capabilities));
		this.lasttime = data.getLong(data
				.getColumnIndex(NetworkColumns.lasttime));
		this.lastlat = data.getFloat(data
				.getColumnIndex(NetworkColumns.lastlat));
		this.lastlon = data.getFloat(data
				.getColumnIndex(NetworkColumns.lastlon));
		this.type = data.getString(data.getColumnIndex(NetworkColumns.type));
	}

	public void updateAverageLevel(List<WigleLocation> locations) {
		int i = 0;

		if (locations.size() < 1) {
			mAverageLevel = 0;
			return;
		}

		for (WigleLocation location : locations)
			i += location.level;

		i /= locations.size();
		mAverageLevel = i;
	}
	
	public int getAverageLevel() {
		return mAverageLevel;
	}

	public String getFormattedLastTime() {
		return SimpleDateFormat.getDateTimeInstance().format(
				new Date(this.lasttime));
	}

	public List<String> getCapabilities() {
		List<String> results = new ArrayList<String>();
		String[] matches = this.capabilities.split("\\]\\[");
		for (int i = 0; i < matches.length; i++)
			results.add(matches[i].replaceAll("[\\]\\[]", ""));

		return results;
	}

	public String getFormattedCapabilities() {
		String result = "";

		for (String cap : getCapabilities())
			result = result + (result.isEmpty() ? "" : ", ") + cap;

		return result;
	}

	public int getSecurityIcon() {
		if (this.capabilities.contains("[WPA2-"))
			return R.drawable.ic_green_wifi;

		if (this.capabilities.contains("[WPA-"))
			return R.drawable.ic_orange_wifi;

		if (this.capabilities.contains("[WEP"))
			return R.drawable.ic_grey_wifi;

		return R.drawable.ic_red_wifi;
	}

	public float getMarkerIcon() {
		if (this.capabilities.contains("[WPA2-"))
			return BitmapDescriptorFactory.HUE_GREEN;

		if (this.capabilities.contains("[WPA-"))
			return BitmapDescriptorFactory.HUE_ORANGE;

		if (this.capabilities.contains("[WEP"))
			return BitmapDescriptorFactory.HUE_BLUE;

		return BitmapDescriptorFactory.HUE_RED;
	}

	@Override
	public String toString() {
		return String
				.format(" - Avg Level: %d dBm\n - Capabilities: %s\n - Frequency: %d MHz\n - Timestamp: %s",
						this.getAverageLevel(),
						this.getFormattedCapabilities(),
						this.frequency,
						this.getFormattedLastTime());
	}
	
	public JSONObject toJson() {
		JSONObject jsonObject = new JSONObject();
		
		try {
			jsonObject.put("bssid", this.bssid);
			jsonObject.put("ssid", this.ssid);
			jsonObject.put("frequency", this.frequency);
			jsonObject.put("capabilities", this.capabilities);
			jsonObject.put("lasttime", this.lasttime);
			jsonObject.put("lastlat", this.lastlat);
			jsonObject.put("lastlon", this.lastlon);
			jsonObject.put("type", this.type);
		} catch(JSONException ex) {
			Log.e(TAG, "JSON Parser error: " + ex.getMessage());
		}
		return jsonObject;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.bssid);
		dest.writeString(this.ssid);
		dest.writeInt(this.frequency);
		dest.writeString(this.capabilities);
		dest.writeLong(this.lasttime);
		dest.writeFloat(this.lastlon);
		dest.writeFloat(this.lastlat);
		dest.writeString(this.type);
	}
	
	public WigleNetwork(Parcel src) {
		this.bssid = src.readString();
		this.ssid = src.readString();
		this.frequency = src.readInt();
		this.capabilities = src.readString();
		this.lasttime = src.readLong();
		this.lastlon = src.readFloat();
		this.lastlat = src.readFloat();
		this.type = src.readString();
	}
	
	
}
