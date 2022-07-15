package com.example.picturemap;

import android.app.LauncherActivity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import android.widget.GridLayout.LayoutParams;
public class AlbumAdapter extends BaseAdapter {//앨범 어뎁터
    int CustomGalleryItemBg;//  정의해 둔 attrs.xml의 resource를 background로 받아올 변수 선언
    String mBasePath;// AlbumAdapter을 선언할 때 지정 경로를 받아오기 위한 변수
    Context mContext;// AlbumAdapter을 선언할 때 해당 activity의 context를 받아오기 위한 context 변수
    String[] mImgs;// 위 mBasePath내의 file list를 String 배열로 저장받을 변수
    Bitmap bm=null;// 지정 경로의 사진을 Bitmap으로 받아오기 위한 변수
    Bitmap bm2;
    Bitmap mThumbnail=null;
    DataSetObservable mDataSetObservable = new DataSetObservable(); // DataSetObservable(DataSetObserver)의 생성

    public String TAG = "Gallery Adapter Example :: ";

    public AlbumAdapter(Context context, String basepath){// AlbumAdapter의 생성자
        this.mContext = context;
        this.mBasePath = basepath;

        File file = new File(mBasePath);// 지정 경로의 directory를 File 변수로 받기

        if(!file.exists()){
            if(!file.mkdirs()){
                Log.d(TAG, "failed to create directory");
            }
        }
        mImgs = file.list();//directory 내 file 명들을 String으로 저장

        // attrs.xml 테마적용
        TypedArray array = mContext.obtainStyledAttributes(R.styleable.GalleryTheme);
        CustomGalleryItemBg = array.getResourceId(R.styleable.GalleryTheme_android_galleryItemBackground, 0);
        array.recycle();
    }

    @Override
    public int getCount() { //mImgs에 저장된 리스트 길이 반환
        File dir = new File(mBasePath);
        mImgs = dir.list();
        return mImgs.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }// 갤러리의 해당 Object로 position을 반환
    public String getItem2(int position) {
        return mImgs[position];
    }//파일 제목 반환
    public String getItem3(int position) { return mBasePath + File.separator + mImgs[position]; }//(지정경로 + \ + 파일이름) String으로 반환
    public Bitmap getItemPath(int position){ return BitmapFactory.decodeFile(mBasePath + File.separator + mImgs[position]); }//(지정경로 + \ + 파일이름) Bitmap으로 반환

    @Override
    public long getItemId(int position) {
        return position;
    }//갤러리의 해당 long으로 position을 반환

    // Override this method according to your need
    // 지정 경로 내 사진들을 뿌려주는 메소드.
    // Bitmap을 사용할 경우 메모리 사용량이 많아져 Thumbnail을 사용해 크기를 줄임
    // setImageDrawable()이나 setImageURI() 등의 method로 대체 가능함
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = null;
        BitmapFactory.Options bitOption = new BitmapFactory.Options();
       //여기서부터 사진 크기를 줄여 메모리 사용량을 줄이는 코드
        bitOption.inSampleSize = 4;
        bitOption.inJustDecodeBounds = true;
        int width = bitOption.outWidth;
        int height = bitOption.outHeight;
        int inSampleSize = 1;
        int reqWidth = 256;
        int reqHeight = 192;
        if((width > reqWidth) || (height > reqHeight)){
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        bitOption.inSampleSize = inSampleSize;
        bitOption.inJustDecodeBounds = false;
        //여기까지 사진 크기를 줄여 메모리 사용량을 줄이는 코드
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
        } else {
            imageView = (ImageView) convertView;
        }

        bm = BitmapFactory.decodeFile(mBasePath + File.separator + mImgs[position],bitOption);//사진 각각의 경로

        //bm2 = Bitmap.createScaledBitmap(bm, 200, 200, true);
        mThumbnail = ThumbnailUtils.extractThumbnail(bm, 200, 200);// 크기가 큰 원본에서 image를 300*300 thumnail을 추출.
        imageView.setPadding(1, 1, 1, 1);
        imageView.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(mThumbnail);//이미지 뷰에 사진 넣기
        return imageView;//이미지뷰 리턴
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer){ // DataSetObserver의 등록(연결)
        mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer){ // DataSetObserver의 해제
        mDataSetObservable.unregisterObserver(observer);
    }

    @Override
    public void notifyDataSetChanged(){ // 위에서 연결된 DataSetObserver를 통한 변경 확인
        mDataSetObservable.notifyChanged();
    }
}