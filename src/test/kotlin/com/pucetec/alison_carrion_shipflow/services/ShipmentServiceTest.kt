package com.pucetec.alison_carrion_shipflow.services

import com.pucetec.alison_carrion_shipflow.exceptions.exceptions.*
import com.pucetec.alison_carrion_shipflow.mappers.ShipmentEventMapper
import com.pucetec.alison_carrion_shipflow.mappers.ShipmentMapper
import com.pucetec.alison_carrion_shipflow.models.entities.*
import com.pucetec.alison_carrion_shipflow.models.requests.ShipmentRequest
import com.pucetec.alison_carrion_shipflow.models.requests.UpdateShipmentStatusRequest
import com.pucetec.alison_carrion_shipflow.models.responses.ShipmentResponse
import com.pucetec.alison_carrion_shipflow.models.responses.UpdateStatusResponse
import com.pucetec.alison_carrion_shipflow.repositories.ShipmentEventRepository
import com.pucetec.alison_carrion_shipflow.repositories.ShipmentRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.time.LocalDate

class ShipmentServiceTest {

    private lateinit var shipmentRepository: ShipmentRepository
    private lateinit var shipmentEventRepository: ShipmentEventRepository
    private lateinit var shipmentMapper: ShipmentMapper
    private lateinit var eventMapper: ShipmentEventMapper
    private lateinit var shipmentService: ShipmentService

    @BeforeEach
    fun setUp() {
        shipmentRepository = mock(ShipmentRepository::class.java)
        shipmentEventRepository = mock(ShipmentEventRepository::class.java)
        shipmentMapper = mock(ShipmentMapper::class.java)
        eventMapper = mock(ShipmentEventMapper::class.java)
        shipmentService = ShipmentService(
            shipmentRepository,
            shipmentEventRepository,
            shipmentMapper,
            eventMapper
        )
    }


    @Test
    fun should_create_shipment_successfully() {
        val request = ShipmentRequest(ShippingType.DOCUMENT.name, 1.0f, "Sobre de cartas", "Quito", "Guayaquil")
        val entity = Shipment(
            trackingId = "1000",
            type = ShippingType.DOCUMENT.name,
            weight = 1.0f,
            description = "Sobre de cartas",
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            estimatedDeliveryDate = LocalDate.now().plusDays(5),
            status = ShippingStatus.PENDING,
            events = emptyList()
        )
        val statusRequest = UpdateShipmentStatusRequest("PENDING", "Creado")
        val response = mock(ShipmentResponse::class.java)

        `when`(shipmentRepository.findTopByOrderByIdDesc()).thenReturn(null)
        `when`(shipmentMapper.toEntity(request, "1000", ShippingType.DOCUMENT)).thenReturn(entity)
        `when`(shipmentRepository.save(entity)).thenReturn(entity)
        `when`(eventMapper.toEntity(statusRequest, entity)).thenReturn(mock(ShipmentEvent::class.java))
        `when`(shipmentEventRepository.save(any())).thenReturn(mock(ShipmentEvent::class.java))
        `when`(shipmentMapper.toResponse(entity)).thenReturn(response)

        val result = shipmentService.createShipment(request)

        assertEquals(response, result)
        verify(shipmentRepository).save(entity)
        verify(shipmentEventRepository).save(any())
    }

    @Test
    fun should_throw_same_city_exception() {
        val request = ShipmentRequest(ShippingType.FRAGILE.name, 1.0f, "Ceramica", "Quito", "Quito")
        assertThrows<SameCityNotAllowedException> {
            shipmentService.createShipment(request)
        }
    }

    @Test
    fun should_throw_description_limit_exceeded_exception() {
        val request = ShipmentRequest(ShippingType.DOCUMENT.name, 1.0f, "A".repeat(51), "Quito", "Guayaquil")
        assertThrows<DescriptionLimitExceededException> {
            shipmentService.createShipment(request)
        }
    }

    @Test
    fun should_throw_unsupported_shipping_type_exception() {
        val request = ShipmentRequest("INVALID", 1.0f, "Zapatos", "Quito", "Guayaquil")
        assertThrows<UnsupportedShippingTypeException> {
            shipmentService.createShipment(request)
        }
    }

    @Test
    fun should_increment_tracking_id_and_use_plus_one_branch() {
        val request = ShipmentRequest(ShippingType.DOCUMENT.name, 1.0f, "Carta de poder", "Quito", "Guayaquil")

        val lastShipment = mock(Shipment::class.java)
        `when`(lastShipment.trackingId).thenReturn("2000")

        val shipment = mock(Shipment::class.java)
        val response = mock(ShipmentResponse::class.java)
        val event = mock(ShipmentEvent::class.java)
        val statusRequest = UpdateShipmentStatusRequest("PENDING", "Envío creado y pendiente.")

        `when`(shipmentRepository.findTopByOrderByIdDesc()).thenReturn(lastShipment)
        `when`(shipmentMapper.toEntity(request, "2001", ShippingType.DOCUMENT)).thenReturn(shipment)
        `when`(shipmentRepository.save(shipment)).thenReturn(shipment)
        `when`(eventMapper.toEntity(statusRequest, shipment)).thenReturn(event)
        `when`(shipmentEventRepository.save(event)).thenReturn(event)
        `when`(shipmentMapper.toResponse(shipment)).thenReturn(response)

        val result = shipmentService.createShipment(request)

        assertEquals(response, result)
        verify(shipmentMapper).toEntity(request, "2001", ShippingType.DOCUMENT)
    }

    @Test
    fun should_fallback_to_default_tracking_id_when_last_id_is_invalid() {
        val request = ShipmentRequest("DOCUMENT", 1.0f, "Sobre", "Quito", "Guayaquil")

        val lastShipment = mock(Shipment::class.java)
        `when`(lastShipment.trackingId).thenReturn("INVALID")

        val shipment = mock(Shipment::class.java)

        val statusRequest = UpdateShipmentStatusRequest("PENDING", "Envío creado y pendiente.")
        val event = mock(ShipmentEvent::class.java)
        val response = mock(ShipmentResponse::class.java)

        `when`(shipmentRepository.findTopByOrderByIdDesc()).thenReturn(lastShipment)
        `when`(shipmentMapper.toEntity(request, "1000", ShippingType.DOCUMENT)).thenReturn(shipment)
        `when`(shipmentRepository.save(shipment)).thenReturn(shipment)
        `when`(eventMapper.toEntity(statusRequest, shipment)).thenReturn(event)
        `when`(shipmentEventRepository.save(event)).thenReturn(event)
        `when`(shipmentMapper.toResponse(shipment)).thenReturn(response)

        val result = shipmentService.createShipment(request)

        assertEquals(response, result)
    }

    @Test
    fun should_fallback_to_1000_when_last_tracking_id_is_null() {
        val request = ShipmentRequest(ShippingType.DOCUMENT.name, 1.0f, "Notificación", "Quito", "Guayaquil")

        val lastShipment = mock(Shipment::class.java)
        `when`(lastShipment.trackingId).thenReturn(null)

        val shipment = mock(Shipment::class.java)
        val event = mock(ShipmentEvent::class.java)
        val response = mock(ShipmentResponse::class.java)
        val statusRequest = UpdateShipmentStatusRequest("PENDING", "Envío creado y pendiente.")

        `when`(shipmentRepository.findTopByOrderByIdDesc()).thenReturn(lastShipment)
        `when`(shipmentMapper.toEntity(request, "1000", ShippingType.DOCUMENT)).thenReturn(shipment)
        `when`(shipmentRepository.save(shipment)).thenReturn(shipment)
        `when`(eventMapper.toEntity(statusRequest, shipment)).thenReturn(event)
        `when`(shipmentEventRepository.save(event)).thenReturn(event)
        `when`(shipmentMapper.toResponse(shipment)).thenReturn(response)

        val result = shipmentService.createShipment(request)

        assertEquals(response, result)
        verify(shipmentMapper).toEntity(request, "1000", ShippingType.DOCUMENT)
    }

    @Test
    fun should_return_all_shipments() {
        val entity = mock(Shipment::class.java)
        val response = mock(ShipmentResponse::class.java)
        `when`(shipmentRepository.findAll()).thenReturn(listOf(entity))
        `when`(shipmentMapper.toResponse(entity)).thenReturn(response)

        val result = shipmentService.getAllShipments()
        assertEquals(1, result.size)
    }

    @Test
    fun should_get_shipment_by_tracking_id() {
        val entity = mock(Shipment::class.java)
        val response = mock(ShipmentResponse::class.java)
        `when`(shipmentRepository.findByTrackingId("1000")).thenReturn(entity)
        `when`(shipmentMapper.toResponse(entity)).thenReturn(response)

        val result = shipmentService.getShipmentByTrackingId("1000")
        assertEquals(response, result)
    }

    @Test
    fun should_throw_shipment_not_found_on_get() {
        `when`(shipmentRepository.findByTrackingId("9999")).thenReturn(null)
        assertThrows<ShipmentNotFoundException> {
            shipmentService.getShipmentByTrackingId("9999")
        }
    }

    @Test
    fun should_return_raw_shipment_by_tracking_id() {
        val entity = mock(Shipment::class.java)
        `when`(shipmentRepository.findByTrackingId("1000")).thenReturn(entity)

        val result = shipmentService.getShipmentByTrackingIdRaw("1000")
        assertEquals(entity, result)
    }

    @Test
    fun should_throw_shipment_not_found_on_raw_get() {
        `when`(shipmentRepository.findByTrackingId("9999")).thenReturn(null)
        assertThrows<ShipmentNotFoundException> {
            shipmentService.getShipmentByTrackingIdRaw("9999")
        }
    }

    @Test
    fun should_update_status_successfully() {
        val request = UpdateShipmentStatusRequest("IN_TRANSIT", "En camino")
        val entity = Shipment(
            trackingId = "1000",
            type = ShippingType.SMALL_BOX.name,
            weight = 0.5f,
            description = "Caja",
            cityFrom = "Loja",
            cityTo = "Quito",
            estimatedDeliveryDate = LocalDate.now().plusDays(2),
            status = ShippingStatus.PENDING,
            events = emptyList()
        )
        val updatedEvent = mock(ShipmentEvent::class.java)
        val response = mock(UpdateStatusResponse::class.java)

        `when`(shipmentRepository.findByTrackingId("1000")).thenReturn(entity)
        `when`(eventMapper.toEntity(request, entity)).thenReturn(updatedEvent)
        `when`(shipmentEventRepository.save(updatedEvent)).thenReturn(updatedEvent)
        `when`(eventMapper.toUpdateStatusResponse(updatedEvent)).thenReturn(response)

        shipmentService.updateShipmentStatus("1000", request)

        assertEquals(ShippingStatus.IN_TRANSIT, entity.status)
        verify(shipmentRepository).save(entity)
        verify(shipmentEventRepository).save(updatedEvent)
    }

    @Test
    fun should_throw_shipment_not_found_on_update() {
        val request = UpdateShipmentStatusRequest("IN_TRANSIT", null)
        `when`(shipmentRepository.findByTrackingId("9999")).thenReturn(null)

        assertThrows<ShipmentNotFoundException> {
            shipmentService.updateShipmentStatus("9999", request)
        }
    }

    @Test
    fun should_throw_unknown_status_exception() {
        val request = UpdateShipmentStatusRequest("UNKNOWN", null)
        val entity = Shipment(
            trackingId = "1000",
            type = ShippingType.DOCUMENT.name,
            weight = 1.0f,
            description = "Sobre",
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            estimatedDeliveryDate = LocalDate.now().plusDays(3),
            status = ShippingStatus.PENDING,
            events = emptyList()
        )
        `when`(shipmentRepository.findByTrackingId("1000")).thenReturn(entity)

        assertThrows<UnknownShippingStatusException> {
            shipmentService.updateShipmentStatus("1000", request)
        }
    }

    @Test
    fun should_throw_disallowed_status_change_exception() {
        val request = UpdateShipmentStatusRequest("DELIVERED", null)
        val entity = Shipment(
            trackingId = "1000",
            type = ShippingType.FRAGILE.name,
            weight = 1.0f,
            description = "Parlantes",
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            estimatedDeliveryDate = LocalDate.now().plusDays(3),
            status = ShippingStatus.PENDING,
            events = emptyList()
        )
        `when`(shipmentRepository.findByTrackingId("1000")).thenReturn(entity)

        assertThrows<DisallowedStatusChangeException> {
            shipmentService.updateShipmentStatus("1000", request)
        }
    }

    @Test
    fun should_throw_shipping_status_rule_exception_when_modifying_final_status() {
        val request = UpdateShipmentStatusRequest("IN_TRANSIT", null)
        val entity = Shipment(
            trackingId = "1000",
            type = ShippingType.SMALL_BOX.name,
            weight = 1.0f,
            description = "caja pequeña",
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            estimatedDeliveryDate = LocalDate.now().plusDays(3),
            status = ShippingStatus.DELIVERED,
            events = emptyList()
        )
        `when`(shipmentRepository.findByTrackingId("1000")).thenReturn(entity)

        assertThrows<ShippingStatusRuleException> {
            shipmentService.updateShipmentStatus("1000", request)
        }
    }

    @Test
    fun should_throw_shipping_status_rule_exception_when_final_status_is_cancelled() {
        val request = UpdateShipmentStatusRequest("IN_TRANSIT", "Intento de cambio")
        val entity = Shipment(
            trackingId = "5000",
            type = ShippingType.DOCUMENT.name,
            weight = 1.0f,
            description = "Sobre",
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            estimatedDeliveryDate = LocalDate.now(),
            status = ShippingStatus.CANCELLED,
            events = emptyList()
        )

        `when`(shipmentRepository.findByTrackingId("5000")).thenReturn(entity)

        val exception = assertThrows<ShippingStatusRuleException> {
            shipmentService.updateShipmentStatus("5000", request)
        }
        assertTrue(exception.message!!.contains("estado final"))
    }

    @Test
    fun should_update_to_delivered_when_in_transit_event_exists() {
        val request = UpdateShipmentStatusRequest("DELIVERED", "Entregado con éxito")
        val pastEvent = ShipmentEvent(
            status = ShippingStatus.IN_TRANSIT,
            comment = "En camino",
            shipment = mock(Shipment::class.java)
        )
        val entity = Shipment(
            trackingId = "3006",
            type = ShippingType.DOCUMENT.name,
            weight = 1.0f,
            description = "Test",
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            estimatedDeliveryDate = LocalDate.now(),
            status = ShippingStatus.IN_TRANSIT,
            events = listOf(pastEvent)
        )
        val savedEvent = mock(ShipmentEvent::class.java)
        val response = mock(UpdateStatusResponse::class.java)

        `when`(shipmentRepository.findByTrackingId("3006")).thenReturn(entity)
        `when`(eventMapper.toEntity(request, entity)).thenReturn(savedEvent)
        `when`(shipmentEventRepository.save(savedEvent)).thenReturn(savedEvent)
        `when`(eventMapper.toUpdateStatusResponse(savedEvent)).thenReturn(response)

        val result = shipmentService.updateShipmentStatus("3006", request)

        assertEquals(response, result)
        assertEquals(ShippingStatus.DELIVERED, entity.status)
        verify(shipmentRepository).save(entity)
        verify(shipmentEventRepository).save(savedEvent)
    }

    @Test
    fun should_throw_status_rule_exception_when_delivered_without_in_transit() {
        val request = UpdateShipmentStatusRequest("DELIVERED", null)
        val entity = Shipment(
            trackingId = "1000",
            type = ShippingType.SMALL_BOX.name,
            weight = 1.0f,
            description = "perfumes",
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            estimatedDeliveryDate = LocalDate.now().plusDays(3),
            status = ShippingStatus.IN_TRANSIT,
            events = emptyList()
        )
        `when`(shipmentRepository.findByTrackingId("1000")).thenReturn(entity)

        assertThrows<ShippingStatusRuleException> {
            shipmentService.updateShipmentStatus("1000", request)
        }
    }

    @Test
    fun should_throw_shipping_status_rule_exception_when_delivered_without_in_transit_event() {
        val request = UpdateShipmentStatusRequest("DELIVERED", "Marcado como entregado")
        val shipment = Shipment(
            trackingId = "1003",
            type = ShippingType.FRAGILE.name,
            weight = 1.5f,
            description = "Camisetas",
            cityFrom = "Cuenca",
            cityTo = "Loja",
            estimatedDeliveryDate = LocalDate.now().plusDays(1),
            status = ShippingStatus.IN_TRANSIT,
            events = listOf(
                ShipmentEvent(
                    status = ShippingStatus.ON_HOLD,
                    comment = "Retenido",
                    shipment = mock(Shipment::class.java)
                )
            )
        )
        `when`(shipmentRepository.findByTrackingId("1003")).thenReturn(shipment)

        val exception = assertThrows<ShippingStatusRuleException> {
            shipmentService.updateShipmentStatus("1003", request)
        }
        assertEquals("Solo se puede marcar como ENTREGADO si antes estuvo EN_TRÁNSITO.", exception.message)
    }

    @Test
    fun should_return_true_when_transition_from_pending_to_in_transit() {
        val method = ShipmentService::class.java.getDeclaredMethod(
            "isValidTransition", ShippingStatus::class.java, ShippingStatus::class.java
        )
        method.isAccessible = true
        val result = method.invoke(shipmentService, ShippingStatus.PENDING, ShippingStatus.IN_TRANSIT) as Boolean
        assertTrue(result)
    }

    @Test
    fun should_return_true_when_transition_from_on_hold_to_in_transit() {
        val method = ShipmentService::class.java.getDeclaredMethod(
            "isValidTransition",
            ShippingStatus::class.java,
            ShippingStatus::class.java
        )
        method.isAccessible = true

        val result = method.invoke(
            shipmentService,
            ShippingStatus.ON_HOLD,
            ShippingStatus.IN_TRANSIT
        ) as Boolean

        assertTrue(result)
    }

    @Test
    fun should_return_true_when_transition_from_on_hold_to_cancelled() {
        val method = ShipmentService::class.java.getDeclaredMethod(
            "isValidTransition",
            ShippingStatus::class.java,
            ShippingStatus::class.java
        )
        method.isAccessible = true

        val result = method.invoke(
            shipmentService,
            ShippingStatus.ON_HOLD,
            ShippingStatus.CANCELLED
        ) as Boolean

        assertTrue(result)
    }

    @Test
    fun should_return_false_when_transition_from_delivered_to_cancelled() {
        val method = ShipmentService::class.java.getDeclaredMethod(
            "isValidTransition", ShippingStatus::class.java, ShippingStatus::class.java
        )
        method.isAccessible = true
        val result = method.invoke(shipmentService, ShippingStatus.DELIVERED, ShippingStatus.CANCELLED)
        assertEquals(false, result)
    }

    @Test
    fun should_return_false_when_transition_from_cancelled_to_any() {
        val method = ShipmentService::class.java.getDeclaredMethod(
            "isValidTransition", ShippingStatus::class.java, ShippingStatus::class.java
        )
        method.isAccessible = true
        val result1 = method.invoke(shipmentService, ShippingStatus.CANCELLED, ShippingStatus.PENDING) as Boolean
        val result2 = method.invoke(shipmentService, ShippingStatus.CANCELLED, ShippingStatus.IN_TRANSIT) as Boolean
        val result3 = method.invoke(shipmentService, ShippingStatus.CANCELLED, ShippingStatus.DELIVERED) as Boolean
        assertFalse(result1)
        assertFalse(result2)
        assertFalse(result3)
    }
}