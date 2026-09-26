package com.example.repository

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * NexusGGR / FiversCan Casino API Aggregator Client for Android.
 * Faithfully mirrors the FiversCan Java API integration (Index.java from https://github.com/casino-api007/FiversCan.git).
 *
 * Methods supported:
 * - provider_list
 * - game_list
 * - user_create
 * - user_deposit
 * - user_withdraw
 * - money_info
 * - game_launch
 */
class FiversCanClient(
    private val apiUrl: String = "https://api.example.com",
    private val agentCode: String = "your_agent_code",
    private val agentToken: String = "your_agent_token"
) {
    companion object {
        private const val TAG = "FiversCanClient"
    }

    class FiversCanException(val method: String, val msg: String, val detail: String?) :
        RuntimeException("$method failed: $msg" + if (detail != null) " ($detail)" else "")

    fun call(method: String, params: Map<String, Any?>): JSONObject {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(apiUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 15000
                doOutput = true
                doInput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
            }

            val body = JSONObject().apply {
                put("method", method)
                put("agent_code", agentCode)
                put("agent_token", agentToken)
                params.forEach { (k, v) -> put(k, v) }
            }

            OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { writer ->
                writer.write(body.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }

            val responseStr = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                val sb = StringBuilder()
                var line: String? = reader.readLine()
                while (line != null) {
                    sb.append(line)
                    line = reader.readLine()
                }
                sb.toString()
            }

            if (responseCode !in 200..299) {
                throw IOException("$method HTTP error: $responseCode - $responseStr")
            }

            val data = JSONObject(responseStr)
            if (data.optInt("status", 0) != 1) {
                throw FiversCanException(method, data.optString("msg", "unknown"), data.optString("detail", null))
            }
            return data
        } catch (e: Exception) {
            Log.e(TAG, "FiversCan API call $method failed: ${e.message}", e)
            throw e
        } finally {
            connection?.disconnect()
        }
    }

    fun providerList(): JSONObject = call("provider_list", emptyMap())

    fun gameList(providerCode: String): JSONObject = call("game_list", mapOf("provider_code" to providerCode))

    fun userCreate(userCode: String): JSONObject = call("user_create", mapOf("user_code" to userCode))

    fun userDeposit(userCode: String, amount: Double, agentSign: String? = null): JSONObject {
        val params = mutableMapOf<String, Any?>("user_code" to userCode, "amount" to amount)
        if (!agentSign.isNullOrEmpty()) params["agent_sign"] = agentSign
        return call("user_deposit", params)
    }

    fun userWithdraw(userCode: String, amount: Double, agentSign: String? = null): JSONObject {
        val params = mutableMapOf<String, Any?>("user_code" to userCode, "amount" to amount)
        if (!agentSign.isNullOrEmpty()) params["agent_sign"] = agentSign
        return call("user_withdraw", params)
    }

    fun moneyInfo(userCode: String? = null): JSONObject {
        val params = if (userCode.isNullOrEmpty()) emptyMap() else mapOf("user_code" to userCode)
        return call("money_info", params)
    }

    fun gameLaunch(
        userCode: String,
        providerCode: String,
        gameCode: String?,
        lang: String = "en",
        lobbyUrl: String? = null,
        rtp: Double? = null
    ): JSONObject {
        val params = mutableMapOf<String, Any?>(
            "user_code" to userCode,
            "provider_code" to providerCode,
            "game_code" to (gameCode ?: ""),
            "lang" to lang
        )
        if (!lobbyUrl.isNullOrEmpty()) params["lobby_url"] = lobbyUrl
        if (rtp != null) params["rtp"] = rtp
        return call("game_launch", params)
    }
}
