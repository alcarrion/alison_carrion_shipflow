package com.pucetec.alison_carrion_shipflow.controllers

import com.pucetec.alison_carrion_shipflow.mappers.ShipmentEventMapper
import com.pucetec.alison_carrion_shipflow.mappers.ShipmentMapper
import com.pucetec.alison_carrion_shipflow.models.requests.ShipmentRequest
import com.pucetec.alison_carrion_shipflow.models.requests.UpdateShipmentStatusRequest
import com.pucetec.alison_carrion_shipflow.models.responses.ShipmentDetailResponse
import com.pucetec.alison_carrion_shipflow.models.responses.ShipmentResponse
import com.pucetec.alison_carrion_shipflow.models.responses.UpdateStatusResponse
import com.pucetec.alison_carrion_shipflow.routes.Routes
import com.pucetec.alison_carrion_shipflow.services.ShipmentService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(Routes.SHIPMENTS)
class ShipmentController(
    private val shipmentService: ShipmentService,
    private val shipmentMapper: ShipmentMapper,
    private val shipmentEventMapper: ShipmentEventMapper
) {

    @PostMapping
    fun createShipment(@RequestBody request: ShipmentRequest): ShipmentResponse =
        shipmentService.createShipment(request)

    @GetMapping
    fun listShipments(): List<ShipmentResponse> =
        shipmentService.getAllShipments()

    @PutMapping("/{trackingId}${Routes.STATUS}")
    fun updateStatus(
        @PathVariable trackingId: String,
        @RequestBody request: UpdateShipmentStatusRequest
    ): UpdateStatusResponse =
        shipmentService.updateShipmentStatus(trackingId, request)

    @GetMapping("/{trackingId}${Routes.EVENTS}")
    fun getShipmentDetailByTrackingId(@PathVariable trackingId: String): ShipmentDetailResponse {
        val shipment = shipmentService.getShipmentByTrackingIdRaw(trackingId)
        return shipmentMapper.toDetailResponse(shipment, shipmentEventMapper)
    }

    @GetMapping("/{trackingId}")
    fun getShipmentByTrackingId(@PathVariable trackingId: String): ShipmentResponse =
        shipmentService.getShipmentByTrackingId(trackingId)
}
