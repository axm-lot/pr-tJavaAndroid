package com.example.emptya;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.DELETE;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @POST("pret")
    Call<JsonObject> saveLoan(@Body JsonObject loanData);
    @DELETE("pret/{id}")
    Call<JsonObject> deleteLoan(@Path("id") String id);

    @PUT("pret/{id}")
    Call<JsonObject> updateLoan(
            @Path("id") String id,
            @Body JsonObject updatedLoan
    );
}
