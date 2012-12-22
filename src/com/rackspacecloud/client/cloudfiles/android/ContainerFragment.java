package com.rackspacecloud.client.cloudfiles.android;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;
import com.rackspacecloud.client.cloudfiles.FilesObject;

public class ContainerFragment extends RoboSherlockListFragment {

	private String mFilesContainerName;
	private List<FilesObject> mObjects;
	
	private ContainerActionListener mListener;
	private OnItemLongClickListener mOnItemLongClickListener;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = (ContainerActionListener) activity;
	}

	public void setList(String containerName, List<FilesObject> objects) {
		mFilesContainerName = containerName;
		mObjects = objects;
		mListener.onChangeContainerTitle(containerName);
		setListAdapter(new FileListAdapter());
		mOnItemLongClickListener = new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				final FilesObject filesObject = mObjects.get(position);
				new AlertDialog.Builder(getActivity())
						.setItems(new String[]{getString(R.string.download),
								getString(R.string.download_and_open),
								getString(R.string.delete)}, new OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
								case 0:
									new AlertDialog.Builder(getActivity())
											.setMessage(getString(R.string.confirm_download_,
													filesObject.getName()))
											.setNegativeButton(R.string.cancel, null)
											.setPositiveButton(R.string.confirm,
													new OnClickListener() {
												
												@Override
												public void onClick(DialogInterface dialog, int which) {
													mListener.onDownloadFile(mFilesContainerName,
															filesObject, false);
												}
											})
											.show();
									break;
								case 1:
									new AlertDialog.Builder(getActivity())
											.setMessage(getString(R.string.confirm_download_and_open_,
													filesObject.getName()))
											.setNegativeButton(R.string.cancel, null)
											.setPositiveButton(R.string.confirm,
													new OnClickListener() {
												
												@Override
												public void onClick(DialogInterface dialog, int which) {
													mListener.onDownloadFile(mFilesContainerName,
															filesObject, true);
												}
											})
											.show();
									break;
								case 2:
									new AlertDialog.Builder(getActivity())
										.setMessage(getString(R.string.confirm_delete_,
												filesObject.getName()))
										.setNegativeButton(R.string.cancel, null)
										.setPositiveButton(R.string.confirm,
												new OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
												mListener.onDeleteFile(mFilesContainerName,
														filesObject.getName());
											}
										})
										.show();
									break;
								default:
									break;
								}
							}
						})
						.show();
				return false;
			}
		};
		getListView().setOnItemLongClickListener(mOnItemLongClickListener);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mListener.onChangeContainerTitle(mFilesContainerName);
		getListView().setOnItemLongClickListener(mOnItemLongClickListener);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.frag_container, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_upload_file:
			mListener.onUploadFile(mFilesContainerName);
			return true;
		case R.id.menu_logout:
			mListener.onLogOut();
			return true;
		default:
			return false;
		}
	}

	public interface ContainerActionListener {
		public void onChangeContainerTitle(String containerName);
		public void onUploadFile(String containerName);
		public void onDownloadFile(String containerName, FilesObject filesObject, boolean open);
		public void onDeleteFile(String containerName, String fileName);
		public void onLogOut();
	}
	
	private class FileListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mObjects.size();
		}

		@Override
		public Object getItem(int position) {
			return mObjects.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_file, null);
			FilesObject object = mObjects.get(position);
			((TextView) convertView.findViewById(R.id.file_name)).setText(object.getName());
			((TextView) convertView.findViewById(R.id.file_size)).setText(object.getSizeStringUnsafe());
			((TextView) convertView.findViewById(R.id.file_date)).setText(object.getLastModifiedUnsafe());
			return convertView;
		}
		
	}
}
