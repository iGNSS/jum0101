package com.nineone.inner_s_tool;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private TextView location_textView;
    private CardView start_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RescanBaseTime = SystemClock.elapsedRealtime();
        location_textView = findViewById(R.id.Location_TextView);
        start_button = findViewById(R.id.Start_Button);
        start_button.setOnClickListener(mClickListener);
        bluetoothCheck();

        //google_gps();
        //startTimerTask();
        //long now2 = System.currentTimeMillis();
       //Log.e("now2", now2+"");

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
                    Runnable runnable_ble_sned;
                    if (!ble_sned_Boolean) {
                        ble_sned_Boolean = true;
                        runnable_ble_sned = new Runnable() {
                            @Override
                            public void run() {

                                Network_Confirm();
                            }
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

    private Timer timer = new Timer();
    private void startTimerTask() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
               // Network_Confirm();
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
          //  Http_post();
        }
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

                JSONObject cred = new JSONObject(SEND_HASHMAP);
                try {
                    cred.put("user_id", "minu@nave");
                    cred.put("pressure", "996.2099");
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
                        String buildlevel_name = build_name+" "+level_name;
                        Runnable runnable_location_textView = new Runnable() {
                            @Override
                            public void run() {
                                location_textView.setText(buildlevel_name);
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
    //    ble_sned_Boolean = false;
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
    private AdvertiseData mService_AdvData;
    private AdvertiseSettings mService_AdvSettings;
    private BluetoothLeAdvertiser mServiceAdvertiser;
    private void main_ble() {

        BluetoothAdapter.getDefaultAdapter().setName("MO-"+"0000");
        mService_AdvSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(false)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                // .setTimeout(0)
                .build();
        if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            mServiceAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
            //   mServiceAdvertiser.startAdvertising(mService_AdvSettings, mService_AdvData, mService_AdvCallback);
        }
    }

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

    private final Handler mhandler1_startAdvertising = new Handler();
    private final Handler mhandler2_stopAdvertising = new Handler();
    private final MyHandler textchange_handler = new MyHandler(this);
    private final MyHandler start_handler = new MyHandler(this);
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
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
    private boolean isonoff = false;
    private boolean mService_Advertiserstart = false;
    private void not_network_ble_send() {
        if(!isonoff)  {
            // // Log.e("asd","asdasd");
            mhandler1_startAdvertising.postDelayed(new Runnable() {
                public void run() {
                    mServiceAdvertiser.startAdvertising(mService_AdvSettings, mService_AdvData, mService_AdvCallback);
                    Log.e("delay_check","mServiceAdvertiser");
                    // Log.e("BLE", "Discovery onScanResult011: " + mService_AdvCallback.toString());
                }
            }, 0);
            mhandler2_stopAdvertising.postDelayed(new Runnable() {
                public void run() {
                    mServiceAdvertiser.stopAdvertising(mService_AdvCallback);
                    mService_Advertiserstart = false;
                    // Log.e("BLE", "Discovery onScanResult012: " +  mService_AdvCallback.toString());
                }
            }, 250);
        }
    }

  /*  private void not_network_ble_UUID_set() {
        if (!mService_Advertiserstart) {
            Runnable runnable10;//.addServiceUuid(pUuid)
            runnable10 = new Runnable() {
                @Override
                public void run() {
                    ParcelUuid pUuid = new ParcelUuid(hiuuid_7);
                    mService_AdvData = new AdvertiseData.Builder()
                            .setIncludeDeviceName(true)
                            .setIncludeTxPowerLevel(false)
                            //.addServiceUuid(pUuid)
                            .addServiceData(pUuid, arrayBytes4)
                            .build();
                    not_network_ble_send();
                    // Log.e("UUID_service2", "asdasd2");
                }
            };

            //
        }
    }*/
    private Button.OnClickListener mClickListener = new View.OnClickListener() {//각 버튼 클릭리스너
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.Start_Button) {
                Intent intent = new Intent(MainActivity.this, MainSectorActivity.class);
                startActivity(intent);
                finish();
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
      //  startScan();
    }

    @Override
    protected void onPause() {
        Log.e("connect_TAG", "onPause");
        super.onPause();
        stopScan();
        isonoff=true;
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (mServiceAdvertiser != null) {
            mServiceAdvertiser.stopAdvertising(mService_AdvCallback);
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


    private final AdvertiseCallback mService_AdvCallback = new AdvertiseCallback() {
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            int statusText;
            switch (errorCode) {
                case ADVERTISE_FAILED_ALREADY_STARTED:
                    //statusText = R.string.status_advertising;
                    break;
                case ADVERTISE_FAILED_DATA_TOO_LARGE:
                    // statusText = R.string.status_advDataTooLarge;
                    break;
                case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    // statusText = R.string.status_advFeatureUnsupported;
                    break;
                case ADVERTISE_FAILED_INTERNAL_ERROR:
                    //statusText = R.string.status_advInternalError;
                    break;
                case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    //statusText = R.string.status_advTooManyAdvertisers;
                    break;
                default:
                    // statusText = R.string.status_notAdvertising;
            }
            location_textView.setText("전송오류");
            // mAdvStatus.setText(statusText);
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            //mAdvStatus.setText(R.string.status_advertising);
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.logout).setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // ignore
            return true;
        }else if (itemId == R.id.logout) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            //저장을 하기위해 Editor를 불러온다.
            SharedPreferences.Editor edit = preferences.edit();
            edit.putString("startname", "");
            edit.apply();
                Intent newIntent = new Intent(MainActivity.this, MainLoginActivity.class);
                startActivity(newIntent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}