package com.template.services

import java.math.BigDecimal

data class ExChangeState(val transferorSymbol: String, val transferorAmount: BigDecimal,
                         val transfereeSymbol: String, val transfereeAmount: BigDecimal, val exRate: Double)
