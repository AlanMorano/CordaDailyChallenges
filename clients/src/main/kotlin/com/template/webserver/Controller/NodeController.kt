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
import net.corda.core.contracts.StateAndRef


private const val CONTROLLER_NAME = "config.controller.name"
//@Value("\${$CONTROLLER_NAME}") private val controllerName: String
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(
        private val rpc: NodeRPCConnection
       ) {
    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }
    private val proxy = rpc.proxy


    private fun KYCState.toJson(): Map<String, Any>{
        return mapOf(
                "node" to node.name.toString(), "name" to name, "age" to age, "address" to address, "birthDate" to birthDate, "status" to status, "religion" to religion,
                "isVerified" to isVerified, "listOfParties" to listOfParties.toString(), "linearId" to linearId.toString()) }
    private fun KYCRequestState.toJson(): Map<String, Any>{
        return mapOf(
                "infoOwner" to infoOwner.name.toString(), "requestor" to requestor.name.toString(), "name" to name,
                "listOfParties" to listOfParties.toString(), "linearId" to linearId.toString()) }
    private fun UserAccountState.toJson(): Map<String, Any>{
        return mapOf(
                "firstName" to firstName, "middleName" to middleName, "lastName" to lastName, "username" to username, "password" to password,
                "email" to email, "role" to role) }

    /**
     *Login
     */

    @PostMapping(value = "/login", produces = arrayOf("application/json"))
    private fun login(
            @RequestParam("username") username: String,
            @RequestParam("password") password: String): ResponseEntity<Map<String, Any>>
    {
        val userStateRefs = rpc.proxy.vaultQueryBy<UserAccountState>().states
        var user : Boolean = false
        var pass : Boolean = false
        var first : String = ""
        var middle : String = ""
        var last : String = ""
        var usern : String = ""
        var role : String = ""

            for(state in userStateRefs){
                if (username==state.state.data.username && password.md5()==state.state.data.password){
                    val x = state.state.data
                    user = true
                    pass = true
                    first = x.firstName
                    middle = x.middleName
                    last = x.lastName
                    usern = x.username
                    role = x.role
                } }
        val(status, message) = try {
            if(user && pass){
                HttpStatus.CREATED to "Login Successful"
            }else
                HttpStatus.BAD_REQUEST to "Login Failed"
        } catch (e: Exception){
            HttpStatus.BAD_REQUEST to "Failed"
        }

        val dataName = mapOf("firstname" to first, "middlename" to middle, "lastname" to last)
        val data = mapOf(
                "username" to usern,
                "role" to role,
                "name" to dataName)
        val result : Any
        result = if(status==HttpStatus.CREATED) data
        else "No data"
        val mess =  mapOf("status" to status,
                "message" to message, "result" to result)
        return ResponseEntity.status(status).body(mess)
    }



    /**
     * Return all KYCState
     */
    @GetMapping(value = "/states/kyc", produces = arrayOf("application/json"))
    private fun getKYCStates(): Map<String, Any>{
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
    @GetMapping(value = "/states/request", produces = arrayOf("application/json"))
    private fun getRequestStates(): Map<String, Any>{

        val requestStateAndRefs = rpc.proxy.vaultQueryBy<KYCRequestState>().states
        val requestStates = requestStateAndRefs.map { it.state.data }
        val list1 = requestStates.map { it.toJson() }
        val status = "status" to "success"
        val message = "message" to "successful in getting ContractState of type KYCRequestState"
        return mapOf(status,message,"result" to list1)
    }

    /**
     * Return all UserAccountState
     */
    @GetMapping(value = "/states/user", produces = arrayOf("application/json"))
    private fun getUserStates(): Map<String, Any>{

        val userStateAndRefs = rpc.proxy.vaultQueryBy<UserAccountState>().states
        val userStates = userStateAndRefs.map { it.state.data }
        val list1 = userStates.map { it.toJson() }
        val status = "status" to "success"
        val message = "message" to "successful in getting ContractState of type UserContractState"
        return mapOf(status,message,"result" to list1)
    }



    /**
     * REGISTER - KYCRegister
     */

    @PostMapping(value = "/states/kyc/create", produces = arrayOf("application/json"))
    private fun createKYC(
            @RequestParam("name") name : String,
            @RequestParam("age") age : Int,
            @RequestParam("address") address : String,
            @RequestParam("birthDate") birthDate: String,
            @RequestParam("status") status : String,
            @RequestParam("religion") religion : String) : ResponseEntity<Map<String, Any>>{

        val (status, message) = try {
            val registerFlow = proxy.startFlowDynamic(
                    KYCRegisterFlow.Initiator::class.java,
                    name, age, address, birthDate, status, religion)
            val result = registerFlow.use { it.returnValue.getOrThrow() }
            HttpStatus.CREATED to "Created new KYCState"
        }catch ( e: Exception) {
            HttpStatus.BAD_REQUEST to "Failed to create new KYCState" }
        val kycStateRef = proxy.vaultQueryBy<KYCState>().states.last()
        val kycState = kycStateRef.state.data.toJson()
        val mess = mapOf("status" to status,
                "message" to message, "result" to kycState)
        return ResponseEntity.status(status).body(mess)
    }

    /**
     * REGISTER - KYCRequest
     */

    @PostMapping(value = "/states/request/create", produces = arrayOf("application/json"))
    private fun createKYCRequest(
            @RequestParam("infoOwner") infoOwner : String,
            @RequestParam("name") name : String) : ResponseEntity<Map<String, Any>>{

        val infoOwnerIdentity = proxy.partiesFromName(infoOwner, exactMatch = false).singleOrNull()
                ?: throw IllegalStateException("No $infoOwner in the network map.")

        val (status, message) = try {
            val registerFlow = proxy.startFlowDynamic(
                    KYCRequestFlow.Initiator::class.java, infoOwnerIdentity.toString(), name)

            val result = registerFlow.use { it.returnValue.getOrThrow() }
            HttpStatus.CREATED to "Created new KYC RequestState"
        }catch ( e: Exception) {
            HttpStatus.BAD_REQUEST to "Failed to create new KYC RequestState"
        }
        val requestStateRef = proxy.vaultQueryBy<KYCRequestState>().states.last()
        val requestState = requestStateRef.state.data.toJson()
        val mess : Any
        mess = if(status==HttpStatus.CREATED) {
            mapOf("status" to status, "message" to message, "result" to requestState)
        } else mapOf("status" to status, "message" to message, "result" to "No data")

        return ResponseEntity.status(status).body(mess)
    }

    /**
     * REGISTER - UserAccountRegisterFlow
     */
    @PostMapping(value = "/states/user/create", produces = arrayOf("application/json"))
    private fun createUser(
            @RequestParam("firstName") firstName : String,
            @RequestParam("middleName") middleName : String,
            @RequestParam("lastName") lastName : String,
            @RequestParam("username") username: String,
            @RequestParam("password") password : String,
            @RequestParam("email") email : String,
            @RequestParam("role") role : String) : ResponseEntity<Map<String, Any>>{


        val (status, message) = try {
            val registerFlow = proxy.startFlowDynamic(
                    UserAccountRegisterFlow.Initiator::class.java, firstName, middleName,
                    lastName, username, password, email, role)
            val result = registerFlow.use { it.returnValue.getOrThrow() }
            HttpStatus.CREATED to "Created new UserAccountState"
        }catch ( e: Exception) {
            HttpStatus.BAD_REQUEST to "Failed to create UserAccountState"
        }
        val userStateRef = proxy.vaultQueryBy<UserAccountState>().states.last()
        val userState = userStateRef.state.data.toJson()
        val mess = mapOf("status" to status,
                "message" to message, "result" to userState)
        return ResponseEntity.status(status).body(mess)
    }










}
