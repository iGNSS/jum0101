package com.nineone.ver;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(version = 2, entities = {UserMyDataList.class})//Room 데이터베이스 저장소
public abstract class UserMyDatabase extends RoomDatabase {

    private static UserMyDatabase INSTANCE;

    public abstract UserMyDao myDao();

    public static UserMyDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (UserMyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),//note_database태그에 데이터 저장
                            UserMyDatabase.class, "note_data")
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

