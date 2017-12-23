package com.kuznetsova.loritimesheets.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.kuznetsova.loritimesheets.API.APIFactory;
import com.kuznetsova.loritimesheets.API.APIService;
import com.kuznetsova.loritimesheets.R;
import com.kuznetsova.loritimesheets.Utils;
import com.kuznetsova.loritimesheets.entity.User;

import java.io.Serializable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TOKEN_PREFERENCE = "TOKEN_PREFERENCE";
    private static final String TOKEN_KEY = "TOKEN_KEY";
    private static final String USER_PREFERENCE = "USER_PREFERENCE";
    private static final String USER_ID = "USER_ID";

    private APIService service;
    private String token;
    public User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        token = getSharedPreferences(TOKEN_PREFERENCE, Context.MODE_PRIVATE).getString(TOKEN_KEY, null);
        if (token!= null) {
            if(Utils.isNetworkAvailable(this)){
                service = APIFactory.getAPIService();
                final Call<User> userCall = service.getUser("Bearer " + token);
                userCall.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            user = response.body();
                            Intent intent = new Intent(SplashScreenActivity.this, WeekTimeEntryActivity.class);
                            getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE).edit().putString(USER_ID, user.getId()).apply();
                            intent.putExtra("user",(Serializable)user);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Intent intent = new Intent(SplashScreenActivity.this, AuthActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(SplashScreenActivity.this, R.string.network_failed, Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 3000);
                    }
                });
            }
            else {
                user = new User(getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE).getString(USER_ID, null));
                Intent intent = new Intent(SplashScreenActivity.this, WeekTimeEntryActivity.class);
                intent.putExtra("user",(Serializable)user);
                startActivity(intent);
                finish();
            }

        }
        else {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
