package com.seikoshadow.apps.textthrough.Database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.seikoshadow.apps.textthrough.Entities.Alert;

import java.util.List;

@Dao
public interface AlertDao {
    @Query("SELECT * FROM alert")
    List<Alert> getAll();

    @Query("SELECT * FROM alert WHERE id IN (:alertIds)")
    List<Alert> loadAllByIds(int[] alertIds);

    @Query("SELECT * FROM alert WHERE alert_name LIKE :name LIMIT 1")
    Alert findByName(String name);

    @Query("SELECT phone_number FROM alert")
    List<String> getAllPhoneNumbers();

    @Insert
    void insertAll(Alert... alerts);

    @Delete
    void delete(Alert alert);
}
