package com.nineone.dummy_data_send;

import java.util.ArrayList;

public class Ble_item {
    private String Tag_Adress;
    private String Tag_Name;
    private int Tag_Rssi;
    private ArrayList<Integer> Tag_Rssi_arrary = new ArrayList<>();
    private byte[] Tag_Adress_byte;
    private byte[] Tag_ScanRecord01,Tag_ScanRecord02;
    private int tag0102;
    public Ble_item(String Tag_Adress, String Tag_Name, int Tag_Rssi,byte[] Tag_Adress_byte, byte[] Tag_ScanRecord,int tag0102) {
        this.Tag_Adress = Tag_Adress;
        this.Tag_Name = Tag_Name;
        this.Tag_Rssi = Tag_Rssi;
       // this.Tag_Rssi = new ArrayList<>();
        this.Tag_Rssi_arrary.add(Tag_Rssi);
        this.Tag_Adress_byte = Tag_Adress_byte;
        this.tag0102 = tag0102;
        if(this.tag0102==1) {
            this.Tag_ScanRecord01 = Tag_ScanRecord;
        }else if(this.tag0102==2) {
            this.Tag_ScanRecord02 = Tag_ScanRecord;
        }

    }

    public int getTag_Rssi() {return Tag_Rssi;}
    public void setTag_Rssi(int tag_Rssi) {Tag_Rssi = tag_Rssi;}

    public byte[] getTag_Adress_byte() {return Tag_Adress_byte;}
    public void setTag_Adress_byte(byte[] tag_Adress_byte) {Tag_Adress_byte = tag_Adress_byte;}

    public byte[] getTag_ScanRecord01() {return Tag_ScanRecord01;}
    public void setTag_ScanRecord01(byte[] tag_ScanRecord01) {Tag_ScanRecord01 = tag_ScanRecord01;}

    public byte[] getTag_ScanRecord02() {return Tag_ScanRecord02;}
    public void setTag_ScanRecord02(byte[] tag_ScanRecord02) {Tag_ScanRecord02 = tag_ScanRecord02;}


    public int getTag0102() {return tag0102;}
    public void setTag0102(int tag0102) {this.tag0102 = tag0102;}

    public String getTag_Adress() { return Tag_Adress; }
    public void setTag_Adress(String tag_Adress) { Tag_Adress = tag_Adress; }

    public String getTag_Name() { return Tag_Name; }
    public void setTag_Name(String tag_Name) { Tag_Name = tag_Name; }

    public ArrayList<Integer> getTag_Rssi_arrary() { return Tag_Rssi_arrary; }
    public void setTag_Rssi_arrary(ArrayList<Integer> tag_Rssi) { Tag_Rssi_arrary = tag_Rssi; }

    public void setTag_int_Rssi(int tag_Rssi) { Tag_Rssi_arrary.add(tag_Rssi); }

}
