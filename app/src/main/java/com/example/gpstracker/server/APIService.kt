package com.example.gpstracker.server;

import okhttp3.Response;
import retrofit2.Call
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface APIService {

//    @PUT("/parents/update")
//    fun updateParentData(@Body requestData: List<update_ortu_item>): Response<PutOrtu>

    @GET("http://127.0.0.1:8000/parents/")
    fun getData(): Call<get_ortu>
}
