package com.nineone.inner_s_tool;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

public class Background_Service extends Service implements SensorEventListener {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public Background_Service() { }
    private long RescanBaseTime;
    private long SensorbaseTime;
    private String ACTION_STOP_SERVICE = "STOP";
    private NotificationCompat.Builder main_Notification;// 알림만들기
    private Notification senser_warning_Notification;// 알림만들기
    private NotificationManager mNotificationManager;
    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    public static final String ACTION_NOTIF_CENCEL = "ACTION_NOTIF_CENCEL";
    private ScanSettings btLeScanSettings;
    private ArrayList<ScanFilter> btLeScanFilters;

    private boolean isonoff = false;

    private BluetoothLeScanner mBluetoothLeScanner;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            Log.e("activityt_TAGaction", action);
            if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
                stopScan();
                isonoff = true;
                onDestroy();
                Log.e("activityt_TAG", "serviceddd");
            }
            switch (action) {
                case ACTION_START_FOREGROUND_SERVICE:

                    if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
                        stopScan();
                        isonoff = true;
                        onDestroy();
                        Log.e("activityt_TAG", "serviceddd");
                    }
                    IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);//ble 상태 감지 필터
                    registerReceiver(mBroadcastReceiver1, filter1);
                    IntentFilter filter2 = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);//gps 상태감지 필터
                    registerReceiver(mBroadcastReceiver1, filter2);
                    IntentFilter filter3 = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);//인터넷 상태 감지 필터
                    registerReceiver(mBroadcastReceiver1, filter3);
                    RescanBaseTime = SystemClock.elapsedRealtime();
                    Intent stopSelf = new Intent(this, Background_Service.class);
                    stopSelf.setAction(ACTION_STOP_SERVICE);
                   // PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_IMMUTABLE);
                    main_Notification = new NotificationCompat.Builder(this, "default");
                    main_Notification.setSmallIcon(R.mipmap.ic_launcher);
                    main_Notification.setContentTitle("현재 위치 측정 중");
                    //   builder.setContentText(fasf[0]+", "+fasf[2]);
                    main_Notification.setContentText("");
                  //  main_Notification.addAction(R.drawable.ic_launcher_foreground, "Close", pStopSelf);
                    Intent notificationIntent = new Intent(this, MainSectorEntrance_Activity.class)
                            .setAction(Intent.ACTION_MAIN)
                            .addCategory(Intent.CATEGORY_LAUNCHER)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

                    main_Notification.setContentIntent(pendingIntent);

                    // 오레오 버전 이상 노티피케이션 알림 설정
                    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    // mNotificationManager.createNotificationChannel(new NotificationChannel("default", "기본채널", NotificationManager.IMPORTANCE_DEFAULT));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mNotificationManager.createNotificationChannel(new NotificationChannel("default", "undead_service", NotificationManager.IMPORTANCE_NONE));
                    }
                    mNotificationManager.notify(1, main_Notification.build());
                    Notification notification = main_Notification.build();
                    startForeground(1, notification);

                    ble_setting();
                    google_gps();
                    startScan();
                     soundPool = new SoundPool.Builder().setMaxStreams(8).build();
                     soundPlay = soundPool.load(getApplicationContext(), R.raw.arml, 1);

                    baseTime = SystemClock.elapsedRealtime();

                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                    stopScan();
                    isonoff = true;
                    stopForegroundService();
                    onDestroy();
                    break;
                case ACTION_NOTIF_CENCEL:
                    Log.e("ACTION_NOTIF_CENCEL", "ACTION_NOTIF_CENCEL");

                    break;
            }
        }


        return START_NOT_STICKY;
    }
    private SoundPool soundPool;
    private int soundPlay;
    private void ble_setting(){

        btLeScanFilters = new ArrayList<ScanFilter>();
        btLeScanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                //  .setReportDelay(0)
                .build();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        ScanFilter scanFilter1 = new ScanFilter.Builder()
                .setManufacturerData(37265, new byte[]{})
                // .setServiceUuid(new ParcelUuid("........ uuid reference ......"))
                .build();
                 /*   ScanFilter scanFilter2 = new ScanFilter.Builder()
                            .setManufacturerData(8, new byte[]{})
                            .build();
                    ScanFilter scanFilter3 = new ScanFilter.Builder()
                            .setDeviceName("TJ-00CB-FFFFFF5D-0000")
                            .build();
                    btLeScanFilters.add(scanFilter2);
                    btLeScanFilters.add(scanFilter3);*/
        btLeScanFilters.add(scanFilter1);


        Log.e("off1", String.valueOf(mBluetoothAdapter.isEnabled()));
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothLeScanner.startScan(btLeScanFilters, btLeScanSettings, leScanCallback);
        }else{
            isonoff = true;

            //stopForegroundService();
          //  onDestroy();
           // Toast.makeText(getApplication(), "블루투스가 꺼져있어\n백그라운드모드가 실행되지 않았습니다.", Toast.LENGTH_SHORT).show();

        }
    }
    private ArrayList<Ble_item> listData = new ArrayList<>();
    private ArrayList<ScannedDevice> sannedDevice_listData = new ArrayList<>();
    private Map<String,Integer> BLE_HASHMAP;
    private Map<String,Object> SEND_HASHMAP;
    private boolean ble_sned_Boolean = false;
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            if (result.getDevice().getName() != null) {

                BluetoothDevice bluetoothDevice = result.getDevice();
                if (bluetoothDevice.getName().startsWith("TJ-")) {
                    int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                    if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
                        bleupdate(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                        Log.e("ble 연결됨", "230");
                    }else {
                        Log.e("rssid1", String.valueOf(result.getRssi()));
                        boolean contains = false;
                        for (Ble_item device : listData) {
                            if (bluetoothDevice.getAddress().equals(device.getTag_Adress())) {
                                contains = true;
                                // update
                                device.setTag_int_Rssi(result.getRssi());
                                break;
                            }
                            //   Log.e("rssid", device.getTag_Name()+", "+device.getTag_Rssi_arrary().size());
                            Log.e("getTag_Rssi", String.valueOf(device.getTag_Rssi_arrary()));
                        }
                        if (!contains) {
                            String[] DeviceNameArray = bluetoothDevice.getName().trim().split("-");
                            if (DeviceNameArray.length >= 3) {
                                Log.e("rssid2", String.valueOf(result.getRssi()));
                                listData.add(new Ble_item(bluetoothDevice.getAddress(), bluetoothDevice.getName(), result.getRssi()));
                            }
                        }
                        Runnable runnable_ble_sned;
                        if (!ble_sned_Boolean) {
                            ble_sned_Boolean = true;
                            runnable_ble_sned = () -> {
                                //  ble_hashmap_add();
                                Network_Confirm();
                            };
                            start_handler.postDelayed(runnable_ble_sned, 1000);
                        }
                    }
                    getEllapse();
                }
            }
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
        }
    };
    private int sensorStartIdx=9;
    private boolean alarm_ON_OFF = false;
    public String bleupdate(BluetoothDevice newDevice, int rssi, byte[] scanRecord) {
        if (!isonoff) {
            if ((newDevice == null) || (newDevice.getAddress() == null) || newDevice.getName() == null) {
                return "";
            }
            long now = System.currentTimeMillis();
            int Sensor_Alarm = 0;
            Sensor_Alarm = scanRecord[9 + 5];
            int senser_type = 0;
            int os2_errer = 0;
            int CO_errer2 = 0;
            int H2S_errer2 = 0;
            int CO2_errer2 = 0;
            int CH4_errer2 = 0;
            //  int Sensor_Alarm = 250;
            senser_type = ((Sensor_Alarm) & 0x07);
            os2_errer = ((Sensor_Alarm >> 7) & 0x01);
            CO_errer2 = ((Sensor_Alarm >> 6) & 0x01);
            H2S_errer2 = ((Sensor_Alarm >> 5) & 0x01);
            CO2_errer2 = ((Sensor_Alarm >> 4) & 0x01);
            CH4_errer2 = ((Sensor_Alarm >> 3) & 0x01);
            StringBuilder alarmstring = new StringBuilder();
            String string_O2 = "";String string_CO = "";String string_H2S = "";String string_CO2 = "";  String string_CH4 = "";

            boolean mAlarm_on = false;
           /* if (newDevice.getName().equals("TJ-00CA-0000000B-0000")) {
                int senser_O2 = ConvertToIntLittle(scanRecord, sensorStartIdx + 6);
                int senser_CO2 = ConvertToIntLittle(scanRecord, sensorStartIdx + 12);

                Log.e("mAlarm_on1", senser_O2 + ", " + senser_CO2 + ", " + Arrays.toString(scanRecord));

                //  Log.e("mAlarm_on2", Integer.parseInt(String.valueOf(Sensor_Alarm),16)+", "+String.valueOf(os2_errer)+", "+String.valueOf(CO2_errer2));
            }*/
            if (os2_errer == 1) {
                mAlarm_on = true;
                alarmstring.append("O2 ");
            }
            if (CO_errer2 == 1) {
                mAlarm_on = true;
                alarmstring.append("CO ");
            }
            if (H2S_errer2 == 1) {
                mAlarm_on = true;
                alarmstring.append("H2S ");
            }
            if (CO2_errer2 == 1) {
                mAlarm_on = true;
                alarmstring.append("CO2 ");
            }
            if (CH4_errer2 == 1) {
                mAlarm_on = true;
                alarmstring.append("CH4 ");
            }
          //
            int senser_O2 = ConvertToIntLittle(scanRecord, sensorStartIdx + 6);
            //   Log.e("655535", String.valueOf(senser_O2));
            if(senser_O2 == 0xFFFF) {
                string_O2 = "센서 측정 중";
            }else{
                string_O2 = String.format(Locale.KOREA,"%.2f", senser_O2 * 0.01) + " %";
            }
            int senser_CO = ConvertToIntLittle(scanRecord, sensorStartIdx + 8);
            if(senser_CO==0xFFFF) {
                string_CO = "센서 측정 중";
            }else{
                string_CO = senser_CO + " ppm";
            }

            int senser_H2S = ConvertToIntLittle(scanRecord, sensorStartIdx + 10);

            if(senser_H2S==0xFFFF) {
                string_H2S = "센서 측정 중";
            }else{
                string_H2S = senser_H2S + " ppm";
            }

            int senser_CO2 = ConvertToIntLittle(scanRecord, sensorStartIdx + 12);

            if(senser_CO2==0xFFFF) {
                string_CO2 = "센서 측정 중";
            }else{
                string_CO2 = senser_CO2 + " ppm";
            }

            int senser_CH4 = ConvertToIntLittle(scanRecord, sensorStartIdx + 14);
            if(senser_CH4==0xFFFF) {
                string_CH4 = "센서 측정 중";
            }else{
                string_CH4 = senser_CH4 + " ppm";
            }
           // mAlarm_on=true;
            String zone_data = "O2:"+string_O2 + ", CO:" + string_CO + ", H2S:" + string_H2S + ", CO2:" + string_CO2 + ",CH4:" + string_CH4;

            if (mAlarm_on) {

                if (!alarm_ON_OFF) {
                    alarmstring.append("경고");
                    alarm_ON_OFF = true;
                    soundPool.play(soundPlay, 1f, 1f, 6, 0, 1f);
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        //  vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                        vibrator.vibrate(VibrationEffect.createOneShot(1000, 150));//0~255
                    } else {
                        vibrator.vibrate(750);

                    }
                    long index_num = 0;
                    String[] DeviceNameArray = newDevice.getName().trim().split("-");
                    index_num = Long.parseLong(DeviceNameArray[2], 16);

                    main_Notification.setContentTitle("네트워크 연결 실패");
                    main_Notification.setContentText(zone_data);
                    mNotificationManager.notify(1, main_Notification.build());
                    Runnable alarm_runnable = new Runnable() {
                        @Override
                        public void run() {
                            //notifyDataSetChanged();
                            alarm_ON_OFF = false;

                        }
                    };
                    listcange_handler.postDelayed(alarm_runnable, 1000);
                }
            }
            boolean contains = false;
            //    Log.e("newDevicegetName",newDevice.getName());
            for (ScannedDevice device : sannedDevice_listData) {

                if (newDevice.getAddress().equals(device.getDevice().getAddress())) {

                    contains = true;
                    // update
                    device.setDisplayName(newDevice.getName());
                    device.setRssi(rssi);
                    device.setLastUpdatedMs(now);
                    device.setScanRecord(scanRecord);
                    // Log.e("mAlarm_on3", Arrays.toString(scanRecord));
                    break;
                }


            }
            if (!contains) {
                String[] DeviceNameArray = newDevice.getName().trim().split("-");
                if (DeviceNameArray.length >= 3) {
                    sannedDevice_listData.add(new ScannedDevice(newDevice, rssi, scanRecord, now));
                }

            }


        }
        return "";
    }
    private void Network_Confirm() {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_MOBILE) {
            ble_hashmap_add();
            //   Http_post(Network_data);
            Log.e("모바일로 연결됨", "650");
        } else if (status == NetworkStatus.TYPE_WIFI) {
            ble_hashmap_add();
            //   Http_post(Network_data);
            Log.e("무선랜으로 연결됨", "652");
        } else {

            Log.e("연결 안됨.", "654");
        }
    }
    private void ble_hashmap_add(){
        BLE_HASHMAP = new HashMap<>();
        for (Ble_item device : listData) {
            int sum = 0;
            int avg = 0;
            for (int i = 0; i < device.getTag_Rssi_arrary().size(); i++) {
                sum += device.getTag_Rssi_arrary().get(i);
            }
            avg = sum / device.getTag_Rssi_arrary().size();
            BLE_HASHMAP.put(device.getTag_Name(),avg);
        }
        SEND_HASHMAP = new HashMap<>();
        SEND_HASHMAP.put("ble",BLE_HASHMAP);
        Log.e("dd-", String.valueOf(listData));
        Log.e("dd-", String.valueOf(BLE_HASHMAP));

        if(listData.size()!=0) {
            listData = new ArrayList<>();
            if (!isonoff) {
                Send_Http_post();
            }
        }
        ble_sned_Boolean = false;
    }

    private boolean mIsScanning = false;
    private void startScan() {
        if ((mBluetoothLeScanner != null) && (!mIsScanning)) {
            mBluetoothLeScanner.startScan(leScanCallback);
            RescanBaseTime = SystemClock.elapsedRealtime();
            mIsScanning = true;
            Log.e("startscan", "287");
        }
        long now = System.currentTimeMillis();
        SimpleDateFormat sdfNow = new SimpleDateFormat("MM_dd_HH_mm_ss");
    }
    private void stopScan() {
        if (mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(leScanCallback);
            mIsScanning = false;
            Log.e("startscan", "295");
        }
    }
    private void reScan() {
        if (mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(leScanCallback);
            mBluetoothLeScanner.startScan(leScanCallback);
        }
    }
    private void getEllapse() {
        long now = SystemClock.elapsedRealtime();
        long ell = now - RescanBaseTime;                            //현재 시간과 지난 시간을 빼서 ell값을 구하고
        long min = (ell / 1000) / 60;
        if (20 < min) {
            Log.e("SystemClock2", min + "," + RescanBaseTime + "," + ell);
            Log.e("BLE Scan:", " ReStart");
            RescanBaseTime = SystemClock.elapsedRealtime();
            reScan();
        }
    }
    private void Send_Http_post() {

        new Thread(() -> {
            try {
                Log.e("dd-", "164");
                String url = "http://stag.nineone.com:8005/api/mobile";
                URL object = null;
                object = new URL(url);
                Log.e("dd-", "168");
                HttpURLConnection con = null;

                con = (HttpURLConnection) object.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("POST");
                JSONArray array = new JSONArray();
                if (SEND_HASHMAP == null) {
                    Log.e("dd-209", "282");
                    return;
                }
                JSONObject cred = new JSONObject(SEND_HASHMAP);
                try {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                    String mstart_name = sp.getString("startname", "");
                    cred.put("user_id", mstart_name);
                    cred.put("pressure", PRESSURE_avg);
                    cred.put("lat", String.valueOf(latitude));
                    cred.put("lng", String.valueOf(longitude));
                    //cred.put("ble", SEND_HASHMAP);
                    cred.put("mobile_time", String.valueOf(System.currentTimeMillis()));
                    Log.e("dd-187", "187");
                } catch (JSONException e) {
                    Log.e("dd-189", "\n" + e.getMessage());
                    e.printStackTrace();
                }
                array.put(cred);
                //    JSONObject cred2 = new JSONObject(SEND_HASHMAP);
                //  array.put(cred2);
                OutputStream os = con.getOutputStream();
                Log.e("dd-195", array.toString());
                os.write(array.toString().getBytes("UTF-8"));
                os.close();
                //display what returns the POST request

                StringBuilder sb = new StringBuilder();
                int HttpResult = con.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    Log.e("dd-209", "\n" + sb.toString());
                    try {
                        JSONObject job = new JSONObject(sb.toString());
                        String build_name = job.getString("build_name");
                        String level_name = job.getString("level_name");
                        String zone_name = job.getString("zone_name");//xprmdlfma
                        String env_O2 = job.getString("env_O2");//null이면 화면에 표시하지않음
                        boolean env_O2_alarm = job.getBoolean("env_O2_alarm");//
                        String env_CO = job.getString("env_CO");//수신
                        boolean env_CO_alarm = job.getBoolean("env_CO_alarm");//
                        String env_H2S = job.getString("env_H2S");//퇴실
                        boolean env_H2S_alarm = job.getBoolean("env_H2S_alarm");
                        String env_Co2 = job.getString("env_Co2");//배터리
                        boolean env_Co2_alarm = job.getBoolean("env_Co2_alarm");//
                        String env_CH4 = job.getString("env_CH4");//상태 코드
                        boolean env_CH4_alarm = job.getBoolean("env_CH4_alarm");
                        String env_user = job.getString("user");//상태 코드
                        boolean mAlarm_on = false;

                      /*  String env_O2_alarm_String = "OFF";
                        String env_CO_alarm_String = "OFF";
                        String env_H2S_alarm_String = "OFF";
                        String env_CO2_alarm_String = "OFF";
                        String env_CH4_alarm_String = "OFF";

                        if(env_O2_alarm){
                            env_O2_alarm_String = "ON";
                        }else if(env_CO_alarm){
                            env_CO_alarm_String = "ON";
                        }else if(env_H2S_alarm){
                            env_H2S_alarm_String = "ON";
                        }else if(env_Co2_alarm){
                            env_CO2_alarm_String = "ON";
                        }else if(env_CH4_alarm){
                            env_CH4_alarm_String = "ON";
                        }*/
                        String buildlevel_name = build_name + " " + level_name + " " + zone_name;
                        String zone_data = env_O2 + "-" + env_CO + "-" + env_H2S + "-" + env_Co2 + "-" + env_CH4;
                        String zone_data2 = "O2:"+env_O2 + ", CO:" + env_CO + ", H2S:" + env_H2S + ", CO2:" + env_Co2 + ",CH4:" + env_CH4;

                        String zone_boolen_data = env_O2_alarm + "-" + env_CO_alarm + "-" + env_H2S_alarm + "-" + env_Co2_alarm + "-" + env_CH4_alarm;
                        String zone_data3 = "O2 : " + env_O2 + "\n"
                                + "CO : " + env_CO + "\n"
                                + "H2S : " + env_H2S + "\n"
                                + "CO2 : " + env_Co2 + "\n"
                                + "CH4 : " + env_CH4;

                        if (env_O2_alarm || env_CO_alarm || env_H2S_alarm || env_Co2_alarm || env_CH4_alarm) {
                            mAlarm_on = true;
                        }
                        if(!alarm_ON_OFF) {
                            if (mAlarm_on) {
                                soundPool.play(soundPlay, 1f, 1f, 6, 0, 1f);
                                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    //  vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                                    vibrator.vibrate(VibrationEffect.createOneShot(1000, 150));//0~255
                                } else {
                                    vibrator.vibrate(750);

                                }
                            }
                        }
                        Runnable runnable_location_textView = new Runnable() {
                            @Override
                            public void run() {
                                main_Notification.setContentTitle("현재 위치 : " + buildlevel_name);
                                main_Notification.setContentText(zone_data2);
                                // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                mNotificationManager.notify(1, main_Notification.build());
                                Intent intent = new Intent("custom-event-name");
                                intent.putExtra("buildlevel_name", buildlevel_name);
                                intent.putExtra("zone_data", zone_data);
                                intent.putExtra("zone_boolen_data", zone_boolen_data);
                                intent.putExtra("env_user", env_user);
                                Log.e("delay_check", "massage");
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                            }
                        };
                        textchange_handler.postDelayed(runnable_location_textView, 0);
                        Runnable alarm_runnable = new Runnable() {
                            @Override
                            public void run() {
                                //notifyDataSetChanged();
                                alarm_ON_OFF = false;

                            }
                        };
                        listcange_handler.postDelayed(alarm_runnable, 1000);
                    } catch (JSONException e) {
                        // Handle error
                    }
                } else {
                    //writeLog(post_data);
                    Log.e("dd-211", "\n" + con.getResponseMessage());
                    //   System.out.println(con.getResponseMessage());
                }
            } catch (IOException e) {
                Log.e("dd-215", e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    private void Exit_Http_post() {
        new Thread(() -> {
            try {
                Log.e("dd-", "164");
                String url = "http://stag.nineone.com:8005/api/pressurecal";
                URL object = null;
                object = new URL(url);
                Log.e("dd-", "168");
                HttpURLConnection con = null;

                con = (HttpURLConnection) object.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("POST");
                JSONArray array = new JSONArray();
                if (SEND_HASHMAP == null) {
                 /*   Log.e("dd-209", "282");
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run()
                        {
                            Toast.makeText(getApplicationContext(),"위치 데이터 수집 중 입니다\n잠시 후 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
                        }
                    }, 0);*/
                    return;
                }
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                String mstart_name = sp.getString("startname", "");
                String Shared_zone_name_num = sp.getString("Shared_zone_name_num", "");
                JSONObject cred = new JSONObject(SEND_HASHMAP);
                try {
                    cred.put("user_id", mstart_name);
                    cred.put("mean_pressure", PRESSURE_avg);
                    //cred.put("ble", SEND_HASHMAP);
                    cred.put("zone_name", Shared_zone_name_num);
                    cred.put("entrance_check" , false);
                    Log.e("dd-187", "187");
                } catch (JSONException e) {
                    Log.e("dd-189", "\n" + e.getMessage());
                    e.printStackTrace();
                }
                array.put(cred);
                //    JSONObject cred2 = new JSONObject(SEND_HASHMAP);
                //  array.put(cred2);
                OutputStream os = con.getOutputStream();
                Log.e("dd-195", array.toString());
                os.write(array.toString().getBytes("UTF-8"));
                os.close();
                //display what returns the POST request

                StringBuilder sb = new StringBuilder();
                int HttpResult = con.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    Log.e("dd-Bufferedexit", "\n" + sb.toString());
                    try {
                        JSONObject job = new JSONObject(sb.toString());
                        boolean return_result = job.getBoolean("result");
                        String return_zone_name = job.getString("zone_name");
                        Log.e("return_result", return_result + "," + return_zone_name);


                    } catch (JSONException e) {
                        // Handle error
                    }
                } else {
                    Log.e("dd-212", "\n" + con.getResponseMessage());
                }
            } catch (IOException e) {
                Log.e("dd-215", e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    private void sendMessage(){
        Log.d("messageService", "Broadcasting message");

    }
    private Sensor mBarometer; // 기압계
    private ArrayList<Float> PRESSURE_add = new ArrayList<>();
    private float PRESSURE_avg;
    private static SensorManager mSensorManager;
    private void senser_check(){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mBarometer = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);//기압계
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null){
            // textBaro.setText("기압계 센서 지원하지 않음");
        }
        mSensorManager.registerListener(this, mBarometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (!isonoff) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_PRESSURE) {//기압
                //     long timestamp = sensorEvent.timestamp;
                float presure = sensorEvent.values[0];
                presure = (float) (Math.round(presure * 100) / 100.0); //소수점 2자리 반올림
                //   Log.e("TYPE_PRESSURE", String.valueOf(presure));
                //   sector_list_adapter.notifyDataSetChanged();
                //기압을 바탕으로 고도를 계산(맞는거 맞아???)
                //float height = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, presure);
                PRESSURE_add.add(presure);
                if (getTime(SensorbaseTime) >= 10) {
                    float sum = 0;
                    int count = 0;
                    for (float device : PRESSURE_add) {
                        sum += device;
                        count++;
                    }
                    PRESSURE_avg = sum / count;
                    SensorbaseTime = SystemClock.elapsedRealtime();
                }
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }
    private long getTime(long timesec) {
        //경과된 시간 체크
        long nowTime = SystemClock.elapsedRealtime();
        long overTime = nowTime - timesec;
        long sec = (overTime / 1000) % 60;
        return sec;
    }

    private double longitude = 0;
    private double latitude = 0;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private void google_gps() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        startLocationUpdates();

    }
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private final LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            if (!isonoff) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    location.getTime();
                    Log.e("gpsTracker21",
                            "위도 : " + longitude + ", " +
                                    "경도 : " + latitude);
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                //log.e("off1", action);
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //log.e("off2", "off2");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                    default:
                        //log.e("off5", String.valueOf(state));
                        break;
                }
            }
            if (action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                //log.e("off6", action + ", " + intent);

            }
            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {

                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (isGpsEnabled || isNetworkEnabled) {
                    //startService();
                } else {
                    // blesendcheck.setText("중지 (GPS가 종료)");
                    //log.e("off8", String.valueOf(isGpsEnabled));
                }
            }
            if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                Log.e("NetworkInfo", action);
                Bundle extras = intent.getExtras();
                NetworkInfo info = (NetworkInfo) extras.getParcelable("networkInfo");
                NetworkInfo.State networkstate = info.getState();
                Log.d("TEST Internet", info.toString() + " " + networkstate.toString());
                if (networkstate == NetworkInfo.State.CONNECTED) {
                    //  Toast.makeText(activity.getApplication(), "Internet connection is on", Toast.LENGTH_LONG).show();
                    Log.e("testCONNECTED", "testCONNECTED");

                } else {
                    Log.e("testCONNECTED", "distestCONNECTED");
                    //   Toast.makeText(activity.getApplication(), "Internet connection is Off", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @Override
    public void onDestroy() { //여기다가 종료할때 모든 코드 넣기 https://developer.android.com/guide/components/services?hl=ko
        super.onDestroy();
        stopScan();
        isonoff = true;
        Exit_Http_post();
        try {
            unregisterReceiver(mBroadcastReceiver1);
        } catch (Exception ignored) {

        }
        if (mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(leScanCallback);
        }
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        stopForeground(true);
        stopSelf();
    }


    private final MyHandler start_handler = new MyHandler(this);
    private final MyHandler textchange_handler = new MyHandler(this);
    private final MyHandler listcange_handler = new MyHandler(this);
    private static class MyHandler extends Handler {
        private final WeakReference<Background_Service> mActivity;
        public MyHandler(Background_Service activity) {
            mActivity = new WeakReference<Background_Service>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            Background_Service activity = mActivity.get();
        }
    }
    private long baseTime;
    private void stopForegroundService() {
        stopForeground(true);
        stopSelf();
      //  stopTimerTask();
    }
    private int ConvertToIntLittle(byte[] txValue, int startidx) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
        // by choosing big endian, high order bytes must be put
        // to the buffer before low order bytes
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        // since ints are 4 bytes (32 bit), you need to put all 4, so put 0
        // for the high order bytes
        byteBuffer.put(txValue[startidx]);
        byteBuffer.put(txValue[startidx + 1]);
        byteBuffer.put((byte) 0x00);
        byteBuffer.put((byte) 0x00);

        byteBuffer.flip();
        int result = byteBuffer.getInt();
        return result;
    }
}
