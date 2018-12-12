package com.template.webserver

import com.template.flow.KYCRegisterFlow
import com.template.flow.KYCRequestFlow
import com.template.flow.UserAccountRegisterFlow
import com.template.states.KYCRequestState
import com.template.states.KYCState
import com.template.states.UserAccountState
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


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



    /**
     * Return all KYCState
     */
    @GetMapping(value = "/kycstates", produces = arrayOf("application/json"))
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
    @GetMapping(value = "/requeststates", produces = arrayOf("application/json"))
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
    @GetMapping(value = "/userstates", produces = arrayOf("application/json"))
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

    @PostMapping(value = "/kyc", produces = arrayOf("application/json"))
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
     * REGISTER - KYCRequestRegister
     */

    @PostMapping(value = "/request", produces = arrayOf("application/json"))
    private fun createKYCRequest(
            @RequestParam("infoOwner") infoOwner : String,
            @RequestParam("name") name : String) : ResponseEntity<Map<String, Any>>{
        val (status, message) = try {
            val registerFlow = proxy.startFlowDynamic(
                    KYCRequestFlow.Initiator::class.java, infoOwner, name)
            val result = registerFlow.use { it.returnValue.getOrThrow() }
            HttpStatus.CREATED to "Created new KYC RequestState"
        }catch ( e: Exception) {
            HttpStatus.BAD_REQUEST to "Failed to create new KYC RequestState"
        }
        val requestStateRef = proxy.vaultQueryBy<KYCRequestState>().states.last()
        val requestState = requestStateRef.state.data.toJson()
        val mess = mapOf("status" to status,
                "message" to message, "result" to requestState)
        return ResponseEntity.status(status).body(mess)

    }

    /**
     * REGISTER - UserAccountRegisterFlow
     */
    @PostMapping(value = "/user", produces = arrayOf("application/json"))
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
