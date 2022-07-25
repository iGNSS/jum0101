package com.ble_connect;

import android.bluetooth.BluetoothDevice;

public class Tag_tiem {
    String id;
    String device_idx;
    String time;
    String sensor_margin;

    public Tag_tiem(String id, String device_idx, String time, String sensor_margin){
        this.id = id;
        this.device_idx = device_idx;
        this.time = time;
        this.sensor_margin = sensor_margin;

    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDevice_idx() {
        return device_idx;
    }

    public void setDevice_idx(String device_idx) {
        this.device_idx = device_idx;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSensor_margin() {
        return sensor_margin;
    }

    public void setSensor_margin(String sensor_margin) {
        this.sensor_margin = sensor_margin;
    }
}
