package com.pucetec.alison_carrion_shipflow.models.responses

data class ShipmentDetailResponse(
    val shipmentInfo: ShipmentResponse,
    val eventHistory: List<ShipmentEventResponse>
)
