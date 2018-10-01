package com.seikoshadow.apps.textthrough.Entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.support.annotation.NonNull;

@Entity
public class Alert {
    @PrimaryKey
    private int id;
    @ColumnInfo(name="alert_name")
    private String name;
    @ColumnInfo(name="phone_number")
    private String phoneNumber;
    @ColumnInfo(name="ringtone_uri")
    private String ringtoneUri;
    @ColumnInfo(name="number_of_rings")
    private int numberOfRings;
    @ColumnInfo(name="alert_active")
    private boolean alertActive;
    @ColumnInfo(name="alert_vibrate")
    private boolean alertVibrate;

    public Alert(String name, String phoneNumber, String ringtoneUri, int numberOfRings, boolean alertVibrate) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.ringtoneUri = ringtoneUri;
        this.alertActive = true;
        this.numberOfRings = numberOfRings;
        this.alertVibrate = alertVibrate;
    }

    public Alert(String name, String phoneNumber, Uri ringtoneUri, int numberOfRings, boolean alertVibrate) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.ringtoneUri = String.valueOf(ringtoneUri);
        this.alertActive = true;
        this.numberOfRings = numberOfRings;
        this.alertVibrate = alertVibrate;
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

    public Uri getRingtoneUriActual() {
        return Uri.parse(this.ringtoneUri);
    }

    public String getRingtoneUri() {
        return this.ringtoneUri;
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

    public void setRingtoneUri(Uri ringtoneUri) {
        this.ringtoneUri = String.valueOf(ringtoneUri);
    }

    public void setRingtoneUri(String ringtoneUri) {
        this.ringtoneUri = ringtoneUri;
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
