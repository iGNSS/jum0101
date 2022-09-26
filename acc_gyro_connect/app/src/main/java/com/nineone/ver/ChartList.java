package com.nineone.ver;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.C;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChartList  extends AppCompatActivity {//걷기 기록들을 리사이클 뷰로 나타내는곳
    private RecyclerView recyclerView;
    private ChartListAdapter listAdapter;
    private ChartListData chartListData;
    DateFormat dateFormat;//날짜파일폴더 원본날짜형식
    SimpleDateFormat formatter;//어플 리스트에 보여질 변환날짜형식
    Date curDate;
    Date[] endDate = new Date[500];//날짜 폴더 저장
    String[] day = new String[500];//formatter로 변환된 날짜 폴더 저장
    Date filefolderday,userfoldername;//
    String fileday,fileday2;
    String saveMainFolder = Environment.getExternalStorageDirectory() + File.separator + "Nineone"; // 메인 폴더 저장 경로
    String saveNameFolder;//이름 폴더 경로
    GridLayoutManager gridLayoutManager;//리사이클뷰를 그리드뷰처럼 보기 위한 함수
    CheckBox deletlist;
    public CheckBox Chk_All;//리스트 전체 체크,해제
    ImageButton backmain, folderback;//메인으로 가기 버튼과 폴더에서 뒤로가기 버튼
    ArrayList<ChartListData> filesMainList = new ArrayList<>();//메인 폴더 리스트 저장
    File Maindirectory;//메인디렉토리 파일 이름 추출을 위한 변수
    File[] Mainfiles;
    ArrayList<ChartListData> filesNameList = new ArrayList<ChartListData>();//이름 폴더 리스트 저장
    File Namedirectory;
    File[] Namefiles;//이름폴더 내부의 파일이름을 추출하기 위한 변수
    ChartListData userlist;//체크된 메인 폴더 반환
    TextView foldername;
    ImageButton deletstart, sendstart;
    Button cancelbutton, delet_mode;
    Button send_mode;
    int page = 0;
    boolean NowMainFolder = true;
    Date filechange;
    String filechangefinsh;
    int deletback = 0;
    Socket socket;
    String androidid;
    TCP_Client data_send_tcp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_chart_list);
        ActionBar actionBar = getSupportActionBar();  //제목줄 객체 얻어오기
        // actionBar.setTitle("차트 리스트");  //액션바 제목설정
        //actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.hide();
        androidid=Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        //   Chk_All=findViewById(R.id.chk_select_all);
        //  Chk_All.setOnClickListener(mClickListener);
        deletstart = findViewById(R.id.chk_delet);
        deletstart.setOnClickListener(mClickListener);
        Chk_All = findViewById(R.id.chk_select_all);
        Chk_All.setOnClickListener(mClickListener);
        cancelbutton = findViewById(R.id.chk_delet_cancel);
        cancelbutton.setOnClickListener(mClickListener);
        delet_mode = findViewById(R.id.chk_delet_mode);
        delet_mode.setOnClickListener(mClickListener);
        sendstart = findViewById(R.id.chk_send);
        sendstart.setOnClickListener(mClickListener);
        send_mode = findViewById(R.id.chk_send_mode);
        send_mode.setOnClickListener(mClickListener);

        foldername = findViewById(R.id.foldername);
        backmain = findViewById(R.id.backMain);
        backmain.setOnClickListener(mClickListener);
        folderback = findViewById(R.id.FolderBack);
        folderback.setOnClickListener(mClickListener);
        foldername.setVisibility(View.INVISIBLE);
        folderback.setVisibility(View.INVISIBLE);

        delet_mode.setVisibility(View.VISIBLE);

        send_mode.setVisibility(View.VISIBLE);

        deletstart.setVisibility(View.GONE);
        Chk_All.setVisibility(View.GONE);
        cancelbutton.setVisibility(View.GONE);

        sendstart.setVisibility(View.GONE);

        dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        formatter = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분 ss초", Locale.getDefault());
        clickMainfolder();

    }

    public void clickMainfolder() {
        MainFolder();
        MainRecyclerView();
        MainRecyclerViewClick();
    }

    public void clicknamefolder() {
        nameFolder();
        nameRecyclerView();
        nameRecyclerViewClick();
    }

    public void MainFolder() {
        filesMainList = new ArrayList<>();
        Maindirectory = new File(saveMainFolder);
        Mainfiles = null;
        Mainfiles = Maindirectory.listFiles();
        for (int i = 0; i < Mainfiles.length; i++) {
            File Namedirectory2 = new File(saveMainFolder + File.separator + Mainfiles[i].getName());
            File[] Namefiles2 = null;
            Namefiles2 = Namedirectory2.listFiles();
            filesMainList.add(new ChartListData(Mainfiles[i].getName(), String.valueOf(Namefiles2.length), false));
        }
        Collections.sort(filesMainList, cmpAsc1);
    }

    public static Comparator<ChartListData> cmpAsc1 = new Comparator<ChartListData>() {
        @Override
        public int compare(ChartListData o1, ChartListData o2) {
            return o1.getName().compareTo(o2.getName());
        }

    };

    public void MainRecyclerView() {

        recyclerView = findViewById(R.id.recyclerview);//리사이클 뷰 연결
        recyclerView.setHasFixedSize(true);
        recyclerView.clearOnChildAttachStateChangeListeners();
        listAdapter = new ChartListAdapter(ChartList.this, filesMainList, Chk_All);
        page = 0;
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

    public void MainRecyclerViewClick() {
        listAdapter.notifyDataSetChanged();//lll
        listAdapter.setOnItemClickListener(new ChartListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {//리스트를 클릭했을때 이벤트
                deletModecencel();
                sendModecencel();
                userlist = filesMainList.get(position);
                saveNameFolder = Environment.getExternalStorageDirectory() + File.separator + "Nineone" + File.separator + userlist.getName();
                foldername.setVisibility(View.VISIBLE);
                foldername.setText(userlist.getName());
                Log.e("aaa", userlist.getName() + "-" + saveNameFolder);
                clicknamefolder();
                folderback.setVisibility(View.VISIBLE);
                NowMainFolder = false;

            }
        });
    }

    public void nameFolder() {
        filesNameList = new ArrayList<>();
        Namedirectory = new File(saveNameFolder);
        Namefiles = null;
        Namefiles = Namedirectory.listFiles();
        for (int i = 0; i < Namefiles.length; i++) {
            try {
                endDate[i] = dateFormat.parse(Namefiles[i].getName());
                day[i] = formatter.format(endDate[i]);
                filesNameList.add(new ChartListData(day[i], "", false));
                // filesNameList.add(new ChartListData(day[i],2));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(filesNameList, cmpAsc2);
    }

    public static Comparator<ChartListData> cmpAsc2 = new Comparator<ChartListData>() {
        @Override
        public int compare(ChartListData o1, ChartListData o2) {
            return o2.getName().compareTo(o1.getName());
        }

    };

    public void nameRecyclerView() {
        recyclerView = findViewById(R.id.recyclerview);//리사이클 뷰 연결
        listAdapter = new ChartListAdapter(this, filesNameList, Chk_All);
        page = 1;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(listAdapter);//리사이클뷰를 MyAdapterdp 연결
        } else {
            gridLayoutManager = new GridLayoutManager(this, 2);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setAdapter(listAdapter);
        } // end if
    }

    public void nameRecyclerViewClick() {
        listAdapter.notifyDataSetChanged();//lll
        listAdapter.setOnItemClickListener(new ChartListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {//리스트를 클릭했을때 이벤트
                deletModecencel();
                sendModecencel();
                ChartListData namedatalist = filesNameList.get(position);
                String csvRoute = null;
                try {
                    filefolderday = formatter.parse(namedatalist.getName());
                    fileday = dateFormat.format(filefolderday);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String path = Environment.getExternalStorageDirectory() + File.separator + "Nineone" + File.separator + userlist.getName() + File.separator + fileday;
                String FolderRoute = userlist.getName() + File.separator + fileday;
                File f = new File(path);
                if (extensionFilter(f).size() != 0) {
                    csvRoute = extensionFilter(f).get(0);
                    Intent listClick = new Intent(getApplicationContext(), ChartMain.class);//클릭시 ListClick으로 이동 및 정보 전달

                    listClick.putExtra("getCSV", csvRoute);
                    listClick.putExtra("getday", fileday);

                    listClick.putExtra("getRoute", FolderRoute);
                    listClick.putExtra("getfoldername", namedatalist.getName());

                    listClick.putExtra("getuser", userlist.getName());
                    startActivity(listClick);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChartList.this);
                    //  builder.setTitle("종료 확인");
                    builder.setMessage("파일이 없습니다.");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
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

    private static final ArrayList<String> EXTENSIONS = new ArrayList<>(Arrays.asList(".csv"));//csv만 찾기

    private ArrayList<String> extensionFilter(File folder) {
        ArrayList<String> result = new ArrayList<>();

        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    result.addAll(extensionFilter(file));
                } else {
                    if (EXTENSIONS.contains(file.getName().substring(file.getName().lastIndexOf(".")))) {
                        result.add(file.toString());
                    }
                }
            }
        } else {

        }
        return result;
    }

    ArrayList<ChartListData> selctList = new ArrayList<ChartListData>();
    public boolean allchecktrue = false;
    int kor;
    String sned_Folder_path;
    String username;
    File[] folder_contents2;
    String usernameget;
    String[] K= new String[10000];
    private Button.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.chk_send_mode:
                    sendMode();
                    listAdapter.activateButtons(true);

                    break;
                case R.id.chk_send:

                    if (listAdapter.checkboxclickList.size() != 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChartList.this);
                        builder.setMessage(listAdapter.checkboxclickList.size() + "개의 파일을 전송하시겠습니까?");
                        builder.setPositiveButton("전송", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (NowMainFolder) {
                                    listAdapter.checkboxclickList.size();
                                    for (int i = 0; i < listAdapter.checkboxclickList.size(); i++) {
                                        Log.e("aaa", Environment.getExternalStorageDirectory() + "/Nineone/" + listAdapter.checkboxclickList.get(i).getName());
                                        username=listAdapter.checkboxclickList.get(i).getName();
                                        String folder_contents= Main_folder_path(username);
                                        folder_contents2 = new File(folder_contents).listFiles();
                                        for(int j=0 ;j<folder_contents2.length;j++){
                                            //  if (folder_contents2[j].getName().substring(folder_contents2[j].getName().length() - 3) == "csv") {
                                            // K[i]=listAdapter.checkboxclickList.get(i).getName();
                                            sned_Folder_path = Name_folder_path2(username, folder_contents2[j].getName());
                                            usernameget = folder_contents2[j].getName();
                                            //  Log.e("sned_Folder_path",listAdapter.checkboxclickList.get(i).getName()+"--"+folder_contents2[j].getName());
                                            //   Log.e("sned_Folder_path",sned_Folder_path);
                                            data_send_tcp = new TCP_Client();
                                            data_send_tcp.execute(this);
                                            Log.e("kko2", folder_contents2[j].getName().substring(folder_contents2[j].getName().length() - 3) );
                                            Log.e("kko2", folder_contents2[j].getName());
                                            //    }
                                        }
                                        Log.e("kko3", username);
                                        //     ConnectThread th = new ConnectThread();
                                        //     th.start();
                                        // MainDelet(listAdapter.checkboxclickList.get(i).getName());
                                    }
                                    listAdapter.notifyDataSetChanged();
                                    Chk_All.setChecked(false);
                                    clickMainfolder();
                                } else {
                                    listAdapter.checkboxclickList.size();
                                    for (int i = 0; i < listAdapter.checkboxclickList.size(); i++) {
                                        Log.e("bbb", Environment.getExternalStorageDirectory() + "/Nineone/" + userlist.getName() + "/" + listAdapter.checkboxclickList.get(i).getName());
                                        sned_Folder_path = Name_folder_path(listAdapter.checkboxclickList.get(i).getName());
                                        try {
                                            userfoldername = formatter.parse(listAdapter.checkboxclickList.get(i).getName());

                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        usernameget = dateFormat.format(userfoldername);
                                        data_send_tcp = new TCP_Client();
                                        data_send_tcp.execute(this);
                                        Log.e("kko2", userlist.getName());
                                        Log.e("kko2", listAdapter.checkboxclickList.get(i).getName());
                                        //ConnectThread th = new ConnectThread();
                                        // th.start();

                                        //NameDelet(listAdapter.checkboxclickList.get(i).getName());

                                    }
                                    // listAdapter.notifyDataSetChanged();
                                    Chk_All.setChecked(false);
                                    clicknamefolder();
                                }
                                sendModecencel();
                            }
                        });
                        builder.setNegativeButton("취소", null);
                        builder.show();

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChartList.this);
                        builder.setMessage("전송하실 폴더를 선택해 주세요");
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.show();
                    }
                    break;
                case R.id.chk_delet_mode:
                    deletMode();
                    listAdapter.activateButtons(true);
                    //aaa.checkdelet.setVisibility(View.VISIBLE);
                    break;
                case R.id.chk_delet:

                    if (listAdapter.checkboxclickList.size() != 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChartList.this);
                        builder.setMessage(listAdapter.checkboxclickList.size() + "개의 파일을 삭제하시겠습니까?");
                        builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (NowMainFolder) {
                                    listAdapter.checkboxclickList.size();
                                    for (int i = 0; i < listAdapter.checkboxclickList.size(); i++) {
                                        Log.e("aaa", Environment.getExternalStorageDirectory() + "/Nineone/" + listAdapter.checkboxclickList.get(i).getName());
                                        MainDelet(listAdapter.checkboxclickList.get(i).getName());
                                    }
                                    listAdapter.notifyDataSetChanged();
                                    Chk_All.setChecked(false);
                                    clickMainfolder();
                                } else {
                                    listAdapter.checkboxclickList.size();
                                    for (int i = 0; i < listAdapter.checkboxclickList.size(); i++) {
                                        Log.e("aaa", Environment.getExternalStorageDirectory() + "/Nineone/" + userlist.getName() + "/" + listAdapter.checkboxclickList.get(i).getName());
                                        NameDelet(listAdapter.checkboxclickList.get(i).getName());
                                    }
                                    listAdapter.notifyDataSetChanged();
                                    Chk_All.setChecked(false);
                                    clicknamefolder();
                                }
                                deletModecencel();
                            }
                        });
                        builder.setNegativeButton("취소", null);
                        builder.show();

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChartList.this);
                        builder.setMessage("삭제하실 폴더를 선택해 주세요");
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.show();
                    }
                    break;
                case R.id.chk_select_all:

                    if (!listAdapter.checked) {
                        // if (!allchecktrue) {
                        for (ChartListData model : filesMainList) {
                            model.setlistckeck(true);
                        }
                        Chk_All.setChecked(true);
                        listAdapter.notifyDataSetChanged();
                        allchecktrue = true;
                        //   }
                    } else {
                        //   if (allchecktrue) {
                        for (ChartListData model : filesMainList) {
                            model.setlistckeck(false);
                        }
                        Chk_All.setChecked(false);
                        listAdapter.notifyDataSetChanged();
                        allchecktrue = false;
                        //  }
                    }
                    listAdapter.ClickItem();
                    break;
                case R.id.chk_delet_cancel:
                    deletModecencel();
                    sendModecencel();
                    Chk_All.setChecked(false);
                    break;
                case R.id.backMain:
                    if(data_send_tcp!=null){
                        data_send_tcp.cancel(true);
                        Log.e("asd","종료");
                    }
                    finish();
                    break;
                case R.id.FolderBack:
                    clickMainfolder();
                    deletModecencel();
                    sendModecencel();
                    NowMainFolder = true;
                    foldername.setVisibility(View.INVISIBLE);
                    folderback.setVisibility(View.INVISIBLE);

                    break;
            }
        }
    };
    @Override
    protected void onStop() {
        // Log.d(TAG, "onStop");
        super.onStop();
        if(data_send_tcp!=null){
            data_send_tcp.cancel(true);
            Log.e("asd","종료");
        }
    }
    // 해당 디렉토리 통째로 비우기
    public void MainDelet(String dirName) {
        File file = new File(Environment.getExternalStorageDirectory() + "/Nineone/" + dirName);
        File[] childFileList = null;
        childFileList = file.listFiles();
        try {
            for (File childFile : childFileList) {
                Log.e("aa1", String.valueOf(childFile));
                if (childFile.isDirectory()) {
                    infolderDelet(childFile.getAbsolutePath());
                } else {
                    childFile.delete();    //하위 파일삭제
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        file.delete();   //root 삭제

    }

    public void infolderDelet(String dirName) {
        File file = new File(dirName);
        File[] childFileList = file.listFiles();
        try {
            for (File childFile : childFileList) {
                if (childFile.isDirectory()) {
                    infolderDelet(childFile.getAbsolutePath());
                } else {
                    childFile.delete();    //하위 파일삭제
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        file.delete();
    }

    public void NameDelet(String dirName) {
        try {
            filechange = formatter.parse(dirName);
            filechangefinsh = dateFormat.format(filechange);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        File file3 = new File(Environment.getExternalStorageDirectory() + "/Nineone/" + userlist.getName() + "/" + filechangefinsh);
        File[] childFileList3 = file3.listFiles();
        try {
            for (File childFile4 : childFileList3) {
                if (childFile4.isDirectory()) {

                } else {
                    childFile4.delete();    //하위 파일삭제
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        file3.delete();   //root 삭제
    }

    public void onBackPressed() {
        if (deletback == 0) {
            if (page == 0) {
//                data_send_tcp.cancel(true);
                finish();

            } else {
                clickMainfolder();
                NowMainFolder = true;
                foldername.setVisibility(View.INVISIBLE);
                folderback.setVisibility(View.INVISIBLE);
            }
        } else if (deletback == 1) {
            deletModecencel();
            sendModecencel();
        }
    }

    public void delete() {

    }

    public void deletMode() {

        send_mode.setVisibility(View.GONE);
        delet_mode.setVisibility(View.GONE);
        deletstart.setVisibility(View.VISIBLE);
        Chk_All.setVisibility(View.VISIBLE);
        cancelbutton.setVisibility(View.VISIBLE);
        deletback = 1;
    }

    public void deletModecencel() {
        listAdapter.activateButtons(false);
        send_mode.setVisibility(View.VISIBLE);
        delet_mode.setVisibility(View.VISIBLE);
        deletstart.setVisibility(View.GONE);
        Chk_All.setVisibility(View.GONE);
        cancelbutton.setVisibility(View.GONE);
        deletback = 0;
    }

    public void sendMode() {

        send_mode.setVisibility(View.GONE);
        delet_mode.setVisibility(View.GONE);
        sendstart.setVisibility(View.VISIBLE);
        Chk_All.setVisibility(View.VISIBLE);
        cancelbutton.setVisibility(View.VISIBLE);
        deletback = 1;
    }

    public void sendModecencel() {
        listAdapter.activateButtons(false);
        send_mode.setVisibility(View.VISIBLE);
        delet_mode.setVisibility(View.VISIBLE);
        sendstart.setVisibility(View.GONE);
        Chk_All.setVisibility(View.GONE);
        cancelbutton.setVisibility(View.GONE);
        deletback = 0;
    }
    String folder_path=Environment.getExternalStorageDirectory() + File.separator + "Nineone" + File.separator;
    String name_folder_path,name_folder_path2;
    String main_folder_path;

    public String Name_folder_path(String s) {//날짜 폴더까지 경로
        try {
            filechange = formatter.parse(s);
            filechangefinsh = dateFormat.format(filechange);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        name_folder_path = folder_path + userlist.getName() + "/" + filechangefinsh;
        return name_folder_path;
    }
    public String Name_folder_path2(String d,String s) {

        name_folder_path2 = folder_path + d + "/" + s;
        return name_folder_path2;
    }
    public String Main_folder_path(String s) {

        main_folder_path = folder_path + s;
        return main_folder_path;
    }

    private DataInputStream data_send;
    int k=0,kk=0;
    public class TCP_Client extends AsyncTask {
        protected  String SERV_IP = "dangjin.nineone.com"; //서버의 ip주소를 작성하면 됩니다.
        protected  int PORT = 7210; //서버의 Port번호를 작성하면 됩니다.
        //   String path = Environment.getExternalStorageDirectory() + File.separator + "Nineone" + File.separator + "20";
        //    File[] files = new File(path).listFiles();
        String sned_user_nameget=usernameget;
        String sned_Folder_path2=sned_Folder_path;
        File[] files2 = new File(sned_Folder_path2).listFiles();
        String sned_username =username;
        @Override
        protected Object doInBackground(Object... params) {
            for (int i = 0; i < files2.length; i++) {

                String str = files2[i].getName().substring(files2[i].getName().length() - 3);
                Log.e("ddd", str);
                if (str.equals("csv")) {
                    try {
                        Log.d("TCP", "server connecting");
                        InetAddress serverAddr = InetAddress.getByName(SERV_IP);
                        Socket sock = new Socket(serverAddr, PORT);

                        try {
                            System.out.println("데이터찾는중");
                            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())), true);
                            if (NowMainFolder) {
                                out.println(files2[i].getName() + "\n" + sned_username.replace(" ", "_") + "\n" + androidid + "\n" + sned_user_nameget);
                                out.flush();
                                data_send = new DataInputStream(new FileInputStream(new File(sned_Folder_path2 + File.separator + files2[i].getName())));
                                Log.e("aaa5", files2[i].getName() + "--" + sned_Folder_path + "-" + sned_user_nameget);
                            } else {
                                out.println(files2[i].getName() + "\n" + userlist.getName().replace(" ", "_") + "\n" + androidid + "\n" + sned_user_nameget);
                                out.flush();
                                data_send = new DataInputStream(new FileInputStream(new File(sned_Folder_path2 + File.separator + files2[i].getName())));
                                Log.e("aaa66", files2[i].getName() + "--" + sned_Folder_path2 + "-" + sned_user_nameget);
                                Log.e("aaa6", files2[i].getName() + "--" + sned_Folder_path + "-" + sned_user_nameget);
                            }
                            // File file1=new File(Environment.getExternalStorageDirectory() + File.separator + "Nineone" + File.separator +"20/"+ files[i].getName());
                            //File file = new File(Environment.getExternalStorageDirectory() + File.separator + "Nineone" + File.separator, "21.mp4"); //읽을 파일 경로 적어 주시면 됩니다.
                            DataInputStream dis = data_send;
                            DataOutputStream dos = new DataOutputStream(sock.getOutputStream());

                            // long fileSize = file1.length();
                            byte[] buf = new byte[104857600];

                            long totalReadBytes = 0;
                            int readBytes;
                            System.out.println("데이터찾기 끝");

                            while ((readBytes = dis.read(buf)) > 0) { //길이 정해주고 서버로 보냅니다.

                                dos.write(buf, 0, readBytes);
                                totalReadBytes += readBytes;
                            }
                            dos.flush();
                            System.out.println("데이터보내기 끝 직전");
                            dos.close();
                            System.out.println("데이터끝");
                            dis.close();
                            sock.close();
                        } catch (IOException e) {
                            Log.d("TCP", "don't send message");
                            e.printStackTrace();
                        }

                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //return null;
                }
            }
            return null;
        }
    }
}