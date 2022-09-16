package com.nineone.dummy_data_send;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private TextView textView1, textView2, textView3, mmiiliscUV, mmiilisRF, mErrerUV, mErrerRF;
    private TextView UV_url_text, UV_port_text, UV_path_text;
    private TextView RF_path_text;
    private TextView ID_name_text;
    private Button ID_name_button;
    private Button button1;
    private boolean mInformation_boolean = false;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        UV_url_text = findViewById(R.id.UVadress);
        UV_port_text = findViewById(R.id.UVport);
        UV_path_text = findViewById(R.id.UVpath);
        RF_path_text = findViewById(R.id.RFpath);
        ID_name_text = findViewById(R.id.IDname);
        ID_name_button = findViewById(R.id.SendButton);
        textView1 = findViewById(R.id.TextView1);
        textView2 = findViewById(R.id.UVcount);
        textView3 = findViewById(R.id.RFcount);
        mmiiliscUV = findViewById(R.id.miiliscUV);
        mmiilisRF = findViewById(R.id.miiliscRF);
        mErrerUV = findViewById(R.id.errerUV);
        mErrerRF = findViewById(R.id.errerRF);
        button1 = findViewById(R.id.Button1);
        SharedPreferences sf = getSharedPreferences("Change_settings", MODE_PRIVATE); //text라는 key에 저장된 값이 있는지 확인. 아무값도 들어있지 않으면 ""를 반환

        UV_url_string = sf.getString("UV_url", "stag.nineone.com");
        UV_port_string = sf.getString("UV_port", "9988");
        UV_path_string = sf.getString("UV_path", "uv");
        RF_path_string = sf.getString("RF_path", "rf");
        ID_name_string = sf.getString("ID_name", "edankim72");

        UV_url_text.setText(UV_url_string);
        UV_port_text.setText(UV_port_string);
        UV_path_text.setText(UV_path_string);

        RF_path_text.setText(RF_path_string);

        ID_name_text.setText(ID_name_string);
        UV_url_text.setOnLongClickListener(mClickListener);
        UV_port_text.setOnLongClickListener(mClickListener);
        UV_path_text.setOnLongClickListener(mClickListener);
        RF_path_text.setOnLongClickListener(mClickListener);
        ID_name_text.setOnLongClickListener(mClickListener);
        ID_name_button.setOnLongClickListener(mClickListener);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mInformation_boolean) {
                    textView1.setText("전송 중");
                    button1.setText("멈춤");

                    mErrerRF.setText("");

                    mErrerUV.setText("");
                    mInformation_boolean = true;
                    list_set_Http();
                } else {
                    a = 0;
                    b = 0;
                    textView1.setText("전송 멈춤");
                    button1.setText("시작");

                    mInformation_boolean = false;
                    stopTimerTask();
                }


            }
        });

        bluetoothCheck();
    }

    private static final int REQUEST_ENABLE_BT = 2;//ble 켜져있는지 확인

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
        //  stopScan();
        //log.e("BLE1245", "130");
    }

    private long RescanBaseTime;

    private void list_set_Http() {
        stopTimerTask();
        Runnable list_change_runnable = new Runnable() {
            @Override
            public void run() {

                startTimerTask1();

            }
        };
        listcange_handler.postDelayed(list_change_runnable, 0);

        // }
    }

    private TimerTask timerTask;
    private Timer timer = new Timer();
    private long a = 0;
    private long b = 0;
    private boolean change = false;
    private int period = 1000;
    int[] intram = {1, 2, 3, 4};
    Random random = new Random();

    private void startTimerTask1() {
        timerTask = new TimerTask() {
            @Override
            public void run() { // 코드 작성

                if (!change) {
                    // a++;
                    change = true;
                    recordUV_check_in_Http_post();
                    textView2.setText(String.valueOf(a));
                } else {
                    //   b++;
                    change = false;
                    recordRF_check_in_Http_post();
                    textView3.setText(String.valueOf(b));
                }
            }
        };
        timer.schedule(timerTask, 1000, 1000);
    }

    private void recordUV_check_in_Http_post() {
        new Thread(() -> {
            try {
                Log.e("dd-", "164");
                String url = "http://" + UV_url_string + ":" + UV_port_string + "/" + UV_path_string;
                Log.e("dd-uvurl", url);
                URL object = null;
                object = new URL(url);
                Log.e("dd-", "168");
                HttpURLConnection con = null;

                con = (HttpURLConnection) object.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("PUT");
                JSONArray array = new JSONArray();

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                String mstart_name = sp.getString("startname", "");
                int n = random.nextInt(intram.length - 1);
                int arandom = intram[n];
                if (SEND_HASHMAP == null) {
                    Runnable runnableUV1 = new Runnable() {
                        @Override
                        public void run() {

                            mErrerUV.setText("BLE 신호 없음");
                        }
                    };
                    UVchange_handler.postDelayed(runnableUV1, 0);

                    return;
                }
                for (int i = 1; i <= arandom; i++) {
                    JSONObject cred = new JSONObject();
                    try {
                        cred.put("user_id", ID_name_string);
                        //cred.put("ble", SEND_HASHMAP)
                        cred.put("mobile_time", String.valueOf(System.currentTimeMillis()));
                       // Log.e("SystemA", String.valueOf(System.currentTimeMillis()));
                        cred.put("index", a + i);
                        Log.e("aabradom_A", a + ", " + (a + i));
                        cred.put("length", i);
                        cred.put("heading", 0);
                        cred.put("looking", true);

                        Log.e("dd-187", "187");
                    } catch (JSONException e) {
                        Runnable runnableUV2 = new Runnable() {
                            @Override
                            public void run() {

                                mErrerUV.setText(e.getMessage());
                            }
                        };
                        UVchange_handler.postDelayed(runnableUV2, 0);
                        Log.e("dd-189", "\n" + e.getMessage());
                        e.printStackTrace();
                    }
                    Log.e("dd-264", array.toString());
                    array.put(cred);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Log.e("dd-265", e.getMessage());
                        e.printStackTrace();
                    }
                }
                //  Log.e("aabradom_A",b + ", "+(arandom-1)+", "+arandom);
                a += (arandom);

                Log.e("dd-513", array.toString());
                OutputStream os = con.getOutputStream();
                Log.e("dd-514", array.toString());
                long uvmillistime = System.currentTimeMillis();
                os.write(array.toString().getBytes("UTF-8"));
                os.close();
                StringBuilder sb = new StringBuilder();
                int HttpResult = con.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    Runnable runnableUV3 = new Runnable() {
                        @Override
                        public void run() {
                            mErrerUV.setText("HTTP_OK " + String.valueOf(HttpResult));
                            mmiiliscUV.setText(Millis_time(uvmillistime));
                        }
                    };
                    UVchange_handler.postDelayed(runnableUV3, 0);


                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));

                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    if (sb.toString().length() != 0) {
                        Runnable runnableUV4 = new Runnable() {
                            @Override
                            public void run() {
                                mErrerUV.setText(sb.toString());
                            }
                        };
                        UVchange_handler.postDelayed(runnableUV4, 0);
                    }

                    Log.e("dd-515", sb.toString());
                    br.close();
                  /*  try {
                        JSONObject job = new JSONObject(sb.toString());
                        boolean return_result = job.getBoolean("result");
                        String return_zone_name = job.getString("zone_name");

                        Log.e("return_result2", return_result + "," + return_zone_name);
                    } catch (JSONException e) {
                        mErrerUV.setText(e.getMessage());
                        Log.e("dd-211", "\n" + e.getMessage());
                        // Handle error
                    }*/
                } else {
                    HttpURLConnection finalCon = con;
                    Runnable runnableUV5 = new Runnable() {
                        @Override
                        public void run() {

                            try {
                                mErrerUV.setText(String.valueOf(HttpResult) + " " + finalCon.getResponseMessage());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    UVchange_handler.postDelayed(runnableUV5, 0);
                    Log.e("dd-212", "\n" + con.getResponseMessage());
                }
            } catch (IOException e) {
                Log.e("dd-215", e.getMessage());
                Runnable runnableUV6 = new Runnable() {
                    @Override
                    public void run() {
                        mErrerUV.setText(e.getMessage());
                    }
                };
                UVchange_handler.postDelayed(runnableUV6, 0);

                e.printStackTrace();
            }
        }).start();
    }

    private void recordRF_check_in_Http_post() {
        new Thread(() -> {
            try {
                Log.e("dd-", "164");
                String url = "http://" + UV_url_string + ":" + UV_port_string + "/" + RF_path_string;
                URL object = null;
                object = new URL(url);
                Log.e("dd-", "168");
                HttpURLConnection con = null;

                con = (HttpURLConnection) object.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("PUT");
                JSONArray array = new JSONArray();

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                String mstart_name = sp.getString("startname", "");
                int n = random.nextInt(intram.length - 1);
                int brandom = intram[n];
                if (SEND_HASHMAP == null) {
                    Runnable runnableRF1 = new Runnable() {
                        @Override
                        public void run() {

                            mErrerRF.setText("BLE 신호 없음");
                        }
                    };
                    RFchange_handler.postDelayed(runnableRF1, 0);
                    return;
                }
                for (int i = 1; i <= brandom; i++) {
                    JSONObject cred = new JSONObject(SEND_HASHMAP);
                    try {
                        cred.put("user_id", ID_name_string);
                        //cred.put("ble", SEND_HASHMAP)
                        cred.put("mobile_time", String.valueOf(System.currentTimeMillis()));
                       // Log.e("SystemB", String.valueOf(System.currentTimeMillis()));
                        cred.put("pressure", b + i);
                        // Log.e("aabradom_B", String.valueOf((b+i)));
                        // Log.e("dd-187u", "187");
                    } catch (JSONException e) {
                        Runnable runnableRF2 = new Runnable() {
                            @Override
                            public void run() {

                                mErrerRF.setText(e.getMessage());
                            }
                        };
                        RFchange_handler.postDelayed(runnableRF2, 0);
                        Log.e("dd-189u", "\n" + e.getMessage());
                        e.printStackTrace();
                    }
                    array.put(cred);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                b += brandom;
                // Log.e("aabradom_B",b + ", "+brandom);
                OutputStream os = con.getOutputStream();
                Log.e("dd-514u", array.toString());
                os.write(array.toString().getBytes("UTF-8"));
                long rfmillistime = System.currentTimeMillis();
                os.close();
                StringBuilder sb = new StringBuilder();
                int HttpResult = con.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    Runnable runnableRF3 = new Runnable() {
                        @Override
                        public void run() {
                            mErrerRF.setText("HTTP_OK " + String.valueOf(HttpResult));
                            mmiilisRF.setText(Millis_time(rfmillistime));//여기가 원인
                        }
                    };
                    RFchange_handler.postDelayed(runnableRF3, 0);


                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));

                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    Log.e("dd-515u", sb.toString());
                    if (sb.toString().length() != 0) {

                        Runnable runnableRF4 = new Runnable() {
                            @Override
                            public void run() {
                                mErrerRF.setText(sb.toString());
                            }
                        };
                        RFchange_handler.postDelayed(runnableRF4, 0);
                    }

                    br.close();
                 /*   try {
                        JSONObject job = new JSONObject(sb.toString());
                        boolean return_result = job.getBoolean("result");
                        String return_zone_name = job.getString("zone_name");

                        Log.e("return_result2", return_result + "," + return_zone_name);
                    } catch (JSONException e) {
                        mErrerRF.setText(e.getMessage());

                        Log.e("dd-211u", "\n" + e.getMessage());
                        // Handle error
                    }*/
                } else {
                    HttpURLConnection finalCon = con;
                    Runnable runnableRF5 = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mErrerRF.setText(String.valueOf(HttpResult) + " " + finalCon.getResponseMessage());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    RFchange_handler.postDelayed(runnableRF5, 0);
                    Log.e("dd-212u", "\n" + con.getResponseMessage());
                }
            } catch (IOException e) {
                Runnable runnableRF6 = new Runnable() {
                    @Override
                    public void run() {
                        mErrerRF.setText(e.getMessage());
                    }
                };
                RFchange_handler.postDelayed(runnableRF6, 0);
                Log.e("dd-215u", e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void stopTimerTask() {//타이머 스톱 함수
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
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

    private String Millis_time(long millis_time) {
        long systemmillis = System.currentTimeMillis() - millis_time;
        String stringmillis = String.valueOf(systemmillis);
        return stringmillis;
    }

    private ArrayList<Ble_item> listData = new ArrayList<>();
    private Map<String, Integer> BLE_HASHMAP;
    private Map<String, Object> SEND_HASHMAP;
    private boolean ble_sned_Boolean = false;

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

    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            if (result.getDevice().getName() != null) {

                BluetoothDevice bluetoothDevice = result.getDevice();
                if (bluetoothDevice.getName().startsWith("TJ-00CB")) {
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
        stopTimerTask();

    }

    private final MyHandler RFchange_handler = new MyHandler(this);
    private final MyHandler UVchange_handler = new MyHandler(this);
    private final MyHandler namechange_handler = new MyHandler(this);
    private final MyHandler listcange_handler = new MyHandler(this);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("종료 확인");
        builder.setMessage("정말로 종료하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }

    private View.OnLongClickListener mClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            if (v.getId() == R.id.UVadress) {
                final EditText editText = new EditText((MainActivity.this));
                editText.setGravity(Gravity.CENTER);
                editText.setText(UV_url_string);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("URL");
                alertDialog.setView(editText);
                alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (editText.getText().length() != 0) {
                            SharedPreferences sharedPreferences = getSharedPreferences("Change_settings", MODE_PRIVATE); //text라는 key에 저장된 값이 있는지 확인. 아무값도 들어있지 않으면 ""를 반환
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("UV_url", editText.getText().toString());
                            editor.apply();
                            Runnable runnable12 = new Runnable() {
                                @Override
                                public void run() {
                                    UV_url_string = sharedPreferences.getString("UV_url", "stag.nineone.com");
                                    UV_url_text.setText(UV_url_string);
                                }
                            };
                            listcange_handler.postDelayed(runnable12, 2);
                        } else {
                            Toast.makeText(getApplicationContext(), "URL를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                alertDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                alertDialog.show();
            }
            if (v.getId() == R.id.UVport) {
                final EditText editText = new EditText((MainActivity.this));
                editText.setGravity(Gravity.CENTER);
                editText.setText(UV_port_string);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Port");
                alertDialog.setView(editText);
                alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (editText.getText().length() != 0) {
                            SharedPreferences sharedPreferences = getSharedPreferences("Change_settings", MODE_PRIVATE); //text라는 key에 저장된 값이 있는지 확인. 아무값도 들어있지 않으면 ""를 반환
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("UV_port", editText.getText().toString());
                            editor.apply();
                            Runnable runnable12 = new Runnable() {
                                @Override
                                public void run() {
                                    UV_port_string = sharedPreferences.getString("UV_port", "9988");
                                    UV_port_text.setText(UV_port_string);
                                }
                            };
                            listcange_handler.postDelayed(runnable12, 2);
                        } else {
                            Toast.makeText(getApplicationContext(), "PORT를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                alertDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alertDialog.show();
            }
            if (v.getId() == R.id.UVpath) {
                final EditText editText = new EditText((MainActivity.this));
                editText.setGravity(Gravity.CENTER);
                editText.setText(UV_path_string);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("UV Path");
                alertDialog.setView(editText);
                alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (editText.getText().length() != 0) {
                            SharedPreferences sharedPreferences = getSharedPreferences("Change_settings", MODE_PRIVATE); //text라는 key에 저장된 값이 있는지 확인. 아무값도 들어있지 않으면 ""를 반환
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("UV_path", editText.getText().toString());
                            editor.apply();
                            Runnable runnable12 = new Runnable() {
                                @Override
                                public void run() {
                                    UV_path_string = sharedPreferences.getString("UV_path", "api/mobile/recordUV");
                                    UV_path_text.setText(UV_path_string);
                                }
                            };
                            listcange_handler.postDelayed(runnable12, 2);
                        } else {
                            Toast.makeText(getApplicationContext(), "PATH를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                alertDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                alertDialog.show();
            }
            if (v.getId() == R.id.RFpath) {
                final EditText editText = new EditText((MainActivity.this));
                editText.setGravity(Gravity.CENTER);
                editText.setText(RF_path_string);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("RF Path");
                alertDialog.setView(editText);
                alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (editText.getText().length() != 0) {
                            SharedPreferences sharedPreferences = getSharedPreferences("Change_settings", MODE_PRIVATE); //text라는 key에 저장된 값이 있는지 확인. 아무값도 들어있지 않으면 ""를 반환
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("RF_path", editText.getText().toString());
                            editor.apply();
                            Runnable runnable12 = new Runnable() {
                                @Override
                                public void run() {
                                    RF_path_string = sharedPreferences.getString("RF_path", "api/mobile/recordRF");
                                    RF_path_text.setText(RF_path_string);
                                }
                            };
                            listcange_handler.postDelayed(runnable12, 2);
                        } else {
                            Toast.makeText(getApplicationContext(), "주소를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                alertDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                alertDialog.show();
            }
            if (v.getId() == R.id.IDname) {
                final EditText editText = new EditText((MainActivity.this));
                editText.setGravity(Gravity.CENTER);
                editText.setText(ID_name_string);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("ID");
                alertDialog.setView(editText);

                alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (editText.getText().length() != 0) {
                            SharedPreferences sharedPreferences = getSharedPreferences("Change_settings", MODE_PRIVATE); //text라는 key에 저장된 값이 있는지 확인. 아무값도 들어있지 않으면 ""를 반환
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("ID_name", editText.getText().toString());
                            editor.apply();
                            Runnable runnable12 = new Runnable() {
                                @Override
                                public void run() {
                                    ID_name_string = sharedPreferences.getString("ID_name", "edankim72");
                                    ID_name_text.setText(ID_name_string);
                                }
                            };
                            listcange_handler.postDelayed(runnable12, 2);
                        } else {
                            Toast.makeText(getApplicationContext(), "ID를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                alertDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                alertDialog.show();
            }
            if (v.getId() == R.id.SendButton) {
                ID_name_in_Http_post();
            }
            return false;
        }
    };
    private String UV_url_string, UV_port_string, UV_path_string;
    private String RF_path_string;
    private String ID_name_string;

    private void ID_name_in_Http_post() {
        new Thread(() -> {
            try {
                Log.e("dd-", "164");
                String url = "http://" + UV_url_string + ":" + UV_port_string + "/" + "user";
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
               // JSONArray array = new JSONArray();

                JSONObject cred = new JSONObject();
                try {
                    cred.put("user_id", ID_name_string);
                    //cred.put("ble", SEND_HASHMAP)
                    cred.put("device_model", Build.MODEL);
                    Log.e("SystemB", Build.MODEL);
                    cred.put("os_version", Build.VERSION.SDK_INT);
                    // Log.e("aabradom_B", String.valueOf((b+i)));
                    // Log.e("dd-187u", "187");
                } catch (JSONException e) {

                    Log.e("dd-189u", "\n" + e.getMessage());
                    e.printStackTrace();
                }
                //array.put(cred);
                OutputStream os = con.getOutputStream();
                Log.e("dd-514u", cred.toString());
                os.write(cred.toString().getBytes("UTF-8"));
                long rfmillistime = System.currentTimeMillis();
                os.close();
                StringBuilder sb = new StringBuilder();
                int HttpResult = con.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {

                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), "utf-8"));
                    mmiilisRF.setText(Millis_time(rfmillistime));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    Log.e("dd-515u", sb.toString());
                    if (sb.toString().length() != 0) {
                       boolean success = false;
                        String errors = null;
                        String message = null;
                        try {
                            JSONObject job = new JSONObject(sb.toString());
                            success = job.getBoolean("success");
                            errors = job.getString("errors");
                            message = job.getString("message");
                            String make;
                            if(success){
                                make = "Success "+message;
                            }else{
                                make = errors+", "+message;
                            }
                            Log.e("return_result2", success + "," + errors+","+message);
                            Runnable runnableRF4 = new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                    alertDialog.setTitle("ID 전송");
                                    alertDialog.setMessage(make);
                                    alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    });
                                    alertDialog.show();
                                }
                            };
                            namechange_handler.postDelayed(runnableRF4, 0);
                        } catch (JSONException e) {

                            Log.e("dd-211u", "\n" + e.getMessage());
                            // Handle error
                        }



                    }
                    br.close();

                } else {
                    HttpURLConnection finalCon = con;

                    Log.e("dd-212u", HttpResult+con.getResponseMessage());
                }
            } catch (IOException e) {
                Runnable runnableRF6 = new Runnable() {
                    @Override
                    public void run() {

                    }
                };
                namechange_handler.postDelayed(runnableRF6, 0);
                Log.e("dd-215u", e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}