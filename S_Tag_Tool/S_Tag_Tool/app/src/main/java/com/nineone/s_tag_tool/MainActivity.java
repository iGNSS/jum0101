package com.nineone.s_tag_tool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String[] permissions;
    private String[] permissions1 = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,

    };

    private String[] permissions2 = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
    };
    MainFragment fragment1;
    Sub1Fragment fragment2;
    private long btnPressTime = 0;
    private boolean systemBoole = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions=permissions2;// 안드로이드 6.0 이상일 경우 퍼미션 체크
            checkPermissions(permissions2);
        }else{
            permissions=permissions1;
            checkPermissions(permissions);
        }*/
       // GPSSetting();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        fragment1 = new MainFragment();
        fragment2 = new Sub1Fragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();
        TextView button = findViewById(R.id.button);
        TextView button2 = findViewById(R.id.button2);
        button2.setVisibility(View.GONE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  Log.e("systemBoole0", String.valueOf(systemBoole));
                   //   Bundle bundle = new Bundle();
                    //  bundle.putBoolean("systemBoole",systemBoole);
                   //   fragment1.setArguments(bundle);
                   //   getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();/*프래그먼트 매니저가 프래그먼트를 담당한다!*/
                      /*getSupportFragmentManager().beginTransaction().add(R.id.container, fragment1).commit();*/
            }
        });
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                /*프래그먼트 매니저가 프래그먼트를 담당한다!*/
                /*프래그먼트 매니저가 프래그먼트를 담당한다!*/
                // getSupportFragmentManager().beginTransaction().attach(fragment1).commitNow();/*프래그먼트 매니저가 프래그먼트를 담당한다!*/
                if(!systemBoole) {
                    systemBoole=true;
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("systemBoole",true);
                    fragment1.setArguments(bundle);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        getSupportFragmentManager().beginTransaction().detach(fragment1).commitNow();/*프래그먼트 매니저가 프래그먼트를 담당한다!*/
                        getSupportFragmentManager().beginTransaction().attach(fragment1).commitNow();/*프래그먼트 매니저가 프래그먼트를 담당한다!*/

                    } else {
                        getSupportFragmentManager().beginTransaction().detach(fragment1).attach(fragment1).commit();
                    }
                }else{
                    systemBoole=false;
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("systemBoole",false);
                    fragment1.setArguments(bundle);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        getSupportFragmentManager().beginTransaction().detach(fragment1).commitNow();/*프래그먼트 매니저가 프래그먼트를 담당한다!*/
                        getSupportFragmentManager().beginTransaction().attach(fragment1).commitNow();/*프래그먼트 매니저가 프래그먼트를 담당한다!*/

                    } else {
                        getSupportFragmentManager().beginTransaction().detach(fragment1).attach(fragment1).commit();
                    }
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();/*프래그먼트 매니저가 프래그먼트를 담당한다!*/
                return false;
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();/*프래그먼트 매니저가 프래그먼트를 담당한다!*/

    }
    private LocationManager locationManager;
    private void GPSSetting() {
        //  ContentResolver res = getContentResolver();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
                    //finish();
                }
            });
            builder.show();

        }

    }
    public void onFragmentChange(int index){
        if(index == 0 ){
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();
        }else if(index == 1){
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment2).commit();
        }
    }

    private static final int MULTIPLE_PERMISSIONS = 101;
   /* private boolean checkPermissions(String[] permissi) {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissi) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        } else {

        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[i])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showToast_PermissionDeny();
                            }
                        }
                    }
                } else {
                    showToast_PermissionDeny();
                }
                break;
            }
        }
    }*/
    private void showToast_PermissionDeny() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
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