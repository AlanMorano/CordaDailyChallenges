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
                    "Username" to it.Username,
                    "Password" to it.Password,
                    "Firstname" to it.Firstname,
                    "Lastname" to it.Lastname,
                    "Email" to it.Email,
                    "Number" to it.Number,
                    "linear ID" to it.linearId.toString()
            )
        }
        val status = "status" to "success"
        val message = "message" to "Successful in getting all AccountState"
        return mapOf(status, message, "result" to list)
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
            val flowResult = mapOf(
                    "Username"          to  regsUsers.Username,
                    "Password"          to  "***",
                    "Firstname"         to  regsUsers.Firstname,
                    "Lastname"          to  regsUsers.Lastname,
                    "Email"             to  regsUsers.Email,
                    "Number"            to  regsUsers.Number,
                    "Transaction ID"    to  result.id.toString()
            )
            ResponseEntity.ok().body(
                    mapOf(
                            "status" to "success",
                            "message" to "Successful Registered",
                            "result" to flowResult)
            )
        } catch (ex: Exception) {
            ResponseEntity.badRequest().body(
                    mapOf(
                            "status" to "Failed",
                            "message" to "Failed Registered",
                            "result" to "[]"))
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
                            "status" to "success",
                            "message" to "Successful Registered",
                            "result" to flowResult)
            )
        } catch (ex: Exception) {
            ResponseEntity.badRequest().body(mapOf("status" to "Failed", "message" to "Failed Registered",
                    "result" to "[]"))
        }
    }
}