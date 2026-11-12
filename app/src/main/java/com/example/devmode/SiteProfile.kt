package com.example.devmode

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Perfil de um site calibrado: seletor de cada papel na interface do chat. */
data class SiteProfile(
    val name: String,
    val url: String,
    val input: String = "",
    val send: String = "",
    val thinking: String = "",
    val response: String = "",
    val stop: String = ""
) {
    fun toJson(): JSONObject = JSONObject()
        .put("name", name)
        .put("url", url)
        .put("input", input)
        .put("send", send)
        .put("thinking", thinking)
        .put("response", response)
        .put("stop", stop)

    companion object {
        fun fromJson(o: JSONObject) = SiteProfile(
            name = o.getString("name"),
            url = o.getString("url"),
            input = o.optString("input"),
            send = o.optString("send"),
            thinking = o.optString("thinking"),
            response = o.optString("response"),
            stop = o.optString("stop")
        )
    }
}

/** Guarda os perfis em um arquivo JSON no armazenamento interno do app. */
class SiteProfileStore(context: Context) {
    private val file = File(context.filesDir, "site_profiles.json")

    fun load(): List<SiteProfile> = runCatching {
        if (!file.exists()) return@runCatching emptyList<SiteProfile>()
        val arr = JSONArray(file.readText())
        List(arr.length()) { SiteProfile.fromJson(arr.getJSONObject(it)) }
    }.getOrDefault(emptyList())

    fun save(profile: SiteProfile) {
        writeAll(load().filter { it.name != profile.name } + profile)
    }

    fun delete(name: String) {
        writeAll(load().filter { it.name != name })
    }

    private fun writeAll(profiles: List<SiteProfile>) {
        val arr = JSONArray()
        profiles.forEach { arr.put(it.toJson()) }
        file.writeText(arr.toString())
    }
}
