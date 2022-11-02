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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nineone.ble.util.ScannedDevice;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/*import static nineone.ble.stag.ScanActivity.STAG_001;
import static nineone.ble.stag.ScanActivity.STAG_002;
import static nineone.ble.stag.ScanActivity.STAG_003;
import static nineone.ble.stag.ScanActivity.STAG_005;
import static nineone.ble.stag.ScanActivity.STAG_007;
import static nineone.ble.stag.ScanActivity.STAG_010;
import static nineone.ble.stag.ScanActivity.STAG_007;
import static nineone.ble.stag.ScanActivity.STAG_008;
import static nineone.ble.stag.ScanActivity.STAG_011;*/
import static nineone.ble.stag.ScanActivity.STAG_0001;
/*import static nineone.ble.stag.ScanActivity.STAG_012;
import static nineone.ble.stag.ScanActivity.STAG_013;
import static nineone.ble.stag.ScanActivity.STAG_021;
import static nineone.ble.stag.ScanActivity.STAG_024;
import static nineone.ble.stag.ScanActivity.STAG_100;
import static nineone.ble.stag.ScanActivity.STAG_201;
import static nineone.ble.stag.ScanActivity.STAG_202;
import static nineone.ble.stag.ScanActivity.STAG_203;
import static nineone.ble.stag.ScanActivity.STAG_220;*/
import static nineone.ble.stag.ScanActivity.dataSaveFolder;
import static nineone.ble.stag.ScanActivity.file_name;
import static nineone.ble.stag.ScanActivity.fos_open_flag_ble;
import static nineone.ble.util.DateUtil.get_yyyyMMddHHmmssSSS;

/**
 * スキャンされたBLEデバイスリストのAdapter
 */
public class DeviceAdapter extends ArrayAdapter<ScannedDevice> {
    private static final String PREFIX_RSSI = "RSSI:";
    private static final String PREFIX_LASTUPDATED = "Last Udpated:";
    private List<ScannedDevice> mList;
    private LayoutInflater mInflater;
    private int mResId;

    public DeviceAdapter(Context context, int resId, List<ScannedDevice> objects) {
        super(context, resId, objects);
        mResId = resId;
        mList = objects;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    int ble_temp_type;

    String sensordata_1 = "";
    String sensordata_2 = BuildConfig.FLAVOR;
    String stag_save_data_type_7 = BuildConfig.FLAVOR;
    String stag_save_data_type_8 = BuildConfig.FLAVOR;
    String stag_save_data = BuildConfig.FLAVOR;
    String[] u_tag_data_7 = new String[65000];
    String[] u_tag_data_8 = new String[65000];
    long u_tag_1_receTime = System.currentTimeMillis();
    long u_tag_2_receTime = System.currentTimeMillis();
    String[] u_tag_data_1 = new String[65000];
    String[] u_tag_data_2 = new String[65000];

    String[] u_tag_data_3 = new String[65000];
    String[] u_tag_data_4 = new String[65000];

    String[] u_tag_data_5 = new String[65000];
    String[] u_tag_data_6 = new String[65000];
    int[] count_a = new int[65000];
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ScannedDevice item = (ScannedDevice) getItem(position);
        if (convertView == null) {
            convertView = mInflater.inflate(mResId, null);
        }
        TextView name = (TextView) convertView.findViewById(R.id.device_name);
        name.setText(item.getDisplayName());

        TextView address = (TextView) convertView.findViewById(R.id.device_address);
        address.setText(item.getDevice().getAddress());

        TextView rssi = (TextView) convertView.findViewById(R.id.device_rssi);
        rssi.setText(PREFIX_RSSI + Integer.toString(item.getRssi()));

        TextView lastupdated = (TextView) convertView.findViewById(R.id.device_lastupdated);
        lastupdated.setText(PREFIX_LASTUPDATED + get_yyyyMMddHHmmssSSS(item.getLastUpdatedMs()));

        TextView ibeaconInfo = (TextView) convertView.findViewById(R.id.device_ibeacon_info);
        Resources res = convertView.getContext().getResources();

        byte[] ble_data = item.getScanRecord();
      //  Log.e("bled", String.valueOf(ble_data));
        String[] DeviceNameArray = item.getDisplayName().trim().split("-");
        String sensordata = "";
        int sensorStartIdx = 9;
        int BATTVal = ble_data[sensorStartIdx] & 0xff;
        double BATTDisplayVal = BATTVal * 0.1;
        float BAROMETER = Float.intBitsToFloat(byteArrayToInt(ble_data, sensorStartIdx + 1));
        String default_sensor = "BATT: " + String.format("%.1f", BATTDisplayVal) + " V\r\n" + "Pressure :" + String.format("%.2f", BAROMETER) + " hPa\r\n";
        short Ax_10 = 0;
        short Ay_10 = 0;
        short Az_10 = 0;
        short Max_Av = 0;
        short Min_Av = 0;
        short t_temp = 0;
        int Move_check = 0;
        int barometerVal = 0;
        int MoveEvent = 0;
        double BAROMETER_10 = 0.0;

        int step = 0;
        float Direction = 0;
        int Direction2 = 0;
        byte stride = 0;
        double Barometer=0;
        short in_out=0;
        int latitude=0;
        int longitude=0;
        short User_Move=0;
        short Major= 0;
        int Minor_Number= 0;

        int BTU1 = 0;
        int BTU2= 0;
        int BTU3= 0;
        int BTU4= 0;
        int BTU5= 0;
        double BATTDisplayVal2=0;
        int i5 = 0;

        int gps_state=0;
        String str3 = BuildConfig.FLAVOR;

        switch (DeviceNameArray[1]) {
            case STAG_0001:
                int parseInt3 = Integer.parseInt(DeviceNameArray[2],16);
                Minor_Number = ConvertToIntLittle2(ble_data, sensorStartIdx + 18);
                Log.e("Minor_Number2", String.valueOf(Minor_Number));

                if(Minor_Number==1) {
                    int BATTVal2 = ble_data[sensorStartIdx] & 0xff;
                    BATTDisplayVal = BATTVal2 * 0.1;
                    step = ConvertToIntLittle(ble_data, sensorStartIdx + 1);
                    Direction = ConvertToIntLittle(ble_data, sensorStartIdx + 3);
                    double Direction3 = Direction * 0.01;
                    Barometer = ConvertToIntLittle(ble_data, sensorStartIdx + 5);
                    BAROMETER_10 = (Barometer + 80000) * 0.01;
                    latitude = ConvertToIntLittle(ble_data, sensorStartIdx + 7);
                    float latitude2 = ConvertToIntLittle(ble_data, sensorStartIdx + 9);
                    double latitude3 = latitude + (latitude2 * 0.01);

                    longitude = ConvertToIntLittle(ble_data, sensorStartIdx + 11);
                    int longitude2 = ConvertToIntLittle(ble_data, sensorStartIdx + 11);
                    double longitude3 = longitude + (longitude2 * 0.01);

                    User_Move = ble_data[sensorStartIdx + 15];
                    i5 = parseInt3;

                    String[] strArr6 = u_tag_data_7;
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append("Batt: ");
                    sb3.append(String.format("%.2f", BATTDisplayVal));
                    sb3.append(" \r\nStepCount: ");
                    sb3.append(String.format("%d",step));
                    sb3.append(" \r\nDirection: ");
                    sb3.append(String.format("%.2f",Direction * 0.01));
                    sb3.append(" \r\nBAROMETER: ");
                    sb3.append(String.format("%.2f", BAROMETER_10)).append(" hPa");
                    sb3.append(" \r\nlatitud: ");
                    sb3.append(String.valueOf(latitude3));
                    sb3.append(" \r\nlongitude: ");
                    sb3.append(String.valueOf(longitude3));
                    sb3.append(" \r\nMove: ");
                    sb3.append(String.format("%d", User_Move));
                    sb3.append(" \r\nIndex_7: ");
                    sb3.append(ble_data[24] & 15);
                    sb3.append(" \r\n");
                    strArr6[i5] = sb3.toString();
                    stag_save_data = step + "," + Direction + ",0_0,0_0,0_0,0_0,0_0";
                    str3 = str3;
                   /* sensordata = "Batt: " + String.format("%.2f", BATTDisplayVal) + " V\r\n"
                            + "STEP: " + String.format("%d", step) + "\r\n"
                            + "Direction: " + String.valueOf(Direction2) + "\r\n"
                            + "Barometer: " + String.format("%.2f", BAROMETER_10) + " hPa\r\n"
                            + "latitude: " + String.valueOf(latitude3) + "\r\n"
                            + "longitude: " + String.valueOf(longitude3) + "\r\n"
                            + "User_Move: " + String.format("%d", User_Move) + "\r\n"
                            + "Minor_Number: " + String.format("%d", Minor_Number) + "\r\n"
                            + "\r\n";
                    str3 = sensordata;*/
                }else if(Minor_Number==2) {
                    Log.e("ddd", ble_data[sensorStartIdx] + "," + ble_data[sensorStartIdx + 1] + "," + ble_data[sensorStartIdx + 2]);
                    i5 = parseInt3;
                    str3 = str3;
                    sensordata_2 = str3;
                    int BTU001 = ConvertToIntLittle(ble_data, sensorStartIdx);
                    String BTU001_1 = String.format("%02X", BTU001&0xffff);
                    int BTU1_1 = (ble_data[sensorStartIdx + 2] & 0xff) * (-1);
                    sensordata_2 += BTU001_1 + "_" + BTU1_1 + " , ";

                    Log.e("ddd", String.valueOf(BTU1));
                    int BTU002 = ConvertToIntLittle(ble_data, sensorStartIdx + 3);
                    String BTU002_1 = String.format("%02X", BTU002&0xffff);
                    int BTU1_2 = (ble_data[sensorStartIdx + 5] & 0xff) * (-1);
                    sensordata_2 += BTU002_1 + "_" + BTU1_2 + " , ";

                    int BTU003 = ConvertToIntLittle(ble_data, sensorStartIdx + 6);
                    int BTU1_3 = (ble_data[sensorStartIdx + 8] & 0xff) * (-1);
                    String BTU003_1 = String.format("%02X", BTU003&0xffff);
                    sensordata_2 += BTU003_1 + "_" + BTU1_3 + " , ";

                    int BTU004 = ConvertToIntLittle(ble_data, sensorStartIdx + 9);
                    int BTU1_4 = (ble_data[sensorStartIdx + 11] & 0xff) * (-1);
                    String BTU004_1 = String.format("%02X", BTU004&0xffff);
                    sensordata_2 += BTU004_1 + "_" + BTU1_4 + " , ";

                    int BTU005 = ConvertToIntLittle(ble_data, sensorStartIdx + 12);
                    int BTU1_5 = (ble_data[sensorStartIdx + 14] & 0xff) * (-1);
                    String BTU005_1 = String.format("%02X", BTU005&0xffff);
                    sensordata_2 += BTU005_1 + "_" + BTU1_5 + ".";

                    stag_save_data = "0,0.0," + sensordata_2;
                    sensordata_2 += "\r\n";
                    sensordata_2 += "gps_state: " + ((ble_data[24] & 240) >> 4) + "\r\n";
                    sensordata_2 += "Count: " + count_a[i5] + "\r\n";
                    //sensordata_2 += "index_8: " + (ble_data[24] & 15) + "\r\n";
                    if(BTU001 == 0 && BTU1_1 == 0 && BTU005 ==0 && BTU1_5==0){
                        count_a[i5] += count_a[i5]+1;
                    }
                    u_tag_data_8[i5] = sensordata_2;
                }
                String[] strArr7 = u_tag_data_7;
                if (strArr7[i5] != null) {
                    str3 = strArr7[i5];
                }
                if (u_tag_data_8[i5] != null) {
                    str3 = str3 + u_tag_data_8[i5];
                }
                break;
         /*   case STAG_007:
                int parseInt3 = Integer.parseInt(DeviceNameArray[2]);

                Minor_Number = ble_data[sensorStartIdx + 20] & 0xff;
                if(Minor_Number==7){
                   int BATTVal2 = ble_data[sensorStartIdx] & 0xff;
                    BATTDisplayVal2 = BATTVal2 * 0.1;
                    step = ConvertToIntLittle(ble_data, sensorStartIdx + 1);
                    Direction= arr2float(ble_data,sensorStartIdx + 3);
                    stride= ble_data[sensorStartIdx + 7];
                    Barometer=ConvertToIntLittle(ble_data,sensorStartIdx+8);
                    in_out= (short) (ble_data[sensorStartIdx + 10] & 0xff);
                    latitude=ConvertToIntLittle(ble_data, sensorStartIdx + 11);
                    longitude=ConvertToIntLittle(ble_data, sensorStartIdx + 13);
                    User_Move=ble_data[sensorStartIdx + 15];
                    BAROMETER_10 = (Barometer + 80000) * 0.01;
                    i5 = parseInt3;


                    sensordata = "Batt: " + String.format("%.2f", BATTDisplayVal) + " V\r\n"
                            + "STEP: " + String.format("%d", step) + "\r\n"
                            + "Direction: " + String.format("%.2f", Direction)+"\r\n"
                            + "stride: " + String.format("%d", stride*10)+"\r\n"
                            + "Barometer: " + String.format("%.2f", BAROMETER_10)  + " hPa\r\n"
                            + "in out: " + String.format("%d", in_out) + "\r\n"
                            + "latitude: " + String.format("%d", latitude)+"\r\n"
                            + "longitude: " + String.format("%d", longitude)  + "\r\n"
                            + "User_Move: " + String.format("%d", User_Move) + "\r\n"
                            + "Minor_Number: " + String.format("%d", Minor_Number) + "\r\n"
                            + "\r\n";
                }else if(Minor_Number==8){


                    BTU1=ConvertToIntLittletree(ble_data, sensorStartIdx);
                    BTU2=ConvertToIntLittletree(ble_data, sensorStartIdx+3);
                    BTU3=ConvertToIntLittletree(ble_data, sensorStartIdx+6);
                    BTU4=ConvertToIntLittletree(ble_data, sensorStartIdx+9);
                    BTU5=ConvertToIntLittletree(ble_data, sensorStartIdx+12);
                    gps_state= ((ble_data[sensorStartIdx + 15]& 0xf0)>> 4);
                    sensordata = "BTU1: " + String.format("%d", BTU1) + "\r\n"
                            + "BTU2: " + String.format("%d", BTU2)+"\r\n"
                            + "BTU3: " + String.format("%d", BTU3)+"\r\n"
                            + "BTU4: " + String.format("%d", BTU4)  + "\r\n"
                            + "BTU5: " + String.format("%d", BTU5)  + "\r\n"
                            + "gps state: " + String.format("%d", gps_state) + "\r\n"

                            + "Minor_Number: " + String.format("%d", Minor_Number) + "\r\n"
                            + "\r\n";
                }
                String[] strArr7 = u_tag_data_7;
                if (strArr7[i5] != null) {
                    str3 = strArr7[i5];
                }
                if (u_tag_data_8[i5] != null) {
                    str3 = str3 + u_tag_data_8[i5];
                }*/
           /* case STAG_007:
                int parseInt3 = Integer.parseInt(DeviceNameArray[2]);

                Minor_Number = ble_data[sensorStartIdx + 20] & 0xff;
                if(Minor_Number==7){
                    int BATTVal2 = ble_data[sensorStartIdx] & 0xff;
                    BATTDisplayVal2 = BATTVal2 * 0.1;
                    step = ConvertToIntLittle(ble_data, sensorStartIdx + 1);
                    Direction= arr2float(ble_data,sensorStartIdx + 3);
                    stride= ble_data[sensorStartIdx + 7];
                    Barometer=ConvertToIntLittle(ble_data,sensorStartIdx+8);
                    in_out= (short) (ble_data[sensorStartIdx + 10] & 0xff);
                    latitude=ConvertToIntLittle(ble_data, sensorStartIdx + 11);
                    longitude=ConvertToIntLittle(ble_data, sensorStartIdx + 13);
                    User_Move=ble_data[sensorStartIdx + 15];
                    BAROMETER_10 = (Barometer + 80000) * 0.01;
                    i5 = parseInt3;

                    String[] strArr6 = u_tag_data_7;
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append("Batt: ");
                    sb3.append(String.format("%.2f", BATTDisplayVal));
                    sb3.append(" \r\nStepCount: ");
                    sb3.append(String.format("%d",step));
                    sb3.append(" \r\nDirection: ");
                    sb3.append(String.format("%.2f", Direction));
                    sb3.append(" \r\nBAROMETER: ");
                    sb3.append(String.format("%.2f", BAROMETER_10)).append(" hPa");
                    sb3.append(" \r\nstride: ");
                    sb3.append(String.format("%d", stride*10));
                    sb3.append(" \r\nIn Out: ");
                    sb3.append(String.format("%d", in_out));
                    sb3.append(" \r\nGPS: ");
                    sb3.append(latitude).append(", ").append(longitude);
                    sb3.append(" \r\nMove: ");
                    sb3.append((ble_data[24] & 240) >> 4);
                    sb3.append(" \r\nIndex_7: ");
                    sb3.append(ble_data[24] & 15);
                    sb3.append(" \r\n");
                    strArr6[i5] = sb3.toString();
                    stag_save_data = step + "," + Direction + ",0_0,0_0,0_0,0_0,0_0";
                    str3 = str3;
                }else if(Minor_Number==8){
                    Log.e("ddd",ble_data[sensorStartIdx]+","+ble_data[sensorStartIdx + 1]+","+ble_data[sensorStartIdx + 2]);
                    i5 = parseInt3;
                    str3=str3;
                    sensordata_2 = str3;

                    BTU1=ConvertToIntLittle(ble_data, sensorStartIdx);
                    int BTU1_1=(ble_data[sensorStartIdx+2] & 0xff) * (-1);
                    sensordata_2 +=BTU1+"_"+BTU1_1+" , ";
                    Log.e("ddd", String.valueOf(BTU1));
                    BTU2=ConvertToIntLittle(ble_data, sensorStartIdx+3);
                    int BTU1_2=(ble_data[sensorStartIdx+5] & 0xff) * (-1);
                    sensordata_2 +=BTU2+"_"+BTU1_2+" , ";
                    BTU3=ConvertToIntLittle(ble_data, sensorStartIdx+6);
                    int BTU1_3=(ble_data[sensorStartIdx+8] & 0xff) * (-1);
                    sensordata_2 +=BTU3+"_"+BTU1_3+" , ";
                    BTU4=ConvertToIntLittle(ble_data, sensorStartIdx+9);
                    int BTU1_4=(ble_data[sensorStartIdx+11] & 0xff) * (-1);
                    sensordata_2 +=BTU4+"_"+BTU1_4+" , ";
                    BTU5=ConvertToIntLittle(ble_data, sensorStartIdx+12);
                    int BTU1_5=(ble_data[sensorStartIdx+14] & 0xff) * (-1);
                    sensordata_2 +=BTU5+"_"+BTU1_5+".";
                    stag_save_data = "0,0.0," + sensordata_2;
                    sensordata_2 += "\r\n";
                    sensordata_2 += "gps_state: " + ((ble_data[24] & 240) >> 4) + "\r\n";
                    sensordata_2 += "index_8: " + (ble_data[24] & 15) + "\r\n";
                    u_tag_data_8[i5] = sensordata_2;
                }
                String[] strArr7 = u_tag_data_7;
                if (strArr7[i5] != null) {
                    str3 = strArr7[i5];
                }
                if (u_tag_data_8[i5] != null) {
                    str3 = str3 + u_tag_data_8[i5];
                }*/
            default:
               // str3 = item.getScanRecordHexString();
        }
        ibeaconInfo.setText(str3);
        //Data Save
        bledata_save(file_name, item.getRssi(), item.getDevice().getName().trim());
        return convertView;
    }


    /**
     * add or update BluetoothDevice List
     *
     * @param newDevice  Scanned Bluetooth Device
     * @param rssi       RSSI
     * @param ble_data advertise data
     * @return summary ex. "iBeacon:3 (Total:10)"
     */
    public String update(BluetoothDevice newDevice, int rssi, byte[] ble_data) {
        if ((newDevice == null) || (newDevice.getAddress() == null) || newDevice.getName() == null) {
            return "";
        }
        long now = System.currentTimeMillis();

        boolean contains = false;
        int Minor_Number = ble_data[9 + 20] & 0xff;
        Log.e("Minor_Number", String.valueOf(Minor_Number));
        for (ScannedDevice device : mList) {
            if (newDevice.getAddress().equals(device.getDevice().getAddress())) {
                contains = true;
                // update
                device.setDisplayName(newDevice.getName());
                device.setRssi(rssi);
                device.setLastUpdatedMs(now);
                device.setScanRecord(ble_data);
                break;

            }
        }

        //이곳이다 찾았당
        if (!contains) {
            String[] DeviceNameArray = newDevice.getName().trim().split("-");

            if (DeviceNameArray[0].equals("TJ") && DeviceNameArray[1].equals("0001")&& DeviceNameArray.length >= 3) {
                if (ScanActivity.BLE_type.equals("")) {
                    mList.add(new ScannedDevice(newDevice, rssi, ble_data, now));
                } else if (ScanActivity.BLE_type.equals(DeviceNameArray[1].toString())) {
                    mList.add(new ScannedDevice(newDevice, rssi, ble_data, now));
                    Log.e("ddd",newDevice.getName()+","+Minor_Number);
                }
            }

        }


        // sort by Name Or RSSI
        Collections.sort(mList, new Comparator<ScannedDevice>() {
            @Override
            public int compare(ScannedDevice lhs, ScannedDevice rhs) {
                if(ScanActivity.sort_type.equals("name")){
                    return lhs.getDisplayName().compareTo(rhs.getDisplayName());
                }else{
                    if (lhs.getRssi() == 0) {
                        return 1;

                    } else if (rhs.getRssi() == 0) {
                        return -1;
                    }
                    if (lhs.getRssi() > rhs.getRssi()) {
                        return -1;
                    } else if (lhs.getRssi() < rhs.getRssi()) {
                        return 1;
                    }
                    return 0;
                }
            }
        });
        notifyDataSetChanged();
        String summary = "";            // 사용 안함
        return summary;
    }

    public int byteArrayToInt(byte bytes[], int startIdx) {
        return ((((int) bytes[startIdx + 3] & 0xff) << 24) |
                (((int) bytes[startIdx + 2] & 0xff) << 16) |
                (((int) bytes[startIdx + 1] & 0xff) << 8) |
                (((int) bytes[startIdx + 0] & 0xff)));
    }
    private int ConvertToIntLittletree(byte[] txValue, int startidx) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(6);
        // by choosing big endian, high order bytes must be put
        // to the buffer before low order bytes
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        // since ints are 4 bytes (32 bit), you need to put all 4, so put 0
        // for the high order bytes
        byteBuffer.put(txValue[startidx]);
        byteBuffer.put(txValue[startidx + 1]);
        byteBuffer.put(txValue[startidx + 2]);
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
    private int ConvertToIntLittle2(byte[] txValue, int startidx) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
        // by choosing big endian, high order bytes must be put
        // to the buffer before low order bytes
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        // since ints are 4 bytes (32 bit), you need to put all 4, so put 0
        // for the high order bytes
        byteBuffer.put(txValue[startidx+1]);
        byteBuffer.put(txValue[startidx]);
        byteBuffer.put((byte) 0x00);
        byteBuffer.put((byte) 0x00);

        byteBuffer.flip();
        int result = byteBuffer.getInt();
        return result;
    }
    private int ConvertToIntLittle3(byte[] txValue, int startidx) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
        // by choosing big endian, high order bytes must be put
        // to the buffer before low order bytes
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        // since ints are 4 bytes (32 bit), you need to put all 4, so put 0
        // for the high order bytes
        byteBuffer.put(txValue[startidx+1]);
        byteBuffer.put(txValue[startidx]);
        byteBuffer.put((byte) 0x00);
        byteBuffer.put((byte) 0x00);

        byteBuffer.flip();
        String result = Integer.toHexString(byteBuffer.getInt());
       // int result = byteBuffer.getInt();
        return byteBuffer.getInt();
    }
    private String ConvertToIntLittle4(byte[] txValue, int startidx) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
        // by choosing big endian, high order bytes must be put
        // to the buffer before low order bytes
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        // since ints are 4 bytes (32 bit), you need to put all 4, so put 0
        // for the high order bytes
        byteBuffer.put(txValue[startidx]);
        byteBuffer.put(txValue[startidx+1]);
        byteBuffer.put((byte) 0x00);
        byteBuffer.put((byte) 0x00);

        byteBuffer.flip();
        String result = String.format("%02X", byteBuffer.getInt()&0xffff);//Integer.toHexString(byteBuffer.getInt());
        // int result = byteBuffer.getInt();
        return result;
    }
    private int ConvertToIntGPS(byte[] txValue, int startidx) {
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
    private int  ConvertTofloatFour(byte[] txValue, int startidx) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(8);
        // by choosing big endian, high order bytes must be put
        // to the buffer before low order bytes
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        // since ints are 4 bytes (32 bit), you need to put all 4, so put 0
        // for the high order bytes
        byteBuffer.put(txValue[startidx]);
        byteBuffer.put(txValue[startidx + 1]);
        byteBuffer.put(txValue[startidx + 2]);
        byteBuffer.put(txValue[startidx + 3]);
        byteBuffer.put((byte) 0x00);
        byteBuffer.put((byte) 0x00);
        byteBuffer.put((byte) 0x00);
        byteBuffer.put((byte) 0x00);
        byteBuffer.flip();
        int result = byteBuffer.getInt();
        return result;
    }
    private int ConvertToIntBig(byte[] txValue, int startidx) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
        // by choosing big endian, high order bytes must be put
        // to the buffer before low order bytes
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        // since ints are 4 bytes (32 bit), you need to put all 4, so put 0
        // for the high order bytes
        byteBuffer.put((byte) 0x00);
        byteBuffer.put((byte) 0x00);
        byteBuffer.put(txValue[startidx]);
        byteBuffer.put(txValue[startidx + 1]);


        byteBuffer.flip();
        int result = byteBuffer.getInt();
        return result;
    }

    private short ConvertToShortBig(byte[] txValue, int startidx) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put(txValue[startidx]);
        bb.put(txValue[startidx + 1]);
        short shortVal = bb.getShort(0);
        return shortVal;
    }

    private short ConvertToShortLittle(byte[] txValue, int startidx) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(txValue[startidx]);
        bb.put(txValue[startidx + 1]);
        short shortVal = bb.getShort(0);
        return shortVal;
    }
    private short ConvertToShorttree(byte[] txValue, int startidx) {
        ByteBuffer bb = ByteBuffer.allocate(3);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(txValue[startidx]);
        bb.put(txValue[startidx + 1]);
        bb.put(txValue[startidx + 2]);
        return bb.getShort(0);
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

    private String ConvertToSensorName(int sensonCode) {
        String sname = "";

        switch (sensonCode) {
            case 1:
                sname = "O2";
                break;
            case 2:
                sname = "CO";
                break;
            case 3:
                sname = "H2S";
                break;
            case 4:
                sname = "Co2";
                break;
        }
        return sname;
    }

    public List<ScannedDevice> getList() {
        return mList;
    }

    private void bledata_save(String file_name, int rssi, String device_name) {
        //long now = System.currentTimeMillis();


        String str_Path_Full = Environment.getExternalStorageDirectory()
                .getAbsolutePath();

        long now = System.currentTimeMillis();
        String now_time = get_yyyyMMddHHmmssSSS(now);

        bledata = now_time + "," + rssi;

        str_Path_Full += "/" + dataSaveFolder + "/" + device_name + "_" + file_name + ".csv";
        File file = new File(str_Path_Full);

        file = new File(path);

        if (!file.exists()) {
            file.mkdir();
        }
        if (file.exists() == false) {
            try {
                file.createNewFile();
            } catch (IOException e) {
//                Log.e(TAG, e.toString());
//            }

            }
        }
        try {
            BufferedWriter bfw;
            bfw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(str_Path_Full, true), "MS949"));
            bfw.write(bledata + "\r\n");
            bfw.flush();
            bfw.close();
        } catch (FileNotFoundException e) {
//            Log.e(TAG, e.toString());
        } catch (IOException e) {
//            Log.e(TAG, e.toString());
        }
    }

    static String bledata;
    static long blenow = 0;
    static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + dataSaveFolder + File.separator;
    static File file;
    static File savefile_ble;
    static FileOutputStream fos_ble;

    public static void bledata_save_1(String file_name, int rssi, String device_name) {

        long now = System.currentTimeMillis();
        String now_time = get_yyyyMMddHHmmssSSS(now);

        // blenow = System.nanoTime();
        bledata = rssi + "," + now_time + "\r\n";
        if (!fos_open_flag_ble) {                       //new file
            try {
                file = new File(path);

                if (!file.exists()) {
                    file.mkdir();
                    savefile_ble = new File(path + file_name + "_" + device_name + ".csv");
                } else {
                    savefile_ble = new File(path + file_name + "_" + device_name + ".csv");
                }

                fos_ble = new FileOutputStream(savefile_ble);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            fos_open_flag_ble = true;
        }

        try {
            fos_ble.write(bledata.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String gps_cal(double ladata, double lodata) {
        double latitude_value = ladata; //Double.parseDouble(ladata);
        double longitude_value = lodata; //Double.parseDouble(lodata);

        double aaa = latitude_value * 0.01;
        int la_do = (int)(Math.floor(aaa));
        double la = (latitude_value - la_do * 100);
        int la_minute = (int)(Math.floor(la));
        double la_second = (la - la_minute) * 60;

        double la_cal = la_do + ((double)la_minute/60) + ((double)la_second/3600);

        double bbb = longitude_value * 0.01;
        int lo_do = (int)(Math.floor(bbb));
        double lo = (longitude_value - lo_do * 100);
        int lo_minute = (int)(Math.floor(lo));
        double lo_second = (lo - lo_minute) * 60;

        double lo_cal = lo_do + ((double)lo_minute/60) + ((double)lo_second/3600);


        return String.format("%.4f", la_cal) + "," + String.format("%.4f", lo_cal);
    }
}
