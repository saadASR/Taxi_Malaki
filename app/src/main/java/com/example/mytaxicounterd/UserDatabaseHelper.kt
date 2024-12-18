package com.example.mytaxicounterd

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class UserDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_USERS_TABLE = ("CREATE TABLE $TABLE_USERS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_USERNAME TEXT," +
                "$COLUMN_PASSWORD TEXT," +
                "$COLUMN_EMAIL TEXT" +
                ")")

        val CREATE_TRAFFIC_TABLE = ("CREATE TABLE $TABLE_TRAFFIC (" +
                "$COLUMN_TRAFFIC_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_DISTANCE REAL," +
                "$COLUMN_TIME INTEGER," +
                "$COLUMN_FARE REAL," +
                "$COLUMN_TIMESTAMP TEXT" +
                ")")

        db.execSQL(CREATE_USERS_TABLE)
        db.execSQL(CREATE_TRAFFIC_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRAFFIC")
        onCreate(db)
    }

    fun saveTraffic(distance: Double, time: Long, fare: Double): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DISTANCE, distance)
            put(COLUMN_TIME, time)
            put(COLUMN_FARE, fare)
            put(COLUMN_TIMESTAMP, System.currentTimeMillis().toString())
        }
        val result = db.insert(TABLE_TRAFFIC, null, values)
        db.close()
        return result != -1L
    }

    fun getTrafficHistory(): List<Traffic> {
        val trafficList = mutableListOf<Traffic>()
        val db = readableDatabase
        val cursor = db.query(TABLE_TRAFFIC, null, null, null, null, null, "$COLUMN_TIMESTAMP DESC")

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRAFFIC_ID))
                val distance = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE))
                val time = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIME))
                val fare = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FARE))
                val timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))

                trafficList.add(Traffic(id, distance, time, fare, timestamp))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return trafficList
    }

    fun addUser(username: String, password: String, email: String): Boolean {
        Log.d("UserDatabaseHelper", "Adding user with email: $email")
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_EMAIL, email)
        }
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    fun checkUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(email, password),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun getUserEmail(email: String): String? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_EMAIL),
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )
        if (cursor != null && cursor.moveToFirst()) {
            Log.d("UserDatabaseHelper", "Cursor columns: ${cursor.columnNames.joinToString()}")
            val columnIndex = cursor.getColumnIndex(COLUMN_EMAIL)
            if (columnIndex != -1) {
                return cursor.getString(columnIndex)
            } else {
                Log.e("UserDatabaseHelper", "Column index for COLUMN_EMAIL is -1")
            }
        } else {
            Log.e("UserDatabaseHelper", "Cursor is null or empty")
        }
        cursor.close()
        db.close()
        return null
    }

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "mytaxicounterd.db"
        private const val TABLE_USERS = "users"
        private const val TABLE_TRAFFIC = "traffic"

        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_EMAIL = "email"

        private const val COLUMN_TRAFFIC_ID = "traffic_id"
        private const val COLUMN_DISTANCE = "distance"
        private const val COLUMN_TIME = "time"
        private const val COLUMN_FARE = "fare"
        private const val COLUMN_TIMESTAMP = "timestamp"
    }
}
