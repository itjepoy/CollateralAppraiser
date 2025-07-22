package com.cremcashcamfin.collateralappraiser.helper

import androidx.core.app.RemoteInput
import java.math.BigInteger
import java.security.MessageDigest

object HashUtils {
    fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        val bigInt = BigInteger(1, digest)
        return bigInt.toString(16).padStart(32, '0').uppercase()
    }
}