package com.example.picturemap;

import android.app.Activity;
import android.content.Intent;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class PopupActivity extends Activity { //AlbumClik에 상세정보를 띄워주는 팝업 클래스

    private TextView txtText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mata_data);

        //UI 객체생성
        txtText = (TextView)findViewById(R.id.txtText);

        //데이터 가져오기
        Intent intent = getIntent();
        String data = intent.getStringExtra("data");//사진 이름 받아오기
        mata(data);//사진 이름을 이용해 사진 정보 추출
    }

    //확인 버튼 클릭
    public void mOnClose(View v){//사진 데이터를 팝업 형태로 AlbumClik에 띄우기
        //데이터 전달하기
        Intent intent = new Intent();//추출한 정보를
        intent.putExtra("result", "Close Popup");
        setResult(RESULT_OK, intent);
        //액티비티(팝업) 닫기
        finish();
    }
    //여기부터 사진 정보 추출을 위한 클래스들
    public void mata(String aaa){//사진위치 추출
        String filename2 = Environment.getExternalStorageDirectory() + "/DCIM/PictureMap/"+aaa;
        try {
            ExifInterface exif2 = new ExifInterface(filename2);
            showExif(exif2);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error!", Toast.LENGTH_LONG).show();
        }

    }
    private void showExif(ExifInterface exif3) {//사진정보 뷰로 보여주기
        String myAttribute = "Exif";
        myAttribute += getTagString(ExifInterface.TAG_DATETIME, exif3);
       /* myAttribute += getTagString(ExifInterface.TAG_FLASH, exif3);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE, exif3);*/
        myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE_REF, exif3);
        // myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE, exif3);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE_REF, exif3);
        myAttribute += getTagString(ExifInterface.TAG_IMAGE_LENGTH, exif3);
        myAttribute += getTagString(ExifInterface.TAG_IMAGE_WIDTH, exif3);
        // myAttribute += getTagString(ExifInterface.TAG_MAKE, exif3);
        // myAttribute += getTagString(ExifInterface.TAG_MODEL, exif3);
        myAttribute += getTagString(ExifInterface.TAG_ORIENTATION, exif3);
        // myAttribute += getTagString(ExifInterface.TAG_WHITE_BALANCE, exif3);
        txtText.setText(myAttribute);
    }//여기까지 사진 정보 추출을 위한 클래스들

    private String getTagString(String tag, ExifInterface exif) {
        return (tag + " : " + exif.getAttribute(tag) + "\n");
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }
}

