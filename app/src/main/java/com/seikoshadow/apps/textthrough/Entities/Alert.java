package com.seikoshadow.apps.textthrough.Entities;

public class Alert {
    private int id;
    private String name;
    private String phoneNumber;
    private int numberOfRings;
    private boolean alertActive;
    private boolean alertVibrate;

    public Alert(String name, String phoneNumber) {
        this.id = 0; //TODO make this dynamic
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.numberOfRings = 1;
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
