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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.rackspacecloud.client.cloudfiles.FilesContainer;

public class ContainerListFragment extends SherlockListFragment {

	private List<FilesContainer> mContainers;
	
	private ContainerListActionListener mListener;
	private OnItemClickListener mOnItemClickListener;
	private OnItemLongClickListener mOnItemLongClickListener;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = (ContainerListActionListener) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	public void setList(List<FilesContainer> containers) {
		mContainers = containers;
		setListAdapter(new ArrayAdapter<FilesContainer>(
				getActivity(), 
				android.R.layout.simple_list_item_1, 
				containers));
		mOnItemClickListener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mListener.onOpenContainer(mContainers.get(position).getName());
			}
		};
		getListView().setOnItemClickListener(mOnItemClickListener);
		mOnItemLongClickListener = new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				final FilesContainer container = mContainers.get(position);
				new AlertDialog.Builder(getActivity())
						.setItems(new String[]{getString(R.string.delete)},
								new OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
								case 0:
									new AlertDialog.Builder(getActivity())
											.setMessage(getString(R.string.confirm_delete_, 
													container.getName()))
											.setNegativeButton(R.string.cancel, null)
											.setPositiveButton(R.string.confirm, 
													new OnClickListener() {
												
												@Override
												public void onClick(DialogInterface dialog, int which) {
													mListener.onDeleteContainer(container.getName());
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
		mListener.onChangeMainTitle();
		getListView().setOnItemClickListener(mOnItemClickListener);
		getListView().setOnItemLongClickListener(mOnItemLongClickListener);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.frag_container_list, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_create_container:
			final View view = LayoutInflater.from(getActivity())
					.inflate(R.layout.dialog_edit, null);
			new AlertDialog.Builder(getActivity())
					.setTitle(R.string.create_container)
					.setView(view)
					.setNegativeButton(R.string.cancel, null)
					.setPositiveButton(R.string.confirm, new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String containerName = ((TextView) view.findViewById(R.id.dialog_edit))
									.getText().toString();
							if (containerName.length() > 0) {
								mListener.onCreateContainer(containerName);
							}
						}
					})
					.show();
			return true;
		case R.id.menu_logout:
			mListener.onLogOut();
			return true;
		default:
			return false;
		}
	}

	public interface ContainerListActionListener {
		public void onChangeMainTitle();
		public void onOpenContainer(String containerName);
		public void onDeleteContainer(String containerName);
		public void onCreateContainer(String containerName);
		public void onLogOut();
	}
}
