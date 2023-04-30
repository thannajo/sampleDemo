package com.morefun.mpos.reader.sampleDemo.retrofit;

import com.morefun.mpos.reader.sampleDemo.model.RequestUser;
import com.morefun.mpos.reader.sampleDemo.model.Response;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface APIInterface {

    @POST("/api/v1/users/login")
    Call<Response> loginUser(@Body RequestUser requestUser);

    @POST("/")
    Call<String> getString(@Body String body);
}
