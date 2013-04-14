package com.blueodin.wifiexporter.types;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

public class WigleLocation {
	private static final String TAG = "WigleLocation";
	public String bssid;
	public int level;
	public float lat;
	public float lon;
	public int altitude;
	public int accuracy;
	public long time;

	public WigleLocation(Cursor data) {
		this.bssid = data.getString(data.getColumnIndex("bssid"));
		this.level = data.getInt(data.getColumnIndex("level"));
		this.lat = data.getFloat(data.getColumnIndex("lat"));
		this.lon = data.getFloat(data.getColumnIndex("lon"));
		this.altitude = data.getInt(data.getColumnIndex("altitude"));
		this.accuracy = data.getInt(data.getColumnIndex("accuracy"));
		this.time = data.getLong(data.getColumnIndex("time"));
	}

	public static class LocationColumns implements BaseColumns {
		public static String bssid = "bssid";
		public static String level = "level";
		public static String lat = "lat";
		public static String lon = "lon";
		public static String altitude = "altitude";
		public static String accuracy = "accuracy";
		public static String time = "time";
		public static String _ID = LocationColumns.bssid;

		public static String[] getColumnNames() {
			return new String[] { bssid, level, lat, lon, altitude, accuracy,
					time };
		}
	}

	public String getTimestamp() {
		return SimpleDateFormat.getDateTimeInstance().format(
				new Date(this.time));
	}

	public String getMapMarkerIcon() {
		if (this.accuracy > 1500)
			return "Grey Circle.png";
		else if (this.accuracy > 1000)
			return "Red Circle.png";
		else if (this.accuracy > 300)
			return "Brown Circle.png";
		else if (this.accuracy > 50)
			return "Orange Circle.png";

		return "Green Circle.png";
	}
	
	public String getFormattedTime() {
		return SimpleDateFormat.getDateTimeInstance().format(
				new Date(this.time));
	}


	@Override
	public String toString() {
		return (String
				.format(" - Level: %d dBm\n -  Altitude: %d m\n - Accuracy: %d m\n - Timestamp: %s",
						this.level,
						this.altitude,
						this.accuracy,
						this.getFormattedTime()));
	}

	public JSONObject toJson() {
		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put("bssid", this.bssid);
			jsonObject.put("level", this.level);
			jsonObject.put("lat", this.lat);
			jsonObject.put("lon", this.lon);
			jsonObject.put("altitude", this.altitude);
			jsonObject.put("accuracy", this.accuracy);
			jsonObject.put("time", this.time);
		} catch (JSONException ex) {
			Log.e(TAG, "JSON Parser error: " + ex.getMessage());
		}

		return jsonObject;
	}

	public int getSignalIcon(Context context) {
		String assetName = "Green Circle.png";
		
		if(this.level < -95)
			assetName = "Grey Circle.png";
		else if(this.level < -90)
			assetName = "Red Circle.png";
		else if(this.level < -80)
			assetName = "Brown Circle.png";
		else if(this.level < -75)
			assetName = "Orange Circle.png";
		
		return context.getResources().getIdentifier(assetName, "drawable", context.getPackageName());
	}
}
