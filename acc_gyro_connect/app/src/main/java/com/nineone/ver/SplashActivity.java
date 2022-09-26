package com.nineone.ver;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.nineone.zntil.ShoolnameList;
import com.nineone.zntil.ShoolnameViewModel;
import com.nineone.zntil.user_aSchool_dialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class SplashActivity extends AppCompatActivity {//어플에서 제일 처음 띄워지는 Splash화면 각종 권한을 확인합니다.
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private final int MY_PERMISSIONS_REQUEST_CAMERA=1001;
    private final int My_ACCESS_FINE_LOCATION=2000;
    TextView id;
    String line = null;
    InputStream in = null;
    BufferedReader reader = null;
    HttpsURLConnection httpsConn = null;
    private boolean saveroom=false;
    private static final String urlString  = "http://lora.nineone.com/appver_check.asp?app_name=ver";
    int versionCode = BuildConfig.VERSION_CODE;
    String versionName = BuildConfig.VERSION_NAME;
    int New_version;
    @Override
    protected void onCreate(Bundle savedInstanceStare) {
        super.onCreate(savedInstanceStare);
        setContentView(R.layout.activity_splash);
        ActionBar actionBar =getSupportActionBar();
        actionBar.hide();
      // GPSSetting();
     //   permissionCheck();
        if(!saveroom) {
            room();
        }
        TedPermission.with(this)
                .setPermissionListener(permission)
                // .setRationaleMessage("녹화를 위하여 권한을 허용해주세요.")
                .setDeniedMessage("권한이 거부되었습니다. 설정 > 권한에서 허용해주세요.")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
        TextView textView = findViewById(R.id.textView);
        textView.setText("NineOne");

    }
    @Override
    protected void onPause() {
        super.onPause();
       // finish();
    }
    public ShoolnameViewModel mWordViewModel;


    public void room(){
       /* Shoolname.add(0,"경동초");
        Shoolname.add(1,"침산초");
        Shoolname.add(2,"팔공초");
        Shoolname.add(3,"용계초");*/
      /*  mWordViewModel = ViewModelProviders.of(this).get(ShoolnameViewModel.class);

        for(int i=0;i<Shoolname.size();i++) {
            ShoolnameList userMyDataList=new ShoolnameList();
            userMyDataList.setShoolName(Shoolname.get(i).toString());
            mWordViewModel.insert(userMyDataList);
        }
        SharedPreferences sf =  getPreferences(getApplication());
        SharedPreferences.Editor editor = sf.edit();//저장하려면 editor가 필요
        editor.putBoolean("name", true); // 입력
        editor.commit();
        editor.apply();*/
    }
   /* @Override
    protected void onCreate(Bundle savedInstanceStare) {
        super.onCreate(savedInstanceStare);
        setContentView(R.layout.activity_splash);
        GPSSetting();
        permissionCheck();
        TedPermission.with(this)
                .setPermissionListener(permission)
                // .setRationaleMessage("녹화를 위하여 권한을 허용해주세요.")
                .setDeniedMessage("권한이 거부되었습니다. 설정 > 권한에서 허용해주세요.")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .check();
        TextView textView = findViewById(R.id.textView);
        textView.setText("로딩중");
        SplashThread introThread = new SplashThread(handler);
        introThread.start();
    }
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }
    };
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    */
    private void GPSSetting() {
        ContentResolver res = getContentResolver();

        boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(res, LocationManager.GPS_PROVIDER);
        if(!gpsEnabled) {
            new AlertDialog.Builder(SplashActivity.this)
                    .setTitle("GPS 설정")
                    .setMessage("GPS를 사용하시겠습니까?")
                    .setPositiveButton("사용", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);

                        }
                    })
                    .setNegativeButton("거절", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        }
    }
    private void permissionCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, My_ACCESS_FINE_LOCATION);

        }
    }
    PermissionListener permission = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                URL url = new URL(urlString);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();//URL을 열어서 연결
                                connection.setRequestMethod("GET"); //전송방식(GET=가져오기),(POST=전송)
                                connection.setDoOutput(true);       //데이터를 쓸 지 설정
                                connection.setDoInput(true);        //데이터를 읽어올지 설정

                                InputStream is = connection.getInputStream();//연결된 데이터의 InputStream(입력스트림)을 가져오기
                                StringBuilder sb = new StringBuilder();
                                //연결 요청 확인
                                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {//null이었을 때
                                    Intent newIntent = new Intent(getApplicationContext(), MainActivity.class);
                                    // newIntent.putExtra("update", a);
                                    startActivity(newIntent);
                                    finish();
                                }
                                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));//요청한 URL출력물을 BufferedReader로 받기
                                String result;
                                while ((result = br.readLine()) != null) {
                                    sb.append(result);//result 문자열을 StringBuilder로 append하기
                                }
                                result = sb.toString();
                                if (result != null && result.matches("^[0-9]*$")) {
                                    New_version = Integer.parseInt(result);
                                    if (New_version > versionCode) {
                                        Intent newIntent = new Intent(getApplicationContext(), Update.class);
                                        newIntent.putExtra("update", New_version);
                                        startActivity(newIntent);
                                        finish();
                                    } else {
                                        Intent newIntent = new Intent(getApplicationContext(), MainActivity.class);
                                        // newIntent.putExtra("update", a);
                                        startActivity(newIntent);
                                        finish();
                                    }
                                } else {
                                    Intent newIntent = new Intent(getApplicationContext(), MainActivity.class);
                                    // newIntent.putExtra("update", a);
                                    startActivity(newIntent);
                                    finish();
                                }
                            }catch (IOException e) {
                                e.printStackTrace();
                                Intent newIntent = new Intent(getApplicationContext(), MainActivity.class);
                                // newIntent.putExtra("update", a);
                                startActivity(newIntent);
                                finish();
                            }
                            //Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            //startActivity(intent);
                            //finish();
                        }
                    }.start();
                }
            },200);
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            Toast.makeText(SplashActivity.this, "권한 거부", Toast.LENGTH_SHORT).show();
            finish();
        }


    };
}