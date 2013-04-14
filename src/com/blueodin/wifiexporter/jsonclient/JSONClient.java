package com.blueodin.wifiexporter.jsonclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.blueodin.wifiexporter.types.WigleLocation;
import com.blueodin.wifiexporter.types.WigleNetwork;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

public class JSONClient {
	private static final String TAG = "JSONClient";
	private String mServiceUri;

	public static class JSONUploadResult {
		private String mResult;
		private String mMessage;
		
		private JSONUploadResult(String result, String message) {
			mResult = result;
			mMessage = message;
		}
		
		public static JSONUploadResult buildFromException(String errorType, Exception exception) {
			return new JSONUploadResult("failure", String.format("%s Error: %s", errorType, exception.getMessage()));
		}
		
		public JSONUploadResult(JsonReader jsonReader) {
			try {
				jsonReader.beginObject();
				while(jsonReader.hasNext()) {
					String name = jsonReader.nextName();
					if(name.equals("result")) {
						if(jsonReader.peek() == JsonToken.NULL)
							jsonReader.skipValue();
						else
							mResult = jsonReader.nextString();
					} else if(name.equals("message")) {
	
						if(jsonReader.peek() == JsonToken.NULL)
							jsonReader.skipValue();
						else
							mMessage = jsonReader.nextString();
					}
				}
			} catch(IOException ex) {
				mResult = "failure";
				mMessage = "Error parsing JSON response: " + ex.getMessage();
				Log.e(TAG, mMessage);
			}
		}
		
		public String getResult() {
			return mResult;
		}
		
		public String getMessage() {
			return mMessage;
		}
		
		public boolean isSuccess() {
			return mResult.equalsIgnoreCase("success");
		}
		
		@Override
		public String toString() {
			return String.format("Result: %s (Message: %s)", mResult, mMessage);
		}
	}
	
	public JSONClient(String serviceUri) {
		mServiceUri = serviceUri;
	}

	public JSONUploadResult uploadData(List<WigleNetwork> networks,
			List<WigleLocation> locations) {
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonNetworksArray = new JSONArray();
		JSONArray jsonLocationsArray = new JSONArray();
		
		for (WigleLocation location : locations) {
			jsonLocationsArray.put(location.toJson());
		}
		
		for (WigleNetwork network : networks) {
			jsonNetworksArray.put(network.toJson());
		}
		
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams();
			HttpPost post = new HttpPost(mServiceUri);
			
			jsonObj.put("version", 1);
			jsonObj.put("networks", jsonNetworksArray);
			jsonObj.put("locations", jsonLocationsArray);
			
			post.setEntity(new StringEntity(jsonObj.toString()));
			
			HttpResponse response = httpClient.execute(post);
			
			InputStream inputStream = response.getEntity().getContent();
			JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream)));
			
			return new JSONUploadResult(jsonReader);
		} catch (JSONException ex) {
			Log.e(TAG, "JSON Parser Error: " + ex.getMessage());
			return JSONUploadResult.buildFromException("JSON Parser", ex);
		} catch (UnsupportedEncodingException ex) {
			Log.e(TAG, "Unsupported Encoding Error: " + ex.getMessage());
			return JSONUploadResult.buildFromException("Unsupported Encoding", ex);
		} catch (ClientProtocolException ex) {
			Log.e(TAG, "Client Protocol Error: " + ex.getMessage());
			return JSONUploadResult.buildFromException("Client Protocol", ex);
		} catch (IOException ex) {
			Log.e(TAG, "Read/Write Error: " + ex.getMessage());
			return JSONUploadResult.buildFromException("Read/Write", ex);
		}
	}
}
