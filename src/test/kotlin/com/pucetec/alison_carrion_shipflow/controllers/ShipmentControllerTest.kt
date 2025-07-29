package com.pucetec.alison_carrion_shipflow.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.pucetec.alison_carrion_shipflow.exceptions.exceptions.*
import com.pucetec.alison_carrion_shipflow.mappers.ShipmentEventMapper
import com.pucetec.alison_carrion_shipflow.mappers.ShipmentMapper
import com.pucetec.alison_carrion_shipflow.models.entities.Shipment
import com.pucetec.alison_carrion_shipflow.models.requests.ShipmentRequest
import com.pucetec.alison_carrion_shipflow.models.requests.UpdateShipmentStatusRequest
import com.pucetec.alison_carrion_shipflow.models.responses.*
import com.pucetec.alison_carrion_shipflow.routes.Routes
import com.pucetec.alison_carrion_shipflow.services.ShipmentService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals

@WebMvcTest(ShipmentController::class)
@Import(ShipmentControllerTest.MockConfig::class)
class ShipmentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var shipmentService: ShipmentService

    @Autowired
    private lateinit var shipmentMapper: ShipmentMapper

    @Autowired
    private lateinit var shipmentEventMapper: ShipmentEventMapper

    private lateinit var objectMapper: ObjectMapper
    private val baseUrl = Routes.SHIPMENTS

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    }


    private val request = ShipmentRequest("DOCUMENT", 2.5f, "Papeles importantes", "Quito", "Guayaquil")
    private val response = ShipmentResponse(
        1L, "1001", "DOCUMENT", 2.5f, "Papeles importantes",
        "Quito", "Guayaquil", "PENDING",
        LocalDate.now(), LocalDateTime.now(), LocalDateTime.now()
    )
    private val updateRequest = UpdateShipmentStatusRequest("IN_TRANSIT", "En camino")
    private val updateResponse = UpdateStatusResponse(1L, "IN_TRANSIT", "En camino", LocalDateTime.now())
    private val detailResponse = ShipmentDetailResponse(
        shipmentInfo = response,
        eventHistory = listOf(
            ShipmentEventResponse(1L, "PENDING", "Creado", LocalDateTime.now())
        )
    )

    @Test
    fun should_list_all_shipments() {
        `when`(shipmentService.getAllShipments()).thenReturn(listOf(response))

        val result = mockMvc.get(baseUrl)
            .andExpect {
                status { isOk() }
                jsonPath("$[0].tracking_id") { value("1001") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_get_shipment_by_tracking_id() {
        `when`(shipmentService.getShipmentByTrackingId("1001")).thenReturn(response)

        val result = mockMvc.get("$baseUrl/1001")
            .andExpect {
                status { isOk() }
                jsonPath("$.tracking_id") { value("1001") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_create_shipment() {
        `when`(shipmentService.createShipment(request)).thenReturn(response)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.post(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.tracking_id") { value("1001") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_update_shipment_status() {
        `when`(shipmentService.updateShipmentStatus("1001", updateRequest)).thenReturn(updateResponse)

        val json = objectMapper.writeValueAsString(updateRequest)

        val result = mockMvc.put("$baseUrl/1001/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("IN_TRANSIT") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_get_shipment_detail() {
        val shipmentMock = mock(Shipment::class.java)
        `when`(shipmentService.getShipmentByTrackingIdRaw("1001")).thenReturn(shipmentMock)
        `when`(shipmentMapper.toDetailResponse(shipmentMock, shipmentEventMapper)).thenReturn(detailResponse)

        val result = mockMvc.get("$baseUrl/1001/events")
            .andExpect {
                status { isOk() }
                jsonPath("$.shipmentInfo.tracking_id") { value("1001") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_shipment_not_found() {
        `when`(shipmentService.getShipmentByTrackingId("9999"))
            .thenThrow(ShipmentNotFoundException("No se encontró el envío con tracking ID 9999."))

        val result = mockMvc.get("$baseUrl/9999")
            .andExpect {
                status { isNotFound() }
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_return_404_when_updating_non_existing_shipment() {
        `when`(shipmentService.updateShipmentStatus("9999", updateRequest))
            .thenThrow(ShipmentNotFoundException("No se encontró el envío con tracking ID 9999."))

        val json = objectMapper.writeValueAsString(updateRequest)

        val result = mockMvc.put("$baseUrl/9999/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isNotFound() }
        }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_return_400_when_invalid_status_is_sent() {
        val invalidUpdateRequest = UpdateShipmentStatusRequest("INVALID_STATUS", "Comentario")
        `when`(shipmentService.updateShipmentStatus("1001", invalidUpdateRequest))
            .thenThrow(UnknownShippingStatusException("Estado inválido."))

        val json = objectMapper.writeValueAsString(invalidUpdateRequest)

        val result = mockMvc.put("$baseUrl/1001/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isBadRequest() }
        }.andReturn()

        assertEquals(400, result.response.status)
    }

    @Test
    fun should_return_400_when_creating_shipment_with_same_city() {
        val badRequest = ShipmentRequest("DOCUMENT", 1.0f, "Sobre", "Quito", "Quito")
        `when`(shipmentService.createShipment(badRequest))
            .thenThrow(SameCityNotAllowedException("La ciudad de origen y destino no pueden ser iguales."))

        val json = objectMapper.writeValueAsString(badRequest)

        val result = mockMvc.post(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isBadRequest() }
        }.andReturn()

        assertEquals(400, result.response.status)
    }

    @Test
    fun should_return_400_when_description_exceeds_limit() {
        val longDescriptionRequest = ShipmentRequest("DOCUMENT", 1.0f, "A".repeat(51), "Quito", "Guayaquil")
        `when`(shipmentService.createShipment(longDescriptionRequest))
            .thenThrow(DescriptionLimitExceededException("La descripción no debe exceder los 50 caracteres."))

        val json = objectMapper.writeValueAsString(longDescriptionRequest)

        val result = mockMvc.post(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isBadRequest() }
        }.andReturn()

        assertEquals(400, result.response.status)
    }

    @Test
    fun should_return_400_when_shipping_type_is_invalid() {
        val invalidTypeRequest = ShipmentRequest("INVALID_TYPE", 1.0f, "Zapatos", "Quito", "Guayaquil")
        `when`(shipmentService.createShipment(invalidTypeRequest))
            .thenThrow(UnsupportedShippingTypeException("Tipo de envío inválido."))

        val json = objectMapper.writeValueAsString(invalidTypeRequest)

        val result = mockMvc.post(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isBadRequest() }
        }.andReturn()

        assertEquals(400, result.response.status)
    }

    @Test
    fun should_return_409_when_invalid_status_transition() {
        val request = UpdateShipmentStatusRequest("DELIVERED", "Intento de salto directo")

        `when`(shipmentService.updateShipmentStatus("1001", request))
            .thenThrow(DisallowedStatusChangeException("No se puede cambiar de PENDING a DELIVERED directamente."))

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$baseUrl/1001/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isConflict() }
        }.andReturn()

        assertEquals(409, result.response.status)
    }

    @Test
    fun should_return_400_when_updating_delivered_shipment() {
        val request = UpdateShipmentStatusRequest("CANCELLED", "Intento de cancelar después de entregado")

        `when`(shipmentService.updateShipmentStatus("1002", request))
            .thenThrow(ShippingStatusRuleException("El envío ya está en estado final (DELIVERED)."))

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$baseUrl/1002/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isBadRequest() }
        }.andReturn()

        assertEquals(400, result.response.status)
    }

    @Test
    fun should_return_400_when_updating_cancelled_shipment() {
        val request = UpdateShipmentStatusRequest("IN_TRANSIT", "Intento de mover después de cancelado")

        `when`(shipmentService.updateShipmentStatus("1003", request))
            .thenThrow(ShippingStatusRuleException("El envío ya está en estado final (CANCELLED)."))

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$baseUrl/1003/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isBadRequest() }
        }.andReturn()

        assertEquals(400, result.response.status)
    }

    @Test
    fun should_return_409_when_on_hold_to_delivered_not_allowed() {
        val request = UpdateShipmentStatusRequest("DELIVERED", "Entrega sin salir de ON_HOLD")

        `when`(shipmentService.updateShipmentStatus("1004", request))
            .thenThrow(DisallowedStatusChangeException("No se puede cambiar de ON_HOLD a DELIVERED."))

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$baseUrl/1004/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isConflict() }
        }.andReturn()

        assertEquals(409, result.response.status)
    }

    @TestConfiguration
    class MockConfig {
        @Bean fun shipmentService(): ShipmentService = mock(ShipmentService::class.java)
        @Bean fun shipmentMapper(): ShipmentMapper = mock(ShipmentMapper::class.java)
        @Bean fun shipmentEventMapper(): ShipmentEventMapper = mock(ShipmentEventMapper::class.java)
    }
}
