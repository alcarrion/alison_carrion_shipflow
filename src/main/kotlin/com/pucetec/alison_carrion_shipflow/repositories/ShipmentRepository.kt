package com.pucetec.alison_carrion_shipflow.repositories

import com.pucetec.alison_carrion_shipflow.models.entities.Shipment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ShipmentRepository : JpaRepository<Shipment, Long> {
    fun findByTrackingId(trackingId: String): Shipment?
    fun findTopByOrderByIdDesc(): Shipment?
}
