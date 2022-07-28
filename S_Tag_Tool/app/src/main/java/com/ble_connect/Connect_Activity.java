package com.ble_connect;

import static java.lang.Thread.sleep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.nineone.s_tag_tool.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Connect_Activity extends AppCompatActivity {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    public static final String TAG = "Stag Main";

    private UartService mService = null;
    private BluetoothDevice mbluetootDevice = null;
    private BluetoothAdapter mBtAdapter = null;

    private ArrayAdapter<String> listAdapter;

    public static String TagName, StartTime;

    TextView vw_txtmacaddrValue;
    TextView vw_txt_tag_adress;
    TextView gyo_cal;
    EditText tag_no, tag_copy_no;
    EditText O2alarm, COalarm, H2Salarm, CO2alarm, CH4alarm;
    String O2alarm_string, COalarm_string, H2Salarm_string, CO2alarm_string, CH4alarm_string;
    Button btn_sendtotag;

    String dataToServer;
    // int delayTime;
    private int rcount = 0;
    private int cetting_count = 0;
    private boolean cetting_boolean = false;
    String Saving_File_name;

    boolean connect_check_flag = false;

    private final String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
    };
    private static final int MULTIPLE_PERMISSIONS = 101;
    private TextView senser_tag_type_Text = null;
    private Spinner rfPowerSpinner = null;
    private Spinner sensorSpinner = null;
    private Spinner calSpinner = null;
    private Spinner sensor_operation_cycle_Spinner = null;
    private Spinner senser_tag_copy_type_Spinner = null;
    private boolean connect_fail = false;
    private String phonenumber;
    private RequestQueue requestQueue;
    private LinearLayout mCA_layout, mD5_layout;
    private boolean D5_true_false = false;
    private String O2alarm_HTTP_string;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("연결");
        mCA_layout = findViewById(R.id.CA_layout);
        mCA_layout.setVisibility(View.VISIBLE);
        mD5_layout = findViewById(R.id.D5_layout);
        mD5_layout.setVisibility(View.GONE);
        D5_true_false = false;
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();

            finish();
            return;
        }
        if (requestQueue == null) {
            // requestQueue 초기화
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }


        TelephonyManager phonData = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
        }
        try {
            phonenumber = phonData.getLine1Number();
            if (phonenumber == null) {
                phonenumber = "01000000000";
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }


        service_init();
        ui_init();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

    /*   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {                                  // 안드로이드 6.0 이상일 경우 퍼미션 체크
            checkPermissions();
        }*/
        //tag type setting
        senser_tag_type_Text = findViewById(R.id.tag_type_text);
        //RF setting
        rfPowerSpinner = findViewById(R.id.rfPower);
        String[] models = getResources().getStringArray(R.array.my_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(), R.layout.custom_spinner_list, models);
        adapter.setDropDownViewResource(R.layout.customer_spinner);
        rfPowerSpinner.setAdapter(adapter);

        rfPowerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {// textView.setText(items[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {//textView.setText("선택하세요");
            }
        });
        rfPowerSpinner.setSelection(0);
        //Sensor 셋팅.

        String[] sensorItems;
        sensorSpinner = findViewById(R.id.sensor_type);
        sensorItems = getResources().getStringArray(R.array.my_sensor);
        ArrayAdapter<String> sensoradapter =
                new ArrayAdapter<>(getBaseContext(), R.layout.custom_spinner_list, sensorItems);
        sensoradapter.setDropDownViewResource(R.layout.customer_spinner);
        sensorSpinner.setAdapter(sensoradapter);

        sensorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {// textView.setText(items[position]);
                Log.e("onItemSelected", (position) + ", " + sensorSpinner.getSelectedItem().toString());
                ui_ture_false(position);
                // sensorType =position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {//textView.setText("선택하세요");
            }
        });
        // sensorSpinner.setSelection(0);
        //자이로 칼 여부
        String[] calItems;
        calSpinner = findViewById(R.id.cal_run);
        calItems = getResources().getStringArray(R.array.my_cal);
        ArrayAdapter<String> caladapter =
                new ArrayAdapter<>(getBaseContext(), R.layout.custom_spinner_list, calItems);
        caladapter.setDropDownViewResource(R.layout.customer_spinner);
        calSpinner.setAdapter(caladapter);

        calSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String cal_check_status = gyo_cal.getText().toString();
                if (cal_check_status.equals("보정 완료") && position == 1) {
                    //Toast.makeText(getApplication(), "Cal On..\r\n" + position, Toast.LENGTH_LONG).show();
                    new AlertDialog.Builder(Connect_Activity.this)
                            .setTitle("센서 보정")
                            .setMessage("다시 한번 센서 보정을 하시겠습니까 ?")
                             .setCancelable(false)
                            .setIcon(R.drawable.nrfuart_hdpi_icon)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // 확인시 처리 로직
                                    Toast.makeText(getApplication(), "전송 버튼을 선택하면 센서 보정을 실시합니다.", Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // 취소시 처리 로직
                                    selectValue(calSpinner, "Off");
                                    Toast.makeText(getApplication(), "선택을 취소 하였습니다.", Toast.LENGTH_LONG).show();
                                }
                            })
                            .show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //textView.setText("선택하세요");
            }
        });

        String[] sensor_operation_cycle_Items;
        sensor_operation_cycle_Spinner = findViewById(R.id.operation_cycle_mode_Spinner);
        sensor_operation_cycle_Items = getResources().getStringArray(R.array.my_operation);
        ArrayAdapter<String> sensor_operation_cycle_adapter =
                new ArrayAdapter<String>(getBaseContext(), R.layout.custom_spinner_list, sensor_operation_cycle_Items);
        sensor_operation_cycle_adapter.setDropDownViewResource(R.layout.customer_spinner);
        sensor_operation_cycle_Spinner.setAdapter(sensor_operation_cycle_adapter);

        sensor_operation_cycle_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {//textView.setText("선택하세요");
            }
        });
        sensor_operation_cycle_Spinner.setSelection(0);

        senser_tag_copy_type_Spinner = findViewById(R.id.tag_copy_type_d5_spinner);
        String[] tag_models = getResources().getStringArray(R.array.tag_type);
        ArrayAdapter<String> tag_spinner_adapter = new ArrayAdapter<>(getBaseContext(), R.layout.custom_spinner_list, tag_models);
        tag_spinner_adapter.setDropDownViewResource(R.layout.customer_spinner);
        senser_tag_copy_type_Spinner.setAdapter(tag_spinner_adapter);

        senser_tag_copy_type_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {// textView.setText(items[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {//textView.setText("선택하세요");
            }
        });
        senser_tag_copy_type_Spinner.setSelection(0);


        btn_sendtotag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send....
                if (connect_check_flag) {
                    //
                    byte[] tagsend_data = new byte[20];
                    tagsend_data[0] = 0x02;
                    tagsend_data[1] = 0x01;
                    tagsend_data[19] = 0x03;

                    boolean send_ok_check = true;
                    String tag_type_val = senser_tag_type_Text.getText().toString();
                    // String tag_type_val = (senser_tag_type_Spinner.getSelectedItem().toString());
                    int tagtype_val = Integer.parseInt(tag_type_val, 16);

                    if (tagtype_val > 0) {
                        tagsend_data[2] = (byte) tagtype_val;
                        tagsend_data[3] = (byte) (tagtype_val >> 8);
                    } else {
                        send_ok_check = false;
                        customToastView("[단말기 타입] 선택해 주세요.");
                    }
                    //단말기 번호 데이터
                    String tag_no_var = tag_no.getText().toString().trim();
                    if (tag_no_var.length() > 0) {
                        boolean isNumeric = tag_no_var.matches("[+-]?\\d*(\\.\\d+)?");
                        if (isNumeric) {
                            long tag_no_val = Long.parseLong(tag_no_var);
                            if (tag_no_val > 0) {
                                tagsend_data[4] = (byte) (tag_no_val);
                                tagsend_data[5] = (byte) (tag_no_val >> 8);
                                tagsend_data[6] = (byte) (tag_no_val >> 16);
                                tagsend_data[7] = (byte) (tag_no_val >> 24);

                            } else {
                                send_ok_check = false;
                                // Toast.makeText(getApplication(), "[단말기 번호] 입력한 데이터가 너무 크거나 적습니다.", Toast.LENGTH_LONG).show();
                                customToastView("[단말기 번호] 입력한 데이터가 너무 크거나 적습니다.");
                            }
                        } else {
                            send_ok_check = false;
                            // Toast.makeText(getApplication(), "[단말기 번호] 입력한 데이터 타입이 안 맞습니다", Toast.LENGTH_LONG).show();

                            customToastView("[단말기 번호] 입력한 데이터 타입이 안 맞습니다");
                        }
                    } else {
                        send_ok_check = false;
                        // Toast.makeText(getApplication(), "[단말기 번호] 정보를 입력해 주세요", Toast.LENGTH_LONG).show();
                        customToastView("[단말기 번호] 정보를 입력해 주세요");

                    }

                    int rfPower_val = Integer.parseInt(rfPowerSpinner.getSelectedItem().toString());
                    tagsend_data[8] = (byte) rfPower_val;
                    if (!D5_true_false) {
                        //Sensor Type 가져오기
                        String sensortype_val = (sensorSpinner.getSelectedItem().toString());
                        int sensor_type_var = 0;
                        switch (sensortype_val) {
                            case "0(없음)":
                                sensor_type_var = 0;
                                break;
                            case "1(O2)":
                                sensor_type_var = 1;
                                break;
                            case "2(5종)":
                                sensor_type_var = 2;
                                break;
                        }
                        tagsend_data[9] = (byte) sensor_type_var;

                        //센서 Cal 동작 여부 가져오기
                        String cal_run = (calSpinner.getSelectedItem().toString());
                        int cal_run_var = 0;
                        if ("On".equals(cal_run)) {
                            cal_run_var = 1;
                        }
                        //Log.e("칼 실행 여부", )
                        tagsend_data[10] = (byte) cal_run_var;

                        int sensor_operation_cycle_val = Integer.parseInt(sensor_operation_cycle_Spinner.getSelectedItem().toString());
                        tagsend_data[11] = (byte) sensor_operation_cycle_val;
                        O2alarm_string = O2alarm.getText().toString().trim().replace(" ", "");

                        if (O2alarm_string.length() != 0) {
                            float tag_O2alarm = Float.parseFloat(O2alarm_string);
                            //   int tag_O2alarm2 = (int) Long.parseLong(O2alarm.getText().toString().trim(), 16);
                            tagsend_data[12] = (byte) (tag_O2alarm * 10);
                        } else {
                            O2alarm_string = "0";
                            tagsend_data[12] = 0;
                        }
                        COalarm_string = COalarm.getText().toString().trim().replace(" ", "");
                        if (COalarm_string.length() != 0) {
                            int tag_COalarm = Integer.parseInt(COalarm_string);
                            int tag_COalarm_set = tag_COalarm / 10;
                            tagsend_data[13] = (byte) tag_COalarm_set;
                        } else {
                            COalarm_string = "0";
                            tagsend_data[13] = 0;
                        }

                        H2Salarm_string = H2Salarm.getText().toString().trim().replace(" ", "");
                        if (H2Salarm_string.length() != 0) {
                            int tag_H2Salarm = Integer.parseInt(H2Salarm.getText().toString().trim());
                            tagsend_data[14] = (byte) tag_H2Salarm;
                        } else {
                            H2Salarm_string = "0";
                            tagsend_data[14] = 0;
                        }

                        CO2alarm_string = CO2alarm.getText().toString().trim().replace(" ", "");
                        if (CO2alarm_string.length() != 0) {
                            int tag_CO2alarm = Integer.parseInt(CO2alarm_string);
                            tagsend_data[15] = (byte) (tag_CO2alarm);
                            tagsend_data[16] = (byte) (tag_CO2alarm >> 8);
                        } else {
                            CO2alarm_string = "0";
                            tagsend_data[15] = 0;
                            tagsend_data[16] = 0;
                        }
                        CH4alarm_string = CH4alarm.getText().toString().trim().replace(" ", "");
                        if (CH4alarm_string.length() != 0) {
                            int tag_CH4alarm = Integer.parseInt(CH4alarm_string);
                            tagsend_data[17] = (byte) (tag_CH4alarm);
                            tagsend_data[18] = (byte) (tag_CH4alarm >> 8);

                            Log.e("ashex", asHex(tagsend_data));
                        } else {
                            CH4alarm_string = "0";
                            tagsend_data[17] = 0;
                            tagsend_data[18] = 0;
                        }
                    } else {
                        String tag_type_copy_val = (senser_tag_copy_type_Spinner.getSelectedItem().toString());
                        int tagtype_copy_val = Integer.parseInt(tag_type_copy_val);

                        if (tagtype_copy_val > 0) {
                            tagsend_data[9] = (byte) (tagtype_copy_val);
                            tagsend_data[10] = (byte) (tagtype_copy_val >> 8);
                        } else {
                            send_ok_check = false;
                            customToastView("[단말기 타입] 선택해 주세요.");
                        }

                        String tag_copy_no_var = tag_copy_no.getText().toString().trim();
                        if (tag_copy_no_var.length() > 0) {
                            boolean isNumeric = tag_copy_no_var.matches("[+-]?\\d*(\\.\\d+)?");
                            if (isNumeric) {
                                long tag_no_val = Long.parseLong(tag_copy_no_var);
                                if (tag_no_val > 0) {
                                    tagsend_data[11] = (byte) (tag_no_val);
                                    tagsend_data[12] = (byte) (tag_no_val >> 8);
                                    tagsend_data[13] = (byte) (tag_no_val >> 16);
                                    tagsend_data[14] = (byte) (tag_no_val >> 24);

                                } else {
                                    send_ok_check = false;
                                    // Toast.makeText(getApplication(), "[단말기 번호] 입력한 데이터가 너무 크거나 적습니다.", Toast.LENGTH_LONG).show();
                                    customToastView("[카피 단말기 번호] 입력한 데이터가 너무 크거나 적습니다.");
                                }
                            } else {
                                send_ok_check = false;
                                // Toast.makeText(getApplication(), "[단말기 번호] 입력한 데이터 타입이 안 맞습니다", Toast.LENGTH_LONG).show();

                                customToastView("[카피 단말기 번호] 입력한 데이터 타입이 안 맞습니다");
                            }
                        } else {
                            send_ok_check = false;
                            // Toast.makeText(getApplication(), "[단말기 번호] 정보를 입력해 주세요", Toast.LENGTH_LONG).show();
                            customToastView("[카피 단말기 번호] 정보를 입력해 주세요");

                        }
                        tagsend_data[15] = 0;
                        tagsend_data[16] = 0;
                        tagsend_data[17] = 0;
                        tagsend_data[18] = 0;
                    }
                    if (send_ok_check) {
                        long countnow = System.currentTimeMillis();
                        SimpleDateFormat aftertime = new SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.KOREA);
                        String nowtime = aftertime.format(countnow);
                        //   String[] DeviceNameArray = mbluetootDevice.getName().trim().split("-");
                        String send_hex_data = asHex(tagsend_data);
                        Log.e("전송 데이터", send_hex_data);
                        mService.writeRXCharacteristic(tagsend_data);
                        //  customToastView("전송 완료");
                        connect_fail = false;
                        if (!D5_true_false) {
                            String senser_data = O2alarm_string + ',' + COalarm_string + ',' + H2Salarm_string + ',' + CO2alarm_string + ',' + CH4alarm_string;
                            String send_data = phonenumber + "," + tag_no.getText().toString().trim() + "," + nowtime + "," + senser_data;
                            Network_Confirm(send_data);
                        } else {
                            String senser_data = senser_tag_copy_type_Spinner.getSelectedItem().toString() + "," + tag_copy_no.getText().toString();
                            String send_data = phonenumber + "," + tag_no.getText().toString().trim() + "," + nowtime + "," + senser_data;
                            Network_Confirm(send_data);
                        }
                        runOnUiThread(new Runnable() {//약간의 딜레이
                            @Override
                            public void run() {
                                ProgressDialog mProgressDialog = ProgressDialog.show(Connect_Activity.this, "", "전송 중 입니다.", true);
                                data_send_handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                                mProgressDialog.dismiss();
                                                AlertDialog.Builder builder = new AlertDialog.Builder(Connect_Activity.this);
                                                builder.setTitle("전송 완료");
                                                builder.setMessage("리스트로 돌아갑니다.");
                                                builder.setCancelable(false);
                                                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        onBackPressed();
                                                    }
                                                });
                                                builder.show();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, 2000);
                            }
                        });
                    }
                } else {
                    customToastView("태그를 연결해 주세요");


                }
            }
        });
        Runnable runnable10 = new Runnable() {
            @Override
            public void run() {
                //  startScan();

                senser_adress_connect();
            }
        };
        connect_handler.postDelayed(runnable10, 2000);
        // Initialize();
    }

    private boolean mConnecting_true = false;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                //  Intent intent = new Intent(Connect_Activity.this, MainActivity.class);
                //  startActivity(intent);
                finish();
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };
    private final MyHandler data_send_handler = new MyHandler(this);
    private final MyHandler connect_handler = new MyHandler(this);
    private final MyHandler D5_connect_handler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<Connect_Activity> mActivity;

        public MyHandler(Connect_Activity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

        }
    }

    private void senser_adress_connect() {
        Intent intent = getIntent();
        String sneser_adress_save = intent.getStringExtra("address");
        Log.e("STag", "351");
        connect_fail = true;
        if (mBtAdapter.isEnabled() && sneser_adress_save != null) {
            if (mService != null) {
                mbluetootDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(sneser_adress_save);
                // mConnecting_true = true;
                mService.connect(sneser_adress_save);
                //    connect_check_flag = true;
            }
        }
    }

    long startDate = System.currentTimeMillis();
    boolean first_only_run = true;
    String result = "";

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("MyReceiver", "Intent: $intent");
            //final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        btn_sendtotag.setBackgroundTintList(ContextCompat.getColorStateList(getApplication(), R.color.blue));

                        //  btn_sendtotag.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.blue));
                        btn_sendtotag.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");

                        vw_txtmacaddrValue.setText(mbluetootDevice.getName());
                        String stringbluetootDevice = String.valueOf(mbluetootDevice);
                        vw_txt_tag_adress.setText(stringbluetootDevice);
                        customToastView(mbluetootDevice.getName() + " 연결");

                        try {
                            sleep(1100);
                        } catch (Exception ignored) {

                        }
                        //
                        startDate = System.currentTimeMillis();
                        first_only_run = true;
                        result = "";
                        //

                        //  mConnecting_true = false;
                        connect_check_flag = true;
                        Saving_File_name = TagName + "_" + mbluetootDevice.toString().replace(":", "") + "_" + StartTime;

                        dataToServer = "";

                        listAdapter.add("[" + currentDateTimeString + "] Connected to: " + mbluetootDevice.getName());
                        invalidateOptionsMenu();
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        // btnConnectDisconnect.setText(R.string.str_connect);
                        btn_sendtotag.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
                        //  btn_sendtotag.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.gray3));
                        btn_sendtotag.setBackgroundTintList(ContextCompat.getColorStateList(getApplication(), R.color.gray3));
                        connect_check_flag = false;
                        cetting_boolean = false;
                        // mConnecting_true = false;
                        cetting_count = 0;
                        UI_reset();

                        listAdapter.add("[" + currentDateTimeString + "] Disconnected to: " + mbluetootDevice.getName());

                        mService.close();
                        //setUiState();


                        //  Toast.makeText(getApplication(), mbluetootDevice.getName() + " 연결해제", Toast.LENGTH_SHORT).show();
                        if (connect_fail && !D5_connect_true) {
                            customToastView(mbluetootDevice.getName() + " 연결실패");
                        } else if (!connect_fail && !D5_connect_true) {
                            customToastView(mbluetootDevice.getName() + " 연결해제");
                        }/*else if(D5_connect_true){
                          //  customToastView("D5 확인. 연결 중");
                        }*/
                        connect_fail = false;
                        try {
                            sleep(100);
                        } catch (Exception ignored) {

                        }
                        invalidateOptionsMenu();

                    }
                });
            }


            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
                // Log.e("730", String.valueOf(mService.getSupportedGattServices()));
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                //  Log.e("STag","Data rece....");\
                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);

                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            // Log.e("ble_datae2",byteArrayToHex(txValue)+",");

                            int receDataLength = txValue.length;
                            //    Log.e("Tag_Rece12", String.valueOf(receDataLength));
                            if (receDataLength == 20) {
                                //  Log.e("Tag_Rece1", txValue[0]+", "+txValue[19]);

                                if (cetting_count > 30 && !cetting_boolean) {
                                    cetting_boolean = true;
                                    customToastView("초기세팅이 되어있지 않습니다.\n초기 세팅을 해주세요.");

                                }
                                cetting_count++;
                                if (txValue[0] == 0x02 && txValue[19] == 0x03) {
                                    rcount++;
                                    cetting_count = 0;
                                    cetting_boolean = true;
                                    //  Log.e("Tag_number753", "753");
                                    if (rcount == 1) {
                                        String r_data_hex = asHex(txValue);
                                        int rece_tag_type = ConvertToIntLittle(txValue, 2);

                                        String tag_tag_type_str = String.format("%04X", rece_tag_type);

                                        senser_tag_type_Text.setText(tag_tag_type_str);

                                        int rece_tag_no = ConvertToIntLittle2(txValue, 4);
                                        tag_no.setText(rece_tag_no + "");

                                        int rece_rf_power = txValue[8];
                                        selectValue(rfPowerSpinner, rece_rf_power);
                                        if (!D5_true_false) {
                                            int rece_sensor_type = txValue[9] & 0xff;
                                            String sensor_type_str = "";
                                            switch (rece_sensor_type) {
                                                case 0:
                                                    sensor_type_str = "0(없음)";
                                                    break;
                                                case 1:
                                                    sensor_type_str = "1(O2)";
                                                    break;
                                                case 2:
                                                    sensor_type_str = "2(5종)";
                                                    break;
                                            }
                                            //   Log.e("Tag Rece Sensor Type: ", rece_sensor_type + "/" + sensor_type_str);
                                            selectValue(sensorSpinner, sensor_type_str);

                                            int rece_cal_status = txValue[10] & 0xff;
                                            String gro_cal_status = "보정 안됨 ";
                                            //   String calSpinnertext ="Off";
                                            if (rece_cal_status == 1) {
                                                gro_cal_status = "보정 완료";
                                                //   calSpinnertext = "On";
                                            }
                                            //   Log.e("Tag Rece cal Type: ", rece_cal_status + "/" + gro_cal_status);
                                            gyo_cal.setText(gro_cal_status + "");
                                            //selectValue(calSpinner, calSpinnertext);

                                       /* int rece_sensor_power_on = txValue[11] & 0xff;
                                        Log.e("Tag sensor Power On: ", rece_sensor_power_on + "");
                                        selectValue(sensor_power_on_Spinner, rece_sensor_power_on);*/

                                            int rece_operation_cycle_on = txValue[11] & 0xff;
                                            // Log.e("Tag Operation Cycle On: ", rece_operation_cycle_on + "");
                                            selectValue(sensor_operation_cycle_Spinner, rece_operation_cycle_on);
                                            int rece_O2_no = txValue[12] & 0xff;
                                            double rece_O2_no2 = (rece_O2_no * 0.1);
                                            O2alarm.setText(String.valueOf(rece_O2_no2));
                                            //   if (rece_sensor_type == 2) {
                                            int rece_CO_no = txValue[13] & 0xff;
                                            int rece_CO_no_SET = rece_CO_no * 10;

                                            COalarm.setText(String.valueOf(rece_CO_no_SET));
                                            int rece_H2S_no = txValue[14] & 0xff;
                                            H2Salarm.setText(String.valueOf(rece_H2S_no));

                                            int rece_CO2_no = ConvertToIntLittle(txValue, 15);
                                            CO2alarm.setText(String.valueOf(rece_CO2_no));

                                            int rece_CH4_no = ConvertToIntLittle(txValue, 17);
                                            CH4alarm.setText(String.valueOf(rece_CH4_no));
                                        } else {
                                            int rece_copy_type = ConvertToIntLittle(txValue, 9);
                                            String rece_copy_type_str = String.format("%04X", rece_copy_type & 0xFF);

                                            selectValue(senser_tag_copy_type_Spinner, rece_copy_type_str);

                                            int rece_copy_tag_no = ConvertToIntLittle2(txValue, 11);
                                            tag_copy_no.setText(rece_copy_tag_no + "");
                                        }
                                        if (tag_tag_type_str.equals("00D5") && !D5_true_false) {
                                            D5_true_false = true;
                                            mCA_layout.setVisibility(View.GONE);
                                            mD5_layout.setVisibility(View.VISIBLE);
                                            //  customToastView("D5화면으로 변경");
                                            Runnable runnable10 = new Runnable() {
                                                @Override
                                                public void run() {
                                                    //  startScan();
                                                    rcount = 0;

                                                       /* Intent intent = getIntent();
                                                        String sneser_adress_save = intent.getStringExtra("address");
                                                        Intent intent2 = new Intent(Connect_Activity.this, Connect_D5_Activity.class);
                                                        intent2.putExtra("address2", sneser_adress_save);
                                                        startActivity(intent2);
                                                        finish();*/
                                                }
                                            };
                                            D5_connect_handler.postDelayed(runnable10, 3000);
                                        }
                                    } else {
                                        int rece_cal_status = txValue[7] & 0xff;
                                        // Log.e("Cal 상태 체크", rece_cal_status + "");
                                    }
                                }
                                // Log.e("STag", "End...." + accelerometerX);
                            } else {
                                //  Log.e(TAG, "BLE DATA Length is " + receDataLength);
                            }

                        } catch (Exception e) {
                            //Log.e(TAG, e.toString());
                        }
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                Toast.makeText(getApplication(), "기기가 UART를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                mService.disconnect();
            }
        }
    };
    private boolean D5_connect_true = false;

    private void UI_reset() {
        vw_txtmacaddrValue.setText("");
        vw_txt_tag_adress.setText("");
        //tag_type.setText("");
        tag_no.setText("0");
        selectValue(rfPowerSpinner, "0");

        selectValue(sensorSpinner, "0(없음)");
        gyo_cal.setText("보정 안됨");
        selectValue(calSpinner, "Off");
        senser_tag_type_Text.setText("");

        // selectValue(senser_tag_type_Spinner, "선택");
        selectValue(sensor_operation_cycle_Spinner, "0");

        O2alarm.setText("");
        COalarm.setText("");
        H2Salarm.setText("");
        CO2alarm.setText("");
        CH4alarm.setText("");
        ;
    }

    private void selectValue(Spinner spinner, Object value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            //Log.e("RF 값은...",spinner.getItemAtPosition(i).toString());
            if (spinner.getItemAtPosition(i).toString().equals(value + "")) {
                spinner.setSelection(i);
                break;
            }
        }
    }


    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onStart() {

        super.onStart();
        Log.e("connect_TAG", "onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("connect_TAG", "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        IntentFilter filter3 = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);//인터넷 상태 감지 필터
        registerReceiver(mBroadcastReceiver2, filter3);
        Log.e("TAG", "onResume2");
        //invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        Log.e("connect_TAG", "onPause");
        super.onPause();

    }

    @Override
    protected void onStop() {
        Log.e("connect_TAG", "onStop");
        if (mbluetootDevice != null) {
            mService.disconnect();
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("connect_TAG", "onDestroy()");
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        try {

            unregisterReceiver(mBroadcastReceiver2);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        try {
            unbindService(mServiceConnection);
        } catch (java.lang.IllegalArgumentException e) {
            //Print to log or make toast that it failed
        }

        if (mService != null) {
            mService.stopSelf();
            mService = null;
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("connect_TAG", "onRestart");
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    connect_fail = true;
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mbluetootDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mbluetootDevice + "mserviceValue" + mService);
                    // ((TextView) findViewById(R.id.deviceName)).setText(mbluetootDevice.getName() + " - connecting");
                    mService.connect(deviceAddress);


                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    //   Intent intent = new Intent(Connect_Activity.this, MainActivity.class);
                    //  startActivity(intent);
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackPressed() {
        if (mbluetootDevice != null) {
            mService.disconnect();
        }
        connect_fail = false;

        finish();

    }


    // Thread 클래스

    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.connect_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
       /* if (mConnecting_true) {
            menu.findItem(R.id.action_request).setVisible(true);
            menu.findItem(R.id.action_connect).setVisible(false);
            menu.findItem(R.id.action_disconnect).setVisible(false);
        }else*/
        if (connect_check_flag) {
            menu.findItem(R.id.action_request).setVisible(false);
            menu.findItem(R.id.action_connect).setVisible(false);
            menu.findItem(R.id.action_disconnect).setVisible(true);
        } else if (!connect_check_flag) {
            menu.findItem(R.id.action_request).setVisible(false);
            menu.findItem(R.id.action_connect).setVisible(true);
            menu.findItem(R.id.action_disconnect).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            //Intent intent = new Intent(Connect_Activity.this, MainActivity.class);
            // startActivity(intent);
            connect_fail = false;
            onBackPressed();
            // finish();
            return true;
        } else if (itemId == R.id.action_connect) {
            connect_fail = true;
            rcount = 0;
            senser_adress_connect();

            return true;
        } else if (itemId == R.id.action_disconnect) {
            connect_fail = false;
            if (mbluetootDevice != null) {
                mService.disconnect();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void ui_init() {
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);

        vw_txtmacaddrValue = (TextView) findViewById(R.id.macaddrValue);
        vw_txt_tag_adress = (TextView) findViewById(R.id.macaddrValue2);
        // intervalTime = (EditText) findViewById(R.id.interval);
        //tag_type = (EditText) findViewById(R.id.tag_type);
        tag_no = (EditText) findViewById(R.id.tag_no);
        tag_no.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e("onTextChanged", i + ", " + i1 + ", " + i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.e("afterTextChanged", editable.toString());
                if (tag_no.length() == 0) {
                    tag_no.setText("0");
                }
            }
        });

        gyo_cal = (TextView) findViewById(R.id.gyo_cal);
        O2alarm = (EditText) findViewById(R.id.tag_O2_edit);
        O2alarm.setFilters(new InputFilter[]{new InputDoubleFilterMinMax(0, 25.5, 1, (Connect_Activity) Connect_Activity.this)});
        COalarm = (EditText) findViewById(R.id.tag_CO_edit);
        COalarm.setFilters(new InputFilter[]{new InputFilterMinMax("0", "2550", (Connect_Activity) Connect_Activity.this)});
        H2Salarm = (EditText) findViewById(R.id.tag_H2S_edit);
        H2Salarm.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255", (Connect_Activity) Connect_Activity.this)});
        CO2alarm = (EditText) findViewById(R.id.tag_CO2_edit);
        CO2alarm.setFilters(new InputFilter[]{new InputFilterMinMax("0", "60000", (Connect_Activity) Connect_Activity.this)});
        CH4alarm = (EditText) findViewById(R.id.tag_CH4_edit);
        CH4alarm.setFilters(new InputFilter[]{new InputFilterMinMax("0", "60000", (Connect_Activity) Connect_Activity.this)});
        //tag_ver = (TextView) findViewById(R.id.tag_ver);
        btn_sendtotag = (Button) findViewById(R.id.btn_sendTotag);

        tag_copy_no = (EditText) findViewById(R.id.tag_copy_no_d5);
        tag_copy_no.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e("onTextChanged", i + ", " + i1 + ", " + i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.e("afterTextChanged", editable.toString());
                if (tag_copy_no.length() == 0) {
                    tag_copy_no.setText("0");
                }
            }
        });
    }

    private int ui_T_F = 0;

    private void ui_ture_false(int mposition) {
        Log.e("Tag_number71248", "1248");
        if (mposition == 0) {
            ui_T_F = 0;
            // rcount=0;
            // UI_reset();
            O2alarm.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray3));//배경색 설정
            O2alarm.setFocusable(false);//포커싱과
            O2alarm.setClickable(false);
            COalarm.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray3));//배경색 설정
            COalarm.setFocusable(false);//포커싱과
            COalarm.setClickable(false);
            H2Salarm.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray3));//배경색 설정
            H2Salarm.setFocusable(false);//포커싱과
            H2Salarm.setClickable(false);
            CO2alarm.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray3));//배경색 설정
            CO2alarm.setFocusable(false);//포커싱과
            CO2alarm.setClickable(false);
            CH4alarm.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray3));//배경색 설정
            CH4alarm.setFocusable(false);//포커싱과
            CH4alarm.setClickable(false);
        } else if (mposition == 1) {
            ui_T_F = 1;

            O2alarm.setBackgroundResource(R.drawable.textview_design);//배경색 설정
            O2alarm.setFocusable(true);//포커싱과
            O2alarm.setClickable(true);
            O2alarm.setFocusableInTouchMode(true);
            COalarm.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray3));//배경색 설정
            COalarm.setFocusable(false);//포커싱과
            COalarm.setClickable(false);
            H2Salarm.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray3));//배경색 설정
            H2Salarm.setFocusable(false);//포커싱과
            H2Salarm.setClickable(false);
            CO2alarm.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray3));//배경색 설정
            CO2alarm.setFocusable(false);//포커싱과
            CO2alarm.setClickable(false);
            CH4alarm.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray3));//배경색 설정
            CH4alarm.setFocusable(false);//포커싱과
            CH4alarm.setClickable(false);
        } else if (mposition == 2) {
            ui_T_F = 2;

            O2alarm.setBackgroundResource(R.drawable.textview_design);//배경색 설정
            O2alarm.setFocusable(true);//포커싱과
            O2alarm.setClickable(true);
            O2alarm.setFocusableInTouchMode(true);
            COalarm.setBackgroundResource(R.drawable.textview_design);//배경색 설정
            COalarm.setFocusable(true);//포커싱과
            COalarm.setClickable(true);
            COalarm.setFocusableInTouchMode(true);
            H2Salarm.setBackgroundResource(R.drawable.textview_design);//배경색 설정
            H2Salarm.setFocusable(true);//포커싱과
            H2Salarm.setClickable(true);
            H2Salarm.setFocusableInTouchMode(true);
            CO2alarm.setBackgroundResource(R.drawable.textview_design);//배경색 설정
            CO2alarm.setFocusable(true);//포커싱과
            CO2alarm.setClickable(true);
            CO2alarm.setFocusableInTouchMode(true);
            CH4alarm.setBackgroundResource(R.drawable.textview_design);//배경색 설정
            CH4alarm.setFocusable(true);//포커싱과
            CH4alarm.setClickable(true);
            CH4alarm.setFocusableInTouchMode(true);
        } else {
            ui_T_F = 3;

            O2alarm.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray3));//배경색 설정
            O2alarm.setFocusable(false);//포커싱과
            O2alarm.setClickable(false);
            COalarm.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray3));//배경색 설정
            COalarm.setFocusable(false);//포커싱과
            COalarm.setClickable(false);
            H2Salarm.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray3));//배경색 설정
            H2Salarm.setFocusable(false);//포커싱과
            H2Salarm.setClickable(false);
            CO2alarm.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray3));//배경색 설정
            CO2alarm.setFocusable(false);//포커싱과
            CO2alarm.setClickable(false);
            CH4alarm.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray3));//배경색 설정
            CH4alarm.setFocusable(false);//포커싱과
            CH4alarm.setClickable(false);
        }
        if (D5_true_false) {

        }
        Log.e("Tag_number71329", "1329");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MULTIPLE_PERMISSIONS) {
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(this.permissions[i])) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            showToast_PermissionDeny();
                        }
                    }
                }

            } else {
                showToast_PermissionDeny();
            }
        }
    }

    private void showToast_PermissionDeny() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }


    private int ConvertToIntLittle2(byte[] txValue, int startidx) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(8);
        // by choosing big endian, high order bytes must be put
        // to the buffer before low order bytes
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        // since ints are 4 bytes (32 bit), you need to put all 4, so put 0

        byteBuffer.put(txValue[startidx]);
        byteBuffer.put(txValue[startidx + 1]);
        byteBuffer.put(txValue[startidx + 2]);
        byteBuffer.put(txValue[startidx + 3]);
        // for the high order bytes
        byteBuffer.put((byte) 0x00);
        byteBuffer.put((byte) 0x00);
        byteBuffer.put((byte) 0x00);
        byteBuffer.put((byte) 0x00);
        byteBuffer.flip();
        int result = byteBuffer.getInt();
        return result;
    }

    private int ConvertToIntLittle(byte[] txValue, int startidx) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
        // by choosing big endian, high order bytes must be put
        // to the buffer before low order bytes
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        // since ints are 4 bytes (32 bit), you need to put all 4, so put 0
        // for the high order bytes
        byteBuffer.put(txValue[startidx]);
        byteBuffer.put(txValue[startidx + 1]);
        byteBuffer.put((byte) 0x00);
        byteBuffer.put((byte) 0x00);

        byteBuffer.flip();
        int result = byteBuffer.getInt();
        return result;
    }

    public static String asHex(byte bytes[]) {
        if ((bytes == null) || (bytes.length == 0)) {
            return "";
        }

        // バイト配列の２倍の長さの文字列バッファを生成。
        StringBuffer sb = new StringBuffer(bytes.length * 2);

        // バイト配列の要素数分、処理を繰り返す。
        for (int index = 0; index < bytes.length; index++) {
            // バイト値を自然数に変換。
            int bt = bytes[index] & 0xff;

            // バイト値が0x10以下か判定。
            if (bt < 0x10) {
                // 0x10以下の場合、文字列バッファに0を追加。
                sb.append("0");
            }

            // バイト値を16進数の文字列に変換して、文字列バッファに追加。
            if (bytes.length == index + 1) {
                sb.append(Integer.toHexString(bt).toUpperCase());
            } else {
                sb.append(Integer.toHexString(bt).toUpperCase() + "-");
            }

        }

        /// 16進数の文字列を返す。
        return sb.toString();
    }


    private void Network_Confirm(String Network_data) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_MOBILE) {
            Http_post(Network_data);
            Log.e("모바일로 연결됨", "650");
        } else if (status == NetworkStatus.TYPE_WIFI) {
            Http_post(Network_data);
            Log.e("무선랜으로 연결됨", "652");
        } else {
            writeLog(Network_data);
            Log.e("연결 안됨.", "654");
        }
    }

    public void Http_post(String post_data) {
        String[] split_data = post_data.trim().split(",");
        new Thread(() -> {
            try {
                String url = "http://stag.nineone.com:8002/si/rece_Setting.asp";
                URL object = null;
                object = new URL(url);

                HttpURLConnection con = null;

                con = (HttpURLConnection) object.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Authorization", "Bearer Key");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("POST");
                JSONArray array = new JSONArray();

                JSONObject cred = new JSONObject();
                try {
                    if (!D5_true_false) {
                        cred.put("id", split_data[0]);
                        cred.put("device_idx", split_data[1]);
                        cred.put("time", split_data[2]);
                        cred.put("sensor_margin", split_data[3] + "," + split_data[4] + "," + split_data[5] + "," + split_data[6] + "," + split_data[7]);
                    } else {
                        cred.put("id", split_data[0]);
                        cred.put("device_idx", split_data[1]);
                        cred.put("time", split_data[2]);
                        cred.put("sensor_margin", split_data[3] + "," + split_data[4]);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                array.put(cred);
                OutputStream os = con.getOutputStream();
                os.write(array.toString().getBytes("UTF-8"));
                os.close();
                //display what returns the POST request

                StringBuilder sb = new StringBuilder();
                int HttpResult = con.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    Log.e("dd-", "\n" + sb.toString());
                } else {
                    writeLog(post_data);
                    Log.e("dd-", "\n" + con.getResponseMessage());
                    //   System.out.println(con.getResponseMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void writeLog(String data) {//csv파일 저장
        // String str_Path = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+ "Nineone"+ File.separator;
        File file;// = new File(str_Path);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "Nineone" + File.separator);
        } else {
            file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Documents" + File.separator + "Nineone" + File.separator);
        }

        if (!file.exists()) {
            file.mkdirs();
        }
        String str_Path_Full;
        //String str_Path_Full = Environment.getExternalStorageDirectory().getAbsolutePath();
        //str_Path_Full += "/Nineone" + File.separator + "save.csv";
        File file2;//= new File(str_Path_Full);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            str_Path_Full = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + File.separator + "Nineone" + File.separator + "save.csv";
            file2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + File.separator + "Nineone" + File.separator, "save.csv");

        } else {
            str_Path_Full = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Documents" + File.separator + "Nineone" + File.separator + "save.csv";
            file2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Documents" + File.separator + "Nineone" + File.separator, "save.csv");
        }
        if (!file2.exists()) {
            try {
                file2.createNewFile();
            } catch (IOException ignored) {
            }
        }

        try {
            BufferedWriter bfw;

            bfw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(str_Path_Full, true), "EUC-KR"));
            bfw.write(data + "\r\n");
            //bfw.write(log_data);
            bfw.flush();
            bfw.close();
            Log.e("TAGddd", "ddd");
        } catch (IOException e) {
            Log.e("TAGddd", e.toString());
        }
    }


    ArrayList<Tag_tiem> arrayList = new ArrayList<>();

    private void ReadTextFile() {//csv 파일 내용을 추출하기
        try {
            String str_Path_Full;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                str_Path_Full = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "Nineone" + File.separator + "save.csv";
                // file2 = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + File.separator+ "Nineone"+ File.separator,"save.csv");
            } else {
                str_Path_Full = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Documents" + File.separator + "Nineone" + File.separator + "save.csv";
                // file2 = new File(Environment.DIRECTORY_DOCUMENTS + File.separator+ "Nineone"+ File.separator + "save.csv");
            }
            FileInputStream is = new FileInputStream(str_Path_Full);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "EUC-KR"));
            // BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = "";
            int row = 0;
            Log.e("aaa", "line");
            while ((line = reader.readLine()) != null) {//해당 파일을 한줄씩 읽기
                String[] token = line.split("\\,", -1);

                Log.e("aaa2", String.valueOf(token.length));
                String sensor_margin;
                if (token.length > 5) {
                    sensor_margin = token[3] + "," + token[4] + "," + token[5] + "," + token[6] + "," + token[7];
                } else {
                    sensor_margin = token[3] + "," + token[4];
                }
                arrayList.add(row, new Tag_tiem(token[0], token[1], token[2], sensor_margin));
                Log.e("aaa3", String.valueOf(row));
                row++;

            }

            reader.close();
            is.close();
            Http_Array_post(arrayList);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("aaa5", String.valueOf(arrayList));

        }
        //return strBuffer.toString();
    }

    public void Http_Array_post(ArrayList<Tag_tiem> array_List) {

        new Thread(() -> {
            try {

                String url = "http://stag.nineone.com:8002/si/rece_Setting.asp";
                URL object = null;
                object = new URL(url);
                HttpURLConnection con;

                con = (HttpURLConnection) object.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Authorization", "Bearer Key");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("POST");
                JSONArray array = new JSONArray();

                for (int i = 0; i < array_List.size(); i++) {
                    JSONObject cred = new JSONObject();
                    try {
                        cred.put("id", array_List.get(i).getId());
                        cred.put("device_idx", array_List.get(i).getDevice_idx());
                        cred.put("time", array_List.get(i).getTime());
                        cred.put("sensor_margin", array_List.get(i).getSensor_margin());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    array.put(cred);
                }
                OutputStream os = con.getOutputStream();
                os.write(array.toString().getBytes(StandardCharsets.UTF_8));
                os.close();
//display what returns the POST request

                StringBuilder sb = new StringBuilder();
                int HttpResult = con.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();
                    if (sb.toString().contains("done")) {
                        Log.e("1dd-", "\n" + sb.toString());
                        fileDelete();
                    } else {

                        Log.e("2dd-", "\n" + sb.toString());
                    }

                } else {
                    Log.e("0dd-", "\n" + con.getResponseMessage());
                    //   System.out.println(con.getResponseMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    private static boolean fileDelete() {
        // String str_Path_Full = Environment.getExternalStorageDirectory().getAbsolutePath();
        // str_Path_Full += "/Nineone" + File.separator + "save.csv";

        try {
            File file;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //     str_Path_Full = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + File.separator+ "Nineone"+ File.separator + "save.csv";
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "Nineone" + File.separator, "save.csv");
            } else {
                //     str_Path_Full = Environment.DIRECTORY_DOCUMENTS + File.separator+ "Nineone"+ File.separator + "save.csv";
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Documents" + File.separator + "Nineone" + File.separator + "save.csv");
            }
            if (file.exists()) {
                file.delete();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle extras = intent.getExtras();
            NetworkInfo info = (NetworkInfo) extras.getParcelable("networkInfo");
            NetworkInfo.State networkstate = info.getState();
            Log.d("TEST Internet", info.toString() + " " + networkstate.toString());
            if (networkstate == NetworkInfo.State.CONNECTED) {
                Log.e("인터넷으로 연결됨", "652");
                //  Toast.makeText(activity.getApplication(), "Internet connection is on", Toast.LENGTH_LONG).show();
                ReadTextFile();
            } else {
                Log.e("인터넷으로 해제됨", "652");
                //   Toast.makeText(activity.getApplication(), "Internet connection is Off", Toast.LENGTH_LONG).show();
            }
        }
    };

    public void customToastView(String text) {

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.connect_success_toast, (ViewGroup) findViewById(R.id.Send_toast));
        TextView textView = layout.findViewById(R.id.toast);
        textView.setText(text);

        Toast toastView = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toastView.setGravity(Gravity.CENTER, 0, 50);
        toastView.setView(layout);
        toastView.show();
    }


}
