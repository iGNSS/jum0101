package com.example.picturemap;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_DCIM;
import static android.os.Environment.DIRECTORY_PICTURES;


public class CamaraActivity extends AppCompatActivity  {
    private static final int REQUEST_IMAGE_CAPTURE = 600;//카메라권한허용 변수
    private String imageFilePath;//방금찍은사진의 위치주소 변수
    private Uri photoUri;//사진 경로를 Uri 형태로 저장
    private MediaScanner mMediaScanner; // 사진 저장 시 갤러리 폴더에 바로 반영사항을 업데이트 시켜주려면 이 것이 필요하다(미디어 스캐닝)

    private ImageView imageView;//찍은 사진을 보여주는 이미지뷰
    Button cameraBtn;//카메라 버튼
    private TextView mView;//사진데이터를 보여주기위한 변수
    // private double lat,lon;
    private GpsTracker gpsTracker;//핸드폰 자체에서 GPS를 받아오기 위한 변수
    private GpsTracker gpsTracker1;
//    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
  //  private static final int PERMISSIONS_REQUEST_CODE = 100;
    private TextView mtextView3;
    private TextView mtextView4;
    private Button mAlbum;
    double longitude;
    double latitude;
    TextView mtextView6;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    File photoFile = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        // 레이아웃과 변수 연결
        imageView = findViewById(R.id.imageview);
        cameraBtn = findViewById(R.id.camera_button);
        cameraBtn.setOnClickListener(mClickListener);
        // 사진 저장 후 미디어 스캐닝을 돌려줘야 갤러리에 반영됨.
        mMediaScanner = MediaScanner.getInstance(getApplicationContext());
        mView = (TextView) findViewById(R.id.textView);
       /* TedPermission.with(getApplicationContext())
                .setPermissionListener(permissionListener)
                .setRationaleMessage("카메라 권한이 필요합니다.")
                .setDeniedMessage("거부하셨습니다.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();*/
        mtextView3 = (TextView) findViewById(R.id.textView3);
        mtextView4 = (TextView) findViewById(R.id.textView4);
        TextView mtextView5 = (TextView) findViewById(R.id.textView5);
        mtextView6 = (TextView) findViewById(R.id.textView6);
      //  TextView mtextView6 = (TextView) findViewById(R.id.textView6);
        gpsTracker1 = new GpsTracker(CamaraActivity.this);
        double lat = gpsTracker1.getLatitude(); // 위도
        double lon = gpsTracker1.getLongitude(); //경도
        mAlbum = (Button)findViewById(R.id.camera_button2);
        mAlbum.setOnClickListener(mClickListener);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = null;//포토 파일 사진을 다시 찍으면 비워줘야함

        mtextView3.setText(DegreeToDMS(lat)+"");//위도 도분초
        mtextView4.setText(DegreeToDMS(lon)+"");//경도 도분초
        //mtextView5.setText(nSecond+"");
       // mtextView6.setText(lat + ", " + lon);//위도와 경도
        // 권한 체크

   }

    public String DegreeToDMS(double degree) {//위도 경도를 도 분 초로 나누는 공식 지워도 무방
        int hour = (int) degree;
        degree -= hour;
        int minute = (int) (degree * 60);
        degree = degree * 60 - minute;
        int second = (int) (degree * 60);
        degree = degree * 60 - second;
        int msecond = (int) (degree * 1000);

        return hour + ":" + minute + "." + second + "/" + msecond;
    }

    private Button.OnClickListener mClickListener = new View.OnClickListener() {//각 버튼 클릭리스너
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            switch (v.getId()){
                case R.id.camera_button://카메라로 이동 버튼
                    if (intent.resolveActivity(getPackageManager()) != null) {
                         photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException e) {
                        }
                        if (photoFile != null) {
                            photoUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), photoFile);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                        }
                    }
                    break;
                case R.id.camera_button2://앨범으로 이동 버튼
                    Intent intent1 = new Intent(getApplicationContext(), AlbumActivity.class);
                    startActivity(intent1);
                    break;
            }
        }
    };


    private File createImageFile() throws IOException {// 사진 촬영 후 썸네일만 띄워줌.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);//사진을 불러와 비트맵 형식으로 저장
            ExifInterface exif = null;

            try {
                exif = new ExifInterface(imageFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int exifOrientation;
            int exifDegree;

            if (exif != null) {
                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                exifDegree = exifOrientationToDegress(exifOrientation);
            } else {
                exifDegree = 0;
            }

            String result = "";
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());//시간 형식
            Date curDate = new Date(System.currentTimeMillis());//찍은시간
            String filename = formatter.format(curDate);//파일이름
            String strFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM) + File.separator + "PictureMap" + File.separator;//폴더이름
            File file = new File(strFolderName);
            if (!file.exists()) {
                file.mkdirs();
            }
            File f = new File(strFolderName + "/" + filename + ".jpg");//File 형식으로 사진저장
            result = f.getPath();

            imageView.setImageBitmap(rotate(bitmap, exifDegree));// 이미지 뷰에 비트맵을 set하여 이미지 표현
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                result = "Save Error fOut";
            }

            rotate(bitmap, exifDegree).compress(Bitmap.CompressFormat.JPEG, 70, fOut);  //사진 정방향으로 돌리기 위한 코드

            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut.close();
                mMediaScanner.mediaScanning(strFolderName + "/" + filename + ".jpg"); // 방금 저장된 사진을 갤러리 폴더 반영 및 최신화
            } catch (IOException e) {
                e.printStackTrace();
                result = "File close Error";
            }
            try {
                Exif_Tag(Environment.getExternalStorageDirectory() + "/DCIM/PictureMap/"+filename+".jpg");//찍은 사진에 정보 넣기
            } catch (IOException e) {
                e.printStackTrace();
            }
            mata(filename);//사진 정보 불러오기
        }
    }
    public void Exif_Tag(String imagelo) throws IOException {//사진에 각종 정보 넣기 기능
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());
        ExifInterface exif = null;
        int exifOrientation;
        int exifDegree;
        try {
            exif = new ExifInterface(imageFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientationToDegress(exifOrientation);//현재 사진 각도
        } else {
            exifDegree = 0;
        }

        ExifInterface exif2 = new ExifInterface(imagelo);
        gpsTracker = new GpsTracker(CamaraActivity.this);
        double lat1 = gpsTracker.getLatitude(); // 위도
        double lon1 = gpsTracker.getLongitude(); //경도
        exif2.setAttribute(ExifInterface.TAG_DATETIME, timeStamp);//사진 찍은 시간 추가
        exif2.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, String.valueOf(lat1));//위도추가
        exif2.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, String.valueOf(lon1));//경도추가
        exif2.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(exifDegree));//사진 기울기
        exif2.saveAttributes();
    }
    public void mata(String aaa){//사진 불러오기
        String filename2 = Environment.getExternalStorageDirectory() + "/DCIM/PictureMap/"+aaa+".jpg";
        try {
            ExifInterface exif2 = new ExifInterface(filename2);
            showExif(exif2);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error!", Toast.LENGTH_LONG).show();
        }
    }
    private void showExif(ExifInterface exif3) {//사진 정보 출력을 위한 뷰
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
        //  myAttribute += getTagString(ExifInterface.TAG_MODEL, exif3);
        myAttribute += getTagString(ExifInterface.TAG_ORIENTATION, exif3);
        // myAttribute += getTagString(ExifInterface.TAG_WHITE_BALANCE, exif3);

        mView.setText(myAttribute);
    }
    private int exifOrientationToDegress(int exifOrientation) {//사진이 몇도나 돌아갔나 계산하는 코드
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {//사진을 정방향으로 돌리기 위한 코드
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /*PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
           // Toast.makeText(getApplicationContext(), "권한이 허용됨",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(getApplicationContext(), "권한이 거부됨",Toast.LENGTH_SHORT).show();
        }
    };
*/
    private final LocationListener mLocationListener = new LocationListener() {//실시간 위치 정보 반환
        public void onLocationChanged(Location location) {
            Log.d("test", "onLocationChanged, location:" + location);
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            location.getAltitude();
            float accuracy = location.getAccuracy();
            location.getProvider();
            mtextView6.setText(longitude + "/" + latitude + "/" + accuracy);
        }

        public void onProviderDisabled(String str) {
            Log.d("test", "onProviderDisabled, provider:" + str);
        }

        public void onProviderEnabled(String str) {
            Log.d("test", "onProviderEnabled, provider:" + str);
        }

        public void onStatusChanged(String str, int i, Bundle bundle) {
            Log.d("test", "onStatusChanged, provider:" + str + ", status:" + i + " ,Bundle:" + bundle);
        }
    };



    private String getTagString(String tag, ExifInterface exif) {
        return (tag + " : " + exif.getAttribute(tag) + "\n");
    }


}
