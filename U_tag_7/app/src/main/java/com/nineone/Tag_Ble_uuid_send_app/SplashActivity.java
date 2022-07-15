package com.nineone.Tag_Ble_uuid_send_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private final int My_ACCESS_FINE_LOCATION=2000;
    private String[] permissions9 = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    };
    private String[] permissions10 = {
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    };
    private String[] permissions11 = {
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
    };
    private String[] permissions;
    private boolean cheack_boolean = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ActionBar actionBar =getSupportActionBar();
        actionBar.hide();
        // GPSSetting();
        checkSDK();

        TextView textView = findViewById(R.id.textView);
        textView.setText("NineOne");

    }
    private void checkSDK() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            switch(android.os.Build.VERSION.SDK_INT) {
                case 31:
                case 30:
                    Log.e("asd30", String.valueOf(Build.VERSION.SDK_INT));
                    permissions=permissions11;
                    checkPermissions(permissions11);
                    break;
                case 29:
                    Log.e("asd29", String.valueOf(Build.VERSION.SDK_INT));
                    permissions=permissions10;
                    checkPermissions(permissions10);
                    break;
                default:
                    permissions=permissions9;
                    checkPermissions(permissions9);
                    break;
            }
        }
    }
    private boolean checkPermissions(String[] per) {
        int result;
        List<String> permissionList = new ArrayList<>();

        for (String pm : per) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
                Log.e("asd88",pm);
            }
            Log.e("asd89",pm);
        }
        if (!permissionList.isEmpty()) {
            Log.e("asd90","asd");
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        } else {
            cheack_boolean=true;
            Log.e("asd94","asd");
            //  GPSSetting();
            Log.e("asd96","asd");
        }
        return true;
    }
    private static final int MULTIPLE_PERMISSIONS = 101;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // grantResults[0] 거부 -> -1
        // grantResults[0] 허용 -> 0 (PackageManager.PERMISSION_GRANTED)
        if (requestCode == MULTIPLE_PERMISSIONS) {
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    Log.e("asd112","asd");
                    if (permissions[i].equals(this.permissions[i])) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            showToast_PermissionDeny();
                        }else{
                            cheack_boolean=true;
                            //Toast.makeText(this, "11", Toast.LENGTH_SHORT).show();

                        }
                    }else{

                        //GPSSetting();
                        //Toast.makeText(this, "22", Toast.LENGTH_SHORT).show();
                        Log.e("asd120","asd");
                    }
                }
                Log.e("asd123","asd");
            } else {
                Log.e("asd124","asd");
                showToast_PermissionDeny();

            }
            Log.e("asd125","asd");
        }
        Log.e("asd126","asd");

    }
    private void showToast_PermissionDeny() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onPause() {
        Log.e("asd136","asd");
        super.onPause();
        // finish();
    }
    @Override
    protected void onResume() {
        Log.e("asd143","asd");
        super.onResume();
        if(cheack_boolean){
            GPSSetting();
        }
        // checkSDK();
    }
    private LocationManager locationManager;
    private void GPSSetting() {
        //  ContentResolver res = getContentResolver();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
            Log.e("asd147","asd");
            builder.setTitle("GPS 설정");
            builder.setMessage("GPS를 사용하시겠습니까?");
            builder.setPositiveButton("사용", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e("asd153","asd");
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("거절", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.show();
        } else {

            Log.e("asd167","asd");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new Thread() {
                        @Override
                        public void run() {
                            Intent newIntent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(newIntent);
                            finish();

                        }
                    }.start();
                }
            }, 200);
        }

    }

}