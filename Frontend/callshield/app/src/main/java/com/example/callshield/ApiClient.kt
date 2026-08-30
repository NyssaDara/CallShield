package com.example.callshield

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.Call
import okhttp3.Response
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.File
import java.io.IOException
import okhttp3.RequestBody.Companion.asRequestBody

object ApiClient {

    private val client = OkHttpClient()

    // ⚠️ Update this every time your laptop's IP changes (check cmd: ipconfig)
    private const val BASE_URL = "http://192.168.1.9:5000"

    fun checkPhone(
        phone: String,
        onResult: (isFlagged: Boolean, reportedCount: Int) -> Unit
    ) {
        val request = Request.Builder()
            .url("$BASE_URL/check?phone=$phone")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                onResult(false, 0)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string()
                        ?: return onResult(false, 0)

                    val json = JSONObject(body)

                    if (json.has("message")) {
                        onResult(false, 0)
                    } else {
                        val reported = json.optInt("Reported_count", 0)
                        val blocked = json.optInt("Blocked_count", 0)

                        onResult(
                            reported > 0 || blocked > 0,
                            reported
                        )
                    }
                }
            }
        })
    }

    fun analyzeAudio(
        audioFile: File,
        onResult: (transcript: String, risk: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "audio",
                audioFile.name,
                audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url("$BASE_URL/analyze")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        onError("Server error: ${it.code}")
                        return
                    }

                    val body = it.body?.string() ?: return
                    val json = JSONObject(body)

                    val transcript = json.optString("transcript", "")
                    val risk = json.optString("risk", "")

                    onResult(transcript, risk)
                }
            }
        })
    }
}