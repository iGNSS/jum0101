package com.nineone.ver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.location.LocationManager;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.ListPreference;
import androidx.room.Room;

import com.gun0912.tedpermission.PermissionListener;
import com.nineone.zntil.ShoolnameList;
import com.nineone.zntil.user_aSchool_dialog;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_USER_LIST = 3;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "Stag Main";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1001;
    private final int My_ACCESS_FINE_LOCATION = 2000;
    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;

    private ArrayAdapter<String> listAdapter;

    public static String TagName, StartTime;

    private String txHex = "";

    private boolean outo_connect_check_flag = false;//?????????????????? ???????????? ????????? ????????????????????? ?????? ?????? ????????????
    private double QQ = 0.0001;//Q : ???????????? ?????????
    private double RR = 0.003;// R : ???????????? ????????? ?????? ???
    private double PP = 1;//P : ?????? ?????? ????????? ?????? ???
    private double KAmx = 0;//????????? 0
    private double KAmy = 0;
    private double KAmz = 0;
    private double K;// K : ?????? ??????

    private double Q2 = 0.00001;//Q : ???????????? ?????????
    private double R2 = 0.003;// R : ???????????? ????????? ?????? ???
    private double P2 = 1;//P : ?????? ?????? ????????? ?????? ???
    private double KAmx2 = 0;//????????? 0
    private double KAmy2 = 0;
    private double KAmz2 = 0;
    private double K2;
    private View decorView;
    
    private Camera camera;//???????????????
    private MediaRecorder mediaRecorder;//???????????????
    private SurfaceView surfaceView;//???????????????
    private SurfaceHolder surfaceHolder;//????????? ?????? ?????????
    private boolean recording = false;//?????????????????? ???????????? ????????????

    private TextView vw_txtmacaddrValue;//????????? ???????????? ?????????
    private TextView rcountValue, geoValue, acceValue, gyroValue;
    private String dataToServer;//????????? ?????? ??????
    private String Saving_File_name;//????????? ?????? ?????? ??????
    private boolean connect_check_flag = false;//????????? ????????? ????????? ??????
    private boolean Senser_check_flag = true;//????????? ?????????????????? ???????????????
    // private boolean secel = false;
    private Button mStartBtn, mStopBtn, mSaveStart, mSaveStop;
    private TextView msavess;
    private Context context = this;
    private Handler mHandler,mHandler2;//????????? ????????????
    private ProgressDialog mProgressDialog,mProgressDialog2;
    private String deviceAddress;//????????????????????????
    private String filename = null;//?????????????????????
    private Date curDate;//??????????????????
    private SimpleDateFormat formatter;//??????????????????
    private SharedPreferences preferences, preferences_name, preferences_snesor;
    private String senser_adress_save;
    private boolean senser_necessary_save=true, name_necessary_save=false;//??????????????? ???????????? ????????? ??????????????????
    //  TextView timer_Value;
    //???????????????
    private TimerTask timerTask;
    private Timer timer = new Timer();
    private int timer_count;
    private TextView countText;
    
    private int uiOption;//???????????? ??????????????? ?????? 
    private Dialog start_delay_dialog;//????????? ????????? 3????????? ???????????? ???????????? ???????????????
    private MyTimer myTimer;//?????? 3??? ???????????? ???????????? ????????? ??????
    private TextView start_delay_text;//?????????????????? 3??? ????????? ????????? ?????????
    private SeekBar mZoomSeekBar;//?????? ???????????? ?????? seekbar
    public String user_name;//??????????????? ?????? ??????
    public String user_age=" ";
    public String user_gender=" ";
    private TextView nameview;//??????????????????
    private String rotation_user_name;
    private String sneser_adress_save;
    private String rotation_sneser_adress_save = null;
    private Camera.Parameters parameters;
    int mPicOrientation;
    //??????????????? ??????????????? ???????????? ?????? ???????????? ?????? ??????
    int cameraId = 0;
    int zoom = 0;
    int shooting_Angle = 0;
    int shooting_Angle2;
    int result, result2;
    //
    int versionCode = BuildConfig.VERSION_CODE;
    private long lastsenserdata;//?????????????????? ????????? ???????????? ????????? ?????????????????? ??????
    boolean senser_data_ok=false;//?????????????????? ????????? ???????????? ????????? ?????????????????? ??????
    String androidid;
    TCP_Client data_send_tcp;
    int rotation2 = 0;
    int degrees = 0;

    boolean Stop_Measurement_click=false;


    public  UserMyDatabase myDatabase;
    // Ball.setBackgroundColor(Color.parseColor("#4B89DC"));
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        //    setContentView(R.layout.main);
        //} else {
        setContentView(R.layout.main_horizontal);
        bluetoothCheck();
        androidid = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        //} // end if
        //GPSSetting();
        getWindow().setFormat(PixelFormat.UNKNOWN);
        start_delay_dialog = new Dialog(MainActivity.this);
        start_delay_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // ????????? ??????
        start_delay_dialog.setContentView(R.layout.delay_count);
        start_delay_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        start_delay_dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        start_delay_dialog.setCancelable(false);
        service_init();
        ui_init();
        Layout_collection();
        OptionCheckSetting();
        myDatabase= Room.databaseBuilder(getApplicationContext(),UserMyDatabase.class,"note_data").allowMainThreadQueries().build();

        //???????????? ??????????????????
        decorView = getWindow().getDecorView();
        uiOption = getWindow().getDecorView().getSystemUiVisibility();

        uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        uiOption |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiOption |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        //   timer_Value = findViewById(R.id.Timer);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        vw_txtmacaddrValue.setText("");
        formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        orientationListener = new OrientationEventListener(this) {
            public void onOrientationChanged(int orientation) {
                setCameraDisplayOrientation(MainActivity.this, cameraId, camera);
            }
        };

    }


    private void Layout_collection() {
        start_delay_text = start_delay_dialog.findViewById(R.id.delay_count);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        //  mStopBtn = (Button)findViewById(R.id.Save_stop);
        // mStopBtn.setOnClickListener(mClickListener);
        mStartBtn = (Button) findViewById(R.id.Save_start);
        mStartBtn.setOnClickListener(mClickListener);
        countText = findViewById(R.id.sConnect);
        mZoomSeekBar = findViewById(R.id.SeekBarzoom);
        nameview = findViewById(R.id.NameView);
    }

    private void startTimerTask() {
        stopTimerTask();
        timer_count=measurement_timeer;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (timer_count > 1) {
                    countText.post(new Runnable() {
                        @Override
                        public void run() {
                            countText.setText(timer_count + " ???");

                            long reqTime = System.currentTimeMillis();
                            if ((reqTime - lastsenserdata) / 1000 >= 1) {
                                senser_data_ok=false;
                                invalidateOptionsMenu();
                            } else {
                                senser_data_ok=true;
                                invalidateOptionsMenu();
                            }
                            //  Log.e("bbc", String.valueOf((reqTime-lastsenserdata)/1000));
                        }

                    });
                    timer_count--;
                    //if(){}
                } else {
                    stopTimerTask();
                    End_measurement();
                }
            }
        };
        timer.schedule(timerTask, 2000, 1000);
    }

    private void stopTimerTask() {
        if (timerTask != null) {
            timer_count = measurement_timeer;
            countText.setText(measurement_timeer+" ???");
            timerTask.cancel();
            timerTask = null;
        }

    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_2:
                //   case R.id.Save_start:
                Option_state();
                break;
            case KeyEvent.KEYCODE_BACK:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("?????? ??????");
                builder.setMessage("????????? ?????????????????????????");
                builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveTaskToBack(true); // ???????????? ?????????????????? ??????
                        finish(); // ???????????? ?????? + ????????? ??????????????? ?????????
                        android.os.Process.killProcess(android.os.Process.myPid()); // ??? ???????????? ??????
                    }
                });
                builder.setNegativeButton("??????", null);
                builder.show();
                break;

            case KeyEvent.KEYCODE_1:
                User_Change();
                break;
        }
        return true;
        // return super.onKeyDown(keyCode, event);
    }

    String strFolderName;

    public void Start_measurement() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!Senser_check_flag) {
                    try {
                        //Toast.makeText(MainActivity.this, "????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                        //?????? ??????
                        curDate = new Date(System.currentTimeMillis());//????????????
                        filename = formatter.format(curDate);//????????????
                        if (user_name != null && user_name.length() != 0) {
                            strFolderName = Environment.getExternalStorageDirectory() + File.separator + "Nineone" + File.separator + user_name + File.separator + filename + File.separator;//????????????
                        } else {
                            strFolderName = Environment.getExternalStorageDirectory() + File.separator + "Nineone" + File.separator + "NineOne" + File.separator + filename + File.separator;
                        }
                        File file = new File(strFolderName);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        Saving_File_name = mDevice.getName().replace("-", "") + "_" + filename + ".csv";
                        if (user_name != null && user_name.length() != 0) {

                            dataToServer = "??????: " + "," + user_name + ","+"??????: " + "," +user_age+","+"??????: " + "," + user_gender+",";

                        }else {

                            dataToServer = "??????: " + "," + "NineOne"  +  ","+"??????: " + "," +user_age+","+"??????: " + "," + user_gender+",";

                        }
                        writeLog(dataToServer, filename);
                        dataToServer = "time" + ","+"count"+"," +"x_acc" + ","+"y_acc" + "," + "z_acc"+ "x_gyro" + ","+"y_gyro" + "," + "z_gyro";
                        writeLog(dataToServer, filename);
                        mediaRecorder = new MediaRecorder();
                        //  mediaRecorder.reset();
                        mediaRecorder.setCamera(camera);
                        camera.unlock();
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
                        mediaRecorder.setOrientationHint(shooting_Angle);
                        mediaRecorder.setOutputFile(strFolderName + filename + ".mp4");
                        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
                        try {
                            mediaRecorder.prepare();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mediaRecorder.start();
                        recording = true;
                        startTimerTask();
                    } catch (Exception e) {
                        e.printStackTrace();
                        mediaRecorder.release();
                    }
                } else {
                    try {
                        //Toast.makeText(MainActivity.this, "????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                        //?????? ??????
                        curDate = new Date(System.currentTimeMillis());//????????????
                        filename = formatter.format(curDate);//????????????
                        if (user_name != null && user_name.length() != 0) {
                            strFolderName = Environment.getExternalStorageDirectory() + File.separator + "Nineone" + File.separator + user_name + File.separator + filename + File.separator;//????????????
                        } else {
                            strFolderName = Environment.getExternalStorageDirectory() + File.separator + "Nineone" + File.separator + "NineOne"  + File.separator + filename + File.separator;
                        }
                        File file = new File(strFolderName);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        mediaRecorder = new MediaRecorder();
                        //  mediaRecorder.reset();
                        mediaRecorder.setCamera(camera);
                        camera.unlock();
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
                        mediaRecorder.setOrientationHint(shooting_Angle);
                        mediaRecorder.setOutputFile(strFolderName + filename + ".mp4");
                        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
                        try {
                            mediaRecorder.prepare();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mediaRecorder.start();
                        mStartBtn.setText("?????? ??????");
                        recording = true;
                        startTimerTask();
                    } catch (Exception e) {
                        e.printStackTrace();
                        mediaRecorder.release();
                    }
                }

            }
        });
    }
    public void End_measurement() {
        if (recording) {
           // recording = false;
            stopTimerTask();
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();

            mHandler = new Handler(Looper.getMainLooper());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog = ProgressDialog.show(MainActivity.this, "", "??????????????????.", true);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                    // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                                    senser_data_ok=false;
                                    invalidateOptionsMenu();
                                    mStartBtn.setText("?????? ??????");
                                    recording = false;
                                    user_name=null;
                                   // nameview.setTextColor(Integer.parseInt("#ffffff"));
                                    nameview.setBackgroundColor(Color.parseColor("#00000000"));
                                    nameview.setText("");
                                    if (!netWork_connect() && measurement_completed_send && !Network_Secondary_Check) {

                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setMessage("??????????????? ???????????? ?????? ????????? ????????? ?????? ???????????????.");
                                        builder.setNegativeButton("??????", null);
                                        builder.show();
                                    }

                                    Network_Secondary_Check = false;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 2000);
                }
            });

            if(netWork_connect() && measurement_completed_send && !Senser_Secondary_Check&&!Network_Secondary_Check) {
                data_send_tcp = new TCP_Client();
                data_send_tcp.execute(this);
                mHandler2 = new Handler(Looper.getMainLooper());
                mHandler2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        customToastView("???????????? ?????? ?????????????????????.");
                    }
                }, 3000);
            }
            Network_Secondary_Check = false;
            Senser_Secondary_Check = false;
            mStartBtn.setText("?????? ??????");

        }
    }

    public void customToastView(String text){

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.send_success_toast, (ViewGroup) findViewById(R.id.Send_toast));
        TextView textView = layout.findViewById(R.id.toast);
        textView.setText(text);

        Toast toastView = Toast.makeText(getApplicationContext(),text, Toast.LENGTH_LONG);
        toastView.setGravity(Gravity.BOTTOM,0,50);
        toastView.setView(layout);
        toastView.show();
    }
    public void StartDelay() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        } // end if
        start_delay_dialog.show(); // ??????????????? ?????????
        myTimer = new MyTimer(3000, 1000);
        myTimer.start();

    }

    class MyTimer extends CountDownTimer {
        public MyTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            start_delay_text.setText(millisUntilFinished / 1000 + "");
        }

        @Override
        public void onFinish() {

            start_delay_dialog.dismiss();
            Start_measurement();
        }
    }

    public void NoSenser() {
        //   Toast.makeText(MainActivity.this, "????????? ?????????????????? ????????????.", Toast.LENGTH_SHORT).show();
        if (!mBtAdapter.isEnabled()) {
            // Log.e(TAG, "onClick - BT not enabled yet2");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            //// Log.e(TAG, "onClick - BT not enabled yet");
            //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
            Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
            startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
        }
    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            //   Log.e(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                //      Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            sneser_adress_save = preferences.getString("shortcut", null); // shortcut?????? ?????? ?????? ??? null?????? ????????? ?????????.
            if (!checkLocationServicesStatus()) {//?????? ????????? ?????? ??????
                //showDialogForLocationServiceSetting();//??????????????? ????????? ?????? ???????????? ??????
            } else if (checkLocationServicesStatus()) {
                //  if(rotation_sneser_adress_save==null) {
                   /* if (mBtAdapter.isEnabled() && sneser_adress_save != null) {
                        if (mService != null) {
                            mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(sneser_adress_save);
                            mService.connect(sneser_adress_save);
                            connect_check_flag = true;
                            Senser_check_flag=false;
                        }

               //     }
              /*  }else if(rotation_sneser_adress_save!=null) {
                    if (mBtAdapter.isEnabled()) {
                        if (mBtAdapter.isEnabled() && sneser_adress_save != null) {
                            if (mService != null) {
                                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(sneser_adress_save);
                                mService.connect(sneser_adress_save);
                                connect_check_flag = true;
                            }
                        }
                    }*/
                //}
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                if (connect_check_flag) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            //   Log.d(TAG, "UART_CONNECT_MSG");
                            // edtMessage.setEnabled(true);
                            try {
                                sleep(1100);
                                // btnSend.setEnabled(true);
                                // btnSend.setTextColor(Color.parseColor("#FFFFFF"));
                            } catch (Exception ignored) {

                            }
                            connect_check_flag = true;
                           // outo_connect_check_flag = true;
                            Senser_check_flag = false;
                            //  Saving_File_name = TagName + "_" + mDevice.toString().replace(":", "") + "_" + StartTime;
                            vw_txtmacaddrValue.setText(mDevice.getName() + "");
                            // mconnect.setText(deviceAddress);
                            // updateConnectionState(R.string.connected);
                            //      intervalTime.setClickable(false);
                            //       intervalTime.setFocusable(false);
                            // String delayTimeSTR = intervalTime.getText().toString();
                            //   delayTime = Integer.parseInt(delayTimeSTR);
                            dataToServer = "";
                            Toast.makeText(getApplication(), mDevice.getName() + " ????????????", Toast.LENGTH_SHORT).show();
                            //((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - ready");
                            //   listAdapter.add("[" + currentDateTimeString + "] Connected to: " + mDevice.getName());
                            // tv_result.setText("[" + currentDateTimeString + "] Connected to: " + mDevice.getName());
                            //del LISTView  messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                            mState = UART_PROFILE_CONNECTED;
                            invalidateOptionsMenu();
                            //Log.e("bbd", String.valueOf(action.equals(UartService.ACTION_DATA_AVAILABLE)));
                            countText.setText(measurement_timeer+" ???");
                        }

                    });
                }
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        //    Log.d(TAG, "UART_DISCONNECT_MSG");
                        mService.close();
                        connect_check_flag = false;
                       // outo_connect_check_flag = false;
                        Senser_check_flag = true;
                        End_measurement();
                        vw_txtmacaddrValue.setText("");
                        Toast.makeText(getApplication(), mDevice.getName() + " ?????? ??????", Toast.LENGTH_SHORT).show();
                        try {
                            sleep(1000);
                        } catch (Exception ex) {

                        }
                        senser_data_ok=false;

                        countText.setText(measurement_timeer+" ???");
                        invalidateOptionsMenu();
                    }
                });
            }
            //*********************//
            else if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                if (mService != null) {
                    mService.enableTXNotification();
                }

            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                //  Ball.setBackgroundColor(Color.parseColor("#ff0000"));
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            int receDataLength = txValue.length;
                            //  Log.e("bbb",UartService.ACTION_DATA_AVAILABLE);
                            if (receDataLength == 20) {
                                //????????? ??????
                                int BATTVal = txValue[0] & 0xff;
                                long Dcount = byteArrayToInt(txValue,2);
                                short accX = shortfrombyte(txValue, 6);
                                String accXString = String.format(Locale.KOREA,"%.2f", accX* 0.01);
                                short accY = shortfrombyte(txValue, 8);
                                String accYString = String.format(Locale.KOREA,"%.2f", accY* 0.01);
                                short accZ = shortfrombyte(txValue, 10);
                                String accZString = String.format(Locale.KOREA,"%.2f", accZ* 0.01);
                                short gyroX = shortfrombyte(txValue, 12);
                                short gyroY = shortfrombyte(txValue, 14);
                                short gyroZ = shortfrombyte(txValue, 16);
                                Log.e("senser acc", Dcount+","+accXString + ","+accYString+","+accZString + gyroX + ","+gyroY+","+gyroZ);
                                if (recording) {
                                    if (!Senser_check_flag) {
                                        //   msavess.setText(conta++ +"");
                                        String accdata  = accXString + ","+accYString+","+accZString;
                                        String rdata = Kamanfiter(gyroX, gyroY, gyroZ);
                                        long now = System.currentTimeMillis();
                                        lastsenserdata = now;
                                        dataToServer = DateUtil.get_yyyyMMddHHmmssSSS(now) + "," + Dcount+","+ accdata + "," + rdata;
                                        writeLog(dataToServer, filename);

                                        mStartBtn.setText("?????? ??????");
                                        if (timer_count <= 0) {
                                            End_measurement();
                                        }
                                    }
                                }
                            } else {
                                //    Log.e(TAG, "BLE DATA Length is " + receDataLength);
                            }

                        } catch (Exception e) {
                            //      Log.e(TAG, e.toString());
                        }

                    }
                });
            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();

            }
        }
    };

    public int byteArrayToInt(byte bytes[], int startIdx) {
        return ((((int) bytes[startIdx + 3] & 0xff) << 24) |
                (((int) bytes[startIdx + 2] & 0xff) << 16) |
                (((int) bytes[startIdx + 1] & 0xff) << 8) |
                (((int) bytes[startIdx + 0] & 0xff)));
    }
    public static float arr2float (byte[] arr, int start) {

        int i = 0;
        int len = 4;
        int cnt = 0;
        byte[] tmp = new byte[len];

        for (i = start; i < (start + len); i++) {
            tmp[cnt] = arr[i];
            cnt++;
        }

        int accum = 0;
        i = 0;
        for ( int shiftBy = 0; shiftBy < 32; shiftBy += 8 ) {
            accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
            i++;
        }
        return Float.intBitsToFloat(accum);
    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //  Log.d(TAG, "onDestroy()");
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            //    Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService = null;
    }

    @Override
    protected void onStop() {
        // Log.d(TAG, "onStop");
        super.onStop();
    }

   /* @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        finish();
    }*/

    @Override
    protected void onRestart() {
        super.onRestart();
        //   Log.d(TAG, "onRestart");
    }

    String getname1=null,getage1=null;
    int getgender1=0;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                    //   Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    // ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - connecting");
                    mService.connect(deviceAddress);
                    connect_check_flag = true;
                    Senser_check_flag = false;
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    //????????? ???????????? Editor??? ????????????.
                    SharedPreferences.Editor edit = preferences.edit();
                    edit.putString("shortcut", deviceAddress); //key, value??? ????????????
                    edit.apply();
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                    startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    //   Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    //  Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "??????????????? ????????? ?????? ????????? ", Toast.LENGTH_SHORT).show();
                    finish();
                }

                break;
            case REQUEST_USER_LIST:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    getname1 = data.getStringExtra("getday");//??????
                    getage1 = data.getStringExtra("getTime");//????????????
                    getgender1 = data.getIntExtra("getDistance",0);//????????????
                   // Log.e("see1", getname1+"  "+getage1+"   "+getgender1);
                    //  getname1 =  data.getStringExtra(mydatalist.getDay());
                    //  getage1 = data.getStringExtra("getTime");//????????????
                    user_name = getname1;
                    nameview.setText(user_name);
                   // nameview.setTextColor(Integer.parseInt("#ffffff"));
                    nameview.setBackgroundColor(Color.parseColor("#C06E44"));
                    if (getage1 == "????????????") {
                        user_age = " ";
                    } else {
                        user_age = getage1;
                    }
                    if (getgender1 == 0) {
                        user_gender = " ";
                    } else if (getgender1 == 1) {
                        user_gender = "???";
                    } else if (getgender1 == 2){
                        user_gender = "???";
                    }

                   // Log.e("see2", user_name+"  "+user_age+"   "+user_gender);

                }
                break;
            default:
                //Log.e(TAG, "wrong request code");
                break;
        }
    }

    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    private void writeLog(String data, String time) {

        String str_Path_Full = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (user_name != null && user_name.length() != 0) {
            str_Path_Full += "/Nineone/" + user_name + "/" + time + "/" + Saving_File_name;//????????????
        } else {
            str_Path_Full += "/Nineone" + File.separator + "NineOne" + File.separator + time + File.separator + Saving_File_name;
        }

        File file = new File(str_Path_Full);

        if (file.exists() == false) {
            try {
                file.createNewFile();
            } catch (IOException e) {
            }
        }
        try {
            BufferedWriter bfw;

            bfw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(str_Path_Full, true),  "EUC-KR"));
            bfw.write(data + "\r\n");
            //bfw.write(log_data);
            bfw.flush();
            bfw.close();
        } catch (FileNotFoundException e) {
            //  Log.e(TAG, e.toString());
        } catch (IOException e) {
            //  Log.e(TAG, e.toString());
        }
    }

    // Thread ?????????
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //  Log.e("?????? ",connect_check_flag + "");
        if (connect_check_flag) {
            if(senser_data_ok) {
                menu.findItem(R.id.exit).setVisible(true);
                menu.findItem(R.id.user_change).setVisible(true);
                menu.findItem(R.id.action_connect).setVisible(false);
                menu.findItem(R.id.action_disconnect).setVisible(true);
                menu.findItem(R.id.checkfalse).setVisible(false);
                menu.findItem(R.id.checkture).setVisible(true);
                //menu.findItem(R.id.action_request).setVisible(false);
            }else{
                menu.findItem(R.id.exit).setVisible(true);
                menu.findItem(R.id.user_change).setVisible(true);
                menu.findItem(R.id.action_connect).setVisible(false);
                menu.findItem(R.id.action_disconnect).setVisible(true);
                menu.findItem(R.id.checkfalse).setVisible(true);
                menu.findItem(R.id.checkture).setVisible(false);
            }
        } else if (!connect_check_flag) {
            menu.findItem(R.id.exit).setVisible(true);
            menu.findItem(R.id.user_change).setVisible(true);
            menu.findItem(R.id.action_connect).setVisible(true);
            menu.findItem(R.id.action_disconnect).setVisible(false);
            menu.findItem(R.id.checkfalse).setVisible(true);
            menu.findItem(R.id.checkture).setVisible(false);

            //menu.findItem(R.id.action_request).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private String requestName;

    @SuppressWarnings("unchecked")
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // ignore
            return true;
        }
        else if (itemId == R.id.exit) {
            if (!recording) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("?????? ??????");
                builder.setMessage("????????? ?????????????????????????");
                builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.setNegativeButton("??????", null);
                builder.show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("????????? ????????? ?????????");
                builder.setNegativeButton("??????", null);
                builder.show();
            }

            return true;
        } else if (itemId == R.id.action_connect) {

            if (!recording) {

                    NoSenser();

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("????????? ????????? ?????????");
                builder.setNegativeButton("??????", null);
                builder.show();
            }
            return true;
        } else if (itemId == R.id.action_disconnect) {
            if (!recording) {
                if (mDevice != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("?????? ??????");
                    builder.setMessage("????????? ?????????????????????????");
                    builder.setPositiveButton("???", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            connect_check_flag = false;
                          //  outo_connect_check_flag = false;
                            Senser_check_flag = true;
                            mService.disconnect();
                            End_measurement();
                            vw_txtmacaddrValue.setText(" ");
                            countText.setText(measurement_timeer+" ???");
                        }
                    });
                    builder.setNegativeButton("?????????", null);
                    builder.show();
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("????????? ????????? ?????????");
                builder.setNegativeButton("??????", null);
                builder.show();
            }
            return true;

        } else if (itemId == R.id.chartpage_move) {
            if (recording) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("?????? ??????");
                builder.setMessage("?????? ????????? ???????????? ????????? ??? ????????????.\n????????? ?????? ???????????????????");
                builder.setPositiveButton("???", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        End_measurement();
                    }
                });
                builder.setNegativeButton("?????????", null);
                builder.show();
            } else {

                Intent chrtclick = new Intent(getApplicationContext(), ChartList.class);//????????? ListClick?????? ?????? ??? ?????? ??????
                startActivity(chrtclick);
                return true;
            }
        } else if (itemId == R.id.videopage_move) {
            if (recording) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("?????? ??????");
                builder.setMessage("?????? ????????? ???????????? ????????? ??? ????????????.\n????????? ?????? ???????????????????");
                builder.setPositiveButton("???", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        End_measurement();
                    }
                });
                builder.setNegativeButton("?????????", null);
                builder.show();
            } else {
                Intent chrtclick2 = new Intent(getApplicationContext(), CVidioList.class);//????????? ListClick?????? ?????? ??? ?????? ??????
                startActivity(chrtclick2);
                //Shoolnaem_Change();

                return true;
            }
        } else if (itemId == R.id.user_change) {
            User_Change();
            return true;
        } else if (itemId == R.id.option) {
            if (recording) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("?????? ??????");
                builder.setMessage("?????? ????????? ????????? ????????? ??? ????????????.\n????????? ?????? ???????????????????");
                builder.setPositiveButton("???", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        End_measurement();
                    }
                });
                builder.setNegativeButton("?????????", null);
                builder.show();
            } else {
                startActivity(new Intent(MainActivity.this, OptionSetting.class));
                return true;
            }
        }
        return super.onMenuItemSelected(featureId, item);
    }

    public void User_Change() {
        if (!recording) {
            Intent intent4 = new Intent(MainActivity.this, UserListActivity.class);
            startActivityForResult(intent4, REQUEST_USER_LIST);

           // UserDialog();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("????????? ????????? ?????????");
            builder.setNegativeButton("??????", null);
            builder.show();
        }
    }
    public void Shoolnaem_Change() {
        if (!recording) {
            Intent intent5 = new Intent(MainActivity.this, user_aSchool_dialog.class);
            startActivity(intent5);

            // UserDialog();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("????????? ????????? ?????????");
            builder.setNegativeButton("??????", null);
            builder.show();
        }
    }
    private void ui_init() {
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        vw_txtmacaddrValue = (TextView) findViewById(R.id.macaddrValue);
        //intervalTime = (EditText) findViewById(R.id.interval);
        //   rcountValue = (TextView) findViewById(R.id.cons);
        //acceValue = (TextView) findViewById(R.id.acce);
        //  gyroValue = (TextView) findViewById(R.id.gyro);
        //  geoValue = (TextView) findViewById(R.id.geo);
        // rawdataValue = (TextView) findViewById(R.id.rawdataValue);
        //  intervalTime.setSelection(intervalTime.length());
    }

    private short shortfrombyte(byte[] RValue, int indexNo) {
        int r = RValue[indexNo + 1] & 0xFF;
        r = (r << 8) | (RValue[indexNo] & 0xFF);

        return (short) r;
    }

    private short shortfrombyte2(byte[] RValue, int indexNo) {
        int r = RValue[indexNo] & 0xFF;
        r = (r << 8) | (RValue[indexNo] & 0xFF);

        return (short) r;
    }

    private String Kamanfiter(int x, int y, int z) {
        String rdata = "";
        K = (PP + QQ) / (PP + QQ + RR);
        PP = RR * (PP + QQ) / (RR + PP + QQ);
        KAmx = KAmx + (x - KAmx) * K;
        KAmy = KAmy + (y - KAmy) * K;
        KAmz = KAmz + (z - KAmz) * K;
        rdata = KAmx + "," + KAmy + "," + KAmz;

        return rdata;

    }


    private OrientationEventListener orientationListener = null;

    public void surfaceCreated(SurfaceHolder holder) {//??????????????? ?????? ???????????? ????????? ??????
        orientationListener.enable();
        camera = Camera.open();
        // camera.setDisplayOrientation(90);
        Display display = MainActivity.this.getWindowManager().getDefaultDisplay();
        // ????????? ?????? ??????
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                camera.setDisplayOrientation(90);
                shooting_Angle = 90;
                break;
            case Surface.ROTATION_90:
                camera.setDisplayOrientation(0);
                shooting_Angle = 0;
                break;
            case Surface.ROTATION_180:
                camera.setDisplayOrientation(90);
                shooting_Angle = 90;
                break;
            case Surface.ROTATION_270:
                camera.setDisplayOrientation(180);
                shooting_Angle = 180;
                break;
        }
      /*  try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
    }




    public void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {//?????????????????? 180??? ????????? ???????????? ?????? ??????
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        if (rotation != rotation2) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
            }
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360; // compensate the mirror
            } else { // back-facing
                result = (info.orientation - degrees + 360) % 360;
                if (shooting_Angle == 0 && shooting_Angle != result) {
                    shooting_Angle = result;
                }
                if (shooting_Angle == 180 && shooting_Angle != result) {
                    shooting_Angle = result;
                }
                result2 = result;
            }

            camera.setDisplayOrientation(result);
        }
        rotation2 = rotation;
        shooting_Angle2 = shooting_Angle;

    }

    private void refreshCamera(Camera camera) {
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        try {
            camera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setCamera(camera);
    }

    private void setCamera(Camera cam) {
        camera = cam;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (recording) {
            camera.startPreview();
            recording = false;
        }

        if (camera != null) {
            try {
                parameters = camera.getParameters();
                mZoomSeekBar.setOnSeekBarChangeListener(ZooseekBar);
                //  parameters.setColorEffect(Camera.Parameters.EFFECT_SEPIA);
                parameters.setZoom(zoom);
                //  parameters.getMaxZoom();
                //   requestLayout();
                camera.setParameters(parameters);
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                setCameraDisplayOrientation(this, cameraId, camera);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //    refreshCamera(camera);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        orientationListener.disable();
    }

    private SeekBar.OnSeekBarChangeListener ZooseekBar = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            //Camera.Parameters parameters = camera.getParameters();

            zoom = progress;
            parameters.setZoom(zoom);

            camera.setParameters(parameters);
            camera.startPreview();

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        // Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            //  Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        OptionCheckSetting();
        End_measurement();
        //invalidateOptionsMenu();
        // restoreState();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            decorView.setSystemUiVisibility(uiOption);
        }
    }


    @Override
    protected void onUserLeaveHint() {//???????????? ????????????
        super.onUserLeaveHint();
        if (recording) {
            End_measurement();
        }

    }

    private void GPSSetting() {
        ContentResolver res = getContentResolver();

        boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(res, LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("GPS ??????")
                    .setMessage("GPS??? ?????????????????????????")
                    .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);

                        }
                    })
                    .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        }
    }

    private void NosenserNoname() {
        if (!recording) {
            Log.e("ddssdd","ddssdd2");
            Senser_connect();
        } else {
            if (timer_count < measurement_timeer) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("?????? ??????");
                builder.setMessage("????????? ?????????????????????????");
                builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Senser_Secondary_Check = true;
                        End_measurement();
                    }
                });
                builder.setNegativeButton("??????", null);
                builder.show();
            }
        }
    }

    private void YessenserNoname() {
        if (connect_check_flag) {
            if (!recording) {
                Log.e("ddssdd","ddssdd");
                StartDelay();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("?????? ??????");
                builder.setMessage("????????? ?????????????????????????");
                builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Senser_Secondary_Check = true;
                        End_measurement();
                    }
                });
                builder.setNegativeButton("??????", null);
                builder.show();

            }
        }
        if (!connect_check_flag) {
            Toast.makeText(MainActivity.this, "????????? ?????????????????? ????????????.", Toast.LENGTH_SHORT).show();
            NoSenser();
        }
    }

    private void NosenserYesname() {
        if (user_name == "" || user_name == null || user_name.trim().isEmpty()) {
            Toast.makeText(getApplication(), "???????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
            user_name = nameview.getText().toString();
            User_Change();
        } else {
            if (!recording) {
                Senser_connect();
            } else {
                if (timer_count < measurement_timeer) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("?????? ??????");
                    builder.setMessage("????????? ?????????????????????????");
                    builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Senser_Secondary_Check = true;
                            End_measurement();
                        }
                    });
                    builder.setNegativeButton("??????", null);
                    builder.show();
                }
            }
        }
    }

    private void YessenserYesname() {
        if (connect_check_flag) {
            if (user_name == "" || user_name == null || user_name.trim().isEmpty()) {
                Toast.makeText(getApplication(), "???????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
                User_Change();
            } else {
                if (!recording) {
                    StartDelay();
                } else {
                    if (timer_count < measurement_timeer) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("?????? ??????");
                        builder.setMessage("????????? ?????????????????????????");
                        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Senser_Secondary_Check = true;
                                End_measurement();
                            }
                        });
                        builder.setNegativeButton("??????", null);
                        builder.show();
                    }
                }
            }
        } else if (!connect_check_flag) {
            Toast.makeText(MainActivity.this, "????????? ?????????????????? ????????????.", Toast.LENGTH_SHORT).show();
            NoSenser();
        }

    }


    private Button.OnClickListener mClickListener = new View.OnClickListener() {//??? ?????? ???????????????
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Save_start:
                    if (measurement_completed_send) {
                        if (netWork_connect()) {
                            Option_state();
                        } else {

                            Measurement_completed_send();
                        }
                    } else {
                        Option_state();
                    }
                    break;

            }
        }
    };

    public void Measurement_completed_send() {
        if (!recording) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("?????? ??? ????????? ?????????");
            builder.setMessage("??????????????? ???????????? ?????? ?????? ??? ????????? ????????? ?????? ????????????.\n?????? ?????? ?????? ???????????????????");
            builder.setPositiveButton("???", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Network_Secondary_Check = true;
                    Option_state();
                }
            });
            builder.setNegativeButton("?????????", null);
            builder.show();
        } else {
            if (netWork_connect()) {
                Option_state();
            } else {
                Option_state();
            }

        }

    }

    private void Option_state() {
        if (senser_necessary_save) {
            if (name_necessary_save) {
                YessenserYesname();
            } else {
                YessenserNoname();
            }
        } else {
            if (name_necessary_save) {
                NosenserYesname();
            } else {
                NosenserNoname();
            }
        }
    }
    boolean Senser_Secondary_Check;
    public void Senser_connect() {
        if (!connect_check_flag&&measurement_completed_send) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("?????? ??????");
            builder.setMessage("????????? ???????????? ?????? ?????? ???????????? ?????? ?????? ????????????.\n??????????????? ?????????????????????????");
            builder.setPositiveButton("???", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Senser_Secondary_Check=true;
                    StartDelay();
                }
            });
            builder.setNegativeButton("?????????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    NoSenser();
                }
            });
            builder.show();
        } else {
            StartDelay();
        }
    }

    private boolean netWork_connect() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ninfo = cm.getActiveNetworkInfo();
        if (ninfo == null) {
            return false;
        } else {
            return true;
        }
    }

    boolean Network_Secondary_Check = false;
    boolean measurement_completed_send = false;
    int measurement_timeer=60;
    private void OptionCheckSetting() {
        SharedPreferences sp = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        boolean send = sp.getBoolean("snedcheck", false);
        boolean senser = sp.getBoolean("essential_sendser", true);
        boolean user = sp.getBoolean("essential_user", false);

        if (send) {
            measurement_completed_send = true;
            Toast.makeText(this, "??????" + measurement_completed_send, Toast.LENGTH_SHORT);
        } else {
            measurement_completed_send = false;
            Toast.makeText(this, "??????" + measurement_completed_send, Toast.LENGTH_SHORT);
        }
        if (senser) {
            senser_necessary_save = true;
            Toast.makeText(this, "???????????? ?????? ON", Toast.LENGTH_SHORT);

        } else {
            senser_necessary_save = false;
            Toast.makeText(this, "???????????? ?????? OFF", Toast.LENGTH_SHORT);
        }
        if (user) {
            name_necessary_save = true;

            Toast.makeText(this, "???????????? ?????? ON", Toast.LENGTH_SHORT);

        } else {
            name_necessary_save = false;

            Toast.makeText(this, "???????????? ?????? OFF", Toast.LENGTH_SHORT);
        }
        //ListPreference LP = (ListPreference)findPreference("measurement_time");
        String timer1 = sp.getString("measurement_time","false");
        if("30 ???".equals(timer1)){
            measurement_timeer=30;
            countText.setText(measurement_timeer+" ???");
        }else if("60 ???".equals(timer1)){
            measurement_timeer=60;
            countText.setText(measurement_timeer+" ???");
        }else if("90 ???".equals(timer1)){
            measurement_timeer=90;
            countText.setText(measurement_timeer+" ???");
        }else if("120 ???".equals(timer1)){
            measurement_timeer=120;
            countText.setText(measurement_timeer+" ???");
        }else if("150 ???".equals(timer1)){
            measurement_timeer=150;
            countText.setText(measurement_timeer+" ???");
        }else if("180 ???".equals(timer1)){
            measurement_timeer=180;
            countText.setText(measurement_timeer+" ???");
        }else{
            measurement_timeer=60;
        }
    }


    private DataInputStream data_send;
    public class TCP_Client extends AsyncTask {
        protected String SERV_IP = "10.10.10.162"; //????????? ip????????? ???????????? ?????????.
        protected int PORT = 7210; //????????? Port????????? ???????????? ?????????.
        String file_path;
        @Override
        protected Object doInBackground(Object... params) {
            Log.e("TCP", "server connecting");
            if (user_name != null && user_name.length() != 0) {
                file_path = Environment.getExternalStorageDirectory() + File.separator + "Nineone" + File.separator + user_name + File.separator + filename;
            } else {
                file_path = Environment.getExternalStorageDirectory() + File.separator + "Nineone" + File.separator + "NineOne" + File.separator + filename;;
            }
            Log.e("TCP", "server connecting");
            File[] files2 = new File(file_path).listFiles();
            for (int i = 0; i < files2.length; i++) {

                String str = files2[i].getName().substring(files2[i].getName().length() - 3);
             //   Log.e("ddd", str);
                if (str.equals("csv")) {
                    try {
                        Log.d("TCP", "server connecting");
                        InetAddress serverAddr = InetAddress.getByName(SERV_IP);
                        Socket sock = new Socket(serverAddr, PORT);

                        try {
                            System.out.println("??????????????????");
                            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())), true);
                            if (user_name != null && user_name.length() != 0) {
                                out.println(files2[i].getName() + "\n" + user_name.replace(" ", "_") + "\n" + androidid + "\n" + filename);
                            } else {
                                out.println(files2[i].getName() + "\n" + "Nineone" + "\n" + androidid + "\n" + filename);
                            }

                            out.flush();
                            data_send = new DataInputStream(new FileInputStream(new File(file_path + File.separator + files2[i].getName())));

                          //  Log.e("aaa5", files2[i].getName() + "--" + file_path + "-" + filename);
                            DataInputStream dis = data_send;
                            DataOutputStream dos = new DataOutputStream(sock.getOutputStream());

                            // long fileSize = file1.length();
                            byte[] buf = new byte[104857600];

                            long totalReadBytes = 0;
                            int readBytes;
                            System.out.println("??????????????? ???");

                            while ((readBytes = dis.read(buf)) > 0) { //?????? ???????????? ????????? ????????????.

                                dos.write(buf, 0, readBytes);
                                totalReadBytes += readBytes;
                            }
                            dos.flush();
                            System.out.println("?????????????????? ??? ??????");
                            dos.close();
                            System.out.println("????????????");
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

    boolean chice_gender=true;



}