package com.template.webserver.controller

import com.template.flows.ApprovedFlow
import com.template.webserver.NodeRPCConnection
import com.template.webserver.customer.CustomerApproved
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
class ApprovedController(
        private val rpc: NodeRPCConnection, @Value("\${$CONTROLLER_NAME}") private val controllerName: String) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    /** Approved request API*/


    @PostMapping(value = "/approvedRequest", produces = arrayOf("application/json"))
    private fun getApproved(@RequestBody customerApproved: CustomerApproved): ResponseEntity<Map<String, Any>> {
        val (status, message) = try {

            val OwnIdentity = rpc.proxy.partiesFromName(customerApproved.requestParty,exactMatch = false).singleOrNull()
                    ?: throw IllegalStateException("No ${customerApproved.requestParty} in the network map.")
            val LinearId = UniqueIdentifier.fromString(customerApproved.Id)
            val requestHandle = rpc.proxy.startFlowDynamic(ApprovedFlow::class.java,OwnIdentity,
                    LinearId
            )

            val result = requestHandle.use { it.returnValue.getOrThrow() }


            val list = mapOf("Successful Approved Request" to customerApproved)
            HttpStatus.CREATED to list

        } catch (ex: Exception) {
            HttpStatus.BAD_REQUEST to "Failed to Approved Request"
        }
        return ResponseEntity.status(status).body(mapOf("data" to message))

    }
}