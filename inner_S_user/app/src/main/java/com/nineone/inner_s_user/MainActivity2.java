package com.nineone.inner_s_user;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import java.util.Timer;
import java.util.TimerTask;

import tjlabs.android.jupiter_android_v2.JupiterCallBackManager;
import tjlabs.android.jupiter_android_v2.JupiterService;
import tjlabs.android.jupiter_android_v2.data.FineLocationTrackingOutput;

public class MainActivity2 extends AppCompatActivity {
    private TextView mmessge;
    private TextView textView1, textView2, textView3, mmiiliscUV;
    private TextView ID_name_text,mlogin_fail;
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
        ID_name_text = findViewById(R.id.IDname);
        mlogin_fail = findViewById(R.id.login_fail);
        //mlogin_fail.setText("로그인을 먼저 해 주세요");
        mlevel_post_result_text = findViewById(R.id.Level_post_result_text);
       // mlevel_post_result_text.setText("층을 전송해 주세요");
        mlevel_post_result_button = findViewById(R.id.Level_post_result_button);


        SendButton = findViewById(R.id.SendButton);

        mmessge = findViewById(R.id.mMessge);
        button1 = findViewById(R.id.Button1);
        textView1 = findViewById(R.id.TextView1);

        SharedPreferences sf = getSharedPreferences("Change_settings", MODE_PRIVATE); //text라는 key에 저장된 값이 있는지 확인. 아무값도 들어있지 않으면 ""를 반환

        ID_name_string = sf.getString("ID_name", "inners_aos");
        ID_name_text.setText(ID_name_string);

        ID_name_text.setOnLongClickListener(mLongClickListener);


        sector_spinner = findViewById(R.id.Sector_spinner);
        floor_spinner = findViewById(R.id.Floor_Spinner);

      //  ArrayAdapter<CharSequence> ward_coutAdapter = ArrayAdapter.createFromResource(this, R.array.Table, android.R.layout.simple_spinner_dropdown_item);
        //R.array.test는 저희가 정의해놓은 1월~12월 / android.R.layout.simple_spinner_dropdown_item은 기본으로 제공해주는 형식입니다.
        ArrayAdapter<CharSequence> ward_coutAdapter = ArrayAdapter.createFromResource(this, R.array.Table, R.layout.spinner_item);

       // ward_coutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sector_spinner.setAdapter(ward_coutAdapter); //어댑터에 연결해줍니다.
        sector_spinner.setSelection(0);
        sector_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Sector_count=position;
                Sector_name=(String) sector_spinner.getItemAtPosition(position);
                Floor_chenck(Sector_count);
            } //이 오버라이드 메소드에서 position은 몇번째 값이 클릭됬는지 알 수 있습니다.
            //getItemAtPosition(position)를 통해서 해당 값을 받아올수있습니다.
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        SendButton.setOnClickListener(mClickListener);
        mlevel_post_result_button.setOnClickListener(mClickListener);
        button1.setOnClickListener(mClickListener);

    }
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int BEFOR_SEND_BT = 3;
    private void bluetoothCheck() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        // mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 지원하지 않는다면 어플을 종료시킨다.
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "이 기기는 블루투스 기능을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) { // 블루투스 활성화를 취소를 클릭하였다면
                //       mblecheck=false;

            } else {
                bluetoothCheck();
                Toast.makeText(getApplicationContext(), "블루투스를 활성화 하여 주세요 ", Toast.LENGTH_SHORT).show();
                //  finish();
            }
        } if (requestCode == BEFOR_SEND_BT) {
            if (resultCode == Activity.RESULT_OK) { // 블루투스 활성화를 취소를 클릭하였다면
                //       mblecheck=false;

            } else {
                bluetoothCheck();
                Toast.makeText(getApplicationContext(), "블루투스를 활성화 하여 주세요 ", Toast.LENGTH_SHORT).show();
                //  finish();
            }
        }  else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    private String Floor_name=null;
    private int Floor_count=0;
    private void Floor_chenck(int count){
        if(count == 0) {
            ArrayAdapter<CharSequence> ward_coutAdapter = ArrayAdapter.createFromResource(this, R.array.KIER_cout, R.layout.spinner_item);
            //R.array.test는 저희가 정의해놓은 1월~12월 / android.R.layout.simple_spinner_dropdown_item은 기본으로 제공해주는 형식입니다.

          //  ward_coutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            floor_spinner.setAdapter(ward_coutAdapter); //어댑터에 연결해줍니다.
            floor_spinner.setSelection(0);
            floor_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Floor_count = position;
                    Floor_name = (String) floor_spinner.getItemAtPosition(position);

                } //이 오버라이드 메소드에서 position은 몇번째 값이 클릭됬는지 알 수 있습니다.

                //getItemAtPosition(position)를 통해서 해당 값을 받아올수있습니다.
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }else if(count == 1) {
            ArrayAdapter<CharSequence> ward_coutAdapter = ArrayAdapter.createFromResource(this, R.array.KEPCO_cout, R.layout.spinner_item);
            //R.array.test는 저희가 정의해놓은 1월~12월 / android.R.layout.simple_spinner_dropdown_item은 기본으로 제공해주는 형식입니다.

         //   ward_coutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            floor_spinner.setAdapter(ward_coutAdapter); //어댑터에 연결해줍니다.
            floor_spinner.setSelection(0);
            floor_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Floor_count = position;
                    Floor_name = (String) floor_spinner.getItemAtPosition(position);

                } //이 오버라이드 메소드에서 position은 몇번째 값이 클릭됬는지 알 수 있습니다.

                //getItemAtPosition(position)를 통해서 해당 값을 받아올수있습니다.
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }else if(count == 2) {
            ArrayAdapter<CharSequence> ward_coutAdapter = ArrayAdapter.createFromResource(this, R.array.KIST_cout, R.layout.spinner_item);
            //R.array.test는 저희가 정의해놓은 1월~12월 / android.R.layout.simple_spinner_dropdown_item은 기본으로 제공해주는 형식입니다.

          //  ward_coutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            floor_spinner.setAdapter(ward_coutAdapter); //어댑터에 연결해줍니다.
            floor_spinner.setSelection(0);
            floor_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Floor_count = position;
                    Floor_name = (String) floor_spinner.getItemAtPosition(position);

                } //이 오버라이드 메소드에서 position은 몇번째 값이 클릭됬는지 알 수 있습니다.

                //getItemAtPosition(position)를 통해서 해당 값을 받아올수있습니다.
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }else if(count == 3) {
            ArrayAdapter<CharSequence> ward_coutAdapter = ArrayAdapter.createFromResource(this, R.array.tjOffice_cout, R.layout.spinner_item);
            //R.array.test는 저희가 정의해놓은 1월~12월 / android.R.layout.simple_spinner_dropdown_item은 기본으로 제공해주는 형식입니다.

         //   ward_coutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            floor_spinner.setAdapter(ward_coutAdapter); //어댑터에 연결해줍니다.
            floor_spinner.setSelection(0);
            floor_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Floor_count = position;
                    Floor_name = (String) floor_spinner.getItemAtPosition(position);

                } //이 오버라이드 메소드에서 position은 몇번째 값이 클릭됬는지 알 수 있습니다.

                //getItemAtPosition(position)를 통해서 해당 값을 받아올수있습니다.
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
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
                    mlogin_fail.setText("ID 전송 실패");
                    mid_send_success= false;
                    failmessage(e.getMessage());
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
                                        alertDialog.setTitle("ID 전송 성공");
                                        alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {*/
                                        String id_sussess_string = "ID 전송 성공 - " + ID_name_string;
                                        mlogin_fail.setText(id_sussess_string);
                                        mlevel_post_result_text.setText("Level을 전송해 주세요");
                                        mSector_Level_success = false;
                                        //textView1.setText("Level을 먼저 전송해 주세요");
                                        jupiterService = new JupiterService();
                                        jupiterService.setJupiterService(getApplication(), ID_name_string);
                                        jupiterService.sendUserInfo();
                                        mid_send_success = true;
                                        SendButton.setText("로그아웃");
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
                                        mlogin_fail.setText("ID 전송 실패");
                                        mid_send_success= false;
                                        failmessage(make);
                                    }
                                };
                                namechange_handler.postDelayed(runnableRF4, 0);
                            }
                            Log.e("dd-return_result2", success + "," + errors+","+message);

                        } catch (JSONException e) {
                            mlogin_fail.setText("ID 전송 실패");
                            failmessage(e.getMessage());
                            mid_send_success= false;
                            Log.e("dd-211u", "\n" + e.getMessage());
                            // Handle error
                        }
                    }
                    br.close();

                } else {
                    HttpURLConnection finalCon = con;
                    mlogin_fail.setText("ID 전송 실패");
                    mid_send_success= false;
                    failmessage(HttpResult+" "+con.getResponseMessage());
                    Log.e("dd-212u", HttpResult+con.getResponseMessage());
                }
            } catch (IOException e) {
                mlogin_fail.setText("ID 전송 실패");
                mid_send_success= false;
                failmessage(e.getMessage());
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
                    mlevel_post_result_text.setText("전송 실패");
                    mSector_Level_success= false;
                    failmessage(e.getMessage());
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
                                      //  textView1.setText("전송 중지");
                                        mSector_Level_success= true;

                                    }
                                };
                                namechange_handler.postDelayed(runnableRF4, 0);
                            }else{
                                make = errors+", "+message;
                                Runnable runnableRF4 = new Runnable() {
                                    @Override
                                    public void run() {
                                        mlevel_post_result_text.setText("전송 실패");
                                        mSector_Level_success = false;
                                        failmessage(make);
                                    }
                                };
                                namechange_handler.postDelayed(runnableRF4, 0);
                            }
                            Log.e("dd-return_result2", success + "," + errors+","+message);

                        } catch (JSONException e) {
                            mlevel_post_result_text.setText("전송 실패");
                            failmessage(e.getMessage());
                            mSector_Level_success = false;
                            Log.e("dd-211u", "\n" + e.getMessage());
                            // Handle error
                        }
                    }
                    br.close();

                } else {
                    HttpURLConnection finalCon = con;
                    mlevel_post_result_text.setText("전송 실패");
                    mSector_Level_success = false;
                    failmessage(HttpResult+" "+con.getResponseMessage());
                    Log.e("dd-212u", HttpResult+con.getResponseMessage());
                }
            } catch (IOException e) {
                mlevel_post_result_text.setText("전송 실패");
                mSector_Level_success = false;
                failmessage(e.getMessage());
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
                alertDialog.setTitle("전송 실패");
                alertDialog.setMessage(make);
                alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alertDialog.show();
            }
        };
        namechange_handler.postDelayed(runnableRF4, 0);
    }



    private TimerTask timerTask;
    private Timer timer = new Timer();
    private void startTimerTask1() {
        timerTask = new TimerTask() {
            @Override
            public void run() { // 코드 작성


                JupiterService_callback();

            }
        };
        timer.schedule(timerTask, 2000, 2000);
    }
    private void stopTimerTask() {//타이머 스톱 함수
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    private void JupiterService_callback(){
        jupiterService.requestFineLocationTrackingUpdate(new JupiterCallBackManager.FineLocationTrackingCallBack() {
            @Override
            public void onResponse(@NonNull FineLocationTrackingOutput fineLocationTrackingOutput) {
                Log.e("sectorDetectionOutput name", fineLocationTrackingOutput.toString());
                //mmessge.setText(fineLocationTrackingOutput.toString());
            }
        });


    }
    @Override
    protected void onStart() {
        super.onStart();
        // Log.e("connect_TAG", "onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        //  Log.e("connect_TAG", "onResume");
        //  startScan();
    }

    @Override
    protected void onPause() {
        //Log.e("connect_TAG", "onPause");
        super.onPause();
        stopTimerTask();
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
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.SendButton) {
                if (!mid_send_success) {

                    ID_name_in_Http_post();
                } else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity2.this);
                    alertDialog.setTitle("로그아웃");
                    alertDialog.setMessage("로그아웃 하시겠습니까?");
                    alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SendButton.setText("로그인");
                            mlogin_fail.setText("로그아웃");
                            SendButton.getBackground().setTint(ContextCompat.getColor(getApplicationContext(), R.color.sky));
                            mSector_Level_success = false;
                            mid_send_success = false;
                            mlevel_post_result_text.setText("");
                            Log.e("notlogin", "asd");
                            if (mInformation_boolean) {
                                textView1.setText("전송 중지");
                                button1.setText("시작");
                                mInformation_boolean = false;
                                //      jupiterService.stopJupiterService();
                            }
                            jupiterService.stopJupiterService();


                        }
                    });
                    alertDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    alertDialog.show();

                }

            }

            if (v.getId() == R.id.Level_post_result_button) {
                if (mid_send_success) {
                    Sector_Level_post(Floor_name);
                } else {
                    mlevel_post_result_text.setText("로그인을 먼저 진행 해 주세요");
                }
            }
            if (v.getId() == R.id.Button1) {

                if (mid_send_success) {
                    if (mSector_Level_success) {
                        beforsend_bluetoothCheck();
                      /*  if (!mInformation_boolean) {

                            jupiterService.startFineLocationTrackingService(JupiterService.PDR_SERVICE, 0);
                            JupiterService_callback();
                            textView1.setText("전송 중");
                            button1.setText("중지");
                            mInformation_boolean = true;
                            //   startTimerTask1();
                        } else {
                            textView1.setText("전송 중지");
                            button1.setText("시작");
                            mInformation_boolean = false;
                            jupiterService.stopJupiterService();
                        }*/
                    } else {
                        textView1.setText("Level을 먼저 전송해 주세요");
                    }
                } else {
                    textView1.setText("로그인을 먼저 진행 해 주세요");
                }
            }

        }
    };
    private void beforsend_bluetoothCheck() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        // mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 지원하지 않는다면 어플을 종료시킨다.
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "이 기기는 블루투스 기능을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            //log.e("BLE1245", "124");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BEFOR_SEND_BT);
        }else{
            if (!mInformation_boolean) {

                jupiterService.startFineLocationTrackingService(JupiterService.PDR_SERVICE, 0);
                JupiterService_callback();
                textView1.setText("전송 중");
                button1.setText("중지");
                mInformation_boolean = true;
                //   startTimerTask1();
            } else {
                textView1.setText("전송 중지");
                button1.setText("시작");
                mInformation_boolean = false;
                jupiterService.stopJupiterService();
            }
        }
        //log.e("BLE1245", "130");
    }
    private View.OnLongClickListener mLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            if (v.getId() == R.id.IDname) {
                if (!mInformation_boolean) {
                    final EditText editText = new EditText((MainActivity2.this));
                    editText.setGravity(Gravity.CENTER);
                    editText.setText(ID_name_string);
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity2.this);
                    alertDialog.setTitle("ID");
                    alertDialog.setView(editText);

                    alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (editText.getText().length() != 0) {
                                SharedPreferences sharedPreferences = getSharedPreferences("Change_settings", MODE_PRIVATE); //text라는 key에 저장된 값이 있는지 확인. 아무값도 들어있지 않으면 ""를 반환
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
                                Toast.makeText(getApplicationContext(), "ID를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    alertDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
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
            //저장을 하기위해 Editor를 불러온다.
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
        builder.setTitle("종료 확인");
        builder.setMessage("정말로 종료하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                moveTaskToBack(true); // 태스크를 백그라운드로 이동
                finish(); // 액티비티 종료 + 태스크 리스트에서 지우기
                android.os.Process.killProcess(android.os.Process.myPid()); // 앱 프로세스 종료
            }
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }

}
