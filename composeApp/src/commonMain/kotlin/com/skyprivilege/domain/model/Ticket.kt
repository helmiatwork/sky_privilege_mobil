package com.skyprivilege.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Ticket(
    val passengerName: String,
    val pnr: String,
    val fromAirport: String,
    val toAirport: String,
    val operatingCarrier: String,
    val flightNumber: String,
    val flightDate: String,
    val compartmentCode: String,
    val seatNumber: String,
    val checkInSequence: String,
    val canonicalHash: String? = null,
    val isValid: Boolean = true
)

class TicketVerificationException(
    override val message: String,
    val reasonCode: String? = null,
    val redeemedAt: String? = null,
    val redeemedOutlet: String? = null,
    val redeemedCashier: String? = null
) : Exception(message)
