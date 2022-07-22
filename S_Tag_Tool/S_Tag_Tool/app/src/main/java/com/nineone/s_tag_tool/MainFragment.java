package com.nineone.s_tag_tool;

import android.Manifest;
import android.app.Activity;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainFragment extends Fragment {

    MainActivity activity;
    final public static String STAG_0007 = "0007";
    final public static String STAG_00C8 = "00C8";
    final public static String STAG_00C9 = "00C9";
    final public static String STAG_00CA = "00CA";
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

    Spinner scanMinorId_spinner;
    LinearLayout bletypeLayout;
    public static String BLE_type;
    static String sort_type = "name";
    private RadioGroup radioGroup;
    private RadioButton r_btn1, r_btn2;
    public static String file_name;

    public static boolean fos_open_flag_ble=false;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanSettings btLeScanSettings;
    private RecyclerViewAdapter recyclerVierAdapter;
    private static final int REQUEST_ENABLE_BT = 2;
    RecyclerView deviceListView;
    private boolean systemBoole=false;
    private LinearLayout empty_stop_view;
    private LinearLayout empty_scan_view;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//이 메소드가 호출될떄는 프래그먼트가 엑티비티위에 올라와있는거니깐 getActivity메소드로 엑티비티참조가능
        activity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
//이제 더이상 엑티비티 참초가안됨
        activity = null;
    }
    private String[] permissions;
    private final String[] permissions1 = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION

    };

    private final String[] permissions2 = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.READ_PHONE_NUMBERS,

    };
    private LocationManager locationManager;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e("activityt_TAG", "onCreat");
//프래그먼트 메인을 인플레이트해주고 컨테이너에 붙여달라는 뜻임
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main , container, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions=permissions2;// 안드로이드 6.0 이상일 경우 퍼미션 체크
            checkPermissions(permissions2);
        }else{
            permissions=permissions1;
            checkPermissions(permissions);
        }
        RescanBaseTime = SystemClock.elapsedRealtime();
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
          // bluetoothCheck();
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
            public void onNothingSelected(AdapterView<?> parent) {//textView.setText("선택하세요");
            }
        });
        scanMinorId_spinner.setSelection(0);
        bletypeLayout = (LinearLayout) rootView.findViewById(R.id.bletypeLayout);
        if ( getArguments() != null ) {

            systemBoole = getArguments().getBoolean("systemBoole");
            Log.e("systemBoole3", String.valueOf(systemBoole));
            if(systemBoole){
                bletypeLayout.setVisibility(View.VISIBLE);
            }else{
                bletypeLayout.setVisibility(View.GONE);
            }

        }
        r_btn1 = (RadioButton) rootView.findViewById(R.id.sort_btn1);
        r_btn2 = (RadioButton) rootView.findViewById(R.id.sort_btn2);
        empty_scan_view = rootView.findViewById(R.id.empty_scan_view);
        empty_stop_view = rootView.findViewById(R.id.empty_stop_view);
        //라디오 그룹 설정
        radioGroup = (RadioGroup) rootView.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);
        GPSSetting();
        init(rootView);
        setHasOptionsMenu(true);
        checkIfEmpty();
      //  startScan();
        /* Button button = rootView.findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onFragmentChange(1);
            }
        });*/
        return rootView;
    }

    private void init(ViewGroup rootView) {
        // BLE check

        bluetoothCheck();
        // init listview
        deviceListView = (RecyclerView) rootView.findViewById(R.id.main_RecyclerView);
        deviceListView.setNestedScrollingEnabled(false);
       // deviceListView.setItemViewCacheSize(8);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        deviceListView.setLayoutManager(linearLayoutManager);
        deviceListView.setItemAnimator(null);
        recyclerVierAdapter = new RecyclerViewAdapter(deviceListView,activity,getContext(),systemBoole);
        recyclerVierAdapter.item_noti();
        deviceListView.setAdapter(recyclerVierAdapter);

        recyclerVierAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {//리스트를 클릭했을때 이벤트
                if(position != RecyclerView.NO_POSITION) {
                    Intent intent = new Intent(activity, Connect_Activity.class);
                    intent.putExtra("address", recyclerVierAdapter.ScannedDeviceList().get(position).getDevice().getAddress());
                    activity.startActivity(intent);

                   // activity.finish();
                    //String mydatalist = myAdapter.getWordAtPosition(position);
                }
                // TODO : 아이템 클릭 이벤트를 MainActivity에서 처리.
            }
        }) ;
        stopScan();
    }
    private void bluetoothCheck(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        btLeScanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build ();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 지원하지 않는다면 어플을 종료시킨다.
        if(mBluetoothAdapter == null){
            Toast.makeText(getActivity(), "이 기기는 블루투스 기능을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            activity.finish();
            return;
        }
        if(!mBluetoothAdapter.isEnabled()){
            //log.e("BLE1245", "124");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        //log.e("BLE1245", "130");
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) { // 블루투스 활성화를 취소를 클릭하였다면
                //       mblecheck=false;

            }else{
                Toast.makeText(getActivity(), "블루투스를 활성화 하여 주세요 ", Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    private void GPSSetting() {
        //  ContentResolver res = getContentResolver();
        locationManager = (LocationManager)activity.getSystemService(activity.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            Log.e("asd147","asd");
            builder.setTitle("GPS 설정");
            builder.setMessage("GPS를 사용하시겠습니까?");
            builder.setPositiveButton("사용", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e("asd153","asd");
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("거절", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //finish();
                }
            });
            builder.show();

        }

    }
    private void checkIfEmpty() {
        if ( deviceListView.getAdapter() != null && mIsScanning) {
            boolean emptyViewVisible = deviceListView.getAdapter().getItemCount() == 0;
            empty_scan_view.setVisibility(emptyViewVisible ? View.VISIBLE : View.GONE);
            deviceListView.setVisibility(emptyViewVisible ? View.GONE : View.VISIBLE);
            empty_stop_view.setVisibility(View.GONE);
        }else if (deviceListView.getAdapter() != null && !mIsScanning){
            boolean emptyViewVisible = deviceListView.getAdapter().getItemCount() == 0;
            empty_stop_view.setVisibility(emptyViewVisible ? View.VISIBLE : View.GONE);
            deviceListView.setVisibility(emptyViewVisible ? View.GONE : View.VISIBLE);
            empty_scan_view.setVisibility(View.GONE);
        }else if (deviceListView.getAdapter() != null && !mIsScanning){
            boolean emptyViewVisible = deviceListView.getAdapter().getItemCount() == 0;
            empty_stop_view.setVisibility(emptyViewVisible ? View.VISIBLE : View.GONE);
            deviceListView.setVisibility(emptyViewVisible ? View.GONE : View.VISIBLE);
            empty_scan_view.setVisibility(View.GONE);
        }else if (deviceListView.getAdapter() != null && !mIsScanning){
            boolean emptyViewVisible = deviceListView.getAdapter().getItemCount() == 0;
            empty_stop_view.setVisibility(emptyViewVisible ? View.VISIBLE : View.GONE);
            deviceListView.setVisibility(emptyViewVisible ? View.GONE : View.VISIBLE);
            empty_scan_view.setVisibility(View.GONE);
        }
    }

    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result){
            checkIfEmpty();
            // Log.e("BLE", "Discovery onScanResult00: 작동1 ");
            if (result.getDevice().getName() != null) {
               //  Log.e("BLE", "Discovery onScanResult01: 작동2 " + result.getDevice().getName());
                if (result.getDevice().getName().startsWith("TJ-")) {
                    //Log.e("123123",result.getDevice().getName());
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
            if(i == R.id.sort_btn1){
             //   Toast.makeText(.this, "분류 타입이 이름으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                sort_type = "name";
            }
            else if(i == R.id.sort_btn2){
              //  Toast.makeText(MainFragment.this, "분류 타입이 RSSI 값으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
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
        if ((mBluetoothAdapter == null) || (!mBluetoothAdapter.isEnabled())||(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
            menu.findItem(R.id.action_scan).setEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // ignore
            return true;
        }else if (itemId == R.id.action_scan) {
            RescanBaseTime = SystemClock.elapsedRealtime();

            recyclerVierAdapter.item_Clear();
            recyclerVierAdapter.notifyDataSetChanged();
            startScan();

            return true;
        } else if (itemId == R.id.action_stop) {
            stopScan();

            return true;
        }else if (itemId == R.id.action_change) {
            recyclerVierAdapter.item_Clear();
            recyclerVierAdapter.notifyDataSetChanged();
            checkIfEmpty();
            return true;
        }/*else if(itemId == R.id.action_clear){
            recyclerVierAdapter.item_Clear();
            recyclerVierAdapter.notifyDataSetChanged();
        }*/
        return super.onOptionsItemSelected(item);
    }

    private void startScan() {
        if(!fos_open_flag_ble) {
            if ((mBluetoothLeScanner != null) && (!mIsScanning)) {
                mBluetoothLeScanner.startScan(leScanCallback);
                RescanBaseTime = SystemClock.elapsedRealtime();
                mIsScanning = true;
                fos_open_flag_ble = true;
                Log.e("startscan", "287");
                activity.invalidateOptionsMenu();
            }
            if(!systemBoole) {
                BLE_type = "";
            }else{
                BLE_type = scanMinorId_spinner.getSelectedItem().toString();
                if (BLE_type.equals("선택")) {
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
        if(fos_open_flag_ble) {
            if (mBluetoothLeScanner != null) {
                mBluetoothLeScanner.stopScan(leScanCallback);
                mIsScanning = false;
                fos_open_flag_ble = false;
                Log.e("startscan", "295");
            }

            // bletypeLayout.setVisibility(View.VISIBLE);

            checkIfEmpty();
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
        long ell = now - RescanBaseTime;                            //현재 시간과 지난 시간을 빼서 ell값을 구하고

        long min = (ell / 1000) / 60;
        if(20<min){
            Log.e("SystemClock2", min+","+RescanBaseTime+","+ell);
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
        Log.e("activityt_TAG","onDestroy");
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
        recyclerVierAdapter.item_Clear();
        try {
            activity.unregisterReceiver(mBroadcastReceiver1);
        } catch (Exception ignored){

        }
        // finish();
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.e("activityt_TAG","onStop");
        //if (mBluetoothLeScanner != null) {
        // recyclerVierAdapter.item_Clear();

       // stopScan();
       // recyclerVierAdapter.item_Clear();
        //}

    }
    @Override
    public void onStart() {

        super.onStart();
     //   recyclerVierAdapter.item_noti();
     //   startScan();
        Log.e("activityt_TAG", "onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("activityt_TAG", "onResume");
        Network_Confirm();
        stopScan();
        recyclerVierAdapter.item_noti();
        recyclerVierAdapter.notifyDataSetChanged();
        if(!mble_gps_false) {

            Runnable runnable10;//  startScan();
            if(!systemBoole) {
                runnable10 = new Runnable() {
                    @Override
                    public void run() {
                        //  startScan();
                        startScan();
                    }
                };
                start_handler.postDelayed(runnable10, 1000);
            }

        }
        checkIfEmpty();
        mble_gps_false=false;
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);//ble 상태 감지 필터
        activity.registerReceiver(mBroadcastReceiver1, filter1);
        IntentFilter filter2 = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);//gps 상태감지 필터
        activity.registerReceiver(mBroadcastReceiver1, filter2);
        IntentFilter filter3 = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);//인터넷 상태 감지 필터
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
                        Toast.makeText(activity.getApplication(), "블루투스가 종료되었습니다.\n 블루투스를 실행시켜 주세요 ", Toast.LENGTH_SHORT).show();
                        //log.e("off1", "off1");
                        mble_gps_false=true;
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
                    Toast.makeText(activity.getApplication(), "GPS가 종료되었습니다.\n GPS를 실행시켜 주세요 ", Toast.LENGTH_SHORT).show();
                    // blesendcheck.setText("중지 (GPS가 종료)");
                    //log.e("off8", String.valueOf(isGpsEnabled));
                }
            }
            if(action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
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
    public void ReadTextFile() {//csv 파일 내용을 추출하기
        try {
            String str_Path_Full;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                str_Path_Full = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+ File.separator+ "Nineone"+ File.separator + "save.csv";
               // file2 = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + File.separator+ "Nineone"+ File.separator,"save.csv");
            } else {
                str_Path_Full = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ "Documents"+ File.separator+ "Nineone"+ File.separator + "save.csv";

                // file2 = new File(Environment.DIRECTORY_DOCUMENTS + File.separator+ "Nineone"+ File.separator + "save.csv");
            }
            FileInputStream is = new FileInputStream(str_Path_Full);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "EUC-KR"));
            // BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = "";
            int row=0;
            Log.e("aaa", "line");
            while ((line = reader.readLine()) != null) {//해당 파일을 한줄씩 읽기
                String[] token = line.split("\\,", -1);

                Log.e("aaa2", String.valueOf(row));
                String sensor_margin = null;
                if(token.length>5) {
                    sensor_margin = token[3] + "," + token[4] + "," + token[5] + "," + token[6] + "," + token[7];
                }else{
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
    public void Http_Array_post(ArrayList<Tag_tiem> array_List){

        new Thread(() -> {
            try {

                String url="http://stag.nineone.com:8002/si/rece_Setting.asp";
                URL object= null;
                object = new URL(url);
                HttpURLConnection con = null;

                con = (HttpURLConnection) object.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Authorization", "Bearer Key");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("POST");
                JSONArray array=new JSONArray();

                for(int i=0;i<array_List.size();i++){
                    JSONObject cred = new JSONObject();
                    try {
                        cred.put("id", array_List.get(i).getId());
                        cred.put("device_idx",array_List.get(i).getDevice_idx());
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
                    Log.e("dd-","\n"+sb.toString());
                    if(sb.toString().contains("done")) {
                        Log.e("1dd-", "\n" + sb.toString());
                        fileDelete();
                    }else{
                        Log.e("2dd-","\n"+sb.toString());
                    }

                } else {
                    Log.e("dd-","\n"+con.getResponseMessage());
                    //   System.out.println(con.getResponseMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }
    private static boolean fileDelete(){
       // String str_Path_Full = Environment.getExternalStorageDirectory().getAbsolutePath();
       // str_Path_Full += "/Nineone" + File.separator + "save.csv";

        try {
            File file;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
           //     str_Path_Full = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + File.separator+ "Nineone"+ File.separator + "save.csv";
                file = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator+ "Nineone"+ File.separator,"save.csv");
            } else {
           //     str_Path_Full = Environment.DIRECTORY_DOCUMENTS + File.separator+ "Nineone"+ File.separator + "save.csv";
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ "Documents" +File.separator+"Nineone"+ File.separator + "save.csv");
            }
            if(file.exists()){
                file.delete();
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    private static final int MULTIPLE_PERMISSIONS = 101;
    private boolean checkPermissions(String[] permissi) {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissi) {
            result = ContextCompat.checkSelfPermission(activity, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        } else {

        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[i])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showToast_PermissionDeny();
                            }
                        }
                    }
                } else {
                    showToast_PermissionDeny();
                }
                break;
            }
        }
    }
    private void showToast_PermissionDeny() {
        Toast.makeText(getActivity(), "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        activity.finish();
    }
    private void Network_Confirm(){
        int status = NetworkStatus.getConnectivityStatus(activity.getApplicationContext());
        if(status == NetworkStatus.TYPE_MOBILE){
            Log.e("모바일로 연결됨","650");
        }else if (status == NetworkStatus.TYPE_WIFI){
            Log.e("무선랜으로 연결됨","652");
        }else {
            Log.e("연결 안됨.","654");
        }
    }
}
