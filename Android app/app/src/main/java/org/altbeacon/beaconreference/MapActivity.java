package org.altbeacon.beaconreference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.altbeacon.beacon.AltBeacon;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;

public class MapActivity extends Activity implements SensorEventListener {
    protected static final String TAG = "MapActivity";

    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorGyroscope;
    private Sensor sensorMagneticField;

    Float acc_x, acc_y, acc_z;
    Float gyr_x, gyr_y, gyr_z;
    Float mag_x, mag_y, mag_z;

    Calendar calendar;
    long startTime;
    long dataTime;

    public String datatext;
    public String dataMQTTtext;

    BeaconReferenceApplication application;

    int count_sensor_index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);                                        // MAX: hides the title bar
        setContentView(R.layout.activity_map);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);           // Create arbitary sensor manager
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);    // Create sensor for accelerometer
        sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);            // Create sensor fo gyroscope
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);   // Create sensor for magnometer

        startTime = calendar.getInstance().getTimeInMillis();                               // Get the current time --> start time (in ms)

        application = ((BeaconReferenceApplication) this.getApplicationContext());          // Indicates whether MapActivity resumed
        application.setMapActivity(this);

        count_sensor_index = 1;                                                             // This limits data updating ONLY when all acc/gyro/mag sensor readings are updated.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        application.setMapActivity(null);                                                                           // To indicate whether MapActivity paused.
        sensorManager.unregisterListener(this);                                                                     // Un-register the sensor.
    }

    @Override
    protected void onResume() {
        super.onResume();
        application.setMapActivity(this);                                                                           // To indicate whether MapActivity resumed.
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);       // Register accelerometer sensor, at 50 Hz (20,000 us)
        sensorManager.registerListener(this, sensorGyroscope, SensorManager.SENSOR_DELAY_GAME);           // Register gyroscope sensor, at 50 Hz (20,000 us)
        sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_GAME);       // Register magnometer sensor, at 50 Hz (20,000 us)
    }

    // Determines action when the "Stop Map Creation" is pressed.
    public void onStopClicked(View view) {
        onPause();
        application.sendPacket("Stop Mapping");
        // onDestroy();
        finish();
    }

    // When "Activate Button" is pressed, register the beacon ID
    public void onActivateClicked(View view) {
        application.registerBeaconIDMapping();
        makeButtonInvisible();
    }

    // Make "Activate Button" visible
    public void makeButtonVisible(){
        ((Button)findViewById(R.id.locateButton)).setVisibility(View.VISIBLE);
    }

    // Make "Activate Button" invisible
    public void makeButtonInvisible(){
        ((Button)findViewById(R.id.locateButton)).setVisibility(View.INVISIBLE);
    }

    // override onSensorChanged to filter sensor change
    // each sensor has its own "onSensorChanged"
    @Override
    public final void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            acc_x = event.values[0];
            acc_y = event.values[1];
            acc_z = event.values[2];
        } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyr_x = event.values[0];
            gyr_y = event.values[1];
            gyr_z = event.values[2];
        } else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mag_x = event.values[0];
            mag_y = event.values[1];
            mag_z = event.values[2];
        }

        // Only display & send data after ALL sensor readings from acc/gyro/mag have updated in that 20,000 us interval.
        if (count_sensor_index == 3) {
            resetText();
            addTimetoText();
            addIMUtoText();
            updateIMULog(datatext);                                                 // Display datatext, including BLE and IMU
            publishIMULog();                                                        // Publish data to MQTT
            count_sensor_index = 1;
        }
        else {
            count_sensor_index += 1;
        }
    }

    // Override onAccuracyChanged
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) { }

    // Reset text
    public void resetText() {
        datatext = "";
        dataMQTTtext = "";
    }

    // Add time to text
    public void addTimetoText() {
        dataTime = calendar.getInstance().getTimeInMillis() - startTime;                                     // Get time of data (in ms)
        datatext += "Time (ms): " + dataTime + "\n";
        dataMQTTtext += dataTime + ",";
    }

    // Add IMU data to text
    public void addIMUtoText() {
        datatext += "Acc X/Y/Z:  "+acc_x +" / "+acc_y +" / "+acc_z +"\n"+"Gyr X/Y/Z:  "+gyr_x+" / "+gyr_y+" / "+gyr_z+"\n"+"Mag X/Y/Z:  "+mag_x+" / "+mag_y+" / "+mag_z+"\n";
        dataMQTTtext += acc_x + "," + acc_y + "," + acc_z + "," + gyr_x + "," + gyr_y + "," + gyr_z + "," + mag_x + "," + mag_y + "," + mag_z;
    }

    // Add BLE data to textView
    public void updateBLELog(final String BLElog) {
        TextView thetext = (TextView)MapActivity.this.findViewById(R.id.BLEdata);
        thetext.setText(BLElog);
    }

    // Publish Time/IMU text to textView
    public void updateIMULog(final String texts) {
        TextView thetext = (TextView)MapActivity.this.findViewById(R.id.IMUdata);
        thetext.setText(texts);
    }

    // Retrieve time + IMU data text
    public void publishIMULog() {
        application.publishTimeIMUdata(dataMQTTtext);
    }

}
