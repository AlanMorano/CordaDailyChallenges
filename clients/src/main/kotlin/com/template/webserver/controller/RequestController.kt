package com.template.webserver.controller

import com.template.flows.RequestFlow
import com.template.webserver.NodeRPCConnection
import com.template.webserver.customer.CustomerRequest
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private const val CONTROLLER_NAME = "config.controller.name"
/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/kyc") // The paths for HTTP requests are relative to this base path.
class RequestController(
        private val rpc: NodeRPCConnection, @Value("\${$CONTROLLER_NAME}")
        private val controllerName: String) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

        /**Request data API*/
    @PostMapping(value = "createRequest", produces = arrayOf("application/json"))
    private fun getRequest(@RequestBody customerRequest: CustomerRequest): ResponseEntity<Map<String, Any>> {
        val (status, message) = try {


            val OwnIdentity = rpc.proxy.partiesFromName(customerRequest.OtherParty,exactMatch = false).singleOrNull()
                ?: throw IllegalStateException("No ${customerRequest.OtherParty} in the network map.")
            val LinearId= UniqueIdentifier.fromString(customerRequest.Id)
            val requestHandle = rpc.proxy.startFlowDynamic(RequestFlow::class.java,
                    OwnIdentity,
                    LinearId)

            val result = requestHandle.use{it.returnValue.getOrThrow()}


            val list = mapOf("Successful send Request" to customerRequest)

            HttpStatus.CREATED to list
        } catch (ex: Exception) { HttpStatus.BAD_REQUEST to "Failed to Request" }
        return ResponseEntity.status(status).body(mapOf( "data" to message))

    }
}