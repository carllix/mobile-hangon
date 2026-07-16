package com.example.hangon.data.util

import dev.turingcomplete.kotlinonetimepassword.HmacAlgorithm
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordConfig
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import org.apache.commons.codec.binary.Base32
import java.util.concurrent.TimeUnit

/**
 * Must match `be-hangon/app/codeword/totp.py` exactly: base32 secret, HMAC-SHA1,
 * 60s time step, 6 digits. A mismatch here silently breaks codeword verification.
 */
object TotpGenerator {
    private const val DIGITS = 6
    private const val PERIOD_SECONDS = 60L

    private val config = TimeBasedOneTimePasswordConfig(
        timeStep = PERIOD_SECONDS,
        timeStepUnit = TimeUnit.SECONDS,
        codeDigits = DIGITS,
        hmacAlgorithm = HmacAlgorithm.SHA1
    )

    fun currentCode(base32Secret: String, atEpochSeconds: Long = System.currentTimeMillis() / 1000): String {
        val secretBytes = Base32().decode(base32Secret)
        val generator = TimeBasedOneTimePasswordGenerator(secretBytes, config)
        return generator.generate(timestamp = atEpochSeconds * 1000)
    }

    fun secondsRemainingInWindow(atEpochSeconds: Long = System.currentTimeMillis() / 1000): Int {
        val elapsedInWindow = atEpochSeconds % PERIOD_SECONDS
        return (PERIOD_SECONDS - elapsedInWindow).toInt()
    }
}
