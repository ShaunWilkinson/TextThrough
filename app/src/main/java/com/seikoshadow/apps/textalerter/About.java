package com.seikoshadow.apps.textalerter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.mikhaellopez.circularimageview.CircularImageView;

import androidx.appcompat.app.AppCompatActivity;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        CircularImageView profileImgView = findViewById(R.id.profileImage);

        profileImgView.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.linkedInURL)));
            startActivity(browserIntent);
        });
    }
}
