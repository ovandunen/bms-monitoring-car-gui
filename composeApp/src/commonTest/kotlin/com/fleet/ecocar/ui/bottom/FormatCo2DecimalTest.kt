package com.fleet.ecocar.ui.bottom

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatCo2DecimalTest {

    @Test
    fun formatCo2Decimal_oneDecimalUsLocale() {
        assertEquals("0.5", formatCo2Decimal(0.484766))
        assertEquals("1.5", formatCo2Decimal(1500.0 / 1000.0))
    }
}
