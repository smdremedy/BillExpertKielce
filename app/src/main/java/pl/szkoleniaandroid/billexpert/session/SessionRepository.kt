package pl.szkoleniaandroid.billexpert.session

import android.content.SharedPreferences

interface SessionRepository {

    fun saveUser(userId: String, token: String)

    fun getToken(): String
    fun getUserId(): String

    fun isLoggedIn(): Boolean
    fun removeUser()
}

class SharedPrefsSessionRepository(private val sharedPreferences: SharedPreferences) : SessionRepository {
    override fun removeUser() {
        sharedPreferences.edit()
            .remove(USER_ID_KEY)
            .remove(TOKEN_KEY)
            .apply()
    }

    override fun saveUser(userId: String, token: String) {
        sharedPreferences.edit()
            .putString(USER_ID_KEY, userId)
            .putString(TOKEN_KEY, token)
            .apply()
    }

    override fun getToken(): String {
        return sharedPreferences.getString(TOKEN_KEY, "")!!
    }

    override fun getUserId(): String {
        return sharedPreferences.getString(USER_ID_KEY, "")!!
    }

    override fun isLoggedIn(): Boolean {
        return getToken().isNotEmpty()
    }

    companion object {
        const val USER_ID_KEY = "userId"
        const val TOKEN_KEY = "token"
    }
}