package com.cremcashcamfin.collateralappraiser.helper

import androidx.core.app.RemoteInput
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Utility object for generating hash values.
 */
object HashUtils {
    fun md5(input: String): String {
        // Get MD5 digest instance
        val md = MessageDigest.getInstance("MD5")

        // Compute digest from input string's byte array
        val digest = md.digest(input.toByteArray())

        // Convert the digest byte array into a BigInteger
        val bigInt = BigInteger(1, digest)

        // Convert BigInteger to a 32-character hexadecimal string, padded with zeros and uppercase
        return bigInt.toString(16).padStart(32, '0').uppercase()
    }
}