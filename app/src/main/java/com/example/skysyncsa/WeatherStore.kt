package com.example.skysyncsa

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class CachedForecast(val location: String, val json: String, val updatedAt: Long)

/** SQLite cache used for the offline-first forecast and saved locations. */
class WeatherStore(context: Context) : SQLiteOpenHelper(context, "skysync.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE weather_cache (id INTEGER PRIMARY KEY, location TEXT NOT NULL, json TEXT NOT NULL, updated_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE saved_locations (name TEXT PRIMARY KEY)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun cacheForecast(location: String, json: String) {
        val values = ContentValues().apply {
            put("id", 1); put("location", location); put("json", json); put("updated_at", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict("weather_cache", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun cachedForecast(): CachedForecast? = readableDatabase.query("weather_cache", null, "id=1", null, null, null, null).use { cursor ->
        if (!cursor.moveToFirst()) null else CachedForecast(cursor.getString(cursor.getColumnIndexOrThrow("location")), cursor.getString(cursor.getColumnIndexOrThrow("json")), cursor.getLong(cursor.getColumnIndexOrThrow("updated_at")))
    }

    fun saveLocation(name: String) {
        if (name.isBlank()) return
        val values = ContentValues().apply { put("name", name.trim()) }
        writableDatabase.insertWithOnConflict("saved_locations", null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun savedLocations(): List<String> = readableDatabase.query("saved_locations", arrayOf("name"), null, null, null, null, "name ASC").use { cursor ->
        buildList { while (cursor.moveToNext()) add(cursor.getString(0)) }
    }
}
