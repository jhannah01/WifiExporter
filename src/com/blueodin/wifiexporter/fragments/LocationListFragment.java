package com.blueodin.wifiexporter.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.blueodin.wifiexporter.WifiExporterApplication;
import com.blueodin.wifiexporter.adapters.LocationAdapter;
import com.blueodin.wifiexporter.types.WigleDbManager;
import com.blueodin.wifiexporter.types.WigleLocation;

public class LocationListFragment extends ListFragment implements WifiExporterApplication.OnWigleDbUpdate {
	private static final String STATE_ACTIVATED_POSITION = "activated_position";
	private Callbacks mCallbacks = sDummyCallbacks;
	private int mActivatedPosition = ListView.INVALID_POSITION;
	private LocationAdapter mLocationAdapter;
	private WifiExporterApplication mApplication;
	
	public interface Callbacks {
		public void onLocationSelected(WigleLocation location);
	}

	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onLocationSelected(WigleLocation location) {
		}
	};

	public LocationListFragment() { }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = (WifiExporterApplication)getActivity().getApplication();
		mLocationAdapter = new LocationAdapter(getActivity(), mApplication.getLocations());
		setListAdapter(mLocationAdapter);
		mApplication.registerWigleDbUpdateCallback(this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mApplication.unregisterWigleDbUpdateCallback(this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);
		mCallbacks.onLocationSelected(mLocationAdapter.getItem(position));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	public void setActivateOnItemClick(boolean activateOnItemClick) {
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
	
	@Override
	public void wigleDbUpdate(WigleDbManager manager) {
		mLocationAdapter.update(manager.getLocations());
		setActivatedPosition(ListView.INVALID_POSITION);
	}
}