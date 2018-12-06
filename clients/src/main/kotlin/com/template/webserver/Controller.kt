package com.template.webserver

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
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
class Controller( rpc: NodeRPCConnection){

    private val myIdentity = rpc.proxy.nodeInfo().legalIdentities.first().name
    private val proxy = rpc.proxy

    companion object {
        private val logger = loggerFor<Controller>()
    }

    /** Returns the node's name. */
    @GetMapping(value = "/myname", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun myName() = myIdentity.toString()

    @GetMapping(value = "/status", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun status() = "200"

    @GetMapping(value = "/servertime", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun serverTime() = LocalDateTime.ofInstant(proxy.currentNodeTime(), ZoneId.of("UTC")).toString()

    @GetMapping(value = "/addresses", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun addresses() = proxy.nodeInfo().addresses.toString()

    @GetMapping(value = "/identities", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun identities() = proxy.nodeInfo().legalIdentities.toString()

    @GetMapping(value = "/platformversion", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun platformVersion() = proxy.nodeInfo().platformVersion.toString()

    @GetMapping(value = "/peers", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun peers() = proxy.networkMapSnapshot().flatMap { it.legalIdentities }.toString()

    @GetMapping(value = "/notaries", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun notaries() = proxy.notaryIdentities().toString()

    @GetMapping(value = "/flows", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun flows() = proxy.registeredFlows().toString()

    @GetMapping(value = "/states", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun states() = proxy.vaultQueryBy<ContractState>().states.toString()

    @GetMapping(value = "/users", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun users() = proxy.vaultQueryBy<ContractState>(QueryCriteria.VaultQueryCriteria()).states.toString()
    }
