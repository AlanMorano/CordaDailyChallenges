package com.template.webserver.Controller

import com.template.KYCShareFlow
import com.template.webserver.NewShare
import com.template.webserver.NodeRPCConnection
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.utilities.getOrThrow
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
class ShareController(
        private val rpc: NodeRPCConnection) {

    companion object {
        private val logger = loggerFor<ShareController>()
    }

    /** Verify New Share in RequestStates **/
    @PostMapping(value = "/Share", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun share(@RequestBody share: NewShare) : ResponseEntity<Map<String,Any>>{

        return try {

            val uniqueID = UniqueIdentifier.fromString(share.ID)

            val flowHandle = rpc.proxy.startFlowDynamic(KYCShareFlow::class.java,
                    uniqueID)

            val result = flowHandle.use { it.returnValue.getOrThrow() }
            val flowResult = mapOf(
                    "Shared Data"       to  share.ID,
                    "Transaction ID"    to  result.id.toString()
            )

            ResponseEntity.ok().body(
                    mapOf(
                            "status" to "success",
                            "message" to "Successful Registered",
                            "result" to flowResult))

        } catch (ex: Exception){
            ResponseEntity.badRequest().body(
                    mapOf(
                    "status" to "Failed",
                    "message" to "Failed Registered",
                    "result" to "[]")
            )
        }
    }
}