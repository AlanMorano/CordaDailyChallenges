package com.template.webserver

import com.template.flow.KYCRegisterFlow
import com.template.states.KYCRequestState
import com.template.states.KYCState
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


private const val CONTROLLER_NAME = "config.controller.name"
/**
 *  A controller for interacting with the node via RPC.
 */
/**
 * Define your API endpoints here.
 */


@RestController
@RequestMapping("/kyc") // The paths for HTTP requests are relative to this base path.
class Controller(
        private val rpc: NodeRPCConnection,
        @Value("\${$CONTROLLER_NAME}") private val controllerName: String ) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }
    private val myIdentity = rpc.proxy.nodeInfo().legalIdentities.first().name

    private val proxy = rpc.proxy

    private fun KYCState.toJson(): Map<String, Any>{
        return mapOf(
                "node" to node.name.toString(),
                "name" to name,
                "age" to age,
                "address" to address,
                "birthDate" to birthDate,
                "status" to status,
                "religion" to religion,
                "isVerified" to isVerified,
                "listOfParties" to listOfParties.toString(),
                "linearId" to linearId.toString()

        )
    }
    private fun KYCRequestState.toJson(): Map<String, Any>{
        return mapOf(
                "infoOwner" to infoOwner.name.toString(),
                "requestor" to requestor.name.toString(),
                "name" to name,
                "listOfParties" to listOfParties.toString(),
                "linearId" to linearId.toString()
        )
    }


    /**
     * Returns status
     */
    @GetMapping(value = "/status", produces = arrayOf("application/json"))
    @ResponseBody
    private fun status() = mapOf("status" to "200")

    /**
     * Returns server time
     */
    @GetMapping(value = "/servertime", produces = arrayOf("application/json") )
    private fun getServerTime(): Map<String, Any>{
        val currentDateTime = LocalDateTime.now()
        val date = currentDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
        val time = currentDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM))
        return mapOf("date" to date, "time" to time)

    }

    /** Returns the node's name. */
    @GetMapping(value = "/me", produces = arrayOf("application/json"))
    private fun myName() = mapOf("me" to myIdentity.toString())

    @GetMapping(value = "/peers", produces = arrayOf("application/json"))
    private fun peersNames(): Map<String, List<String>> {
        val nodes = proxy.networkMapSnapshot()
        val nodeNames = nodes.map {
            it.legalIdentities.first().name
        }
        val filteredNodeNames = nodeNames.filter {
            it.organisation  !in listOf(controllerName, myIdentity)
        }
        val filteredNodeNamesToStr = filteredNodeNames.map {
            it.toString()
        }
        return mapOf("peers" to filteredNodeNamesToStr)
    }

    /**
     * Return all KYCState
     */
    @GetMapping(value = "/userstates", produces = arrayOf("application/json"))
    private fun getUserStates(): Map<String, Any>{
        val userStateAndRefs = rpc.proxy.vaultQueryBy<KYCState>().states
        val userStates = userStateAndRefs.map { it.state.data }
        val list1 = userStates.map { it.toJson() }
        val status = "status" to "success"
        val message = "message" to "successful in getting ContractState of type KYCState"
        return mapOf(status,message, "result" to list1)
    }

    /**
     * Return all KYCRequestState
     */
    @GetMapping(value = "/requeststates", produces = arrayOf("application/json"))
    private fun getRequestStates(): Map<String, Any>{

        val requestStateAndRefs = rpc.proxy.vaultQueryBy<KYCRequestState>().states
        val requestStates = requestStateAndRefs.map { it.state.data }
        val list1 = requestStates.map { it.toJson() }
        val status = "status" to "success"
        val message = "message" to "successful in getting ContractState of type KYCState"
        return mapOf(status,message,"result" to list1)
    }

    /**
     *Login
     */
    @PostMapping(value = "/login", produces = arrayOf("application/json"))
    private fun login(
            @RequestParam("username") username: String,
            @RequestParam("password") password: String): ResponseEntity<Map<String, Any>>
    {
        val(status, message) = try {
            if(username == "testuser" && password == "testpass"){
            HttpStatus.CREATED to "Login Success"
            }else
                HttpStatus.BAD_REQUEST to "Login Failed"
        } catch (e: Exception){
                HttpStatus.BAD_REQUEST to "Failed"
    }

        val dummyName = mapOf("firstname" to "Xtian", "middlename" to "Pogi", "lastname" to "Dismaya")
        val dummyData = mapOf(
                "username" to "testuser",
                "accountId" to "12345678",
                "name" to dummyName)
        val result : Any
       if(status==HttpStatus.CREATED) result = dummyData
        else result = "No data"
      val mess =  mapOf("status" to status,
              "message" to message, "result" to result)

        return ResponseEntity.status(status).body(mess)
    }


    @PostMapping(value = "/user", produces = arrayOf("application/json"))
    private fun create(
            @RequestParam("name") name : String,
            @RequestParam("age") age : Int,
            @RequestParam("address") address : String,
            @RequestParam("birthDate") birthDate: String,
            @RequestParam("status") status : String,
            @RequestParam("religion") religion : String) : ResponseEntity<Map<String, Any>>{

        val (status, message) = try {
            val registerFlow = proxy.startFlowDynamic(
                    KYCRegisterFlow.Initiator::class.java,
                    name,
                    age,
                    address,
                    birthDate,
                    status,
                    religion
            )
            val result = registerFlow.use { it.returnValue.getOrThrow() }
            HttpStatus.CREATED to "Created new userstate"
        }catch ( e: Exception) {
            HttpStatus.BAD_REQUEST to "Failed new create"
        }
        return ResponseEntity.status(status).body(mapOf("status" to message))
    }











}
