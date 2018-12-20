package com.template.webserver.controller

import com.template.flows.UpdateFlow
import com.template.webserver.NodeRPCConnection
import com.template.webserver.customer.CustomerUpdate
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private const val CONTROLLER_NAME = "config.controller.name"
/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/kyc") // The paths for HTTP requests are relative to this base path.
class UpdateController(
        private val rpc: NodeRPCConnection, @Value("\${$CONTROLLER_NAME}")
        private val controllerName: String) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    /**Update data API*/
    @PutMapping(value = "/update", produces = arrayOf("application/json"))
    private fun getUpdate(@RequestBody customerUpdates: CustomerUpdate): ResponseEntity<Map<String, Any>> {
        val (status, message) = try {

            val LinearId = UniqueIdentifier.fromString(customerUpdates.Id)
            val registerFlow = proxy.startFlowDynamic(UpdateFlow::class.java,
                    LinearId,
                    customerUpdates.name,
                    customerUpdates.age,
                    customerUpdates.address,
                    customerUpdates.birthDate,
                    customerUpdates.status,
                    customerUpdates.religion
            )
            val data = mapOf("data successfully updated!!!" to customerUpdates)
            val result = registerFlow.use { it.returnValue.getOrThrow() }
            HttpStatus.CREATED to data
        } catch (ex: Exception) {
            HttpStatus.BAD_REQUEST to "Failed to Update"
        }
        return ResponseEntity.status(status).body(mapOf("data" to message))

    }
}