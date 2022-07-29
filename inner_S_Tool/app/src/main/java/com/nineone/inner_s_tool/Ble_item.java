package com.nineone.inner_s_tool;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

public class Ble_item {
    private String Tag_Adress;
    private String Tag_Name;
    private ArrayList<Integer> Tag_Rssi_arrary = new ArrayList<>();

    public Ble_item(String Tag_Adress, String Tag_Name, int Tag_Rssi) {
        this.Tag_Adress = Tag_Adress;
        this.Tag_Name = Tag_Name;
       // this.Tag_Rssi = new ArrayList<>();
        this.Tag_Rssi_arrary.add(Tag_Rssi);
    }

    public String getTag_Adress() { return Tag_Adress; }
    public void setTag_Adress(String tag_Adress) { Tag_Adress = tag_Adress; }

    public String getTag_Name() { return Tag_Name; }
    public void setTag_Name(String tag_Name) { Tag_Name = tag_Name; }

    public ArrayList<Integer> getTag_Rssi_arrary() { return Tag_Rssi_arrary; }
    public void setTag_Rssi_arrary(ArrayList<Integer> tag_Rssi) { Tag_Rssi_arrary = tag_Rssi; }

    public void setTag_int_Rssi(int tag_Rssi) { Tag_Rssi_arrary.add(tag_Rssi); }

}
