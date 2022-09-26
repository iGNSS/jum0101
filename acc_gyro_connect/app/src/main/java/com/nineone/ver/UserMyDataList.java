package com.nineone.ver;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName="mydatalist")
public class UserMyDataList {
    /* renamed from: id */
    @PrimaryKey(autoGenerate = true)//기본키를 id로 설정
    private int id;

    @ColumnInfo(name="USERNAME")
    private String name;
    private String age;
    //  @ColumnInfo(name = "email")
    private int gender;


    public UserMyDataList(@NonNull String name,String age,int gender) {
        this.name = name;
        this.age = age;
        this.gender = gender;
    }
    public void UserMyDataList2(@NonNull int id, String name,String age,int gender) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getAge() {
        return this.age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public int getGender() {
        return this.gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }


    public UserMyDataList(){
        this.name = "name";
        this.age = "age";
        this.gender = gender;

    }

    @Override
    public String toString() {
        return "zMyDataList{" +
                "id=" + id +
                ", name=" + name +
                ", age='" + age + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }


}