package com.rackspacecloud.client.cloudfiles.android;

import roboguice.inject.InjectView;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;

public class AuthFragment extends RoboSherlockFragment {
	
	private AuthFragActionListener mListener;
	
	@InjectView(R.id.edit_username)
	private EditText mEditUsername;
	@InjectView(R.id.edit_passwd)
	private EditText mEditPasswd;
	@InjectView(R.id.check_save_pass)
	private CheckBox mCheckSavePass;
	@InjectView(R.id.check_auto_login)
	private CheckBox mCheckAutoLogIn;
	@InjectView(R.id.cancel)
	private Button mButtonCancel;
	@InjectView(R.id.confirm)
	private Button mButtonConfirm;
	
	private String mSavedUsername, mSavedPasswd;
	private boolean mDoSavePass, mDoAutoLogIn;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = (AuthFragActionListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.frag_auth, null);
	}

	public void setSavedFields(String username, String passwd,
			boolean savePass, boolean autoLogIn) {
		mSavedUsername = username;
		mSavedPasswd = passwd;
		mDoSavePass = savePass;
		mDoAutoLogIn = autoLogIn;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		mCheckSavePass.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					mCheckAutoLogIn.setChecked(false);
				}
			}
		});
		mCheckAutoLogIn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mCheckSavePass.setChecked(true);
				}
			}
		});
		
		mEditUsername.setText(mSavedUsername);
		mEditPasswd.setText(mSavedPasswd);
		mCheckSavePass.setChecked(mDoSavePass);
		mCheckAutoLogIn.setChecked(mDoAutoLogIn);
		
		mButtonCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mListener.onQuit();
			}
		});
		mButtonConfirm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mListener.onLogIn(mEditUsername.getText().toString(),
						mEditPasswd.getText().toString(), 
						mCheckSavePass.isChecked(), 
						mCheckAutoLogIn.isChecked());
			}
		});
	}

	public interface AuthFragActionListener {
		public void onLogIn(String username, String passwd,
				boolean savePass, boolean autoLogIn);
		public void onQuit();
	}
}
