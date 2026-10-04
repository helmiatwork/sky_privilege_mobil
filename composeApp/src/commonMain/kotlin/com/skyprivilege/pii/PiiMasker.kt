package com.skyprivilege.pii

object PiiMasker {
    fun maskName(name: String?): String {
        if (name.isNullOrBlank() || name.trim() == "-") return "-"
        val parts = name.trim().split(Regex("\\s+"))
        return parts.joinToString(" ") { part ->
            if (part.length <= 1) part
            else part.first() + "*".repeat(part.length - 1)
        }
    }

    fun maskPnr(pnr: String?): String {
        if (pnr.isNullOrBlank()) return "-"
        val clean = pnr.trim()
        return if (clean.length <= 3) clean
        else clean.take(2) + "*".repeat((clean.length - 3).coerceAtLeast(1)) + clean.takeLast(1)
    }
}
