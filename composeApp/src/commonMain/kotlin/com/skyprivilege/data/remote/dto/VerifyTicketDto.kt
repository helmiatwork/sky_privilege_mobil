package com.skyprivilege.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyTicketRequest(
    @SerialName("barcode_data") val barcodeData: String,
    @SerialName("outlet_id") val outletId: Long
)

@Serializable
data class ParsedTicketDto(
    val valid: Boolean = false,
    @SerialName("passenger_name") val passengerName: String? = null,
    val pnr: String? = null,
    @SerialName("from_airport") val fromAirport: String? = null,
    @SerialName("to_airport") val toAirport: String? = null,
    @SerialName("operating_carrier") val operatingCarrier: String? = null,
    @SerialName("flight_number") val flightNumber: String? = null,
    @SerialName("flight_date") val flightDate: String? = null,
    @SerialName("compartment_code") val compartmentCode: String? = null,
    @SerialName("seat_number") val seatNumber: String? = null,
    @SerialName("check_in_sequence") val checkInSequence: String? = null
)

@Serializable
data class VerifyTicketResponse(
    val success: Boolean,
    @SerialName("pnr_hash") val pnrHash: String? = null,
    val ticket: ParsedTicketDto? = null,
    val error: String? = null
)
