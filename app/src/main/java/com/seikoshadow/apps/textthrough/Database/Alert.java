package com.seikoshadow.apps.textthrough.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import android.net.Uri;

@Entity (indices = {@Index(value = "phone_number", unique = true)})
public class Alert {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name="alert_name")
    private String name;
    @ColumnInfo(name="phone_number")
    private String phoneNumber;
    @ColumnInfo(name="ringtone_uri")
    private String ringtoneUri;
    @ColumnInfo(name="ringtone_name")
    private String ringtoneName;
    @ColumnInfo(name="number_of_rings")
    private int numberOfRings;
    @ColumnInfo(name="alert_active")
    private boolean alertActive;
    @ColumnInfo(name="alert_vibrate")
    private boolean alertVibrate;

    public Alert(String name, String phoneNumber, String ringtoneName, String ringtoneUri, int numberOfRings, boolean alertVibrate) {
        this.name = name;
        this.phoneNumber = phoneNumber.toLowerCase();
        this.ringtoneName = ringtoneName;
        this.ringtoneUri = ringtoneUri;
        this.alertActive = true;
        this.numberOfRings = numberOfRings;
        this.alertVibrate = alertVibrate;
    }

    public Alert(String name, String phoneNumber, String ringtoneName, Uri ringtoneUri, int numberOfRings, boolean alertVibrate) {
        this.name = name;
        this.phoneNumber = phoneNumber.toLowerCase();
        this.ringtoneName = ringtoneName;
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

    public String getRingtoneUri() {
        return this.ringtoneUri;
    }

    public String getRingtoneName() {
        return this.ringtoneName;
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
        this.phoneNumber = phoneNumber.toLowerCase();
    }

    public void setRingtoneUri(Uri ringtoneUri) {
        this.ringtoneUri = String.valueOf(ringtoneUri);
    }

    public void setRingtoneName(String ringtoneName) {
        this.ringtoneName = ringtoneName;
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
