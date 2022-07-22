package com.nineone.s_tag_tool;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    MainFragment fragment1;
    Sub1Fragment fragment2;
    private boolean systemBoole = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        fragment1 = new MainFragment();
        fragment2 = new Sub1Fragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();
        TextView button = findViewById(R.id.button);
        TextView button2 = findViewById(R.id.button2);
        button2.setVisibility(View.GONE);

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

        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();/*프래그먼트 매니저가 프래그먼트를 담당한다!*/

    }

  /*  private void GPSSetting() {
        //  ContentResolver res = getContentResolver();
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
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

    }*/

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("종료 확인");
        builder.setMessage("정말로 종료하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //moveTaskToBack(true); // 태스크를 백그라운드로 이동
                finish(); // 액티비티 종료 + 태스크 리스트에서 지우기
               // android.os.Process.killProcess(android.os.Process.myPid()); // 앱 프로세스 종료
            }
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("activityt_TAG2","onDestroy");
        //  if (mBluetoothLeScanner != null) {
        //  recyclerVierAdapter.item_Clear();
        //      stopScan();
        // }


    }
    @Override
    public void onPause() {
        Log.e("activityt_TAG2", "onPause");
        super.onPause();

        // finish();
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.e("activityt_TAG2","onStop");
        //}

    }
    @Override
    public void onStart() {

        super.onStart();
        //   startScan();
        Log.e("activityt_TAG2", "onStart()");
    }

}