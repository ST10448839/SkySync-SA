package com.example.skysyncsa

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

/** SkySync SA Part 2 prototype with live API, SQLite cache, settings and alerts. */
class MainActivity : AppCompatActivity() {
    private val preferences by lazy { getSharedPreferences("skysync_preferences", Context.MODE_PRIVATE) }
    private val store by lazy { WeatherStore(this) }
    private val executor = Executors.newSingleThreadExecutor()
    private val connectivityManager by lazy { getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    private lateinit var content: LinearLayout
    private var currentForecast: ForecastDisplay? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private val black by lazy { color(R.color.sky_black) }
    private val surface by lazy { color(R.color.sky_surface) }
    private val orange by lazy { color(R.color.sky_orange) }
    private val orangeDark by lazy { color(R.color.sky_orange_dark) }
    private val yellow by lazy { color(R.color.sky_yellow) }
    private val primaryTextColor by lazy { color(R.color.sky_text) }
    private val muted by lazy { color(R.color.sky_muted) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 50)
        }
        observeNetwork()
        if (preferences.getBoolean("signed_in", false)) showHome() else showWelcome()
    }

    override fun onDestroy() {
        networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 51 && grantResults.any { it == PackageManager.PERMISSION_GRANTED }) useCurrentLocation()
    }

    private fun observeNetwork() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    if (preferences.getBoolean("signed_in", false)) runOnUiThread { showHome() }
                }
            }
            connectivityManager.registerDefaultNetworkCallback(networkCallback!!)
        }
    }

    private fun showScreen(title: String) {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(black) }
        val scroll = ScrollView(this).apply { isFillViewport = true }
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20.dp, 20.dp, 20.dp, 26.dp); setBackgroundColor(black) }
        content.addView(TextView(this).apply {
            text = title; gravity = Gravity.CENTER; setTextColor(Color.WHITE); textSize = 25f; setTypeface(typeface, Typeface.BOLD); background = rounded(orangeDark, 28f)
        }, LinearLayout.LayoutParams(-1, 105.dp))
        content.addView(space(20)); scroll.addView(content); root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f)); setContentView(root)
    }

    private fun showWelcome() {
        showScreen(t("WELCOME", "WELKOM", "SIYAKWAMUKELA"))
        content.addView(TextView(this).apply { text = "☀  SkySync SA"; setTextColor(yellow); textSize = 27f; gravity = Gravity.CENTER; setTypeface(typeface, Typeface.BOLD) }, LinearLayout.LayoutParams(-1, 58.dp))
        content.addView(label(t("Your weather. In sync.", "Jou weer. Gesinchroniseer.", "Isimo sezulu sakho. Siyavumelana."))); content.addView(space(12))
        val email = input(t("Email address", "E-posadres", "Ikheli le-imeyili"), false)
        val password = input(t("Password", "Wagwoord", "Iphasiwedi"), true)
        val status = label("")
        content.addView(email); content.addView(space(12)); content.addView(password); content.addView(space(16))
        content.addView(button(t("Login / Sign Up", "Teken in / Registreer", "Ngena / Bhalisa")) {
            val enteredEmail = email.text.toString().trim(); val enteredPassword = password.text.toString()
            when {
                !android.util.Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches() -> status.text = t("Enter a valid email address.", "Voer 'n geldige e-posadres in.", "Faka ikheli le-imeyili elivumelekile.")
                enteredPassword.length < 6 -> status.text = t("Password must have at least 6 characters.", "Wagwoord moet minstens 6 karakters hê.", "Iphasiwedi kufanele ibe nezinhlamvu eziyisi-6.")
                preferences.getString("password_hash", null) == null -> {
                    preferences.edit().putString("email", enteredEmail).putString("password_hash", hashPassword(enteredPassword)).putBoolean("signed_in", true).apply(); showHome()
                }
                preferences.getString("email", "") == enteredEmail && preferences.getString("password_hash", "") == hashPassword(enteredPassword) -> {
                    preferences.edit().putBoolean("signed_in", true).apply(); showHome()
                }
                else -> status.text = t("Email or password is incorrect.", "E-pos of wagwoord is verkeerd.", "I-imeyili noma iphasiwedi ayilungile.")
            }
        })
        content.addView(space(10)); content.addView(status); content.addView(space(24))
        content.addView(label(t("New users are registered automatically. Passwords are stored only as SHA-256 hashes.", "Nuwe gebruikers word outomaties geregistreer. Wagwoorde word slegs as SHA-256-hashtes gestoor.", "Abasebenzisi abasha babhaliswa ngokuzenzakalelayo. Amaphasiwedi agcinwa njenge-SHA-256 hash kuphela.")))
    }

    private fun showHome() {
        showScreen(t("HOME", "TUIS", "IKHAYA"))
        val location = preferences.getString("location", "Cape Town") ?: "Cape Town"
        val weatherCard = card().apply { gravity = Gravity.CENTER_HORIZONTAL }
        val city = headline("$location  |  ${t("Loading...", "Laai...", "Kuyalayisha...")}")
        val state = label(t("Connecting to the Open-Meteo REST API", "Koppel aan die Open-Meteo REST API", "Ixhuma ku-Open-Meteo REST API"))
        weatherCard.addView(city); weatherCard.addView(space(9)); weatherCard.addView(state); content.addView(weatherCard); content.addView(space(14))
        val details = label(t("Fetching the latest forecast...", "Laai die jongste voorspelling...", "Ilanda isibikezelo sakamuva..."))
        content.addView(card().apply { addView(headline("🌤  ${t("Current conditions", "Huidige toestande", "Izimo zamanje")}")); addView(space(8)); addView(details) }); content.addView(space(14))
        val forecastPreview = label("")
        content.addView(card().apply { addView(headline("📅 ${t("Next forecast", "Volgende voorspelling", "Isibikezelo esilandelayo")}")); addView(space(8)); addView(forecastPreview) }); content.addView(space(14))
        content.addView(card().apply { addView(headline("✨ ${t("Plan My Day", "Beplan my dag", "Hlela usuku lwami")}")); addView(space(6)); addView(label(t("Use the hourly forecast to plan travel, clothes and outdoor activities.", "Gebruik die uurlikse voorspelling om reis, klere en buitelugaktiwiteite te beplan.", "Sebenzisa isibikezelo samahora ukuhlela uhambo, izingubo nemisebenzi yangaphandle."))) })
        content.addView(space(20)); content.addView(button(t("Check Details", "Sien besonderhede", "Bheka imininingwane")) { showDetails() }); content.addView(space(10))
        content.addView(outlineButton(t("Radar map", "Radar-kaart", "Imephu ye-radar")) { showRadar() }); content.addView(space(10)); content.addView(outlineButton(t("Settings", "Instellings", "Izilungiselelo")) { showSettings() })
        refreshForecast(location, city, details, forecastPreview)
    }

    private fun showDetails() {
        showScreen(t("DETAIL", "BESONDERHEDE", "IMINININGWANE"))
        val forecast = currentForecast
        content.addView(card().apply { addView(headline("⏱ ${t("Hourly forecast", "Uurlikse voorspelling", "Isibikezelo samahora")}")); addView(space(10)); addView(label(forecast?.hourly ?: t("Open Home to load forecast data.", "Maak Tuis oop om data te laai.", "Vula Ikhaya ukuze ulande idatha."))) }); content.addView(space(14))
        content.addView(card().apply { addView(headline("📆 ${t("Seven-day forecast", "Sewe-dae voorspelling", "Isibikezelo sezinsuku eziyisikhombisa")}")); addView(space(10)); addView(label(forecast?.daily ?: "-")) }); content.addView(space(14))
        content.addView(card().apply { addView(headline(t("Offline and sync", "Vanlyn en sinchroniseer", "Ngaphandle kwe-inthanethi nokuvumelanisa"))); addView(space(8)); addView(label(t("The latest successful forecast is stored in SQLite. When a connection returns, SkySync refreshes and replaces the cached data.", "Die jongste suksesvolle voorspelling word in SQLite gestoor. Wanneer die verbinding terugkeer, verfris SkySync die kasdata.", "Isibikezelo sokugcina sigcinwa ku-SQLite. Uma uxhumano lubuya, i-SkySync ivuselela idatha."))) })
        content.addView(space(20)); content.addView(button(t("Return to Home", "Terug na Tuis", "Buyela Ekhaya")) { showHome() }); content.addView(space(10)); content.addView(outlineButton(t("Radar map", "Radar-kaart", "Imephu ye-radar")) { showRadar() })
    }

    private fun showRadar() {
        showScreen(t("RADAR MAP", "RADAR-KAART", "IMEPHU YE-RADAR"))
        content.addView(label(t("Interactive rainfall radar is supplied by RainViewer in a web map.", "Interaktiewe reënradar word deur RainViewer verskaf.", "I-radar yemvula esebenzisanayo ihlinzekwa yi-RainViewer."))); content.addView(space(10))
        val map = WebView(this).apply { settings.javaScriptEnabled = true; webViewClient = WebViewClient(); loadUrl("https://www.rainviewer.com/map.html") }
        content.addView(map, LinearLayout.LayoutParams(-1, 420.dp)); content.addView(space(18)); content.addView(button(t("Return to Home", "Terug na Tuis", "Buyela Ekhaya")) { showHome() })
    }

    private fun showSettings() {
        showScreen(t("SETTINGS", "INSTELLINGS", "IZILUNGISELELO")); content.addView(label(t("Personalise your SkySync experience.", "Pas jou SkySync-ervaring aan.", "Yenza i-SkySync ivumelane nawe."))); content.addView(space(12))
        val languageOptions = arrayOf("English", "Afrikaans", "isiZulu")
        val language = Spinner(this).apply { adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, languageOptions); setSelection(languageOptions.indexOf(preferences.getString("language", "English")).coerceAtLeast(0)); background = rounded(Color.WHITE, 15f) }
        val city = input(t("Default city", "Verstekstad", "Idolobha elizenzakalelayo"), false).apply { setText(preferences.getString("location", "Cape Town")) }
        val rain = check(t("Rain-starting-soon alerts", "Reën-begin-binnekort waarskuwings", "Izaziso zemvula esizoqala"), "rain_alert", true)
        val severe = check(t("Severe-weather alerts", "Ernstige weer waarskuwings", "Izaziso zesimo sezulu esibi"), "severe_alert", true)
        val daily = check(t("Daily morning forecast", "Daaglikse oggend voorspelling", "Isibikezelo sasekuseni sansuku zonke"), "daily_alert", false)
        val saved = label("")
        fun updateSaved() { saved.text = "${t("Saved locations", "Gestoorde liggings", "Izindawo ezigciniwe")}: ${store.savedLocations().ifEmpty { listOf("-") }.joinToString()}" }
        updateSaved()
        content.addView(card().apply {
            addView(headline(t("Language", "Taal", "Ulimi"))); addView(space(8)); addView(language, LinearLayout.LayoutParams(-1, 52.dp)); addView(space(15)); addView(headline(t("Default location", "Verstekligging", "Indawo ezenzakalelayo"))); addView(space(8)); addView(city); addView(space(12)); addView(rain); addView(severe); addView(daily); addView(space(10)); addView(saved)
        })
        content.addView(space(14)); content.addView(outlineButton(t("Save this location", "Stoor hierdie ligging", "Gcina le ndawo")) { store.saveLocation(city.text.toString()); updateSaved() }); content.addView(space(10))
        content.addView(outlineButton(t("Use current location", "Gebruik huidige ligging", "Sebenzisa indawo yamanje")) { useCurrentLocation() }); content.addView(space(18))
        content.addView(button(t("Apply Changes", "Pas veranderinge toe", "Sebenzisa izinguquko")) {
            preferences.edit().putString("language", language.selectedItem.toString()).putString("location", city.text.toString().ifBlank { "Cape Town" }).putBoolean("rain_alert", rain.isChecked).putBoolean("severe_alert", severe.isChecked).putBoolean("daily_alert", daily.isChecked).apply()
            store.saveLocation(city.text.toString()); showHome()
        })
        content.addView(space(10)); content.addView(outlineButton(t("Sign Out", "Teken uit", "Phuma")) { preferences.edit().putBoolean("signed_in", false).apply(); showWelcome() })
    }

    private fun useCurrentLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 51); return
        }
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (location != null) {
            preferences.edit().putString("location", "Current location").putString("latitude", location.latitude.toString()).putString("longitude", location.longitude.toString()).putBoolean("use_current", true).apply(); showHome()
        }
    }

    private fun refreshForecast(location: String, city: TextView, details: TextView, preview: TextView) {
        executor.execute {
            try {
                val coordinates = coordinatesFor(location)
                val endpoint = "https://api.open-meteo.com/v1/forecast?latitude=${coordinates.first}&longitude=${coordinates.second}&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m&hourly=temperature_2m,precipitation_probability,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min&forecast_days=7&timezone=Africa%2FJohannesburg"
                val raw = get(endpoint); store.cacheForecast(location, raw); store.saveLocation(location)
                val display = formatForecast(raw, location, false); currentForecast = display; runOnUiThread { applyForecast(display, city, details, preview) }; sendAlerts(display)
            } catch (_: Exception) {
                val cache = store.cachedForecast(); val display = cache?.let { formatForecast(it.json, it.location, true) } ?: ForecastDisplay(location, "22 C  Partly cloudy", t("Offline: no saved forecast is available yet.", "Vanlyn: geen gestoorde voorspelling beskikbaar nie.", "Ngaphandle kwe-inthanethi: asikho isibikezelo esigciniwe."), "-", "-", false, false)
                currentForecast = display; runOnUiThread { applyForecast(display, city, details, preview) }
            }
        }
    }

    private fun applyForecast(display: ForecastDisplay, city: TextView, details: TextView, preview: TextView) {
        city.text = "${display.location}  |  ${display.current}"; details.text = display.details; preview.text = display.hourly
    }

    private fun coordinatesFor(location: String): Pair<Double, Double> {
        if (preferences.getBoolean("use_current", false)) {
            val latitude = preferences.getString("latitude", null)?.toDoubleOrNull(); val longitude = preferences.getString("longitude", null)?.toDoubleOrNull()
            if (latitude != null && longitude != null) return Pair(latitude, longitude)
        }
        preferences.edit().putBoolean("use_current", false).apply()
        val encoded = URLEncoder.encode(location, "UTF-8")
        val result = JSONObject(get("https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=1&language=en&format=json")).getJSONArray("results").getJSONObject(0)
        return Pair(result.getDouble("latitude"), result.getDouble("longitude"))
    }

    private fun formatForecast(raw: String, location: String, offline: Boolean): ForecastDisplay {
        val json = JSONObject(raw); val current = json.getJSONObject("current"); val hourly = json.getJSONObject("hourly"); val daily = json.getJSONObject("daily")
        val temp = current.getDouble("temperature_2m").toInt(); val code = current.getInt("weather_code"); val humidity = current.getInt("relative_humidity_2m"); val feels = current.getDouble("apparent_temperature").toInt(); val wind = current.getDouble("wind_speed_10m").toInt()
        val times = hourly.getJSONArray("time"); val temperatures = hourly.getJSONArray("temperature_2m"); val rain = hourly.getJSONArray("precipitation_probability"); val hourlyCodes = hourly.getJSONArray("weather_code")
        val hourlyText = StringBuilder(); var rainSoon = false
        for (i in 0 until minOf(5, times.length())) { val chance = rain.getInt(i); if (chance >= 60) rainSoon = true; hourlyText.append(times.getString(i).takeLast(5)).append("  ").append(temperatures.getDouble(i).toInt()).append(" C  ").append(chance).append("%\n") }
        val dayText = StringBuilder(); val max = daily.getJSONArray("temperature_2m_max"); val min = daily.getJSONArray("temperature_2m_min"); val dayCodes = daily.getJSONArray("weather_code"); val dates = daily.getJSONArray("time")
        for (i in 0 until minOf(7, dates.length())) dayText.append(dates.getString(i)).append("  ").append(min.getDouble(i).toInt()).append("-").append(max.getDouble(i).toInt()).append(" C  ").append(weatherName(dayCodes.getInt(i))).append("\n")
        val severe = code >= 95 || (0 until minOf(5, hourlyCodes.length())).any { hourlyCodes.getInt(it) >= 95 }
        val source = if (offline) t("Offline - cached forecast", "Vanlyn - gestoorde voorspelling", "Ngaphandle kwe-inthanethi - isibikezelo esigciniwe") else t("Live data from Open-Meteo REST API", "Lewendige data van Open-Meteo REST API", "Idatha ebukhoma evela ku-Open-Meteo REST API")
        return ForecastDisplay(location, "$temp C  ${weatherName(code)}", "$source\n${t("Feels like", "Voel soos", "Kuzwakala njenge")} $feels C\n${t("Humidity", "Humiditeit", "Umswakama")} $humidity%  |  ${t("Wind", "Wind", "Umoya")} $wind km/h", hourlyText.toString().trim(), dayText.toString().trim(), rainSoon, severe)
    }

    private fun sendAlerts(forecast: ForecastDisplay) {
        val today = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        if (forecast.rainSoon && preferences.getBoolean("rain_alert", true)) postNotification(t("Rain forecast", "Reënvoorspelling", "Isibikezelo semvula"), t("Rain is likely in the next few hours.", "Reën is waarskynlik in die volgende paar uur.", "Imvula kungenzeka emahoreni ambalwa azayo."))
        if (forecast.severe && preferences.getBoolean("severe_alert", true)) postNotification(t("Severe weather", "Ernstige weer", "Isimo sezulu esibi"), t("Thunderstorm conditions are forecast.", "Donderstormtoestande word voorspel.", "Kulindeleke izimo zokuduma kwezulu."))
        if (preferences.getBoolean("daily_alert", false) && preferences.getString("last_daily_alert", "") != today) { preferences.edit().putString("last_daily_alert", today).apply(); postNotification(t("Daily SkySync forecast", "Daaglikse SkySync-voorspelling", "Isibikezelo sansuku zonke se-SkySync"), forecast.current) }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(NotificationChannel("weather_alerts", "SkySync weather alerts", NotificationManager.IMPORTANCE_DEFAULT))
    }

    private fun postNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(this, "weather_alerts") else Notification.Builder(this)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify((System.currentTimeMillis() % 100000).toInt(), builder.setSmallIcon(R.mipmap.ic_launcher).setContentTitle(title).setContentText(message).setAutoCancel(true).build())
    }

    private fun get(address: String): String { val connection = URL(address).openConnection() as HttpURLConnection; connection.connectTimeout = 10000; connection.readTimeout = 10000; return connection.inputStream.bufferedReader().use { it.readText() }.also { connection.disconnect() } }
    private fun weatherName(code: Int): String = when (code) { 0 -> t("Clear", "Helder", "Kucacile"); 1, 2 -> t("Partly cloudy", "Gedeeltelik bewolk", "Kunamafu kancane"); 3 -> t("Cloudy", "Bewolk", "Kunamafu"); 45, 48 -> t("Foggy", "Misperig", "Kunenkwankwane"); in 51..67, in 80..82 -> t("Rain", "Reën", "Imvula"); in 71..77, in 85..86 -> t("Snow", "Sneeu", "Iqhwa"); in 95..99 -> t("Thunderstorm", "Donderstorm", "Ukuduma kwezulu"); else -> t("Cloudy", "Bewolk", "Kunamafu") }
    private fun check(title: String, key: String, default: Boolean) = CheckBox(this).apply { text = title; setTextColor(primaryTextColor); isChecked = preferences.getBoolean(key, default); buttonTintList = android.content.res.ColorStateList.valueOf(orange) }
    private fun card() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(18.dp, 17.dp, 18.dp, 17.dp); background = rounded(surface, 20f) }
    private fun input(hint: String, password: Boolean) = EditText(this).apply { this.hint = hint; setHintTextColor(Color.rgb(105, 105, 105)); setTextColor(Color.rgb(25, 25, 25)); textSize = 16f; setPadding(18.dp, 0, 18.dp, 0); background = rounded(Color.WHITE, 15f); inputType = if (password) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS; layoutParams = LinearLayout.LayoutParams(-1, 58.dp) }
    private fun button(label: String, action: () -> Unit) = Button(this).apply { text = label; setTextColor(Color.WHITE); textSize = 16f; isAllCaps = false; background = rounded(orange, 16f); setOnClickListener { action() }; layoutParams = LinearLayout.LayoutParams(-1, 56.dp) }
    private fun outlineButton(label: String, action: () -> Unit) = Button(this).apply { text = label; setTextColor(orange); textSize = 16f; isAllCaps = false; background = strokeRounded(surface, orange, 16f); setOnClickListener { action() }; layoutParams = LinearLayout.LayoutParams(-1, 54.dp) }
    private fun headline(value: String) = TextView(this).apply { text = value; setTextColor(primaryTextColor); textSize = 19f; setTypeface(typeface, Typeface.BOLD) }
    private fun label(value: String) = TextView(this).apply { text = value; setTextColor(muted); textSize = 14f; setLineSpacing(3f, 1f) }
    private fun space(height: Int) = View(this).apply { layoutParams = LinearLayout.LayoutParams(1, height.dp) }
    private fun rounded(fill: Int, radius: Float) = GradientDrawable().apply { setColor(fill); cornerRadius = radius.dp }
    private fun strokeRounded(fill: Int, stroke: Int, radius: Float) = GradientDrawable().apply { setColor(fill); cornerRadius = radius.dp; setStroke(2.dp, stroke) }
    private fun color(resource: Int) = ContextCompat.getColor(this, resource)
    private fun t(english: String, afrikaans: String = english, isiZulu: String = english): String = when (preferences.getString("language", "English")) { "Afrikaans" -> afrikaans; "isiZulu" -> isiZulu; else -> english }
    private val Int.dp get() = (this * resources.displayMetrics.density).toInt()
    private val Float.dp get() = this * resources.displayMetrics.density
    private fun hashPassword(value: String): String = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}

private data class ForecastDisplay(val location: String, val current: String, val details: String, val hourly: String, val daily: String, val rainSoon: Boolean, val severe: Boolean)
