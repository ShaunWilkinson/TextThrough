package com.seikoshadow.apps.textthrough.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.seikoshadow.apps.textthrough.Entities.Alert;

@Database(entities = {Alert.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AlertDao alertDao();
}
