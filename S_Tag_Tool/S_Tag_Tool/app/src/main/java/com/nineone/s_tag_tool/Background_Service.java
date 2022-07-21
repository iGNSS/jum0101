package com.nineone.s_tag_tool;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Background_Service extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public Background_Service() {
    }

    private BluetoothLeScanner mBluetoothLeScanner;
    private String ACTION_STOP_SERVICE = "STOP";
    private NotificationCompat.Builder builder;// 알림만들기
    private Notification builder2;// 알림만들기
    private NotificationManager mNotificationManager;
    private boolean mService_Advertiserstart = false;
    private String phonenumber;
    private String phonenumber_back;
    private RecyclerViewAdapter recyclerVierAdapter;
    private long RescanBaseTime;
    private static final String TAG_FOREGROUND_SERVICE = "FOREGROUND_SERVICE";

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
                isonoff = true;
                onDestroy();
                Log.e("activityt_TAG", "serviceddd");
            }
            switch (action) {

                case ACTION_START_FOREGROUND_SERVICE:
                    if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
                        isonoff = true;
                        onDestroy();
                        Log.e("activityt_TAG", "serviceddd");
                    }
                    Log.e("activityt_TAG2b", "activityt_TAG2");

                    RescanBaseTime = SystemClock.elapsedRealtime();
                    Intent stopSelf = new Intent(this, Background_Service.class);
                    stopSelf.setAction(ACTION_STOP_SERVICE);
                    PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_IMMUTABLE);

                    builder = new NotificationCompat.Builder(this, "default");
                    builder.setSmallIcon(R.mipmap.ic_launcher);
                    builder.setContentTitle("S Tag Tool");
                    //   builder.setContentText(fasf[0]+", "+fasf[2]);
                    builder.setContentText("스캔 중");

                    builder.addAction(R.drawable.ic_launcher_foreground, "Close", pStopSelf);

                    Intent notificationIntent = new Intent(this, MainActivity.class)
                            .setAction(ACTION_STOP_FOREGROUND_SERVICE)
                            .addCategory(Intent.CATEGORY_LAUNCHER)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

                    builder.setContentIntent(pendingIntent);

                    // 오레오 버전 이상 노티피케이션 알림 설정
                    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    // mNotificationManager.createNotificationChannel(new NotificationChannel("default", "기본채널", NotificationManager.IMPORTANCE_DEFAULT));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mNotificationManager.createNotificationChannel(new NotificationChannel("default", "undead_service", NotificationManager.IMPORTANCE_NONE));
                    }
                    mNotificationManager.notify(1, builder.build());
                    Notification notification = builder.build();
                    startForeground(1, notification);
                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

                    soundPool = new SoundPool.Builder().setMaxStreams(8).build();
                    soundPlay = soundPool.load(getApplicationContext(), R.raw.arml, 1);

                        Runnable runnable10 = new Runnable() {
                            @Override
                            public void run() {
                                //  startScan();
                                if (!isonoff) {
                                    mBluetoothLeScanner.startScan(leScanCallback);
                                }
                            }
                        };
                        timechange_handler.postDelayed(runnable10, 1000);
                        //   startTimerTask();

                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                    isonoff=true;
                    stopForegroundService();
                    onDestroy();
                    break;
            }
        }



        return START_NOT_STICKY;
    }
    private void stopForegroundService() {

        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            if (!isonoff) {

                if (result.getDevice().getName() != null) {
                    //  Log.e("BLE", "Discovery onScanResult01: 작동2 " + result.getDevice().getName());
                    if (result.getDevice().getName().startsWith("TJ-")) {
                        Log.e("123123",result.getDevice().getName());
                        update(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                        getEllapse();
                    }
                }
            }else {
                isonoff=true;
                onDestroy();
            }
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {

        }
    };
    private int sensorStartIdx = 9;
    private boolean alarm_ON_OFF = false;
    private SoundPool soundPool;
    private int soundPlay;
    private final ArrayList<ScannedDevice> listData = new ArrayList<>();
    public String update(BluetoothDevice newDevice, int rssi, byte[] scanRecord) {
        if (!isonoff) {
            if ((newDevice == null) || (newDevice.getAddress() == null) || newDevice.getName() == null) {
                return "";
            }

            long now = System.currentTimeMillis();

            boolean contains = false;
            for (ScannedDevice device : listData) {
                if (newDevice.getAddress().equals(device.getDevice().getAddress())) {

                    contains = true;
                    // update
                    device.setDisplayName(newDevice.getName());
                    device.setRssi(rssi);
                    device.setLastUpdatedMs(now);
                    device.setScanRecord(scanRecord);
                    Log.e("mAlarm_on3", Arrays.toString(scanRecord));
                    break;
                }
                builder.setContentTitle("수신중인 기기 수: " + String.valueOf(listData.size()));
                //builder.setContentText(tagAdapter.getItemTop5());
                // Log.e("tagAdapter1rssi", tagAdapter.getItemTop5());
                mNotificationManager.notify(1, builder.build());
                byte[] ble_data = device.getScanRecord();
                boolean mAlarm_on = false;
                //long now = System.currentTimeMillis();
                Date dateNow = new Date(now);
                Date dateCreated = new Date(device.getLastUpdatedMs());
                long duration = dateNow.getTime() - dateCreated.getTime();
                long nResult = duration / 1000;
                //  Log.e("Arrays.toString12",device.getDisplayName()+","+nResult);
                boolean getstopfalse = false;
                if (nResult > 3) {
                    getstopfalse = true;
                }
                int Sensor_Alarm = 0;
                Sensor_Alarm = ble_data[sensorStartIdx + 5];
                int senser_type = 0;
                int os2_errer = 0;
                int CO_errer2 = 0;
                int H2S_errer2 = 0;
                int CO2_errer2 = 0;
                int CH4_errer2 = 0;
                //  int Sensor_Alarm = 250;
                senser_type = ((Sensor_Alarm) & 0x07);
                os2_errer = ((Sensor_Alarm >> 7) & 0x01);
                CO_errer2 = ((Sensor_Alarm >> 6) & 0x01);
                H2S_errer2 = ((Sensor_Alarm >> 5) & 0x01);
                CO2_errer2 = ((Sensor_Alarm >> 4) & 0x01);
                CH4_errer2 = ((Sensor_Alarm >> 3) & 0x01);
                StringBuilder alarmstring = new StringBuilder();
                Log.e("mAlarm_on0", Arrays.toString(ble_data));
                if (os2_errer == 1 && !getstopfalse) {
                    mAlarm_on = true;
                    alarmstring.append("O2 ");
                }
                if (CO_errer2 == 1 && !getstopfalse) {
                    mAlarm_on = true;
                    alarmstring.append("CO ");
                    Log.e("mAlarm_on1", Arrays.toString(ble_data));
                }
                if (H2S_errer2 == 1 && !getstopfalse) {
                    mAlarm_on = true;
                    alarmstring.append("H2S ");
                }
                if (CO2_errer2 == 1 && !getstopfalse) {
                    mAlarm_on = true;
                    alarmstring.append("CO2 ");
                    Log.e("mAlarm_on2", Arrays.toString(ble_data));
                }
                if (CH4_errer2 == 1 && !getstopfalse) {
                    mAlarm_on = true;
                    alarmstring.append("CH4 ");
                }
                if (mAlarm_on) {


                    Runnable alarm_runnable = new Runnable() {
                        @Override
                        public void run() {
                            //notifyDataSetChanged();
                            alarm_ON_OFF = false;

                        }
                    };
                    if (!alarm_ON_OFF) {
                        alarmstring.append("경고");
                        Log.e("alarm_ON_OFF", " alarm_ON_OFF");
                        alarm_ON_OFF = true;
                        soundPool.play(soundPlay, 1f, 1f, 6, 0, 1f);
                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            //  vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                            vibrator.vibrate(VibrationEffect.createOneShot(1000, 150));//0~255
                        } else {
                            vibrator.vibrate(750);

                        }
                        listcange_handler.postDelayed(alarm_runnable, 1000);
                        builder2 = new NotificationCompat.Builder(getApplicationContext(), "default")
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle(device.getDisplayName())
                                .setContentText(alarmstring)
                                //.setContentText("스캔 중")
                                .setGroup(GROUP_KEY_WORK_EMAIL)
                                .build();
                        phonenumber_back2++;

                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                        notificationManager.notify(phonenumber_back2, builder2);
                    }
                }
            }
            if (!contains) {
                String[] DeviceNameArray = newDevice.getName().trim().split("-");
                if (DeviceNameArray.length >= 3) {
                    listData.add(new ScannedDevice(newDevice, rssi, scanRecord, now));
                }

            }


        }
        return "";
    }
    private boolean isonoff = false;
    @Override
    public void onDestroy() { //여기다가 종료할때 모든 코드 넣기 https://developer.android.com/guide/components/services?hl=ko
        super.onDestroy();
        isonoff=true;
        if (mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(leScanCallback);
        }

        stopForeground(true);
        //stopService(new Intent(getApplicationContext(), Background_Service.class));
        stopSelf();


        // Log.e("serviceddd","serviceddd");
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        //do something you want
        //stop service
        this.stopSelf();
    }
    private ArrayList<String> mtag_name_arrary = new ArrayList();
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
    private final MyHandler timechange_handler = new MyHandler(this);
    private final MyHandler listcange_handler = new MyHandler(this);
    private static class MyHandler extends Handler {
        private final WeakReference<Background_Service> mActivity;

        public MyHandler(Background_Service activity) {
            mActivity = new WeakReference<Background_Service>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Background_Service activity = mActivity.get();
            if (activity != null) {

            }
        }
    }

    private Timer timer = new Timer();
    int phonenumber_back2=2;
    String GROUP_KEY_WORK_EMAIL = "com.android.example.WORK_EMAIL";

    private void startTimerTask () {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                builder2 = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("MO-"+phonenumber_back2)
              //  builder2.setContentText(fasf[0]+", "+fasf[2]);
                .setContentText("스캔 중")
                .setGroup(GROUP_KEY_WORK_EMAIL)
                .build();
                phonenumber_back2++;

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                notificationManager.notify(phonenumber_back2, builder2);


            }
        },1000, 1500);

    }

}
