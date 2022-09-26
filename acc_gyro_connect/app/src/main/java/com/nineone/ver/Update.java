package com.nineone.ver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class Update extends AppCompatActivity {//자동 업데이트 화면제어
    TextView update,old;
    int versionCode = BuildConfig.VERSION_CODE;
    int New_version;
    private Button download_button;
    private String update_filename;
    long downloadId=0;
    private Context context;
    private DownloadManager mDownloadManager;
    private Handler mHandler;
    private ProgressDialog mProgressDialog;
    private MediaScanner mMediaScanner;
    Uri downloadUri = Uri.parse("http://lora.nineone.com/nineonever/Nineone_VER.apk");
    File outputFile;
    String pathlick = Environment.getExternalStorageDirectory().toString() + File.separator + Environment.DIRECTORY_DOWNLOADS+ File.separator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        context = this.getBaseContext();
        mMediaScanner = MediaScanner.getInstance(getApplicationContext());
        update=findViewById(R.id.update);
        old=findViewById(R.id.old);
        Intent listClick = getIntent();//WalkList에서 정보를 받아오기
        New_version = listClick.getExtras().getInt("update");//새버전
        old.setText("현재버전: "+ versionCode);
        update.setText("업데이트버전: " +New_version);
        APK_Down_permit();

        download_button=findViewById(R.id.download_button);
        download_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update_filename= "NineOneVER_"+New_version+".apk";
                mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                if(isFileDownloaded(update_filename)!=0){
                    DownloadManager.Request request = new DownloadManager.Request(downloadUri);
                   // Log.e("STag1", String.valueOf(request));
                    request.setTitle("NineOneVER_" + New_version);  // 다운로드 제목
                    request.setDescription("다운로드 중..");    // 다운로드 설명
                    request.setNotificationVisibility(1);  // 상단바에 완료 결과 0 은 안뜸
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, update_filename);
                    downloadId = mDownloadManager.enqueue(request);
                    try {
                        mMediaScanner.mediaScanning(pathlick+update_filename); // 방금 저장된 파일을 폴더에 반영 및 최신화
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    File path = new File(Environment.getExternalStorageDirectory().toString() + File.separator + Environment.DIRECTORY_DOWNLOADS);
                    outputFile = new File(path, update_filename);
                    mHandler = new Handler();
                    runOnUiThread(new Runnable() {//apk파일이 URL에서 다운될 때 동안 대기하기위한 핸들러함수
                        @Override public void run() {
                            mProgressDialog = ProgressDialog.show(Update.this,"", "저장중입니다.",true);
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (mProgressDialog!=null&&mProgressDialog.isShowing()){
                                            mProgressDialog.dismiss();
                                            onPostExecute();
                                        }
                                    } catch ( Exception e ) { e.printStackTrace();
                                    }
                                }
                            }, 3000);
                        }
                    } );

                }else{
                    File path = new File(Environment.getExternalStorageDirectory().toString() + File.separator + Environment.DIRECTORY_DOWNLOADS);
                    outputFile = new File(path, update_filename);
                    onPostExecute();
                }
            }
        });
    }
    protected void onPostExecute() {//SDK파일 버전에 따라 사용 코드가 달라 넣었습니다.
        mMediaScanner.mediaScanning(pathlick+update_filename);; // 방금 저장된 사진을 갤러리 폴더 반영 및 최신화
        //  Log.e("STag2", String.valueOf(pathlick+ready_filename));
        if (Build.VERSION.SDK_INT >= 24) {
            // Android Nougat ( 7.0 ) and later
            installApk(outputFile);
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri apkUri = Uri.fromFile(outputFile);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
        }

    }

    public void installApk(File file) {//다운받은 apk파일 설치 코드
        Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider",file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
        finish();
    }

    private int isFileDownloaded(String fileName) {//중복파일검사
        if (fileName != null && !TextUtils.isEmpty(fileName)) {
            File file = new File(Environment.getExternalStorageDirectory().toString() + File.separator + Environment.DIRECTORY_DOWNLOADS,"/"+fileName);
            if (file.exists()) {
                return 0;
            } else {
                // write here code for download new file
                return 1;
            }
        }
        return 1;
    }
    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("종료 확인");
        builder.setMessage("정말로 종료하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }
    public void APK_Down_permit() {
        if (Build.VERSION.SDK_INT >= 26) { // 출처를 알 수 없는 앱 설정 화면 띄우기
            PackageManager pm = getPackageManager();
            //Log.e("Package Name", pm.getInstalledPackages(0).get(0).packageName);
            //Log.e("Package Name", getPackageName());
            if (!pm.canRequestPackageInstalls()) {
                AlertDialog.Builder b = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog);
                b.setTitle("알림");
                b.setMessage("보안을 위해 스마트폰 환경설정의 '앱 설치 허용'을 설정해 주시기 바랍니다.설정화면으로 이동하시겠습니까?");
                b.setCancelable(false);
                b.setPositiveButton("설정하기", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                });

                b.setNegativeButton("건너띄기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       finish();
                        Toast.makeText(getApplication(), "업데이트를 위해 '앱 설치 허용'이 필요합니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                b.show();
            }
        }
    }
}