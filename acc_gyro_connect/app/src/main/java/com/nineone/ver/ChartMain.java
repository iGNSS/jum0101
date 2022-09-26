package com.nineone.ver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.internal.view.SupportMenu;
import androidx.core.view.ViewCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.gun0912.tedpermission.PermissionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ChartMain extends AppCompatActivity {
    TextView daytime;
    String saveFile;
    Button bottom;
    String getday=null;
    String [][] date=new String[10000][1000];
    private LineChart chart1;//그래프 함수들
    private LineChart chart2;
    private LineChart chart3;
    String getfoldername = null;
    String getfolderRoute = null;
    String getusername = null;
    private Handler mHandler;
    private ProgressDialog mProgressDialog;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Intent listClick = getIntent();//WalkList에서 정보를 받아오기
        getday = listClick.getExtras().getString("getCSV");//csv파일경로
        getfoldername = listClick.getExtras().getString("getfoldername");
        getfolderRoute = listClick.getExtras().getString("getRoute");
        getusername = listClick.getExtras().getString("getuser");
        ActionBar actionBar = getSupportActionBar();  //제목줄 객체 얻어오기
        actionBar.setDisplayHomeAsUpEnabled(true);
        textView=findViewById(R.id.textview5);

      //  saveFile = Environment.getExternalStorageDirectory() + File.separator + "Nineone" + File.separator + getday + File.separator + getday+".csv"; // 저장 경로
       // daytime = findViewById(R.id.daytime);
        //daytime.setText(getday);
        saveFile = getday ; // 저장 경로

        Chart_Line();
        mHandler = new Handler();
        runOnUiThread(new Runnable() {
            @Override public void run() {
                mProgressDialog = ProgressDialog.show(ChartMain.this,"", "그래프를 생성중입니다.",true);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ReadTextFile(saveFile);
                            actionBar.setTitle(getfoldername + "-" + getusername);
                            if (mProgressDialog!=null&&mProgressDialog.isShowing()){
                                mProgressDialog.dismiss();
                            }
                        } catch ( Exception e ) {
                            e.printStackTrace();
                        }
                    }
                }, 100);
            }
        } );

        //   String strFolderName = Environment.getExternalStorageDirectory() + File.separator + "Nineone" + File.separator + "20210514_163443" + File.separator;//폴더이름
// 폴더 생성
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
                onBackPressed(); //블루투스 센서 연결
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //경로의 텍스트 파일읽기
    ArrayList<Float> x_dataList=new ArrayList<Float>();
    ArrayList<Float> y_dataList=new ArrayList<Float>();
    ArrayList<Float> z_dataList=new ArrayList<Float>();
    String name=null;
    String gender=null;
    String age=null;
 //   float[] x_data = new float[10000];
 //   float[] y_data = new float[10000];
 //   float[] z_data = new float[10000];
    public void ReadTextFile(String path) {
        StringBuffer strBuffer = new StringBuffer();
        try {
            FileInputStream is = new FileInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "EUC-KR"));
           // BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = "";
            int row=0;
            int dow=0;
            int i;
            Log.e("aaa", "line");
            while ((line = reader.readLine()) != null) {
                String[] token = line.split("\\,", -1);
                if(dow>1) {
                    Log.e("aaa2", String.valueOf(row));
                    x_dataList.add(row, Float.parseFloat(token[5]));
                    Log.e("aaa3", String.valueOf(row));
                    y_dataList.add(row, Float.parseFloat(token[6]));
                    z_dataList.add(row, Float.parseFloat(token[7]));

                    //   x_data[row] = Float.parseFloat(token[1]);
                    //  y_data[row] = Float.parseFloat(token[2]);
                    //  z_data[row] = Float.parseFloat(token[3]);
                    Log.e("aaa", String.valueOf(row));
                    row++;
                }
                if(dow==0) {
                    name=token[1];
                    age=token[3];
                    gender=token[5];
                }
                dow++;
                //strBuffer.append(line + "\n");
            }
            textView.setText(name+"-"+age+"-"+gender);
            Log.e("bbb", "line");
            reader.close();
            is.close();
            addEntry1(x_dataList);
            addEntry2(y_dataList);
            addEntry3(z_dataList);
            //Chat 그리기
        //    addEntry1(x_data);
        //    addEntry2(y_data);
        //    addEntry3(z_data);
        } catch (IOException e) {
            e.printStackTrace();

        }
        //return strBuffer.toString();
    }
    public void Chart_Line() {
        chart1 = (LineChart) findViewById(R.id.LineChart1);
        chart2 = (LineChart) findViewById(R.id.LineChart2);
        chart3 = (LineChart) findViewById(R.id.LineChart3);
        chart1.setDrawGridBackground(true);
        chart1.setBackgroundColor(Color.WHITE);
        chart1.setGridBackgroundColor(Color.WHITE);
        chart1.getDescription().setEnabled(true);
        chart2.setDrawGridBackground(true);
        chart2.setBackgroundColor(Color.WHITE);
        chart2.setGridBackgroundColor(Color.WHITE);
        chart2.getDescription().setEnabled(true);
        chart3.setDrawGridBackground(true);
        chart3.setBackgroundColor(Color.WHITE);
        chart3.setGridBackgroundColor(Color.WHITE);
        chart3.getDescription().setEnabled(true);

    }
    private void addEntry1(ArrayList<Float> chartdata) {
        LineData data = chart1.getData();//차트1에 넣어줄 데이터 생성
        if (data == null) {//data가 비어있으면
            data = new LineData();//라이브러리에서 라인차트 데이터를 받아와 Data에 넣어준다 준다
            chart1.setData(data);//차트원에 데이터를 넣어준다
        }
        ILineDataSet set1 = data.getDataSetByIndex(0);//그래프에 출력할 선디자인 생성


        if (set1 == null) { //createSet1에 데이터를 받아 첫번째 선 디자인 넣어주기
            set1 = createSet1();
            data.addDataSet(set1);
        }

        for(int i =0; i < chartdata.size();i++){
            if(chartdata.get(i) != 0.0){
                data.addEntry(new Entry((float)set1.getEntryCount(), chartdata.get(i)), 0);//첫번째 선 생성 x좌표는 시간 y좌표는 임의의 값
            }
        }

        data.notifyDataChanged();

        chart1.notifyDataSetChanged();//리스트뷰를 수정해주는 notifyDataSetChanged함수를 사용 chart1이 150까지 출력되면
        // chart1.setVisibleXRangeMaximum(9000);//x좌표는 150까지만 출력
        // 차트를 자동으로 새로 고침
        chart1.moveViewTo(data.getEntryCount(), 50f, YAxis.AxisDependency.LEFT);//X축이 다음 지정된 x축으로 이동하면서 지정된 y축으로 뷰포인트가 맞춰진다.(x=시간,y= 센서값
    }
    /*private void addEntry1(float[] chartdata) {
        LineData data = chart1.getData();//차트1에 넣어줄 데이터 생성
        if (data == null) {//data가 비어있으면
            data = new LineData();//라이브러리에서 라인차트 데이터를 받아와 Data에 넣어준다 준다
            chart1.setData(data);//차트원에 데이터를 넣어준다
        }
        ILineDataSet set1 = data.getDataSetByIndex(0);//그래프에 출력할 선디자인 생성


        if (set1 == null) { //createSet1에 데이터를 받아 첫번째 선 디자인 넣어주기
            set1 = createSet1();
            data.addDataSet(set1);
        }

        for(int i =0; i < chartdata.length;i++){
            if(chartdata[i] != 0.0){
                data.addEntry(new Entry((float)set1.getEntryCount(), chartdata[i]), 0);//첫번째 선 생성 x좌표는 시간 y좌표는 임의의 값
            }
        }

        data.notifyDataChanged();

        chart1.notifyDataSetChanged();//리스트뷰를 수정해주는 notifyDataSetChanged함수를 사용 chart1이 150까지 출력되면
        // chart1.setVisibleXRangeMaximum(9000);//x좌표는 150까지만 출력
        // 차트를 자동으로 새로 고침
        chart1.moveViewTo(data.getEntryCount(), 50f, YAxis.AxisDependency.LEFT);//X축이 다음 지정된 x축으로 이동하면서 지정된 y축으로 뷰포인트가 맞춰진다.(x=시간,y= 센서값
    }             */                                                                      //AxisDependency: DataSet)이 그려 질 축을 왼쪽 또는 오른쪽으로 지정하는 열거 형.

    private void addEntry2(ArrayList<Float>  chartdata) {
        LineData data = chart2.getData();
        if (data == null) {
            data = new LineData();
            chart2.setData(data);
        }
        ILineDataSet set1 = data.getDataSetByIndex(0);
        if (set1 == null) {
            set1 = createSet2();
            data.addDataSet(set1);
        }
        for(int i =0; i < chartdata.size();i++){
            if(chartdata.get(i) != 0.0){
                data.addEntry(new Entry((float)set1.getEntryCount(), chartdata.get(i)), 0);//첫번째 선 생성 x좌표는 시간 y좌표는 임의의 값
            }
        }
        data.notifyDataChanged();
        // let the chart know it's data has changed
        chart2.notifyDataSetChanged();
        //    chart2.setVisibleXRangeMaximum(50);
        // this automatically refreshes the chart (calls invalidate())
        chart2.moveViewTo(data.getEntryCount(), 50f, YAxis.AxisDependency.LEFT);
    }
    private void addEntry3(ArrayList<Float>  chartdata) {
        LineData data = chart3.getData();
        if (data == null) {
            data = new LineData();
            chart3.setData(data);
        }
        ILineDataSet set1 = data.getDataSetByIndex(0);
        if (set1 == null) {
            set1 = createSet3();
            data.addDataSet(set1);
        }
        for(int i =0; i < chartdata.size();i++){
            if(chartdata.get(i) != 0.0){
                data.addEntry(new Entry((float)set1.getEntryCount(), chartdata.get(i)), 0);//첫번째 선 생성 x좌표는 시간 y좌표는 임의의 값
            }
        }
        data.notifyDataChanged();
        // let the chart know it's data has changed
        chart3.notifyDataSetChanged();
        //  chart3.setVisibleXRangeMaximum(50);
        // this automatically refreshes the chart (calls invalidate())
        chart3.moveViewTo(data.getEntryCount(), 50f, YAxis.AxisDependency.LEFT);
    }
    private LineDataSet createSet1() {//x축 선의 디자인 데이터
        LineDataSet set = new LineDataSet((List<Entry>) null, "X");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(SupportMenu.CATEGORY_MASK);
        set.setDrawCircles(false);
        set.setDrawCircleHole(false);
        set.setLineWidth(1.0f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(ViewCompat.MEASURED_STATE_MASK);
        set.setValueTextSize(9.0f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSet2() {
        LineDataSet set = new LineDataSet((List<Entry>) null, "Y");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(-16711936);
        set.setDrawCircles(false);
        set.setDrawCircleHole(false);
        set.setLineWidth(1.0f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(ViewCompat.MEASURED_STATE_MASK);
        set.setValueTextSize(9.0f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSet3() {
        LineDataSet set = new LineDataSet((List<Entry>) null, "Z");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(-16776961);
        set.setDrawCircles(false);
        set.setDrawCircleHole(false);
        set.setLineWidth(1.0f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(ViewCompat.MEASURED_STATE_MASK);
        set.setValueTextSize(9.0f);
        set.setDrawValues(false);
        return set;
    }

}

