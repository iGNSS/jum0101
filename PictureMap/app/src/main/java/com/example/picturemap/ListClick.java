package com.example.picturemap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.LocationButtonView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class ListClick extends AppCompatActivity implements OnMapReadyCallback {
    String getday=null,getTime=null,getDistance=null,getCity=null;//각각 WalkList에서 정보를 받아오기 위함 변수드ㅏㄹ
    TextView mDay,mtime,mdistance;
    List<LatLng> coords;
    private NaverMap naverMap;
    private ViewGroup mapViewContainer;//지도를 띄어주는 변수
    private MapView mapView;//지도 띄우기 위한 변수
    PathOverlay pathOverlay = new PathOverlay();
    private LocationButtonView locationButtonView;
    private FusedLocationSource mLocationSource;
    private static int PERMISSION_REQUEST_CODE = 1;
    double latitude1 =0;
    double longitude1 = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_click);
        //지도 띄워주기
        mapView = new MapView(this);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_fragment);
        mapViewContainer.addView(mapView);
        //액션바 제목 설정
        ActionBar actionBar = getSupportActionBar();  //제목줄 객체 얻어오기
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("기록");

        Intent listClick = getIntent();//WalkList에서 정보를 받아오기
        getday = listClick.getExtras().getString("getday");//날짜
        getTime = listClick.getExtras().getString("getTime");//걸은시간
        getDistance = listClick.getExtras().getString("getDistance");//이동거리
        getCity = listClick.getExtras().getString("getCity");//이동경로
        coords = Converters.fromString(getCity);//String으로 컨버트된 이동경로를 List로 변환
        mDay =(TextView)findViewById(R.id.Day);
        mtime =(TextView)findViewById(R.id.time);
        mdistance =(TextView)findViewById(R.id.distance);
        if(coords.size()>0) {
            latitude1 = coords.get(0).latitude;//coords의 첫번째 위도
            longitude1 = coords.get(0).longitude;//coords의 첫번째 경도
        }
        mDay.setText(getday +"");
        mtime.setText(getTime+"");
        mdistance.setText(getDistance+"");

        naverMapBasicSettings();
    }

    //여기까지 위치 접근권한 허용 기능 구현
    public void naverMapBasicSettings() {
        mapView.getMapAsync(this);
        //내위치 버튼
        // 내위치 찾기 위한 source
        mLocationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);
    }
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {//지도 객체의 초기 세팅을 위한 클래스
        naverMap.setMapType(NaverMap.MapType.Basic); // 지도 타입
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_MOUNTAIN, true);
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BICYCLE, true);
        naverMap.setIndoorEnabled(true);

        pathOverlay.setWidth(10);//라인 크기
        pathOverlay.setOutlineWidth(2);//라인태두리 크기
        pathOverlay.setColor(Color.parseColor("#0099FF"));//라인색
        pathOverlay.setHideCollidedSymbols(true);//라인 위에있는 심볼을 지운다
        pathOverlay.setHideCollidedMarkers(true);//라인 위에있는 마크를 지운다.
       // naverMap.moveCamera(CameraUpdate.scrollTo(new LatLng(latitude,longitude)));
        if(coords.size()>0) {//걸은 경로가 있으면 지도에 라인 생성
            pathOverlay.setCoords(coords);//위치 좌표들 넣기
            pathOverlay.setMap(naverMap);//라인에 적용
            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(latitude1, longitude1));//라인의 첫번째 위치로 카메라 이동
            naverMap.moveCamera(cameraUpdate);//지도 업데이트
        }
        else if(coords.size()<=0){//걸은 경로가 없으면 지도 표시 안함
            mapViewContainer.addView(null);
        }
    }
}