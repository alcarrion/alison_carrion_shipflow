package com.pucetec.alison_carrion_shipflow.models.responses

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class UpdateStatusResponse(
    val id: Long,
    val status: String,
    val comment: String?,

    @JsonProperty("updated_at")
    val updatedAt: LocalDateTime
)
