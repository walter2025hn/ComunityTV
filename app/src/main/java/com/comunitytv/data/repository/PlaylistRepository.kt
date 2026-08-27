package com.comunitytv.data.repository

import com.comunitytv.data.models.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.BufferedReader
import java.io.StringReader
import java.net.URLEncoder

class PlaylistRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    // --- PARSER M3U (desde URL) ---
    suspend fun fetchM3U(url: String): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            parseM3UContent(body)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseM3UContent(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val reader = BufferedReader(StringReader(content))
        var line: String?
        var currentExtInf: String? = null

        while (reader.readLine().also { line = it } != null) {
            val current = line!!
            when {
                current.startsWith("#EXTINF:") -> {
                    currentExtInf = current
                }
                current.startsWith("#") -> {
                    // Ignorar otros comentarios
                }
                current.isNotBlank() && !current.startsWith("#") -> {
                    // Es la URL del stream
                    val url = current.trim()
                    if (currentExtInf != null) {
                        val channel = parseExtInf(currentExtInf, url)
                        if (channel != null) channels.add(channel)
                        currentExtInf = null
                    }
                }
            }
        }
        return channels
    }

    private fun parseExtInf(extinf: String, url: String): Channel? {
        // Formato: #EXTINF:-1 tvg-logo="logo.png" group-title="Deportes",Nombre Canal
        return try {
            var name = "Canal"
            var logo = ""
            var group = "General"

            // Extraer grupo
            val groupMatch = Regex("group-title=\"([^\"]*)\"").find(extinf)
            group = groupMatch?.groupValues?.get(1) ?: "General"

            // Extraer logo
            val logoMatch = Regex("tvg-logo=\"([^\"]*)\"").find(extinf)
            logo = logoMatch?.groupValues?.get(1) ?: ""

            // Extraer nombre (después de la última coma)
            val lastComma = extinf.lastIndexOf(',')
            if (lastComma != -1 && lastComma < extinf.length - 1) {
                name = extinf.substring(lastComma + 1).trim()
            }

            Channel(
                id = url.hashCode().toString(),
                name = name.ifEmpty { "Canal" },
                group = group,
                url = url,
                logo = logo
            )
        } catch (e: Exception) {
            null
        }
    }

    // --- CLIENTE XTREAM CODES (API) ---
    suspend fun fetchXtream(server: String, user: String, pass: String): List<Channel> =
        withContext(Dispatchers.IO) {
            try {
                val baseUrl = if (server.endsWith("/")) server else "$server/"
                val apiUrl = "${baseUrl}player_api.php?username=$user&password=$pass"

                val request = Request.Builder().url(apiUrl).build()
                val response = client.newCall(request).execute()
                val jsonString = response.body?.string() ?: return@withContext emptyList()

                val json = JSONObject(jsonString)
                val channelsArray = json.getJSONArray("channels")
                val result = mutableListOf<Channel>()

                for (i in 0 until channelsArray.length()) {
                    val obj = channelsArray.getJSONObject(i)
                    val name = obj.getString("name")
                    val streamId = obj.getString("stream_id")
                    val group = obj.getString("category_name")
                    val logo = obj.getString("stream_icon")
                    // Construir URL de stream para Xtream
                    val streamUrl = "${baseUrl}live/$user/$pass/$streamId.m3u8"

                    result.add(
                        Channel(
                            id = streamId,
                            name = name,
                            group = group,
                            url = streamUrl,
                            logo = logo
                        )
                    )
                }
                result
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
}
