package com.nineone.inner_s_tool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private SimpleDateFormat timeformat;
    private SimpleDateFormat aftertime;
    private long RescanBaseTime;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private static final int REQUEST_ENABLE_BT = 2;//ble 켜져있는지 확인
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RescanBaseTime = SystemClock.elapsedRealtime();
        textView = findViewById(R.id.TextView);
        bluetoothCheck();
        google_gps();
        startTimerTask();
    }
    private ArrayList<Ble_item> listData = new ArrayList<>();
    private Map<String,Integer> BLA_HASHMAP;
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
                        Log.e("rssid", device.getTag_Name()+", "+device.getTag_Rssi_arrary().size());
                        Log.e("getTag_Rssi", String.valueOf(device.getTag_Rssi_arrary()));
                    }
                    if (!contains) {
                        String[] DeviceNameArray = bluetoothDevice.getName().trim().split("-");
                        if ( DeviceNameArray.length >= 3) {
                            Log.e("rssid2", String.valueOf(result.getRssi()));
                            listData.add(new Ble_item(bluetoothDevice.getAddress(), bluetoothDevice.getName(), result.getRssi()));
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

    private Timer timer = new Timer();
    private void startTimerTask() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Network_Confirm();
                // Log.e("Min10", String.valueOf(Min10())+" , "+getTime());
            }
        },0, 1000);

    }
    public void stopTimerTask() {//타이머 스톱 함수
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

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
           // writeLog(Network_data);
            Log.e("연결 안됨.", "654");
        }
    }

    private void ble_hashmap_add(){
        BLA_HASHMAP = new HashMap<>();
        for (Ble_item device : listData) {
            int sum = 0;
            int avg = 0;
            for (int i = 0; i < device.getTag_Rssi_arrary().size(); i++) {
                sum += device.getTag_Rssi_arrary().get(i);
            }
            avg = sum / device.getTag_Rssi_arrary().size();
            BLA_HASHMAP.put(device.getTag_Name(),avg);
        }
        Log.e("dd-", String.valueOf(BLA_HASHMAP));
        listData = new ArrayList<>();
        Http_post();
    }

    public void Http_post() {

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

                JSONObject cred = new JSONObject();
                try {
                    cred.put("user_id", "minu");
                    cred.put("pressure", "996.2099");
                    cred.put("lat", String.valueOf(latitude));
                    cred.put("lng", String.valueOf(longitude));
                    cred.put("ble", String.valueOf(BLA_HASHMAP));
                    cred.put("mobile_time", String.valueOf(SystemClock.elapsedRealtime()));
                    Log.e("dd-", "187");
                } catch (JSONException e) {
                    Log.e("dd-189", "\n" + e.getMessage());
                    e.printStackTrace();
                }
                array.put(cred);
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
    private void reScan() {
        if (mBluetoothLeScanner != null) {
            // stopScan();
            mBluetoothLeScanner.stopScan(leScanCallback);
            mBluetoothLeScanner.startScan(leScanCallback);

        }
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
    private void bluetoothCheck() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        // mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) { // 블루투스 활성화를 취소를 클릭하였다면
                //       mblecheck=false;

            } else {
                Toast.makeText(getApplicationContext(), "블루투스를 활성화 하여 주세요 ", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("종료 확인");
        builder.setMessage("정말로 종료하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                moveTaskToBack(true); // 태스크를 백그라운드로 이동
                finish(); // 액티비티 종료 + 태스크 리스트에서 지우기
                android.os.Process.killProcess(android.os.Process.myPid()); // 앱 프로세스 종료
            }
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }
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
    }

    @Override
    protected void onPause() {
        Log.e("connect_TAG", "onPause");
        super.onPause();
        stopScan();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        stopTimerTask();

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
}