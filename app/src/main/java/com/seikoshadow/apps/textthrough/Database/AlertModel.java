package com.seikoshadow.apps.textthrough.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AlertModel {
    @Query("SELECT * FROM alert")
    LiveData<List<Alert>> getAll();

    @Query("SELECT * FROM alert WHERE id IN (:alertIds)")
    LiveData<List<Alert>> loadAllByIds(int[] alertIds);

    @Query("SELECT * FROM alert WHERE id LIKE :id LIMIT 1")
    Alert findById(int id);

    @Query("SELECT * FROM alert WHERE alert_name LIKE :name LIMIT 1")
    LiveData<Alert> findByName(String name);

    @Query("SELECT * FROM alert WHERE phone_number LIKE :phoneNumber")
    Alert findByPhoneNumber(String phoneNumber);

    @Query("SELECT phone_number FROM alert")
    List<String> getAllPhoneNumbers();

    @Query("SELECT phone_number FROM alert LIMIT 1")
    String isTherePhoneNumber();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Alert... alerts);

    @Delete
    void delete(Alert alert);
}
