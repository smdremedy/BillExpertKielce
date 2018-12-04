package pl.szkoleniaandroid.billexpert.api

import retrofit2.Call
import retrofit2.http.*
import java.io.Serializable
import java.util.*

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

    @Headers(
        "X-Parse-Application-Id: RRQfzogXeuQI2VzK0bqEgn02IElfm3ifCUf1lNQX",
        "X-Parse-REST-API-Key: mt4btJUcnmVaEJGzncHqkogm0lDM3n2185UNSjiX"
    )
    @GET("classes/Bill")
    fun getBills(@Header("X-Parse-Session-Token") token: String): Call<BillsResponse>


    @Headers(
        "X-Parse-Application-Id: RRQfzogXeuQI2VzK0bqEgn02IElfm3ifCUf1lNQX",
        "X-Parse-REST-API-Key: mt4btJUcnmVaEJGzncHqkogm0lDM3n2185UNSjiX"
    )
    @POST("classes/Bill")
    fun postBill(@Header("X-Parse-Session-Token") token: String, @Body bill: Bill) : Call<PostBillResponse>
}

class PostBillResponse(
    val objectId: String
) {

}

data class BillsResponse(
    val results: List<Bill>
)

enum class Category {
    OTHER,
    BILLS,
    CAR,
    CHEMISTRY,
    CLOTHES,
    COSMETICS,
    ELECTRONICS,
    ENTERTAINMENT,
    FOOD,
    FURNITURE,
    GROCERIES,
    HEALTH,
    SHOES,
    SPORT,
    TOYS,
    TRAVEL
}

data class Bill(
    val userId: String,
    val date: Date = Date(),
    val name: String = "",
    val amount: Double = 0.0,
    val category: Category = Category.OTHER,
    val comment: String = "",
    val objectId: String = ""
) : Serializable {
    fun getCategoryUrl()  = "file:///android_asset/${category.name.toLowerCase()}.png"
}

data class LoginResponse(
    val objectId: String,
    val username: String,
    val sessionToken: String
)
