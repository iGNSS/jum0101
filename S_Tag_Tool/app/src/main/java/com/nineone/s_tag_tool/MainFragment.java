package com.nineone.s_tag_tool;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ble_connect.Connect_Activity;
import com.ble_connect.NetworkStatus;
import com.ble_connect.Tag_tiem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainFragment extends Fragment {

    MainActivity activity;
    final public static String STAG_0001 = "0001";
    final public static String STAG_0007 = "0007";
    final public static String STAG_00C8 = "00C8";
    final public static String STAG_00C9 = "00C9";
    final public static String STAG_00CA = "00CA";
    final public static String STAG_00D5 = "00D5";
    final public static String STAG_001 = "001";
    final public static String STAG_002 = "002";
    final public static String STAG_005 = "005";
    final public static String STAG_007 = "007";
    final public static String STAG_003 = "003";
    final public static String STAG_010 = "010";
    final public static String STAG_100 = "100";
    final public static String STAG_011 = "011";
    final public static String STAG_012 = "012";
    final public static String STAG_013 = "013";
    final public static String STAG_021 = "021";
    final public static String STAG_024 = "024";
    final public static String STAG_201 = "201";
    final public static String STAG_202 = "202";
    final public static String STAG_203 = "203";
    final public static String STAG_220 = "220";
    final public static String STAG_903 = "903";


    long RescanBaseTime;

    private Spinner scanMinorId_spinner;
    
    private LinearLayout bletypeLayout; //adress ???????????? ????????? ????????????layout
    public static String BLE_type;
    static String sort_type = "name";
    private RadioGroup radioGroup;
    private RadioButton r_btn1, r_btn2;
    public static String file_name;

    public static boolean fos_open_flag_ble = false;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanSettings btLeScanSettings;

    private static final int REQUEST_ENABLE_BT = 2;//ble ??????????????? ??????

    private RecyclerViewAdapter recyclerVierAdapter;
    private RecyclerView deviceListView;
    private LinearLayout empty_stop_view; //???????????? ???????????? ??? ???
    private LinearLayout empty_scan_view;
    private boolean systemBoolean = false;

    private LocationManager locationManager;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//??? ???????????? ??????????????? ?????????????????? ?????????????????? ???????????????????????? getActivity???????????? ????????????????????????
        activity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
//?????? ????????? ???????????? ???????????????
        activity = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e("activityt_TAG", "onCreat");
//??????????????? ????????? ???????????????????????? ??????????????? ??????????????? ??????
        
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);//????????????????????? ?????? ????????????
        // bluetoothCheck();
        RescanBaseTime = SystemClock.elapsedRealtime();
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        addressMode_UI_setting(rootView);
        GPSSetting();
        list_setting(rootView);
        check_If_list_Empty();

        return rootView;
    }

    private static final String TAG_FOREGROUND_SERVICE = "FOREGROUND_SERVICE";

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

    private boolean startForeground = false;

    private void startService() {
        if (!startForeground) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                startForeground = true;
                //log.e("startService", "startService");
                mIsScanning = true;
                activity.invalidateOptionsMenu();
                Intent serviceIntent1 = new Intent(getContext(), Background_Service.class);
                serviceIntent1.setAction(ACTION_START_FOREGROUND_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.startForegroundService(serviceIntent1);
                } else {
                    activity.startService(serviceIntent1);
                }
            }
        }
    }

    private void stopService() {
        //if(startForeground) {
        startForeground = false;
        mIsScanning = false;
        activity.invalidateOptionsMenu();
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            Log.e("activityt_TAGstop", service.service.getClassName());
            if ("com.nineone.s_tag_tool.Background_Service".equals(service.service.getClassName())) {
                Log.e("activityt_TAGstop2", service.service.getClassName());
                //log.e("stopService", "stopService");
                Intent serviceIntent1 = new Intent(getContext(), Background_Service.class);
                serviceIntent1.setAction(ACTION_STOP_FOREGROUND_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.startForegroundService(serviceIntent1);
                } else {
                    activity.startService(serviceIntent1);
                }
            }
        }
        // }
    }
    private void addressMode_UI_setting(ViewGroup rootView){
        scanMinorId_spinner = (Spinner) rootView.findViewById(R.id.minorid);
        String[] tag_models = getResources().getStringArray(R.array.tag_type);
        ArrayAdapter<String> tag_spinner_adapter = new ArrayAdapter<>(activity.getBaseContext(), R.layout.custom_spinner_list, tag_models);
        tag_spinner_adapter.setDropDownViewResource(R.layout.customer_spinner);
        scanMinorId_spinner.setAdapter(tag_spinner_adapter);
        scanMinorId_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {// textView.setText(items[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {//textView.setText("???????????????");
            }
        });
        scanMinorId_spinner.setSelection(0);
        bletypeLayout = (LinearLayout) rootView.findViewById(R.id.bletypeLayout);
        if (getArguments() != null) {

            systemBoolean = getArguments().getBoolean("systemBoole");
            Log.e("systemBoole3", String.valueOf(systemBoolean));
            if (systemBoolean) {
                bletypeLayout.setVisibility(View.VISIBLE);
            } else {
                bletypeLayout.setVisibility(View.GONE);
            }

        }
        r_btn1 = (RadioButton) rootView.findViewById(R.id.sort_btn1);
        r_btn2 = (RadioButton) rootView.findViewById(R.id.sort_btn2);
        empty_scan_view = rootView.findViewById(R.id.empty_scan_view);
        empty_stop_view = rootView.findViewById(R.id.empty_stop_view);
        //????????? ?????? ??????
        radioGroup = (RadioGroup) rootView.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);
    }
    private void list_setting(ViewGroup rootView) {
        // BLE check

        bluetoothCheck();
        // init listview
        deviceListView = (RecyclerView) rootView.findViewById(R.id.main_RecyclerView);
        deviceListView.setNestedScrollingEnabled(false);
        // deviceListView.setItemViewCacheSize(8);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        deviceListView.setLayoutManager(linearLayoutManager);
        deviceListView.setItemAnimator(null);
        recyclerVierAdapter = new RecyclerViewAdapter(activity, getContext(), systemBoolean);
        recyclerVierAdapter.item_noti();
        deviceListView.setAdapter(recyclerVierAdapter);

        recyclerVierAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {//???????????? ??????????????? ?????????
                if (position != RecyclerView.NO_POSITION) {
                    Intent intent = new Intent(activity, Connect_Activity.class);
                    intent.putExtra("address", recyclerVierAdapter.ScannedDeviceList().get(position).getDevice().getAddress());

                    background_service_start = true;
                    activity.startActivity(intent);

                    // activity.finish();
                    //String mydatalist = myAdapter.getWordAtPosition(position);
                }
                // TODO : ????????? ?????? ???????????? MainActivity?????? ??????.
            }
        });
        stopScan();
    }

    private boolean background_service_start = false;

    private void bluetoothCheck() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        btLeScanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
       // mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // ???????????? ???????????? ????????? ???????????????.
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "??? ????????? ???????????? ????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
            activity.finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            //log.e("BLE1245", "124");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        //log.e("BLE1245", "130");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) { // ???????????? ???????????? ????????? ??????????????????
                //       mblecheck=false;
            } else {
                Toast.makeText(getActivity(), "??????????????? ????????? ?????? ????????? ", Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void GPSSetting() {
        //  ContentResolver res = getContentResolver();
        locationManager = (LocationManager) activity.getSystemService(activity.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("GPS ??????");
            builder.setMessage("GPS??? ?????????????????????????");
            builder.setCancelable(false);
            builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e("asd153", "asd");
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //finish();
                }
            });
            builder.show();

        }

    }

    private void check_If_list_Empty() {
        if (deviceListView.getAdapter() != null && mIsScanning) {
            boolean emptyViewVisible = deviceListView.getAdapter().getItemCount() == 0;
            empty_scan_view.setVisibility(emptyViewVisible ? View.VISIBLE : View.GONE);
            deviceListView.setVisibility(emptyViewVisible ? View.GONE : View.VISIBLE);
            empty_stop_view.setVisibility(View.GONE);
        } else if (deviceListView.getAdapter() != null && !mIsScanning) {
            boolean emptyViewVisible = deviceListView.getAdapter().getItemCount() == 0;
            empty_stop_view.setVisibility(emptyViewVisible ? View.VISIBLE : View.GONE);
            deviceListView.setVisibility(emptyViewVisible ? View.GONE : View.VISIBLE);
            empty_scan_view.setVisibility(View.GONE);
        } else if (deviceListView.getAdapter() != null && !mIsScanning) {
            boolean emptyViewVisible = deviceListView.getAdapter().getItemCount() == 0;
            empty_stop_view.setVisibility(emptyViewVisible ? View.VISIBLE : View.GONE);
            deviceListView.setVisibility(emptyViewVisible ? View.GONE : View.VISIBLE);
            empty_scan_view.setVisibility(View.GONE);
        } else if (deviceListView.getAdapter() != null && !mIsScanning) {
            boolean emptyViewVisible = deviceListView.getAdapter().getItemCount() == 0;
            empty_stop_view.setVisibility(emptyViewVisible ? View.VISIBLE : View.GONE);
            deviceListView.setVisibility(emptyViewVisible ? View.GONE : View.VISIBLE);
            empty_scan_view.setVisibility(View.GONE);
        }
    }

    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            check_If_list_Empty();
            // Log.e("BLE", "Discovery onScanResult00: ??????1 ");
            if (result.getDevice().getName() != null) {
                //  Log.e("BLE", "Discovery onScanResult01: ??????2 " + result.getDevice().getName());
                if (result.getDevice().getName().startsWith("TJ-")) {
                    Log.e("1231236", result.getDevice().getName()+","+String.valueOf(result.getScanRecord().getManufacturerSpecificData()));
                    Log.e("1231235", Arrays.toString(result.getScanRecord().getBytes()));
                    if(result.getDevice().getName().equals("TJ-00CA-00000010-0000")) {
                        int senser_O2 = ConvertToIntLittle(result.getScanRecord().getBytes(), 9 + 6);
                        int senser_CO2 = ConvertToIntLittle(result.getScanRecord().getBytes(), 9 + 12);
                        Log.e("mAlarm_on1", senser_O2+", "+senser_CO2+", " +  Arrays.toString(result.getScanRecord().getBytes()));
                    }
                    byte[] byte_ScanRocord = result.getScanRecord().getBytes();
                    int isLeft = byte_ScanRocord[9];
                    String byte_ScanRocord_str = Arrays.toString(byte_ScanRocord);
                 //   Log.e("1231235", isLeft+","+byte_ScanRocord_str);
                  //  Log.e("1231236", result.getDevice().getName()+","+String.valueOf(result.getScanRecord().getManufacturerSpecificData()));
                    recyclerVierAdapter.update(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                    getEllapse();
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {

        }
    };



    private boolean mIsScanning = false;
    RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            if (i == R.id.sort_btn1) {
                //   Toast.makeText(.this, "?????? ????????? ???????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                sort_type = "name";
            } else if (i == R.id.sort_btn2) {
                //  Toast.makeText(MainFragment.this, "?????? ????????? RSSI ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                sort_type = "rssi";
            }
        }
    };

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
        if (mIsScanning) {
            menu.findItem(R.id.action_scan).setVisible(false);
            menu.findItem(R.id.action_stop).setVisible(true);
        } else {
            menu.findItem(R.id.action_scan).setVisible(true);
            menu.findItem(R.id.action_stop).setVisible(false);
        }
        if ((mBluetoothAdapter == null) || (!mBluetoothAdapter.isEnabled()) || (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
            menu.findItem(R.id.action_scan).setEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // ignore
            return true;
        } else if (itemId == R.id.action_scan) {
            RescanBaseTime = SystemClock.elapsedRealtime();

            recyclerVierAdapter.item_Clear();
            recyclerVierAdapter.notifyDataSetChanged();
            startScan();
            recyclerVierAdapter.BGW_adress_add_reset();
            return true;
        } else if (itemId == R.id.action_stop) {
            stopScan();
            return true;
        } else if (itemId == R.id.action_change) {
            recyclerVierAdapter.item_Clear();
            recyclerVierAdapter.notifyDataSetChanged();
            recyclerVierAdapter.BGW_adress_add_reset();

            check_If_list_Empty();
            bluetoothCheck();
            return true;
        }/*else if(itemId == R.id.action_clear){
            recyclerVierAdapter.item_Clear();
            recyclerVierAdapter.notifyDataSetChanged();
        }*/
        return super.onOptionsItemSelected(item);
    }

    private void startScan() {
        if (!fos_open_flag_ble) {
            if ((mBluetoothLeScanner != null) && (!mIsScanning)) {
                mBluetoothLeScanner.startScan(leScanCallback);
                RescanBaseTime = SystemClock.elapsedRealtime();
                mIsScanning = true;
                fos_open_flag_ble = true;
                Log.e("startscan", "287");
                activity.invalidateOptionsMenu();
            }
            if (!systemBoolean) {
                BLE_type = "";
            } else {
                BLE_type = scanMinorId_spinner.getSelectedItem().toString();
                if (BLE_type.equals("??????")) {
                    BLE_type = "";
                }
            }
            //  InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            //   inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            // bletypeLayout.setVisibility(View.GONE);
            String subtitle = "SCAN Type: " + BLE_type;
            if (BLE_type.equals("")) {
                subtitle = "SCAN Type: ALL";
            }

            //getActivity().getActionBar().setSubtitle(subtitle);

            long now = System.currentTimeMillis();
            SimpleDateFormat sdfNow = new SimpleDateFormat("MM_dd_HH_mm_ss");
            file_name = sdfNow.format(new Date(now));


        }
    }


    private void stopScan() {
        if (fos_open_flag_ble) {
            if (mBluetoothLeScanner != null) {
                mBluetoothLeScanner.stopScan(leScanCallback);
                mIsScanning = false;
                fos_open_flag_ble = false;
                Log.e("startscan", "295");
            }
            // bletypeLayout.setVisibility(View.VISIBLE);
            check_If_list_Empty();
            activity.invalidateOptionsMenu();

        }
    }

    private void reScan() {
        if (mBluetoothLeScanner != null) {
            // stopScan();
            mBluetoothLeScanner.stopScan(leScanCallback);
            Runnable runnable10 = new Runnable() {
                @Override
                public void run() {
                    //  startScan();
                    mBluetoothLeScanner.startScan(leScanCallback);
                }
            };
            timechange_handler.postDelayed(runnable10, 0);


        }

    }

    private void getEllapse() {

        long now = SystemClock.elapsedRealtime();
        long ell = now - RescanBaseTime;                            //?????? ????????? ?????? ????????? ?????? ell?????? ?????????

        long min = (ell / 1000) / 60;
        if (20 < min) {
            Log.e("SystemClock2", min + "," + RescanBaseTime + "," + ell);
            Log.e("BLE Scan:", " ReStart");
            RescanBaseTime = SystemClock.elapsedRealtime();
            reScan();
        }
    }

    private final MyHandler timechange_handler = new MyHandler(this);
    private final MyHandler start_handler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<MainFragment> mActivity;

        public MyHandler(MainFragment activity) {
            mActivity = new WeakReference<MainFragment>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainFragment activity = mActivity.get();
            if (activity != null) {

            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("activityt_TAG", "onDestroy");
        //  if (mBluetoothLeScanner != null) {
        //  recyclerVierAdapter.item_Clear();
        //      stopScan();
        // }


    }

    @Override
    public void onPause() {
        Log.e("activityt_TAG", "onPause");
        super.onPause();
        //  if (mBluetoothLeScanner != null) {

        stopScan();
        recyclerVierAdapter.item_noti();
        //  recyclerVierAdapter.notifyDataSetChanged();
        //  recyclerVierAdapter.item_Clear();
        try {
            activity.unregisterReceiver(mBroadcastReceiver1);
        } catch (Exception ignored) {

        }
        //  background_service_start=false;
        //   startService();
        // finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("activityt_TAG", "onStop");
        //if (mBluetoothLeScanner != null) {
        // recyclerVierAdapter.item_Clear();
        if (!background_service_start) {
            ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);

            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                Log.e("activityt_TAG1", service.service.getClassName());
                if (!"com.nineone.s_tag_tool.Background_Service".equals(service.service.getClassName())) {
                    //log.e("onResume", "onResume2");
                    startService();
                } else {

                    startForeground = true;
                }
            }
            if (manager.getRunningServices(Integer.MAX_VALUE).size() == 0) {
                startService();
                not_rescan = true;
                startForeground = true;
            }

        }
        background_service_start = false;
        // stopScan();
        // recyclerVierAdapter.item_Clear();
        //}

    }

    @Override
    public void onStart() {

        super.onStart();
        Network_Confirm();
        stopScan();
        recyclerVierAdapter.item_noti();
        stopService();
        Log.e("activityt_TAG", "onStart()");
    }

    private boolean not_rescan = false;

    @Override
    public void onResume() {
        super.onResume();
        Log.e("activityt_TAG", "onResume");
        Log.e("Log.e", "673");
        background_service_start = false;
        if (!mble_gps_false) {
            if (mBluetoothAdapter.isEnabled()) {
                Runnable runnable10;//  startScan();

                if (!systemBoolean) {

                    runnable10 = new Runnable() {
                        @Override
                        public void run() {
                            //  startScan();
                            //  if(!not_rescan) {
                            startScan();
                            //   }
                            //background_service_start=true;
                        }
                    };
                    start_handler.postDelayed(runnable10, 0);
                }
            }

        }
        check_If_list_Empty();
        mble_gps_false = false;
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);//ble ?????? ?????? ??????
        activity.registerReceiver(mBroadcastReceiver1, filter1);
        IntentFilter filter2 = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);//gps ???????????? ??????
        activity.registerReceiver(mBroadcastReceiver1, filter2);
        IntentFilter filter3 = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);//????????? ?????? ?????? ??????
        activity.registerReceiver(mBroadcastReceiver1, filter3);

        // checkSDK();
    }

    private boolean mble_gps_false = false;
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                //log.e("off1", action);
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        stopScan();
                        activity.invalidateOptionsMenu();
                        Toast.makeText(activity.getApplication(), "??????????????? ?????????????????????.\n ??????????????? ???????????? ????????? ", Toast.LENGTH_SHORT).show();
                        //log.e("off1", "off1");
                        mble_gps_false = true;
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //log.e("off2", "off2");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        activity.invalidateOptionsMenu();

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
                    activity.invalidateOptionsMenu();
                    //log.e("off7", String.valueOf(isGpsEnabled));
                } else {
                    stopScan();
                    activity.invalidateOptionsMenu();
                    //  mblecheck = false;
                    Toast.makeText(activity.getApplication(), "GPS??? ?????????????????????.\n GPS??? ???????????? ????????? ", Toast.LENGTH_SHORT).show();
                    // blesendcheck.setText("?????? (GPS??? ??????)");
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
                    ReadTextFile();
                } else {
                    Log.e("testCONNECTED", "distestCONNECTED");
                    //   Toast.makeText(activity.getApplication(), "Internet connection is Off", Toast.LENGTH_LONG).show();
                }
            }
        }
    };
    ArrayList<Tag_tiem> arrayList = new ArrayList<>();

    public void ReadTextFile() {//csv ?????? ????????? ????????????
        try {
            String str_Path_Full;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                str_Path_Full = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "Nineone" + File.separator + "save.csv";
                // file2 = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + File.separator+ "Nineone"+ File.separator,"save.csv");
            } else {
                str_Path_Full = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Documents" + File.separator + "Nineone" + File.separator + "save.csv";

                // file2 = new File(Environment.DIRECTORY_DOCUMENTS + File.separator+ "Nineone"+ File.separator + "save.csv");
            }
            FileInputStream is = new FileInputStream(str_Path_Full);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "EUC-KR"));
            // BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = "";
            int row = 0;
            Log.e("aaa", "line");
            while ((line = reader.readLine()) != null) {//?????? ????????? ????????? ??????
                String[] token = line.split("\\,", -1);

                Log.e("aaa2", String.valueOf(row));
                String sensor_margin = null;
                if (token.length > 5) {
                    sensor_margin = token[3] + "," + token[4] + "," + token[5] + "," + token[6] + "," + token[7];
                } else {
                    sensor_margin = token[3] + "," + token[4];
                }
                arrayList.add(row, new Tag_tiem(token[0], token[1], token[2], sensor_margin));
                Log.e("aaa3", String.valueOf(row));
                row++;
            }

            reader.close();
            is.close();
            Http_Array_post(arrayList);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("aaa5", String.valueOf(arrayList));

        }
        //return strBuffer.toString();
    }

    public void Http_Array_post(ArrayList<Tag_tiem> array_List) {

        new Thread(() -> {
            try {

                String url = "http://stag.nineone.com:8002/si/rece_Setting.asp";
                URL object = null;
                object = new URL(url);
                HttpURLConnection con = null;

                con = (HttpURLConnection) object.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Authorization", "Bearer Key");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("POST");
                JSONArray array = new JSONArray();

                for (int i = 0; i < array_List.size(); i++) {
                    JSONObject cred = new JSONObject();
                    try {
                        cred.put("id", array_List.get(i).getId());
                        cred.put("device_idx", array_List.get(i).getDevice_idx());
                        cred.put("time", array_List.get(i).getTime());
                        cred.put("sensor_margin", array_List.get(i).getSensor_margin());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    array.put(cred);
                }
                OutputStream os = con.getOutputStream();
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
                    Log.e("dd-", "\n" + sb.toString());
                    if (sb.toString().contains("done")) {
                        Log.e("1dd-", "\n" + sb.toString());
                        fileDelete();
                    } else {
                        Log.e("2dd-", "\n" + sb.toString());
                    }

                } else {
                    Log.e("dd-", "\n" + con.getResponseMessage());
                    //   System.out.println(con.getResponseMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    private static boolean fileDelete() {
        // String str_Path_Full = Environment.getExternalStorageDirectory().getAbsolutePath();
        // str_Path_Full += "/Nineone" + File.separator + "save.csv";

        try {
            File file;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //     str_Path_Full = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + File.separator+ "Nineone"+ File.separator + "save.csv";
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "Nineone" + File.separator, "save.csv");
            } else {
                //     str_Path_Full = Environment.DIRECTORY_DOCUMENTS + File.separator+ "Nineone"+ File.separator + "save.csv";
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Documents" + File.separator + "Nineone" + File.separator + "save.csv");
            }
            if (file.exists()) {
                file.delete();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private void Network_Confirm() {
        int status = NetworkStatus.getConnectivityStatus(activity.getApplicationContext());
        if (status == NetworkStatus.TYPE_MOBILE) {
            Log.e("???????????? ?????????", "650");
        } else if (status == NetworkStatus.TYPE_WIFI) {
            Log.e("??????????????? ?????????", "652");
        } else {
            Log.e("?????? ??????.", "654");
        }
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
