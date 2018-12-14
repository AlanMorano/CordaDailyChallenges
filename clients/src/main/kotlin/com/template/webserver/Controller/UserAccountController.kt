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
class UserAccountController(
        private val rpc: NodeRPCConnection
) {
    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }
    private val proxy = rpc.proxy


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
          proxy.startFlowDynamic(
                    UserAccountRegisterFlow.Initiator::class.java,
                    user.firstName,
                    user.middleName,
                    user.lastName,
                    user.username,
                    user.password,
                    user.email,
                    user.role
            )
//            val out = registerFlow.use { it.returnValue.getOrThrow() }
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
