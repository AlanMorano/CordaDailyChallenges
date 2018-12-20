package com.template.webserver.Controller

import com.template.KYCShareFlow
import com.template.KYCState
import com.template.RequestState
import com.template.webserver.NewShare
import com.template.webserver.NodeRPCConnection
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.messaging.vaultQueryBy
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
@RequestMapping("/Admin") // The paths for HTTP requests are relative to this base path.
class ShareController(
        private val rpc: NodeRPCConnection) {

    companion object {
        private val logger = loggerFor<ShareController>()
    }

    /** Verify New Share in RequestStates **/
    @PostMapping(value = "/Share", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun shared(@RequestBody share: NewShare) : ResponseEntity<Map<String,Any>>{

    return try {

            val OwnerIdentity = rpc.proxy.partiesFromName(share.Owner, exactMatch = false).singleOrNull()
                ?: throw IllegalStateException("No ${share.Owner} in the network map.")
            val uniqueID = UniqueIdentifier.fromString(share.ID)

            val flowHandle = rpc.proxy.startFlowDynamic(KYCShareFlow::class.java,
                    OwnerIdentity.name.organisation,
                    uniqueID)
            val result = flowHandle.use { it.returnValue.getOrThrow() }
            val flowResult = mapOf(
                    "Shared Data"       to  OwnerIdentity.toString(),
                    "ID"                to  uniqueID.toString(),
                    "Transaction ID"    to  result.id.toString()
            )

            ResponseEntity.ok().body(
                    mapOf(
                            "status" to "Success",
                            "message" to "User's Share Successful",
                            "result" to flowResult))

        } catch (ex: Exception) {
            ResponseEntity.badRequest().body(
                    mapOf(
                    "status" to "Failed",
                    "message" to "Share Failed",
                    "result" to "[]")
            )
        }
    }
}