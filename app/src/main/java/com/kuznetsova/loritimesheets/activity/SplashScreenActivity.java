package com.kuznetsova.loritimesheets.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.kuznetsova.loritimesheets.activity.AuthActivity;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TOKEN_PREFERENCE = "TOKEN_PREFERENCE";
    private static final String TOKEN_KEY = "TOKEN_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent;
        if (getSharedPreferences(TOKEN_PREFERENCE, Context.MODE_PRIVATE).getString(TOKEN_KEY, null)!= null)
            intent = new Intent(this, WeekTimeEntryActivity.class);
        else
            intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
        finish();
    }
}
