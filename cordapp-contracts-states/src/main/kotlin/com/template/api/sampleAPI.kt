package com.template.api


import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import javax.ws.rs.*
import javax.ws.rs.core.MediaType



@Path("sample")
class sampleAPI (private val corp: CordaRPCOps){


    private val me = corp.nodeInfo().legalIdentities.first()
    private val legalName = me.name
    private val SERVICES_NAME = CordaX500Name("Notary","London","GB")
    private val NodeA = CordaX500Name("PartyA","Manila","PH")
    private val NodeB = CordaX500Name("PartyB","Cebu","PH")

    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    fun whoami() = mapOf("me" to legalName.organisation)


    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPeers():Map<String, List<CordaX500Name>>{
        val nodeInfo = corp.networkMapSnapshot()
        return mapOf("" to nodeInfo
                .map { it.legalIdentities.first().name}
                .filter{ it !in listOf(legalName,SERVICES_NAME,NodeA )})
    }







}
