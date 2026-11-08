package com.fleet.ecocar.infrastructure.navigation

import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

/** GET with the named timeouts. Does not throw for timeout or a dead host. */
class UrlConnectionValhallaHttp : ValhallaHttp {
    override fun get(url: String): ValhallaHttpResult {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
        }
        return try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            ValhallaHttpResult.Response(code, body)
        } catch (_: SocketTimeoutException) {
            ValhallaHttpResult.Timeout
        } catch (_: IOException) {
            ValhallaHttpResult.Unreachable
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val CONNECT_TIMEOUT_MS = 8_000
        const val READ_TIMEOUT_MS = 15_000
    }
}
