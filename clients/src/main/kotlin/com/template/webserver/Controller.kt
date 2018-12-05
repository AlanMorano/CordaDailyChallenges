package com.template.webserver

import net.corda.core.contracts.ContractState
import net.corda.core.messaging.vaultQueryBy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneId


private const val CONTROLLER_NAME = "config.controller.name"
/**
 *  A controller for interacting with the node via RPC.
 */
/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/api") // The paths for HTTP requests are relative to this base path.
class Controller(
        rpc: NodeRPCConnection,
        @Value("\${$CONTROLLER_NAME}") private val controllerName: String ) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }
    private val myName = rpc.proxy.nodeInfo().legalIdentities.first().name

    private val proxy = rpc.proxy

    /** Returns the node's name. */
    @GetMapping(value = "/myname", produces = arrayOf("text/plain"))
    private fun myName() = myName.toString()

    @GetMapping(value = "/status", produces = arrayOf("text/plain"))
    private fun status() = "200"

    @GetMapping(value = "/servertime", produces = arrayOf("text/plain"))
    private fun serverTime() = LocalDateTime.ofInstant(proxy.currentNodeTime(), ZoneId.of("UTC")).toString()

    @GetMapping(value = "/addresses", produces = arrayOf("text/plain"))
    private fun addresses() = proxy.nodeInfo().addresses.toString()

    @GetMapping(value = "/identities", produces = arrayOf("text/plain"))
    private fun identities() = proxy.nodeInfo().legalIdentities.toString()

    @GetMapping(value = "/platformversion", produces = arrayOf("text/plain"))
    private fun platformVersion() = proxy.nodeInfo().platformVersion.toString()

    @GetMapping(value = "/peers", produces = arrayOf("text/plain"))
    private fun peers() = proxy.networkMapSnapshot().flatMap { it.legalIdentities }.toString()

    @GetMapping(value = "/notaries", produces = arrayOf("text/plain"))
    private fun notaries() = proxy.notaryIdentities().toString()

    @GetMapping(value = "/flows", produces = arrayOf("text/plain"))
    private fun flows() = proxy.registeredFlows().toString()

    @GetMapping(value = "/states", produces = arrayOf("text/plain"))
    private fun states() = proxy.vaultQueryBy<ContractState>().states.toString()

}