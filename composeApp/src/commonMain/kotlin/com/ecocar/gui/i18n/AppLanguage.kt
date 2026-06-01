package com.ecocar.gui.i18n

enum class AppLanguage(val code: String, val bcp47: String) {
    DE("de", "de"),
    EN("en", "en"),
    FR("fr", "fr"),
    WO("wo", "wo"),
    ;

    companion object {
        fun fromCode(code: String): AppLanguage = entries.find { it.code == code } ?: DE
    }
}
