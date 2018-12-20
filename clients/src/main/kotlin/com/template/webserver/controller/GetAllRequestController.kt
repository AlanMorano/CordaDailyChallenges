package com.template.webserver.controller

import com.template.states.RequestState
import com.template.webserver.NodeRPCConnection
import net.corda.core.messaging.vaultQueryBy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private const val CONTROLLER_NAME = "config.controller.name"
/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/kyc") // The paths for HTTP requests are relative to this base path.
 class GetAllRequestController(
        private val rpc: NodeRPCConnection, @Value("\${$CONTROLLER_NAME}") private val controllerName: String) {

        companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }
        private val myName = rpc.proxy.nodeInfo().legalIdentities.first().name
        private val proxy = rpc.proxy
    /** Return all the Users of RequestStates **/
@GetMapping(value = "/requestData", produces = arrayOf("application/json"))
private fun requestData(): ResponseEntity<Map<String, Any>> {
    val (status, message) = try {


        val RequestStateAndRefs = rpc.proxy.vaultQueryBy<RequestState>().states
        val RequestStates = RequestStateAndRefs.map { it.state.data }
        val list = RequestStates.map {
            mapOf(
                    "OtherParty"     to  it.OtherParty.name.toString(),
                    "requestParty"     to  it.requestParty.name.toString(),
                    "linearId"      to  it.Id.toString()
            )
        }

        HttpStatus.CREATED to list
    } catch (ex: Exception) { HttpStatus.BAD_REQUEST to "Failed to Request" }
    return ResponseEntity.status(status).body(mapOf("data" to message))
}
}