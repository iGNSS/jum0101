package com.nineone.inner_s_user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telecom.TelecomManager;
import android.util.Log;
import android.util.LogPrinter;
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
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import tjlabs.android.jupiter_android_v2.JupiterCallBackManager;
import tjlabs.android.jupiter_android_v2.JupiterService;
import tjlabs.android.jupiter_android_v2.data.BuildingDetectionOutput;
import tjlabs.android.jupiter_android_v2.data.CoarseLevelDetectionOutput;
import tjlabs.android.jupiter_android_v2.data.FineLevelDetectionOutput;
import tjlabs.android.jupiter_android_v2.data.FineLocationTrackingOutput;
import tjlabs.android.jupiter_android_v2.data.SectorDetectionOutput;

public class MainActivity extends AppCompatActivity {
    private TextView mmessge;
    private TextView textView1, textView2, textView3, mmiiliscUV;
    private TextView ID_name_text,mlogin_fail;
    private Button button1,SendButton;
    private String ID_name_string;
    private JupiterService jupiterService;
    private boolean mInformation_boolean = false;
    private boolean mid_send_success = false;
    private String NAME_url_string, NAME_port_string, NAME_path_string;
    private TextView NAME_url_text, NAME_port_text, NAME_path_text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ID_name_text = findViewById(R.id.IDname);
        mlogin_fail = findViewById(R.id.login_fail);
        mlogin_fail.setText("ID를 전송해 주세요");
        NAME_url_text = findViewById(R.id.UVadress);
        NAME_port_text = findViewById(R.id.UVport);
        NAME_path_text = findViewById(R.id.UVpath);
        SendButton = findViewById(R.id.SendButton);
        //textView2 = findViewById(R.id.UVcount);
      //  mmiiliscUV = findViewById(R.id.miiliscUV);
        mmessge = findViewById(R.id.mMessge);
        button1 = findViewById(R.id.Button1);
        textView1 = findViewById(R.id.TextView1);
       /* jupiterService = new JupiterService();
        jupiterService.setJupiterService(getApplication(), "inners_aos");
        jupiterService.sendUserInfo();*/

        SharedPreferences sf = getSharedPreferences("Change_settings", MODE_PRIVATE); //text라는 key에 저장된 값이 있는지 확인. 아무값도 들어있지 않으면 ""를 반환
        NAME_url_string = sf.getString("UV_url", "stag.nineone.com");
        NAME_port_string = sf.getString("UV_port", "9988");
        NAME_path_string = sf.getString("UV_path", "user");
        NAME_url_text.setText(NAME_url_string);
        NAME_port_text.setText(NAME_port_string);
        NAME_path_text.setText(NAME_path_string);

        ID_name_string = sf.getString("ID_name", "inners_aos");
        ID_name_text.setText(ID_name_string);
        ID_name_text.setOnLongClickListener(mClickListener);
        NAME_url_text.setOnLongClickListener(mClickListener);
        NAME_port_text.setOnLongClickListener(mClickListener);
        NAME_path_text.setOnLongClickListener(mClickListener);
     //   ID_name_in_Http_post();


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mid_send_success) {
                    if (!mInformation_boolean) {

                        jupiterService.startFineLocationTrackingService(JupiterService.PDR_SERVICE, 0);
                        JupiterService_callback();
                        textView1.setText("전송 중");
                        button1.setText("멈춤");

                        mInformation_boolean = true;
                        //   startTimerTask1();
                    } else {
                        textView1.setText("전송 멈춤");
                        button1.setText("시작");

                        mInformation_boolean = false;
                        jupiterService.stopJupiterService();

                        //  stopTimerTask();
                    }
                }else{
                    textView1.setText("ID를 먼저 로그인해 주세요");
                }


            }
        });
        SendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ID_name_in_Http_post();
            }
        });
    }
    private String url;
    private void ID_name_in_Http_post() {
        new Thread(() -> {
            try {
                Log.e("dd-", "164");
                String url = "http://" + NAME_url_string + ":" + NAME_port_string + "/" + NAME_path_string;

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
                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                        alertDialog.setTitle("ID 전송 성공");
                                        alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                mlogin_fail.setText("ID 전송 성공");
                                                jupiterService = new JupiterService();
                                                jupiterService.setJupiterService(getApplication(), "inners_aos");
                                                jupiterService.sendUserInfo();
                                                mid_send_success= true;
                                            }
                                        });
                                        alertDialog.show();
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
    private void failmessage(String make){
        Runnable runnableRF4 = new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("ID 전송 실패");
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
                mmessge.setText(fineLocationTrackingOutput.toString());
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
       jupiterService.stopJupiterService();

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
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
        }
    }
    private View.OnLongClickListener mClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            if (v.getId() == R.id.IDname) {

                final EditText editText = new EditText((MainActivity.this));
                editText.setGravity(Gravity.CENTER);
                editText.setText(ID_name_string);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
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
            if (v.getId() == R.id.UVadress) {
                final EditText editText = new EditText((MainActivity.this));
                editText.setGravity(Gravity.CENTER);
                editText.setText(NAME_url_string);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("URL");
                alertDialog.setView(editText);
                alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (editText.getText().length() != 0) {
                            SharedPreferences sharedPreferences = getSharedPreferences("Change_settings", MODE_PRIVATE); //text라는 key에 저장된 값이 있는지 확인. 아무값도 들어있지 않으면 ""를 반환
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("UV_url", editText.getText().toString());
                            editor.apply();
                            Runnable runnable12 = new Runnable() {
                                @Override
                                public void run() {
                                    NAME_url_string = sharedPreferences.getString("UV_url", "stag.nineone.com");
                                    NAME_url_text.setText(NAME_url_string);
                                }
                            };
                            listcange_handler.postDelayed(runnable12, 2);
                        } else {
                            Toast.makeText(getApplicationContext(), "URL를 입력해 주세요.", Toast.LENGTH_SHORT).show();
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
            if (v.getId() == R.id.UVport) {
                final EditText editText = new EditText((MainActivity.this));
                editText.setGravity(Gravity.CENTER);
                editText.setText(NAME_port_string);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Port");
                alertDialog.setView(editText);
                alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (editText.getText().length() != 0) {
                            SharedPreferences sharedPreferences = getSharedPreferences("Change_settings", MODE_PRIVATE); //text라는 key에 저장된 값이 있는지 확인. 아무값도 들어있지 않으면 ""를 반환
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("UV_port", editText.getText().toString());
                            editor.apply();
                            Runnable runnable12 = new Runnable() {
                                @Override
                                public void run() {
                                    NAME_port_string = sharedPreferences.getString("UV_port", "9988");
                                    NAME_port_text.setText(NAME_port_string);
                                }
                            };
                            listcange_handler.postDelayed(runnable12, 2);
                        } else {
                            Toast.makeText(getApplicationContext(), "PORT를 입력해 주세요.", Toast.LENGTH_SHORT).show();
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
            if (v.getId() == R.id.UVpath) {
                final EditText editText = new EditText((MainActivity.this));
                editText.setGravity(Gravity.CENTER);
                editText.setText(NAME_path_string);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("UV Path");
                alertDialog.setView(editText);
                alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (editText.getText().length() != 0) {
                            SharedPreferences sharedPreferences = getSharedPreferences("Change_settings", MODE_PRIVATE); //text라는 key에 저장된 값이 있는지 확인. 아무값도 들어있지 않으면 ""를 반환
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("UV_path", editText.getText().toString());
                            editor.apply();
                            Runnable runnable12 = new Runnable() {
                                @Override
                                public void run() {
                                    NAME_path_string = sharedPreferences.getString("UV_path", "api/mobile/recordUV");
                                    NAME_path_text.setText(NAME_path_string);
                                }
                            };
                            listcange_handler.postDelayed(runnable12, 2);
                        } else {
                            Toast.makeText(getApplicationContext(), "PATH를 입력해 주세요.", Toast.LENGTH_SHORT).show();
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
        menu.findItem(R.id.logout).setVisible(true);
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
            Intent newIntent = new Intent(MainActivity.this, MainLoginActivity.class);
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