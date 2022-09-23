package com.nineone.inner_s_user;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import tjlabs.android.jupiter_android_v2.JupiterService;

public class test extends AppCompatActivity {
    private JupiterService jupiterService;
    private Handler mHandler;
    private ProgressDialog mProgressDialog;
    private boolean mwileboolean = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        try {
            Runtime.getRuntime().exec(new String[]{"logcat", "-c"});
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        jupiterService = new JupiterService();

        jupiterService.setJupiterService(getApplication(), "inners_aos");
        jupiterService.sendUserInfo();
        Log.e("stoppp","stoppp");
        mHandler = new Handler();
        runOnUiThread(new Runnable() {//약간의 딜레이를 준후 차트 불러오기
            @Override public void run() {
                mProgressDialog = ProgressDialog.show(test.this,"", "로그인 중.",true);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int a= 0;
                        try {
                            while(!mwileboolean && a<=100) {
                                Process logcat;
                                final StringBuilder log = new StringBuilder();
                                String textline = null;
                                try {
                                    logcat = Runtime.getRuntime().exec(new String[]{"logcat", "-d"});
                                    BufferedReader br = new BufferedReader(new InputStreamReader(logcat.getInputStream()),4*1024);
                                    String line;

                                    String separator = System.getProperty("line.separator");
                                    while ((line = br.readLine()) != null) {
                                        CharSequence cs = "retrofit:";
                                        if (line.contains(cs)) {
                                            //log.append(line.substring(line.indexOf("retrofit:"),line.length()));
                                            log.append(line);
                                            log.append("\n");
                                            textline=line;
                                        }
                                    }
                                    TextView tv = (TextView)findViewById(R.id.textView1);
                                    tv.setText(textline);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                        } catch ( Exception e ) {
                            e.printStackTrace();
                        }
                    }
                }, 1000);
            }
        } );
    /*    Process logcat;

        final StringBuilder log = new StringBuilder();
        String textline = null;
        try {
            logcat = Runtime.getRuntime().exec(new String[]{"logcat", "-d"});
            BufferedReader br = new BufferedReader(new InputStreamReader(logcat.getInputStream()),4*1024);
            String line;

            String separator = System.getProperty("line.separator");
            while ((line = br.readLine()) != null) {
                CharSequence cs = "retrofit:";
                if (line.contains(cs)) {
                    //log.append(line.substring(line.indexOf("retrofit:"),line.length()));
                    log.append(line);
                    log.append("\n");
                    textline=line;
                }
            }
            TextView tv = (TextView)findViewById(R.id.textView1);
            tv.setText(textline);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }

}