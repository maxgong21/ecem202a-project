package org.altbeacon.beaconreference;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;


/**
 * Created by dyoung on 12/13/2013. https://github.com/AltBeacon/android-beacon-library-reference
 * Modified by Max Gong on 10/14/2020.
 */
public class BeaconReferenceApplication extends Application implements BootstrapNotifier, BeaconConsumer, RangeNotifier {

    private static final String TAG = "BeaconReferenceApp";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private MonitoringActivity monitoringActivity = null;
    private MapActivity mapActivity = null;
    private BeaconManager beaconManager;
    private String cumulativeLog = "";
    private Notification.Builder builder;

    // These are UUID, MajorIDs, MinorIDs of the iBeacons I purchased. Please enter your own based on the iBeacons YOU purchase.
    private final String desiredUUID = "426c7565-4368-6172-6d42-6561636f6e73";
    private final String desiredMajorID = "3838";
    private final String desiredMinorIDs[] = {"1", "2", "3", "4"};
    private final int numDesiredMinorIDs = desiredMinorIDs.length;

    // Define MQTT variables
    String clientID;
    private MqttAndroidClient client;
    private String brokerAddress = "tcp://broker.hivemq.com:1883";                  // This is the address of HiveMQ Broker
    private String topic_IP = "topic_IP";                                           // MQTT topic for Indoor Positioning (IP) (monitoring)
    private String topic_MB = "topic_MB";                                           // MQTT topic for Mappinng Beacons (MB) (mapping)
    int mqttSuccessfulConnection = 0;                                               // 0 = unsuccessful MQTT connection

    // Define variables for Mapping Beacons (MB)
    String beaconIDwithGreatestRSSI = null;
    String beaconIDtoRegister = null;
    int beaconIDIndex;
    int nearBeaconState = 0;

    // Define UDP variables
    private DatagramSocket socket;
    public String addrString = "192.168.1.3";
    private int port = 12345;

    public void onCreate() {
        super.onCreate();
        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        int numBeacons = 0;                 // MAX: Added this variable to help with foreground ranging function

        clientID = MqttAsyncClient.generateClientId();
/*
        // Set up MQTT client

        client = new MqttAndroidClient(this.getApplicationContext(), brokerAddress, clientID);

        // Define what happens when client connects
        client.setCallback(new MqttCallbackExtended() {
            @Override public void connectComplete(boolean b, String s) { Log.w(TAG, s); }
            @Override public void connectionLost(Throwable throwable) { Log.w(TAG, "mqtt Connection lost!"); }
            @Override public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception { Log.w(TAG, mqttMessage.toString()); }
            @Override public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) { Log.w(TAG, "mqtt Delivery complete!"); }
        });

        // Connect MQTT client to server
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {

                // If connection successful, try publishing.
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "mqtt onSuccess");
                    mqttSuccessfulConnection = 1;
                    try {
                        client.publish(topic_IP, new MqttMessage("MQTT Initialized!".getBytes()));     // Publish message to topic
                    } catch (MqttException ex) {  }
                }

                // If connection unsuccessful, ...
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "mqtt onFailure");
                    Log.d(TAG, exception.toString());
                }
            });
        } catch (MqttException e) {}
*/

        // Detect iBeacons
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        beaconManager.setDebug(true);


        // Scans for iBeacons in foreground, even when app is closed
        // Builds notification to make user aware of background scanning
        builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setContentTitle("Scanning beacons...");
        Intent intent = new Intent(this, MonitoringActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);


        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My Notification Channel ID",
                    "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My Notification Channel Description");
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }


        // Enable foreground scanning
        beaconManager.enableForegroundServiceScanning(builder.build(), 456);


        // For the above foreground scanning service to be useful, you need to disable
        // JobScheduler-based scans (used on Android 8+) and set a fast background scan
        // cycle that would otherwise be disallowed by the operating system.
        beaconManager.setEnableScheduledScanJobs(false);
        beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setBackgroundScanPeriod(1100);
        Log.d(TAG, "setting up background monitoring for beacons and power saving");


        // Monitors beacons
        Region region = new Region("backgroundRegion", null, null, null);       // NEED TO MODIFY: SO WE ONLY DETECT MajorID WE WANT, may have to include multiple regions???
        regionBootstrap = new RegionBootstrap(this, region);

        // Starts ranging in background & foreground by  binding to BeaconConsumer
        beaconManager.bind(this);

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the BeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%
        // backgroundPowerSaver = new BackgroundPowerSaver(this);

        // If you wish to test beacon detection in the Android Emulator, you can use code like this:
        // BeaconManager.setBeaconSimulator(new TimedBeaconSimulator() );
        // ((TimedBeaconSimulator) BeaconManager.getBeaconSimulator()).createTimedSimulatedBeacons();
        try {
            socket = new DatagramSocket(port);
        }
        catch (IOException e){
            Log.d(TAG, "Socket error in onCreate");
            e.printStackTrace();
        }
    }

    // Disables monitoring & ranging, both in foreground & background
    // This function is called when the "Disable Monitoring" button is pressed.
    public void disableMonitoring() {
        if (regionBootstrap != null) {
            regionBootstrap.disable();
            regionBootstrap = null;
        }

        // Stops ranging in background & foreground by unbinding from BeaconConsumer
        beaconManager.unbind(this);

        // Send message that monitoring is disabled
        //sendPacket("STOP");
        new UDPTask().execute("STOP");
    }


    // Enables monitoring & ranging, both in foreground & background
    // This function is called when the "Enable Monitoring" button is pressed.
    public void enableMonitoring() {
        Region region = new Region("backgroundRegion", null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);

        // Starts ranging in background & foreground by  binding to BeaconConsumer
        beaconManager.bind(this);

/*
        // Notifies PC that monitoring is on
        if (mqttSuccessfulConnection == 1) {
            try {
                Log.d(TAG, "mqtt DATA monitoring...");
                client.publish(topic_IP, new MqttMessage("Now Monitoring...".getBytes()));     // Publish message to topic
            } catch (MqttException e) {}
        }
*/
        // Notifies PC through UDP that monitoring is on
        Log.d(TAG, "UDP data monitoring...");
        new UDPTask().execute("Now monitoring...");
    }


    // Enter region, part of BootStrapNotifier
    @Override
    public void didEnterRegion(Region arg0) {
        Log.d(TAG, "did enter region.");
    }


    // Exit region, part of BootStrapNotifier
    @Override
    public void didExitRegion(Region region) {
        Log.d(TAG, "I no longer see a beacon.");
    }

    // Modified to include Ranging of beacons
    @Override
    public void didDetermineStateForRegion(int state, Region region) {

        if (monitoringActivity != null || mapActivity != null){
            String s = state == 0 ? "Out of distance" : "Inside distance";
            if (state == 1){
                try {
                    beaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    beaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    // Sets MonitoringActivity.java
    public void setMonitoringActivity(MonitoringActivity activity) {
        this.monitoringActivity = activity;
    }


    // Sets MapActivity.java
    public void setMapActivity(MapActivity activity) {
        this.mapActivity = activity;
    }



    // Override BeaconConsumer onBeaconServiceConnect function
    // Now calls RangeNotifier to detect RSSI & MajorID
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(this);
    }


    // Override RangeNotifier didRangeBeaconsInRegion function
    // Now includes finding all detected beacons RSSI & MajorID
    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

        // Store MajorID and RSSI of beacons & Display them
        String beaconMinorIDs[] = new String[beacons.size()];
        double beaconRSSIs[] = new double[beacons.size()];
        String tempUUID, tempMajorID, tempMinorID;
        int i = 0;
        String tempInfoString = "";
        String beaconInfoString = "";
        String beaconInfoStringMQTT = clientID;
        String timeIMUdatatext = "";
        int aretherebeacons = 0;                                        // indicate whether there are beacons, to help determine send MQTT data

        // Iterate through list of beacons
        for (Iterator<Beacon> iterator = beacons.iterator(); iterator.hasNext();){
            Beacon tempBeacon = iterator.next();
            tempUUID = tempBeacon.getId1().toString();
            tempMajorID = tempBeacon.getId2().toString();
            tempMinorID = tempBeacon.getId3().toString();

            // Filter list of beacons - Only consider the desired MinorIDs
            for (int j = 0; j < numDesiredMinorIDs; j++){

                // Filter list of beacons - Only consider the desired UUID & MajorIDs
                if (desiredUUID.equals(tempUUID) && desiredMajorID.equals(tempMajorID)) {
                    if (desiredMinorIDs[j].equals(tempMinorID)){

                        aretherebeacons = 1;

                        // Get MinorID & RSSI
                        beaconMinorIDs[i] = tempMinorID;
                        beaconRSSIs[i] = tempBeacon.getRssi();
                        Log.d(TAG, "BEACON DETECTED WITH MinorID: " + beaconMinorIDs[i]);

                        // MAPPING BEACONS --> make the "Activate Beacon" button visible when get close enough to beacon
                        // This only happens when mapActivity is active
                        if (mapActivity != null){

                            // If beacon RSSI is very strong, then make the "Activate Button" visible
                            if (nearBeaconState == 0 && beaconRSSIs[i] > -60){
                                nearBeaconState = 1;
                                beaconIDwithGreatestRSSI = beaconMinorIDs[i];
                                beaconIDIndex = i;
                                mapActivity.makeButtonVisible();
                            }

                            // Don't detect for greatest RSSI until user leaves the beacon that was just "Activated"
                            else if (nearBeaconState == 1 && beaconMinorIDs[i] == beaconIDwithGreatestRSSI && beaconRSSIs[i] < -70){
                                nearBeaconState = 0;
                                beaconIDwithGreatestRSSI = null;                          // Reset beaconIDwithLowestRSSI
                            }

                        }

                        // Add to data to display
                        beaconInfoString += "MinorID: " + beaconMinorIDs[i] + " / RSSI: " + beaconRSSIs[i] + "\n";

                        // Add to data to send through MQTT topic_IP
                        beaconInfoStringMQTT += "," + beaconMinorIDs[i] + "," + beaconRSSIs[i];


                        // Update the next beacon
                        i++;

                        // Since we verified a beacon, exit inner for-loop to verify next beacon
                        break;
                    }
                }
            }
        }
/*
        // Send BLE data through MQTT
        if (mqttSuccessfulConnection == 1) {

                    // Publish to topic_IP when not mapActivity & there are beacons
                    if (mapActivity == null && aretherebeacons == 1) {
                        try {
                            Log.d(TAG, "topic_IP mqtt DATA publishing...");
                            client.publish(topic_IP, new MqttMessage(beaconInfoStringMQTT.getBytes()));     // Publish message to topic
                        } catch (MqttException e) {}
            }
        }
*/
        // Send BLE data through UDP, only when mapActivity is null AND if there's beacons detected
        if (mapActivity == null && aretherebeacons == 1){
            //sendPacket(beaconInfoStringMQTT);
            new UDPTask().execute(beaconInfoStringMQTT);
        }

        // Display detected beacons in MonitoringActivity
        if (monitoringActivity != null){
            monitoringActivity.updateLog(beaconInfoString);
        }

        // Display detected beacons in MappingActivity
        if (mapActivity != null){
            mapActivity.updateBLELog(beaconInfoString);
        }
    }

    // When "Activate Beacon" is pressed, this fucntion is called, to send the beaconID to MQTT
    public void registerBeaconIDMapping() {
        beaconIDtoRegister = beaconIDwithGreatestRSSI;
    }

    // Send Time + IMU data to MQTT when mapping.
    // This function is called by MapActivity.
    public void publishTimeIMUdata (String dataMQTTText){

        // if user hits "Activate Beacon" to activate beacon, then send to MQTT topic_MB
        if (beaconIDtoRegister != null) {
            dataMQTTText += ","+beaconIDtoRegister;
            beaconIDtoRegister = null;
            Log.d(TAG, "BEACON DETECTED REGISTERED");
            Log.d(TAG, dataMQTTText);
        }
/*
        // Send Time+IMU (+beacon data if any) to MQTT
        if (mqttSuccessfulConnection == 1) {
            try {
                Log.d(TAG, "topic_MB mqtt DATA publishing...");
                MqttMessage m = new MqttMessage();
                m.setPayload(dataMQTTText.getBytes());
                m.setQos(2);
                m.setRetained(false);
                client.publish(topic_MB, m);
            } catch (MqttException e) {}
        }
*/
        // Send Time+IMU (+beacon data if any) to UDP
        //sendPacket(dataMQTTText);
        new UDPTask().execute(dataMQTTText);

    }


    // Send message through UDP to computer
    public void sendPacket(String message) {
        Log.e(TAG, "Message from MAPACTIVITY");
        new UDPTask().execute(message);
    }


    // Android requires network operations to be conducted NOT in the main UI thread. So need ASYNC for background network operations:
    public class UDPTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... message) {
            Log.d(TAG, "Begin sending UDP message");
            Log.d(TAG, "Value of message: " + message[0]);
            try {
                byte[] messageData = message[0].getBytes();
                InetAddress addr = InetAddress.getByName(addrString);       // IP address of computer
                DatagramPacket thePacket = new DatagramPacket(messageData, messageData.length, addr, port);
                socket.send(thePacket);
            }
            catch (SocketException e) {
                Log.e(TAG, "UDP socket error");
                e.printStackTrace();
            }
            catch (IOException e) {
                Log.e(TAG, "UDP send failed");
                e.printStackTrace();
            }
            Log.e(TAG, "UDP sent");
            return null;
        }
    }

}
