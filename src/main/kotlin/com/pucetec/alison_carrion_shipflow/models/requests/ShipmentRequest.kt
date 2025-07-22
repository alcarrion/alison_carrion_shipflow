package com.pucetec.alison_carrion_shipflow.models.requests

import com.fasterxml.jackson.annotation.JsonProperty

data class ShipmentRequest(
    val type: String,
    val weight: Float,
    val description: String,

    @JsonProperty("city_from")
    val cityFrom: String,

    @JsonProperty("city_to")
    val cityTo: String
)
