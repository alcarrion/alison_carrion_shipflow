package com.pucetec.alison_carrion_shipflow.models.entities

enum class ShippingStatus(val statusName: String) {
    PENDING("PENDING"),
    IN_TRANSIT("IN_TRANSIT"),
    DELIVERED("DELIVERED"),
    ON_HOLD("ON_HOLD"),
    CANCELLED("CANCELLED")
}
