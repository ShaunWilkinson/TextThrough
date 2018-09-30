package com.seikoshadow.apps.textthrough.Entities;

import android.net.Uri;

/**
 * The ringtone entity holds the friendly name and Uri for system ringtones
 */
public class Ringtone {
    private String name;
    private Uri ringtoneUri;

    public Ringtone() {
        this.name = null;
        this.ringtoneUri = null;
    }

    public Ringtone(String name, Uri ringtoneUri) {
        this.name = name;
        this.ringtoneUri = ringtoneUri;
    }

    public String getName() {
        return this.name;
    }

    public Uri getRingtoneUri() {
        return this.ringtoneUri;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRingtoneUri(Uri ringtoneUri) {
        this.ringtoneUri = ringtoneUri;
    }
}
