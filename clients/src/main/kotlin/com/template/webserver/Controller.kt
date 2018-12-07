package com.template.webserver

import com.template.states.UserState
import net.corda.core.contracts.ContractState
import net.corda.core.messaging.vaultQueryBy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneId
import javax.ws.rs.core.MediaType
import net.corda.client.jackson.JacksonSupport
import net.corda.core.contracts.StateAndRef




private const val CONTROLLER_NAME = "config.controller.name"
/**
 *  A controller for interacting with the node via RPC.
 */
/**
 * Define your API endpoints here.
 */


@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(
        private val rpc: NodeRPCConnection,
        @Value("\${$CONTROLLER_NAME}") private val controllerName: String ) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }
    private val myIdentity = rpc.proxy.nodeInfo().legalIdentities.first().name

    private val proxy = rpc.proxy


    @GetMapping(value = "/status", produces = arrayOf("application/json"))
    private fun status() = mapOf("status" to "200")

    @GetMapping(value = "/servertime", produces = arrayOf("application/json"))
    private fun serverTime() = LocalDateTime.ofInstant(proxy.currentNodeTime(), ZoneId.of("UTC")).toString()

    @GetMapping(value = "/addresses", produces = arrayOf("application/json"))
    private fun addresses() = proxy.nodeInfo().addresses.toString()

    @GetMapping(value = "/identities", produces = arrayOf("application/json"))
    private fun identities() = proxy.nodeInfo().legalIdentities.toString()

    @GetMapping(value = "/platformversion", produces = arrayOf("application/json"))
    private fun platformVersion() = proxy.nodeInfo().platformVersion.toString()

    @GetMapping(value = "/peers", produces = arrayOf("application/json"))
    private fun peers() = proxy.networkMapSnapshot().flatMap { it.legalIdentities }.toString()

    @GetMapping(value = "/notaries", produces = arrayOf("application/json"))
    private fun notaries() = proxy.notaryIdentities().toString()

    @GetMapping(value = "/flows", produces = arrayOf("application/json"))
    private fun flows() = proxy.registeredFlows().toString()

    @GetMapping(value = "/states", produces = arrayOf("application/json"))
    private fun states() = proxy.vaultQueryBy<UserState>().states.toString()


//    private fun UserState.toJson(): Map<String, String>{
//        return mapOf("node" to node.toString(), "name" to name, "age" to age.toString())
//    }

    /** Returns the node's name. */
    @GetMapping(value = "/me", produces = arrayOf("application/json"))
    private fun myName() = mapOf("me" to myIdentity.organisation)

    @GetMapping(value = "/peersnames", produces = arrayOf("application/json"))
    private fun peersNames(): Map<String, List<String>> {
        val nodes = rpc.proxy.networkMapSnapshot()
        val nodeNames = nodes.map {
            it.legalIdentities.first().name
        }
        val filteredNodeNames = nodeNames.filter {
            it.organisation !in listOf(controllerName, myIdentity)
        }
        val filteredNodeNamesToStr = filteredNodeNames.map {
            it.toString()
        }
        return mapOf("peers" to filteredNodeNamesToStr)
    }

    @GetMapping(value = "/userStates", produces = arrayOf("application/json"))
    private fun getUserStates(): Map<String, List<UserState>>{




        val userStateAndRefs = proxy.vaultQueryBy<UserState>().states

        val mapper = JacksonSupport.createNonRpcMapper()
        val json = mapper.writeValueAsString(userStateAndRefs)

        val userStates = userStateAndRefs.map { it.state.data }

        val userStateData = userStates.map { state ->
            UserState(state.node,
                    state.name,
                    state.age,
                    state.address,
                    state.birthDate,
                    state.status,
                    state.religion,
                    state.isVerified,
                    state.listOfParties,
                    state.linearId)
        }
        return mapOf("userStates" to  userStateData)
    }

}
