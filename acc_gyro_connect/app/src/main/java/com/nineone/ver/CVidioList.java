package com.nineone.ver;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class    CVidioList extends AppCompatActivity {//걷기 기록들을 리사이클 뷰로 나타내는곳
    private RecyclerView recyclerView;
    private ChartListAdapter listAdapter;
    DateFormat dateFormat;
    SimpleDateFormat formatter;
    Date curDate;
    Date[] endDate= new Date[500];
    String[] day= new String[500];
    Date filecsv;
    String fileday;
    String saveMainFile = Environment.getExternalStorageDirectory() + File.separator + "Nineone" ; // 저장 경로
    String saveNameFile ;
    GridLayoutManager gridLayoutManager;
    Button deletlist;
    CheckBox Chk_All;
    ImageButton backmain,folderback;
    ArrayList<ChartListData> filesMainList = new ArrayList<>();
    File Maindirectory;
    File[] Mainfiles;
    ArrayList<ChartListData> filesNameList = new ArrayList<ChartListData>();
    File Namedirectory;
    File[] Namefiles;
    ChartListData userlist;
    TextView foldername;
    int page=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_video_list);
        ActionBar actionBar = getSupportActionBar();  //제목줄 객체 얻어오기
        // actionBar.setTitle("차트 리스트");  //액션바 제목설정
        //actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.hide();

        // Chk_All=findViewById(R.id.chk_select_all);
        //  Chk_All.setOnClickListener(mClickListener);
        //  deletlist=findViewById(R.id.Deletlist);
        //  deletlist.setOnClickListener(mClickListener);
        foldername=findViewById(R.id.foldername);
        backmain=findViewById(R.id.backMain);
        backmain.setOnClickListener(mClickListener);
        folderback=findViewById(R.id.FolderBack);
        folderback.setOnClickListener(mClickListener);
        foldername.setVisibility(View.INVISIBLE);
        folderback.setVisibility(View.INVISIBLE);

        dateFormat = new SimpleDateFormat ("yyyyMMdd_HHmmss", Locale.getDefault());
        formatter = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분 ss초", Locale.getDefault());
        MainFolder();
        MainRecyclerView();
        MainRecyclerViewClick();

    }
    public void MainFolder(){
        filesMainList = new ArrayList<>();
        Maindirectory = new File(saveMainFile);
        Mainfiles=null;
        Mainfiles = Maindirectory.listFiles();
        for (int i = 0; i < Mainfiles.length; i++) {
            File Namedirectory2= new File(saveMainFile + File.separator + Mainfiles[i].getName());
            File[] Namefiles2=null;
            Namefiles2=Namedirectory2.listFiles();
            filesMainList.add(new ChartListData(Mainfiles[i].getName(),String.valueOf(Namefiles2.length),false));
        }
        Collections.sort(filesMainList,cmpAsc);
    }
    public static Comparator<ChartListData> cmpAsc = new Comparator<ChartListData>() {
        @Override
        public int compare(ChartListData o1, ChartListData o2) {
            return o1.getName().compareTo(o2.getName());
        }

    };
    public void MainRecyclerView(){

        recyclerView = findViewById(R.id.recyclerview);//리사이클 뷰 연결
        recyclerView.clearOnChildAttachStateChangeListeners();
        listAdapter=new ChartListAdapter(this,filesMainList, Chk_All);
        page=0;
        if (getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(listAdapter);//리사이클뷰를 MyAdapterdp 연결
        } else {
            gridLayoutManager = new GridLayoutManager(this, 2);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setAdapter(listAdapter);
        } // end if
    }
    public void  MainRecyclerViewClick() {
        listAdapter.notifyDataSetChanged();//lll
        listAdapter.setOnItemClickListener(new ChartListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {//리스트를 클릭했을때 이벤트
                userlist = filesMainList.get(position);
                saveNameFile=Environment.getExternalStorageDirectory() + File.separator + "Nineone"+File.separator+userlist.getName();
                foldername.setVisibility(View.VISIBLE);
                foldername.setText(userlist.getName());
                Log.e("aaa",userlist.getName()+"-"+saveNameFile);
                filesNameList = new ArrayList<>();
                Namedirectory = new File(saveNameFile);
                Namefiles=null;
                Namefiles = Namedirectory.listFiles();
                nameFolder();
                nameRecyclerView();
                nameRecyclerViewClick();
                folderback.setVisibility(View.VISIBLE);
            }
        });
    }
    public void nameFolder(){

        for (int i = 0; i < Namefiles.length; i++) {
            try {
                endDate[i] = dateFormat.parse(Namefiles[i].getName());
                day[i] = formatter.format(endDate[i]);
                filesNameList.add(new ChartListData(day[i],"",false));

                Collections.reverse(filesNameList);
                // filesNameList.add(new ChartListData(day[i],2));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(filesNameList,cmpAsc2);
    }
    public static Comparator<ChartListData> cmpAsc2 = new Comparator<ChartListData>() {
        @Override
        public int compare(ChartListData o1, ChartListData o2) {
            return o2.getName().compareTo(o1.getName());
        }

    };

    public void nameRecyclerView(){
        recyclerView = findViewById(R.id.recyclerview);//리사이클 뷰 연결
        listAdapter=new ChartListAdapter(this,filesNameList,Chk_All);
        page=1;
        if (getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(listAdapter);//리사이클뷰를 MyAdapterdp 연결
        } else {
            gridLayoutManager = new GridLayoutManager(this, 2);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setAdapter(listAdapter);
        } // end if
    }
    public void nameRecyclerViewClick(){
        listAdapter.notifyDataSetChanged();//lll
        listAdapter.setOnItemClickListener(new ChartListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {//리스트를 클릭했을때 이벤트
                ChartListData namedatalist = filesNameList.get(position);
                String csvRoute = null;
                try {
                    filecsv = formatter.parse(namedatalist.getName());
                    fileday = dateFormat.format(filecsv);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String path=Environment.getExternalStorageDirectory() + File.separator + "Nineone" + File.separator +userlist.getName()+ File.separator + fileday;
                String FolderRoute=userlist.getName()+ File.separator + fileday;
                File f = new File(path);
                    Intent listClick = new Intent(getApplicationContext(), CVideo.class);//클릭시 ListClick으로 이동 및 정보 전달
                    listClick.putExtra("getday",fileday );
                    listClick.putExtra("getRoute",FolderRoute );
                    listClick.putExtra("getfoldername",namedatalist.getName());
                    listClick.putExtra("getuser",userlist.getName() );
                    startActivity(listClick);

                //String mynamedatalist = myAdapter.getWordAtPosition(position);
                // TODO : 아이템 클릭 이벤트를 MainActivity에서 처리.
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // 메뉴 아이템 버튼을 클릭했을 때 이벤트
        if (item.getItemId() == android.R.id.home) {//뒤로가기를 누르면 홈으로 돌아간다
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Button.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.chk_select_all:

                    break;
                //case R.id.Deletlist:

                //    break;
                case R.id.backMain:
                    finish();
                    break;
                case R.id.FolderBack:
                    MainFolder();
                    MainRecyclerView();
                    MainRecyclerViewClick();
                    foldername.setVisibility(View.INVISIBLE);
                    folderback.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };
    public void onBackPressed() {
        if (page==0) {
            finish();
        } else {
            MainFolder();
            MainRecyclerView();
            MainRecyclerViewClick();
            foldername.setVisibility(View.INVISIBLE);
            folderback.setVisibility(View.INVISIBLE);
        }
    }
    public void delete(){

    }


}