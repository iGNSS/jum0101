package com.nineone.s_tag_tool;

import android.bluetooth.BluetoothDevice;

public class Recycler_Item {

    private BluetoothDevice mDevice;
    private String mDisplayName;
    private int mRssi;
    private String m_Uuid_data;
    private byte[] mScanRecord;

    public BluetoothDevice getmDevice() {
        return mDevice;
    }

    public void setmDevice(BluetoothDevice mDevice) {
        this.mDevice = mDevice;
    }

    public String getmDisplayName() {
        return mDisplayName;
    }

    public void setmDisplayName(String mDisplayName) {
        this.mDisplayName = mDisplayName;
    }

    public int getmRssi() {
        return mRssi;
    }

    public void setmRssi(int mRssi) {
        this.mRssi = mRssi;
    }

    public String getM_Uuid_data() {
        return m_Uuid_data;
    }

    public void setM_Uuid_data(String m_Uuid_data) {
        this.m_Uuid_data = m_Uuid_data;
    }
}
