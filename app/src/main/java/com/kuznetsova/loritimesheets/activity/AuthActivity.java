package com.kuznetsova.loritimesheets.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kuznetsova.loritimesheets.API.APIFactory;
import com.kuznetsova.loritimesheets.API.APIService;
import com.kuznetsova.loritimesheets.R;
import com.kuznetsova.loritimesheets.Utils;
import com.kuznetsova.loritimesheets.entity.User;
import com.kuznetsova.loritimesheets.entity.Token;

import java.io.Serializable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthActivity extends AppCompatActivity {

    private static final String TOKEN_PREFERENCE = "TOKEN_PREFERENCE";
    private static final String TOKEN_KEY = "TOKEN_KEY";
    private static final String USER_PREFERENCE = "USER_PREFERENCE";
    private static final String USER_ID = "USER_ID";

    private EditText etLogin;
    private EditText etPassword;
    private ProgressDialog progressDialog;
    private APIService service;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        getSupportActionBar().setTitle(getString(R.string.sign_in_title));

        etLogin = (EditText) findViewById(R.id.et_login);
        etPassword = (EditText) findViewById(R.id.et_password);

        progressDialog = new ProgressDialog(this);
    }

    public void onBtnSignInClick(View view) {
        if (validateLogPass()) {
            if (Utils.isNetworkAvailable(this)) {
                service = APIFactory.getAPIService();
                progressDialog.setMessage(getString(R.string.login_to_system));
                progressDialog.show();
                Call<Token> call = service.signIn("password", etLogin.getText().toString(), etPassword.getText().toString());
                call.enqueue(new Callback<Token>() {
                    @Override
                    public void onResponse(Call<Token> call, Response<Token> response) {
                        if (response.isSuccessful()) {
                            getSharedPreferences(TOKEN_PREFERENCE, Context.MODE_PRIVATE).edit().putString(TOKEN_KEY, response.body().getToken()).apply();
                            final Call<User> userCall = service.getUser("Bearer " + getSharedPreferences(TOKEN_PREFERENCE, Context.MODE_PRIVATE).getString(TOKEN_KEY, null));
                            userCall.enqueue(new Callback<User>() {
                                @Override
                                public void onResponse(Call<User> call, Response<User> response) {
                                    if (response.isSuccessful()) {
                                        user = response.body();
                                        getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE).edit().putString(USER_ID, user.getId()).apply();
                                        progressDialog.dismiss();
                                        Intent intent = new Intent(AuthActivity.this, WeekTimeEntryActivity.class);
                                        intent.putExtra("user", (Serializable) user);
                                        startActivity(intent);
                                        finish();
                                    }
                                }

                                @Override
                                public void onFailure(Call<User> call, Throwable t) {
                                    progressDialog.dismiss();
                                    Toast.makeText(AuthActivity.this, R.string.network_failed, Toast.LENGTH_SHORT).show();
                                }
                            });


                        } else if (response.errorBody() != null) {
                            int code = response.code();
                            if (code >= 400 && code <= 451) {
                                Toast.makeText(AuthActivity.this, getString(R.string.error_invalid_login_or_password), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Token> call, Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(AuthActivity.this, getString(R.string.network_failed), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(AuthActivity.this, getString(R.string.network_failed), Toast.LENGTH_SHORT).show();
            }
        }

    }

    private boolean validateLogPass() {
        boolean valid = true;
        if (TextUtils.isEmpty(etLogin.getText().toString())||TextUtils.isEmpty(etPassword.getText().toString())) {
            Toast.makeText(this,getString(R.string.error_invalid_login_or_password),Toast.LENGTH_SHORT).show();
            valid = false;
        }
        return valid;
    }
}
