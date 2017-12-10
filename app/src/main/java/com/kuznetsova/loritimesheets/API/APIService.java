package com.kuznetsova.loritimesheets.API;

import com.kuznetsova.loritimesheets.entity.Token;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface APIService {
    @FormUrlEncoded
    @Headers({"Content-Type: application/x-www-form-urlencoded","Authorization: Basic Y2xpZW50OnNlY3JldA=="})
    @POST("/app/rest/v2/oauth/token")
    Call<Token> signIn(@Field("grant_type") String grantType, @Field("username") String login, @Field("password") String password);
}
