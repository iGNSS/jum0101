package com.example.picturemap;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.naver.maps.geometry.LatLng;

import java.util.List;

@Database(version = 2, entities = {MyDataList.class})//Room 데이터베이스 저장소
@TypeConverters({Converters.class})
public abstract class MyDatabase extends RoomDatabase {

    private static MyDatabase INSTANCE;

    public abstract MyDao myDao();

    public static MyDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (MyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),//note_database태그에 데이터 저장
                            MyDatabase.class, "note_database")
                            //if you want create db only in memory, not in file
                            //Room.inMemoryDatabaseBuilder
                            //(context.getApplicationContext(), DataRoomDbase.class)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    public static void destroyInstance() {
        INSTANCE = null;
    }

}
