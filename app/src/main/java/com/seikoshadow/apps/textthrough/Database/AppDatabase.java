package com.seikoshadow.apps.textthrough.Database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.seikoshadow.apps.textthrough.Entities.Alert;
import com.seikoshadow.apps.textthrough.constants;

@Database(entities = {Alert.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase appDatabase;
    public abstract AlertModel alertModel();

    // If there's no app database already then create otherwise return the current database
    public static AppDatabase getInstance(Context context) {
        if(appDatabase == null) {
            appDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, constants.APPDATABASENAME)
            .allowMainThreadQueries() //TODO may want to stop allowing mainthreadqueries
            .fallbackToDestructiveMigration() // Definitely remove this
            .build();
        }
        return appDatabase;
    }

    public static void destroyInstance() {
        appDatabase = null;
    }
}
