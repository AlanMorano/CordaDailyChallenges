package com.template.webserver.Controller

import com.template.UserRegisterFlow
import com.template.UserState
import com.template.UserUpdateFlow
import com.template.webserver.*
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
@RequestMapping("/User") // The paths for HTTP requests are relative to this base path.
class UserController(
        private val rpc: NodeRPCConnection) {

    companion object {
        private val logger = loggerFor<UserController>()
    }

    /** Return all the Users of UserStates **/
    @GetMapping(value = "/Users", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun userstate(): Map<String, Any> {
        val UserStateAndRefs = rpc.proxy.vaultQueryBy<UserState>().states
        val UserStates = UserStateAndRefs.map { it.state.data }
        val list = UserStates.map {
            mapOf(
                    "Username"  to it.Username,
                    "Password"  to it.Password,
                    "Firstname" to it.Firstname,
                    "Lastname"  to it.Lastname,
                    "Email"     to it.Email,
                    "Number"    to it.Number,
                    "linear ID" to it.linearId.toString()
            )
        }
        val status = "status" to "Success"
        val message = "message" to "Successful in Returning All UserState"
        return mapOf(status, message, "result" to list)
    }

    /** Return one of the Users of KYCStates **/
    @GetMapping(value = "/Users/{Id}", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun kycUser(@PathVariable("Id") linearID : String,@RequestBody getUser: GetUser): ResponseEntity<Map<String,Any>> {


        val uniqueID = UniqueIdentifier.fromString(linearID)
        val flowHandle = rpc.proxy.vaultQueryBy<UserState>().states
        val data = flowHandle.find { stateAndRef ->
            stateAndRef.state.data.linearId == uniqueID
        }

        return if (data != null) {
            getUser.Username        =   data.state.data.Username
            getUser.Password        =   data.state.data.Password
            getUser.Firstname       =   data.state.data.Firstname
            getUser.Lastname        =   data.state.data.Lastname
            getUser.Email           =   data.state.data.Email
            getUser.Number          =   data.state.data.Number
            getUser.LinearId        =   data.state.data.linearId.id.toString()

            val list = mapOf(
                    "Username"      to  getUser.Username,
                    "Password"      to  getUser.Password,
                    "Firstname"     to  getUser.Firstname,
                    "Lastname"      to  getUser.Lastname,
                    "Email"         to  getUser.Email,
                    "Number"        to  getUser.Number,
                    "LinearId"      to  getUser.LinearId
            )

            ResponseEntity.ok().body(
                    mapOf(
                            "status" to "Success",
                            "message" to "Successful Returning User's Information",
                            "result" to list))
        } else {
            ResponseEntity.badRequest().body(
                    mapOf(
                            "status" to "Failed",
                            "message" to "Failed in Returning User's Information",
                            "result" to "[]")
            )
        }
    }

    /** Register New User in UserStates **/
    @PostMapping(value = "/Register", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun userregister(@RequestBody regsUsers: NewRegsUser): ResponseEntity<Map<String, Any>> {

        return try {
            val flowHandle = rpc.proxy.startFlowDynamic(UserRegisterFlow::class.java,
                    regsUsers.Username,
                    regsUsers.Password,
                    regsUsers.Firstname,
                    regsUsers.Lastname,
                    regsUsers.Email,
                    regsUsers.Number)
            val result = flowHandle.use { it.returnValue.getOrThrow() }

            var password : String = ""
            for(x in regsUsers.Password){
                password += "*"
            }

            val flowResult = mapOf(
                    "Username"          to  regsUsers.Username,
                    "Password"          to  password,
                    "Firstname"         to  regsUsers.Firstname,
                    "Lastname"          to  regsUsers.Lastname,
                    "Email"             to  regsUsers.Email,
                    "Number"            to  regsUsers.Number,
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

    /** Update Users in UserStates **/
    @PostMapping(value = "/Update", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun userupdate(@RequestBody updateUser: NewUpdateUser): ResponseEntity<Map<String, Any>> {

        return try {
            val uniqueID = UniqueIdentifier.fromString(updateUser.ID)
            val flowHandle = rpc.proxy.startFlowDynamic(UserUpdateFlow::class.java,
                    updateUser.NewFirstname,
                    updateUser.NewLastname,
                    updateUser.NewEmail,
                    updateUser.NewNumber,
                    uniqueID)

            val result = flowHandle.use { it.returnValue.getOrThrow() }
            val flowResult = mapOf(
                    "FirstName"         to  updateUser.NewFirstname,
                    "LastName"          to  updateUser.NewLastname,
                    "Email"             to  updateUser.NewEmail,
                    "Number"            to  updateUser.NewNumber,
                    "Transaction ID"    to  result.id.toString()
            )
            ResponseEntity.ok().body(
                    mapOf(
                            "status" to "Success",
                            "message" to "Update Successful",
                            "result" to flowResult)
            )
        } catch (ex: Exception) {
            ResponseEntity.badRequest().body(
                    mapOf(
                            "status" to "Failed",
                            "message" to "Update Failed",
                            "result" to "[]")
            )
        }
    }
}