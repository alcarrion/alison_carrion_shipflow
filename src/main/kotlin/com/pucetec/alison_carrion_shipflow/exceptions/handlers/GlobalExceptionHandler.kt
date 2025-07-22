package com.pucetec.alison_carrion_shipflow.exceptions.handlers

import com.pucetec.alison_carrion_shipflow.exceptions.exceptions.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(SameCityNotAllowedException::class)
    fun handleSameCity(ex: SameCityNotAllowedException): ResponseEntity<Map<String, String>> =
        ResponseEntity(mapOf("error" to ex.message.orEmpty()), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(DescriptionLimitExceededException::class)
    fun handleDescriptionLimit(ex: DescriptionLimitExceededException): ResponseEntity<Map<String, String>> =
        ResponseEntity(mapOf("error" to ex.message.orEmpty()), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(UnsupportedShippingTypeException::class)
    fun handleUnsupportedType(ex: UnsupportedShippingTypeException): ResponseEntity<Map<String, String>> =
        ResponseEntity(mapOf("error" to ex.message.orEmpty()), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(ShipmentNotFoundException::class)
    fun handleShipmentNotFound(ex: ShipmentNotFoundException): ResponseEntity<Map<String, String>> =
        ResponseEntity(mapOf("error" to ex.message.orEmpty()), HttpStatus.NOT_FOUND)

    @ExceptionHandler(UnknownShippingStatusException::class)
    fun handleUnknownStatus(ex: UnknownShippingStatusException): ResponseEntity<Map<String, String>> =
        ResponseEntity(mapOf("error" to ex.message.orEmpty()), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(DisallowedStatusChangeException::class)
    fun handleDisallowedTransition(ex: DisallowedStatusChangeException): ResponseEntity<Map<String, String>> =
        ResponseEntity(mapOf("error" to ex.message.orEmpty()), HttpStatus.CONFLICT)

    @ExceptionHandler(ShippingStatusRuleException::class)
    fun handleStatusRule(ex: ShippingStatusRuleException): ResponseEntity<Map<String, String>> =
        ResponseEntity(mapOf("error" to ex.message.orEmpty()), HttpStatus.BAD_REQUEST)
}
