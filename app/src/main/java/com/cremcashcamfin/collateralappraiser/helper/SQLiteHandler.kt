package com.cremcashcamfin.collateralappraiser.helper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


/**
 * SQLiteHandler is a helper class to manage local SQLite database creation and version management.
 * It creates and manages a simple "users" table with id, name, and email columns.
 */
class SQLiteHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    /**
     * Called when the database is created for the first time.
     * This method creates the users table.
     */
    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_USERS_TABLE = ("CREATE TABLE $TABLE_USERS ("
                + "$KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT," // Auto-incrementing primary key
                + "$KEY_NAME TEXT,"                             // Name column
                + "$KEY_EMAIL TEXT)")                           // Email column
        db.execSQL(CREATE_USERS_TABLE)
    }

    /**
     * Called when the database needs to be upgraded.
     * Here, we simply drop the old table and create a new one.
     *
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older table if it exists
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        // Create tables again
        onCreate(db)
    }

    /**
     * Deletes all users from the users table.
     * This is useful for logout or reset operations.
     */
    fun deleteUsers() {
        writableDatabase.use { db ->
            db.delete(TABLE_USERS, null, null) // Delete all rows from the users table
        }
    }

    companion object {
        // Database version and name
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "userManager"

        // Table name and column keys
        private const val TABLE_USERS = "users"
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
    }
}