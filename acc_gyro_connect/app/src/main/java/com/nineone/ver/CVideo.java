package com.nineone.ver;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.gun0912.tedpermission.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class CVideo extends AppCompatActivity implements SurfaceHolder.Callback {
    TextView daytime;
    Button bottom;
    String getday = null;
    String getfoldername = null;
    String getfolderRoute = null;
    String getusername = null;
    private Handler mHandler;
    private ProgressDialog mProgressDialog;
    // String vidos=Environment.getExternalStorageDirectory() + File.separator + "Nineone"+File.separator  ; // 저장 경로
    private VideoView videoView;
    private MediaController mediaController; // 재생이나 정지와 같은 미디어 제어 버튼부를 담당
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    MediaPlayer mediaPlayer;
    String vidos = null;
    Button start_stop_button, Pause_button;//button3;
    boolean isPlaying = false, isPlaying2 = false;
    SeekBar seekBar;
    TextView time, time2;
    double pos; // 재생 멈춘 시점
    boolean pause_restart = false;
    boolean start_stop = false;
    boolean timestart_stop = false;
    private Runnable mRunnable;
    //경로의 텍스트 파일읽기
    ArrayList<Float> x_dataList = new ArrayList<Float>();
    ArrayList<Float> y_dataList = new ArrayList<Float>();
    ArrayList<Float> z_dataList = new ArrayList<Float>();
    int duration2;
    private Button listback2;
    private TextView Folder, Username;
    private boolean seekstop=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Intent listClick = getIntent();//WalkList에서 정보를 받아오기
        getday = listClick.getExtras().getString("getday");
        getfoldername = listClick.getExtras().getString("getfoldername");
        getfolderRoute = listClick.getExtras().getString("getRoute");
        getusername = listClick.getExtras().getString("getuser");

        setContentView(R.layout.activity_video);
        ActionBar actionBar = getSupportActionBar();  //제목줄 객체 얻어오기
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getfoldername + "-" + getusername);
        mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        vidos = Environment.getExternalStorageDirectory() + File.separator + "Nineone" + File.separator + getfolderRoute + File.separator + getday + ".mp4"; // 저장 경로
        surfaceView = (SurfaceView) findViewById(R.id.vv);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(CVideo.this);
        mHandler = new Handler();
        time = findViewById(R.id.playerCurrentTimeText);
        // time2=findViewById(R.id.playerTotalTimeText);
        //Log.e("aaa",vidos+getday+"/"+getday+".mp4");
        seekBar = (SeekBar)findViewById(R.id.seekBar1);//SeekBar 추후 추가예정
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
               // isPlaying = true;
              //  pause_restart = false;
                int ttt = seekBar.getProgress(); // 사용자가 움직여놓은 위치
                mediaPlayer.seekTo(ttt-1);
                if(seekstop) {
                    isPlaying=true;
                    mediaPlayer.start();
                    new MyThread().start();
                }else{

                }
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
             //   isPlaying = false;
            //    pause_restart = true;
                if (seekstop) {
                    isPlaying=false;
                    mediaPlayer.pause();

                    //    }else{
                }

            }
            public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser) {
                if (seekBar.getMax()==progress) {
               //     isPlaying = false;
               //     pause_restart = true;
                    if (seekstop) {
                        mediaPlayer.pause();
                //    }else{

                    }

                }
            }
        });


        start_stop_button = (Button) findViewById(R.id.start_stop);
        start_stop_button.setOnClickListener(mClickListener);
        Pause_button = (Button) findViewById(R.id.Pause);
        Pause_button.setOnClickListener(mClickListener);
        //   button3 = (Button) findViewById(R.id.button3);
        start_stop_button.setVisibility(View.VISIBLE);
        Pause_button.setVisibility(View.INVISIBLE);

        mHandler = new Handler();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog = ProgressDialog.show(CVideo.this, "", "영상을 불러오는 중입니다.", true);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                i = 0;
                                mediaPlayer.setLooping(false);
                                initializeSeekBar();
                                playVideo();
                                start_stop = true;
                                isPlaying = true;
                                seekstop=true;
                                timestart_stop = true;
                                start_stop_button.setText("멈춤");
                                // start_stop_button.setVisibility(View.VISIBLE);
                                Pause_button.setVisibility(View.VISIBLE);
                                int a = mediaPlayer.getDuration(); // 노래의 재생시간(miliSecond)
                                seekBar.setMax(a);// 씨크바의 최대 범위를 노래의 재생시간으로 설정
                                new MyThread().start();
                                seekBar.setProgress(0);
                                mProgressDialog.dismiss();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 500);
            }
        });

    }

    private Button.OnClickListener mClickListener = new View.OnClickListener() {//각 버튼 클릭리스너
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.start_stop:
                    // TODO Auto-generated method stub
                    // 씨크바 그려줄 쓰레드 시작
                    if (!start_stop) {
                        i = 0;
                        mediaPlayer.setLooping(false);
                      //  initializeSeekBar();
                     //   playVideo();
                        start_stop = true;
                        isPlaying = true;
                        timestart_stop = true;
                        seekstop=true;
                        start_stop_button.setText("멈춤");
                        // start_stop_button.setVisibility(View.VISIBLE);
                        Pause_button.setVisibility(View.VISIBLE);
                     //   int a = mediaPlayer.getDuration(); // 노래의 재생시간(miliSecond)
                     //   seekBar.setMax(a);// 씨크바의 최대 범위를 노래의 재생시간으로 설정
                        mediaPlayer.start();
                        new MyThread().start();
                        //seekBar.setProgress(0);
                        //   button3.setVisibility(View.VISIBLE);
                    } else {
                        isPlaying = false; // 쓰레드 종료
                        pause_restart = false;
                        timestart_stop = false;
                        start_stop = false;
                        seekstop=false;
                        start_stop_button.setText("재생");
                        mediaPlayer.pause();
                        Pause_button.setVisibility(View.INVISIBLE);
                        i = 0;
                       // seekBar.setProgress(0);
                        mediaPlayer.seekTo(0);

                    }
                    break;
                case R.id.Pause:
                    // TODO Auto-generated method stub
                    // 일시중지
                    if (!pause_restart) {
                        Pause_button.setText("다시시작");

                        mediaPlayer.pause();
                        isPlaying = false;
                        pause_restart = true;
                        seekstop=false;
                        pos = mediaPlayer.getCurrentPosition();
                       // mediaPlayer.seekTo((int) pos); // 일시정지 시점으로 이동
                        Log.e("pos",pos+" , "+mediaPlayer.getCurrentPosition());
                    } else {
                        Pause_button.setText("일시정지");

                        // mediaPlayer.seekTo(pos);
                        isPlaying = true;
                        pause_restart = false;
                        seekstop=true;
                    //    mediaPlayer.seekTo((int) pos); // 일시정지 시점으로 이동
                        Log.e("pos2",pos+" , "+mediaPlayer.getCurrentPosition());
                        mediaPlayer.start();
                        new MyThread().start();
                    }
                    // 쓰레드 정지
                    break;
                case R.id.listbackbutton:
                    onBackPressed();
            }
        }
    };

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("리스트 이동");
        builder.setMessage("리스트로 이동하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isPlaying = false; // 쓰레드 종료
                pause_restart = false;
                timestart_stop = false;
                seekstop=false;
                mediaPlayer.pause();
                mediaPlayer.stop(); // 멈춤
                mediaPlayer.release(); // 자원 해제
                Pause_button.setVisibility(View.INVISIBLE);
                try {
                    sleep(1000);
                } catch (Exception ignored) {

                }
                finish();
            }
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }

    public void Backpressed() {

    }

    @Override
    protected void onStop() {
        super.onStop();
        isPlaying = false; // 쓰레드 종료
        pause_restart = false;
        timestart_stop = false;
        seekstop=false;
        Pause_button.setVisibility(View.INVISIBLE);
    }

    Float aFloat;


    int i = 0;

    class MyThread extends Thread {
        @Override
        public void run() { // 쓰레드가 시작되면 콜백되는 메서드
            //
            while (isPlaying) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());

            }
        }
    }
    protected void initializeSeekBar() {
        // sb.setMax(mediaPlayer.getDuration()/1000);

        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    //   int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000; // In milliseconds
                    //  sb.setProgress(mCurrentPosition);
                    if (timestart_stop) {
                        getAudioStats();
                    }
                }
                mHandler.postDelayed(mRunnable, 0);
            }
        };
        mHandler.postDelayed(mRunnable, 1000);
    }

    protected void getAudioStats() {
        // In milliseconds
        float due = mediaPlayer.getCurrentPosition();
        int duration = mediaPlayer.getCurrentPosition() / 1000;
        int duration2 = mediaPlayer.getDuration() / 1000;
        time.setText(duration + " / " + duration2);


    }

    PermissionListener permission = new PermissionListener() {
        @Override
        public void onPermissionGranted() {

        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {

        }


    };

    private void playVideo() {
        // 비디오를 처음부터 재생할땐 0
        mediaPlayer.seekTo(0);
        // 비디오 재생 시작
        mediaPlayer.start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        } else {
            mediaPlayer.reset();
        }
        try {
            mediaPlayer.setDataSource(vidos);
            mediaPlayer.setVolume(0, 0); //볼륨 제거
            mediaPlayer.setDisplay(surfaceHolder); // 화면 호출
            mediaPlayer.prepare(); // 비디오 load 준비
            duration2 = mediaPlayer.getDuration();
            time.setText(0 + " / " + duration2 / 1000);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    //여기다 완료 후 코드 작성
                    isPlaying = false; // 쓰레드 종료
                    pause_restart = false;
                    timestart_stop = false;

                    //timestart_stop = false;
                    start_stop = false;
                    mediaPlayer.seekTo(0);
                    i = 0;
                    start_stop_button.setText("재생");
                    mediaPlayer.pause();
                    //    start_stop_button.setVisibility(View.VISIBLE);
                    Pause_button.setVisibility(View.INVISIBLE);
                    // button3.setVisibility(View.INVISIBLE);
                   // thread.interrupt();

                }
            });
            //  mediaPlayer.setOnCompletionListener(completionListener); // 비디오 재생 완료 리스너

            //  mediaPlayer.start();

        } catch (Exception e) {
            Log.e("MyTag", "surface view error : " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chartmanu, menu);
        menu.findItem(R.id.listback).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.listback:
            case android.R.id.home://뒤로가기를 누르면 홈으로 돌아간다
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}