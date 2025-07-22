package com.pucetec.alison_carrion_shipflow.models.entities

enum class ShippingType(val typeName: String) {
    DOCUMENT("D"),
    SMALL_BOX("SB"),
    FRAGILE("F");

    companion object {
        fun from(value: String): ShippingType? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
