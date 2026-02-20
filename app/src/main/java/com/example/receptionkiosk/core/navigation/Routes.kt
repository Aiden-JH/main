package com.example.receptionkiosk.core.navigation

object Routes {
    const val Home = "home"
    const val Form = "form/{purposeId}"
    const val Success = "success"
    const val AdminPin = "admin-pin"
    const val Settings = "settings"
    const val Status = "status"

    fun form(purposeId: Long): String = "form/$purposeId"
}
