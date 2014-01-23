package com.nirab.susu;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity {

	private String dataFolder;
	ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mProgressDialog = new ProgressDialog(MainActivity.this);
		mProgressDialog.setMessage("In order to use map and routing offline we need to download awesome OpenStreetMap data to your phone, This may take a while depending on your Internet Connection, Please have Patience");
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setCancelable(false);
		
		dataFolder = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/susu/data/";
		if (!new File(dataFolder).exists()) {
			new File(dataFolder).mkdirs();}
		

		File filecheck = new File(Environment
				.getExternalStorageDirectory().getPath()
				+ "/susu/data/kathmandu-gh/kathmandu.map");

		if (!filecheck.exists()) {
			startDownload();
		}
		else {
		

		Thread timer = new Thread() {
			public void run() {
				try {
					sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					
					Intent openMain = new Intent(MainActivity.this,
							ChooseActivity.class);
					startActivity(openMain);

				}
			}

		};

		timer.start();
		
		}
		
	}

	private void startDownload() {
		final DownloadTask downloadTask = new DownloadTask(MainActivity.this);
		downloadTask
				.execute("https://dl.dropboxusercontent.com/u/95497883/kathmandu-gh.zip");

		mProgressDialog
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						downloadTask.cancel(true);
					}
				});

	}

	// AsyncTask to download a file
	private class DownloadTask extends AsyncTask<String, Integer, String> {

		private Context context;

		public DownloadTask(Context context) {
			this.context = context;
		}

		@Override
		protected String doInBackground(String... sUrl) {
			// take CPU lock to prevent CPU from going off if the user
			// presses the power button during download
			PowerManager pm = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wl = pm.newWakeLock(
					PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
			wl.acquire();

			try {
				InputStream input = null;
				OutputStream output = null;
				HttpURLConnection connection = null;
				try {
					URL url = new URL(sUrl[0]);
					connection = (HttpURLConnection) url.openConnection();
					connection.connect();

					// expect HTTP 200 OK, so we don't mistakenly save error
					// report
					// instead of the file
					if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
						return "Server returned HTTP "
								+ connection.getResponseCode() + " "
								+ connection.getResponseMessage();

					File filecheck = new File(Environment
							.getExternalStorageDirectory().getPath()
							+ "/susu/data/kathmandu-gh/kathmandu.map");

					if (filecheck.exists()) {
						Log.i("File Exists", "Code Gets here, file exists");
						return "exists";
						// if (connection.getResponseCode() ==
						// HttpURLConnection.HTTP_NOT_MODIFIED) {
						//
						// return null;
						// }
					}

					// this will be useful to display download percentage
					// might be -1: server did not report the length
					int fileLength = connection.getContentLength();
					Log.i("Length", String.valueOf(fileLength));

					// download the file
					input = connection.getInputStream();
					Log.i("OutPutDir", Environment
							.getExternalStorageDirectory().getPath()
							+ "/susu/data" + "/kathmandu-gh.zip");
					output = new FileOutputStream(Environment
							.getExternalStorageDirectory().getPath()
							+ "/susu/data" + "/kathmandu-gh.zip");


					byte data[] = new byte[4096];
					long total = 0;
					int count;
					while ((count = input.read(data)) != -1) {
						// allow canceling with back button
						if (isCancelled())
							return null;
						total += count;
						// publishing the progress....
						if (fileLength > 0) // only if total length is known
							publishProgress((int) (total * 100 / fileLength));
						output.write(data, 0, count);
					}
				} catch (Exception e) {
					return e.toString();
				} finally {
					try {
						if (output != null)
							output.close();
						if (input != null)
							input.close();
					} catch (IOException ignored) {
					}

					if (connection != null)
						connection.disconnect();
				}
			} finally {
				wl.release();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			// if we get here, length is known, now set indeterminate to false
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setMax(100);
			mProgressDialog.setProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			mProgressDialog.dismiss();
			if (result != null) {
				if (result == "exists") {
					Toast.makeText(context,
							"File Already Exists and is up to date",
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(context, "Download error: " + result,
							Toast.LENGTH_LONG).show();
				}
			}

			else {
				Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT)
						.show();
				unpackZip(Environment.getExternalStorageDirectory().getPath()
						+ "/susu/data/", "kathmandu-gh.zip");
				Intent openMain = new Intent(MainActivity.this,
						ChooseActivity.class);
				startActivity(openMain);
			}
		}
	}

	// To unzip files

	private boolean unpackZip(String path, String zipname) {
		InputStream is;
		ZipInputStream zis;
		try {
			String filename;
			is = new FileInputStream(path + zipname);
			zis = new ZipInputStream(new BufferedInputStream(is));
			ZipEntry ze;
			byte[] buffer = new byte[1024];
			int count;

			while ((ze = zis.getNextEntry()) != null) {

				filename = ze.getName();

				// Need to create directories if not exists, or
				// it will generate an Exception...
				if (ze.isDirectory()) {
					File fmd = new File(path + filename);
					fmd.mkdirs();
					continue;
				}

				FileOutputStream fout = new FileOutputStream(path + filename);

				while ((count = zis.read(buffer)) != -1) {
					fout.write(buffer, 0, count);
				}

				fout.close();
				zis.closeEntry();
			}

			zis.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
