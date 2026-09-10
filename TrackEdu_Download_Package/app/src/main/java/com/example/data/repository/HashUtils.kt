package com.example.data.repository

import java.security.MessageDigest

object HashUtils {
    /**
     * Hashes plaintext password using SHA-256 with a salt for local storage security.
     */
    fun hashPassword(password: String): String {
        val salt = "trackedu_salt_2026_"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest((salt + password).toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Compares provided password against stored hash.
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        return hashPassword(password) == storedHash
    }
}
