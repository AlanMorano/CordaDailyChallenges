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
class LoginController(
        private val rpc: NodeRPCConnection
) {
    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }
    private val proxy = rpc.proxy

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






}
