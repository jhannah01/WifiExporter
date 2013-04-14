package com.blueodin.wifiexporter.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.blueodin.wifiexporter.WifiExporterApplication;
import com.blueodin.wifiexporter.adapters.NetworkAdapter;
import com.blueodin.wifiexporter.types.WigleDbManager;
import com.blueodin.wifiexporter.types.WigleNetwork;

public class NetworkListFragment extends ListFragment implements WifiExporterApplication.OnWigleDbUpdate {
	private static final String STATE_ACTIVATED_POSITION = "activated_position";
	private Callbacks mCallbacks = sDummyCallbacks;
	private int mActivatedPosition = ListView.INVALID_POSITION;
	private NetworkAdapter mNetworkAdapter;
	private WifiExporterApplication mApplication;
	
	public interface Callbacks {
		public void onNetworkSelected(WigleNetwork network);
	}

	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onNetworkSelected(WigleNetwork network) {
		}
	};

	public NetworkListFragment() { }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = (WifiExporterApplication)getActivity().getApplication();
		mNetworkAdapter = new NetworkAdapter(getActivity(), mApplication.getNetworks());
		setListAdapter(mNetworkAdapter);
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
		mCallbacks.onNetworkSelected(mNetworkAdapter.getItem(position));
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
		mNetworkAdapter.update(manager.getNetworks());
		setActivatedPosition(ListView.INVALID_POSITION);
	}
}