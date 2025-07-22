package com.pucetec.alison_carrion_shipflow.models.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "shipment_events")
data class ShipmentEvent(

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: ShippingStatus,

    @Column(name = "comment", length = 255)
    val comment: String? = null,

    @ManyToOne
    @JoinColumn(name = "shipment_id", nullable = false)
    val shipment: Shipment,

    @Column(name = "event_date", nullable = false)
    val eventDate: LocalDateTime = LocalDateTime.now()

) : BaseEntity()
