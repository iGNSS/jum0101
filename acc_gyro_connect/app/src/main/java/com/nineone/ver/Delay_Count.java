package com.nineone.ver;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.TextView;

public class Delay_Count extends Activity { //AlbumClik에 상세정보를 띄워주는 팝업 클래스

    private TextView txtText;
    MyTimer myTimer;
    Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.delay_count);

        //UI 객체생성
        txtText = (TextView) findViewById(R.id.delay_count);

        //데이터 가져오기
        Intent intent = getIntent();
        myTimer = new MyTimer(3000, 1000);
        myTimer.start();
    }



    class MyTimer extends CountDownTimer
    {
        public MyTimer(long millisInFuture, long countDownInterval)
        {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            txtText.setText(millisUntilFinished/1000 + " 초");
        }

        @Override
        public void onFinish() {

            finish();;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
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
