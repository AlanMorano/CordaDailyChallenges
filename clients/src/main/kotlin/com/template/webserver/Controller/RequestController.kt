package com.template.webserver.Controller

import com.template.KYCRegisterFlow
import com.template.KYCRequestFlow
import com.template.KYCState
import com.template.RequestState
import com.template.webserver.NewRegsKYC
import com.template.webserver.NewRegsRequest
import com.template.webserver.NodeRPCConnection
import net.corda.client.jackson.JacksonSupport
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.IllegalArgumentException
import java.security.acl.Owner

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/Request") // The paths for HTTP requests are relative to this base path.
class RequestController(
        private val rpc: NodeRPCConnection) {

    companion object {
        private val logger = loggerFor<RequestController>()
    }

    /** Return all the Users of RequestStates **/
    @GetMapping(value = "/Request", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun requeststate(): Map<String, Any> {

        val RequestStateAndRefs = rpc.proxy.vaultQueryBy<RequestState>().states
        val RequestStates = RequestStateAndRefs.map { it.state.data }
        val list = RequestStates.map {
            mapOf(
                    "infoOwner"     to  it.ownNode.name.toString(),
                    "requestor"     to  it.requestNode.name.toString(),
                    "linearId"      to  it.IdState.toString()
            )
        }
        val status = "status" to "success"
        val message = "message" to "Successful in getting all GetState"
        return mapOf(status, message, "result" to list)
    }

    /** Register New User in RequestState **/
    @PostMapping(value = "/Register", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun requestregister(@RequestBody regsRequest: NewRegsRequest): ResponseEntity<Map<String, Any>> {



        return try {

            val OwnerIdentity = rpc.proxy.partiesFromName(regsRequest.Owner, exactMatch = false).singleOrNull()
                    ?: throw IllegalStateException("No ${regsRequest.Owner} in the network map.")
            val uniqueID = UniqueIdentifier.fromString(regsRequest.ID)

            val flowHandle = rpc.proxy.startFlowDynamic(KYCRequestFlow::class.java,
                    OwnerIdentity.name.organisation,
                    uniqueID)
            val result = flowHandle.use { it.returnValue.getOrThrow() }

            val requestStateAndRef = rpc.proxy.vaultQueryBy<RequestState>().states.last()
            val requestState = requestStateAndRef.state.data
            val flowResult = mapOf(
                    "Owner"             to  requestState.ownNode.toString(),
                    "Requestor"         to  requestState.requestNode.toString(),
                    "linearID"                to  requestState.IdState.toString(),
                    "listOfParties"     to  requestState.participants.toString(),
                    "Transaction ID"    to  result.id.toString()
            )

            ResponseEntity.ok().body(
                    mapOf(
                            "status" to "success",
                            "message" to "Successful Registered",
                            "result" to flowResult))
        } catch (ex: Exception) {

            ResponseEntity.badRequest().body(
                    mapOf(
                            "status" to "Failed",
                            "message" to "Failed to Request KYC users",
                            "result" to "[]")
            )
        }
    }
}

