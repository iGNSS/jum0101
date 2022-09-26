package com.nineone.ver;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.gun0912.tedpermission.PermissionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class ChartVideo extends AppCompatActivity implements SurfaceHolder.Callback{
    TextView daytime;
    String saveFile;
    Button bottom;
    String getcsv=null;
    String getday=null;
    String getfoldername=null;
    String getfolderRoute=null;
    String getusername=null;
    private LineChart chart4;
    private Handler mHandler;
    private ProgressDialog mProgressDialog;
    // String vidos=Environment.getExternalStorageDirectory() + File.separator + "Nineone"+File.separator  ; // 저장 경로
    private VideoView videoView;
    private MediaController mediaController; // 재생이나 정지와 같은 미디어 제어 버튼부를 담당
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    MediaPlayer mediaPlayer;
    String vidos= null;
    Button button1, button2;//button3;
    boolean isPlaying = false,isPlaying2=false;
    SeekBar seekBar;
    TextView time,time2;
    int pos; // 재생 멈춘 시점
    boolean pause_restart=false;
    boolean start_stop=false;
    boolean timestart_stop=false;
    boolean chartstop=false;
    private Runnable mRunnable;
    private Thread thread;
    //경로의 텍스트 파일읽기
    ArrayList<Float> x_dataList=new ArrayList<Float>();
    ArrayList<Float> y_dataList=new ArrayList<Float>();
    ArrayList<Float> z_dataList=new ArrayList<Float>();
    int duration2;
    private Button listback2;
    private TextView Folder,Username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent listClick = getIntent();//WalkList에서 정보를 받아오기
        getcsv = listClick.getExtras().getString("getCSV");//csv파일 경로
        getday = listClick.getExtras().getString("getday");
        getfoldername = listClick.getExtras().getString("getfoldername");
        getfolderRoute = listClick.getExtras().getString("getRoute");
        getusername=listClick.getExtras().getString("getuser");
        if (getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_chart_video);
            ActionBar actionBar = getSupportActionBar();  //제목줄 객체 얻어오기
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getfoldername+"-"+getusername);
        } else {
            setContentView(R.layout.activity_chart_video_horizontal);
            ActionBar actionBar = getSupportActionBar();  //제목줄 객체 얻어오기
            actionBar.hide();
            Folder=(TextView)findViewById(R.id.Filename);
            Username=(TextView)findViewById(R.id.Username);
            Folder.setText(getfoldername);
            Username.setText(getusername);
            listback2=(Button)findViewById(R.id.listbackbutton);
            listback2.setOnClickListener(mClickListener);
        }

        mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        vidos= Environment.getExternalStorageDirectory() + File.separator + "Nineone"+File.separator+getfolderRoute+File.separator+getday+".mp4" ; // 저장 경로
        surfaceView = (SurfaceView) findViewById(R.id.vv);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(ChartVideo.this);
        mHandler = new Handler();
        time=findViewById(R.id.playerCurrentTimeText);
        // time2=findViewById(R.id.playerTotalTimeText);
        //Log.e("aaa",vidos+getday+"/"+getday+".mp4");
       /* seekBar = (SeekBar)findViewById(R.id.seekBar1);//SeekBar 추후 추가예정
       seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){

                    return false;
                }
                return true;

            }
        });*/
        saveFile = getcsv ; // 저장 경로
        Chart_Line();

        button1 = (Button) findViewById(R.id.start_stop);
        button1.setOnClickListener(mClickListener);
        button2 = (Button) findViewById(R.id.Pause);
        button2.setOnClickListener(mClickListener);
        //   button3 = (Button) findViewById(R.id.button3);
        button1.setVisibility(View.VISIBLE);
        button2.setVisibility(View.INVISIBLE);

        mHandler = new Handler();
        runOnUiThread(new Runnable() {
            @Override public void run() {
                mProgressDialog = ProgressDialog.show(ChartVideo.this,"", "영상을 불러오는 중입니다.",true);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ReadTextFile(saveFile);
                            if (mProgressDialog!=null&&mProgressDialog.isShowing()){
                                aaa();
                                mProgressDialog.dismiss();
                            }
                        } catch ( Exception e ) {
                            e.printStackTrace();
                        }
                    }
                }, 100);
            }
        } );

    }
    private Button.OnClickListener mClickListener = new View.OnClickListener() {//각 버튼 클릭리스너
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.start_stop:
                    // TODO Auto-generated method stub
                    // 씨크바 그려줄 쓰레드 시작
                    if(!start_stop) {
                        chart4.clearValues();
                        i = 0;
                        mediaPlayer.setLooping(false);
                        initializeSeekBar();
                        playVideo();
                        // new MyThread().start();
                        feedMultiple();
                        // new MyThread2().start();
                        start_stop = true;
                        isPlaying = true;
                        timestart_stop = true;
                        button1.setText("멈춤");
                        // button1.setVisibility(View.VISIBLE);
                        button2.setVisibility(View.VISIBLE);

                        //   button3.setVisibility(View.VISIBLE);
                    }else{
                        isPlaying = false; // 쓰레드 종료
                        pause_restart=false;
                        chartstop=false;
                        timestart_stop = false;
                        start_stop = false;
                        button1.setText("재생");
                        mediaPlayer.pause();
                        button2.setVisibility(View.INVISIBLE);
                        //  chart4.clearValues();
                        i=0;
                        thread.interrupt();
                        isPlaying = false;
                    }
                    break;
                case R.id.Pause:
                    // TODO Auto-generated method stub
                    // 일시중지
                    if(!pause_restart) {
                        button2.setText("다시시작");
                        // pos = mediaPlayer.getCurrentPosition();
                        mediaPlayer.pause();
                        isPlaying = false;
                        pause_restart=true;
                        chartstop=true;
                    }else {
                        button2.setText("일시정지");
                        // mediaPlayer.seekTo((int) pos); // 일시정지 시점으로 이동
                        // mediaPlayer.seekTo(pos);
                        isPlaying = true;
                        pause_restart=false;
                        chartstop=false;
                        mediaPlayer.start();
                        // new MyThread().start();
                    }
                    // 쓰레드 정지
                    break;
                case R.id.listbackbutton:
                    onBackPressed();
            }
        }
    };
    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("리스트 이동");
        builder.setMessage("리스트로 이동하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isPlaying = false; // 쓰레드 종료
                pause_restart=false;
                timestart_stop=false;
                mediaPlayer.pause();
                mediaPlayer.stop(); // 멈춤
                mediaPlayer.release(); // 자원 해제
                button2.setVisibility(View.INVISIBLE);
                chart4.clear();
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
    public void Backpressed(){

    }
    @Override
    protected void onStop() {
        super.onStop();
        isPlaying = false; // 쓰레드 종료
        pause_restart=false;
        timestart_stop=false;
        button2.setVisibility(View.INVISIBLE);
        chart4.clear();
        //비디오 일시 정지
    }
    Float aFloat;
    public void ReadTextFile(String path) {
        StringBuffer strBuffer = new StringBuffer();
        try {
            InputStream is = new FileInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = "";
            int row=0;
            int i;
            while ((line = reader.readLine()) != null) {
                String[] token=line.split("\\,",-1);
                x_dataList.add(row,Float.parseFloat(token[1]));
                y_dataList.add(row,Float.parseFloat(token[2]));
                z_dataList.add(row,Float.parseFloat(token[3]));
                row++;
                //strBuffer.append(line + "\n");
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
        //return strBuffer.toString();
    }
    public void Chart_Line() {

        chart4 = (LineChart) findViewById(R.id.LineChart4);

        chart4.setDrawGridBackground(true);
        chart4.setBackgroundColor(Color.WHITE);
        chart4.setGridBackgroundColor(Color.WHITE);
        chart4.getDescription().setEnabled(true);
        chart4.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart4.animateXY(1000, 1000);
        chart4.getXAxis().setGranularity(500);
        chart4.getXAxis().setGranularityEnabled (true);

        chart4.invalidate();
        LineData data = new LineData();
        chart4.setData(data);


    }
    private void addEntry4(float num,float num2,float num3) {
        LineData data = chart4.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            ILineDataSet set2 = data.getDataSetByIndex(1);
            ILineDataSet set3 = data.getDataSetByIndex(2);
            if (set == null) {
                set = createSet1();
                data.addDataSet(set);
            }
            if (set2 == null) {
                set2 = createSet2();
                data.addDataSet(set2);
            }
            if (set3 == null) {
                set3 = createSet3();
                data.addDataSet(set3);
            }
            data.addEntry(new Entry(set.getEntryCount(), num), 0);
            data.addEntry(new Entry(set2.getEntryCount(), num2), 1);
            data.addEntry(new Entry(set3.getEntryCount(), num3), 2);
            data.notifyDataChanged();
            chart4.notifyDataSetChanged();
            // data.clearValues();
            //  chart4.setVisibleXRangeMinimum(x_dataList.size()-1);
            chart4.setVisibleXRangeMaximum(x_dataList.size());
            chart4.moveViewToX(data.getEntryCount());
            chart4.getXAxis().setAxisMaxValue(x_dataList.size());
            chart4.getXAxis().setAxisMinValue(0);
            chart4.getXAxis().setLabelCount(6);

        }
    }
    int i=0;
    int aaa=x_dataList.size()/1000;
    public void aaa(){
        if(x_dataList.size()>=7800){
            aaa=7;
        }else if(x_dataList.size()>=6800){
            aaa=9;
        }else if(x_dataList.size()>=5800){
            aaa=10;
        }else if(x_dataList.size()>=4800){
            aaa=11;
        }else if(x_dataList.size()>=4800){
            aaa=18;
        }
    }
    private void feedMultiple() {
        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(x_dataList.size()!=i) {
                    addEntry4(x_dataList.get(i),y_dataList.get(i),z_dataList.get(i));
                    i++;
                }
            }
        };
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        while (!chartstop) {
                            runOnUiThread(runnable);
                            Thread.sleep(aaa);
                        }
                    }
                }catch (InterruptedException ie) {
                    ie.printStackTrace();

                }
            }

        });
        thread.start();
    }
    /*    class MyThread extends Thread {
            @Override
            public void run() { // 쓰레드가 시작되면 콜백되는 메서드
                //
                while (isPlaying) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());

                }
            }
        }*/
    private LineDataSet createSet1() {
        LineDataSet set = new LineDataSet(null, "x");
        set.setFillAlpha(110);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setFillColor(Color.parseColor("#d7e7fa"));
        set.setColor(Color.parseColor("#0B80C9"));
        set.setCircleColor(Color.parseColor("#FFA1B4DC"));
        set.setCircleHoleColor(Color.BLUE);
        set.setValueTextColor(Color.WHITE);
        set.setDrawValues(false);
        set.setLineWidth(2);
        set.setCircleRadius(6);
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setValueTextSize(9f);
        // set.setDrawFilled(true);
        set.setHighLightColor(Color.rgb(244, 117, 117));

        return set;
    }
    private LineDataSet createSet2() {
        LineDataSet set = new LineDataSet(null, "y");
        set.setFillAlpha(110);
        set.setFillColor(Color.parseColor("#ae1932"));
        set.setColor(Color.parseColor("#ae1932"));
        set.setCircleColor(Color.parseColor("#FFA1B4DC"));
        set.setCircleHoleColor(Color.BLUE);
        set.setValueTextColor(Color.WHITE);
        set.setDrawValues(false);
        set.setLineWidth(2);
        set.setCircleRadius(6);
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setValueTextSize(9f);
        //set.setDrawFilled(true);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        return set;
    }
    private LineDataSet createSet3() {
        LineDataSet set = new LineDataSet(null, "y");
        set.setFillAlpha(110);
        set.setFillColor(Color.parseColor("#09ce20"));
        set.setColor(Color.parseColor("#09ce20"));
        set.setCircleColor(Color.parseColor("#FFA1B4DC"));
        set.setCircleHoleColor(Color.BLUE);
        set.setValueTextColor(Color.WHITE);
        set.setDrawValues(false);
        set.setLineWidth(2);
        set.setCircleRadius(6);
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setValueTextSize(9f);
        //set.setDrawFilled(true);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        return set;
    }
    protected void initializeSeekBar(){
        // sb.setMax(mediaPlayer.getDuration()/1000);

        mRunnable = new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer!=null){
                    //   int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000; // In milliseconds
                    //  sb.setProgress(mCurrentPosition);
                    if(timestart_stop) {
                        getAudioStats();
                    }
                }
                mHandler.postDelayed(mRunnable,0);
            }
        };
        mHandler.postDelayed(mRunnable,1000);
    }

    protected void getAudioStats(){
        // In milliseconds
        float due = mediaPlayer.getCurrentPosition();
        int duration  = mediaPlayer.getCurrentPosition()/1000;
        int duration2  = mediaPlayer.getDuration()/1000;
        time.setText(duration+" / "+duration2);


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
            duration2= mediaPlayer.getDuration();
            time.setText(0+" / "+ duration2/1000);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    //여기다 완료 후 코드 작성
                    isPlaying = false; // 쓰레드 종료
                    pause_restart=false;
                    //chartstop=false;
                    //timestart_stop = false;
                    start_stop = false;
                    mediaPlayer.seekTo(0);
                    button1.setText("재생");
                    //    button1.setVisibility(View.VISIBLE);
                    button2.setVisibility(View.INVISIBLE);
                    // button3.setVisibility(View.INVISIBLE);
                    // thread.interrupt();

                }
            });
            //  mediaPlayer.setOnCompletionListener(completionListener); // 비디오 재생 완료 리스너

            //  mediaPlayer.start();

        } catch (Exception e) {
            Log.e("MyTag","surface view error : " + e.getMessage());
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