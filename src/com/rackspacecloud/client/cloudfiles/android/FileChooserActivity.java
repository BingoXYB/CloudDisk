package com.rackspacecloud.client.cloudfiles.android;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;
import com.google.inject.Inject;

public class FileChooserActivity extends RoboSherlockFragmentActivity {
	
	public static final String KEY_RESULT = "file";

	@Inject
	private FragmentManager mFragManager;
	
	private File root;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			root = Environment.getExternalStorageDirectory();
			mFragManager.beginTransaction()
				.replace(android.R.id.content, new FileChooserFragment(root)).commit();
		} else {
			Toast.makeText(this, R.string.external_storage_unavailable,
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@SuppressLint("ValidFragment")
	private class FileChooserFragment extends RoboSherlockListFragment {
		
		private File mDir;
		private List<File> mList;
		
		public FileChooserFragment(File dir) {
			mDir = dir;
			
			File[] files = dir.listFiles();
			mList = new ArrayList<File>(Arrays.asList(files));
			
			List<File> removed = new ArrayList<File>();
			for (File file : mList) {
				if (file.getName().startsWith(".")) {
					removed.add(file);
				}
			}
			mList.removeAll(removed);
			removed.clear();
			
			Collections.sort(mList);
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			setTitle(mDir.toString());
			setListAdapter(new FileListAdapter());
			getListView().setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					File file = mList.get(position);
					if (file.isDirectory()) {
						mFragManager.beginTransaction().replace(android.R.id.content,
								new FileChooserFragment(file))
								.addToBackStack(null).commit();
					} else {
						Intent intent = getIntent();
						intent.putExtra(KEY_RESULT, file.getAbsolutePath());
						setResult(RESULT_OK, intent);
						finish();
					}
				}
			});
		}
		
		private class FileListAdapter extends BaseAdapter {

			@Override
			public int getCount() {
				return mList.size();
			}

			@Override
			public Object getItem(int position) {
				return mList.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				convertView = LayoutInflater.from(FileChooserActivity.this)
						.inflate(R.layout.list_item_file, null);
				File file = mList.get(position);
				((TextView) convertView.findViewById(R.id.file_name)).setText(file.getName());
				((TextView) convertView.findViewById(R.id.file_size))
						.setText(getFileSizeString(file));
				((TextView) convertView.findViewById(R.id.file_date))
						.setText(getFileLastModified(file));
				return convertView;
			}
			
			private String getFileSizeString(File file) {
				if (file.isDirectory()) return "";
				
				long kb = 1024;
		        long mb = kb*1024;
		        long gb = mb*1024;

		        long size = file.length();
		        
		            //KB
		        if (size > gb)
		            return (size/gb) + " GB";
		        else if (size > mb)
		            return (size/mb)+" MB";
		        else if (size > kb)
		            return (size/kb) +" KB";
		        else
		            return size+" Bytes";
			}
			
			private String getFileLastModified(File file) {
				return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")
					.format(new Date(file.lastModified()));
			}
		}	
	}
	
	
}
