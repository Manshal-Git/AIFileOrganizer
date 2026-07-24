package com.manshal79.aifileorganizer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform