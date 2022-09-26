package com.nineone.zntil;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName="shoolnamelista")
public class ShoolnameList {
    /* renamed from: id */

    @PrimaryKey(autoGenerate = true)//기본키를 id로 설정
    private int id;
    @ColumnInfo(name="SHOOLNAME")
    public String shoolname;
    public void ShoolnameList(@NonNull String name) {
        this.shoolname = name;

    }
    public void ShoolnameList2( int id,@NonNull String name) {
        this.id = id;
        this.shoolname = name;
    }
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getShoolName() { return shoolname; }

    public void setShoolName(String name) { this.shoolname = name; }

    public ShoolnameList(){
        this.shoolname = "name";
    }

    @Override
    public String toString() {
        return "zMyDataList{" +

                ", name=" + shoolname +

                '}';
    }

}
