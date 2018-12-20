package com.template.webserver.controller

import com.template.flows.RegisterFlow
import com.template.states.UserState
import com.template.webserver.NodeRPCConnection
import com.template.webserver.customer.Customers
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private const val CONTROLLER_NAME = "config.controller.name"
/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/kyc") // The paths for HTTP requests are relative to this base path.
class RegisterController(
        private val rpc: NodeRPCConnection, @Value("\${$CONTROLLER_NAME}")
        private val controllerName: String) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }
    private val proxy = rpc.proxy

            /**Register data API */
@PostMapping(value = "/register",produces = arrayOf("application/json"))
    private fun getRegister(@RequestBody customers: Customers): ResponseEntity<Map<String, Any>> {
        val (status, message) = try {
            val registerFlow = proxy.startFlowDynamic(RegisterFlow::class.java,

                    customers.name,
                    customers.age,
                    customers.address,
                    customers.birthDate,
                    customers.status,
                    customers.religion
            )
            val data = mapOf("data Successfully Registered" to customers)
            val result = registerFlow.use { it.returnValue.getOrThrow() }
            HttpStatus.CREATED to data
        } catch (ex: Exception) {
            HttpStatus.BAD_REQUEST to "Failed to Register"
        }
        return ResponseEntity.status(status).body(mapOf( "data" to message))

    }


}