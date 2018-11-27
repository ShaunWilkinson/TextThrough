package com.seikoshadow.apps.textalerter.Database;

import android.content.Context;

import com.seikoshadow.apps.textalerter.constants;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Alert.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase appDatabase;
    public abstract AlertModel alertModel();

    // If there's no app database already then create otherwise return the current database
    public static AppDatabase getInstance(Context context) {
        if(appDatabase == null) {
            appDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, constants.APPDATABASENAME)
            .allowMainThreadQueries()
            .build();
        }
        return appDatabase;
    }

    public static void destroyInstance() {
        appDatabase = null;
    }
}
