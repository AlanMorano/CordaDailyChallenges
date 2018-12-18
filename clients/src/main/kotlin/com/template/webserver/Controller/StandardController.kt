package com.template.webserver.Controller

import com.template.webserver.NodeRPCConnection
import net.corda.core.utilities.loggerFor
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneId

@RestController
@RequestMapping("/Standard") // The paths for HTTP requests are relative to this base path.
class StandardController(
        private val rpc: NodeRPCConnection) {

    companion object {
        private val logger = loggerFor<StandardController>()
    }

    private val myIdentity = rpc.proxy.nodeInfo().legalIdentities.first().name
    private val rpcOps = rpc.proxy

    /** Returns the node's name. */
    @GetMapping(value = "/MyName", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun myName() = myIdentity.toString()

    /** Get the Status **/
    @GetMapping(value = "/Status", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun status() = "200"

    /** Get the Servertime **/
    @GetMapping(value = "/ServerTime", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun serverTime() = LocalDateTime.ofInstant(rpcOps.currentNodeTime(), ZoneId.of("UTC")).toString()

    /** Get the Address **/
    @GetMapping(value = "/Addresses", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun addresses() = rpcOps.nodeInfo().addresses.toString()

    /** Get the PlatFormVersion **/
    @GetMapping(value = "/PlatformVersion", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun platformVersion() = rpcOps.nodeInfo().platformVersion.toString()

    /** Get all the Peers **/
    @GetMapping(value = "/Peers", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun peers() = rpcOps.networkMapSnapshot().flatMap { it.legalIdentities }.toString()

    /** Get all the notaries **/
    @GetMapping(value = "/Notaries", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun notaries() = rpcOps.notaryIdentities().toString()

    /** Get all the Flows **/
    @GetMapping(value = "/Flows", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun flows() = rpcOps.registeredFlows().toString()

}

