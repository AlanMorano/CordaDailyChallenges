package com.template.webserver.Controller

import com.template.flow.KYCRegisterFlow
import com.template.flow.KYCRequestFlow
import com.template.flow.UserAccountRegisterFlow
import com.template.states.KYCRequestState
import com.template.states.KYCState
import com.template.states.UserAccountState
import com.template.webserver.NodeRPCConnection
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.template.flow.Encryption.md5
import com.template.models.*
import net.corda.core.contracts.StateAndRef
import javax.servlet.http.HttpServletRequest


private const val CONTROLLER_NAME = "config.controller.name"
//@Value("\${$CONTROLLER_NAME}") private val controllerName: String
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class RequestController(
        private val rpc: NodeRPCConnection
) {
    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }
    private val proxy = rpc.proxy

    /**
     * Return all KYCRequestState
     */

    @GetMapping(value = "/states/request", produces = arrayOf("application/json"))
    private fun getRequestStates() : ResponseEntity<Map<String,Any>>{
        val (status, result ) = try {
            val requestStateRef = rpc.proxy.vaultQueryBy<KYCRequestState>().states
            val requestStates = requestStateRef.map { it.state.data }
            val list = requestStates.map {
                requestModel(
                        infoOwner = it.infoOwner.name.toString(),
                        requestor = it.requestor.toString(),
                        name = it.name,
                        listOfParties = it.listOfParties.toString(),
                        linearId = it.linearId.toString()
                )
            }
            HttpStatus.CREATED to list
        }catch( e: Exception){
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status==HttpStatus.CREATED){
            "message" to "Successful in getting ContractState of type KYCRequestState"}
        else{ "message" to "Failed to get ContractState of type KYCRequestState"}
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat,mess,res))

    }


    /**
     * REGISTER - KYCRequest
     */

    @PostMapping(value = "/states/request/create", produces = arrayOf("application/json"))
    private fun createRequset(@RequestBody createRequest: createRequest) : ResponseEntity<Map<String,Any>> {

        val (status, result) = try {
            val request = createRequest(
                    infoOwner = createRequest.infoOwner,
                    name = createRequest.name
            )
            val infoOwnerIdentity = proxy.partiesFromName(request.infoOwner, exactMatch = false).singleOrNull()
                    ?: throw IllegalStateException("No ${request.infoOwner} in the network map.")

           proxy.startFlowDynamic(
                    KYCRequestFlow.Initiator::class.java,
                    infoOwnerIdentity.name.organisation,
                    request.name
            )
//            val out = registerFlow.use { it.returnValue.getOrThrow() }
            val requestStateRef = proxy.vaultQueryBy<KYCRequestState>().states.last()
            val requestStateData = requestStateRef.state.data
            val list = requestModel(
                    infoOwner = requestStateData.infoOwner.toString(),
                    requestor = requestStateData.requestor.toString(),
                    name = requestStateData.name,
                    listOfParties = requestStateData.listOfParties.toString(),
                    linearId = requestStateData.linearId.toString()
            )
            HttpStatus.CREATED to list
        }catch (e: Exception){
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status==HttpStatus.CREATED){
            "message" to "Successful in creating ContractState of type KYCRequestState"}
        else{ "message" to "Failed to create ContractState of type KYCRequestState"}
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat,mess,res))

    }



}
