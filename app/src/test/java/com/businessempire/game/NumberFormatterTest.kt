package com.businessempire.game

import com.businessempire.game.domain.NumberFormatter
import com.businessempire.game.domain.NumberStyle
import org.junit.Assert.assertEquals
import org.junit.Test

class NumberFormatterTest {
    @Test fun smallNumbers() {
        assertEquals("0", NumberFormatter.short(0.0))
        assertEquals("50", NumberFormatter.short(50.0))
        assertEquals("4.5", NumberFormatter.short(4.5))
        assertEquals("999", NumberFormatter.short(999.0))
        assertEquals("-12", NumberFormatter.short(-12.0))
    }

    @Test fun namedSuffixes() {
        assertEquals("1.23K", NumberFormatter.short(1_234.0))
        assertEquals("12.3K", NumberFormatter.short(12_340.0))
        assertEquals("123K", NumberFormatter.short(123_400.0))
        assertEquals("1.00M", NumberFormatter.short(999_999.0))
        assertEquals("4.56B", NumberFormatter.short(4.56e9))
        assertEquals("7.00T", NumberFormatter.short(7e12))
        assertEquals("1.00Qa", NumberFormatter.short(1e15))
        assertEquals("1.00Dc", NumberFormatter.short(1e33))
    }

    @Test fun letterSuffixesAfterDecillion() {
        assertEquals("1.00aa", NumberFormatter.short(1e36))
        assertEquals("1.00ab", NumberFormatter.short(1e39))
        assertEquals("1.00ba", NumberFormatter.short(1e36 * 1000.0.pow(26)))
    }

    @Test fun scientific() {
        assertEquals("1.23e3", NumberFormatter.format(1_234.0, NumberStyle.SCIENTIFIC))
        assertEquals("1.00e6", NumberFormatter.format(999_999.0, NumberStyle.SCIENTIFIC))
        assertEquals("500", NumberFormatter.format(500.0, NumberStyle.SCIENTIFIC))
    }

    private fun Double.pow(n: Int) = Math.pow(this, n.toDouble())
}
