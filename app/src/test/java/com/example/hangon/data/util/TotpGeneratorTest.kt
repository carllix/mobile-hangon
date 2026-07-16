package com.example.hangon.data.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TotpGeneratorTest {

    // Cross-language parity fixture: this exact (secret, epoch) pair was fed through
    // be-hangon's `derive_codeword()` (pyotp-based) and the resulting code recorded
    // here. Generic RFC 6238 test vectors don't apply since they assume 30s/8-digit
    // defaults, not this app's 60s/6-digit config — this is the one check that
    // actually proves the two implementations agree.
    @Test
    fun `matches backend pyotp output for a fixed secret and time`() {
        val secret = "JBSWY3DPEHPK3PXP"
        val epochSeconds = 1767225600L // 2026-01-01T00:00:00Z

        val code = TotpGenerator.currentCode(secret, epochSeconds)

        assertEquals("254303", code)
    }

    @Test
    fun `code is stable within the same 60s window`() {
        val secret = "JBSWY3DPEHPK3PXP"
        val windowStart = 1767225600L

        val codeAtStart = TotpGenerator.currentCode(secret, windowStart)
        val codeMidWindow = TotpGenerator.currentCode(secret, windowStart + 30)

        assertEquals(codeAtStart, codeMidWindow)
    }

    @Test
    fun `code changes across a window boundary`() {
        val secret = "JBSWY3DPEHPK3PXP"
        val windowStart = 1767225600L

        val codeInWindow = TotpGenerator.currentCode(secret, windowStart)
        val codeNextWindow = TotpGenerator.currentCode(secret, windowStart + 60)

        assert(codeInWindow != codeNextWindow)
    }

    @Test
    fun `secondsRemainingInWindow counts down within a 60s period`() {
        val windowStart = 1767225600L

        assertEquals(60, TotpGenerator.secondsRemainingInWindow(windowStart))
        assertEquals(1, TotpGenerator.secondsRemainingInWindow(windowStart + 59))
    }
}
