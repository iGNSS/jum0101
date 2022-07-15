package com.example.picturemap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.GroundOverlay;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.overlay.PolylineOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.LocationButtonView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements Overlay.OnClickListener, OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    public static final String EXTRA_REPLY =
            "com.example.android.roomwordssample.REPLY";
    private static int PERMISSION_REQUEST_CODE = 1;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private FusedLocationSource mLocationSource;
    private NaverMap naverMap;
    private MapView mapView;
    private LocationButtonView locationButtonView;
    private ViewGroup mapViewContainer;
    private TextView mX,mY;
    private Button mbutton1,mbutton2;
    private double lat,lon;
    private double lat2,lon2;
    private ImageButton mBack;
    private int Mode=0;
   // private TextView mstats_total_time;
    private boolean walkState = false;
    private boolean mTimer= false;
    String Saving_File_name;
    String dataToServer;
    String formatDate;
    private boolean mSave = false;
    public static final String LAYER_GROUP_MOUNTAIN = null;

    private double f_lat;
    private double f_long;
    public String basePath = null;
    String StarttimetDate;
    String StoptimetDate;
    private Chronometer mstats_total_time;
    long stopTime = 0;
    double bef_lat;
    double bef_long;
    double cur_lat;
    double cur_long;
    private TextView mdistance;

    public static MyDatabase myDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mLocationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);
        //mapView = findViewById(R.id.map_fragment);
        //checkRunTimePermission();
        // 지도 객체 생성
        save();
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();

        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeWalkState();        //걸음 상태 변경
            }
        });
        mapView = new MapView(this);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_fragment);
        mapViewContainer.addView(mapView);
        mX = findViewById(R.id.X_a);
        mY = findViewById(R.id.Y_a);
        mBack = (ImageButton) findViewById(R.id.BackButton);
        mX.bringToFront();
        mY.bringToFront();
       // mapView.onCreate(savedInstanceState);
       // mapView.getMapAsync(this);

        myDatabase= Room.databaseBuilder(getApplicationContext(),MyDatabase.class,"note_database").allowMainThreadQueries().build();

        mLocationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);
        mbutton1 = (Button) findViewById(R.id.button1);
        mbutton1.setOnClickListener(mClickListener);
        mbutton2 = (Button) findViewById(R.id.button2);
        mbutton2.setOnClickListener(mClickListener);
        mdistance = (TextView) findViewById(R.id.ddistance);
        mdistance.bringToFront();
        mstats_total_time = (Chronometer)findViewById(R.id.chronometer);
        mstats_total_time.bringToFront();
        mstats_total_time.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer chronometer) {
                Object obj;
                Object obj2;
                Object obj3;
                long elapsedRealtime = SystemClock.elapsedRealtime() - chronometer.getBase();
                int i = (int) (elapsedRealtime / 3600000);
                long j = elapsedRealtime - ((long) (3600000 * i));
                int i2 = ((int) j) / 60000;
                int i3 = ((int) (j - ((long) (60000 * i2)))) / 1000;
                StringBuilder sb = new StringBuilder();
                if (i < 10) {
                    obj = "0" + i;
                } else {
                    obj = Integer.valueOf(i);
                }
                sb.append(obj);
                sb.append(":");
                if (i2 < 10) {
                    obj2 = "0" + i2;
                } else {
                    obj2 = Integer.valueOf(i2);
                }
                sb.append(obj2);
                sb.append(":");
                if (i3 < 10) {
                    obj3 = "0" + i3;
                } else {
                    obj3 = Integer.valueOf(i3);
                }
                sb.append(obj3);
                chronometer.setText(sb.toString());
            }
        });
        this.mstats_total_time.setText("00:00:00");
        ((FloatingActionButton) findViewById(R.id.fab)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                MainActivity.this.changeWalkState();
            }
        });
        naverMapBasicSettings();

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "PictureMap");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
            }
        }
        //TextView textView = (TextView)findViewById(R.id.textView2);
        basePath = mediaStorageDir.getPath();
    }


    private final Button.OnClickListener mClickListener = new View.OnClickListener() {//각 버튼 클릭리스너
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.button1:
                    Intent intent = new Intent(getApplicationContext(),CamaraActivity.class);
                    startActivity(intent);
                    break;
                case R.id.button2:
                    Intent intent2 = new Intent(getApplicationContext(),AlbumActivity.class);
                    startActivity(intent2);
                    break;
            }
        }
    };



    private long currentTime1;
    private long lastTime;
  //  private long timer=0;
    //PolylineOverlay polyline = new PolylineOverlay();
    PathOverlay pathOverlay = new PathOverlay();
    List<LatLng> coords = new ArrayList<>();
    private Marker marker1 = new Marker();
    private InfoWindow infoWindow1 = new InfoWindow();
    private Vector<LatLng> markersPosition;
    private Vector<Marker> activeMarkers;
    Context context;

    //특정 사진의 위치를 나타내는 함수들
    String stringData;
    String LATITUDE_REF = null;
    String LONGITUDE_REF = null;
    String DATETIME = null;
    double d1 = 0;
    double d2 = 0;   //특정 사진의 위치를 나타내는 함수들

    ExifInterface exif;
    private GpsTracker gpsTracker;

    // 현재 카메라가 보고있는 위치
    public LatLng getCurrentPosition(NaverMap naverMap) {
        CameraPosition cameraPosition = naverMap.getCameraPosition();
        return new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
    }
    public final static double REFERANCE_LAT_X3 = 3 / 109.96;
    public final static double REFERANCE_LNG_X3 = 3 / 88.74;
    public boolean withinSightMarker(LatLng currentPosition, LatLng markerPosition) {
        boolean withinSightMarkerLat = Math.abs(currentPosition.latitude - markerPosition.latitude) <= REFERANCE_LAT_X3;
        boolean withinSightMarkerLng = Math.abs(currentPosition.longitude - markerPosition.longitude) <= REFERANCE_LNG_X3;
        return withinSightMarkerLat && withinSightMarkerLng;
    }
  /*  // 지도상에 표시되고있는 마커들 지도에서 삭제
    private void freeActiveMarkers() {
        if (activeMarkers == null) {
            activeMarkers = new Vector<Marker>();
            return;
        }
        for (Marker activeMarker: activeMarkers) {
            activeMarker.setMap(null);
        }
        activeMarkers = new Vector<Marker>();
    }*/

    //String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/PictureMap"; //파일 위치

   // File f = new File(path);
   // File[] files = f.listFiles(new FileFilter() {
    //    @Override
    //    public boolean accept(File pathname) {
     //       return pathname.getName().toLowerCase(Locale.US).endsWith(".jpg"); //확장자
     //   }
   // });
    //앨범폴더의 사진위치를 모두 나타내는 함수들
    ExifInterface exif1;
    Bitmap[] bitmap = new Bitmap[1000];
    String[] LATITUDE_REF1= new String[1000];
    String[] LONGITUDE_REF1 = new String[1000];
    String[] DATETIME1 = new String[1000];
    double[] d3 = new double[1000];
    double[] d4 = new double[1000];//앨범폴더의 사진위치를 모두 나타내는 함수들
    double[] d5 = new double[1000];
    private  InfoWindow infoWindow;

    int cou = 0;
    private double dist;
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
       // LatLng initialPosition = new LatLng(37.506855, 127.066242);
        naverMap.setMapType(NaverMap.MapType.Basic); // 지도 타입
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_MOUNTAIN, true);
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BICYCLE, true);
        naverMap.setIndoorEnabled(true);
        locationButtonView.setMap(naverMap);
        naverMap.setLocationSource(mLocationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        //InfoWindow infoWindow = new InfoWindow();
        Marker marker1 = new Marker();//앨범에서 불러올때 마커
        markersPosition = new Vector<LatLng>();//지도자체의 사진 마커
        Intent intent = getIntent();//여기부터 앨범에서 특정 사진 위치 표시 마커
        stringData = intent.getStringExtra("picturedata");
        InfoWindow mInfoWindow = new InfoWindow();
        try {
            if(stringData==null){
                exif=null;
            }
            else {
                gpsTracker = new GpsTracker(MainActivity.this);
                exif = new ExifInterface(stringData);
                LATITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                LONGITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
                DATETIME = exif.getAttribute(ExifInterface.TAG_DATETIME);
                d1 = Double.parseDouble(LATITUDE_REF);
                d2 = Double.parseDouble(LONGITUDE_REF);
                String address = getCurrentAddress(d1, d2);
                naverMap.moveCamera(CameraUpdate.scrollTo(new LatLng(d1, d2)));
                marker1.setPosition(new LatLng(d1, d2));
                mInfoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(this) {
                    @NonNull
                    @Override
                    public CharSequence getText(@NonNull InfoWindow infoWindow) {
                        return DATETIME + "\n"+address ;
                    }
                });
                marker1.setMap(naverMap);
                mInfoWindow.open(marker1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }//여기까지 특정 위치 표시마커

        //GPS정보가 변경되었을 시 호출되는 이벤트
        naverMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener() {
            @Override
            public void onLocationChange(@NonNull Location location){
                //List<LatLng> coords = new ArrayList<>();
                lat = location.getLatitude();
                lon = location.getLongitude();
                Log.e("lat,lon",lat+","+lon);
                double alt  = location.getAltitude();
                currentTime1 = System.currentTimeMillis();
                mX.setText("X: " + lat);
                //mY.setText("Y: " + lon);
                mY.setText("고도: "+alt);
                if (walkState) {//걸음 시작 버튼이 눌렸을 때

                    pathOverlay.setWidth(10);
                    pathOverlay.setOutlineWidth(2);
                    pathOverlay.setColor(Color.parseColor("#0099FF"));
                    pathOverlay.setHideCollidedSymbols(true);
                    pathOverlay.setHideCollidedMarkers(true);
                   //pathOverlay.setPatternImage(OverlayImage.fromResource(R.drawable.path_pattern));
                   // pathOverlay.setPatternInterval(10);
                    mdistance.setText("이동거리 : " + ((float) sum_dist) + " m");
                    long gabOfTime = (currentTime1 - lastTime);
                    if (gabOfTime > 1000) {
                        naverMap.moveCamera(CameraUpdate.scrollTo(new LatLng(lat, lon)));//카메라 고정
                        Collections.addAll(coords, new LatLng(lat, lon), new LatLng(lat, lon));
                        lastTime = currentTime1;
                        cur_lat = lat;
                        cur_long = lon;
                        Location location2 = new Location("point A");
                        location2.setLatitude(cur_lat);
                        location2.setLongitude(cur_long);
                        Location location3 = new Location("point B");
                        location3.setLatitude(bef_lat);
                        location3.setLongitude(bef_long);
                        bef_lat = lat;
                        bef_long = lon;
                        if (cou >= 1) {
                            double distanceTo = (double) location2.distanceTo(location3);
                            dist = distanceTo;
                            Double.isNaN(distanceTo);
                            double d = (double) ((int) (distanceTo * 1000.0d));
                            Double.isNaN(d);
                            double d2 = d / 1000.0d;
                            dist = d2;
                            sum_dist += d2;
                        }

                        cou++;
                        // startLatLng = new LatLng(lat, lon);
                        coords.add(new LatLng(lat, lon));
                        SAVE();
                        pathOverlay.setCoords(coords);
                        pathOverlay.setMap(naverMap);
                    }
                }
            }
        });
        /*infoWindow = new InfoWindow();
        infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(this) {
            @NonNull
            @Override
            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                return "제발 되라";
            }
        });*/
       // Marker marker = new Marker();;
        //찍은 사진 위치가 마커로 표시되는 코드
        String path = Environment.getExternalStorageDirectory() + "/DCIM/PictureMap/";
        File directory = new File(path);
        File[] files = directory.listFiles();

        List<String> filesNameList = new ArrayList<>();
        int aa=0;
        for (int i=0; i< files.length; i++) {
            exif1=null;
            // mtext3.setText("file: " + files[i]+"\n");
            //  filesNameList.add(files[i].getName());
            try {
                exif1 = new ExifInterface(String.valueOf(files[i]));
                if(exif1.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)!=null) {
                    LATITUDE_REF1[i] = exif1.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                    LONGITUDE_REF1[i] = exif1.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
                    DATETIME1[i] = exif1.getAttribute(ExifInterface.TAG_DATETIME);
                    bitmap[i] = BitmapFactory.decodeFile(String.valueOf(files[i]));
                    d3[i] = Double.parseDouble(LATITUDE_REF1[i]);
                    d4[i] = Double.parseDouble(LONGITUDE_REF1[i]);
                    //d5[i] = Double.parseDouble(DATETIME1[i]);
                    markersPosition.add(new LatLng(d3[i], d4[i]));
                    //   {//지도에 모든 머커 표시
                    activeMarkers = new Vector<Marker>();
                    Marker marker2 = new Marker();
                    marker2.setTag(null);
                    marker2.setTag(i);

                    for (LatLng markerPosition : markersPosition) {
                        marker2.setPosition(markerPosition);
                    }
                    marker2.setMap(naverMap);
                    marker2.setOnClickListener(this);
                    activeMarkers.add(marker2);
                }
                else if( exif1.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)==null){

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        infoWindow = new InfoWindow();
        infoWindow.setAdapter(new InfoWindow.DefaultViewAdapter(this){
            @NonNull
            @Override
            protected View getContentView(@NonNull InfoWindow infoWindow) {
                Marker marker = infoWindow.getMarker();
                int a = (int) marker.getTag();
                View view = View.inflate(MainActivity.this, R.layout.view_info_window, null);
                ((TextView) view.findViewById(R.id.name)).setText(DATETIME1[a]+"");
                ImageView imageView=(ImageView) view.findViewById(R.id.picture);
               // mThumbnail = ThumbnailUtils.extractThumbnail(bm, 200, 200);
                imageView.setImageBitmap(bitmap[a]);
             
                return view;
            }
        });

      // 카메라 이동 되면 호출 되는 이벤트,카메라에 위치에 따라 마커가 보이거나 숨겨짐
      /*  naverMap.addOnCameraChangeListener(new NaverMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(int reason, boolean animated) {
                freeActiveMarkers();
                // 정의된 마커위치들중 가시거리 내에있는것들만 마커 생성
                LatLng currentPosition = getCurrentPosition(naverMap);
                for (LatLng markerPosition: markersPosition) {
                    if (!withinSightMarker(currentPosition, markerPosition))
                        continue;
                    Marker marker = new Marker();
                    marker.setPosition(markerPosition);
                    marker.setMap(naverMap);
                    activeMarkers.add(marker);
                }
            }
        });
        //여기까지 다중마커*/
    }

    private double sum_dist;
    private void changeWalkState(){
        Object obj;
        Object obj2;
        Object obj3;
        if(!walkState) {
            long now = System.currentTimeMillis();//날짜 불러오는 함수
            Date date = new Date(now);
            SimpleDateFormat mFormat = new SimpleDateFormat("yy.MM.dd_HH:mm:ss");//날짜 출력 형식
            formatDate = mFormat.format(date);
            mapViewContainer.setClickable(false );
            Toast.makeText(getApplicationContext(), "걸음 시작", Toast.LENGTH_SHORT).show();
            walkState = true;
            mSave=true;
            mTimer=true;
            naverMap.moveCamera(CameraUpdate.scrollTo(new LatLng(lat, lon)));
            naverMap.moveCamera(CameraUpdate.zoomTo(20));//줌 20으로 고정
            mstats_total_time.setBase(SystemClock.elapsedRealtime());
            mstats_total_time.start();
           // startLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());        //현재 위치를 시작점으로 설정
        }else{
           // zMyDataList myDataList=new zMyDataList();
            mapViewContainer.setClickable(true );
            Toast.makeText(getApplicationContext(), "걸음 종료", Toast.LENGTH_SHORT).show();
            f_lat = lat;
            f_long = lon;
            //polyline.setMap(null);
            long base = mstats_total_time.getBase() - SystemClock.elapsedRealtime();
            stopTime = base;
            int i = (int) ((base / 3600000) * -1);
            long j = (long) (3600000 * i);
            int i2 = (int) (((base - j) * -1) / 60000);
            int i3 = (int) ((((base - j) - ((long) (60000 * i2))) * -1) / 1000);
            StringBuilder sb = new StringBuilder();
            if (i < 10) {
                obj = "0" + i;
            } else {
                obj = Integer.valueOf(i);
            }
            sb.append(obj);
            sb.append(":");
            if (i2 < 10) {
                obj2 = "0" + i2;
            } else {
                obj2 = Integer.valueOf(i2);
            }
            sb.append(obj2);
            sb.append(":");
            if (i3 < 10) {
                obj3 = "0" + i3;
            } else {
                obj3 = Integer.valueOf(i3);
            }
            sb.append(obj3);
            String sb2 = sb.toString();
            StoptimetDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
            //this.f75db.todoDao().insert(new Todo(sb2, this.mdistance.getText().toString(), this.coords));
            myDatabase.myDao().insert(new MyDataList(StoptimetDate,sb2,mdistance.getText().toString(),coords));
          //  myDataList.setName(sb2);
           // myDataList.setEmail(mdistance.getText().toString());
           // myDataList.setCity(coords);
            //myDatabase.myDao().addData(myDataList);
            sb2=null;
            Toast.makeText(getApplicationContext(), "걸음 종료", Toast.LENGTH_SHORT).show();
            walkState = false;
            mSave = false;
            coords.clear();
            stopTime = 0;
            mstats_total_time.stop();
           mdistance.setText("이동거리 : 0.0 m");
            sum_dist = 0.0;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       // super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(mLocationSource.onRequestPermissionsResult(requestCode,permissions,grantResults)){
            if(!mLocationSource.isActivated()){
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
                finish();
                Toast.makeText(MainActivity.this, "지도를 사용하기 위해 위치정보가 필요합니다.", Toast.LENGTH_LONG).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"+ "위치 설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 위치 정보를 수신을 위해 위치 서비스가 필요합니다..", Toast.LENGTH_LONG).show();
                finish();
            }
        });
        builder.create().show();
    }
    LocationManager locationManager;
    public boolean checkLocationServicesStatus() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
//여기까지 위치 접근권한 허용 기능 구현
    public void naverMapBasicSettings() {
        mapView.getMapAsync(this);
        //내위치 버튼
        locationButtonView = findViewById(R.id.locationbuttonview);
        locationButtonView.bringToFront();
        // 내위치 찾기 위한 source
        mLocationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);
    }


    public void SAVE(){//저장 작업 실행
        if(!mSave){

        }
        else {
            String aaa=formatDate;
            dataToServer = "";//데이터 서버 초기화
            Saving_File_name = aaa +".csv";//저장 파일 이름
            dataToServer =mX.getText().toString() + "," + mY.getText().toString();
            //데이터 서버에 저장될 형식
            writeLog(dataToServer);
        }
    }
    public void writeLog(String data) {//저장 기능 설정 및 저장 위치 설정
        File str_Path_Full = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Stag");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String str_Path_Full2 = str_Path_Full + "/Stag/" + this.Saving_File_name;
        File file = new File(str_Path_Full2);
        if (!file.exists()) {//파일이 존재하지않으면
            try {
                file.createNewFile();//새 파일 생성
            } catch (IOException e) {
            }
        }
        try {
            BufferedWriter bfw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(str_Path_Full2, true), "MS949"));
            bfw.write(data + "\r\n");
            bfw.flush();
            bfw.close();
        } catch (FileNotFoundException e2) {
            Log.e(TAG, e2.toString());
        } catch (IOException e3) {
            Log.e(TAG, e3.toString());
        }
    }
    public void save(){
        //외장메모리(SD card)가 있는지?
        String state= Environment.getExternalStorageState();
        //외장메모리 상태가 연결(mounted) 되어 있는지 확인
        if(!state.equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(this, "SDcard is not mounted", Toast.LENGTH_SHORT).show();
            return;
        }
        //동적퍼미션 체크작업(api 23버전 이상일때)
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            //이 앱이 사용자로부터 퍼미션을 받았는지 체크
            int checkResult=checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            //퍼미션이 허용되어 있지 않다면?
            if(checkResult== PackageManager.PERMISSION_DENIED) { //PackageManager.PERMISSION_DENIED 또는 PackageManager.PERMISSION_GRANTED 가 있다. (허용, 허용되지 않음)
                //사용자에게 퍼미션을 요청하는 다이얼로그를 보여주는 메소드를 실행
                String[] permisions= new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permisions,100);
                return;
            }
        }//
    }
    public String getCurrentAddress( double latitude, double longitude) {
        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }
        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }
        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";
    }

    @Override
    public boolean onClick(@NonNull Overlay overlay) {
        if (overlay instanceof Marker) {
            Marker marker = (Marker) overlay;
            if (marker.getInfoWindow() != null) {
                infoWindow.close();
            } else {
                infoWindow.open(marker);
            }
            return true;
        }
        return false;
    }
}