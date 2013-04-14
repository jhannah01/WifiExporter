package com.blueodin.wifiexporter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import com.google.android.gms.maps.GoogleMap;

import java.util.List;

public class SettingsActivity extends PreferenceActivity {
	private static final boolean ALWAYS_SIMPLE_PREFS = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
		
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			getActionBar().setDisplayHomeAsUpEnabled(true);
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
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		setupSimplePreferencesScreen();
	}

	@SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}

		addPreferencesFromResource(R.xml.pref_general);

		PreferenceCategory fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_maps);
		
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_maps);

		//bindPreferenceSummaryToValue(findPreference("example_text"));
	}

	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);
	}

	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		if (!isSimplePreferences(this))
			loadHeadersFromResource(R.xml.pref_headers, target);
	}

	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

			} else
				preference.setSummary(stringValue);
			
			return true;
		}
	};

	private static void bindPreferenceSummaryToValue(Preference preference) {
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
						""));
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment {
		public static final String KEY_PREF_USE_WIGLEWIFI = "use_wiglewifi";
		public static final String KEY_PREF_SERVICE_URI = "service_uri";
		
		public static boolean useWigleWifi(SharedPreferences sharedPrefs) {
			return sharedPrefs.getBoolean(KEY_PREF_USE_WIGLEWIFI, true);
		}
		
		public static String getServiceUri(SharedPreferences sharedPrefs, Resources res) {
			return sharedPrefs.getString(KEY_PREF_SERVICE_URI, res.getString(R.string.pref_default_service_uri));
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);

			//bindPreferenceSummaryToValue(findPreference("example_text"));
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class MapsPreferenceFragment extends
			PreferenceFragment {
		public static final String KEY_PREF_MAP_TYPE = "map_type";
		public static final String KEY_PREF_USE_MY_LOCATION = "use_my_location";
		
		public static int getMapType(SharedPreferences sharedPrefs) {
			String mapType = sharedPrefs.getString(KEY_PREF_MAP_TYPE, "normal");
			
			if(mapType.equals("hybrid"))
				return GoogleMap.MAP_TYPE_HYBRID;
			else if(mapType.equals("satellite"))
				return GoogleMap.MAP_TYPE_SATELLITE;
			else if(mapType.equals("terrain"))
				return GoogleMap.MAP_TYPE_TERRAIN;
			
			return GoogleMap.MAP_TYPE_NORMAL;
		}
		
		public static boolean useMyLocation(SharedPreferences sharedPrefs) {
			return sharedPrefs.getBoolean(KEY_PREF_USE_MY_LOCATION, true);
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_maps);
		}
	}
}
