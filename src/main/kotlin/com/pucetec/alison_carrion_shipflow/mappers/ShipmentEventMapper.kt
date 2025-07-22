package com.pucetec.alison_carrion_shipflow.mappers

import com.pucetec.alison_carrion_shipflow.models.entities.Shipment
import com.pucetec.alison_carrion_shipflow.models.entities.ShipmentEvent
import com.pucetec.alison_carrion_shipflow.models.entities.ShippingStatus
import com.pucetec.alison_carrion_shipflow.models.requests.UpdateShipmentStatusRequest
import com.pucetec.alison_carrion_shipflow.models.responses.ShipmentEventResponse
import com.pucetec.alison_carrion_shipflow.models.responses.UpdateStatusResponse
import org.springframework.stereotype.Component

@Component
class ShipmentEventMapper {

    fun toResponse(entity: ShipmentEvent): ShipmentEventResponse {
        return ShipmentEventResponse(
            id = entity.id,
            status = entity.status.statusName,
            comment = entity.comment,
            eventDate = entity.eventDate,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toUpdateStatusResponse(entity: ShipmentEvent): UpdateStatusResponse {
        return UpdateStatusResponse(
            id = entity.id,
            status = entity.status.statusName,
            comment = entity.comment,
            eventDate = entity.eventDate
        )
    }

    fun toEntity(request: UpdateShipmentStatusRequest, shipment: Shipment): ShipmentEvent {
        return ShipmentEvent(
            status = ShippingStatus.valueOf(request.status),
            comment = request.comment,
            shipment = shipment
        )
    }
}
