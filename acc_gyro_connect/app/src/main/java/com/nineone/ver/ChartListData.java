package com.nineone.ver;

public class ChartListData {
    private String name;
    private String number;
    private boolean listckeck;
    public ChartListData(String name, String number,boolean listckeck) {
        this.name = name;
        this.number = number;
        this.listckeck=listckeck;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public boolean getlistckeck() {
        return listckeck;
    }

    public void setlistckeck(boolean listckeck) {
        this.listckeck = listckeck;
    }
}
