package com.pucetec.alison_carrion_shipflow.controllers

import com.pucetec.alison_carrion_shipflow.models.requests.ShipmentRequest
import com.pucetec.alison_carrion_shipflow.models.requests.UpdateShipmentStatusRequest
import com.pucetec.alison_carrion_shipflow.models.responses.ShipmentEventResponse
import com.pucetec.alison_carrion_shipflow.models.responses.ShipmentResponse
import com.pucetec.alison_carrion_shipflow.models.responses.UpdateStatusResponse
import com.pucetec.alison_carrion_shipflow.routes.Routes
import com.pucetec.alison_carrion_shipflow.services.ShipmentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(Routes.SHIPMENTS)
class ShipmentController(
    private val shipmentService: ShipmentService
) {

    @PostMapping
    fun create(@RequestBody request: ShipmentRequest): ResponseEntity<ShipmentResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(shipmentService.createShipment(request))

    @GetMapping
    fun getAll(): List<ShipmentResponse> =
        shipmentService.getAllShipments()

    @PutMapping("/{id}${Routes.STATUS}")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestBody request: UpdateShipmentStatusRequest
    ): ResponseEntity<UpdateStatusResponse> =
        ResponseEntity.ok(shipmentService.updateShipmentStatus(id, request))

    @GetMapping("/{id}${Routes.EVENTS}")
    fun getEvents(@PathVariable id: Long): List<ShipmentEventResponse> =
        shipmentService.getShipmentEvents(id)
}
