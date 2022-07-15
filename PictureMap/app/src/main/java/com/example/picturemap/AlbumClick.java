package com.example.picturemap;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import uk.co.senab.photoview.PhotoViewAttacher;

public class AlbumClick extends AppCompatActivity {
    public String basePath = null;// 지정 경로를 받아오기 위한 변수
    private TextView mView;

    private ScaleGestureDetector mScaleGestureDetector;//줌 아웃 함수
    private float mScaleFactor = 1.0f;
    private ImageView mimageView;//사진을 받아오기 위한 이미지뷰
    PhotoViewAttacher mAttacher; //사진의 줌기능을 위한 변수(PhotoViewAttacher=줌기능 오픈소스)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_click);

        // get intent data
        Intent i = getIntent();
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "PictureMap");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
            }
        }
        basePath = mediaStorageDir.getPath();
        // mText = (TextView) findViewById(R.id.Text);
        // Selected image id
        int position = i.getExtras().getInt("id");//AlbumActivity에서 받아온 사진 데이터
        AlbumAdapter albumAdapter = new AlbumAdapter(this,basePath);

        ActionBar actionBar = getSupportActionBar();  //제목줄 객체 얻어오기
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(albumAdapter.getItem2(position));  //액션바 제목설정

        mimageView = (ImageView) findViewById(R.id.full_image_view);
        mimageView.setImageBitmap(albumAdapter.getItemPath(position));//이미지뷰에 앨범에서 클릭한 사진 비트맵 넣기

        mAttacher = new PhotoViewAttacher(mimageView);//줌기능 추가

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manu, menu);
        return true;
    }

    private final int REQUEST_CODE_ALPHA = 100;
    private String filename;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = getIntent();
        int position = i.getExtras().getInt("id");//AlbumActivity에서 사진 정보를 받아옴
        AlbumAdapter albumAdapter = new AlbumAdapter(this,basePath);//앨범 어뎁터 연결
        filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"PictureMap/"+albumAdapter.getItem2(position);
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.btn1:
                //데이터 담아서 팝업(액티비티) 호출하기위한 데이터 보내기
                Intent intent = new Intent(this, PopupActivity.class);
                intent.putExtra("data",albumAdapter.getItem2(position));//albumAdapter에서 getItem2를 받아와 사진 이름을 전송
                startActivityForResult(intent, 1);
                //break;
                return true;
            case R.id.btn2:
                //이사진이 어디서 찍혔는지 지도에 표시해주기 위해 보네는 데이터
                Intent intent2 = new Intent(this, MainActivity.class);
                intent2.putExtra("picturedata",albumAdapter.getItem3(position));//albumAdapter에서 getItem2를 받아와 사진 이름을 전송
                startActivityForResult(intent2, REQUEST_CODE_ALPHA);
                //stopBtn();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }





}