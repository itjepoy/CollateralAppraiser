package com.cremcashcamfin.collateralappraiser

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import com.cremcashcamfin.collateralappraiser.helper.SQLiteHandler

object SessionManager {
    private const val PREF_NAME = "app_session"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_EMPLOYEE_ID = "employee_id"
    private const val KEY_FULLNAME = "fullname"

    fun setLogin(context: Context, empId: String, fullname: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_EMPLOYEE_ID, empId)
            putString(KEY_FULLNAME, fullname)
        }

    }

    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getFullname(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_FULLNAME, null)
    }

    fun getEmpID(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_EMPLOYEE_ID, null)
    }

    fun logoutUser(context: Context) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit { clear() }

        SQLiteHandler(context).deleteUsers()

        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)

        if (context is Activity) context.finish()
    }
}

