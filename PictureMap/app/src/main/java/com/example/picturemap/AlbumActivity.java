package com.example.picturemap;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class AlbumActivity extends AppCompatActivity {
    public String basePath = null;//
    public GridView mGridView;//그리드뷰 함수
    private AlbumAdapter mAlbumAdapter;//앨범 어댑터 연결 함수

    private String[] imgs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_album);
        ActionBar actionBar = getSupportActionBar();  //제목줄 객체 얻어오기
        actionBar.setTitle("앨범");  //액션바 제목설정
        actionBar.setDisplayHomeAsUpEnabled(true);

        // App.을 실행하자 마자 지정한 경로의 생성 및 접근에 용이하도록 아래와 같이 생성
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "PictureMap");// 지정 경로의 directory를 File 변수로 받아

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
            }
        }
        basePath = mediaStorageDir.getPath();//File mediaStorageDir에 넣어준 경로 String으로 반환

        mGridView = (GridView) findViewById(R.id.gridview); // .xml의 GridView와 연결
        mAlbumAdapter = new AlbumAdapter(this, basePath); // 앞에서 정의한 Image Adapter와 연결
        mGridView.setAdapter(mAlbumAdapter); // GridView가 Custom Image Adapter에서 받은 값을 뿌릴 수 있도록 연결
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), mCustomImageAdapter.getItemPath(position), Toast.LENGTH_LONG).show();
                Intent i = new Intent(getApplicationContext(), AlbumClick.class);//앨범 액티비티로 이동
                // passing array index
                i.putExtra("id", position);//position정보 넘겨주기
                startActivity(i);
            }
        });
    }
}