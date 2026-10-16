package com.example.biztrackci.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val frenchFormat: NumberFormat = NumberFormat.getIntegerInstance(Locale.FRANCE)

    fun formatFcfa(amount: Double): String {
        return "${frenchFormat.format(amount.toLong())} FCFA"
    }

    fun formatFcfa(amount: Long): String {
        return "${frenchFormat.format(amount)} FCFA"
    }

    fun formatFcfa(amount: Int): String {
        return "${frenchFormat.format(amount.toLong())} FCFA"
    }
}

fun Double.toFcfa(): String = CurrencyFormatter.formatFcfa(this)
fun Long.toFcfa(): String = CurrencyFormatter.formatFcfa(this)
fun Int.toFcfa(): String = CurrencyFormatter.formatFcfa(this.toDouble())
