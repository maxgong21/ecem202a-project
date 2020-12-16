package org.altbeacon.beaconreference;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

/**
 *
 * @author dyoung
 * @author Matt Tyler
 */
public class MonitoringActivity extends Activity  {
	protected static final String TAG = "MonitoringActivity";
	private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
	private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);										// MAX: hides the title bar
		setContentView(R.layout.activity_monitoring);
		verifyBluetooth();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
					== PackageManager.PERMISSION_GRANTED) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
					if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
							!= PackageManager.PERMISSION_GRANTED) {
						if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
							final AlertDialog.Builder builder = new AlertDialog.Builder(this);
							builder.setTitle("This app needs background location access");
							builder.setMessage("Please grant location access so this app can detect beacons in the background.");
							builder.setPositiveButton(android.R.string.ok, null);
							builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

								@TargetApi(23)
								@Override
								public void onDismiss(DialogInterface dialog) {
									requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
											PERMISSION_REQUEST_BACKGROUND_LOCATION);
								}

							});
							builder.show();
						}
						else {
							final AlertDialog.Builder builder = new AlertDialog.Builder(this);
							builder.setTitle("Functionality limited");
							builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
							builder.setPositiveButton(android.R.string.ok, null);
							builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

								@Override
								public void onDismiss(DialogInterface dialog) {
								}

							});
							builder.show();
						}
					}
				}
			} else {
				if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
					requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
									Manifest.permission.ACCESS_BACKGROUND_LOCATION},
							PERMISSION_REQUEST_FINE_LOCATION);
				}
				else {
					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Functionality limited");
					builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
						}

					});
					builder.show();
				}

			}
		}

		// Edit IP address in the EditText view
		BeaconReferenceApplication application = ((BeaconReferenceApplication) this.getApplicationContext());
		EditText editText = (EditText)findViewById(R.id.IPaddress);
		editText.setText(application.addrString, TextView.BufferType.EDITABLE);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_REQUEST_FINE_LOCATION: {
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Log.d(TAG, "fine location permission granted");
				} else {
					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Functionality limited");
					builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
						}

					});
					builder.show();
				}
				return;
			}
			case PERMISSION_REQUEST_BACKGROUND_LOCATION: {
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Log.d(TAG, "background location permission granted");
				} else {
					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Functionality limited");
					builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons when in the background.");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
						}

					});
					builder.show();
				}
				return;
			}
		}
	}

	// Determines action when the "Create Map" button is pressed.
	public void onMappingClicked(View view) {
		Intent myIntent = new Intent(this, MapActivity.class);
		this.startActivity(myIntent);
	}

	// Determines action when the "Update" button is pressed.
	public void onUpdateClicked(View view) {
		Log.d(TAG, "on updated clicked");
		BeaconReferenceApplication application = ((BeaconReferenceApplication) this.getApplicationContext());
		EditText ipInput = (EditText) findViewById(R.id.IPaddress);
		application.addrString = ipInput.getText().toString();
	}

	// Determines action when the "Disable Monitoring" or "Re-Enable Monitoring" button is pressed.
	public void onEnableClicked(View view) {
		BeaconReferenceApplication application = ((BeaconReferenceApplication) this.getApplicationContext());
		if (BeaconManager.getInstanceForApplication(this).getMonitoredRegions().size() > 0) {
			application.disableMonitoring();																		// disables monitoring (this is function in "BeaconReferenceApplication.java'
			((Button)findViewById(R.id.enableButton)).setText("Enable Monitoring");
			((TextView)findViewById(R.id.textViewStatus)).setText("Not Monitoring");								// MAX: Changes status to "Not Monitoring"
			((RelativeLayout)findViewById(R.id.activity_monitoring_layout)).setBackgroundColor(Color.WHITE);		// MAX: Change background color of MainActivity to white
			((TextView)findViewById(R.id.textView1)).setVisibility(View.INVISIBLE);									// MAX: Make "View detected beacons" invisible
			((TextView)findViewById(R.id.detectedDevicesText)).setVisibility(View.INVISIBLE);						// MAX: Make "View detected beacons" invisible
			((TextView)findViewById(R.id.blank)).setVisibility(View.VISIBLE);						// MAX: Make "View detected beacons" invisible
			((TextView)findViewById(R.id.blank2)).setVisibility(View.VISIBLE);						// MAX: Make "View detected beacons" invisible
			((Button)findViewById(R.id.mapCreateButton)).setVisibility(View.INVISIBLE);
			((EditText)findViewById(R.id.IPaddress)).setVisibility(View.INVISIBLE);
			((Button)findViewById(R.id.updateIP)).setVisibility(View.INVISIBLE);
		}
		else {
			Log.d(TAG, "MONITORING...");				// MAX: This only prints when clicked.
			((Button)findViewById(R.id.enableButton)).setText("Disable Monitoring");
			((TextView)findViewById(R.id.textViewStatus)).setText("Monitoring");									// MAX: Changes status to "Monitoring"
			((RelativeLayout)findViewById(R.id.activity_monitoring_layout)).setBackgroundColor(Color.parseColor("#ccffcc"));	// MAX: Change background color of MainActivity to green
			((TextView)findViewById(R.id.textView1)).setVisibility(View.VISIBLE);									// MAX: Make "View detected beacons" visible.
			((TextView)findViewById(R.id.detectedDevicesText)).setVisibility(View.VISIBLE);							// MAX: Make "View detected beacons" visible.
			((TextView)findViewById(R.id.blank)).setVisibility(View.INVISIBLE);						// MAX: Make "View detected beacons" invisible
			((TextView)findViewById(R.id.blank2)).setVisibility(View.INVISIBLE);						// MAX: Make "View detected beacons" invisible
			((Button)findViewById(R.id.mapCreateButton)).setVisibility(View.VISIBLE);
			((EditText)findViewById(R.id.IPaddress)).setVisibility(View.VISIBLE);
			((Button)findViewById(R.id.updateIP)).setVisibility(View.VISIBLE);
			application.enableMonitoring();
		}

	}

    @Override
    public void onResume() {
        super.onResume();
        BeaconReferenceApplication application = ((BeaconReferenceApplication) this.getApplicationContext());
        application.setMonitoringActivity(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((BeaconReferenceApplication) this.getApplicationContext()).setMonitoringActivity(null);
    }

	private void verifyBluetooth() {

		try {
			if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Bluetooth not enabled");
				builder.setMessage("Please enable bluetooth in settings and restart this application.");
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						//finish();
			            //System.exit(0);
					}
				});
				builder.show();
			}
		}
		catch (RuntimeException e) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Bluetooth LE not available");
			builder.setMessage("Sorry, this device does not support Bluetooth LE.");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					//finish();
		            //System.exit(0);
				}

			});
			builder.show();

		}

	}


	// Print detected devices
	public void updateLog(final String log) {
		TextView beaconsText = (TextView) findViewById(R.id.detectedDevicesText);
		beaconsText.setText(log);
    }



}
