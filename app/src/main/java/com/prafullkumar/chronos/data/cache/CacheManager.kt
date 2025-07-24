package com.prafullkumar.chronos.data.cache

import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class CacheManager {
    private val cache = ConcurrentHashMap<String, CacheEntry<Any>>()

    private data class CacheEntry<T>(
        val value: T,
        val expiry: Long
    ) {
        fun isExpired() = System.currentTimeMillis() > expiry
    }

    fun <T : Any> put(key: String, value: T, duration: Duration) {
        val expiry = System.currentTimeMillis() + duration.toMillis()
        cache[key] = CacheEntry(value, expiry)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        val entry = cache[key] ?: return null

        return if (entry.isExpired()) {
            cache.remove(key)
            null
        } else {
            entry.value as T
        }
    }

    fun clear(key: String) {
        cache.remove(key)
    }
}