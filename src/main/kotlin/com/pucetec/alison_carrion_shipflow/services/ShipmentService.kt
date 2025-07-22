package com.pucetec.alison_carrion_shipflow.services

import com.pucetec.alison_carrion_shipflow.exceptions.exceptions.*
import com.pucetec.alison_carrion_shipflow.mappers.ShipmentEventMapper
import com.pucetec.alison_carrion_shipflow.mappers.ShipmentMapper
import com.pucetec.alison_carrion_shipflow.models.entities.ShippingStatus
import com.pucetec.alison_carrion_shipflow.models.entities.ShippingType
import com.pucetec.alison_carrion_shipflow.models.requests.ShipmentRequest
import com.pucetec.alison_carrion_shipflow.models.requests.UpdateShipmentStatusRequest
import com.pucetec.alison_carrion_shipflow.models.responses.ShipmentEventResponse
import com.pucetec.alison_carrion_shipflow.models.responses.ShipmentResponse
import com.pucetec.alison_carrion_shipflow.models.responses.UpdateStatusResponse
import com.pucetec.alison_carrion_shipflow.repositories.ShipmentEventRepository
import com.pucetec.alison_carrion_shipflow.repositories.ShipmentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ShipmentService(
    private val shipmentRepository: ShipmentRepository,
    private val shipmentEventRepository: ShipmentEventRepository,
    private val shipmentMapper: ShipmentMapper,
    private val eventMapper: ShipmentEventMapper
) {

    fun createShipment(request: ShipmentRequest): ShipmentResponse {
        if (request.cityFrom.trim().equals(request.cityTo.trim(), ignoreCase = true)) {
            throw SameCityNotAllowedException("La ciudad de origen y destino no pueden ser iguales.")
        }

        if (request.description.length > 50) {
            throw DescriptionLimitExceededException("La descripción no debe exceder los 50 caracteres.")
        }

        val shippingType = ShippingType.from(request.type)
            ?: throw UnsupportedShippingTypeException("Tipo de envío inválido. Usa: DOCUMENT, SMALL_BOX o FRAGILE.")

        val lastShipment = shipmentRepository.findTopByOrderByIdDesc()
        val trackingId = if (lastShipment != null) {
            lastShipment.trackingId.toLongOrNull()?.plus(1)?.toString() ?: "1000"
        } else {
            "1000"
        }

        val shipment = shipmentMapper.toEntity(request, trackingId, shippingType)
        val savedShipment = shipmentRepository.save(shipment)

        val initialEvent = eventMapper.toEntity(
            UpdateShipmentStatusRequest(ShippingStatus.PENDING.name, "Envío creado y pendiente."),
            savedShipment
        )
        shipmentEventRepository.save(initialEvent)

        return shipmentMapper.toResponse(savedShipment)
    }

    fun getAllShipments(): List<ShipmentResponse> =
        shipmentRepository.findAll().map { shipmentMapper.toResponse(it) }

    fun getShipmentEvents(trackingId: String): List<ShipmentEventResponse> {
        val shipment = shipmentRepository.findByTrackingId(trackingId)
            ?: throw ShipmentNotFoundException("No se encontró el envío con tracking ID $trackingId.")

        return shipmentEventRepository.findByShipmentIdOrderByEventDateAsc(shipment.id)
            .map { eventMapper.toResponse(it) }
    }

    fun getShipmentByTrackingId(trackingId: String): ShipmentResponse {
        val shipment = shipmentRepository.findByTrackingId(trackingId)
            ?: throw ShipmentNotFoundException("No se encontró el envío con tracking ID $trackingId.")
        return shipmentMapper.toResponse(shipment)
    }

    fun updateShipmentStatus(trackingId: String, request: UpdateShipmentStatusRequest): UpdateStatusResponse {
        val shipment = shipmentRepository.findByTrackingId(trackingId)
            ?: throw ShipmentNotFoundException("No se encontró el envío con tracking ID $trackingId.")

        val newStatus = ShippingStatus.entries.find { it.name == request.status }
            ?: throw UnknownShippingStatusException("Estado inválido: ${request.status}. Usa: PENDING, IN_TRANSIT, DELIVERED, ON_HOLD, CANCELLED.")

        if (!isValidTransition(shipment.status, newStatus)) {
            throw DisallowedStatusChangeException("No se puede cambiar el estado de ${shipment.status} a $newStatus.")
        }

        if (newStatus == ShippingStatus.DELIVERED) {
            val wasInTransit = shipment.events.any { it.status == ShippingStatus.IN_TRANSIT }
            if (!wasInTransit) {
                throw ShippingStatusRuleException("Solo se puede marcar como ENTREGADO si antes estuvo EN TRÁNSITO.")
            }
        }

        shipment.status = newStatus
        shipmentRepository.save(shipment)

        val event = eventMapper.toEntity(request, shipment)
        val savedEvent = shipmentEventRepository.save(event)

        return eventMapper.toUpdateStatusResponse(savedEvent)
    }

    private fun isValidTransition(current: ShippingStatus, next: ShippingStatus): Boolean {
        return when (current) {
            ShippingStatus.PENDING -> next == ShippingStatus.IN_TRANSIT
            ShippingStatus.IN_TRANSIT -> next in listOf(
                ShippingStatus.DELIVERED,
                ShippingStatus.ON_HOLD,
                ShippingStatus.CANCELLED
            )
            ShippingStatus.ON_HOLD -> next in listOf(
                ShippingStatus.IN_TRANSIT,
                ShippingStatus.CANCELLED
            )
            ShippingStatus.DELIVERED, ShippingStatus.CANCELLED -> false
        }
    }
}
