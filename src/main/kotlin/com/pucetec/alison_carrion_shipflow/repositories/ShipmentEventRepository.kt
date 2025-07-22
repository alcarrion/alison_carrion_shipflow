package com.pucetec.alison_carrion_shipflow.repositories

import com.pucetec.alison_carrion_shipflow.models.entities.ShipmentEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ShipmentEventRepository : JpaRepository<ShipmentEvent, Long> {
    fun findByShipmentIdOrderByEventDateAsc(shipmentId: Long): List<ShipmentEvent>
}
