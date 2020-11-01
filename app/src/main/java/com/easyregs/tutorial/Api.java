package com.easyregs.tutorial;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface Api {
    @POST("CustomerLoginTrackAPI")
    Call<Information> postURLInfo(@Body JsonObject ussdObject);
}
