package com.fleet.ecocar.browser

object BrowserHome {
    const val DEFAULT_URL: String = "https://www.car-solaire.org"

    /** Blank input uses [fallback]. A value without a scheme gets https://. */
    fun resolve(raw: String?, fallback: String = DEFAULT_URL): String {
        val trimmed = raw?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            return fallback.trim().ifEmpty { DEFAULT_URL }
        }
        if (trimmed.contains("://")) return trimmed
        return "https://$trimmed"
    }
}
