package com.fleet.ecocar.infrastructure.navigation

sealed class ValhallaHttpResult {
    class Response(val code: Int, val body: String) : ValhallaHttpResult()
    data object Timeout : ValhallaHttpResult()
    data object Unreachable : ValhallaHttpResult()
}

fun interface ValhallaHttp {
    fun get(url: String): ValhallaHttpResult
}
