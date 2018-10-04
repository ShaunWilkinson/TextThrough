package com.seikoshadow.apps.textthrough.Database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.seikoshadow.apps.textthrough.Entities.Alert;

import java.util.List;

@Dao
public interface AlertDao {
    @Query("SELECT * FROM alert")
    LiveData<List<Alert>> getAll();

    @Query("SELECT * FROM alert WHERE id IN (:alertIds)")
    LiveData<List<Alert>> loadAllByIds(int[] alertIds);

    @Query("SELECT * FROM alert WHERE alert_name LIKE :name LIMIT 1")
    LiveData<Alert> findByName(String name);

    @Query("SELECT phone_number FROM alert")
    LiveData<List<String>> getAllPhoneNumbers();

    @Query("SELECt phone_number FROM alert")
    List<String> getAllPhoneNumbersList();

    @Query("SELECT phone_number FROM alert LIMIT 1")
    String isTherePhoneNumber();

    @Insert
    void insertAll(Alert... alerts);

    @Delete
    void delete(Alert alert);
}
