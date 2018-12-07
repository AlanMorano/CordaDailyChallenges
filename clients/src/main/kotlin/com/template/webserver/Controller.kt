package com.template.webserver

import com.template.UserState
import net.corda.core.contracts.hash
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.loggerFor
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneId


/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/api") // The paths for HTTP requests are relative to this base path.
class Controller(
    private val rpc: NodeRPCConnection) {

        companion object {
            private val logger = loggerFor<Controller>()
        }

    private val myIdentity = rpc.proxy.nodeInfo().legalIdentities.first().name
    private val rpcOps = rpc.proxy

    /** Maps a UserState to a JSON object. */
    private fun UserState.toJson(): Map<String, Any> {
        return mapOf(
                "Party" to Node.name.organisation,
                "name" to Name,
                "age" to Age,
                "address" to Address,
                "birthdate" to BirthDate,
                "status" to Status,
                "religion" to Religion,
                "linead Id" to linearId,
                "list parties" to parties.toString(),
                "notary" to notaries(),
                "txhash" to hashCode(),
                "txhasss" to hash()
        )
    }
    private fun UserState.toName(): Map<String, Any>{
        return mapOf(
                "name" to Name
        )
    }

    /** Returns the node's name. */
    @GetMapping(value = "/myname", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun myName() = myIdentity.toString()

    @GetMapping(value = "/status", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun status() = "200"

    @GetMapping(value = "/servertime", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun serverTime() = LocalDateTime.ofInstant(rpcOps.currentNodeTime(), ZoneId.of("UTC")).toString()

    @GetMapping(value = "/addresses", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun addresses() = rpcOps.nodeInfo().addresses.toString()

    @GetMapping(value = "/identities", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun identities() = rpcOps.nodeInfo().legalIdentities.toString()

    @GetMapping(value = "/platformversion", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun platformVersion() = rpcOps.nodeInfo().platformVersion.toString()

    @GetMapping(value = "/peers", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun peers() = rpcOps.networkMapSnapshot().flatMap { it.legalIdentities }.toString()

    @GetMapping(value = "/notaries", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun notaries() = rpcOps.notaryIdentities().toString()

    @GetMapping(value = "/flows", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun flows() = rpcOps.registeredFlows().toString()

    /** Returns a list of existing User's. */
    @GetMapping(value = "/users", produces = arrayOf("application/json"))
    private fun details(): List<Map<String, Any>> {
        val userStateAndRefs = rpc.proxy.vaultQueryBy<UserState>().states
        val userStates = userStateAndRefs.map { it.state.data }
        return userStates.map { it.toJson() }
    }

    @GetMapping(value = "/getnames", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun getnames() : Map<String, List<String>>{
        val userStateAndRef = rpc.proxy.vaultQueryBy<UserState>().states
        val userStates = userStateAndRef.map { it.state.data }
        val statename = userStates.map { it.Name }
        return mapOf("name" to statename)
    }

    /** Returns a list of existing User's. */
    @GetMapping(value = "/getNames", produces = arrayOf("application/json"))
    private fun getNames(): List<Map<String, Any>> {
        val userStateAndRefs = rpc.proxy.vaultQueryBy<UserState>().states
        val userStates = userStateAndRefs.map { it.state.data }
        return userStates.map { it.toName() }
    }
}
