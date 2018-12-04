package com.template.client

import com.template.states.UserState
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.contracts.StateAndRef
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.loggerFor
import org.eclipse.jetty.server.Authentication
import org.slf4j.Logger


fun main(args: Array<String>){
    sampleClientRPC().main(args)
}

private class sampleClientRPC{

    companion object{
        val logger: Logger = loggerFor<sampleClientRPC>()
        private fun logState(state: StateAndRef<UserState>) = logger.info("{}", state.state.data)
    }

    fun main(args: Array<String>){
        require(args.size == 1){"Usage: sampleClientRPC <node address>"}
        val nodeAddress = NetworkHostAndPort.parse(args[0])
        val client = CordaRPCClient(nodeAddress)

        val proxy = client.start("user1","test").proxy

        val (snapshot, updates)= proxy.vaultTrack(UserState::class.java)

        snapshot.states.forEach{ logState(it)}
        updates.toBlocking().subscribe{ update ->}


    }
}