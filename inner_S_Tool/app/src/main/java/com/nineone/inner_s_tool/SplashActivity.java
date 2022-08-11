package com.nineone.inner_s_tool;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {//어플에서 제일 처음 띄워지는 Splash화면 각종 권한을 확인합니다.

    private final int My_ACCESS_FINE_LOCATION = 2000;
    private String[] mPermissions;
    private final String[] permissions9 = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION


    };
    private String[] permissions10 = {
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private final String[] permissions11 = {
           // Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    };
    private boolean all_permissions_cheack_boolean = false;
    private boolean backgr_permissions_cheack_boolean = false;

    private void backgroundPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 2);
        }

    }

    private void background_permissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
        Log.e("asd147", "asd");
        builder.setTitle("백그라운드 위치 권한을 위해 항상 허용으로 설정해주세요.");
        builder.setMessage("설정 화면으로 이동 하시겠습니까?");
        builder.setCancelable(false);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                backgroundPermission();
            }
        });
        builder.setNegativeButton("거절", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();

                finish();
            }
        });
        builder.show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceStare) {

        super.onCreate(savedInstanceStare);
        Log.e("spetc", "onCreate");
        setContentView(R.layout.activity_splash);
     //   ActionBar actionBar = getSupportActionBar();
     //   actionBar.hide();

        android_ver_check();
    }

    private static final int MULTIPLE_PERMISSIONS = 101;

    private void android_ver_check() {
        if (Build.VERSION.SDK_INT >= 30) {
            Log.e("activityt_TAG", "onCreat");
            mPermissions = permissions11;// 안드로이드 11.0 이상일 경우 퍼미션 체크

        } else if (Build.VERSION.SDK_INT >= 29) {
            mPermissions = permissions10;
        } else {
            mPermissions = permissions9;
        }
        checkPermissions(mPermissions);
    }

    private boolean checkPermissions(String[] permissi) {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissi) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                Log.e("spetc", "101");
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            Log.e("spetc", "102");
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);

            Log.e("spetc", "103");

            return false;
        } else {

            all_permissions_cheack_boolean = true;


            Log.e("asd94", "asd");
            Log.e("asd96", "asd");
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MULTIPLE_PERMISSIONS) {
            Log.e("spetc", "111");
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("spetc", "113");
                for (int i = 0; i < permissions.length; i++) {
                    Log.e("spetc", "114" + ", " + permissions[i] + ", " + permissions[i] + "," + grantResults[i] + "," + PackageManager.PERMISSION_GRANTED);

                    if (permissions[i].equals(mPermissions[i])) {
                        Log.e("spetc", "115" + ", " + permissions[i] + "," + grantResults[i] + "," + PackageManager.PERMISSION_GRANTED);
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Log.e("spetc", "117, " + grantResults[i] + "," + PackageManager.PERMISSION_GRANTED);

                            permissions_refuse_dialog();

                            // showToast_PermissionDeny();
                        } else {
                            all_permissions_cheack_boolean = true;
                            //Toast.makeText(this, "11", Toast.LENGTH_SHORT).show();

                        }
                    }
                }
                Log.e("spetc", "123");
            } else {
                Log.e("spetc", "125");
                permissions_refuse_dialog();
                // showToast_PermissionDeny();
            }
            Log.e("spetc", "127");
        } else {
            Log.e("spetc", "128");
        }
        Log.e("spetc", "129");
    }

    @Override
    protected void onStart() {

        super.onStart();
        Log.e("spetc", "onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("spetc", "onResume");
        if (all_permissions_cheack_boolean) {
          //  GPSSetting();
             if (Build.VERSION.SDK_INT >= 29) {

               int permissionCheck2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                if (permissionCheck2 == PackageManager.PERMISSION_DENIED) { //백그라운드 위치 권한 확인
                    //위치 권한 요청
                    background_permissionDialog();
                } else {
                    Log.e("dd--216","dd");
                    buttonSwitchGPS_ON();
               //     GPSSetting();
                }
            } else {
                 Log.e("dd--221","dd");
                 buttonSwitchGPS_ON();
              //  GPSSetting();
            }
            //  background_permissionDialog();
        }
    }

    private LocationManager locationManager;

    private void GPSSetting() {
        //  ContentResolver res = getContentResolver();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
            Log.e("asd147", "asd");
            builder.setTitle("GPS 설정");
            builder.setMessage("GPS를 사용하시겠습니까?");
            builder.setCancelable(false);
            builder.setPositiveButton("사용", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e("asd153", "asd");
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

            Log.e("asd167", "asd");
            next_activity();
        }

    }
    private void next_activity(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread() {
                    @Override
                    public void run() {
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        String mstart_name = sp.getString("startname", "");
                        Intent newIntent;
                        if(mstart_name.equals("")) {
                            newIntent = new Intent(getApplicationContext(), MainLoginActivity.class);
                        }else{
                            SharedPreferences sp2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            String Shared_zone_name_num = sp2.getString("Shared_zone_name_num", "0");
                            if(Shared_zone_name_num.equals("0")) {
                                newIntent = new Intent(getApplicationContext(), MainActivity.class);
                            }else{
                                newIntent = new Intent(getApplicationContext(), MainSectorEntrance_Activity.class);
                            }
                        }
                        startActivity(newIntent);
                        finish();

                    }
                }.start();
            }
        }, 200);
    }
    @Override
    protected void onPause() {
        Log.e("spetc", "onPause");
        super.onPause();

    }

    @Override
    protected void onStop() {
        Log.e("spetc", "onStop");

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("spetc", "onDestroy()");


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("spetc", "onRestart");
        checkPermissions(mPermissions);
    }

    private void permissions_refuse_dialog() {

        AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
        localBuilder.setTitle("권한 설정")
                .setMessage("권한을 허용하지 않으면\n앱을 사용 하실 수 없습니다..")
                .setPositiveButton("권한 설정하러 가기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);

                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                            startActivity(intent);
                        }
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {

                        showToast_PermissionDeny();
                    }
                })
                .show();
    }

    private void showToast_PermissionDeny() {
        Toast.makeText(getApplicationContext(), "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }
  private int REQUEST_CHECK_SETTINGS = 1002;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            Log.e("dd--921","dd");
            if (resultCode == Activity.RESULT_OK) { // gps
                Log.e("asd167", "asd");
                next_activity();
                Log.e("dd--924","dd");
            } else {
              //  buttonSwitchGPS_ON();
                Log.e("dd--926","dd");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    public void buttonSwitchGPS_ON() {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder();

        locationSettingsRequestBuilder.addLocationRequest(locationRequest);


        locationSettingsRequestBuilder.setAlwaysShow(true);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build());

        Log.e("dd--1172","dd");
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                next_activity();
                Log.e("dd--1076","dd");
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                if (e instanceof ResolvableApiException) {
                    Log.e("dd--1085","dd");
                    try {
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        resolvableApiException.startResolutionForResult(SplashActivity.this, REQUEST_CHECK_SETTINGS);
                        Log.e("dd--1089","dd");
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        Log.e("dd--1091",sendIntentException.getMessage());
                        sendIntentException.printStackTrace();
                    }
                }
            }
        });
    }
}