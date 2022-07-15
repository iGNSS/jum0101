package com.example.picturemap;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.naver.maps.geometry.LatLng;

import java.util.List;

@Entity(tableName="mydatalist")//Room 데이터 베이스에 들어갈 데이터 모델이 무엇인지 정해는 클래스
public class MyDataList {
    /* renamed from: id */
    @PrimaryKey(autoGenerate = true)//기본키를 id로 설정
    private int id;
   // @ColumnInfo(name = "name")
    private String day;
    private String time;
   //  @ColumnInfo(name = "email")
    private String distance;

   // @ColumnInfo(name = "city")
    private List<LatLng> city;

    public MyDataList(@NonNull String day, String time, String distance, List<LatLng> city) {
        this.day = day;
        this.time = time;
        this.distance = distance;
        this.city = city;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDay() { return day; }

    public void setDay(String day) { this.day = day; }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDistance() {
        return this.distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public List<LatLng> getCity() {
        return this.city;
    }

    public void setCity(List<LatLng> city) {
        this.city = city;
    }

    public MyDataList(){
        this.day = "day";
        this.time = "time";
        this.distance ="distance";

       // this.city = "city";
    }

    @Override
    public String toString() {
        return "zMyDataList{" +
                "id=" + id +
                ", time='" + time + '\'' +
                ", distance='" + distance + '\'' +
                ", city=" + city +
                '}';
    }


}