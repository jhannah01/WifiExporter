package com.blueodin.wifiexporter;

import com.blueodin.wifiexporter.WifiExporterApplication.OnWigleDbUpdate;
import com.blueodin.wifiexporter.jsonclient.JSONUploadTask;
import com.blueodin.wifiexporter.jsonclient.JSONUploadTaskParams;
import com.blueodin.wifiexporter.jsonclient.JSONClient.JSONUploadResult;
import com.blueodin.wifiexporter.types.WigleDbManager;
import com.blueodin.wifiexporter.adapters.ExpandableNetworkAdapter;
import com.blueodin.wifiexporter.fragments.NetworkMapFragment;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnWigleDbUpdate {
	protected static final String TAG = "MainActivity";

	private WifiExporterApplication mApplication;

	private ExpandableListView mNetworkList;
	private ExpandableNetworkAdapter mListAdapter;
	private NetworkMapFragment mNetworkMap;
	private SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mApplication = ((WifiExporterApplication) getApplication());
		
		PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
		PreferenceManager.setDefaultValues(this, R.xml.pref_maps, false);

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		setContentView(R.layout.activity_main);

		mNetworkMap = (NetworkMapFragment) getFragmentManager().findFragmentById(R.id.network_map_fragment);
		
		mListAdapter = new ExpandableNetworkAdapter(this);
		mListAdapter.update(mApplication.getNetworks(), mApplication.getLocationMap());

		mNetworkList = ((ExpandableListView) findViewById(R.id.network_list));
		mNetworkList.setAdapter(mListAdapter);

		mNetworkList
				.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
					@Override
					public void onGroupExpand(int groupPosition) {
						mNetworkMap.addNetwork(mListAdapter.getNetworkFromGroup(groupPosition));

						if (mNetworkMap.isHidden())
							getFragmentManager().beginTransaction()
									.show(mNetworkMap).commit();
					}
				});

		mNetworkList
				.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
					@Override
					public void onGroupCollapse(int groupPosition) {
						mNetworkMap.removeNetwork(mListAdapter.getNetworkFromGroup(groupPosition));
					}
				});

	}

	@Override
	protected void onResume() {
		super.onResume();
		mApplication.registerWigleDbUpdateCallback(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mApplication.unregisterWigleDbUpdateCallback(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_toggle_map:
			toggleMap();
			return true;

		case R.id.menu_parse_db:
			mApplication.parseWigleDb();
			return true;

		case R.id.menu_network_map:
			startActivity(new Intent(this, NetworkMapActivity.class));
			return true;

		case R.id.menu_upload:
			uploadDatabase();
			return true;

		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;

		case R.id.menu_exit:
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void wigleDbUpdate(WigleDbManager manager) {
		mListAdapter.update(manager.getNetworks(), manager.getLocationMap());
	}

	private void uploadDatabase() {
		(new JSONUploadTask() {
			@Override
			protected void onPostExecute(JSONUploadResult result) {
				if(result.isSuccess()) {
					Log.i(TAG, "Successfully uploaded results: '" + result.getMessage() + "'");
					Toast.makeText(MainActivity.this, "Successfully uploaded results", Toast.LENGTH_SHORT).show();
				} else {
					Log.w(TAG, "Failure uploading results: '" + result.getMessage() + "'");
					
					(new AlertDialog.Builder(MainActivity.this))
					.setTitle("WifiExporter Upload")
					.setMessage("Error uploading results: " + result.getMessage())
					.setNeutralButton("Close", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
					.show();
				}
			}
		}).execute(JSONUploadTaskParams.buildParams(mSharedPreferences, mApplication));
	}

	private void toggleMap() {
		FragmentManager fm = getFragmentManager();

		if (mNetworkMap.isHidden())
			fm.beginTransaction().show(mNetworkMap).commit();
		else
			fm.beginTransaction().hide(mNetworkMap).commit();
	}
}
