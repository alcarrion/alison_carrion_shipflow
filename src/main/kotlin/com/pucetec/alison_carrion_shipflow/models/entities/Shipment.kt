package com.pucetec.alison_carrion_shipflow.models.entities

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "shipments")
data class Shipment(

    @Column(name = "tracking_id", unique = true, nullable = false)
    val trackingId: String,

    @Column(nullable = false)
    val type: String,

    @Column(nullable = false)
    val weight: Float,

    @Column(nullable = false, length = 50)
    val description: String,

    @Column(name = "city_from", nullable = false)
    val cityFrom: String,

    @Column(name = "city_to", nullable = false)
    val cityTo: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ShippingStatus = ShippingStatus.PENDING,

    @Column(name = "estimated_delivery_date", nullable = false)
    val estimatedDeliveryDate: LocalDate,

    @OneToMany(mappedBy = "shipment", cascade = [CascadeType.ALL], orphanRemoval = true)
    val events: List<ShipmentEvent> = emptyList()

) : BaseEntity()
