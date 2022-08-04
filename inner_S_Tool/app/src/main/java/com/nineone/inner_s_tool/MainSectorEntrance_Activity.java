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
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.lang.ref.WeakReference;

public class MainSectorEntrance_Activity extends AppCompatActivity{

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private static final int REQUEST_ENABLE_BT = 2;//ble 켜져있는지 확인
    private TextView location_textView,data_textView,user_textView,data_boolen_textView;
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

        user_textView = findViewById(R.id.User_TextView);
        data_textView = findViewById(R.id.Data_TextView);
        data_boolen_textView = findViewById(R.id.Data_boolen_TextView);
        bluetoothCheck();
        startService();
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
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // Get extra data included in the Intent
            String message_buildlevel_name = intent.getStringExtra("buildlevel_name");
            String message_zone_data = intent.getStringExtra("zone_data");
            String message_zone_boolen_data = intent.getStringExtra("zone_boolen_data");
            String message_env_user = intent.getStringExtra("env_user");
            //log.e("receiver", "Got message: " + message);
            Log.e("delay_check","textData");
            String[] zone_data_array = message_zone_data.trim().split("-");
            String[] zone_data_boolen_array = message_zone_boolen_data.trim().split("-");
            location_textView.setText(message_buildlevel_name);
            data_textView.setText("농도\n\n"+zone_data_array[0]+" %\n\n"+zone_data_array[1]+" ppm\n\n"+zone_data_array[2]+" ppm\n\n"+zone_data_array[3]+" ppm\n\n"+zone_data_array[4]+" ppm");
            data_boolen_textView.setText("경고\n\n"+zone_data_boolen_array[0]+"\n\n"+zone_data_boolen_array[1]+"\n\n"+zone_data_boolen_array[2]+"\n\n"+zone_data_boolen_array[3]+"\n\n"+zone_data_boolen_array[4]);

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
                Toast.makeText(getApplicationContext(), "블루투스를 활성화 하여 주세요 ", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    private boolean mble_gps_false = false;

}
