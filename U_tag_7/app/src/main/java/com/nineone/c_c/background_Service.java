package com.nineone.c_c;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelUuid;
import android.os.PowerManager;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import com.nineone.c_c.databinding.ActivityMainBinding;
public class background_Service extends Service implements SensorEventListener {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }
    private static SensorManager mSensorManager;
    private Sensor mAccelerometer; // 가속도 센스
    private Sensor mGyroerometer; // 자이로 센스
    private Sensor mMagnetometer; // 자력계 센스
    private Sensor mBarometer; // 기압계

    public background_Service() {
    }

    private PowerManager powerManager;
    // private PowerManager.WakeLock wakeLock;
    private List<ScanFilter> btLeScanFilters;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanSettings btLeScanSettings;
    private String ACTION_STOP_SERVICE = "STOP";
    private NotificationCompat.Builder builder;
    private NotificationManager mNotificationManager;
    private AdvertiseData mService_AdvData;
    private AdvertiseSettings mService_AdvSettings;
    private BluetoothLeAdvertiser mServiceAdvertiser;
    private boolean mService_Advertiserstart = false;
    private String phonenumber;
    private String phonenumber_back;
    static {
        System.loadLibrary("c_c");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);//가속도
        mGyroerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);//자력계
        mBarometer = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);//기압계
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null){
            //   textAcc.setText("가속도 센서를 지원하지 않습니다.");
        }
        // boolean chk2 = mSensorManager.registerListener(listener, mGyroerometer,SensorManager.SENSOR_DELAY_UI);
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) == null){
            // textGyr.setText("자이로 센서 지원하지 않음");
        }
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null){
            // textBaro.setText("기압계 센서 지원하지 않음");
        }
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mGyroerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mBarometer, SensorManager.SENSOR_DELAY_UI);
        if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
            onDestroy();
            // Log.e("onDestroy", "serviceddd");
        }

        // Log.e("BLE33", String.valueOf(intent)+", "+String.valueOf(flags));
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
       /* IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver1, filter1);
        IntentFilter filter2 = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(mBroadcastReceiver1, filter2);*/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
        }
        phonenumber = tm.getLine1Number();

        if(phonenumber!=null) {
            phonenumber_back = phonenumber.substring(phonenumber.length()-4,phonenumber.length());
            int num=3;
            for (int a = 0; a < 4; a++) {
                int sub1 = phonenumber.length()-(2+(a*2));//13-2=11, 13-4=9,  13-6=7, 13-8=5,
                int sub2 = phonenumber.length()-(a*2);    //13-0=13, 13-2=11, 13-4=9, 13-6=7
              /*  int sub1 = phonenumber.length()-(a*2);//    12-8 = 4,  12-6=6  12-4=8
                int sub2 = phonenumber.length()-((a*2)-2);//12-8-2=6,12-6-2=4*/
                String stringco= phonenumber.substring(sub1,sub2);
                int parsedResult = (int) Long.parseLong(stringco, 16);
                // Log.e("phonenumber1", String.valueOf(parsedResult));
                byte convert = (byte) (parsedResult);
                arrayBytes4[num]=convert;//7 8   5 6   3 4
                num--;
            }
            // Log.e("phonenumber3", Arrays.toString(arrayBytes4));
        }else{
            phonenumber_back="0123";
            for (int a = 0; a < 4; a++) {
                int parsedResult = (int) Long.parseLong(String.valueOf(a), 16);
                byte convert = (byte) (parsedResult);
                arrayBytes4[a]= convert;
            }
        }

    //    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // bluetoothCheck();
        Intent stopSelf = new Intent(this, background_Service.class);
        stopSelf.setAction(ACTION_STOP_SERVICE);

        PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf,PendingIntent.FLAG_IMMUTABLE);

        builder = new NotificationCompat.Builder(this, "default");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("MO-"+phonenumber_back);
        builder.setContentText("스캔 중");
        builder.addAction(R.drawable.ic_launcher_foreground,"Close", pStopSelf);

        Intent notificationIntent = new Intent(this, MainActivity.class)
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
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

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        btLeScanFilters = new ArrayList<ScanFilter>();
        ScanFilter.Builder scanbuilder = new ScanFilter.Builder();
        scanbuilder.setManufacturerData(0x004c, new byte[] {});
        //scanbuilder.setDeviceName("NI-201");
        ScanFilter filter = scanbuilder.build();
        //btLeScanFilters.add(filter);
        btLeScanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build ();
     //   mBluetoothLeScanner.startScan(btLeScanFilters, btLeScanSettings,leScanCallback);
        mBluetoothLeScanner.startScan(leScanCallback);
        main_ble();
        for(int a=0;a<=15;a++){
            arrayBytes16_8[a]=0x00;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(arrayBytes16_8);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        hiuuid_8 = new UUID(high, low);
        tagAdapter = new TagAdapter(getApplicationContext());

        return START_NOT_STICKY;
    }

    private void main_ble() {

        BluetoothAdapter.getDefaultAdapter().setName("MO-"+phonenumber_back);
        mBluetoothAdapter.getRemoteDevice("00:19:19:40:D7:2B");
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
    private boolean isonoff = false;
    @Override
    public void onDestroy() { //여기다가 종료할때 모든 코드 넣기 https://developer.android.com/guide/components/services?hl=ko
        super.onDestroy();
            isonoff=true;
            if (mServiceAdvertiser != null) {
                mServiceAdvertiser.stopAdvertising(mService_AdvCallback);
            }
            if (mBluetoothLeScanner != null) {
                mBluetoothLeScanner.stopScan(leScanCallback);
            }

            stopForeground(true);
            //stopService(new Intent(getApplicationContext(), MyService4.class));
            stopSelf();


        // Log.e("serviceddd","serviceddd");
    }



    private final byte[] arrayBytes16_7 = new byte[16];
    private final byte[] arrayBytes16_8 = new byte[16];
    private final byte[] arrayBytes4 = new byte[4];
    private UUID hiuuid_8;
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result){
            // Log.e("BLE", "Discovery onScanResult00: " + result.getDevice().getName());
            if (result.getDevice().getName() != null) {
                // Log.e("BLE", "Discovery onScanResult01: " + result.getDevice().getName());
                if (result.getDevice().getName().startsWith("NI-201")) {
                    // Log.e("BLE", "Discovery onScanResult02: " + result.getDevice().getName());
                    //   // Log.e("BLE", "Discovery onScanResult011: " + result.getDevice().getName());
                    //    // Log.e("BLE", "Discovery onScanResult012: " + Arrays.toString(result.getScanRecord().getBytes()));
                    //    // Log.e("BLE", "Discovery onScanResult013: " + result.getDevice().getAddress());
                    //    // Log.e("BLE", "Discovery onScanResult02: " + byteArrayToHex(result.getScanRecord().getBytes()));
                    /*// Log.e("BLE", "Discovery onScanResult1: " + result);
                    // Log.e("BLE", "Discovery onScanResult2: " + result.getScanRecord());
                    // Log.e("BLE", "Discovery onScanResult3: " + Arrays.toString(result.getScanRecord().getBytes()));
                    // Log.e("BLE", "Discovery onScanResult4: " + result.getScanRecord().getBytes());*/
                    if (!isonoff) {
                        addScanResult(result);
                    }else{
                        onDestroy();
                    }
                }
            }
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                //// Log.e("BLE", "Discovery onBatchScanResults: " + result.getDevice().getName().getBytes());
                addScanResult( result );
            }
        }
        private void addScanResult( ScanResult _result ) {
            // get scanned device
            BluetoothDevice device = _result.getDevice();
            String device_addName = device.getName();
            int device_addRssi = _result.getRssi();
            int device_addbyte_num1 = (int) Long.parseLong(String.valueOf(_result.getScanRecord().getBytes()[9+18]), 16);
            byte device_addbyte_num1_1 = (byte) device_addbyte_num1;
            int device_addbyte_num2 = (int) Long.parseLong(String.valueOf(_result.getScanRecord().getBytes()[9+19]), 16);
            byte device_addbyte_num2_1= (byte) device_addbyte_num2;
            byte device_addbyte_rssi = (byte) _result.getRssi();
            // Log.e("tagAdaptercount", String.valueOf(tagAdapter.getItemCount()));
            // Log.e("tagAdapter1name", device_addName);
            //// Log.e("tagAdapter1rssi", String.valueOf(device_addRssi));
            // Log.e("tagAdapter1num1", String.valueOf(device_addbyte_num1));
            // Log.e("tagAdapter1num2", String.valueOf(device_addbyte_num2));
           // // Log.e("tagAdapter1rssi", String.valueOf(device_addbyte_rssi));
           /* for (int a = 0; a < 16; a++) {
                arrayBytes16[a] = newScanRecord[(sensorStartIdx+15) - a];//9+15=24
            }*/
            tagAdapter.addItem(device_addName, device_addRssi, device_addbyte_num1_1, device_addbyte_num2_1, device_addbyte_rssi);
            //0 3 6 9 12 15
            if(tagAdapter.getItemCount()>=5) {
                for (int a = 0; a <= 4; a++) {
                    arrayBytes16_7[(a * 3)] = tagAdapter.getItemInmlist().get(a).getItem_byte_rssi();    //0 3 6 9  12
                    arrayBytes16_7[(a * 3) + 1] = tagAdapter.getItemInmlist().get(a).getItem_byte_num2();//1 4 7 10 13
                    arrayBytes16_7[(a * 3) + 2] = tagAdapter.getItemInmlist().get(a).getItem_byte_num1();//2 5 8 11 14
                }
            }else{
                for (int a = 0; a < tagAdapter.getItemCount(); a++) {
                    arrayBytes16_7[(a * 3)] = tagAdapter.getItemInmlist().get(a).getItem_byte_rssi();  //0 3 6 9 12 15
                    arrayBytes16_7[(a * 3) + 1] = tagAdapter.getItemInmlist().get(a).getItem_byte_num2();//1 4 7 10 13
                    arrayBytes16_7[(a * 3) + 2] = tagAdapter.getItemInmlist().get(a).getItem_byte_num1();//2 5 8 11 14
                }
                for(int a = tagAdapter.getItemCount(); a <= 4; a++) {
                    arrayBytes16_7[(a * 3)] = 0;  //0 3 6 9 12 15
                    arrayBytes16_7[(a * 3) + 1] = 0;//1 4 7 10 13
                    arrayBytes16_7[(a * 3) + 2] = 0;//2 5 8 11 14
                }
            }
            arrayBytes16_7[15] = 0x07;  //0 3 6 9 12 15

          /*  ByteBuffer byteBuffer = ByteBuffer.wrap(arrayBytes16_7);
            long high = byteBuffer.getLong();
            long low = byteBuffer.getLong();
            UUID hiuuid_7 = new UUID(high, low);
            ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
            bb.putLong(hiuuid_7.getMostSignificantBits());
            bb.putLong(hiuuid_7.getLeastSignificantBits());
            bb.array();

            UUID suuid = UUID.nameUUIDFromBytes(arrayBytes16_7);
            String datad = new String(arrayBytes16_7);
            // Log.e("uuid_main_00", tagAdapter.getItemInName().toString());

            if (!mService_Advertiserstart) {
                Runnable runnable10;//.addServiceUuid(pUuid)
                if(!send_7_8) {
                    runnable10 = new Runnable() {
                        @Override
                        public void run() {
                           // send_7_8 = true;

                            ParcelUuid pUuid = new ParcelUuid(hiuuid_7);
                            mService_AdvData = new AdvertiseData.Builder()
                                    .setIncludeDeviceName(true)
                                    .setIncludeTxPowerLevel(false)
                                    //.addServiceUuid(pUuid)
                                    .addServiceData(pUuid, arrayBytes4)
                                    .build();
                            handler2();
                            // Log.e("UUID_service2", "asdasd2");
                        }
                    };
                }else{
                    runnable10 = new Runnable() {
                        @Override
                        public void run() {
                           // send_7_8 = false;
                            arrayBytes16_8[15] = 0x08;
                            ParcelUuid pUuid = new ParcelUuid(hiuuid_8);
                            mService_AdvData = new AdvertiseData.Builder()
                                    .setIncludeDeviceName(true)
                                    .setIncludeTxPowerLevel(false)
                                    //.addServiceUuid(pUuid)
                                    .addServiceData(pUuid, arrayBytes4)
                                    .build();
                            handler2();
                            // Log.e("UUID_service3", "asdasd2");
                        }
                    };
                }
                mService_Advertiserstart = true;
                listcange_handler.postDelayed(runnable10, 1000);
                // add the device to the result list
            }*/
        }
    };
    private boolean send_7_8 = false;
    public static String byteArrayToHexaString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte data : bytes) {
            builder.append(String.format("%02X ", data));
        }
        return builder.toString();
    }
    private void handler1() {
        // Log.e("tagAdaptercount", String.valueOf(tagAdapter.getItemCount()));
        // Log.e("tagAdapter1name", tagAdapter.getItemInName().toString());
        // Log.e("tagAdapter1rssi", tagAdapter.getItemInRssi().toString());
        mhandler_setContentTitle.postDelayed(new Runnable() {
            public void run() {
                builder.setContentTitle(tagAdapter.getItemTop5());
                //builder.setContentText(tagAdapter.getItemTop5());
                // Log.e("tagAdapter1rssi", tagAdapter.getItemTop5());
                mNotificationManager.notify(1, builder.build());
            }
        }, 0);
    }
    private void handler2() {
        if(!isonoff)  {
            mhandler_setContentTitle.postDelayed(new Runnable() {
                public void run() {
                    builder.setContentText(tagAdapter.getItemTop5());
                    //builder.setContentText(tagAdapter.getItemTop5());
                    //  // Log.e("tagAdapter1rssi", tagAdapter.getItemTop5());
                    mNotificationManager.notify(1, builder.build());
                }
            }, 0);
            // // Log.e("asd","asdasd");
            mhandler1_startAdvertising.postDelayed(new Runnable() {
                public void run() {
                    mServiceAdvertiser.startAdvertising(mService_AdvSettings, mService_AdvData, mService_AdvCallback);
                    // Log.e("BLE", "Discovery onScanResult011: " + mService_AdvCallback.toString());
                }
            }, 0);
            mhandler2_stopAdvertising.postDelayed(new Runnable() {
                public void run() {
                    mServiceAdvertiser.stopAdvertising(mService_AdvCallback);
                    mService_Advertiserstart = false;
                    // Log.e("BLE", "Discovery onScanResult012: " +  mService_AdvCallback.toString());
                }
            }, 500);
        }
    }
    private TagAdapter tagAdapter;
    private final Handler mhandler_setContentTitle = new Handler();
    private final Handler mhandler1_startAdvertising = new Handler();
    private final Handler mhandler2_stopAdvertising = new Handler();
    private final MyHandler listcange_handler = new MyHandler(this);
    private float[] arrayfloat=new float[6];
    private native float[] Utag_Arrary(float accx,float accy,float accz,float gyrox,float gyroy,float gyroz);
    private float[] fasf = new float[5];
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            //  textAcc.setText("가속도 센서값\nx: " + String.format("%.4f", sensorEvent.values[0]) + "\ny: " + String.format("%.4f", sensorEvent.values[1])+ "\nz: " + String.format("%.4f", sensorEvent.values[2]));
           // tv.setText(stringFromJN0(String.format("%.4f", sensorEvent.values[0]),String.format("%.4f", sensorEvent.values[1]),String.format("%.4f", sensorEvent.values[2])));
            arrayfloat[0]=sensorEvent.values[0];
            arrayfloat[1]=sensorEvent.values[1];
            arrayfloat[2]=sensorEvent.values[2];
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // textGyr.setText("\n자이로 센서값\nx: " + String.format("%.4f", sensorEvent.values[0]) + "\ny: " + String.format("%.4f", sensorEvent.values[1]) + "\nz: " + String.format("%.4f", sensorEvent.values[2])+ "\n");
            arrayfloat[3]=sensorEvent.values[0];
            arrayfloat[4]=sensorEvent.values[1];
            arrayfloat[5]=sensorEvent.values[2];
        }/* else if (sensorEvent.sensor.getType() == Sensor.TYPE_PRESSURE) {//기압
            long timestamp = sensorEvent.timestamp;
            float presure = sensorEvent.values[0];
            presure = (float) (Math.round(presure*100)/100.0); //소수점 2자리 반올림
            //기압을 바탕으로 고도를 계산(맞는거 맞아???)
            float height = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, presure);
            //textBaro.setText("기압계 센서값\nx: " + String.format("%.4f", presure) +" hPa \n 고도: "+height+"m" );

        }*/
        // Log.e("onAccuracyChange",arrayfloat[0]+","+ arrayfloat[1]+","+  arrayfloat[2]+","+  arrayfloat[3]+","+  arrayfloat[4]+","+  arrayfloat[5]);
        fasf = Utag_Arrary(arrayfloat[0], arrayfloat[1], arrayfloat[2], arrayfloat[3], arrayfloat[4], arrayfloat[5]);
        sendMessage();
        // Log.e("onAccuracyChange1",fasf[0]+","+ fasf[1]+","+  fasf[2]+","+  fasf[3]+","+  fasf[4]);
        int intBits1 = (int) fasf[0];
        arrayBytes16_8[13] = (byte) (intBits1 >> 8);
        arrayBytes16_8[14] = (byte) (intBits1);
        // Log.e("onAccuracyChange2",fasf[2]+" , "+(byte) (intBits1 >> 24)+","+ (byte) (intBits1 >> 16)+","+(byte) (intBits1 >> 8)+" ,"+(byte) (intBits1));
        int intBits2 =  Float.floatToIntBits(fasf[2]);
        arrayBytes16_8[9] = (byte) (intBits2 >> 24);
        arrayBytes16_8[10] = (byte) (intBits2 >> 16);
        arrayBytes16_8[11] = (byte) (intBits2 >> 8);
        arrayBytes16_8[12] = (byte) (intBits2);
        arrayBytes16_8[0] = 0x01;
        // Log.e("onAccuracyChange3",(byte) (intBits2 >> 24)+","+ (byte) (intBits2 >> 16)+","+(byte) (intBits2 >> 8)+" ,"+(byte) (intBits2));

        ByteBuffer UUID_byteBuffer = ByteBuffer.wrap(arrayBytes16_8);
        long high_08 = UUID_byteBuffer.getLong();
        long low_08 = UUID_byteBuffer.getLong();
        hiuuid_8 = new UUID(high_08, low_08);
        if(tagAdapter.getItemCount()>=5) {
            for (int a = 0; a <= 4; a++) {
                arrayBytes16_7[(a * 3)] = tagAdapter.getItemInmlist().get(a).getItem_byte_rssi();  //0 3 6 9 12 15
                arrayBytes16_7[(a * 3) + 1] = tagAdapter.getItemInmlist().get(a).getItem_byte_num2();//1 4 7 10 13
                arrayBytes16_7[(a * 3) + 2] = tagAdapter.getItemInmlist().get(a).getItem_byte_num1();//2 5 8 11 14
            }
        }else{
            for(int a = 0; a <= 4; a++) {
                arrayBytes16_7[(a * 3)] = 0;  //0 3 6 9 12 15
                arrayBytes16_7[(a * 3) + 1] = 0;//1 4 7 10 13
                arrayBytes16_7[(a * 3) + 2] = 0;//2 5 8 11 14
            }
        }
        arrayBytes16_7[15] = 0x07;  //0 3 6 9 12 15
        arrayBytes16_8[15] = 0x08;
        //
        ByteBuffer byteBuffer = ByteBuffer.wrap(arrayBytes16_7);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        UUID hiuuid_7 = new UUID(high, low);
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(hiuuid_7.getMostSignificantBits());
        bb.putLong(hiuuid_7.getLeastSignificantBits());
        bb.array();
        UUID suuid = UUID.nameUUIDFromBytes(arrayBytes16_7);
        String datad = new String(arrayBytes16_7);
        // Log.e("uuid_main_00", tagAdapter.getItemInName().toString());

        if (!mService_Advertiserstart) {
            Runnable runnable10;//.addServiceUuid(pUuid)
            if (!send_7_8) {
                runnable10 = new Runnable() {
                    @Override
                    public void run() {
                         send_7_8 = true;

                        ParcelUuid pUuid = new ParcelUuid(hiuuid_7);
                        mService_AdvData = new AdvertiseData.Builder()
                                .setIncludeDeviceName(true)
                                .setIncludeTxPowerLevel(false)
                                //.addServiceUuid(pUuid)
                                .addServiceData(pUuid, arrayBytes4)
                                .build();
                        handler2();
                        // Log.e("UUID_service2", "asdasd2");
                    }
                };
            } else {
                runnable10 = new Runnable() {
                    @Override
                    public void run() {
                        send_7_8 = false;
                        arrayBytes16_8[15] = 0x08;
                        ParcelUuid pUuid = new ParcelUuid(hiuuid_8);
                        mService_AdvData = new AdvertiseData.Builder()
                                .setIncludeDeviceName(true)
                                .setIncludeTxPowerLevel(false)
                                //.addServiceUuid(pUuid)
                                .addServiceData(pUuid, arrayBytes4)
                                .build();
                        handler2();
                        // Log.e("UUID_service3", "asdasd2");
                    }
                };
            }
            mService_Advertiserstart = true;
            listcange_handler.postDelayed(runnable10, 1000);
            //
        }
        // textBaro.setText("  "+fasf[0]+" , "+fasf[1]+" , "+fasf[2]);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Log.e("onAccuracyChanged","onAccuracyChanged");
    }
    private static class MyHandler extends Handler {
        private final WeakReference<background_Service> mActivity;

        public MyHandler(background_Service activity) {
            mActivity = new WeakReference<background_Service>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            background_Service activity = mActivity.get();
            if (activity != null) {
                // ...
            }
        }
    }
    public String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for(final byte b: a)
            sb.append(String.format("%02x ", b&0xff));
        return sb.toString();
    }
    private int ConvertToIntLittle(byte[] txValue, int startidx) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(txValue[startidx]);
        byteBuffer.put(txValue[startidx + 1]);
        byteBuffer.put((byte) 0x00);
        byteBuffer.put((byte) 0x00);

        byteBuffer.flip();
        int result = byteBuffer.getInt();
        return result;
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
            builder.setContentText("전송오류");
            // mAdvStatus.setText(statusText);
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            //mAdvStatus.setText(R.string.status_advertising);
        }
    };
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                // Log.e("off1", action);
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        //Intent enableIntent = new Intent(BluetoothAdapter.ACTION_STATE_CHANGED);
                        //startActivityForResult(enableIntent, REQUEST_ENABLE_BT3);
                    //   onDestroy();
                        Toast.makeText(getApplication(), "블루투스가 종료되었습니다.\n 블루투스를 실행시켜 주세요 ", Toast.LENGTH_SHORT).show();
                        // Log.e("off1", "off1");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        // Log.e("off2", "off2");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        // Log.e("off3", "off3");

                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        // Log.e("off4", "off4");
                        break;
                    default:
                        // Log.e("off5", String.valueOf(state));
                        break;
                }
            }
            if (action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                // Log.e("off6", action + ", " + intent);

            }
            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {

                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (isGpsEnabled || isNetworkEnabled) {
                    // Log.e("off7", String.valueOf(isGpsEnabled));
                } else {
                    onDestroy();
                    Toast.makeText(getApplication(), "GPS가 종료되었습니다.\n GPS를 실행시켜 주세요 ", Toast.LENGTH_SHORT).show();

                    // // Log.e("off8", String.valueOf(isGpsEnabled));
                }
            }
        }
    };

    private void sendMessage(){
        Log.d("messageService", "Broadcasting message");
        Intent intent = new Intent("custom-event-name");
        intent.putExtra("messageString", "This is my first message!");
        intent.putExtra("messageFloat", fasf);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
