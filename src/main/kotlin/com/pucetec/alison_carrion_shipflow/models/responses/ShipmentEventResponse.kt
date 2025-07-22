package com.pucetec.alison_carrion_shipflow.models.responses

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class ShipmentEventResponse(
    val id: Long,
    val status: String,
    val comment: String?,

    @JsonProperty("event_date")
    val eventDate: LocalDateTime,

    @JsonProperty("created_at")
    val createdAt: LocalDateTime,

    @JsonProperty("updated_at")
    val updatedAt: LocalDateTime
)
