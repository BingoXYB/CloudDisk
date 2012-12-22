package com.rackspacecloud.client.cloudfiles.android;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DataHelper {

	private SharedPreferences mPrefs;
	
	@Inject
	public DataHelper(Context context) {
		mPrefs = context.getSharedPreferences("account", Context.MODE_PRIVATE);
	}
	
	public void setAccount(Account account) {
		mPrefs.edit().putString("account", account.toString()).commit();
	}
	
	public Account getAccount() {
		return Account.parseAccount(mPrefs.getString("account", ""));
	}
	
	public void setAutoLogIn(boolean autoLogIn) {
		mPrefs.edit().putBoolean("auto_login", autoLogIn).commit();
	}
	
	public boolean doAutoLogIn() {
		return mPrefs.getBoolean("auto_login", false);
	}
	
	public void clear() {
		mPrefs.edit().clear().commit();
	}
}
