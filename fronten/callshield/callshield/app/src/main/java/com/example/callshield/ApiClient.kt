package com.example.callshield

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

object ApiClient {

    private val client = OkHttpClient()

    // YOUR PC's IPv4 address
    private const val BASE_URL = "http://192.168.1.42:5000"

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
                e.printStackTrace()
                onResult(false, 0)
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {

                response.use {

                    if (!response.isSuccessful) {
                        onResult(false, 0)
                        return
                    }

                    val body = response.body?.string()

                    if (body.isNullOrEmpty()) {
                        onResult(false, 0)
                        return
                    }

                    try {

                        val json = JSONObject(body)

                        // Number isn't present in database
                        if (json.has("message")) {

                            onResult(false, 0)

                        } else {

                            val reported =
                                json.optInt("Reported_count", 0)

                            val blocked =
                                json.optInt("Blocked_count", 0)

                            val isFlagged =
                                reported > 0 || blocked > 0

                            onResult(
                                isFlagged,
                                reported
                            )
                        }

                    } catch (e: Exception) {

                        e.printStackTrace()
                        onResult(false, 0)
                    }
                }
            }
        })
    }
}