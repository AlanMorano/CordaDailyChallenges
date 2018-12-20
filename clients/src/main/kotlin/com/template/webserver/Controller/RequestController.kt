package com.template.webserver.Controller


import com.template.KYCRequestFlow
import com.template.RequestState
import com.template.webserver.GetRequest
import com.template.webserver.NewRegsRequest
import com.template.webserver.NodeRPCConnection
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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

    /** Return all the Request of RequestStates **/
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
        val status = "status" to "Success"
        val message = "message" to "Successful in Returning All RequestState"
        return mapOf(status, message, "result" to list)
    }

    /** Return one of the Request of RequestStates **/
    @GetMapping(value = "/Users/{Id}", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun requestUser(@PathVariable("Id") linearID : String,@RequestBody getRequest: GetRequest): ResponseEntity<Map<String,Any>> {


        val uniqueID = UniqueIdentifier.fromString(linearID)
        val flowHandle = rpc.proxy.vaultQueryBy<RequestState>().states
        val data = flowHandle.find { stateAndRef ->
            stateAndRef.state.data.IdState == uniqueID
        }

        return if (data != null) {
            getRequest.Owner = data.state.data.ownNode.toString()
            getRequest.Request = data.state.data.requestNode.toString()
            getRequest.ID = data.state.data.IdState.id.toString()

            val list = mapOf(
                    "Owner"     to  getRequest.Owner,
                    "Requester" to  getRequest.Request,
                    "ID"        to getRequest.ID
            )

            ResponseEntity.ok().body(
                    mapOf(
                            "status" to "Success",
                            "message" to "Successful Returning Request's Information",
                            "result" to list))
        } else {
            ResponseEntity.badRequest().body(
                    mapOf(
                            "status" to "Failed",
                            "message" to "Failed in Returning Request's Information",
                            "result" to "[]")
            )
        }
    }

    /** Register New Request in RequestState **/
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
                    "linearID"          to  requestState.IdState.toString(),
                    "listOfParties"     to  requestState.participants.toString(),
                    "Transaction ID"    to  result.id.toString()
            )

            ResponseEntity.ok().body(
                    mapOf(
                            "status" to "Success",
                            "message" to "Register Successful",
                            "result" to flowResult)
            )
        } catch (ex: Exception) {

            ResponseEntity.badRequest().body(
                    mapOf(
                            "status" to "Failed",
                            "message" to "Register Failed",
                            "result" to "[]")
            )
        }
    }
}

