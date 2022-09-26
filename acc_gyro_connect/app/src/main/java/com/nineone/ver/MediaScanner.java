package com.nineone.ver;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.text.TextUtils;

public class MediaScanner {//안드로이드 7.0부터는 URI의 path를 알아내는 것을 FileProvider로 사용해야 하므로 특정 디렉토리를 사용하는 것이 어려워졌습니다
                            //Broadcast방식으로도 미디어스캔을 할 수는 있지만 성능저하의 문제 등으로 추천되지 않습니다..
                           //그래서 특정 파일에 미디어 스캔을 하기 위해 MediaScannerConnection을 사용해야 합니다.
                           //다른 class들과 독립적으로 사용하기 위해 MediaScanner.java 를 생성하여 class를 만들어주었습니다.
                           //apk파일을 다운 후 폴더에 방영해 바로 설치 할 수 있도록 하는 용도로 사용되었습니다.
    private Context mContext;
    private static volatile MediaScanner mMediaInstance = null;
    private MediaScannerConnection mMediaScanner;//미디어 스캐닝을 하기위한 변수
    //private MediaScannerConnection.MediaScannerConnectionClient mMediaScannerClient;

    private String mFilePath;//파일 주소로를 반환하기 위한 변수

    public static MediaScanner getInstance( Context context ) {
        if( null == context )
            return null;
        if( null == mMediaInstance )
            mMediaInstance = new MediaScanner( context );
        return mMediaInstance;
    }
    public static void releaseInstance() {
        if ( null != mMediaInstance ) {
            mMediaInstance = null;
        }
    }

    MediaScanner(Context context) {
        mContext = context;
        mFilePath = "";
        MediaScannerConnection.MediaScannerConnectionClient mediaScanClient;
        mediaScanClient = new MediaScannerConnection.MediaScannerConnectionClient(){
            @Override public void onMediaScannerConnected() {
                mMediaScanner.scanFile(mFilePath, null);//미디어 스캐너 연결
//                mFilePath = path;
            }
            @Override public void onScanCompleted(String path, Uri uri) {//미디어 스캔이 완료 되면 연결해제
                System.out.println("::::MediaScan Success::::");
                mMediaScanner.disconnect();
            }
        };
        mMediaScanner = new MediaScannerConnection(mContext, mediaScanClient);
    }

    public void mediaScanning(final String path) {

        if( TextUtils.isEmpty(path) )
            return;
        mFilePath = path;

        if( !mMediaScanner.isConnected() )
            mMediaScanner.connect();

        //mMediaScanner.scanFile( path,null );
    }
}


