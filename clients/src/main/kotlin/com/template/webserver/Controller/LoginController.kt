package com.template.webserver.Controller

import com.template.UserState
import com.template.webserver.Logs
import com.template.webserver.NodeRPCConnection
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.loggerFor
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class LoginController(
        private val rpc: NodeRPCConnection) {

    companion object {
        private val logger = loggerFor<LoginController>()
    }

    /** Log-in of the Users in UserStates **/
    @PostMapping(value = "/Login", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun Authentication(@RequestBody logs: Logs): ResponseEntity<Map<String, Any>> {

        val flowHandle = rpc.proxy.vaultQueryBy<UserState>().states
        val user = flowHandle.find { stateAndRef ->
            stateAndRef.state.data.Username == logs.Username &&
                    stateAndRef.state.data.Password == logs.Password
        }

        return if (user != null) {
            val result = mapOf(
                    "Username" to user.state.data.Username,
                    "Password" to user.state.data.Password,
                    "Firstname" to user.state.data.Firstname,
                    "Lastname" to user.state.data.Lastname,
                    "Email" to user.state.data.Email,
                    "Number" to user.state.data.Number,
                    "linear ID" to user.state.data.linearId
            )

            ResponseEntity.ok().body(
                    mapOf(
                            "status" to "success",
                            "message" to "Successful Log-in",
                            "result" to result))
        } else {
            ResponseEntity.badRequest().body(
                    mapOf(
                            "status" to "failed",
                            "message" to "Failed to login",
                            "result" to "[]")
            )
        }
    }
}