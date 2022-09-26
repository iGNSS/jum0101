package com.nineone.zntil;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(version = 1, entities = {ShoolnameList.class},exportSchema = false)//Room 데이터베이스 저장소
public abstract class ShoolnameDatabase extends RoomDatabase {
    private static ShoolnameDatabase INSTANCE;

    public abstract ShoolnameDao myDao();

    public static ShoolnameDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (ShoolnameDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),//note_database태그에 데이터 저장
                            ShoolnameDatabase.class, "Shool_name_data")
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
