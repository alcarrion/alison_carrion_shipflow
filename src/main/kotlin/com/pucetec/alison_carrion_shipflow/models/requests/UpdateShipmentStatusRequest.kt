package com.pucetec.alison_carrion_shipflow.models.requests


data class UpdateShipmentStatusRequest(
    val status: String,
    val comment: String? = null
)
