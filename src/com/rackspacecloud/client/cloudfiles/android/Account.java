package com.rackspacecloud.client.cloudfiles.android;

import org.json.JSONException;
import org.json.JSONObject;

public class Account {

	public String username, passwd;
	
	public Account(String username, String passwd) {
		this.username = username;
		this.passwd = passwd;
	}
	
	public static Account parseAccount(String string) {
		try {
			JSONObject object = new JSONObject(string);
			String username = object.getString("username");
			String passwd = object.getString("passwd");
			return new Account(username, passwd);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String toString() {
		JSONObject object = new JSONObject();
		try {
			object.put("username", username);
			object.put("passwd", passwd);
			return object.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
