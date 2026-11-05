package com.example.skysyncsa

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class CachedForecast(val location: String, val json: String, val updatedAt: Long)
data class PendingSettings(val location: String, val language: String, val rainAlert: Boolean, val severeAlert: Boolean, val dailyAlert: Boolean)

/** SQLite cache used for the offline-first forecast and saved locations. */
class WeatherStore(context: Context) : SQLiteOpenHelper(context, "skysync.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE weather_cache (id INTEGER PRIMARY KEY, location TEXT NOT NULL, json TEXT NOT NULL, updated_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE saved_locations (name TEXT PRIMARY KEY)")
        db.execSQL("CREATE TABLE pending_settings (id INTEGER PRIMARY KEY, location TEXT NOT NULL, language TEXT NOT NULL, rain_alert INTEGER NOT NULL, severe_alert INTEGER NOT NULL, daily_alert INTEGER NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) db.execSQL("CREATE TABLE pending_settings (id INTEGER PRIMARY KEY, location TEXT NOT NULL, language TEXT NOT NULL, rain_alert INTEGER NOT NULL, severe_alert INTEGER NOT NULL, daily_alert INTEGER NOT NULL)")
    }

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

    /** Keeps the latest settings change safe while the device is offline. */
    fun queueSettings(location: String, language: String, rainAlert: Boolean, severeAlert: Boolean, dailyAlert: Boolean) {
        val values = ContentValues().apply {
            put("id", 1); put("location", location); put("language", language)
            put("rain_alert", if (rainAlert) 1 else 0); put("severe_alert", if (severeAlert) 1 else 0); put("daily_alert", if (dailyAlert) 1 else 0)
        }
        writableDatabase.insertWithOnConflict("pending_settings", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun pendingSettings(): PendingSettings? = readableDatabase.query("pending_settings", null, "id=1", null, null, null, null).use { cursor ->
        if (!cursor.moveToFirst()) null else PendingSettings(
            cursor.getString(cursor.getColumnIndexOrThrow("location")), cursor.getString(cursor.getColumnIndexOrThrow("language")),
            cursor.getInt(cursor.getColumnIndexOrThrow("rain_alert")) == 1, cursor.getInt(cursor.getColumnIndexOrThrow("severe_alert")) == 1,
            cursor.getInt(cursor.getColumnIndexOrThrow("daily_alert")) == 1
        )
    }

    fun clearPendingSettings() { writableDatabase.delete("pending_settings", "id=1", null) }
}
