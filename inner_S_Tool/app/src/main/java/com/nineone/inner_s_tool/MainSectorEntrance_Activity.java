package com.nineone.inner_s_tool;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.lang.ref.WeakReference;

public class MainSectorEntrance_Activity extends AppCompatActivity{

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private static final int REQUEST_ENABLE_BT = 2;//ble 켜져있는지 확인
    private TextView location_textView,data_textView,user_textView,data_boolen_textView;
    private Button exit_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sector_entrance);

      //  Intent intent = getIntent();//차트 리스트 페이지에서 정보 받아오기
     //   String get_zone_name_num = intent.getExtras().getString("zone_name_num");//
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String Shared_zone_name_num = sp.getString("Shared_zone_name_num", "");
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("No " + Shared_zone_name_num + " Confferdam");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        location_textView = findViewById(R.id.Location_TextView);

        user_textView = findViewById(R.id.User_TextView);
        mdata_height_TextView= findViewById(R.id.Height_TextView);
        mdata_entrance_TextView = findViewById(R.id.Entrance_TextView);
        text_arrary();
    //    data_textView = findViewById(R.id.Data_TextView);
     //   data_boolen_textView = findViewById(R.id.Data_boolen_TextView);
        bluetoothCheck();
        startService();
        exit_button = findViewById(R.id.exit_Button);
        exit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
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
    @Override
    protected void onStart() {
        super.onStart();
        Log.e("connect_TAG", "onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("connect_TAG", "onResume");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("custom-event-name"));
        mble_gps_false = false;

    }

    @Override
    protected void onPause() {
        Log.e("connect_TAG", "onPause");
        super.onPause();

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
    private TextView mdata_O2_textView,mdata_CO_textView,mdata_H2S_textView,mdata_CO2_textView,mdata_CH4_textView;
    private TextView mdata_O2_boolean_textView,mdata_CO_boolean_textView,mdata_H2S_boolean_textView,mdata_CO2_boolean_textView,mdata_CH4_boolean_textView;
    private TextView mdata_height_TextView,mdata_entrance_TextView;
    private void text_arrary(){
        mdata_O2_textView = findViewById(R.id.Data_O2_TextView);
        mdata_O2_boolean_textView = findViewById(R.id.Data_O2_Boolean_TextView);
        mdata_CO_textView = findViewById(R.id.Data_CO_TextView);
        mdata_CO_boolean_textView = findViewById(R.id.Data_CO_Boolean_TextView);
        mdata_H2S_textView = findViewById(R.id.Data_H2S_TextView);
        mdata_H2S_boolean_textView = findViewById(R.id.Data_H2S_Boolean_TextView);
        mdata_CO2_textView = findViewById(R.id.Data_CO2_TextView);
        mdata_CO2_boolean_textView = findViewById(R.id.Data_CO2_Boolean_TextView);
        mdata_CH4_textView = findViewById(R.id.Data_CH4_TextView);
        mdata_CH4_boolean_textView = findViewById(R.id.Data_CH4_Boolean_TextView);
    }
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // Get extra data included in the Intent
            String message_buildlevel_name = intent.getStringExtra("buildlevel_name");
            String message_zone_data = intent.getStringExtra("zone_data");
            String message_zone_boolen_data = intent.getStringExtra("zone_boolen_data");
            String message_env_user = intent.getStringExtra("env_user");
            String message_zone_height = intent.getStringExtra("zone_height");
            String message_zone_entrance = intent.getStringExtra("zone_entrance");
            mdata_height_TextView.setText(message_zone_height);
            mdata_entrance_TextView.setText(message_zone_entrance);
            //log.e("receiver", "Got message: " + message);
            Log.e("delay_check", "textData");
            String[] zone_data_array = message_zone_data.trim().split("-");
            String[] zone_data_boolen_array = message_zone_boolen_data.trim().split("-");
            location_textView.setText(message_buildlevel_name);
            mdata_O2_textView.setText(zone_data_array[0]);
            mdata_CO_textView.setText(zone_data_array[1]);
            mdata_H2S_textView.setText(zone_data_array[2]);
            mdata_CO2_textView.setText(zone_data_array[3]);
            mdata_CH4_textView.setText(zone_data_array[4]);
            String env_O2_alarm_String = "OFF";
            String env_CO_alarm_String = "OFF";
            String env_H2S_alarm_String = "OFF";
            String env_CO2_alarm_String = "OFF";
            String env_CH4_alarm_String = "OFF";
            if (zone_data_boolen_array[0].equals("true")) {
                env_O2_alarm_String = "ON";
                mdata_O2_boolean_textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
            }else{
                mdata_O2_boolean_textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            }
            if (zone_data_boolen_array[1].equals("true")) {
                env_CO_alarm_String = "ON";
                mdata_CO_boolean_textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
            }else{
                mdata_CO_boolean_textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            }
            if (zone_data_boolen_array[2].equals("true")) {
                env_H2S_alarm_String = "ON";
                mdata_H2S_boolean_textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
            }else{
                mdata_H2S_boolean_textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            }
            if (zone_data_boolen_array[3].equals("true")) {
                env_CO2_alarm_String = "ON";
                mdata_CO2_boolean_textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
            }else{
                mdata_CO2_boolean_textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            }
            if (zone_data_boolen_array[4].equals("true")) {
                env_CH4_alarm_String = "ON";
                mdata_CH4_boolean_textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
            }else{
                mdata_CH4_boolean_textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            }
            mdata_O2_boolean_textView.setText(env_O2_alarm_String);
            mdata_CO_boolean_textView.setText(env_CO_alarm_String);
            mdata_H2S_boolean_textView.setText(env_H2S_alarm_String);
            mdata_CO2_boolean_textView.setText(env_CO2_alarm_String);
            mdata_CH4_boolean_textView.setText(env_CH4_alarm_String);

            //data_textView.setText("농도\n\n"+zone_data_array[0]+" %\n\n"+zone_data_array[1]+" ppm\n\n"+zone_data_array[2]+" ppm\n\n"+zone_data_array[3]+" ppm\n\n"+zone_data_array[4]+" ppm");
            // data_boolen_textView.setText("경고\n\n"+zone_data_boolen_array[0]+"\n\n"+zone_data_boolen_array[1]+"\n\n"+zone_data_boolen_array[2]+"\n\n"+zone_data_boolen_array[3]+"\n\n"+zone_data_boolen_array[4]);

            user_textView.setText(message_env_user);
            //Log.d("receiver", "Got message: " + message);
        }
    };
    private final MyHandler textchange_handler = new MyHandler(this);
    private final MyHandler start_handler = new MyHandler(this);
    private static class MyHandler extends Handler {
        private final WeakReference<MainSectorEntrance_Activity> mActivity;

        public MyHandler(MainSectorEntrance_Activity activity) {
            mActivity = new WeakReference<MainSectorEntrance_Activity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            MainSectorEntrance_Activity activity = mActivity.get();
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("퇴장 확인");
        builder.setMessage("정말로 퇴장하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //  moveTaskToBack(true); // 태스크를 백그라운드로 이동
                stopService();
                Intent intent = new Intent(MainSectorEntrance_Activity.this,MainSectorActivity.class);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                //저장을 하기위해 Editor를 불러온다.
                SharedPreferences.Editor edit = preferences.edit();
                edit.putString("Shared_zone_name_num", "0");
                edit.apply();

                startActivity(intent);
                finish(); // 액티비티 종료 + 태스크 리스트에서 지우기
                //android.os.Process.killProcess(android.os.Process.myPid()); // 앱 프로세스 종료
            }
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }
    private void bluetoothCheck() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "이 기기는 블루투스 기능을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) { // 블루투스 활성화를 취소를 클릭하였다면
            } else {
                bluetoothCheck();
                Toast.makeText(getApplicationContext(), "블루투스를 활성화 하여 주세요 ", Toast.LENGTH_SHORT).show();
             //   finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    private boolean mble_gps_false = false;

}
