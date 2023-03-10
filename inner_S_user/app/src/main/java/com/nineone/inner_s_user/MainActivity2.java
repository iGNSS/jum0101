package com.nineone.inner_s_user;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import tjlabs.android.jupiter_android_v2.JupiterCallBackManager;
import tjlabs.android.jupiter_android_v2.JupiterService;
import tjlabs.android.jupiter_android_v2.data.FineLocationTrackingOutput;

public class MainActivity2 extends AppCompatActivity implements SensorEventListener {
    private TextView mmessge,mtextMessge;
    private TextView textView1, textView2, textView3, mmiiliscUV;
    private TextView ID_name_text,mlogin_fail,mloncation;
    private TextView mlevel_post_result_text;
    private Button mlevel_post_result_button;
    private Button button1,SendButton;
    private String ID_name_string;
    private JupiterService jupiterService;
    private boolean mInformation_boolean = false;
    private boolean mid_send_success = false;
    private Spinner sector_spinner, floor_spinner;
    private String Sector_name=null;
    private int Sector_count=0;
    // private String NAME_url_string, NAME_port_string, NAME_path_string;
    //  private TextView NAME_url_text, NAME_port_text, NAME_path_text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        bluetoothCheck();
        SensorbaseTime = SystemClock.elapsedRealtime();
        ID_name_text = findViewById(R.id.IDname);
        mlogin_fail = findViewById(R.id.login_fail);
        //mlogin_fail.setText("???????????? ?????? ??? ?????????");
        mlevel_post_result_text = findViewById(R.id.Level_post_result_text);
       // mlevel_post_result_text.setText("?????? ????????? ?????????");
        mlevel_post_result_button = findViewById(R.id.Level_post_result_button);

        SendButton = findViewById(R.id.SendButton);

        mmessge = findViewById(R.id.mMessge);
        mtextMessge =findViewById(R.id.mtextMessge);
        button1 = findViewById(R.id.Button1);
        textView1 = findViewById(R.id.TextView1);

        SharedPreferences sf = getSharedPreferences("Change_settings", MODE_PRIVATE); //text?????? key??? ????????? ?????? ????????? ??????. ???????????? ???????????? ????????? ""??? ??????

        ID_name_string = sf.getString("ID_name", "inners_aos");
        ID_name_text.setText(ID_name_string);

        ID_name_text.setOnLongClickListener(mLongClickListener);

        SendButton.setOnClickListener(mClickListener);
        mlevel_post_result_button.setOnClickListener(mClickListener);
        button1.setOnClickListener(mClickListener);
        //senser_check();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) { // ???????????? ???????????? ????????? ??????????????????
                //       mblecheck=false;

            } else {
                bluetoothCheck();
                Toast.makeText(getApplicationContext(), "??????????????? ????????? ?????? ????????? ", Toast.LENGTH_SHORT).show();
                //  finish();
            }
        } if (requestCode == BEFOR_SEND_BT) {
            if (resultCode == Activity.RESULT_OK) { // ???????????? ???????????? ????????? ??????????????????
                //       mblecheck=false;

            } else {
                bluetoothCheck();
                Toast.makeText(getApplicationContext(), "??????????????? ????????? ?????? ????????? ", Toast.LENGTH_SHORT).show();
                //  finish();
            }
        }  else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    private String Floor_name=null;
    private int Floor_count=0;
    private void Spinner(){
       /* sector_spinner = findViewById(R.id.Sector_spinner);
        floor_spinner = findViewById(R.id.Floor_Spinner);

        //  ArrayAdapter<CharSequence> ward_coutAdapter = ArrayAdapter.createFromResource(this, R.array.Table, android.R.layout.simple_spinner_dropdown_item);
        //R.array.test??? ????????? ??????????????? 1???~12??? / android.R.layout.simple_spinner_dropdown_item??? ???????????? ??????????????? ???????????????.
        ArrayAdapter<CharSequence> ward_coutAdapter = ArrayAdapter.createFromResource(this, R.array.Table, R.layout.spinner_item);

        // ward_coutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sector_spinner.setAdapter(ward_coutAdapter); //???????????? ??????????????????.
        sector_spinner.setSelection(0);
        sector_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Sector_count=position;
                Sector_name=(String) sector_spinner.getItemAtPosition(position);
                Floor_chenck(Sector_count);
            } //??? ??????????????? ??????????????? position??? ????????? ?????? ??????????????? ??? ??? ????????????.
            //getItemAtPosition(position)??? ????????? ?????? ?????? ????????????????????????.
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });*/
    }
    private void Floor_chenck(int count){
     /*   if(count == 0) {
            ArrayAdapter<CharSequence> ward_coutAdapter = ArrayAdapter.createFromResource(this, R.array.KIER_cout, R.layout.spinner_item);
            //R.array.test??? ????????? ??????????????? 1???~12??? / android.R.layout.simple_spinner_dropdown_item??? ???????????? ??????????????? ???????????????.

          //  ward_coutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            floor_spinner.setAdapter(ward_coutAdapter); //???????????? ??????????????????.
            floor_spinner.setSelection(0);
            floor_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Floor_count = position;
                    Floor_name = (String) floor_spinner.getItemAtPosition(position);

                } //??? ??????????????? ??????????????? position??? ????????? ?????? ??????????????? ??? ??? ????????????.

                //getItemAtPosition(position)??? ????????? ?????? ?????? ????????????????????????.
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }else if(count == 1) {
            ArrayAdapter<CharSequence> ward_coutAdapter = ArrayAdapter.createFromResource(this, R.array.KEPCO_cout, R.layout.spinner_item);
            //R.array.test??? ????????? ??????????????? 1???~12??? / android.R.layout.simple_spinner_dropdown_item??? ???????????? ??????????????? ???????????????.

         //   ward_coutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            floor_spinner.setAdapter(ward_coutAdapter); //???????????? ??????????????????.
            floor_spinner.setSelection(0);
            floor_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Floor_count = position;
                    Floor_name = (String) floor_spinner.getItemAtPosition(position);

                } //??? ??????????????? ??????????????? position??? ????????? ?????? ??????????????? ??? ??? ????????????.

                //getItemAtPosition(position)??? ????????? ?????? ?????? ????????????????????????.
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }else if(count == 2) {
            ArrayAdapter<CharSequence> ward_coutAdapter = ArrayAdapter.createFromResource(this, R.array.KIST_cout, R.layout.spinner_item);
            //R.array.test??? ????????? ??????????????? 1???~12??? / android.R.layout.simple_spinner_dropdown_item??? ???????????? ??????????????? ???????????????.

          //  ward_coutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            floor_spinner.setAdapter(ward_coutAdapter); //???????????? ??????????????????.
            floor_spinner.setSelection(0);
            floor_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Floor_count = position;
                    Floor_name = (String) floor_spinner.getItemAtPosition(position);

                } //??? ??????????????? ??????????????? position??? ????????? ?????? ??????????????? ??? ??? ????????????.

                //getItemAtPosition(position)??? ????????? ?????? ?????? ????????????????????????.
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }else if(count == 3) {
            ArrayAdapter<CharSequence> ward_coutAdapter = ArrayAdapter.createFromResource(this, R.array.tjOffice_cout, R.layout.spinner_item);
            //R.array.test??? ????????? ??????????????? 1???~12??? / android.R.layout.simple_spinner_dropdown_item??? ???????????? ??????????????? ???????????????.

         //   ward_coutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            floor_spinner.setAdapter(ward_coutAdapter); //???????????? ??????????????????.
            floor_spinner.setSelection(0);
            floor_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Floor_count = position;
                    Floor_name = (String) floor_spinner.getItemAtPosition(position);

                } //??? ??????????????? ??????????????? position??? ????????? ?????? ??????????????? ??? ??? ????????????.

                //getItemAtPosition(position)??? ????????? ?????? ?????? ????????????????????????.
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }*/
    }
    private String url;
    private void ID_name_in_Http_post() {
        new Thread(() -> {
            try {
                Log.e("dd-", "164");
                String url = "http://stag.nineone.com:9988/user";

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
                    mlogin_fail.setText(e.getMessage());
                    mid_send_success= false;
                    //failmessage(e.getMessage());
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
                    // mmiilisRF.setText(Millis_time(rfmillistime));
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
                                Runnable runnableRF4 = new Runnable() {
                                    @Override
                                    public void run() {
                                      /*  AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity2.this);
                                        alertDialog.setTitle("ID ?????? ??????");
                                        alertDialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {*/
                                        String id_sussess_string = "ID ?????? ?????? - " + ID_name_string;
                                        mlogin_fail.setText("");
                                        mlevel_post_result_text.setText("");
                                        mSector_Level_success = false;
                                       // textView1.setText("Level??? ?????? ????????? ?????????");
                                        jupiterService = new JupiterService();
                                        jupiterService.setJupiterService(getApplication(), ID_name_string);
                                        jupiterService.sendUserInfo();
                                        jupiterService.setUVDListLength(3);
                                        mid_send_success = true;
                                        SendButton.setText("????????????");
                                        SendButton.getBackground().setTint(ContextCompat.getColor(getApplicationContext(), R.color.red));
                                        mid_send_success = true;
                                        /*    }
                                        });
                                        alertDialog.show();*/
                                    }
                                };
                                namechange_handler.postDelayed(runnableRF4, 0);
                            }else{
                                make = errors+", "+message;
                                Runnable runnableRF4 = new Runnable() {
                                    @Override
                                    public void run() {
                                        mlogin_fail.setText(make);
                                        mid_send_success= false;
                                      //  failmessage(make);
                                    }
                                };
                                namechange_handler.postDelayed(runnableRF4, 0);
                            }
                            Log.e("dd-return_result2", success + "," + errors+","+message);

                        } catch (JSONException e) {
                            mlogin_fail.setText(e.getMessage());
                           // failmessage(e.getMessage());
                            mid_send_success= false;
                            Log.e("dd-211u", "\n" + e.getMessage());
                            // Handle error
                        }
                    }
                    br.close();

                } else {
                    HttpURLConnection finalCon = con;
                    mlogin_fail.setText(HttpResult+" "+con.getResponseMessage());
                    mid_send_success= false;
                   // failmessage(HttpResult+" "+con.getResponseMessage());
                    Log.e("dd-212u", HttpResult+con.getResponseMessage());
                }
            } catch (IOException e) {
                mlogin_fail.setText("????????? ????????? ??? ????????????.\n???????????? ????????? ????????? ?????????");
                mid_send_success= false;
               // failmessage(e.getMessage());
                Log.e("dd-215u", e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    private void Pressure_post() {
        new Thread(() -> {
            try {
                Log.e("dd-", "164");
                String url = "http://stag.nineone.com:9988/pressurecal";

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
                    cred.put("pressure", PRESSURE_avg);
                    Log.e("PRESSURE_avg", String.valueOf(PRESSURE_avg));
                } catch (JSONException e) {
                    mlevel_post_result_text.setText(e.getMessage());
                    mSector_Level_success= false;
                    // failmessage(e.getMessage());
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
                    // mmiilisRF.setText(Millis_time(rfmillistime));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    Log.e("dd-fffu", sb.toString());
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
                                Runnable runnableRF4 = new Runnable() {
                                    @Override
                                    public void run() {
                                       // mlevel_post_result_text.setText(snedOK);
                                        //  textView1.setText("?????? ??????");
                                        mSector_Level_success= true;
                                        mlevel_post_result_text.setText(""+PRESSURE_avg);

                                    }
                                };
                                namechange_handler.postDelayed(runnableRF4, 0);
                            }else{
                                make = "???????????? ??????\n"+message;
                                Runnable runnableRF4 = new Runnable() {
                                    @Override
                                    public void run() {
                                        mlevel_post_result_text.setText(make);
                                        mSector_Level_success = false;
                                        // failmessage(make);
                                    }
                                };
                                namechange_handler.postDelayed(runnableRF4, 0);
                            }
                            Log.e("dd-return_result2", success + "," + errors+","+message);

                        } catch (JSONException e) {
                            Runnable runnableRF5 = new Runnable() {
                                @Override
                                public void run() {
                                    mlevel_post_result_text.setText(e.getMessage());
                                }
                            };
                            namechange_handler.postDelayed(runnableRF5, 0);

                            //failmessage(e.getMessage());
                            mSector_Level_success = false;
                            Log.e("dd-211u", "\n" + e.getMessage());
                            // Handle error
                        }
                    }
                    br.close();

                } else {
                    HttpURLConnection finalCon = con;

                    mlevel_post_result_text.setText(HttpResult+" "+con.getResponseMessage());
                    mSector_Level_success = false;
                    //  failmessage(HttpResult+" "+con.getResponseMessage());
                    Log.e("dd-212u", HttpResult+con.getResponseMessage());
                }
            } catch (IOException e) {

                Runnable runnableRF5 = new Runnable() {
                    @Override
                    public void run() {
                        mlevel_post_result_text.setText("????????? ????????? ??? ????????????.\n???????????? ????????? ????????? ?????????");
                    }
                };
                namechange_handler.postDelayed(runnableRF5, 0);

                mSector_Level_success = false;
                //failmessage(e.getMessage());
                Log.e("dd-215u", e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    private boolean mSector_Level_success = false;
    private void Sector_Level_post(String plevel) {
        new Thread(() -> {
            try {
                Log.e("dd-", "164");
                String url = "http://inners.nineone.com:9988/level";

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
                    cred.put("level_name", plevel);
                } catch (JSONException e) {
                    mlevel_post_result_text.setText(e.getMessage());
                    mSector_Level_success= false;
                   // failmessage(e.getMessage());
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
                    // mmiilisRF.setText(Millis_time(rfmillistime));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    Log.e("dd-fffu", sb.toString());
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
                                Runnable runnableRF4 = new Runnable() {
                                    @Override
                                    public void run() {
                                        String snedOK = Sector_name+" -  "+plevel;
                                        mlevel_post_result_text.setText(snedOK);
                                      //  textView1.setText("?????? ??????");
                                        mSector_Level_success= true;

                                    }
                                };
                                namechange_handler.postDelayed(runnableRF4, 0);
                            }else{
                                make = errors+", "+message;
                                Runnable runnableRF4 = new Runnable() {
                                    @Override
                                    public void run() {
                                        mlevel_post_result_text.setText(make);
                                        mSector_Level_success = false;
                                       // failmessage(make);
                                    }
                                };
                                namechange_handler.postDelayed(runnableRF4, 0);
                            }
                            Log.e("dd-return_result2", success + "," + errors+","+message);

                        } catch (JSONException e) {
                            mlevel_post_result_text.setText(e.getMessage());
                            //failmessage(e.getMessage());
                            mSector_Level_success = false;
                            Log.e("dd-211u", "\n" + e.getMessage());
                            // Handle error
                        }
                    }
                    br.close();

                } else {
                    HttpURLConnection finalCon = con;
                    mlevel_post_result_text.setText(HttpResult+" "+con.getResponseMessage());
                    mSector_Level_success = false;
                  //  failmessage(HttpResult+" "+con.getResponseMessage());
                    Log.e("dd-212u", HttpResult+con.getResponseMessage());
                }
            } catch (IOException e) {
                mlevel_post_result_text.setText("????????? ????????? ??? ????????????.\n???????????? ????????? ????????? ?????????");
                mSector_Level_success = false;
                //failmessage(e.getMessage());
                Log.e("dd-215u", e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    private void failmessage(String make){
        Runnable runnableRF4 = new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity2.this);
                alertDialog.setTitle("?????? ??????");
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
    }



  /*  private TimerTask timerTask;
    private Timer timer = new Timer();
    private void startTimerTask1() {
        timerTask = new TimerTask() {
            @Override
            public void run() { // ?????? ??????
                JupiterService_callback();

            }
        };
        timer.schedule(timerTask, 2000, 2000);
    }
    private void stopTimerTask() {//????????? ?????? ??????
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }*/


    @Override
    protected void onStart() {
        super.onStart();
        // Log.e("connect_TAG", "onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter2 = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);//gps ???????????? ??????
        filter2.addAction(Intent.ACTION_PROVIDER_CHANGED);
        registerReceiver(mBroadcastReceiver1, filter2);
        //  Log.e("connect_TAG", "onResume");
        //  startScan();
    }

    @Override
    protected void onPause() {
        //Log.e("connect_TAG", "onPause");
        super.onPause();
        //stopTimerTask();
        if(mSensorManager!=null){
            mSensorManager.unregisterListener(this);
        }
        try {
            unregisterReceiver(mBroadcastReceiver1);
        } catch (Exception ignored) {

        }
        if(jupiterService!=null) {
            jupiterService.stopJupiterService();
        }
    }

    @Override
    protected void onStop() {
        // Log.e("connect_TAG", "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Log.e("connect_TAG", "onDestroy()");
    }

    private final MyHandler namechange_handler = new MyHandler(this);
    private final MyHandler listcange_handler = new MyHandler(this);
    private final MyHandler start_handler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity2> mActivity;

        public MyHandler(MainActivity2 activity) {
            mActivity = new WeakReference<MainActivity2>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity2 activity = mActivity.get();
        }
    }

    private void beforsend_bluetoothCheck() {
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
            startActivityForResult(enableIntent, BEFOR_SEND_BT);
        }else{
            if (!mInformation_boolean) {
             //   Runnable runnableRF4 = new Runnable() {
             //       @Override
             //       public void run() {
                        jupiterService.startFineLocationTrackingService(JupiterService.PDR_SERVICE, 0);


                        jupiterService.requestFineLocationTrackingUpdate(new JupiterCallBackManager.FineLocationTrackingCallBack() {
                            @Override
                            public void onResponse(@NonNull FineLocationTrackingOutput fineLocationTrackingOutput) {
                                //Log.e("sectorDetectionOutput name", fineLocationTrackingOutput.toString());
                               // mmessge.setText(fineLocationTrackingOutput.toString());
                                /*   String findlocation0 = "Index "+"\n"
                                            + "Mobile Time "+"\n"
                                            + "Building Name "+"\n"
                                            + "Level "+"\n"
                                            + "X "+"\n"
                                            + "Y ";
                                    String findlocation1 = fineLocationTrackingOutput.getIndex()+"\n"
                                            + fineLocationTrackingOutput.getMobile_time()+"\n"
                                            + fineLocationTrackingOutput.getBuilding()+"\n"
                                            + fineLocationTrackingOutput.getLevel()+"\n"
                                            + fineLocationTrackingOutput.getX()+"\n"
                                            + fineLocationTrackingOutput.getY();*/
                                String findlocation0 = "Building Name "+"\n\n"
                                        + "Level Name "+"\n\n"
                                        + "X "+"\n\n"
                                        + "Y "+"\n\n"
                                        + "Index";
                                String findlocation1 = fineLocationTrackingOutput.getBuilding()+"\n\n"
                                        + fineLocationTrackingOutput.getLevel()+"\n\n"
                                        + fineLocationTrackingOutput.getX()+"\n\n"
                                        + fineLocationTrackingOutput.getY()+"\n\n"
                                        + fineLocationTrackingOutput.getIndex();
                               // Log.e("sendRSSISet ",)
                                Runnable runnable12 = new Runnable() {
                                    @Override
                                    public void run() {
                                        mtextMessge.setText(findlocation0);
                                        mmessge.setText(findlocation1);
                                    }
                                };
                                start_handler.postDelayed(runnable12, 0);


                                //????????????
                                // ????????????
                               // x
                               //         y
                               // ?????????
                            }
                        });
                //    }
             //   };
               // start_handler.postDelayed(runnableRF4, 2000);

                textView1.setText("?????? ???");
                button1.setText("??????");
                mInformation_boolean = true;
               // JupiterService_callback();
                //   startTimerTask1();
            } else {
                textView1.setText("?????? ??????");
                button1.setText("??????");
                mInformation_boolean = false;
                jupiterService.stopJupiterService();
            }
        }
        //log.e("BLE1245", "130");
    }
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.SendButton) {
                if (!mid_send_success) {
                    ID_name_in_Http_post();
                } else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity2.this);
                    alertDialog.setTitle("????????????");
                    alertDialog.setMessage("???????????? ???????????????????");
                    alertDialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SendButton.setText("?????????");
                            mlogin_fail.setText("");
                            SendButton.getBackground().setTint(ContextCompat.getColor(getApplicationContext(), R.color.sky));
                            mSector_Level_success = false;
                            mid_send_success = false;
                            mlevel_post_result_text.setText("");
                            Log.e("notlogin", "asd");

                            if (mInformation_boolean) {
                                textView1.setText("?????? ??????");
                                button1.setText("??????");
                                mInformation_boolean = false;
                                //  jupiterService.stopJupiterService();
                            }
                            textView1.setText("?????? ??????");
                            button1.setText("??????");
                            jupiterService.stopJupiterService();


                        }
                    });
                    alertDialog.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    alertDialog.show();

                }

            }

            if (v.getId() == R.id.Level_post_result_button) {
                if (mid_send_success) {
                    if (!mInformation_boolean) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity2.this);
                        alertDialog.setTitle("????????????");
                        alertDialog.setMessage("????????? ????????? ???????????????????");
                        alertDialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                porsenser();
                            }
                        });
                        alertDialog.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        alertDialog.show();

                    }else{
                        mlevel_post_result_text.setText("????????? ?????? ??? ?????????");
                    }
                    // mlevel_post_result_text.setText(""+PRESSURE_avg);
                    //  Sector_Level_post(Floor_name);
                } else {
                    mlevel_post_result_text.setText("???????????? ?????? ?????? ??? ?????????");
                }
            }
            if (v.getId() == R.id.Button1) {

                if (mid_send_success) {
                    //  if (mSector_Level_success) {
                    beforsend_bluetoothCheck();
                      /*  if (!mInformation_boolean) {
                            jupiterService.startFineLocationTrackingService(JupiterService.PDR_SERVICE, 0);
                            JupiterService_callback();
                            textView1.setText("?????? ???");
                            button1.setText("??????");
                            mInformation_boolean = true;
                            //   startTimerTask1();
                        } else {
                            textView1.setText("?????? ??????");
                            button1.setText("??????");
                            mInformation_boolean = false;
                            jupiterService.stopJupiterService();
                        }*/
                    //  } else {
                    //      textView1.setText("Level??? ?????? ????????? ?????????");
                    //  }
                } else {
                    textView1.setText("???????????? ?????? ?????? ??? ?????????");
                }
            }

        }
    };
    private View.OnLongClickListener mLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            if (v.getId() == R.id.IDname) {
                if (!mid_send_success) {
              //  if (!mInformation_boolean) {
                    final EditText editText = new EditText((MainActivity2.this));
                    editText.setGravity(Gravity.CENTER);
                    editText.setText(ID_name_string);
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity2.this);
                    alertDialog.setTitle("ID");
                    alertDialog.setView(editText);

                    alertDialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (editText.getText().length() != 0) {
                                SharedPreferences sharedPreferences = getSharedPreferences("Change_settings", MODE_PRIVATE); //text?????? key??? ????????? ?????? ????????? ??????. ???????????? ???????????? ????????? ""??? ??????
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("ID_name", editText.getText().toString());
                                editor.apply();
                                Runnable runnable12 = new Runnable() {
                                    @Override
                                    public void run() {
                                        ID_name_string = sharedPreferences.getString("ID_name", "inners_aos");
                                        ID_name_text.setText(ID_name_string);
                                    }
                                };
                                listcange_handler.postDelayed(runnable12, 2);
                            } else {
                                Toast.makeText(getApplicationContext(), "ID??? ????????? ?????????.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    alertDialog.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    alertDialog.show();
                }else{
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity2.this);
                    alertDialog.setMessage("??????????????? ?????? ?????? ??? ?????????");

                    alertDialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });

                    alertDialog.show();
                }
            }
            return false;
        }
    };

    private static SensorManager mSensorManager;
    private Sensor mBarometer; // ?????????

    private void senser_check() {
        mlevel_post_result_text.setText("");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mBarometer = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);//?????????
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null) {
            Barometer_have_boolean = false;
            Log.e("????????????", "763");
        }
        mSensorManager.registerListener(this, mBarometer, SensorManager.SENSOR_DELAY_UI);
    }
    private Handler mHandler;
    private ProgressDialog mProgressDialog;
    private void porsenser(){
        jupiterService.startFineLocationTrackingService(JupiterService.PDR_SERVICE, 0);
        textView1.setText("?????? ???");
        button1.setText("??????");
        mInformation_boolean = true;
        PRESSURE_add = new ArrayList<>();
        senser_check();
        if(Barometer_have_boolean) {
            mHandler = new Handler();

            runOnUiThread(new Runnable() {//????????? ???????????? ?????? ?????? ????????????
                @Override
                public void run() {
                    mProgressDialog = ProgressDialog.show(MainActivity2.this, "", "???????????? ??????????????????.\n???????????? : 10???", true);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                    Pressure_post();


                                    mProgressDialog.dismiss();
                                    mSensorManager.unregisterListener(MainActivity2.this);
                                    textView1.setText("?????? ??????");
                                    button1.setText("??????");
                                    mInformation_boolean = false;
                                    jupiterService.stopJupiterService();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 10000);
                }
            });
        }else{
            mlevel_post_result_text.setText("??????????????????");
        }
        //mSensorManager.unregisterListener(this);
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
            Log.e("??????dlTdma", String.valueOf(PRESSURE_add.size()));
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

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.e("testdCONNECTEDgg", action + "," + intent.getAction());

            if (action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                //log.e("off6", action + ", " + intent);
            }
        }
    };
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int BEFOR_SEND_BT = 3;
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
        //log.e("BLE1245", "130");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.logout).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // ignore
            return true;
        }else if (itemId == R.id.logout) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            //????????? ???????????? Editor??? ????????????.
            SharedPreferences.Editor edit = preferences.edit();
            edit.putString("startname", "");
            edit.apply();
            Intent newIntent = new Intent(MainActivity2.this, MainLoginActivity.class);
            startActivity(newIntent);
            finish();
            return true;
        }else if(itemId == R.id.exit){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("?????? ??????");
        builder.setMessage("????????? ?????????????????????????");
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                moveTaskToBack(true); // ???????????? ?????????????????? ??????
                finish(); // ???????????? ?????? + ????????? ??????????????? ?????????
                android.os.Process.killProcess(android.os.Process.myPid()); // ??? ???????????? ??????
            }
        });
        builder.setNegativeButton("??????", null);
        builder.show();
    }

}
