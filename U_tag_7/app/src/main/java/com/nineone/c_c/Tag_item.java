package com.nineone.c_c;

public class Tag_item {

    private String item_Name;
    private int item_Rssi;
    private byte item_byte_num1;
    private byte item_byte_num2;
    private byte item_byte_rssi;
    private String item_time;


    public Tag_item(String item_Name, int item_Rssi, byte item_byte_num1, byte item_byte_num2, byte item_byte_rssi, String item_time){
        this.item_Name = item_Name;
        this.item_Rssi = item_Rssi;
        this.item_byte_num1  = item_byte_num1;
        this.item_byte_num2 = item_byte_num2;
        this.item_byte_rssi = item_byte_rssi;
        this.item_time = item_time;
    }
    public String getItem_Name() { return item_Name; }
    public void setItem_Name(String item_Name) { this.item_Name = item_Name; }

    public int getItem_Rssi() { return item_Rssi; }
    public void setItem_Rssi(int item_Rssi) { this.item_Rssi = item_Rssi; }

    public byte getItem_byte_num1() { return item_byte_num1; }
    public void setItem_byte_num1(byte item_byte_num1) { this.item_byte_num1 = item_byte_num1; }

    public byte getItem_byte_num2() { return item_byte_num2; }
    public void setItem_byte_num2(byte item_byte_num2) { this.item_byte_num2 = item_byte_num2; }
    public byte getItem_byte_rssi() { return item_byte_rssi; }
    public void setItem_byte_rssi(byte item_byte_rssi) { this.item_byte_rssi = item_byte_rssi; }

    public String getItem_time() { return item_time; }
    public void setItem_time(String item_time) { this.item_time = item_time; }
}
