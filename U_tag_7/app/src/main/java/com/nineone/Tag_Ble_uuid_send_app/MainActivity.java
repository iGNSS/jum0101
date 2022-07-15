package com.nineone.Tag_Ble_uuid_send_app;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
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
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.nineone.Tag_Ble_uuid_send_app.databinding.ActivityMainBinding;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private BluetoothAdapter mBluetoothAdapter;
    private LocationManager locationManager;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_ENABLE_BT2 = 3;

    private TextView textPhonNumber, textData, textTime;
    private  TextView tv;
    private static SensorManager mSensorManager;
    private Sensor mAccelerometer; // 가속도 센스
    private Sensor mGyroerometer; // 자이로 센스
    private Sensor mMagnetometer; // 자력계 센스
    private Sensor mBarometer;
    // Used to load the 'c_c' library on application startup.
    static {
        System.loadLibrary("Tag_Ble_uuid_send_app");
    }
    private native void reset();
    private ActivityMainBinding binding;
    private int time_sec=0;
    private String phonenumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TelephonyManager phonData = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
        }
        phonenumber = phonData.getLine1Number();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);//ble 상태 감지 필터
        registerReceiver(mBroadcastReceiver1, filter1);
        IntentFilter filter2 = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);//gps 상태감지 필터
        registerReceiver(mBroadcastReceiver1, filter2);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Example of a call to a native method
        tv = binding.sampleText;

        textPhonNumber = findViewById(R.id.textView_PhonNumber);
        if(phonenumber!=null) {
            textPhonNumber.setText(phonenumber);
        }else{
            String Null_PhonNumber = "010-1234-1234";
            textPhonNumber.setText(Null_PhonNumber);

        }
        textData = findViewById(R.id.textViewData);
        textTime = findViewById(R.id.textViewTime);

        //log.e("onre","onre");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);//가속도
        mGyroerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);//자력계
        mBarometer = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);//기압계
        //log.e("onre1","onre1");
       // boolean chk1 = mSensorManager.registerListener(listener, mAccelerometer,SensorManager.SENSOR_DELAY_UI);
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null){
            textPhonNumber.setText("가속도 센서를 지원하지 않습니다.");
        }
       // boolean chk2 = mSensorManager.registerListener(listener, mGyroerometer,SensorManager.SENSOR_DELAY_UI);
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) == null){
            textData.setText("자이로 센서 지원하지 않음");
        }
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null){
            textTime.setText("기압계 센서 지원하지 않음");
        }
        startTimerTask();
        bluetoothCheck();
    }

    /**
     * A native method that is implemented by the 'c_c' native library,
     * which is packaged with this application.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    private void bluetoothCheck(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 지원하지 않는다면 어플을 종료시킨다.
        if(mBluetoothAdapter == null){
            Toast.makeText(this, "이 기기는 블루투스 기능을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if(!mBluetoothAdapter.isEnabled()){
            //log.e("BLE1245", "124");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }else{
            ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo  service  : manager.getRunningServices(Integer.MAX_VALUE)) {
                //log.e("onResume", service.service.getClassName());
                if (!"com.nineone.c_c.background_Service".equals(service.service.getClassName())) {
                    //log.e("onResume", "onResume2");
                    startService();
                }else{
                    startForeground=true;
                }
            }
            startService();
        }
        //log.e("BLE1245", "130");
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                //log.e("BLE1355", "355");
                if (resultCode == RESULT_OK) { // 블루투스 활성화를 확인을 클릭하였다면
                    Toast.makeText(getApplicationContext(), "블루투스 활성화", Toast.LENGTH_LONG).show();
                    ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
                    for (ActivityManager.RunningServiceInfo  service  : manager.getRunningServices(Integer.MAX_VALUE)) {
                        //log.e("onResume", service.service.getClassName());
                        if (!"com.nineone.c_c.background_Service".equals(service.service.getClassName())) {
                            //log.e("onResume", "onResume2");
                            startService();
                        }else{
                            startForeground=true;
                        }
                    }
                    startService();
                    //         mblecheck = true;
                } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화를 취소를 클릭하였다면
                    //       mblecheck=false;
                    Toast.makeText(this, "블루투스를 활성화 하여 주세요 ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case REQUEST_ENABLE_BT2:
                //log.e("BLE1366", "366");
                if (resultCode == RESULT_OK) { // 블루투스 활성화를 확인을 클릭하였다면
                    Toast.makeText(getApplicationContext(), "블루투스 활성화", Toast.LENGTH_LONG).show();
                } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화를 취소를 클릭하였다면
                    Toast.makeText(this, "블루투스를 활성화 하여 주세요 ", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private boolean startForeground = false;
    @Override
    protected void onResume() {
        super.onResume();
        //log.e("onResume", "onResume");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("custom-event-name"));
    }
    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(this);
        super.onPause();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    private void startService(){
        if(!startForeground){
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                startForeground=true;
                //log.e("startService", "startService");
                mIsScanning=true;
                invalidateOptionsMenu();
                Intent serviceIntent1 = new Intent(MainActivity.this, background_Service.class);
                serviceIntent1.setAction("startForeground");
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent1);
                   } else {
                    startService(serviceIntent1);
                   }
            }else{
                Toast.makeText(getApplication(), "GPS를 활성화 하여 주세요 ", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    private void stopService(){
        if(startForeground) {
            startForeground = false;
            mIsScanning = false;
            invalidateOptionsMenu();
            //log.e("stopService", "stopService");
            Intent serviceIntent1 = new Intent(MainActivity.this, background_Service.class);
            serviceIntent1.setAction("startForeground");
            stopService(serviceIntent1);

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
                        stopService();
                        Toast.makeText(getApplication(), "블루투스가 종료되었습니다.\n 블루투스를 실행시켜 주세요 ", Toast.LENGTH_SHORT).show();
                        //log.e("off1", "off1");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //log.e("off2", "off2");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        //log.e("off3", "off3");
                        startService();
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
                    startService();
                    //log.e("off7", String.valueOf(isGpsEnabled));
                } else {
                    stopService();
                  //  mblecheck = false;
                    Toast.makeText(getApplication(), "GPS가 종료되었습니다.\n GPS를 실행시켜 주세요 ", Toast.LENGTH_SHORT).show();
                   // blesendcheck.setText("중지 (GPS가 종료)");
                    //log.e("off8", String.valueOf(isGpsEnabled));
                }
            }
        }
    };
    private boolean mIsScanning = false;
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mIsScanning) {
            menu.findItem(R.id.action_scan).setVisible(false);
            menu.findItem(R.id.action_stop).setVisible(false);
            menu.findItem(R.id.action_clear).setVisible(false);
            menu.findItem(R.id.action_back).setVisible(false);
            menu.findItem(R.id.action_reset).setVisible(true);
        } else {
            menu.findItem(R.id.action_scan).setVisible(false);
            menu.findItem(R.id.action_stop).setVisible(false);
            menu.findItem(R.id.action_clear).setVisible(false);
            menu.findItem(R.id.action_back).setVisible(false);
            menu.findItem(R.id.action_reset).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }
    @SuppressWarnings("unchecked")
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // ignore
            return true;
        }else if (itemId == R.id.action_scan) {

            startService();
            return true;
        } else if (itemId == R.id.action_stop) {
            stopService();
            return true;
        }else if (itemId == R.id.action_reset) {
            reset();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
    private final MyHandler mHandler = new MyHandler(this);

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //log.e("off1", "off1");
        Runnable runnable10;//.addServiceUuid(pUuid)
        runnable10 = new Runnable() {
            @Override
            public void run() {
                //sendMessageToService("from main");
            }
        };
        mHandler.postDelayed(runnable10, 2000);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;
        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg){
            MainActivity activity = mActivity.get();

        }
    }
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // Get extra data included in the Intent
            String message = intent.getStringExtra("messageString");
            //log.e("receiver", "Got message: " + message);
            float[] messagefloat= intent.getFloatArrayExtra("messageFloat");
            String dataText = "step count: " + messagefloat[0] + "\nstep_length: " + messagefloat[1] + "\ndegree: " + messagefloat[2]+ "\nDirection: " + messagefloat[3]+ "\nStepWidth: " + messagefloat[4];
            Log.e("delay_check","textData");
            textData.setText(dataText);

            //Log.d("receiver", "Got message: " + message);
        }
    };
    private TimerTask timerTask;
    private final Timer starttimer = new Timer();
    private void startTimerTask() {//타이머 함수 0초가 되면 측정 종료
        stopTimerTask();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                textTime.post(new Runnable() {
                    @Override
                    public void run() {
                        textTime.setText(time_sec + " 초");
                        //  //log.e("bbc", String.valueOf((reqTime-lastsenserdata)/1000));
                    }

                });
                time_sec++;
            }
        };
        starttimer.schedule(timerTask, 1000, 1000);
    }
    private void stopTimerTask() {//타이머 스톱 함수
        if (timerTask != null) {
            time_sec = 0;
            textTime.setText("0 초");
            timerTask.cancel();
            timerTask = null;
        }

    }

}