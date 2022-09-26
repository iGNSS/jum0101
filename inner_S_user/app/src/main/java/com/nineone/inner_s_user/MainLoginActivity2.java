package com.nineone.inner_s_user;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import java.util.regex.Pattern;

public class MainLoginActivity2 extends AppCompatActivity {
    private EditText editText;
    private TextView textView;
    private Button loginbutton_ok_no_text;
    private String url;
    String mstart_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mstart_name = sp.getString("ID_name", "inners_aos");
        boolean check_id = sp.getBoolean("ID_save", false);

        editText = findViewById(R.id.LoginEdit);
        editText.setText(mstart_name);
        editText.setSelected(false);

        loginbutton_ok_no_text = findViewById(R.id.LoginButton_ok_no_text);
        loginbutton_ok_no_text.setEnabled(false);
        textView = findViewById(R.id.LoginEdit_ok_no_text);
        if(editText.getText().toString().trim().length()<=1) {
            textView.setTextColor(Color.parseColor("#FF0000"));
            textView.setText("최소 2글자 이상 입력 바랍니다.");
            editText.setSelected(false);
            loginbutton_ok_no_text.getBackground().setTint(ContextCompat.getColor(getApplicationContext(), R.color.gray));
            loginbutton_ok_no_text.setEnabled(false);
        } else {

            textView.setTextColor(Color.parseColor("#FFFFFF"));
            textView.setText("");
            editText.setSelected(true);
            loginbutton_ok_no_text.getBackground().setTint(ContextCompat.getColor(getApplicationContext(), R.color.black));
            loginbutton_ok_no_text.setEnabled(true);
        }
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(editText.getText().toString().trim().length()<=1) {
                    textView.setTextColor(Color.parseColor("#FF0000"));
                    textView.setText("최소 2글자 이상 입력 바랍니다.");
                    editText.setSelected(false);
                    loginbutton_ok_no_text.getBackground().setTint(ContextCompat.getColor(getApplicationContext(), R.color.gray));
                    loginbutton_ok_no_text.setEnabled(false);
                } else {

                    textView.setTextColor(Color.parseColor("#FFFFFF"));
                    textView.setText("");
                    editText.setSelected(true);
                    loginbutton_ok_no_text.getBackground().setTint(ContextCompat.getColor(getApplicationContext(), R.color.black));
                    loginbutton_ok_no_text.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        loginbutton_ok_no_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ID_name_in_Http_post();
             /*   SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                //저장을 하기위해 Editor를 불러온다.
                SharedPreferences.Editor edit = preferences.edit();
                edit.putString("ID_name", editText.getText().toString());
                edit.apply();
                Intent newIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(newIntent);
                finish();*/
            }
        });
    }
    private void ID_name_in_Http_post() {
        new Thread(() -> {
            try {
                Log.e("dd-", "164");
                url = "http://stag.nineone.com:9988/user";
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
                    cred.put("user_id", mstart_name);
                    //cred.put("ble", SEND_HASHMAP)
                    cred.put("device_model", Build.MODEL);
                    Log.e("SystemB", Build.MODEL);
                    cred.put("os_version", Build.VERSION.SDK_INT);
                    // Log.e("aabradom_B", String.valueOf((b+i)));
                    // Log.e("dd-187u", "187");
                } catch (JSONException e) {
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
                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainLoginActivity2.this);
                                        alertDialog.setTitle("ID 전송 성공");
                                        //alertDialog.setMessage(make);
                                        alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                                //저장을 하기위해 Editor를 불러온다.
                                                SharedPreferences.Editor edit = preferences.edit();
                                                edit.putString("ID_name", editText.getText().toString());
                                                edit.apply();
                                                Intent newIntent = new Intent(getApplicationContext(), MainActivity.class);
                                                startActivity(newIntent);
                                                finish();
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
                                        failmessage(make);
                                    }
                                };
                                namechange_handler.postDelayed(runnableRF4, 0);
                            }
                            Log.e("dd-return_result2", success + "," + errors+","+message);

                        } catch (JSONException e) {
                            failmessage(e.getMessage());
                            Log.e("dd-211u", "\n" + e.getMessage());
                            // Handle error
                        }



                    }
                    br.close();

                } else {
                    HttpURLConnection finalCon = con;
                    failmessage(HttpResult+" "+con.getResponseMessage());
                    Log.e("dd-212u", HttpResult+con.getResponseMessage());
                }
            } catch (IOException e) {
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
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainLoginActivity2.this);
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
    private final MyHandler namechange_handler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<MainLoginActivity2> mActivity;

        public MyHandler(MainLoginActivity2 activity) {
            mActivity = new WeakReference<MainLoginActivity2>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainLoginActivity2 activity = mActivity.get();
        }
    }

    protected InputFilter filterId = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[a-zA-Z0-9]+$");

            if (!ps.matcher(source).matches()) {
                return source.toString().replaceAll("[^a-zA-Z\\s]", " ").trim();
            }
            return null;
        }
    };
}
