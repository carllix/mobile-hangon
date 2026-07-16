package com.example.hangon.data.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TotpGeneratorTest {

    @Test
    fun `matches backend pyotp output for a fixed secret and time`() {
        val secret = "JBSWY3DPEHPK3PXP"
        val epochSeconds = 1767225600L

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
