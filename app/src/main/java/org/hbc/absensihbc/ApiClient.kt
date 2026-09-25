package org.hbc.absensihbc

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObjec
import java.io.IOException

object ApiClient {
    // Ganti URL ini kalau nanti deploy ulang Web App dan URL-nya berubah
    const val BASE_URL ="https://script.google.com/macros/s/AKfycbxTtd6lAG2_4jhuuWCLaQwlGHpxzTnG9fB-LbypQy1ssaOqouyP63Y4vr9vBh4HfWT5Hw/exec"
    const val API_SECRET = "hbc2026"

    private val client = OkHttpClient()

    fun lookup(nim: String, onResult: (JSONObject?) -> Unit) {
        val url = "$BASE_URL?action=lookup&nim=$nim&secret=$API_SECRET"
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = onResult(null)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                onResult(if (body != null) JSONObject(body) else null)
            }
        })
    }

    fun daftarKegiatan(onResult: (List<String>) -> Unit) {
        val url = "$BASE_URL?action=daftarKegiatan&secret=$API_SECRET"
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = onResult(emptyList())
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body == null) { onResult(emptyList()); return }
                val json = JSONObject(body)
                val arr = json.optJSONArray("daftar")
                val list = mutableListOf<String>()
                if (arr != null) for (i in 0 until arr.length()) list.add(arr.getString(i))
                onResult(list)
            }
        })
    }

    fun catatAbsensi(nim: String, jenisKegiatan: String, status: String, onResult: (JSONObject?) -> Unit) {
        val json = JSONObject()
        json.put("secret", API_SECRET)
        json.put("action", "absensi")
        json.put("nim", nim)
        json.put("jenisKegiatan", jenisKegiatan)
        json.put("status", status)
        postJson(json, onResult)
    }

    fun catatJamCp(nim: String, jenisKegiatan: String, jam: Double, cp: Int, onResult: (JSONObject?) -> Unit) {
        val json = JSONObject()
        json.put("secret", API_SECRET)
        json.put("action", "jamCp")
        json.put("nim", nim)
        json.put("jenisKegiatan", jenisKegiatan)
        json.put("jamMagang", jam)
        json.put("cp", cp)
        postJson(json, onResult)
    }

    private fun postJson(json: JSONObject, onResult: (JSONObject?) -> Unit) {
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder().url(BASE_URL).post(body).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = onResult(null)
            override fun onResponse(call: Call, response: Response) {
                val respBody = response.body?.string()
                onResult(if (respBody != null) JSONObject(respBody) else null)
            }
        })
    }
}
