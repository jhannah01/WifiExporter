package com.blueodin.wifiexporter.jsonclient;

import android.os.AsyncTask;

public class JSONUploadTask extends AsyncTask<JSONUploadTaskParams, Void, JSONClient.JSONUploadResult> {
	@Override
	protected JSONClient.JSONUploadResult doInBackground(JSONUploadTaskParams... params) {
		JSONUploadTaskParams taskParams = params[0];
		
		JSONClient jsonClient = new JSONClient(taskParams.getServiceUri());
		
		return jsonClient.uploadData(taskParams.getNetworks(), taskParams.getLocations());
	}

}
