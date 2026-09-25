package org.hbc.absensihbc

import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object ApiClient {
    const val BASE_URL = "https://script.google.com/macros/s/AKfycbxTtd6lAG2_4jhuuWCLaQwlGHpxzTnG9fB-LbypQy1ssaOqouyP63Y4vr9vBh4HfWT5Hw/exec"
    const val API_SECRET = "hbc2026"

    private val client = OkHttpClient()

    // ---------- LOOKUP (GET) ----------
    fun lookup(nim: String, onResult: (JSONObject?) -> Unit) {
        val url = "$BASE_URL?action=lookup&nim=$nim&secret=$API_SECRET"
        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = onResult(null)
            override fun onResponse(call: Call, response: Response) {
                onResult(response.body?.string()?.let { JSONObject(it) })
            }
        })
    }

    // ---------- DAFTAR KEGIATAN (GET) ----------
    fun daftarKegiatan(onResult: (List<String>) -> Unit) {
        val url = "$BASE_URL?action=daftarKegiatan&secret=$API_SECRET"
        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = onResult(emptyList())
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: run { onResult(emptyList()); return }
                val json = try { JSONObject(body) } catch (ex: Exception) { onResult(emptyList()); return }
                val arr = json.optJSONArray("daftar")
                val list = mutableListOf<String>()
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(obj.optString("nama"))
                    }
                }
                onResult(list)
            }
        })
    }

    // ---------- CATAT ABSENSI (POST Form) ----------
    fun catatAbsensi(nim: String, jenisKegiatan: String, status: String, onResult: (JSONObject?) -> Unit) {
        postForm(
            mapOf(
                "secret" to API_SECRET,
                "action" to "absensi",
                "nim" to nim,
                "jenisKegiatan" to jenisKegiatan,
                "status" to status
            ), onResult
        )
    }

    // ---------- CATAT JAM & CP (POST Form) ----------
    fun catatJamCp(nim: String, jenisKegiatan: String, jam: Double, cp: Int, onResult: (JSONObject?) -> Unit) {
        postForm(
            mapOf(
                "secret" to API_SECRET,
                "action" to "jamCp",
                "nim" to nim,
                "jenisKegiatan" to jenisKegiatan,
                "jamMagang" to jam.toString(),
                "cp" to cp.toString()
            ), onResult
        )
    }

    // ---------- HELPER: POST Form ----------
    private fun postForm(params: Map<String, String>, onResult: (JSONObject?) -> Unit) {
        val builder = FormBody.Builder()
        params.forEach { (k, v) -> builder.add(k, v) }
        val request = Request.Builder().url(BASE_URL).post(builder.build()).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = onResult(null)
            override fun onResponse(call: Call, response: Response) {
                val text = response.body?.string()
                val json = try { if (text != null) JSONObject(text) else null } catch (ex: Exception) { null }
                onResult(json)
            }
        })
    }
}