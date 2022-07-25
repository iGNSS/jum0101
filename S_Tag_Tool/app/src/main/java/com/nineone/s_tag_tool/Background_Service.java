package com.nineone.s_tag_tool;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    public static final String ACTION_NOTIF_CENCEL = "ACTION_NOTIF_CENCEL";
    private ScanSettings btLeScanSettings;
    private ArrayList<ScanFilter> btLeScanFilters;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            Log.e("activityt_TAGaction", action);
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
                    btLeScanFilters = new ArrayList<ScanFilter>();

                    btLeScanSettings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                          //  .setReportDelay(0)
                            .build();

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
                            .setAction(Intent.ACTION_MAIN)
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
                    ScanFilter scanFilter = new ScanFilter.Builder()
                            .setManufacturerData(37265, new byte[]{})
                            // .setServiceUuid(new ParcelUuid("........ uuid reference ......"))
                            .build();
                    btLeScanFilters.add(scanFilter);
                    mBluetoothLeScanner.startScan(btLeScanFilters, btLeScanSettings, leScanCallback);
                    Runnable runnable10 = new Runnable() {
                        @Override
                        public void run() {
                            //  startScan();
                            if (!isonoff) {
                                mBluetoothLeScanner.startScan(btLeScanFilters, btLeScanSettings, leScanCallback);
                            }
                        }
                    };
                  /*  for (int i = 30000; i <= 40000; i++) {
                        ScanFilter scanFilter = new ScanFilter.Builder()
                                .setManufacturerData(i, new byte[]{})
                               // .setServiceUuid(new ParcelUuid("........ uuid reference ......"))
                                .build();
                        btLeScanFilters.add(scanFilter);
                        if (i == 40000) {
                            timechange_handler.postDelayed(runnable10, 1000);
                        }
                    }*/
                    // timechange_handler.postDelayed(runnable10, 1000);
                    //   startTimerTask();

                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                    isonoff = true;
                    stopForegroundService();
                    onDestroy();
                    break;
                case ACTION_NOTIF_CENCEL:
                    Log.e("ACTION_NOTIF_CENCEL","ACTION_NOTIF_CENCEL");

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
                    if (result.getDevice().getName().startsWith("TJ-00CA")) {
                     //   Log.e("1231234", result.getDevice().getName()+", "+String.valueOf(result.getScanRecord().getManufacturerSpecificData()));
                     //   Log.e("1231235", Arrays.toString(result.getScanRecord().getBytes()));
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
          //  Log.e("1231236", String.valueOf(results.size()));
            for(ScanResult scanResult : results){
            //    Log.e("1231237", scanResult.getDevice().getName());
                if (scanResult.getDevice().getName().startsWith("TJ-00CA")) {
                    update(scanResult.getDevice(), scanResult.getRssi(), scanResult.getScanRecord().getBytes());
                }
            }
            getEllapse();

        }
    };
    private int sensorStartIdx = 9;
    private boolean alarm_ON_OFF = false;
    private SoundPool soundPool;
    private int soundPlay;
    private final ArrayList<ScannedDevice> listData = new ArrayList<>();
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    public String update(BluetoothDevice newDevice, int rssi, byte[] scanRecord) {
        if (!isonoff) {
            if ((newDevice == null) || (newDevice.getAddress() == null) || newDevice.getName() == null) {
                return "";
            }
            long now = System.currentTimeMillis();
            int Sensor_Alarm = 0;
            Sensor_Alarm = scanRecord[sensorStartIdx + 5];
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
            boolean mAlarm_on = false;
            if(newDevice.getName().equals("TJ-00CA-0000000B-0000")) {
                int senser_O2 = ConvertToIntLittle(scanRecord, sensorStartIdx + 6);
                int senser_CO2 = ConvertToIntLittle(scanRecord, sensorStartIdx + 12);

              //  Log.e("mAlarm_on1", senser_O2+", "+senser_CO2+", " + Arrays.toString(scanRecord));

              //  Log.e("mAlarm_on2", Integer.parseInt(String.valueOf(Sensor_Alarm),16)+", "+String.valueOf(os2_errer)+", "+String.valueOf(CO2_errer2));
            }
            if (os2_errer == 1 ) {
                mAlarm_on = true;
                alarmstring.append("O2 ");
            }
            if (CO_errer2 == 1 ) {
                mAlarm_on = true;
                alarmstring.append("CO ");
            }
            if (H2S_errer2 == 1 ) {
                mAlarm_on = true;
                alarmstring.append("H2S ");
            }
            if (CO2_errer2 == 1 ) {
                mAlarm_on = true;
                alarmstring.append("CO2 ");
            }
            if (CH4_errer2 == 1 ) {
                mAlarm_on = true;
                alarmstring.append("CH4 ");
            }

            if (mAlarm_on) {

                if (!alarm_ON_OFF) {
                    alarmstring.append("경고");
                     alarm_ON_OFF = true;
                    soundPool.play(soundPlay, 1f, 1f, 6, 0, 1f);
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        //  vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                        vibrator.vibrate(VibrationEffect.createOneShot(1000, 150));//0~255
                    } else {
                        vibrator.vibrate(750);

                    }
                    boolean tag_names_Confirm_ture = false;
                  /*  for(String tag_names_Confirm : mNotification_tag_names) {
                        if(tag_names_Confirm.equals(newDevice.getName())) {
                            tag_names_Confirm_ture=true;
                        }
                    }if(!tag_names_Confirm_ture){*/
                        long index_num = 0;
                        String[] DeviceNameArray = newDevice.getName().trim().split("-");
                        index_num = Long.parseLong(DeviceNameArray[2], 16);

                        String sneer_type_name = "";
                        if(senser_type==1){
                            sneer_type_name = "BTS1"+"-"+index_num;
                        }else if(senser_type==2){
                            sneer_type_name = "BTS5"+"-"+index_num;
                        }else if(senser_type==3){
                            sneer_type_name = "BTS0"+"-"+index_num;
                        }
                        mNotification_tag_names.add(newDevice.getName());
                       /* Intent notificationIntent = new Intent()
                                .setAction(ACTION_NOTIF_CENCEL).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                         PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
                       */
                        Intent stopSelf = new Intent(this, Background_Service.class);
                        stopSelf.setAction(ACTION_NOTIF_CENCEL);
                        PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf,PendingIntent.FLAG_IMMUTABLE );

                        builder2 = new NotificationCompat.Builder(getApplicationContext(), "default")
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle(sneer_type_name)
                                .setContentText(alarmstring)
                                .setAutoCancel(true)
                                //.setContentText("스캔 중")
                                .setGroup(GROUP_KEY_WORK_EMAIL)
                                .setContentIntent(pStopSelf)
                                .build();

                        //builder2.flags |= Notification.FLAG_AUTO_CANCEL;
                        phonenumber_back2++;

                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                    PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK  |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.ON_AFTER_RELEASE, "My:Tag");
                    wakeLock.acquire(5000);
                    if(index_num<2147483647) {
                        int int_index_num = (int) index_num;
                        notificationManager.notify(int_index_num, builder2);
                        // }
                    }
                    Runnable alarm_runnable = new Runnable() {
                        @Override
                        public void run() {
                            //notifyDataSetChanged();
                            alarm_ON_OFF = false;

                        }
                    };
                    listcange_handler.postDelayed(alarm_runnable, 1000);
                }
            }
            boolean contains = false;

          for (ScannedDevice device : listData) {

                if (newDevice.getAddress().equals(device.getDevice().getAddress())) {

                    contains = true;
                    // update
                    device.setDisplayName(newDevice.getName());
                    device.setRssi(rssi);
                    device.setLastUpdatedMs(now);
                    device.setScanRecord(scanRecord);
                   // Log.e("mAlarm_on3", Arrays.toString(scanRecord));
                    break;
                }


            }
            if (!contains) {
                String[] DeviceNameArray = newDevice.getName().trim().split("-");
                if (DeviceNameArray.length >= 3) {
                    listData.add(new ScannedDevice(newDevice, rssi, scanRecord, now));
                    builder.setContentTitle("수신중인 기기 수: " + String.valueOf(listData.size()));
                    //builder.setContentText(tagAdapter.getItemTop5());
                    // Log.e("tagAdapter1rssi", tagAdapter.getItemTop5());
                    mNotificationManager.notify(1, builder.build());
                }

            }


        }
        return "";
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
    private final ArrayList<String> mNotification_tag_names = new ArrayList<>();
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
                    mBluetoothLeScanner.startScan(btLeScanFilters, btLeScanSettings, leScanCallback);
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
