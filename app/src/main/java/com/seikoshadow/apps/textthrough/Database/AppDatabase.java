package com.seikoshadow.apps.textthrough.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.seikoshadow.apps.textthrough.Entities.Alert;
import com.seikoshadow.apps.textthrough.constants;

@Database(entities = {Alert.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase appDatabase;
    private Context context;
    public abstract AlertDao alertDao();

    // If there's no app database already then create otherwise return the current database
    public static AppDatabase getInstance(Context context) {
        if(appDatabase == null) {
            appDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, constants.APPDATABASENAME)
            .allowMainThreadQueries() //TODO may want to stop allowing mainthreadqueries
            .build();
        }
        return appDatabase;
    }

    public static void destroyInstance() {
        appDatabase = null;
    }
}
