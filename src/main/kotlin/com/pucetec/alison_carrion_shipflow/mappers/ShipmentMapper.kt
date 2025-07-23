package com.pucetec.alison_carrion_shipflow.mappers

import com.pucetec.alison_carrion_shipflow.models.entities.Shipment
import com.pucetec.alison_carrion_shipflow.models.entities.ShippingType
import com.pucetec.alison_carrion_shipflow.models.requests.ShipmentRequest
import com.pucetec.alison_carrion_shipflow.models.responses.ShipmentDetailResponse
import com.pucetec.alison_carrion_shipflow.models.responses.ShipmentResponse
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ShipmentMapper {

    fun toEntity(request: ShipmentRequest, trackingId: String, shippingType: ShippingType): Shipment {
        return Shipment(
            trackingId = trackingId,
            type = shippingType.name,
            weight = request.weight,
            description = request.description,
            cityFrom = request.cityFrom,
            cityTo = request.cityTo,
            estimatedDeliveryDate = LocalDate.now().plusDays(5)
        )
    }

    fun toResponse(entity: Shipment): ShipmentResponse {
        return ShipmentResponse(
            id = entity.id,
            trackingId = entity.trackingId,
            type = entity.type,
            weight = entity.weight,
            description = entity.description,
            cityFrom = entity.cityFrom,
            cityTo = entity.cityTo,
            status = entity.status.statusName,
            estimatedDeliveryDate = entity.estimatedDeliveryDate,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toDetailResponse(entity: Shipment, eventMapper: ShipmentEventMapper): ShipmentDetailResponse {
        return ShipmentDetailResponse(
            shipmentInfo = toResponse(entity),
            eventHistory = entity.events.map { eventMapper.toResponse(it) }
        )
    }

}
