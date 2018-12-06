package com.template.wew

import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("kyc")
class SuperAPI(private val rpcOps: CordaRPCOps){
    //my party
    private val me = rpcOps.nodeInfo().legalIdentities.first()
    //my party name
    private val myLegalName = me.name
    private val SERVICE_NODE_NAME = CordaX500Name("Notary", "London","GB")
    private val BANK_ONE = CordaX500Name("PartyA", "Manila", "PH")
    private val BANK_TWO = CordaX500Name("PartyB", "Cebu", "PH")

    /**
     * Return node's name
     */
    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    fun whoAmI() = mapOf("me" to myLegalName.organisation)

    /**
     * Returns all parties registered with the network map.
     */
    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = rpcOps.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                .filter { it !in listOf(myLegalName, SERVICE_NODE_NAME, BANK_ONE) })
    }
}
