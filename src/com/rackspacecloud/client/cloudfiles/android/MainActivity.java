package com.rackspacecloud.client.cloudfiles.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.util.RoboAsyncTask;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.actionbarsherlock.view.Window;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesConstants;
import com.rackspacecloud.client.cloudfiles.FilesContainer;
import com.rackspacecloud.client.cloudfiles.FilesObject;
import com.rackspacecloud.client.cloudfiles.android.AuthFragment.AuthFragActionListener;
import com.rackspacecloud.client.cloudfiles.android.ContainerFragment.ContainerActionListener;
import com.rackspacecloud.client.cloudfiles.android.ContainerListFragment.ContainerListActionListener;

public class MainActivity extends RoboSherlockFragmentActivity 
		implements AuthFragActionListener, ContainerListActionListener,
				ContainerActionListener{
	
	private static final int REQUEST_FILE_CHOOSER = 1;
	
	@Inject
	private DataHelper mDataHelper;
	@Inject
	private FragmentManager mFragManager;
	
	private AuthFragment mFragAuth;
	private ContainerListFragment mFragContainerList;
	private Map<String, ContainerFragment> mMapContainerFrags;
	
	private FilesClient mClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        
        mMapContainerFrags = new HashMap<String, ContainerFragment>();
        
        showLogIn();
        Account acc = mDataHelper.getAccount();
        if (acc != null) {
        	boolean autoLogIn = mDataHelper.doAutoLogIn();
        	mFragAuth.setSavedFields(acc.username, acc.passwd, true, autoLogIn);
        	if (autoLogIn) {
        		onLogIn(acc.username, acc.passwd, true, true);
        	}
        }
    }
    
    private void showErrorMessage(int errorType, Exception e) {
    	Toast.makeText(this, getString(errorType)+": "+e, Toast.LENGTH_LONG).show();
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_FILE_CHOOSER:
			if (resultCode == RESULT_OK) {
				uploadFile(getTitle().toString(),
						data.getStringExtra(FileChooserActivity.KEY_RESULT));
			}
			break;
		default:
			break;
		}
	}
	
    // ++++++++++++++++++++ Auth ++++++++++++++++++++
    
    private void showLogIn() {
    	if (mFragAuth == null) {
    		mFragAuth = new AuthFragment();
    	}
    	mFragManager.beginTransaction().replace(android.R.id.content, mFragAuth).commit();
    }
    
    @Override
	public void onLogIn(final String username, final String passwd,
			final boolean savePass, final boolean autoLogIn) {
    	
		new RoboAsyncTask<Boolean>(this) {

			@Override
			protected void onPreExecute() throws Exception {
				setSupportProgressBarIndeterminateVisibility(true);
			}

			@Override
			public Boolean call() throws Exception {
				mClient = new FilesClient(username, passwd, 
						"http://192.168.100.22:5000/v2.0/tokens");
				return mClient.login();
			}

			@Override
			protected void onException(Exception e) throws RuntimeException {
				showErrorMessage(R.string.login_failed, e);
			}

			@Override
			protected void onFinally() throws RuntimeException {
				setSupportProgressBarIndeterminateVisibility(false);
			}

			@Override
			protected void onSuccess(Boolean result) throws Exception {
				if (result) {
					if (savePass) {
						mDataHelper.setAccount(new Account(username, passwd));
						mDataHelper.setAutoLogIn(autoLogIn);
					}
					showContainers();
				} else {
					showErrorMessage(R.string.login_failed, null);
				}
			}
			
		}.execute();
	}

	@Override
	public void onQuit() {
		finish();
	}

	
    // -------------------- Auth --------------------

	// ++++++++++++++++++++ Container List ++++++++++++++++++++
	
	private void showContainers() {
		if (mFragContainerList == null) {
			mFragContainerList = new ContainerListFragment();
		}
		mFragManager.beginTransaction().replace(android.R.id.content, mFragContainerList)
			.commit();
		updateContainers();
	}
	
	private void updateContainers() {
		new RoboAsyncTask<Void>(this) {
			
			private List<FilesContainer> containers;

			@Override
			public Void call() throws Exception {
				containers = mClient.listContainers();
				return null;
			}

			@Override
			protected void onException(Exception e) throws RuntimeException {
				showErrorMessage(R.string.list_containers_failed, e);
			}

			@Override
			protected void onSuccess(Void t) throws Exception {
				mFragContainerList.setList(containers);
			}
			
		}.execute();
	}
	
	@Override
	public void onChangeMainTitle() {
		Account account = mDataHelper.getAccount();
		if (account != null) {
			setTitle(account.username);
		}
	}

	@Override
	public void onOpenContainer(String containerName) {
		showContainer(containerName);
	}

	@Override
	public void onDeleteContainer(final String containerName) {
		new RoboAsyncTask<Boolean>(this) {

			@Override
			public Boolean call() throws Exception {
				return mClient.deleteContainer(containerName);
			}

			@Override
			protected void onException(Exception e) throws RuntimeException {
				showErrorMessage(R.string.delete_container_failed, e);
			}

			@Override
			protected void onFinally() throws RuntimeException {
				setSupportProgressBarIndeterminateVisibility(false);
			}

			@Override
			protected void onPreExecute() throws Exception {
				setSupportProgressBarIndeterminateVisibility(true);
			}

			@Override
			protected void onSuccess(Boolean result) throws Exception {
				if (result) {
					updateContainers();
				} else {
					showErrorMessage(R.string.delete_container_failed, null);
				}
			}
			
		}.execute();
	}

	@Override
	public void onCreateContainer(final String containerName) {
		new RoboAsyncTask<Void>(this) {

			@Override
			public Void call() throws Exception {
				mClient.createContainer(containerName);
				return null;
			}

			@Override
			protected void onException(Exception e) throws RuntimeException {
				showErrorMessage(R.string.create_container_failed, e);
			}

			@Override
			protected void onFinally() throws RuntimeException {
				setSupportProgressBarIndeterminateVisibility(false);
			}

			@Override
			protected void onPreExecute() throws Exception {
				setSupportProgressBarIndeterminateVisibility(true);
			}

			@Override
			protected void onSuccess(Void t) throws Exception {
				updateContainers();
			}
			
		}.execute();
	}

	@Override
	public void onLogOut() {
		mDataHelper.clear();
		mFragManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		showLogIn();
	}

	
	// -------------------- Container List --------------------

	// ++++++++++++++++++++ Container ++++++++++++++++++++
	
	private void showContainer(String containerName) {
		if (!mMapContainerFrags.containsKey(containerName)) {
			ContainerFragment fragment = new ContainerFragment();
			mMapContainerFrags.put(containerName, fragment);
		}
		mFragManager.beginTransaction()
			.replace(android.R.id.content, mMapContainerFrags.get(containerName))
			.addToBackStack(null).commit();
		updateContainer(containerName);
	}
	
	private void updateContainer(final String containerName) {
		new RoboAsyncTask<Void>(this) {
			
			private List<FilesObject> objects;

			@Override
			public Void call() throws Exception {
				objects = mClient.listObjects(containerName);
				for (FilesObject object : objects) {
					object.getMetaData();
				}
				return null;
			}

			@Override
			protected void onException(Exception e) throws RuntimeException {
				showErrorMessage(R.string.open_container_failed, e);
			}

			@Override
			protected void onSuccess(Void t) throws Exception {
				mMapContainerFrags.get(containerName).setList(containerName, objects);
			}
			
		}.execute();
	}
	
	@Override
	public void onChangeContainerTitle(String containerName) {
		setTitle(containerName);
	}

	@Override
	public void onUploadFile(String containerName) {
		Intent intent = new Intent(this, FileChooserActivity.class);
		startActivityForResult(intent, REQUEST_FILE_CHOOSER);
	}
	
	private void uploadFile(final String containerName, final String filePath) {
		new RoboAsyncTask<String>(this) {

			@Override
			public String call() throws Exception {
				File file = new File(filePath);
				String name = file.getName();
				String extention = "";
				int dotLocation = name.lastIndexOf('.');
				if (dotLocation > 0) {
					extention = name.substring(dotLocation + 1);
				}
				String mimeType = FilesConstants.getMimetype(extention);
				return mClient.storeObject(containerName, file, mimeType);
			}

			@Override
			protected void onException(Exception e) throws RuntimeException {
				showErrorMessage(R.string.upload_file_failed, e);
			}

			@Override
			protected void onFinally() throws RuntimeException {
				setSupportProgressBarIndeterminateVisibility(false);
			}

			@Override
			protected void onPreExecute() throws Exception {
				setSupportProgressBarIndeterminateVisibility(true);
			}

			@Override
			protected void onSuccess(String result) throws Exception {
				if (result == null) {
					showErrorMessage(R.string.upload_file_failed, null);
				} else {
					Toast.makeText(MainActivity.this, 
							getString(R.string.file_uploaded_in_, containerName), 
							Toast.LENGTH_LONG).show();
					updateContainer(containerName);
				}
			}
			
		}.execute();
	}

	@Override
	public void onDownloadFile(final String containerName, final FilesObject filesObject,
			final boolean open) {
		new RoboAsyncTask<Void>(this) {

			private File destFile;
			
			@Override
			public Void call() throws Exception {
				InputStream input = mClient.getObjectAsStream(containerName, 
						filesObject.getName());
				File root = Environment.getExternalStorageDirectory();
				File appRoot = new File(root, "CloudDisk");
				if (!appRoot.exists()) appRoot.mkdir();
				File containerDir = new File(appRoot, containerName);
				if (!containerDir.exists()) containerDir.mkdir();
				destFile = new File(containerDir, filesObject.getName());
				FileOutputStream output = new FileOutputStream(destFile);
				byte[] data = new byte[1024];
				int count = 0;
				while ((count = input.read(data)) != -1) {
					output.write(data, 0, count);
				}
				output.flush();
				output.close();
				input.close();
				return null;
			}

			@Override
			protected void onFinally() throws RuntimeException {
				setSupportProgressBarIndeterminateVisibility(false);
			}

			@Override
			protected void onPreExecute() throws Exception {
				setSupportProgressBarIndeterminateVisibility(true);
			}

			@Override
			protected void onSuccess(Void t) throws Exception {
				if (open) {
					String mimeType = filesObject.getMimeTypeUnsafe();
					if ("application/octet-stream".equals(mimeType)) {
						if (filesObject.getName().endsWith(".apk")) {
							mimeType = "application/vnd.android.package-archive";
						} else {
							mimeType = "application/*";
						}
					}
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(destFile), mimeType);
					try {
						startActivity(intent);
					} catch (ActivityNotFoundException e) {
						Toast.makeText(MainActivity.this, R.string.cannot_open_filetype,
								Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(MainActivity.this, 
							getString(R.string.file_saved_in_, destFile.toString()), 
							Toast.LENGTH_LONG).show();
				}
			}
			
		}.execute();
	}

	@Override
	public void onDeleteFile(final String containerName, final String fileName) {
		new RoboAsyncTask<Void>(this) {

			@Override
			public Void call() throws Exception {
				mClient.deleteObject(containerName, fileName);
				return null;
			}

			@Override
			protected void onException(Exception e) throws RuntimeException {
				showErrorMessage(R.string.delete_file_failed, e);
			}

			@Override
			protected void onFinally() throws RuntimeException {
				setSupportProgressBarIndeterminateVisibility(false);
			}

			@Override
			protected void onPreExecute() throws Exception {
				setSupportProgressBarIndeterminateVisibility(true);
			}

			@Override
			protected void onSuccess(Void result) throws Exception {
				updateContainer(containerName);
			}
			
		}.execute();
	}
	
	// -------------------- Container --------------------
}
