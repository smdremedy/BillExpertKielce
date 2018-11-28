package pl.szkoleniaandroid.billexpert.api

import retrofit2.Call
import retrofit2.http.*

interface BillApi {

    @Headers(
        "X-Parse-Application-Id: RRQfzogXeuQI2VzK0bqEgn02IElfm3ifCUf1lNQX",
        "X-Parse-REST-API-Key: mt4btJUcnmVaEJGzncHqkogm0lDM3n2185UNSjiX",
        "X-Parse-Revocable-Session: 1"
    )
    @GET("login")
    fun getLogin(
        @Query("username") username: String,
        @Query("password") password: String
    ): Call<LoginResponse>
}

class LoginResponse(
    val sessionToken: String
)
