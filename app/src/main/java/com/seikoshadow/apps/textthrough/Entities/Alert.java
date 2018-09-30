package com.seikoshadow.apps.textthrough.Entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Alert {
    @PrimaryKey
    private int id;
    @ColumnInfo(name="alert_name")
    private String name;
    @ColumnInfo(name="phone_number")
    private String phoneNumber;
    @ColumnInfo(name="number_of_rings")
    private int numberOfRings;
    @ColumnInfo(name="alert_active")
    private boolean alertActive;
    @ColumnInfo(name="alert_vibrate")
    private boolean alertVibrate;

    public Alert(String name, String phoneNumber) {
        this.id = 0; //TODO make this dynamic
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.numberOfRings = 1;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public int getNumberOfRings() {
        return this.numberOfRings;
    }

    public boolean isAlertActive() {
        return this.alertActive;
    }

    public boolean isAlertVibrate() {
        return alertVibrate;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setNumberOfRings(int numberOfRings) {
        this.numberOfRings = numberOfRings;
    }

    public void setAlertActive(boolean alertActive) {
        this.alertActive = alertActive;
    }

    public void setAlertVibrate(boolean alertVibrate) {
        this.alertVibrate = alertVibrate;
    }
}
