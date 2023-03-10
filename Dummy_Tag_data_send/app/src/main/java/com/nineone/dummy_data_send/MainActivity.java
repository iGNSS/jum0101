package com.nineone.dummy_data_send;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.InputType;
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
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView textView1, textView2, textView3, mmiiliscUV, mmiilisRF, mErrerUV, mErrerRF ,mErrerTag;
    private TextView UV_url_text, UV_port_text,UV_delay_text;
    private Button button1;
    private boolean mInformation_boolean = false;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private String UV_url_string, UV_port_string;
    private int UV_delay_string;
    private  SimpleDateFormat aftertime;
    private int delay=1000;
    //???????????? ??????, ble ????????? ????????????(????????? ??????), socket ???????????? ????????? ?????? ????????? ??????
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        UV_url_text = findViewById(R.id.UVadress);
        UV_port_text = findViewById(R.id.UVport);
        UV_delay_text = findViewById(R.id.UVdelay);

        textView1 = findViewById(R.id.TextView1);
        textView2 = findViewById(R.id.UVcount);
        mmiiliscUV = findViewById(R.id.miiliscUV);
        mErrerUV = findViewById(R.id.errerUV);
        mErrerTag = findViewById(R.id.errerTag);
        button1 = findViewById(R.id.Button1);
        aftertime = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
        SharedPreferences sf = getSharedPreferences("Change_settings", MODE_PRIVATE); //text?????? key??? ????????? ?????? ????????? ??????. ???????????? ???????????? ????????? ""??? ??????
        // Datbuffer_Put();
        UV_url_string = sf.getString("UV_url", "10.10.10.11");
        UV_port_string = sf.getString("UV_port", "3009");
        UV_delay_string = sf.getInt("UV_delay",1000);
        UV_url_text.setText(UV_url_string);
        UV_port_text.setText(UV_port_string);
        UV_delay_text.setText(String.valueOf(UV_delay_string));
        UV_url_text.setOnLongClickListener(mClickListener);
        UV_port_text.setOnLongClickListener(mClickListener);
        UV_delay_text.setOnLongClickListener(mClickListener);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mInformation_boolean) {
                    delay = UV_delay_string;

                    startScan();
                    check_If_list_Empty();
                   // porsenser();
                    textView1.setText("?????? ???");
                    button1.setText("??????");


                    mErrerUV.setText("");
                    mErrerTag.setText("");
                    mInformation_boolean = true;
                    // list_set_Http();
                } else {
                    mblecheck=false;
                    stopScan();
                    check_If_list_Empty();
                    a = 0;
                    b = 0;
                    tag_count=0;
                   // mSensorManager.unregisterListener(MainActivity.this);

                    textView1.setText("?????? ??????");
                    button1.setText("??????");

                    mInformation_boolean = false;
                    stopTimerTask();
                }


            }
        });

        bluetoothCheck();
        check_If_list_Empty();
    }

    private static final int REQUEST_ENABLE_BT = 2;//ble ??????????????? ??????

    private void bluetoothCheck() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        // mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // ???????????? ???????????? ????????? ???????????????.
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "??? ????????? ???????????? ????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
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
    private int period = 3000;
    int[] intram = {1, 2, 3, 4};
    Random random = new Random();
    private int uvindex = 0;
    int period2 = 4000;

    private void startTimerTask1() {
        timerTask = new TimerTask() {
            @Override
            public void run() { // ?????? ??????
                period2 = period % 3000;
                //Log.e("aabradom_",period2+", "+ period);
                if (period2 == 0) {
                    // a++;
                    change = true;
                    // recordUV_check_in_Http_post();
                    textView2.setText(String.valueOf(a));
                    uvindex = uvindex + 3;
                    period = 0;
                }

                period += 1000;

                //   b++;
                change = false;
                // recordRF_check_in_Http_post();
               // textView3.setText(String.valueOf(b));

            }
        };

        timer.schedule(timerTask, 1000, 1000);
    }

   /* private void Datbuffer_Put() {
        mdatabuffer[0] = (byte) Long.parseLong("02", 16);
        // int parsedResult = (int) Long.parseLong("D4", 16);
        mdatabuffer[1] = (byte) Long.parseLong("D4", 16);
        Log.e("asd2", String.format("%02x", mdatabuffer[1] & 0xff));
        mdatabuffer[2] = (byte) Long.parseLong("00", 16);
        mdatabuffer[3] = (byte) Long.parseLong("71", 16);
        mdatabuffer[4] = (byte) Long.parseLong("FF", 16);
        mdatabuffer[5] = (byte) Long.parseLong("FF", 16);
        mdatabuffer[6] = (byte) Long.parseLong("FF", 16);
        mdatabuffer[7] = (byte) Long.parseLong("4A", 16);
        mdatabuffer[8] = (byte) Long.parseLong("A7", 16);
        mdatabuffer[9] = (byte) Long.parseLong("51", 16);
        mdatabuffer[10] = (byte) Long.parseLong("41", 16);
        mdatabuffer[11] = (byte) Long.parseLong("0C", 16);
        mdatabuffer[12] = (byte) Long.parseLong("00", 16);
        mdatabuffer[13] = (byte) Long.parseLong("00", 16);
        mdatabuffer[14] = (byte) Long.parseLong("00", 16);
        mdatabuffer[15] = (byte) Long.parseLong("00", 16);
        mdatabuffer[16] = (byte) Long.parseLong("00", 16);
        mdatabuffer[17] = (byte) Long.parseLong("00", 16);
        mdatabuffer[18] = (byte) Long.parseLong("00", 16);
        //mdatabuffer[19] = (byte) Long.parseLong("00", 16);
        //mdatabuffer[20] = (byte) Long.parseLong("03", 16);
        //recordUV_check_in_socket_thread thread = new recordUV_check_in_socket_thread(mdatabuffer);
        //thread.start();
    }*/

    //  private byte[] mdatabuffer = new byte[6500];





    private void stopTimerTask() {//????????? ?????? ??????
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mBluetoothLeScanner.stopScan(leScanCallback);

            mBluetoothLeScanner.startScan(leScanCallback);
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

    private String Millis_time(long millis_time) {
        long systemmillis = System.currentTimeMillis() - millis_time;
        String stringmillis = String.valueOf(systemmillis);
        return stringmillis;
    }


    private byte[] blecall_arr = new byte[500];
    private boolean mblecheck = false;
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            mblecheck=true;
            check_If_list_Empty();
            mblecheck=false;
            if (result.getDevice().getName() != null) {
                BluetoothDevice bluetoothDevice = result.getDevice();
                if (bluetoothDevice.getName().startsWith("TJ-0001")) {///asd
                    //  Log.e("rssid1", String.valueOf(result.getRssi()));
                    Log.e("result_ble1", String.valueOf(result.getDevice().getName()));
                    Log.e("result_ble2", Arrays.toString(result.getScanRecord().getBytes()));
                    String[] DeviceNameArray = bluetoothDevice.getName().trim().split("-");
                    byte[] U_tag_Address = new byte[13];
                    byte[] U_tag_record = new byte[21];
                    String adress = result.getDevice().getAddress();
                    String[] address_arr = adress.trim().split(":");
                    for (int i = 0; i < 6; i++) {
                        U_tag_Address[i] = (byte) Long.parseLong(address_arr[i], 16);
                    }
                    byte rssi = (byte) result.getRssi();
                    U_tag_Address[6] = rssi;
                    U_tag_Address[7] = (byte) Long.parseLong("01", 16);
                    U_tag_Address[8] = (byte) Long.parseLong("00", 16);

                    U_tag_Address[9] = ((byte) ((Long.parseLong(DeviceNameArray[2], 16)) & 0xff));
                    U_tag_Address[10] = (byte) (((Long.parseLong(DeviceNameArray[2], 16)) & 0xff) << 8);
                    U_tag_Address[11] = (byte) (((Long.parseLong(DeviceNameArray[2], 16)) & 0xff) << 16);
                    U_tag_Address[12] = (byte) (((Long.parseLong(DeviceNameArray[2], 16)) & 0xff) << 24);
                    for (int j = 0; j < 21; j++) {
                        U_tag_record[j] = (byte) (result.getScanRecord().getBytes()[j+9] & 0xff);
                    }

                    //Log.e("result_ble2", String.valueOf(result.getDevice().getName()));
                    boolean contains = false;
                    int Minor_Number = ConvertToIntLittle2(result.getScanRecord().getBytes(), 9 + 18);
                    //Log.e("result_ble3", String.valueOf(Minor_Number));
                    if (Minor_Number == 1) {
                        for (Ble_item device : listData) {
                            if (bluetoothDevice.getAddress().equals(device.getTag_Adress())) {
                                contains = true;
                                // update
                                device.setTag_int_Rssi(result.getRssi());
                                device.setTag_Rssi(result.getRssi());
                                device.setTag_ScanRecord01(U_tag_record);
                                device.setTag0102(1);
                                break;
                            }
                        }
                        if (!contains) {
                            if (DeviceNameArray.length >= 3) {
                                //  Log.e("rssid2", String.valueOf(result.getRssi()));
                                listData.add(new Ble_item(bluetoothDevice.getAddress(), bluetoothDevice.getName(), result.getRssi(),U_tag_Address, U_tag_record, 1));
                            }
                        }
                    } else if (Minor_Number == 2) {
                        for (Ble_item device : listData) {
                            if (bluetoothDevice.getAddress().equals(device.getTag_Adress())) {
                                contains = true;
                                // update
                                device.setTag_Rssi(result.getRssi());
                                device.setTag_int_Rssi(result.getRssi());
                                device.setTag_ScanRecord02(U_tag_record);
                                device.setTag0102(2);
                                break;
                            }
                        }
                        if (!contains) {
//                            String[] DeviceNameArray = bluetoothDevice.getName().trim().split("-");
                            if (DeviceNameArray.length >= 3) {
                                //  Log.e("rssid2", String.valueOf(result.getRssi()));
                                listData.add(new Ble_item(bluetoothDevice.getAddress(), bluetoothDevice.getName(), result.getRssi(),U_tag_Address, U_tag_record, 2));
                            }
                        }
                    }



                    getEllapse();
                } else {

                }
            }
            Runnable runnable_ble_sned;

            if(mInformation_boolean) {
                if (!ble_sned_Boolean && listData != null) {
                    ble_sned_Boolean = true;
                    runnable_ble_sned = () -> {
                        // Datbuffer_Put();

                        recordUV_check_in_socket_thread thread = new recordUV_check_in_socket_thread();
                        thread.start();

                        //ble_ScanRecord_arr();
                        //  ble_hashmap_add();
                        // Network_Confirm();
                    };
                    start_handler.postDelayed(runnable_ble_sned, delay);
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
        }
    };

    private ArrayList<Byte> mbytearr = new ArrayList<>();
    private void Datbuffer_Put() {
        mdatabuffer.add((byte) Long.parseLong("02", 16));
        // int parsedResult = (int) Long.parseLong("D4", 16);
        mdatabuffer.add((byte) Long.parseLong("D3", 16));//type
        mdatabuffer.add((byte) Long.parseLong("00", 16));
        mdatabuffer.add((byte) Long.parseLong("3C", 16)); //number
        mdatabuffer.add((byte) Long.parseLong("00", 16));
        mdatabuffer.add((byte) Long.parseLong("00", 16));
        mdatabuffer.add((byte) Long.parseLong("00", 16));
        mdatabuffer.add((byte) Long.parseLong("00", 16));//BATT
        mdatabuffer.add((byte) Long.parseLong("00", 16));//Barometer
        mdatabuffer.add((byte) Long.parseLong("00", 16));
        mdatabuffer.add((byte) Long.parseLong("00", 16));//??????
        mdatabuffer.add((byte) Long.parseLong("00", 16));
        mdatabuffer.add((byte) Long.parseLong("00", 16));//?????????x
        mdatabuffer.add((byte) Long.parseLong("00", 16));
        mdatabuffer.add((byte) Long.parseLong("00", 16));//y
        mdatabuffer.add((byte) Long.parseLong("00", 16));
        mdatabuffer.add((byte) Long.parseLong("00", 16));//z
        mdatabuffer.add((byte) Long.parseLong("00", 16));
        mdatabuffer.add((byte) Long.parseLong("00", 16));//move
        mdatabuffer.add((byte) Long.parseLong(String.valueOf(listData.size()), 16));//Reserve
        Log.d("mdatabuffer0", String.valueOf(listData.size()));
        ble_ScanRecord_arr();
        mdatabuffer.addAll(mbytearr);
        // mdatabuffer.addAll(mbytearr);
        mdatabuffer.add((byte) Long.parseLong("03", 16));
        Log.d("mdatabuffer1", String.valueOf(mdatabuffer.size()));
        Log.d("mdatabuffer2", Arrays.toString(mdatabuffer.toArray()));
       /* if (listData.size() != 0) {
            listData = new ArrayList<>();
            //  Send_Http_post();
        }*/
    }
    private void ble_ScanRecord_arr() {
        mbytearr = new ArrayList<>();
        Log.e("U_tag_record", String.valueOf(listData.size()));
        for (Ble_item device : listData) {
            Log.e("U_tag_record", device.getTag_Name());
            Log.e("U_tag_record", String.valueOf(listData.size()));
            if (device.getTag_ScanRecord01() != null) {
                Log.e("U_tag_record1", "1 " + String.valueOf(device.getTag_ScanRecord01().length));
            }if ( device.getTag_ScanRecord02() != null) {
                Log.e("U_tag_record2", "2 " + String.valueOf(device.getTag_ScanRecord02().length));
            }
            if (device.getTag_ScanRecord01() != null && device.getTag_ScanRecord02() != null) {
              //  Log.e("U_tag_record1","1 "+ String.valueOf(device.getTag_Adress_byte().length));
              //  Log.e("U_tag_record2","2 "+ String.valueOf(device.getTag_ScanRecord01().length));
              //  Log.e("U_tag_record3","3 "+ String.valueOf(device.getTag_ScanRecord02().length));
                for (int i = 0; i < device.getTag_Adress_byte().length; i++) {
                    mbytearr.add(device.getTag_Adress_byte()[i]);

                }
                Log.e("byte_sussss11", Arrays.toString(device.getTag_Adress_byte()));
                for (int i = 0; i < device.getTag_ScanRecord01().length; i++) {
                    mbytearr.add(device.getTag_ScanRecord01()[i]);
                }
                Log.e("byte_sussss12", Arrays.toString(device.getTag_ScanRecord01()));
                for (int i = 0; i < device.getTag_ScanRecord02().length; i++) {
                    mbytearr.add(device.getTag_ScanRecord02()[i]);
                }
                Log.e("byte_sussss13", Arrays.toString(device.getTag_ScanRecord02()));
            }
            Log.e("byte_sussss14", Arrays.toString(mbytearr.toArray()));
            Log.e("byte_sussss15", String.valueOf(mbytearr.size()));
        }
       byte[] bytes2 = new byte[mbytearr.size()];
        for (int i = 0; i < mbytearr.size(); i++) {
            bytes2[i] = mbytearr.get(i);
        }
        String tmp = byteArrayToHex(bytes2);
        Log.e("byte_sussss16", Arrays.toString(mbytearr.toArray()));
        Log.e("byte_sussss17", tmp);
        Log.e("rssid1", String.valueOf(mbytearr.size()));
        Log.e("rssid2", String.valueOf(mbytearr.size() / 55));
        Log.e("rssid3", Arrays.toString(mbytearr.toArray()));


    }
    private ArrayList<Byte> mdatabuffer = new ArrayList<>();
    private boolean socket_connect = false;
    private int tag_count =0;
    private boolean tag_name_bool = false;
    class recordUV_check_in_socket_thread extends Thread {
        String host; // ?????? IP
        int port;
        ArrayList<Byte> data; // ?????? ?????????
        DataOutputStream dos;
        DataInputStream dis;
        Socket socket;
        byte[] bytes;

        public recordUV_check_in_socket_thread() {
            this.host = UV_url_string;
            //this.host = "10.10.10.11";
            this.port = Integer.parseInt(UV_port_string);
            //this.port = 3009;
            //     this.data = databuffer;
        }
        @Override
        public void run() {
            Datbuffer_Put();
            bytes = new byte[mdatabuffer.size()];
            for (int i = 0; i < mdatabuffer.size(); i++) {
                bytes[i] = mdatabuffer.get(i);
            }

            // ?????? ??????

            try {
                socket = new Socket(host, port);
                socket_connect = true;

            } catch (IOException e1) {
                socket_connect = false;
                Runnable runnableUV2 = new Runnable() {
                    @Override
                    public void run() {
                        mErrerUV.setText("??????????????????");
                    }
                };
                UVchange_handler.postDelayed(runnableUV2, 0);
                Log.w("servers??????2:", "??????????????????");

                e1.printStackTrace();
            }

            Log.w("servers: ", "????????????????????? ????????? ????????????");
            if (socket_connect) {
                OutputStream os = null;
                try {
                    os = socket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dos = new DataOutputStream(os);

                Log.w("servers??????:", "???????????? ??????");

                try {

                    //dos.writeUTF(Integer.toString(bytes.length));
                    // dos.flush();
                    dos.write(bytes);
                    dos.flush();
                    String tmp = byteArrayToHex(bytes);
                    Log.e("servers", tmp);
                    Log.e("servers", String.valueOf(bytes.length));
                    Log.e("servers", "?????????...");
                    //  DataInputStream dIn = new DataInputStream(socket.getInputStream());
                    Log.e("servers1", "?????????...");
                    //  byte[] buffer = new byte[1024];
                    // StringBuilder inputLine = new StringBuilder();
                    Log.e("servers2", "?????????...");
                    //String tmpp;
                    //while ((tmpp = dIn.readLine()) != null) {
                    //   Log.e("servers3", "?????????...");
                    //  inputLine.append(tmpp);
                    //}
                    Log.e("servers4", "?????????...");
                    //byte buffer2 = dIn.readByte();
                    //  byteint = dIn.read(buffer);
                    Log.e("servers", "???????????????: ????????? ?????? ?????????...");

                    //  String tmp = byteArrayToHex(buffer);
                    //  Log.e("servers", "???????????????: ????????? ?????????:" + byteint);
                    //Log.e("servers", "???????????????: ????????? ?????????2:" + inputLine.toString());
                    Log.e("servers", "??????");
                    //InputStream in = socket.getInputStream();
                    // Log.e("??????","?????????????????????="+in);
                    // DataInputStream dis = new DataInputStream(in);
                    //Log.e("??????","?????????????????????="+dis);
                    os.close();
                    dos.close();
                    //dis.close();
                    //sb.toString();
                    //Log.e("??????", String.valueOf(dis.readByte()));
                    Runnable runnableUV3 = new Runnable() {
                        @Override
                        public void run() {
                            Log.e("tests", "0");
                            tag_name_bool=true;
                            //mErrerUV.setText("HTTP_OK " + String.valueOf(HttpResult));
                            long uvmillistime = System.currentTimeMillis();
                            String item_updateMS = aftertime.format(uvmillistime);
                            mmiiliscUV.setText(item_updateMS);
                            tag_count++;

                            textView2.setText(String.valueOf(tag_count));
                            mErrerUV.setText("?????? ??????\n????????? ?????? ???");


                            tag_name_bool = false;
                            Log.e("servererror", String.valueOf(listData.size()));
                            Log.e("tests", "1");
                        }
                    };
                    UVchange_handler.postDelayed(runnableUV3, 0);
                    socket.close();

                } catch (Exception e) {
                    Log.e("servererror", "eee");
                }
            }


            mdatabuffer = new ArrayList<>();
            bytes = new byte[mdatabuffer.size()];
            ble_sned_Boolean = false;
            Log.e("tests", "2");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder sb3 = new StringBuilder();
                    for (Ble_item device : listData) {
                        sb3.append(device.getTag_Name());
                        sb3.append(" ");
                        sb3.append(device.getTag_Rssi());
                        sb3.append("\r\n");
                    }
                    mErrerTag.setText(sb3.toString().toString());
                    if (listData.size() != 0 && !tag_name_bool) {
                        listData = new ArrayList<>();
                        //  Send_Http_post();
                    }
                    // Stuff that updates the UI
                }
            });
            Log.e("tests", "3");
        }
       /* @Override
        public void run() {
            Datbuffer_Put();
            try {
                Log.d("TCP1", "servers connecting");
                InetAddress serverAddr = InetAddress.getByName(host);
                Socket sock = new Socket(serverAddr, port);
                try {
                    Log.d("TCP1", "??????????????????");
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())), true);

                    out.println(Arrays.toString(mdatabuffer.toArray()));
                    Log.d("TCP1", Arrays.toString(mdatabuffer.toArray()));
                    out.flush();
                    Log.d("TCP1", "????????????");
                    DataInputStream dIn = new DataInputStream(sock.getInputStream());
                    Log.e("TCP1", "?????????...");
                    byte[] buffer = new byte[1024];
                    // StringBuilder inputLine = new StringBuilder();
                    Log.e("TCP1", "?????????...");
                    //byte buffer2 = dIn.readByte();
                    byteint = dIn.read(buffer);
                    Log.e("TCP1", "???????????????: ????????? ?????? ?????????...");

                    //  String tmp = byteArrayToHex(buffer);
                    Log.e("TCP1", "???????????????: ????????? ?????????:" + byteint);
                    sock.close();
                    mdatabuffer = new ArrayList<>();
                } catch (IOException e) {
                    Log.d("TCP1", "don't send message");
                    e.printStackTrace();
                }

            } catch (UnknownHostException e) {
                Log.d("TCP11", "don't send message");
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("TCP12", "don't send message");
                e.printStackTrace();
            }
            mdatabuffer = new ArrayList<>();
            bytes = new byte[mdatabuffer.size()];
            ble_sned_Boolean = false;
            //return null;
        }*/


    }
    private void check_If_list_Empty() {
        if (!mblecheck && mIsScanning) {
            mErrerUV.setText("BLE ?????? ??????");
        } else if (!mIsScanning) {
            mErrerUV.setText("");
        }
    }

    private ArrayList<Ble_item> listData = new ArrayList<>();
    private Map<String, Integer> BLE_HASHMAP;
    private Map<String, Object> SEND_HASHMAP;
    private boolean ble_sned_Boolean = false;

    private static SensorManager mSensorManager;
    private Sensor mBarometer; // ?????????

    private void senser_check() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mBarometer = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);//?????????
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null) {
            Barometer_have_boolean = false;
            Log.e("????????????", "763");
        }
        mSensorManager.registerListener(this, mBarometer, SensorManager.SENSOR_DELAY_UI);
    }

    private void porsenser() {
        mInformation_boolean = true;
        PRESSURE_add = new ArrayList<>();
        senser_check();

        //mSensorManager.unregisterListener(this);
    }

    private int ConvertToIntLittle2(byte[] txValue, int startidx) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
        // by choosing big endian, high order bytes must be put
        // to the buffer before low order bytes
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        // since ints are 4 bytes (32 bit), you need to put all 4, so put 0
        // for the high order bytes
        byteBuffer.put(txValue[startidx + 1]);
        byteBuffer.put(txValue[startidx]);
        byteBuffer.put((byte) 0x00);
        byteBuffer.put((byte) 0x00);

        byteBuffer.flip();
        int result = byteBuffer.getInt();
        return result;
    }
    public String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for (final byte b : a)
            sb.append(String.format("%02x ", b & 0xff));
        return sb.toString();
    }

    public String readUTF8(DataInputStream in) throws IOException {
        int length = in.readInt();
        byte[] encoded = new byte[length];
        in.readFully(encoded, 0, length);
        return new String(encoded);
    }
    private long SensorbaseTime;
    private int SensorbaseTimecount = 2;
    private ArrayList<Float> PRESSURE_add;
    private float PRESSURE_avg;
    private boolean Barometer_have_boolean = true;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PRESSURE) {//??????
            //     long timestamp = sensorEvent.timestamp;
            //Log.e("??????dlTdma", String.valueOf(PRESSURE_add.size()));
            float presure = sensorEvent.values[0];
            presure = (float) (Math.round(presure * 100) / 100.0); //????????? 2?????? ?????????
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
        //????????? ?????? ??????
        long nowTime = SystemClock.elapsedRealtime();
        //???????????? ????????? ????????? ??????
        long overTime = nowTime - timesec;
        //
        // Log.e("overtime", String.valueOf(overTime));
        long sec = (overTime / 1000) % 60;
        long min = ((overTime / 1000) / 60) % 60;
        long hour = ((overTime / 1000) / 60) / 60;
        //  long ms = overTime % 1000;

        return sec;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("connect_TAG", "onResume");

      //  startScan();
    }

    @Override
    protected void onPause() {
        Log.e("connect_TAG", "onPause");
        super.onPause();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
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
        builder.setTitle("?????? ??????");
        builder.setMessage("????????? ?????????????????????????");
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("??????", null);
        builder.show();
    }
    
    private View.OnLongClickListener mClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            if (!mInformation_boolean) {
            } else {
                Toast.makeText(getApplicationContext(), "????????? ????????? ?????????.", Toast.LENGTH_LONG).show();

            }
            if (v.getId() == R.id.UVadress) {
                if (!mInformation_boolean) {
                    final EditText editText = new EditText((MainActivity.this));
                    editText.setGravity(Gravity.CENTER);
                    editText.setText(UV_url_string);
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setTitle("URL");
                    alertDialog.setView(editText);
                    alertDialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (editText.getText().length() != 0) {
                                SharedPreferences sharedPreferences = getSharedPreferences("Change_settings", MODE_PRIVATE); //text?????? key??? ????????? ?????? ????????? ??????. ???????????? ???????????? ????????? ""??? ??????
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
                                Toast.makeText(getApplicationContext(), "URL??? ????????? ?????????.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    alertDialog.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    alertDialog.show();
                } else {
                    Toast.makeText(getApplicationContext(), "????????? ????????? ?????????.", Toast.LENGTH_LONG).show();

                }
            }
            if (v.getId() == R.id.UVport) {
                if (!mInformation_boolean) {
                    final EditText editText = new EditText((MainActivity.this));
                    editText.setGravity(Gravity.CENTER);
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                    editText.setText(UV_port_string);

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setTitle("Port");
                    alertDialog.setView(editText);
                    alertDialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (editText.getText().length() != 0) {
                                SharedPreferences sharedPreferences = getSharedPreferences("Change_settings", MODE_PRIVATE); //text?????? key??? ????????? ?????? ????????? ??????. ???????????? ???????????? ????????? ""??? ??????
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
                                Toast.makeText(getApplicationContext(), "PORT??? ????????? ?????????.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    alertDialog.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    alertDialog.show();
                } else {
                    Toast.makeText(getApplicationContext(), "????????? ????????? ?????????.", Toast.LENGTH_LONG).show();

                }
            }
            if (v.getId() == R.id.UVdelay) {
                if (!mInformation_boolean) {
                    final EditText editText = new EditText((MainActivity.this));
                    editText.setGravity(Gravity.CENTER);
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                    editText.setText(String.valueOf(UV_delay_string));

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setTitle("Delay");
                    alertDialog.setMessage("Min:1000, Max:5000\n??????:ms");
                    alertDialog.setView(editText);

                    alertDialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (editText.getText().length() != 0) {

                                if (Integer.parseInt(editText.getText().toString()) >= 1000 && Integer.parseInt(editText.getText().toString()) <= 5000) {
                                    SharedPreferences sharedPreferences = getSharedPreferences("Change_settings", MODE_PRIVATE); //text?????? key??? ????????? ?????? ????????? ??????. ???????????? ???????????? ????????? ""??? ??????
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putInt("UV_delay", Integer.parseInt(editText.getText().toString()));
                                    editor.apply();
                                    Runnable runnable12 = new Runnable() {
                                        @Override
                                        public void run() {
                                            UV_delay_string = sharedPreferences.getInt("UV_delay", 1000);
                                            UV_delay_text.setText(String.valueOf(UV_delay_string));
                                        }
                                    };
                                    listcange_handler.postDelayed(runnable12, 2);
                                } else {
                                    Toast.makeText(getApplicationContext(), "????????? ?????? ????????? ?????????.", Toast.LENGTH_LONG).show();
                                }

                            } else {
                                Toast.makeText(getApplicationContext(), "Delay ?????? ????????? ?????????.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    alertDialog.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    alertDialog.show();
                } else {
                    Toast.makeText(getApplicationContext(), "????????? ????????? ?????????.", Toast.LENGTH_LONG).show();

                }
            }

            return false;
        }
    };


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
                    //cred.put("user_id", ID_name_string);
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

                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                  //  mmiilisRF.setText(Millis_time(rfmillistime));
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
                            if (success) {
                                make = "Success " + message;
                            } else {
                                make = errors + ", " + message;
                            }
                            Log.e("return_result2", success + "," + errors + "," + message);
                            Runnable runnableRF4 = new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                    alertDialog.setTitle("ID ??????");
                                    alertDialog.setMessage(make);
                                    alertDialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
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

                    Log.e("dd-212u", HttpResult + con.getResponseMessage());
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