package com.nineone.inner_s_tool;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainSectorEntranceActivity extends AppCompatActivity implements SensorEventListener {
    private long RescanBaseTime;
    private long SensorbaseTime;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private static final int REQUEST_ENABLE_BT = 2;//ble 켜져있는지 확인
    private static SensorManager mSensorManager;
    private TextView location_textView,data_textView,user_textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sector_entrance);
        Intent intent = getIntent();//차트 리스트 페이지에서 정보 받아오기
        String get_zone_name_num = intent.getExtras().getString("zone_name_num");//csv파일경로
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("No " + get_zone_name_num + " Confferdam");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        location_textView = findViewById(R.id.Location_TextView);
        data_textView = findViewById(R.id.Data_TextView);
        user_textView = findViewById(R.id.User_TextView);
        google_gps();
        bluetoothCheck();
        senser_check();
    }
    private void bluetoothCheck() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        // 지원하지 않는다면 어플을 종료시킨다.
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "이 기기는 블루투스 기능을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            //log.e("BLE1245", "124");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        stopScan();
        //log.e("BLE1245", "130");
    }
    private ArrayList<Ble_item> listData = new ArrayList<>();
    private Map<String,Integer> BLE_HASHMAP;
    private Map<String,Object> SEND_HASHMAP;
    private boolean ble_sned_Boolean = false;
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            if (result.getDevice().getName() != null) {

                BluetoothDevice bluetoothDevice = result.getDevice();
                if (bluetoothDevice.getName().startsWith("TJ-")) {
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
                        if ( DeviceNameArray.length >= 3) {
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
                    getEllapse();
                }
            }
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
        }
    };
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
            // writeLog(Network_data);
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
              Send_Http_post();
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
            invalidateOptionsMenu();
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
        invalidateOptionsMenu();
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
                if(SEND_HASHMAP==null){
                    Log.e("dd-209", "282");
                    return;
                }
                JSONObject cred = new JSONObject(SEND_HASHMAP);
                try {
                    cred.put("user_id", "minu@nave");
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
                        String env_O2_alarm = job.getString("env_O2_alarm");//
                        String env_CO = job.getString("env_CO");//수신
                        String env_CO_alarm = job.getString("env_CO_alarm");//
                        String env_H2S = job.getString("env_H2S");//퇴실
                        String env_H2S_alarm = job.getString("env_H2S_alarm");
                        String env_Co2 = job.getString("env_Co2");//배터리
                        String env_Co2_alarm = job.getString("env_Co2_alarm");//
                        String env_CH4 = job.getString("env_CH4");//상태 코드
                        String env_CH4_alarm = job.getString("env_CH4_alarm");
                        String env_user = job.getString("user");//상태 코드
                        String buildlevel_name = build_name+" "+level_name+" "+zone_name;
                        String zone_data = "O2 : " + env_O2 + "\n"
                                + "CO : " + env_CO + "\n"
                                + "H2S : " + env_H2S + "\n"
                                + "CO2 : " + env_Co2 + "\n"
                                + "CH4 : " + env_CH4;
                        Runnable runnable_location_textView = new Runnable() {
                            @Override
                            public void run() {
                                location_textView.setText(buildlevel_name);
                                data_textView.setText(zone_data);
                                user_textView.setText(env_user);
                            }
                        };
                        textchange_handler.postDelayed(runnable_location_textView, 0);
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
    private Sensor mBarometer; // 기압계
    private ArrayList<Float> PRESSURE_add = new ArrayList<>();
    private float PRESSURE_avg;

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
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PRESSURE) {//기압
            //     long timestamp = sensorEvent.timestamp;
            float presure = sensorEvent.values[0];
            presure = (float) (Math.round(presure * 100) / 100.0); //소수점 2자리 반올림
            //   Log.e("TYPE_PRESSURE", String.valueOf(presure));
            //   sector_list_adapter.notifyDataSetChanged();
            //기압을 바탕으로 고도를 계산(맞는거 맞아???)
            //float height = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, presure);
            PRESSURE_add.add(presure);
            if(getTime(SensorbaseTime)>=10) {
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

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates();
            }
        });
    }
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private final LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
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
    };
    private Button.OnClickListener mClickListener = new View.OnClickListener() {//각 버튼 클릭리스너
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

            }
        }
    };
    @Override
    protected void onStart() {
        super.onStart();
        Log.e("connect_TAG", "onStart()");
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.e("connect_TAG", "onResume");
        startScan();
       /* IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);//ble 상태 감지 필터
        registerReceiver(mBroadcastReceiver1, filter1);
        IntentFilter filter2 = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);//gps 상태감지 필터
        registerReceiver(mBroadcastReceiver1, filter2);
        IntentFilter filter3 = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);//인터넷 상태 감지 필터
        registerReceiver(mBroadcastReceiver1, filter3);*/
    }


    @Override
    protected void onPause() {
        Log.e("connect_TAG", "onPause");
        super.onPause();
        stopScan();
        mSensorManager.unregisterListener(this);
      /*
       isonoff=true;
      if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (mServiceAdvertiser != null) {
            mServiceAdvertiser.stopAdvertising(mService_AdvCallback);
        }*/


    }

    @Override
    protected void onStop() {
        Log.e("connect_TAG", "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("connect_TAG", "onDestroy()");
    }

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    private boolean startForeground = false;
    private LocationManager locationManager;
    private void startService() {
        if (!startForeground) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                startForeground = true;
                invalidateOptionsMenu();
                Intent serviceIntent1 = new Intent(getApplicationContext(), Background_Service.class);
                serviceIntent1.setAction(ACTION_START_FOREGROUND_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent1);
                } else {
                    startService(serviceIntent1);
                }
            }
        }
    }

    private void stopService() {
        //if(startForeground) {
        startForeground = false;
        invalidateOptionsMenu();
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            Log.e("activityt_TAGstop", service.service.getClassName());
            if ("com.nineone.inner_s_tool.Background_Service".equals(service.service.getClassName())) {
                Log.e("activityt_TAGstop2", service.service.getClassName());
                //log.e("stopService", "stopService");
                Intent serviceIntent1 = new Intent(getApplicationContext(), Background_Service.class);
                serviceIntent1.setAction(ACTION_STOP_FOREGROUND_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent1);
                } else {
                    startService(serviceIntent1);
                }
            }
        }
        // }
    }

    private final MyHandler textchange_handler = new MyHandler(this);
    private final MyHandler start_handler = new MyHandler(this);
    private static class MyHandler extends Handler {
        private final WeakReference<MainSectorEntranceActivity> mActivity;

        public MyHandler(MainSectorEntranceActivity activity) {
            mActivity = new WeakReference<MainSectorEntranceActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            MainSectorEntranceActivity activity = mActivity.get();
        }
    }
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                //log.e("off1", action);
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        invalidateOptionsMenu();
                        Toast.makeText(getApplication(), "블루투스가 종료되었습니다.\n 블루투스를 실행시켜 주세요 ", Toast.LENGTH_SHORT).show();
                        //log.e("off1", "off1");
                        //mble_gps_false = true;
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //log.e("off2", "off2");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        invalidateOptionsMenu();

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
                    invalidateOptionsMenu();
                    //log.e("off7", String.valueOf(isGpsEnabled));
                } else {
                    invalidateOptionsMenu();
                    //  mblecheck = false;
                    Toast.makeText(getApplication(), "GPS가 종료되었습니다.\n GPS를 실행시켜 주세요 ", Toast.LENGTH_SHORT).show();
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
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("퇴장 확인");
        builder.setMessage("정말로 퇴장하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              //  moveTaskToBack(true); // 태스크를 백그라운드로 이동
                finish(); // 액티비티 종료 + 태스크 리스트에서 지우기
                //android.os.Process.killProcess(android.os.Process.myPid()); // 앱 프로세스 종료
            }
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }
}