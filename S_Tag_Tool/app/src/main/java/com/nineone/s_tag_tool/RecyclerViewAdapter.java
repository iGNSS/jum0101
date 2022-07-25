package com.nineone.s_tag_tool;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.nineone.s_tag_tool.MainFragment.STAG_0007;
import static com.nineone.s_tag_tool.MainFragment.STAG_00C8;
import static com.nineone.s_tag_tool.MainFragment.STAG_00C9;
import static com.nineone.s_tag_tool.MainFragment.STAG_00CA;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<ScannedDevice> listData = new ArrayList<>();

    @Override
    public int getItemCount() {
        return listData.size();
    }
    // Item의 클릭 상태를 저장할 array 객체
    private final SparseBooleanArray selectedItems = new SparseBooleanArray();
    // 직전에 클릭됐던 Item의 position
    private int prePosition = -1;
    private final Activity activity;
    private final Context mContext;
    private final boolean mScanmode;
    private SoundPool soundPool;
    private int soundPlay;
    public RecyclerViewAdapter(Activity activity, Context mContext,boolean mScanmode) {
        //this.recyclerView = recyclerView;
        this.activity=activity;
        this.mContext = mContext;
        this.mScanmode=mScanmode;
        startTimerTask();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        soundPool = new SoundPool.Builder().setMaxStreams(8).build();
        soundPlay = soundPool.load(activity, R.raw.arml, 1);

        return new ViewHolder(view);
    }
    SimpleDateFormat aftertime;
    private boolean alarm_ON_OFF = false;
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final ScannedDevice item = listData.get(position);
        final ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.onBind(listData.get(position),position, selectedItems);
        int get_position = viewHolder.getAdapterPosition();
        aftertime = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
        viewHolder.tagname.setText(item.getDisplayName());
        if(mScanmode){
            viewHolder.tagadress.setText(item.getDevice().getAddress());
        }
        String item_Rssi = item.getRssi()+" dBm";
        String item_updateMS = aftertime.format(item.getLastUpdatedMs());
        viewHolder.tagrssi.setText(item_Rssi);
        viewHolder.tagdevicetime.setText(item_updateMS);
        viewHolder.tagdata.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        viewHolder.tagdata_etc.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        viewHolder.tagdevicetime.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        viewHolder.tagrssi.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        viewHolder.tagadress.setTextColor(ContextCompat.getColor(mContext, R.color.black));


        long now = System.currentTimeMillis();
        Date dateNow = new Date(now);
        Date dateCreated = new Date(item.getLastUpdatedMs());
        long duration = dateNow.getTime() - dateCreated.getTime();
        long nResult  = duration/1000;
        //  Log.e("Arrays.toString12",device.getDisplayName()+","+nResult);
        boolean getstopfalse = false;
        if (nResult>3) {
            getstopfalse=true;
        }
        byte[] ble_data = item.getScanRecord();
        String[] DeviceNameArray = item.getDisplayName().trim().split("-");
        String sensordata = "";
        String sensordata_etc = "";
        int sensorStartIdx = 9;
        int BATTVal = ble_data[sensorStartIdx] & 0xff;
        double BATTDisplayVal = BATTVal * 0.1;
        short Ax_10 = 0;
        short Ay_10 = 0;
        short Az_10 = 0;

        short temperature=0;

        int Move_check = 0;

        int barometerVal = 0;
        double BAROMETER_10 = 0.0;


        int minor_number = 0;
        int major_number = 0;
        int Sensor_Alarm = 0;
        //  int Sensor_Alarm = 250;
        int senser_type =  0;
        int os2_errer =  0;
        int CO_errer2 =  0;
        int H2S_errer2 = 0;
        int CO2_errer2 = 0;
        int CH4_errer2 = 0;
        //String index_num = "";
        long index_num = 0;
        boolean mAlarm_on = false;
        String string_O2 = "";String string_CO = "";String string_H2S = "";String string_CO2 = "";  String string_CH4 = "";
        switch (DeviceNameArray[1]) {

        /*    case STAG_0007:
                int receDataLength = ble_data.length;
                Log.e("Tag_Rece0", ", "+receDataLength);
                //   if (receDataLength == 20) {
                Log.e("Tag_Rece1",DeviceNameArray[1]);
                Log.e("Tag_Rece2", Arrays.toString(ble_data));

             //   Log.e("Tag getUuids: ", Arrays.toString(item.getDevice().getUuids()) + "");
                if (ble_data[0] == 0x02 && ble_data[19] == 0x03) {
                    int rece_tag_type = ConvertToIntLittle(ble_data, 2);
                    Log.e("Tag_Rece3", ble_data[1]+", "+ble_data[2]);
                    String tag_type_str = "";
                    switch (rece_tag_type) {
                        case 200:
                            tag_type_str = "00C8";

                            break;
                        case 201:
                            tag_type_str = "00C9";
                            break;
                        case 202:
                            tag_type_str = "00CA";
                            break;
                        case 7:
                            tag_type_str = "0007";
                            break;
                    }

                    int rece_tag_no = ConvertToIntLittle2(ble_data, 4);

                    int rece_rf_power = ble_data[8];
                    Log.e("Tag Rece RF Power: ", rece_rf_power + "");

                    int rece_sensor_type = ble_data[9] & 0xff;
                    String sensor_type_str = "";
                    switch (rece_sensor_type) {
                        case 1:
                            sensor_type_str = "O2";
                            break;
                        case 2:
                            sensor_type_str = "Co";
                            break;
                        case 3:
                            sensor_type_str = "H2S";
                            break;
                        case 4:
                            sensor_type_str = "Co2";
                            break;
                    }
                    Log.e("Tag Rece Sensor Type: ", rece_sensor_type + "/" + sensor_type_str);

                    int rece_cal_status = ble_data[10] & 0xff;
                    String gro_cal_status = "보정 안됨 ";
                    if (rece_cal_status == 1) {
                        gro_cal_status = "보정 완료";
                    }
                    Log.e("Tag Rece cal Type: ", rece_cal_status + "/" + gro_cal_status);

                    int rece_sensor_power_on = ble_data[11] & 0xff;
                    Log.e("Tag sensor Power On: ", rece_sensor_power_on + "");

                    int rece_sensor_read = ble_data[12] & 0xff;
                    Log.e("Tag sensor Read: ", rece_sensor_read + "");

                    int rece_sensor_power_off = ble_data[13] & 0xff;
                    Log.e("Tag sensor Read: ", rece_sensor_power_off + "");

                    //  int rece_bar_ADC = txValue[14] & 0xff;
                    //  Log.e("Tag sensor Read: ", rece_bar_ADC + "");

                    int rece_bar_ADC = ConvertToIntLittle(ble_data, 14);
                    Log.e("Tag sensor Read: ", rece_bar_ADC + "");
                    int rece_ver = ble_data[18] & 0xff;

                    Log.e("Tag Ver: ", rece_ver + "");
                    sensordata = "ID Type: " + tag_type_str + " \r\n"
                            + "Tag Number: " + String.valueOf(rece_tag_no) + " \r\n"
                            + "RF Power: " + String.valueOf(rece_rf_power) +" \r\n"
                            + "Sensor Type: " + String.valueOf(sensor_type_str) +" \r\n"
                            + "Gyro Cal: " + String.valueOf(gro_cal_status) +" \r\n"
                            + "Sens On Time: " + String.valueOf(rece_sensor_power_on) +" \r\n"
                            + "Sens Read Time: " + String.valueOf(rece_sensor_read) +" \r\n"
                            + "Sens Off Time: " + String.valueOf(rece_sensor_power_off) +" \r\n"
                            + "Bar ADC" + String.valueOf(rece_bar_ADC);
                    // } else {
                    //      int rece_cal_status = ble_data[7] & 0xff;
                    //     Log.e("칼 상태 체크", rece_cal_status + "");
                    // }


                    // Log.e("STag", "End...." + accelerometerX);
                } else {
                    Log.e("BLE TAG DATA", "BLE DATA Length is " + receDataLength);
                }
                break;*/
            case STAG_0007:
           case STAG_00C8:
           case STAG_00C9:
               sensordata = item.getScanRecordHexString();
               viewHolder.tagdata.setText(sensordata);
               viewHolder.tagadress.setText(item.getDevice().getAddress());
               viewHolder.tagdata_etc.setText("");
               if(getstopfalse){
                   //sensordata = "수신 중지 됨";
                   //viewHolder.tagdevicetime.setText("수신 중지 됨");
                   viewHolder.tagadress.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                   viewHolder.tagdata.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                   viewHolder.tagdata_etc.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                   viewHolder.tagdevicetime.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                   viewHolder.tagrssi.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
               }
                break;
           /*  case STAG_00C9:

                barometerVal = ConvertToIntLittle(ble_data, sensorStartIdx + 1);

                Ax_10 = ConvertToShortLittle(ble_data, sensorStartIdx + 3);
                Ay_10 = ConvertToShortLittle(ble_data, sensorStartIdx + 5);
                Az_10 = ConvertToShortLittle(ble_data, sensorStartIdx + 7);

                Move_check = ble_data[sensorStartIdx + 9] & 0xff;
                //double ASqartVal = ASqart;

                temperature = ConvertToShortLittle(ble_data, sensorStartIdx + 10);
                major_number  = ConvertToShortLittle(ble_data, sensorStartIdx + 16);
                minor_number= ConvertToShortLittle(ble_data, sensorStartIdx + 18);
                BAROMETER_10 = (barometerVal + 80000) * 0.01;
                sensordata =
                         "Acc: " + String.format(Locale.KOREA,"%.2f", Ax_10 * 0.01)
                        + " , " + String.format(Locale.KOREA,"%.2f", Ay_10 * 0.01)
                        + " , " + String.format(Locale.KOREA,"%.2f", Az_10 * 0.01) + "\r\n"
                        + "Move: " + String.format("%s", Move_check) + "\r\n"
                        + "temperature: " + String.format(Locale.KOREA,"%.2f", temperature * 0.01)
                        + "Major: " + major_number + "\r\n"
                        + "Minor: " + minor_number;
                sensordata_etc =  "BATT : " + String.format(Locale.KOREA, "%.2f", BATTDisplayVal) + " V   "
                        + "BM  : " + String.format(Locale.KOREA,"%.2f", BAROMETER_10) + " hPa";
                viewHolder.tagdata.setText(sensordata);
                viewHolder.tagdata_etc.setText(sensordata_etc);
                break;
            case STAG_0007:*/
            case STAG_00CA:

                barometerVal = ConvertToIntLittle(ble_data, sensorStartIdx + 1);

                 Sensor_Alarm = ble_data[sensorStartIdx + 5];
              //  int Sensor_Alarm = 250;
                 senser_type =  ((Sensor_Alarm) & 0x07);
               // Log.e("ble_data00", String.valueOf(senser_type));
                 os2_errer =  ((Sensor_Alarm >> 7)& 0x01);
               // Log.e("os2_errer", String.valueOf(os2_errer));
                 CO_errer2 =  ((Sensor_Alarm >> 6) & 0x01);
              //  Log.e("CO_errer2", String.valueOf(CO_errer2));
                 H2S_errer2 = ((Sensor_Alarm >> 5) & 0x01);
              //  Log.e("H2S_errer2", String.valueOf(H2S_errer2));
                 CO2_errer2 = ((Sensor_Alarm >> 4) & 0x01);
             //   Log.e("CO2_errer2", String.valueOf(CO2_errer2));
                 CH4_errer2 = ((Sensor_Alarm >> 3) & 0x01);
              //  Log.e("CH4_errer2", String.valueOf(CH4_errer2));
                if(DeviceNameArray.length>=3) {
                    //index_num = DeviceNameArray[2];
                    index_num = Long.parseLong(DeviceNameArray[2], 16);
                }
             //   Log.e("senser_type", String.valueOf(senser_type));
                if(!mScanmode){
                    String sneer_type_name = "";
                    if(senser_type==1){
                         sneer_type_name = "BTS1"+"-"+index_num;
                    }else if(senser_type==2){
                         sneer_type_name = "BTS5"+"-"+index_num;
                    }else if(senser_type==3){
                        sneer_type_name = "BTS0"+"-"+index_num;
                    }else if(senser_type==5){
                        sneer_type_name = "BTS1"+"-"+index_num;
                    }else if(senser_type==6){
                        sneer_type_name = "BTS5"+"-"+index_num;
                    }

                    viewHolder.tagadress.setText(sneer_type_name);
                }
               // Log.e("ble_data0",item.getDisplayName());
              //  Log.e("ble_data1",(Arrays.toString(ble_data)));
              //  Log.e("ble_data2", byteArrayToHex(ble_data));
                int senser_O2 = ConvertToIntLittle(ble_data, sensorStartIdx + 6);
                Log.e("655535", String.valueOf(senser_O2));
                if(senser_O2 == 0xFFFF) {
                    string_O2 = "센서 측정 중";
                    viewHolder.tagbutton.setEnabled(false);
                    viewHolder.tagbutton.getBackground().setTint(ContextCompat.getColor(mContext, R.color.gray));
                }else{
                    string_O2 = String.format(Locale.KOREA,"%.2f", senser_O2 * 0.01) + " %  ";

                    viewHolder.tagbutton.setEnabled(true);
                    viewHolder.tagbutton.getBackground().setTint(ContextCompat.getColor(mContext, R.color.black));
                }

                int senser_CO = ConvertToIntLittle(ble_data, sensorStartIdx + 8);
                if(senser_CO==0xFFFF) {
                    string_CO = "센서 측정 중";
                }else{
                    string_CO = senser_CO + " ppm  ";
                }

                int senser_H2S = ConvertToIntLittle(ble_data, sensorStartIdx + 10);

                if(senser_H2S==0xFFFF) {
                    string_H2S = "센서 측정 중";
                }else{
                    string_H2S = senser_H2S + " ppm  ";
                }

                int senser_CO2 = ConvertToIntLittle(ble_data, sensorStartIdx + 12);

                if(senser_CO2==0xFFFF) {
                    string_CO2 = "센서 측정 중";
                }else{
                    string_CO2 = senser_CO2 + " ppm  ";
                }

                int senser_CH4 = ConvertToIntLittle(ble_data, sensorStartIdx + 14);
                if(senser_CH4==0xFFFF) {
                    string_CH4 = "센서 측정 중";
                }else{
                    string_CH4 = senser_CH4 + " ppm  ";
                }
                if(item.getDisplayName().equals("TJ-00CA-0000000B-0000")) {
                    int senser_O22 = ConvertToIntLittle(ble_data, 9 + 6);
                    int senser_CO22 = ConvertToIntLittle(ble_data, 9 + 12);

                    Log.e("mAlarm_on1", senser_O22+", "+senser_CO22);

               //     Log.e("mAlarm_on2", Integer.parseInt(String.valueOf(Sensor_Alarm),16)+", "+String.valueOf(os2_errer)+", "+String.valueOf(CO2_errer2));
                }
              //  Log.e("bledata",senser_O2+", "+senser_CO+", "+senser_H2S+", "+senser_CO2+", "+senser_CH4+", ");
                String O2_alarm = "";
                String CO_alarm = "";
                String H2S_alarm = "";
                String CO2_alarm = "";
                String CH4_alarm = "";
                if(os2_errer==1 && !getstopfalse) {
                    O2_alarm = "- 알람 ON";
                    mAlarm_on = true;
                }
                if(CO_errer2==1 && !getstopfalse) {
                    CO_alarm = "- 알람 ON";
                    mAlarm_on = true;
                }
                if(H2S_errer2==1 && !getstopfalse) {
                    H2S_alarm = "- 알람 ON";
                    mAlarm_on = true;
                }
                if(CO2_errer2==1 && !getstopfalse) {
                    CO2_alarm = "- 알람 ON";
                    mAlarm_on = true;
                }
                if(CH4_errer2==1 && !getstopfalse) {
                    CH4_alarm = "- 알람 ON";
                    mAlarm_on = true;
                }
                if(!mAlarm_on){
                    viewHolder.linearbackcolorlayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                }else {
                    viewHolder.linearbackcolorlayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.red));

                    Runnable alarm_runnable = new Runnable() {
                        @Override
                        public void run() {
                            //notifyDataSetChanged();
                            alarm_ON_OFF = false;

                        }
                    };
                    if (!alarm_ON_OFF) {
                        alarm_ON_OFF=true;
                        soundPool.play(soundPlay, 1f, 1f, 6, 0, 1f);
                        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            //  vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                            vibrator.vibrate(VibrationEffect.createOneShot(1000, 150));//0~255
                        } else {
                            vibrator.vibrate(750);

                        }
                        listcange_handler.postDelayed(alarm_runnable, 1000);
                    }
                }
           //     major_number  = ConvertToIntBig(ble_data, sensorStartIdx + 16);
            //    minor_number= ConvertToIntBig(ble_data, sensorStartIdx + 18);

                BAROMETER_10 = (barometerVal + 80000) * 0.01;
                sensordata_etc =  "BATT : " + String.format(Locale.KOREA, "%.2f", BATTDisplayVal) + " V   "
                        + "BM  : " + String.format(Locale.KOREA,"%.2f", BAROMETER_10) + " hPa";
                if(senser_type==1) {
                    sensordata = "O2    : " + string_O2 + O2_alarm;
                    viewHolder.tagdata.setText(sensordata);
                    viewHolder.tagdata_etc.setText(sensordata_etc);
                }
                else if(senser_type==2) {
                    sensordata = "O2    : " + string_O2 + O2_alarm + "\r\n"
                            + "CO    : " + string_CO + CO_alarm + "\r\n"
                            + "H2S  : " + string_H2S + H2S_alarm + "\r\n"
                            + "CO2  : " + string_CO2 + CO2_alarm + "\r\n"
                            + "CH4  : " + string_CH4 + CH4_alarm;
                    viewHolder.tagdata.setText(sensordata);
                    viewHolder.tagdata_etc.setText(sensordata_etc);

                }else if(senser_type==3) {
                    sensordata = "BATT: " + String.format(Locale.KOREA,"%.2f", BATTDisplayVal) + " V\r\n"
                            + "Sensor Type: " + Sensor_Alarm + "\r\n"
                            + "BM     : " + String.format(Locale.KOREA,"%.2f", BAROMETER_10) + " hPa\r\n";
                    viewHolder.tagdata.setText(sensordata);
                    viewHolder.tagdata_etc.setText(sensordata_etc);
                }else if(senser_type==0) {
                    sensordata = "BATT: " + String.format(Locale.KOREA,"%.2f", BATTDisplayVal) + " V\r\n"
                            + "BM : " + String.format(Locale.KOREA,"%.2f", BAROMETER_10) + " hPa\r\n"
                            //+ "Move: " + Move_check + " \r\n"
                            + "Sensor type: 에러" + "";
                    viewHolder.tagdata.setText(sensordata);
                }else{
                    sensordata = "데이터 없음";
                    Log.e("ble_data1",(Arrays.toString(ble_data)));
                    viewHolder.tagdata.setText(sensordata);
                }
                if(getstopfalse){
                    //sensordata = "수신 중지 됨";
                    //viewHolder.tagdevicetime.setText("수신 중지 됨");
                    viewHolder.tagadress.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                    viewHolder.tagdata.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                    viewHolder.tagdata_etc.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                    viewHolder.tagdevicetime.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                    viewHolder.tagrssi.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                }



                break;


            default:
                long index_num2 = 0;
                sensordata = item.getScanRecordHexString();
                viewHolder.tagdata.setText(sensordata);
                viewHolder.tagadress.setText(item.getDevice().getAddress());
                viewHolder.tagdata_etc.setText("");

                    //long  index_num2 = DeviceNameArray[2];
                index_num2 = Long.parseLong(DeviceNameArray[2], 16);


             //   Log.e("senser_type", String.valueOf(senser_type));
                if(!mScanmode){
                    String sneer_type_name = "";
                    sneer_type_name = "BTS1"+"-"+index_num2;
                    viewHolder.tagadress.setText(sneer_type_name);
                }
                if(getstopfalse){
                    //sensordata = "수신 중지 됨";
                    //viewHolder.tagdevicetime.setText("수신 중지 됨");
                    viewHolder.tagadress.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                    viewHolder.tagdata.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                    viewHolder.tagdata_etc.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                    viewHolder.tagdevicetime.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                    viewHolder.tagrssi.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                }
             //   Log.e("ble_data0",item.getDisplayName());
              //  Log.e("ble_data1",(Arrays.toString(ble_data)));
              //  Log.e("ble_data2", asHex(ble_data));
               // sensordata = item.getScanRecordHexString();
        }

      //  viewHolder.changeVisibility(selectedItems.get(get_position));
        viewHolder.setOnViewHolderItemClickListener(new OnViewHolderItemClickListener() {
            @Override
            public void onViewHolderItemClick() {
                if (selectedItems.get(get_position)) {
                    // 펼쳐진 Item을 클릭 시
                    selectedItems.delete(get_position);
                } else {
                    // 직전의 클릭됐던 Item의 클릭상태를 지움
                    //   selectedItems.delete(prePosition);
                    // 클릭한 Item의 position을 저장
                    selectedItems.put(get_position, true);
                }
                // 해당 포지션의 변화를 알림
                if (prePosition != -1) {
                    notifyItemChanged(prePosition);
                }
                notifyItemChanged(get_position);
                // 클릭된 position 저장
                prePosition = get_position;
            }
        });
      /*  viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (selectedItems.get(get_position)) {
                    // 펼쳐진 Item을 클릭 시
                    selectedItems.delete(get_position);
                } else {
                    // 직전의 클릭됐던 Item의 클릭상태를 지움
                    //   selectedItems.delete(prePosition);
                    // 클릭한 Item의 position을 저장
                    selectedItems.put(get_position, true);
                }
                // 해당 포지션의 변화를 알림
                if (prePosition != -1) {
                    notifyItemChanged(prePosition);
                }
                notifyItemChanged(get_position);
                // 클릭된 position 저장
                prePosition = get_position;
            }
        });*/
     /*   viewHolder.tagbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(get_position != RecyclerView.NO_POSITION){

                    if(mListener !=null){
                        mListener.onItemClick(view,get_position);

                    }
                }
            }
        });*/
    }

    private OnItemClickListener mListener = null; //어뎁터 클릭기능 추가 리스너
    public interface OnItemClickListener {
        void onItemClick(View v, int position) ;
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener ;
    }

    public ArrayList<ScannedDevice> ScannedDeviceList(){
        return listData;
    }



    public String update(BluetoothDevice newDevice, int rssi, byte[] scanRecord) {
        if ((newDevice == null) || (newDevice.getAddress() == null) || newDevice.getName() == null) {
            return "";
        }
        long now = System.currentTimeMillis();

        boolean contains = false;
        for (ScannedDevice device : listData) {
            if (newDevice.getAddress().equals(device.getDevice().getAddress())) {
                contains = true;
                // update
                device.setDisplayName(newDevice.getName());
                device.setRssi(rssi);
                device.setLastUpdatedMs(now);
                device.setScanRecord(scanRecord);

                break;
            }

        }
        if (!contains) {
            String[] DeviceNameArray = newDevice.getName().trim().split("-");
            if ( DeviceNameArray.length >= 3) {
          //  if (DeviceNameArray[0].equals("NI") && DeviceNameArray.length >= 3) {
                if (MainFragment.BLE_type.equals("")) {
                  //  Log.e("qweasd3","asdasd");
                    listData.add(new ScannedDevice(newDevice, rssi, scanRecord, now));
                } else if (MainFragment.BLE_type.equals(DeviceNameArray[1].toString())) {
                    listData.add(new ScannedDevice(newDevice, rssi, scanRecord, now));
                   // list_notifyDataSetChanged();
                }
            }

        }

        // sort by Name Or RSSI
        Collections.sort(listData, new Comparator<ScannedDevice>() {
            @Override
            public int compare(ScannedDevice lhs, ScannedDevice rhs) {
                if(MainFragment.sort_type.equals("name")){
                    if(lhs.getDisplayName()!=null&&rhs.getDisplayName()!=null) {
                        return lhs.getDisplayName().compareTo(rhs.getDisplayName());
                    }else{
                        return 0;
                    }
                }else{
              //  if(MainFragment.sort_type.equals("rssi")){
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
                }/*else{
                    if(lhs.getDisplayName()!=null&&rhs.getDisplayName()!=null) {
                        String[] DeviceNameArray1 = lhs.getDisplayName().trim().split("-");
                        String[] DeviceNameArray2 = rhs.getDisplayName().trim().split("-");
                        return DeviceNameArray1[2].compareTo(DeviceNameArray2[2]);
                    }else{
                        return 0;
                    }
                }*/
            }
        });
      //  Collections.sort(listData, cmpAsc1);
      //  Collections.sort(listData, cmpAsc2);
       if(!testboolean){
           testboolean=true;
            list_notifyDataSetChanged();

       }
        return "";
    }
    public Comparator<ScannedDevice> cmpAsc1 = new Comparator<ScannedDevice>() {
        @Override
        public int compare(ScannedDevice o1, ScannedDevice o2) {
            String[] DeviceNameArray1 = o1.getDisplayName().trim().split("-");
            String[] DeviceNameArray2 = o2.getDisplayName().trim().split("-");
       //     Log.e("ScanRecordArray",o1.getDisplayName()+" , "+o2.getDisplayName());
            return DeviceNameArray1[2].compareTo(DeviceNameArray2[2]);
        }
    };

    public Comparator<ScannedDevice> cmpAsc2 = new Comparator<ScannedDevice>() {
        @Override
        public int compare(ScannedDevice o1, ScannedDevice o2) {
            byte[] ScanRecordArray1 = o1.getScanRecord();
            byte[] ScanRecordArray2 = o2.getScanRecord();
           // Log.e("ScanRecordArray1", (ScanRecordArray1[14]&0xFF) + " , " + (ScanRecordArray2[14]&0xFF));
            if((ScanRecordArray1[14]&0xFF)>7||(ScanRecordArray2[14]&0xFF)>7) {
           //     Log.e("ScanRecordArray2", ScanRecordArray1[14] + " , " + ScanRecordArray2[14]);
                return ScanRecordArray2[14] - ScanRecordArray1[14];
            }
            return 0;
        }
    };
    boolean testboolean = false;
    private void list_notifyDataSetChanged(){
        Runnable runnable10 = new Runnable() {
            @Override
            public void run() {
               //notifyDataSetChanged();
                testboolean=false;
                notifyItemRangeChanged(0, item_Count(), null);
            }
        };
        listcange_handler.postDelayed(runnable10, 0);
    }
    private final MyHandler listcange_handler = new MyHandler(this);
    private Timer timer = new Timer();
    private void startTimerTask () {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        for (ScannedDevice device : listData) {
                            aftertime = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
                            long now = System.currentTimeMillis();
                            Date dateNow = new Date(now);
                            Date dateCreated = new Date(device.getLastUpdatedMs());
                            long duration = dateNow.getTime() - dateCreated.getTime();
                            long nResult  = duration/1000;
                          //  Log.e("Arrays.toString12",device.getDisplayName()+","+nResult);
                            if (nResult>=5) {
                                // update
                                device.setDisplayName(device.getDisplayName());
                                device.setRssi(device.getRssi());
                                device.setLastUpdatedMs(device.getLastUpdatedMs());
                                byte[] bytes = device.getScanRecord();
                              //  bytes[9+5] = (byte) (bytes[9+5] & 0x07);
                             /*   if((bytes[9+5] & 0x07)==1){
                                    bytes[9+5] = 0x05;
                                }else if((bytes[9+5] & 0x07)==2){
                                    bytes[9+5] = 0x06;
                                }*/
                                //bytes[9+5] = (byte) ((bytes[9+5] & 0x07) + 0x04);
                            //    Log.e("Arrays.toString2",device.getDisplayName()+","+Arrays.toString( bytes)+", "+ String.valueOf(bytes[14]));
                                device.setScanRecord(bytes);
                                if(!testboolean){
                                    testboolean=true;
                                    list_notifyDataSetChanged();

                                }
                            }

                        }

                    }
                });
            }
        },0, 1500);

    }

    public void stopTimerTask() {//타이머 스톱 함수
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

    }
    private static class MyHandler extends Handler {
        private final WeakReference<RecyclerViewAdapter> mActivity;

        public MyHandler(RecyclerViewAdapter activity) {
            mActivity = new WeakReference<RecyclerViewAdapter>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            RecyclerViewAdapter activity = mActivity.get();
            // ...
        }
    }

    public void item_Clear(){
        listData.clear();
    }
    public int item_Count(){
        return listData.size();

    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView tagname;
        private final TextView tagadress;
        private final TextView tagrssi;
        private final TextView tagdata;
        private final TextView tagdata_etc;
        private final Button tagbutton;
        private final TextView tagdevicetime;
        private TextView tagdevice_drow;
        private final ImageView lowicon;

      //  private LinearLayout dataLinearLayout;
        LinearLayout linearlayout;
        LinearLayout linearbackcolorlayout;
        OnViewHolderItemClickListener onViewHolderItemClickListener;
        public ViewHolder(View itemView) {//ViewHolder에 띄울 텍스트
            super(itemView);
            //txtid=(TextView)itemView.findViewById(R.id.txt_id);
            tagname=(TextView)itemView.findViewById(R.id.device_name);
            tagadress=(TextView)itemView.findViewById(R.id.device_address);
            tagrssi=(TextView)itemView.findViewById(R.id.device_rssi);
            tagdevicetime=(TextView)itemView.findViewById(R.id.device_time);
            tagdata=(TextView) itemView.findViewById(R.id.device_data);
            tagdata_etc=(TextView) itemView.findViewById(R.id.device_data_etc);
            tagbutton=(Button)itemView.findViewById(R.id.device_button);
            //tagdevice_drow=(TextView) itemView.findViewById(R.id.device_drow);
            lowicon=(ImageView) itemView.findViewById(R.id.imagelowView);
            linearlayout =(LinearLayout) itemView.findViewById(R.id.linearlayout);
            linearbackcolorlayout =(LinearLayout) itemView.findViewById(R.id.linearbackcolorlayout);
        //    dataLinearLayout =(LinearLayout) itemView.findViewById(R.id.dataLinearLayout);
            linearlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onViewHolderItemClickListener.onViewHolderItemClick();
                }
            });
            tagbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION){

                        if(mListener !=null){
                           // Intent intent = new Intent(activity, Connect_Activity.class);
                           // intent.putExtra("address",listData.get(pos).getDevice().getAddress());
                          //  activity.startActivity(intent);
                           // activity.finish();
                            mListener.onItemClick(view,pos);

                        }
                    }
                }
            });
        }
        public void onBind(ScannedDevice itemData, int position, SparseBooleanArray selectedItems){

            changeVisibility(selectedItems.get(position));
        }


        private void changeVisibility(final boolean isExpanded) {
          //  tagdata.getLayoutParams().height = 600;
          //  Log.e("getLayoutParams", String.valueOf((int) animation.getAnimatedValue()));
            tagdata.requestLayout();
            tagdata_etc.requestLayout();
            // imageView가 실제로 사라지게하는 부분
            tagdata.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            tagdata_etc.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            if(isExpanded) {
                lowicon.setImageResource(R.drawable.up_bg_result1);
              //  tagdevice_drow.setText("접기");
            }else{
                lowicon.setImageResource(R.drawable.down_bg_result1);
              //  tagdevice_drow.setText("펼치기");
            }
            
        }

        public void setOnViewHolderItemClickListener(OnViewHolderItemClickListener onViewHolderItemClickListener) {
            this.onViewHolderItemClickListener = onViewHolderItemClickListener;
        }
        /*void onBind(Z_list_item z_list_item) {
            tagname.setText(z_list_item.getItem_tag_name());
        }*/
    }
    public void item_noti(){
        //listData.clear(); //here items is an ArrayList populating the RecyclerView
        if (!listData.isEmpty()) {

            listData.clear(); //The list for update recycle view
        }
        listData.size();
        notifyDataSetChanged();
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


    private short ConvertToShortLittle(byte[] txValue, int startidx) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(txValue[startidx]);
        bb.put(txValue[startidx + 1]);
        short shortVal = bb.getShort(0);
        return shortVal;
    }
    private long ConvertToLongLittle(byte[] txValue, int startidx) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(txValue[startidx]);
        bb.put(txValue[startidx + 1]);
        long shortVal = bb.getShort(0);
        return shortVal;
    }

  /*  public static class RecyclerViewEmptySupport extends RecyclerView {
        private View emptyView;

        private final AdapterDataObserver emptyObserver = new AdapterDataObserver() {


            @Override
            public void onChanged() {
                Adapter<?> adapter =  getAdapter();
                if(adapter != null && emptyView != null) {
                    if(adapter.getItemCount() == 0) {
                        emptyView.setVisibility(View.VISIBLE);
                        RecyclerViewEmptySupport.this.setVisibility(View.GONE);
                    }
                    else {
                        emptyView.setVisibility(View.GONE);
                        RecyclerViewEmptySupport.this.setVisibility(View.VISIBLE);
                    }
                }

            }
        };

        public RecyclerViewEmptySupport(Context context) {
            super(context);
        }

        public RecyclerViewEmptySupport(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public RecyclerViewEmptySupport(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        public void setAdapter(Adapter adapter) {
            super.setAdapter(adapter);

            if(adapter != null) {
                adapter.registerAdapterDataObserver(emptyObserver);
            }

            emptyObserver.onChanged();
        }

      //  public void setEmptyView(View emptyView) { this.emptyView = emptyView; }
    }*/
   /*
    static String bytesToBinaryString(Byte b) {
        StringBuilder builder = new StringBuilder();
        String one=null;
        for (int i = 0; i < 8; i++) {
            builder.append(((0x80 >>> i) & b) == 0 ? '0' : '1');

        }

        return builder.toString();
    }
     public int byteArrayToInt(byte bytes[], int startIdx) {
        return ((((int) bytes[startIdx + 3] & 0xff) << 24) |
                (((int) bytes[startIdx + 2] & 0xff) << 16) |
                (((int) bytes[startIdx + 1] & 0xff) << 8) |
                (((int) bytes[startIdx + 0] & 0xff)));
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
            case 0:
                sname = "";
                break;
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
            if(bytes.length == index + 1){
                sb.append(Integer.toHexString(bt).toUpperCase());
            }else{
                sb.append(Integer.toHexString(bt).toUpperCase()+"-");
            }

        }

        /// 16進数の文字列を返す。
        return sb.toString();
    }
    public String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for(final byte b: a)
            sb.append(String.format("%02x ", b&0xff));
        return sb.toString();
    }*/
}
