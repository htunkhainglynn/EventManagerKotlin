package com.example.eventmanager.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.eventmanager.BuildConfig
import com.example.eventmanager.data.EventManagerDatabase
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class CloudUploadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!hasInternetConnection()) return@withContext Result.retry()

        val database = EventManagerDatabase.getInstance(applicationContext)
        val events = database.eventDao().getAllEvents()
        val sessions = database.sessionDao().getAllSessions()
        if (events.isEmpty()) {
            return@withContext Result.success(output("No events to upload"))
        }

        val payload = JSONObject().apply {
            put("uploadedAt", System.currentTimeMillis())
            put("events", JSONArray().apply {
                events.forEach { event ->
                    put(JSONObject().apply {
                        put("id", event.id)
                        put("name", event.name)
                        put("date", event.date)
                        put("organiser", event.organiser)
                        put("location", event.location)
                        put("description", event.description)
                        put("contactEmail", event.contactEmail)
                        put("contactPhone", event.contactPhone)
                        put("updatedAt", event.updatedAt)
                        put("sessions", JSONArray().apply {
                            sessions.filter { it.eventId == event.id }.forEach { session ->
                                put(JSONObject().apply {
                                    put("id", session.id)
                                    put("name", session.name)
                                    put("time", session.time)
                                    put("speaker", session.speaker)
                                    put("room", session.room)
                                    put("type", session.type)
                                    put("description", session.description)
                                    put("updatedAt", session.updatedAt)
                                })
                            }
                        })
                    })
                }
            })
        }

        runCatching { postJson(BuildConfig.UPLOAD_ENDPOINT, payload.toString()) }
            .fold(
                onSuccess = { statusCode ->
                    if (statusCode in 200..299) {
                        database.eventDao().markSynced(events.map { it.id }, System.currentTimeMillis())
                        Result.success(output("Uploaded ${events.size} event(s)"))
                    } else if (statusCode in 500..599) {
                        Result.retry()
                    } else {
                        Result.failure(output("Upload rejected (HTTP $statusCode)"))
                    }
                },
                onFailure = { Result.retry() }
            )
    }

    private fun hasInternetConnection(): Boolean {
        val manager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun postJson(endpoint: String, json: String): Int {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
        }
        return try {
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(json) }
            connection.responseCode
        } finally {
            connection.disconnect()
        }
    }

    private fun output(message: String) = androidx.work.workDataOf("message" to message)
}
