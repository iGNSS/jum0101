package com.nineone.gps_save;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private String[] permissions;
    private final String[] permissions1 = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE

    };

    private final String[] permissions2 = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };
    private LocationManager locationManager;
    private EditText startedit;
    private Button startbutton;
    // private GpsTracker gpsTracker;
    private TextView textView1, textView2, textView3, msgettime, textView4, textView5;
    private boolean start_true = false;
    private String mstart_name = null;
    // private LocationManager lm;
    private long baseTime, pauseTime;
    private long starttime, beforetime, nowtime;
    private SimpleDateFormat timeformat;
    private GPSListener gpsListener;
    gpsLocationListener gpslocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions = permissions2;// ??????????????? 6.0 ????????? ?????? ????????? ??????
            checkPermissions(permissions2);
        } else {
            permissions = permissions1;
            checkPermissions(permissions);
        }
      // GPSSetting();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //????????? ???????????? Editor??? ????????????.
        startedit = findViewById(R.id.startEdit);
        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        msgettime = findViewById(R.id.sgettime);
        textView4 = findViewById(R.id.textView4);
        textView5 = findViewById(R.id.textView5);
        startbutton = findViewById(R.id.startButton);


        //    gpsListener = new GPSListener();

        //    gpsTracker = new GpsTracker(MainActivity.this);

        startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startedit.getText().toString().length() == 0 || startedit.getText().toString().equals(" ") || startedit.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "??????????????? ????????? ?????????", Toast.LENGTH_SHORT).show();
                } else {
                    if (!start_true) {
                        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        gpslocationListener = new gpsLocationListener();
                        start_true = true;
                        startbutton.setText("??????");
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        //????????? ???????????? Editor??? ????????????.
                        SharedPreferences.Editor edit = preferences.edit();
                        edit.putString("startname", startedit.getText().toString());
                        edit.apply();
                        baseTime = SystemClock.elapsedRealtime();
                        startedit.setFocusable(false);//????????????
                        startedit.setClickable(false);
                        startTimerTask();
                    //   google_gps();
                   //     startLocationUpdates();
                        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                        } else {
                            Log.e("start?","starts");
                         //   startTimerTask();
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpslocationListener);
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, gpslocationListener);

                            //   lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, gpsLocationListener);
                        }
                    } else {

                     //   fusedLocationClient.removeLocationUpdates(locationCallback);
                     //   fusedLocationClient = null;
                        startedit.setFocusable(true);//????????????
                        startedit.setClickable(true);
                        startedit.setFocusableInTouchMode(true);
                        start_true = false;
                        startbutton.setText("??????");
                        locationManager.removeUpdates(gpslocationListener);
                        locationManager = null;
                        gpslocationListener = null;
                        stopTimerTask();
                    }
                }
            }
        });
        aftertime = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
        starttime = System.currentTimeMillis();
        timeformat = new SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.KOREA);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mstart_name = sp.getString("startname", "");
        startedit.setText(mstart_name + "");

    }


    ;


    SimpleDateFormat aftertime;
    String provider = null;
    double longitude = 0;
    double latitude = 0;
    double altitude = 0;
    float accuracy = 0;
    long time = 0;
    String time2;

    private LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                nowtime = System.currentTimeMillis();
                String checktime = timeformat.format(nowtime);
                provider = location.getProvider();
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                altitude = location.getAltitude();

                accuracy = location.getAccuracy();
                time = location.getTime();
                time2 = timeformat.format(time);
                location.getTime();
                Log.e("gpsTracker21", "???????????? : " + provider + ", " +
                        "?????? : " + longitude + ", " +
                        "?????? : " + latitude + ", " +
                        "??????  : " + altitude + " , ??????: " + getTime());

                String mgpsTracker = latitude + "," + longitude + "," + altitude + "," + provider + "," + checktime;
                writeLog(mgpsTracker);
                time = location.getTime();
                String Allloction = "???????????? : " + provider + "\n" +
                        "?????? : " + latitude + "\n" +
                        "?????? : " + longitude + "\n" +
                        "??????  : " + altitude + "\n" +
                        "?????????  : " + accuracy + "\n" +
                        "????????????  : " + time2 + "\n" +
                        "????????????: " + getTime();
                textView4.setText(Allloction);
                // Update UI with location data
                // ...
            }
        }
    };

    class gpsLocationListener implements LocationListener {
        //final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                nowtime = System.currentTimeMillis();
                String checktime = timeformat.format(nowtime);
                //     if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                provider = location.getProvider();
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                altitude = location.getAltitude();

                accuracy = location.getAccuracy();
                time = location.getTime();
                time2 = timeformat.format(time);
                location.getTime();
                Log.e("gpsTracker21", "???????????? : " + provider + ", " +
                        "?????? : " + longitude + ", " +
                        "?????? : " + latitude + ", " +
                        "??????  : " + altitude + " , ??????: " + getTime());

                String mgpsTracker = latitude + "," + longitude + "," + altitude + "," + provider + "," + checktime;
                writeLog(mgpsTracker);
                
                time = location.getTime();
                textView4.setText("???????????? : " + provider + "\n" +
                        "?????? : " + latitude + "\n" +
                        "?????? : " + longitude + "\n" +
                        "??????  : " + altitude + "\n" +
                        "?????????  : " + accuracy + "\n" +
                        "????????????  : " + time2 + "\n" +
                        "????????????: " + getTime());
                //   msgettime.setText(getTime());

                Log.e("gpsTracker1", "??????: " + latitude + ", ??????: " + longitude + ", ??????: " + altitude + ", ??????: " + checktime);
            }else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                nowtime = System.currentTimeMillis();
                String checktime = timeformat.format(nowtime);
                //     if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                provider = location.getProvider();
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                altitude = location.getAltitude();

                accuracy = location.getAccuracy();
                time = location.getTime();
                time2 = timeformat.format(time);
                location.getTime();
                Log.e("gpsTracker21", "???????????? : " + provider + ", " +
                        "?????? : " + longitude + ", " +
                        "?????? : " + latitude + ", " +
                        "??????  : " + altitude + " , ??????: " + getTime());

                time = location.getTime();
                textView4.setText("???????????? : " + provider + "\n" +
                        "?????? : " + latitude + "\n" +
                        "?????? : " + longitude + "\n" +
                        "??????  : " + altitude + "\n" +
                        "?????????  : " + accuracy + "\n" +
                        "????????????  : " + time2 + "\n" +
                        "????????????: " + getTime());
                //   msgettime.setText(getTime());

            }

        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e("gpsTracker3", provider);
        }

        public void onProviderEnabled(String provider) {
            Log.e("gpsTracker4", provider);
        }

        public void onProviderDisabled(String provider) {
            Log.e("gpsTracker5", provider);
        }
    }


    private Timer timer;
    private int mcount = 0;
    private void startTimerTask() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {

                        //  nowtime = beforetime-starttime;
                        //mstartTime.setText(stecount(base));
                        msgettime.setText(getTime());
                   /*     mcount++;
                        if(mcount==60){
                        mcount = 0;
                            fusedLocationClient.removeLocationUpdates(locationCallback);
                            fusedLocationClient = null;
                            locationRequest.
                        }*/
                        /*     gpsTracker = new GpsTracker(MainActivity.this);*/
                    /*    gpsTracker.getLatitude();
                        gpsTracker.getLocation();
                        gpsTracker.getLongitude();
                        gpsTracker.getAltitude();
                        textView1.setText(gpsTracker.getProvider()+"\n"+ gpsTracker.getLatitude()+"");
                        textView2.setText(gpsTracker.getLongitude()+"");
                        textView3.setText(gpsTracker.getAltitude()+"");*/
                        //mstartTime.setText(timeformat.format(nowtime));
                        //  nowtime = System.currentTimeMillis();
                        // String checktime =  timeformat.format(nowtime);
                        //  String mgpsTracker= gpsTracker.getLatitude()+","+ gpsTracker.getLongitude()+","+gpsTracker.getAltitude()+","+checktime;
                        //  writeLog(mgpsTracker);
                        //  Log.e("gpsTracker1","??????: "+gpsTracker.getLatitude()+", ??????: "+ gpsTracker.getLongitude()+", ??????: "+gpsTracker.getAltitude()+", ??????: "+checktime);


                     /*   LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        Location loc_Current = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        cur_lat = loc_Current.getLatitude(); //??????
                        cur_lon = loc_Current.getLongitude(); //??????*/


                    }
                });
            }
        }, 0, 1000);

    }

    private void stopTimerTask() {//????????? ?????? ??????
        if (timer != null) {
            timer.cancel();
            timer = null;
            //  baseTime = 0;
        }

    }

    private String getTime() {
        //????????? ?????? ??????

        long nowTime = SystemClock.elapsedRealtime();
        //???????????? ????????? ????????? ???????
        long overTime = nowTime - baseTime;
        // long hour = (overTime/ 100) / 360;
        // long min = overTime/1000/60;
        // long sec = (overTime/1000)%60;
        Log.e("overtime", String.valueOf(overTime));
        long sec = (overTime / 1000) % 60;
        long min = ((overTime / 1000) / 60)% 60;
        long hour = ((overTime / 1000) / 60) / 60;
        //  long ms = overTime % 1000;

        @SuppressLint("DefaultLocale") String recTime = String.format("%02d:%02d:%02d", hour, min, sec);

        return recTime;
    }



    private void writeLog(String data){//csv?????? ??????
        File file;// = new File(str_Path);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator+ "Nineone"+ File.separator);
        } else {
            file = new File( Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ "Documents"+ File.separator+ "Nineone"+ File.separator );
        }

        if (!file.exists()) {
            file.mkdirs();
        }
        String str_Path_Full;
        File file2 ;//= new File(str_Path_Full);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            str_Path_Full = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()  + File.separator+ "Nineone"+ File.separator + startedit.getText().toString()+".csv";
            file2 = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + File.separator+ "Nineone"+ File.separator,startedit.getText().toString()+".csv");

        } else {
            str_Path_Full = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ "Documents"+ File.separator+ "Nineone"+ File.separator + startedit.getText().toString()+".csv";
            file2 = new File( Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ "Documents"+ File.separator+ "Nineone"+ File.separator , startedit.getText().toString()+".csv");
        }
        if (!file2.exists()) {
            try {
                file2.createNewFile();
                writeLog(startedit.getText().toString());
            } catch (IOException ignored) {
            }
        }
        try {
            BufferedWriter bfw;

            bfw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(str_Path_Full,true),  "EUC-KR"));
            bfw.write(data + "\r\n");
            //bfw.write(log_data);
            bfw.flush();
            bfw.close();
            Log.e("TAGddd", "ddd");
        } catch (FileNotFoundException e) {
            Log.e("TAGddd", e.toString());
        } catch (IOException e) {
            Log.e("TAGddd", e.toString());
        }

    }
    public String getCurrentAddress( double latitude, double longitude) {

        //????????????... GPS??? ????????? ??????
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //???????????? ??????
            Toast.makeText(this, "???????????? ????????? ????????????", Toast.LENGTH_LONG).show();
            return "???????????? ????????? ????????????";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "????????? GPS ??????", Toast.LENGTH_LONG).show();
            return "????????? GPS ??????";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "?????? ?????????", Toast.LENGTH_LONG).show();
            return "?????? ?????????";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }
    private static final int MULTIPLE_PERMISSIONS = 101;
    private boolean checkPermissions(String[] permissi) {
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
    }
    private void showToast_PermissionDeny() {
        Toast.makeText(this, "?????? ????????? ?????? ???????????? ?????? ???????????????. ???????????? ?????? ?????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
        finish();
    }
    private void GPSSetting() {
        //  ContentResolver res = getContentResolver();
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            Log.e("asd147","asd");
            builder.setTitle("GPS ??????");
            builder.setMessage("GPS??? ?????????????????????????");
            builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e("asd153","asd");
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //finish();
                }
            });
            builder.show();

        }

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
    @Override
    protected void onStart() {
        super.onStart();
        Log.e("connect_TAG", "onStart()");
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.e("connect_TAG", "onResume");

    }

    @Override
    protected void onPause() {
        Log.e("connect_TAG", "onPause");
        super.onPause();
        locationManager.removeUpdates(gpslocationListener);
    }
    @Override
    protected void onStop() {
        Log.e("connect_TAG", "onStop");

        super.onStop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("connect_TAG", "onDestroy()");

    }
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;

    private void google_gps() {

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(50);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            //  fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */);
        }
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                startLocationUpdates();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull final Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getApplicationContext());
                    builder1.setTitle("Continious Location Request");
                    builder1.setMessage("This request is essential to get location update continiously");
                    builder1.create();
                    builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            try {
                                resolvable.startResolutionForResult(MainActivity.this,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                    builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplication(), "Location update permission not granted", Toast.LENGTH_LONG).show();
                        }
                    });
                    builder1.show();
                }
            }
        });


    }

    private int REQUEST_CHECK_SETTINGS = 102;
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
}