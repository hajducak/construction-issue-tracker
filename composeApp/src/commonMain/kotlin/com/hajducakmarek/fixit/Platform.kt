package com.hajducakmarek.fixit

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform