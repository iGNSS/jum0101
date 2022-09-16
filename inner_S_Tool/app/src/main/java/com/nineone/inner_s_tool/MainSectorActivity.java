package com.nineone.inner_s_tool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainSectorActivity extends AppCompatActivity implements SensorEventListener {
    private long RescanBaseTime;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private static final int REQUEST_ENABLE_BT = 2;//ble 켜져있는지 확인
    private static SensorManager mSensorManager;
    private TextView CAG_button, Information_button;//coming and going
    private boolean mInformation_boolean = false;

    private long SensorbaseTime;
    private long List_update_Time;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sector);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("삼성 중공업");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        SensorbaseTime = SystemClock.elapsedRealtime();
        List_update_Time = SystemClock.elapsedRealtime();
        CAG_button = findViewById(R.id.coming_and_going);
        CAG_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInformation_boolean) {
                    CAG_button.setSelected(true);
                    Information_button.setSelected(false);
                    mInformation_boolean = false;
                    recyclerViewAdapter.item_Clear();
                    list_set_Http();
                }


            }
        });
        CAG_button.setSelected(true);
        Information_button = findViewById(R.id.Information_button);
        Information_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mInformation_boolean) {
                    CAG_button.setSelected(false);
                    Information_button.setSelected(true);
                    mInformation_boolean = true;
                    recyclerViewAdapter.item_Clear();
                    list_set_Http();
                }
            }
        });
        mInformation_boolean = false;
        RecyclerViewlayout();
        bluetoothCheck();
        buttonSwitchGPS_ON();
        senser_check();

    }

    private RecyclerView mSector_RecyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;

    private void RecyclerViewlayout() {
        mSector_RecyclerView = (RecyclerView) findViewById(R.id.sector_RecyclerView);
        mSector_RecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mSector_RecyclerView.setItemAnimator(null);
        mSector_RecyclerView.setLayoutManager(linearLayoutManager);
        recyclerViewAdapter = new RecyclerViewAdapter(getApplicationContext(), false);
        recyclerViewAdapter.item_noti();
        mSector_RecyclerView.setAdapter(recyclerViewAdapter);

        /*recyclerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {//리스트를 클릭했을때 이벤트
                if (position != RecyclerView.NO_POSITION) {
                    String[] LintNameArray = recyclerViewAdapter.sector_list_items().get(position).getItem_tag_name().split(" ");
                    Entrance_check_in_Http_post(LintNameArray[1]);
                    Log.e("dd-", LintNameArray[1] + " -143");

                   Intent intent = new Intent(MainSectorActivity.this, MainSectorEntranceActivity.class);
                    intent.putExtra("address", recyclerViewAdapter.sector_list_items().get(position).getItem_tag_name());
                    startActivity(intent);

                    // activity.finish();
                    //String mydatalist = myAdapter.getWordAtPosition(position);
                }
                // TODO : 아이템 클릭 이벤트를 MainActivity에서 처리.
            }
        });*/

        list_set_Http();
        recyclerViewAdapter.notifyDataSetChanged();
    }

    private void list_set_Http() {
        recyclerViewAdapter.item_Clear();
        stopTimerTask();
        Runnable list_change_runnable = new Runnable() {
            @Override
            public void run() {
                if (!mInformation_boolean) {
                    // recyclerViewAdapter = new RecyclerViewAdapter(getApplicationContext(), false);
                    recyclerViewAdapter.item_noti();
                    // mSector_RecyclerView.setAdapter(recyclerViewAdapter);
                    for (int i = 1; i <= 6; i++) {
                        recyclerViewAdapter.update("No " + i + " Confferdam", "", false, false);
                    }
                    recyclerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View v, int position) {//리스트를 클릭했을때 이벤트

                            int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                            if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainSectorActivity.this);
                                builder.setCancelable(false);
                                builder.setTitle("네트워크 오류");
                                builder.setMessage("네트워크가 꺼져 있어 입장이 불가합니다.");
                                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //  finish();
                                    }
                                });
                                builder.show();
                            } else {
                                if (position != RecyclerView.NO_POSITION) {
                                    String[] LintNameArray = recyclerViewAdapter.sector_list_items().get(position).getItem_tag_name().split(" ");
                                    Entrance_check_in_Http_post(LintNameArray[1]);
                                    Log.e("dd-", LintNameArray[1] + " -143");
                                }
                            }
                        }
                    });
                    startTimerTask1();
                } else {
                    // recyclerViewAdapter = new RecyclerViewAdapter(getApplicationContext(), true);
                    recyclerViewAdapter.item_noti();
                    // mSector_RecyclerView.setAdapter(recyclerViewAdapter);
                    int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                    if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainSectorActivity.this);
                        builder.setCancelable(false);
                        builder.setTitle("네트워크 오류");
                        builder.setMessage("네트워크가 꺼져 자세한 정보를 불러올 수 없습니다.");
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //  finish();
                            }
                        });
                        builder.show();
                    } else {
                        new Thread(() -> {
                            Get_Information_list_Http();
                        }).start();
                    }
                }
            }
        };
        listcange_handler.postDelayed(list_change_runnable, 0);

        // }
    }

    private TimerTask timerTask;
    private Timer timer = new Timer();
    private int period = 3000;

    private void startTimerTask1() {
        timerTask = new TimerTask() {
            @Override
            public void run() { // 코드 작성
                if (!Entrance_check) {
                    if (mInformation_boolean) {
                        period = 10000;
                        Get_Information_list_Http();
                    } else {
                        period = 3000;
                        work_location_check_Http_post();
                    }
                }
            }
        };
        timer.schedule(timerTask, 1000, period);
    }

    private void startTimerTask2() {
        timerTask = new TimerTask() {
            @Override
            public void run() { // 코드 작성
                if (!Entrance_check) {
                    if (!mInformation_boolean) {
                        Log.e("return_result1", "startTimerTask2");
                        work_location_check_Http_post();
                    }
                }
            }
        };
        timer.schedule(timerTask, 1000, 3000);
    }

    private void Get_Information_list_Http() {
        Document doc = null;
        try {
            Log.e("dd-", "164");
            String url = "http://stag.nineone.com:8005/api/mobile";
            doc = Jsoup.connect(url).ignoreContentType(true).get();
            Elements contents = doc.select("body");          //회차 id값 가져오기
            for (Element li : contents) {
                JSONObject jsonObject = new JSONObject(li.text());
                String jsondata;
                if (jsonObject.has("data")) {
                    jsondata = jsonObject.getString("data");
                } else {
                    jsondata = "N/A";
                }
                Log.e("dd-", jsondata + " -165");

                String jsontag_data = jsondata;
                JSONArray jsonArray = new JSONArray(jsontag_data);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject subJsonObject = jsonArray.getJSONObject(i);
                    String zone_name = subJsonObject.getString("zone_name");//xprmdlfma
                    String env_O2 = subJsonObject.getString("env_O2");//null이면 화면에 표시하지않음
                    String env_O2_alarm = subJsonObject.getString("env_O2_alarm");//
                    String env_CO = subJsonObject.getString("env_CO");//수신
                    String env_CO_alarm = subJsonObject.getString("env_CO_alarm");//
                    String env_H2S = subJsonObject.getString("env_H2S");//퇴실
                    String env_H2S_alarm = subJsonObject.getString("env_H2S_alarm");
                    String env_Co2 = subJsonObject.getString("env_Co2");//배터리
                    String env_Co2_alarm = subJsonObject.getString("env_Co2_alarm");//
                    String env_CH4 = subJsonObject.getString("env_CH4");//상태 코드
                    String env_CH4_alarm = subJsonObject.getString("env_CH4_alarm");//
                    String zone_data = "O2 : " + env_O2 + " %\n"
                            + "CO : " + env_CO + " ppm\n"
                            + "H2S : " + env_H2S + " ppm\n"
                            + "CO2 : " + env_Co2 + " ppm\n"
                            + "CH4 : " + env_CH4 + " ppm";
                    Log.e("zone_data1", zone_data);
                    if (mInformation_boolean) {
                        recyclerViewAdapter.update("No." + zone_name + " Confferdam", zone_data, false, true);
                        startTimerTask1();
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("dd-", e.getMessage() + " -166");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("dd-", e.getMessage() + " -167");
            e.printStackTrace();
        }
    }

    private void stopTimerTask() {//타이머 스톱 함수
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    private final MyHandler list_location_Confirm_handler = new MyHandler(this);
    private boolean Entrance_check = false;

    private void work_location_check_Http_post() {
        new Thread(() -> {
            try {
                Log.e("dd-", "164");
                String url = "http://stag.nineone.com:8005/api/scanentrance";
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

                    return;
                }
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                String mstart_name = sp.getString("startname", "");
                JSONObject cred = new JSONObject(SEND_HASHMAP);
                try {
                    cred.put("user_id", mstart_name);
                    cred.put("lat", String.valueOf(latitude));
                    cred.put("lng", String.valueOf(longitude));
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
                        String return_zone_name = job.getString("zone_name");

                        Runnable Entrance_fail_runnable = new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 1; i <= 6; i++) {
                                    if (!mInformation_boolean) {
                                        recyclerViewAdapter.update("No " + i + " Confferdam", "", return_zone_name.equals(String.valueOf(i)), false);
                                    }
                                }
                            }
                        };
                        list_location_Confirm_handler.postDelayed(Entrance_fail_runnable, 0);

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

    private final MyHandler data_send_handler = new MyHandler(this);

    private void Entrance_check_in_Http_post(String number) {
        new Thread(() -> {
            try {
                Entrance_check = true;
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
                    Log.e("dd-512", "282");
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "BLE 데이터 수집 중 입니다\n잠시 후 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
                        }
                    }, 0);
                    return;
                }
                if (Barometer_have_boolean) {
                    if (PRESSURE_avg == 0) {
                        Log.e("dd-513", "282");
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "기압 데이터 수집 중 입니다\n잠시 후 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
                            }
                        }, 0);
                        return;
                    }
                }
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                String mstart_name = sp.getString("startname", "");
                JSONObject cred = new JSONObject(SEND_HASHMAP);
                try {
                    cred.put("user_id", mstart_name);
                    cred.put("mean_pressure", PRESSURE_avg);
                    Log.e("PRESSURE_avg", String.valueOf(PRESSURE_avg));
                    //cred.put("ble", SEND_HASHMAP);
                    cred.put("zone_name", number);
                    cred.put("entrance_check", true);
                    Log.e("dd-187", "187");
                } catch (JSONException e) {
                    Log.e("dd-189", "\n" + e.getMessage());
                    e.printStackTrace();
                }
                array.put(cred);
                //    JSONObject cred2 = new JSONObject(SEND_HASHMAP);
                //  array.put(cred2);
                OutputStream os = con.getOutputStream();
                Log.e("dd-514", array.toString());
                os.write(array.toString().getBytes("UTF-8"));
                os.close();
                //display what returns the POST request

                //  Intent intent = new Intent(MainSectorActivity.this, MainSectorEntrance_Activity.class);
                // intent.putExtra("zone_name_num", 6);
                //  startActivity(intent);

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
                    Log.e("dd-515", sb.toString());
                    try {
                        JSONObject job = new JSONObject(sb.toString());
                        boolean return_result = job.getBoolean("result");
                        String return_zone_name = job.getString("zone_name");

                        Log.e("return_result2", return_result + "," + return_zone_name);
                        if (return_result) {
                            runOnUiThread(new Runnable() {//약간의 딜레이
                                @Override
                                public void run() {
                                    ProgressDialog mProgressDialog = ProgressDialog.show(MainSectorActivity.this, "", "입장 중 입니다.", true);
                                    data_send_handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                                    mProgressDialog.dismiss();
                                                    stopScan();
                                                    Intent intent = new Intent(MainSectorActivity.this, MainSectorEntrance_Activity.class);
                                                    //  intent.putExtra("zone_name_num", return_zone_name);

                                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                                    //저장을 하기위해 Editor를 불러온다.
                                                    SharedPreferences.Editor edit = preferences.edit();
                                                    edit.putString("Shared_zone_name_num", return_zone_name);
                                                    edit.apply();

                                                    startActivity(intent);
                                                    finish();
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }, 1000);
                                }
                            });

                        } else {
                            Runnable Entrance_fail_runnable = new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainSectorActivity.this);
                                    builder.setCancelable(false);
                                    builder.setTitle("위치 오류");
                                    builder.setMessage("현재 위치는 No " + return_zone_name + " Confferdam 입니다" + "\n확인 후 재입장 해 주세요");
                                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //  finish();
                                        }
                                    });
                                    builder.show();
                                }
                            };
                            Entrance_fail_handler.postDelayed(Entrance_fail_runnable, 0);
                        }
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

    private ArrayList<Ble_item> listData = new ArrayList<>();
    private Map<String, Integer> BLE_HASHMAP;
    private Map<String, Object> SEND_HASHMAP;
    private boolean ble_sned_Boolean = false;
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            if (result.getDevice().getName() != null) {

                BluetoothDevice bluetoothDevice = result.getDevice();
                if (bluetoothDevice.getName().startsWith("TJ-")) {
                    //  Log.e("rssid1", String.valueOf(result.getRssi()));
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
                            ble_hashmap_add();
                            // Network_Confirm();
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

    private void ble_hashmap_add() {
        BLE_HASHMAP = new HashMap<>();
        for (Ble_item device : listData) {
            int sum = 0;
            int avg = 0;
            for (int i = 0; i < device.getTag_Rssi_arrary().size(); i++) {
                sum += device.getTag_Rssi_arrary().get(i);
            }
            avg = sum / device.getTag_Rssi_arrary().size();
            BLE_HASHMAP.put(device.getTag_Name(), avg);
        }
        SEND_HASHMAP = new HashMap<>();
        SEND_HASHMAP.put("ble", BLE_HASHMAP);
        //  Log.e("dd-", String.valueOf(listData));
        //  Log.e("dd-", String.valueOf(BLE_HASHMAP));

        if (listData.size() != 0) {
            listData = new ArrayList<>();
            //  Send_Http_post();
        }
        ble_sned_Boolean = false;
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

                JSONObject cred = new JSONObject(SEND_HASHMAP);
                try {
                    cred.put("user_id", "minu@nave");
                    cred.put("mean_pressure", PRESSURE_avg);
                    //cred.put("ble", SEND_HASHMAP);
                    cred.put("zone_name", "1");
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
                        String build_name = job.getString("result");
                        String level_name = job.getString("zone_name");

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

    private Sensor mBarometer; // 기압계
    private ArrayList<Float> PRESSURE_add = new ArrayList<>();
    private float PRESSURE_avg;
    private boolean Barometer_have_boolean = true;

    private void senser_check() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mBarometer = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);//기압계
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null) {
            Barometer_have_boolean = false;
            Log.e("기압없음", "763");
        }
        mSensorManager.registerListener(this, mBarometer, SensorManager.SENSOR_DELAY_UI);

    }

    private int SensorbaseTimecount = 5;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PRESSURE) {//기압
            //     long timestamp = sensorEvent.timestamp;
            Log.e("기압없음", "764");
            float presure = sensorEvent.values[0];
            presure = (float) (Math.round(presure * 100) / 100.0); //소수점 2자리 반올림
            PRESSURE_add.add(presure);
            if (getTime(SensorbaseTime) >= SensorbaseTimecount) {
                float sum = 0;
                int count = 0;
                for (float device : PRESSURE_add) {
                    sum += device;
                    count++;
                }
                SensorbaseTimecount = 10;
                PRESSURE_avg = sum / count;
                SensorbaseTime = SystemClock.elapsedRealtime();
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private long getTime(long timesec) {
        //경과된 시간 체크
        long nowTime = SystemClock.elapsedRealtime();
        //시스템이 부팅된 이후의 시간
        long overTime = nowTime - timesec;
        //
        // Log.e("overtime", String.valueOf(overTime));
        long sec = (overTime / 1000) % 60;
        long min = ((overTime / 1000) / 60) % 60;
        long hour = ((overTime / 1000) / 60) / 60;
        //  long ms = overTime % 1000;

        return sec;
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

    private final LocationCallback locationCallback = new LocationCallback() {
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
                bluetoothCheck();
                Toast.makeText(getApplicationContext(), "블루투스를 활성화 하여 주세요 ", Toast.LENGTH_SHORT).show();
                // finish();
            }
        }
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            Log.e("dd--921", "dd");
            if (resultCode == Activity.RESULT_OK) { // 블루투스 활성화를 취소를 클릭하였다면
                //       mblecheck=false;
                Log.e("dd--924", "dd");
            } else {
                buttonSwitchGPS_ON();
                Log.e("dd--926", "dd");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private final MyHandler Entrance_fail_handler = new MyHandler(this);
    private final MyHandler listcange_handler = new MyHandler(this);
    private final Handler mhandler1_startAdvertising = new Handler();
    private final Handler mhandler2_stopAdvertising = new Handler();
    private final MyHandler textchange_handler = new MyHandler(this);
    private final MyHandler start_handler = new MyHandler(this);


    private static class MyHandler extends Handler {
        private final WeakReference<MainSectorActivity> mActivity;

        public MyHandler(MainSectorActivity activity) {
            mActivity = new WeakReference<MainSectorActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainSectorActivity activity = mActivity.get();
        }
    }


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
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);//ble 상태 감지 필터
        registerReceiver(mBroadcastReceiver1, filter1);
        IntentFilter filter2 = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);//gps 상태감지 필터
        filter2.addAction(Intent.ACTION_PROVIDER_CHANGED);
        registerReceiver(mBroadcastReceiver1, filter2);

        IntentFilter filter3 = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);//인터넷 상태 감지 필터
        registerReceiver(mBroadcastReceiver1, filter3);


        startScan();
    }

    @Override
    protected void onPause() {
        Log.e("connect_TAG", "onPause");
        super.onPause();
        stopScan();
        stopTimerTask();
        mSensorManager.unregisterListener(this);
        try {
            unregisterReceiver(mBroadcastReceiver1);
        } catch (Exception ignored) {

        }
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

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.e("testdCONNECTEDgg", action + "," + intent.getAction());
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                //log.e("off1", action);
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        bluetoothCheck();
                        //  stopScan();
                        invalidateOptionsMenu();
                        Toast.makeText(getApplication(), "블루투스가 종료되었습니다.\n 블루투스를 실행시켜 주세요 ", Toast.LENGTH_SHORT).show();
                        //log.e("off1", "off1");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        stopScan();
                        //log.e("off2", "off2");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        invalidateOptionsMenu();
                        startScan();
                        //log.e("off3", "off3");
                        // startService();
                        // mblecheck = true;
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        //log.e("off4", "off4");
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
                    Log.e("testCONNECTEDg", "testCONNECTEDg");
                    //log.e("off7", String.valueOf(isGpsEnabled));
                } else {
                    // stopScan();
                    Log.e("testdCONNECTEDg", "testdCONNECTEDg");
                    invalidateOptionsMenu();
                    //  mblecheck = false;
                    buttonSwitchGPS_ON();
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
                    list_set_Http();
                    Log.e("testCONNECTED", "testCONNECTED");
                } else {
                    Log.e("testCONNECTED", "distestCONNECTED");
                    //   Toast.makeText(activity.getApplication(), "Internet connection is Off", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.blemenu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.REstart_scan).setVisible(true);
        if (mIsScanning) {
            menu.findItem(R.id.action_scan).setVisible(false);
            menu.findItem(R.id.action_stop).setVisible(true);
            menu.findItem(R.id.REstart_scan).setTitle("");
        } else {
            menu.findItem(R.id.action_scan).setVisible(true);
            menu.findItem(R.id.action_stop).setVisible(false);
            menu.findItem(R.id.REstart_scan).setTitle("재시작");
        }
        if ((mBluetoothAdapter == null) || (!mBluetoothAdapter.isEnabled()) || (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
            menu.findItem(R.id.action_scan).setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MainSectorActivity.this, Main_Card_Activity.class);
        startActivity(intent);
        finish(); // 액티비티 종료 + 태스크 리스트에서 지우기

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } if (itemId == R.id.REstart_scan) {
            if(!mIsScanning) {
                startScan();
            }else{
                stopScan();
            }
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    /*
      private AdvertiseData mService_AdvData;
        private AdvertiseSettings mService_AdvSettings;
        private BluetoothLeAdvertiser mServiceAdvertiser;
        private boolean isonoff = false;
        private boolean mService_Advertiserstart = false;
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

        private void not_network_ble_UUID_set() {
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
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    public void buttonSwitchGPS_ON() {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder();

        locationSettingsRequestBuilder.addLocationRequest(locationRequest);


        locationSettingsRequestBuilder.setAlwaysShow(true);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build());

        Log.e("dd--1172", "dd");
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.e("dd--1076", "dd");
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                if (e instanceof ResolvableApiException) {
                    Log.e("dd--1085", "dd");
                    try {
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        resolvableApiException.startResolutionForResult(MainSectorActivity.this, REQUEST_CHECK_SETTINGS);
                        Log.e("dd--1089", "dd");
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        Log.e("dd--1091", sendIntentException.getMessage());
                        sendIntentException.printStackTrace();
                    }
                }
            }
        });
    }
}