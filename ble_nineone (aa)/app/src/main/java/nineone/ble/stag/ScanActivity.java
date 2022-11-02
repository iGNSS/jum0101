/*
 * Copyright (C) 2013 youten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nineone.ble.stag;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nineone.ble.util.BleUtil;
import nineone.ble.util.ScannedDevice;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
import android.support.annotation.IdRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ScanActivity extends Activity implements BluetoothAdapter.LeScanCallback {

    private BluetoothAdapter mBTAdapter;
    private DeviceAdapter mDeviceAdapter;
    private boolean mIsScanning;

    private String[] permissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
    private static final int MULTIPLE_PERMISSIONS = 101;

    long RescanBaseTime;
    private int bleRestartTime = 5 * 1000 * 60;

    EditText scanMinorId;
    LinearLayout bletypeLayout;
    public static String BLE_type;

    public static String file_name;

    public static boolean fos_open_flag_ble;

    //khsig_20200515 start
    int serverResponseCode = 0;
    TextView title;
    String[] fileList;
    int sendCounter = 0, doneCounter = 1;

    public static String dataSaveFolder = "Stag";
    String dataFileNameCheckSTR = "scenario";

    String HOST_URL = "http://stag.nineone.com:8000/kist/upload.asp";
    //khsig_20200515 end

    //Stag ID List
  /*  final public static String STAG_001 = "001";
    final public static String STAG_002 = "002";
    final public static String STAG_005 = "005";
    final public static String STAG_007 = "007";
    final public static String STAG_003 = "003";
    final public static String STAG_010 = "010";
    final public static String STAG_100 = "100";
    final public static String STAG_007 = "007";
    final public static String STAG_008 = "008";
    final public static String STAG_011 = "011";*/
    final public static String STAG_0001 = "0001";
  /*  final public static String STAG_012 = "012";
    final public static String STAG_013 = "013";
    final public static String STAG_021 = "021";
    final public static String STAG_024 = "024";
    final public static String STAG_201 = "201";
    final public static String STAG_202 = "202";
    final public static String STAG_203 = "203";
    final public static String STAG_220 = "220";*/

    static String Stag_job_done_list="";

    private RadioButton r_btn1, r_btn2;
    private RadioGroup radioGroup;

    static String sort_type = "name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_scan);

        Stag_job_done_list =
               /* STAG_002 + ": 2020.01.01" + "\r\n"
                + STAG_010 + ": 2020.01.01" + "\r\n"
                + STAG_100 + ": 2020.01.01" + "\r\n"*/
                STAG_0001 + ": 2020.06.03" + "\r\n";
                /*+ STAG_012 + ": 2020.06.03" + "\r\n"
                + STAG_013 + ": 2020.06.03" + "\r\n"
                + STAG_024 + ": 2020.06.03" + "\r\n"
                + STAG_005 + ": 2020.11.05" + "\r\n"
                + STAG_003 + ": 2021.02.15" + "\r\n";*/
        init();

        title = (TextView) findViewById(R.id.title);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {                                  // 안드로이드 6.0 이상일 경우 퍼미션 체크
            checkPermissions();
        }

        scanMinorId = (EditText) findViewById(R.id.minorid);
        bletypeLayout = (LinearLayout) findViewById(R.id.bletypeLayout);

        r_btn1 = (RadioButton) findViewById(R.id.sort_btn1);
        r_btn2 = (RadioButton) findViewById(R.id.sort_btn2);

        //라디오 그룹 설정
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);
    }

    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        } else {

        }
        return true;
    }

    //라디오 그룹 클릭 리스너
    RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            if(i == R.id.sort_btn1){
                Toast.makeText(ScanActivity.this, "분류 타입이 이름으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                sort_type = "name";
            }
            else if(i == R.id.sort_btn2){
                Toast.makeText(ScanActivity.this, "분류 타입이 RSSI 값으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                sort_type = "rssi";
            }
        }
    };
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
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
                return;
            }
        }
    }

    private void showToast_PermissionDeny() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if ((mBTAdapter != null) && (!mBTAdapter.isEnabled())) {
            Toast.makeText(this, R.string.bt_not_enabled, Toast.LENGTH_SHORT).show();
            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopScan();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mIsScanning) {
            menu.findItem(R.id.action_scan).setVisible(false);
            menu.findItem(R.id.action_stop).setVisible(true);
        } else {
            menu.findItem(R.id.action_scan).setEnabled(true);
            menu.findItem(R.id.action_scan).setVisible(true);
            menu.findItem(R.id.action_stop).setVisible(false);
        }
        if ((mBTAdapter == null) || (!mBTAdapter.isEnabled())) {
            menu.findItem(R.id.action_scan).setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // ignore
            return true;
        } else if (itemId == R.id.action_scan) {
            RescanBaseTime = SystemClock.elapsedRealtime();
            bleRestartTimer.sendEmptyMessage(0);
            BLE_type = scanMinorId.getText().toString();
            //BLE_type="007";

            mDeviceAdapter.clear();
            mDeviceAdapter.notifyDataSetChanged();
            startScan();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(scanMinorId.getWindowToken(), 0);

            bletypeLayout.setVisibility(View.GONE);
            String subtitle = "SCAN Type: " + BLE_type;
            if (BLE_type.equals("")) {
                subtitle = "SCAN Type: ALL";
            }
            getActionBar().setSubtitle(subtitle);

            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            long now = System.currentTimeMillis();
            SimpleDateFormat sdfNow = new SimpleDateFormat("MM_dd_HH_mm_ss");
            file_name = sdfNow.format(new Date(now));

            fos_open_flag_ble = false;

            return true;
        } else if (itemId == R.id.action_stop) {
            bleRestartTimer.removeMessages(0);
            stopScan();
            bletypeLayout.setVisibility(View.VISIBLE);
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            return true;
        }else if (itemId == R.id.action_staglist) {
            showLicense();
            return true;
        } else if (itemId == R.id.action_clear) {
            if ((mDeviceAdapter != null) && (mDeviceAdapter.getCount() > 0)) {
                mDeviceAdapter.clear();
                mDeviceAdapter.notifyDataSetChanged();
                getActionBar().setSubtitle("");

            }

            //khsig_20200515 file send to server start
            if (false) {
                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                // messageText.setText("uploading started.....");
                                title.setText("Sending !!");
                                Log.e("File send Start", "");
                            }
                        });

                        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + dataSaveFolder + File.separator;
                        File list = new File(path);

                        fileList = list.list(new FilenameFilter() {
                            public boolean accept(File dir, String filename) {
                                Boolean bOK = false;
                                if (filename.contains(dataFileNameCheckSTR)) bOK = true;
                                return bOK;
                            }
                        });

                        doneCounter = 0;
                        for (int i = 0; i < fileList.length; i++) {
                            sendCounter = i + 1;
                            String fullFileName = path + fileList[i];
                            Log.e("File Name is  ", fullFileName);
                            uploadFile(HOST_URL, fileList[i], fullFileName);
                        }

                    }
                }).start();
                //khsig_20200515 file send to server end
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onLeScan(final BluetoothDevice newDeivce, final int newRssi, final byte[] newScanRecord) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String summary = mDeviceAdapter.update(newDeivce, newRssi, newScanRecord);
//                if (summary != null) {
//                    getActionBar().setSubtitle(summary);
//                }
            }
        });
    }

    private void init() {
        // BLE check
        if (!BleUtil.isBLESupported(this)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // BT check
        BluetoothManager manager = BleUtil.getManager(this);
        if (manager != null) {
            mBTAdapter = manager.getAdapter();
        }
        if (mBTAdapter == null) {
            Toast.makeText(this, R.string.bt_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // init listview
        ListView deviceListView = (ListView) findViewById(R.id.list);
        mDeviceAdapter = new DeviceAdapter(this, R.layout.listitem_device, new ArrayList<ScannedDevice>());
        deviceListView.setAdapter(mDeviceAdapter);
        stopScan();
    }

    private void startScan() {
        if ((mBTAdapter != null) && (!mIsScanning)) {
            mBTAdapter.startLeScan(this);
            mIsScanning = true;
            setProgressBarIndeterminateVisibility(true);
            invalidateOptionsMenu();
        }
    }

    private void stopScan() {
        if (mBTAdapter != null) {
            mBTAdapter.stopLeScan(this);
        }
        mIsScanning = false;
        setProgressBarIndeterminateVisibility(false);
        invalidateOptionsMenu();
    }

    Handler bleRestartTimer = new Handler() {
        public void handleMessage(android.os.Message msg) {
            getEllapse();
            bleRestartTimer.sendEmptyMessage(0);                //0은 메시지를 구분하기 위한 것
        }

        ;
    };

    void getEllapse() {

        long now = SystemClock.elapsedRealtime();
        long ell = now - RescanBaseTime;                            //현재 시간과 지난 시간을 빼서 ell값을 구하고

        if (ell > bleRestartTime) {
            Log.e("BLE Scan:", " ReStart");
            RescanBaseTime = SystemClock.elapsedRealtime();
            reScan();
        }
    }

    private void reScan() {
        if (mBTAdapter != null) {
            mBTAdapter.stopLeScan(this);
            mBTAdapter.startLeScan(this);
        }

    }


    private void showLicense() {
        LicenseDialogFragment dialogFragment = new LicenseDialogFragment();
        dialogFragment.show(getFragmentManager(), "STag Job List");
    }

    /**
     * LicenseDialogFrament
     */
    public static class LicenseDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setIcon(R.drawable.ic_launcher);
            builder.setTitle(R.string.license_title);
            builder.setPositiveButton(android.R.string.ok, null);

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_license, null);
            TextView tv = (TextView) content.findViewById(R.id.text01);
            String bodytxt = "Name: 작업일" + "\r\n"
                            + "===============" + "\r\n"
                            + Stag_job_done_list;
            tv.setText(bodytxt);
            builder.setView(content);

            return builder.create();
        }
    }
    // https://periar.tistory.com/entry/Android-Http-Post-%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%98%EC%97%AC-%ED%8C%8C%EC%9D%BC%EC%A0%84%EC%86%A1-ASP-net
    //https://taetanee.tistory.com/entry/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-php-%ED%8C%8C%EC%9D%BC-%EC%A0%84%EC%86%A1-%EC%98%88%EC%A0%9C
    // khsig_20200515
    // public void HttpFileUpload(String urlString, String params, String fileName)
    public int uploadFile(String upLoadServerUri, String File_name, String sourceFileUri) {

        String fileName = sourceFileUri;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        //String minno = "01075204165";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {
            // dialog.dismiss();
            Log.e("uploadFile", "Source File not exist :");

            runOnUiThread(new Runnable() {
                public void run() {
                    //  messageText.setText("Source File not exist :"
                    //          +uploadFilePath + "" + uploadFileName);
                }
            });
            return 0;
        } else {
            try {

                // open a URL connection to the Servlet

                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);


                // Open a HTTP  connection to  the URL

                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", File_name);
                //conn.setRequestProperty("minno", minno);

                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + File_name + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];


                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                Log.e("File size", "" + bytesRead);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }


                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.e("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuffer sbResult = new StringBuffer();
                String str = "";
                while ((str = br.readLine()) != null) {
                    sbResult.append(str);
                }

                Log.e("서버 결과", "" + sbResult);
                if (sbResult.toString().equals("Done")) {
                    doneCounter++;
                } else {
                    Log.e("Server Error", sbResult.toString());
                }
                //khsig_20200515 전송 상태 표시할 UI 변경
                title.setText(sendCounter + " / " + fileList.length);

                if (serverResponseCode == 200 && fileList.length == sendCounter) {

                    runOnUiThread(new Runnable() {
                        public void run() {
                            String msg = doneCounter + " Files Upload Completed.\n\n ";
                            //  messageText.setText(msg);
                            Toast.makeText(ScanActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();
            } catch (MalformedURLException ex) {
                // dialog.dismiss();
                ex.printStackTrace();
                runOnUiThread(new Runnable() {
                    public void run() {
                        // messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(ScanActivity.this, "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {
                //dialog.dismiss();
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    public void run() {
                        // messageText.setText("Got Exception : see logcat ");
                        // Toast.makeText(MainActivity.this, "Got Exception : see logcat ",
                        //         Toast.LENGTH_SHORT).show();

                    }
                });
                Log.e("server Exception", "Exception : "
                        + e.getMessage(), e);
            }
            //dialog.dismiss();
            return serverResponseCode;
        } // End else block
    }
}
