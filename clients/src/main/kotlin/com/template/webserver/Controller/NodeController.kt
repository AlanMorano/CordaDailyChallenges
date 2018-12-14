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
class Controller(
        private val rpc: NodeRPCConnection
       ) {
    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }
    private val proxy = rpc.proxy


    private fun KYCRequestState.toJson(): Map<String, Any>{
        return mapOf(
                "infoOwner" to infoOwner.name.toString(),
                "requestor" to requestor.name.toString(),
                "name" to name,
                "listOfParties" to listOfParties.toString(),
                "linearId" to linearId.toString()) }
    private fun UserAccountState.toJson(): Map<String, Any>{
        return mapOf(
                "firstName" to firstName,
                "middleName" to middleName,
                "lastName" to lastName,
                "username" to username,
                "password" to password,
                "email" to email,
                "role" to role) }

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
            private fun getKYCStates() : ResponseEntity<Map<String,Any>>{
        val (status, result ) = try {
            val kycStateRef = rpc.proxy.vaultQueryBy<KYCState>().states
            val kycStates = kycStateRef.map { it.state.data }
            val list = kycStates.map {
                kycModel(
                        node = it.node.name.toString(),
                        name = it.name,
                        age = it.age,
                        address = it.address,
                        birthday = it.birthDate,
                        status = it.status,
                        religion = it.religion,
                        isVerified = it.isVerified,
                        listOfParties = it.listOfParties.toString(),
                        linearId = it.linearId.toString())
            }
            HttpStatus.CREATED to list
        }catch( e: Exception){
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status==HttpStatus.CREATED){
          "message" to "Successful in getting ContractState of type KYCState"}
        else{ "message" to "Failed to get ContractState of type KYCState"}
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat,mess,res))


    }

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
     * Return all UserAccountState
     */
    @GetMapping(value = "/states/user", produces = arrayOf("application/json"))
    private fun getUserAccountStates() : ResponseEntity<Map<String,Any>>{
        val (status, result ) = try {
            val requestStateRef = rpc.proxy.vaultQueryBy<UserAccountState>().states
            val requestStates = requestStateRef.map { it.state.data }
            val list = requestStates.map {
                userAccountModel(
                        firstName = it.firstName,
                        middleName = it.middleName,
                        lastName = it.lastName,
                        username = it.username,
                        password = it.password,
                        email = it.email,
                        role = it.role
                )
            }
            HttpStatus.CREATED to list
        }catch( e: Exception){
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status==HttpStatus.CREATED){
            "message" to "Successful in getting ContractState of type UserAccountState"}
        else{ "message" to "Failed to get ContractState of type UserAccountState"}
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat,mess,res))
    }



    /**
     * REGISTER - KYCRegister
     */

    @PostMapping(value = "/states/kyc/create", produces = arrayOf("application/json"))
    private fun createKYC(@RequestBody createKYC: createKYC) : ResponseEntity<Map<String,Any>> {

        val (status, result) = try {
            val kyc = createKYC(
                        name = createKYC.name,
                        age = createKYC.age,
                        address = createKYC.address,
                        birthday = createKYC.birthday,
                        status = createKYC.status,
                        religion = createKYC.religion
            )

            val registerFlow = proxy.startFlowDynamic(
                    KYCRegisterFlow.Initiator::class.java,
                    kyc.name,
                    kyc.age,
                    kyc.address,
                    kyc.birthday,
                    kyc.status,
                    kyc.religion
            )
            val out = registerFlow.use { it.returnValue.getOrThrow() }
            val kycStateRef = proxy.vaultQueryBy<KYCState>().states.last()
            val kycStateData = kycStateRef.state.data
            val list = kycModel(
                    node = kycStateData.node.name.toString(),
                    name = kycStateData.name,
                    age = kycStateData.age,
                    address = kycStateData.address,
                    birthday = kycStateData.birthDate,
                    status = kycStateData.status,
                    religion = kycStateData.religion,
                    isVerified = kycStateData.isVerified,
                    listOfParties = kycStateData.listOfParties.toString(),
                    linearId = kycStateData.linearId.toString()
            )
            HttpStatus.CREATED to list
        }catch (e: Exception){
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status==HttpStatus.CREATED){
            "message" to "Successful in creating ContractState of type KYCState"}
        else{ "message" to "Failed to create ContractState of type KYCState"}
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

            val registerFlow = proxy.startFlowDynamic(
                    KYCRequestFlow.Initiator::class.java,
                   infoOwnerIdentity.name.organisation,
                    request.name
            )
            val out = registerFlow.use { it.returnValue.getOrThrow() }
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

    /**
     * REGISTER - UserAccountRegisterFlow
     */

    @PostMapping(value = "/states/user/create", produces = arrayOf("application/json"))
    private fun createUser(@RequestBody createUserAccount: createUserAccount) : ResponseEntity<Map<String,Any>> {

        val (status, result) = try {
            val user = createUserAccount(
                 firstName = createUserAccount.firstName,
                    middleName = createUserAccount.middleName ,
                    lastName = createUserAccount.lastName,
                    username = createUserAccount.username,
                    password = createUserAccount.password,
                    email = createUserAccount.email,
                    role = createUserAccount.role
            )
            val registerFlow = proxy.startFlowDynamic(
                    UserAccountRegisterFlow.Initiator::class.java,
                    user.firstName,
                    user.middleName,
                    user.lastName,
                    user.username,
                    user.password,
                    user.email,
                    user.role
            )
            val out = registerFlow.use { it.returnValue.getOrThrow() }
            val userStateRef = proxy.vaultQueryBy<UserAccountState>().states.last()
            val userStateData = userStateRef.state.data
            val list = userAccountModel(
                  firstName = userStateData.firstName,
                    middleName = userStateData.middleName,
                    lastName = userStateData.lastName,
                    username = userStateData.username,
                    password = userStateData.password,
                    email = userStateData.email,
                    role = userStateData.role
            )
            HttpStatus.CREATED to list
        }catch (e: Exception){
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status==HttpStatus.CREATED){
            "message" to "Successful in creating ContractState of type UserAccountState"}
        else{ "message" to "Failed to create ContractState of type UserAccountState"}
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat,mess,res))
    }









}
