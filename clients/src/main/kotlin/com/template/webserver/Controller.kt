package com.template.webserver

import com.template.RegisterFlow
import com.template.UpdateFlow
import com.template.UserState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.hash
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import org.apache.qpid.proton.amqp.messaging.Accepted
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneId
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Response


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

    private fun UserState.toName(): Map<String, Any> {
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
    private fun getnames(): Map<String, List<String>> {
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

    @GetMapping(value = "/kyc", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun kyc(): Map<String, List<Map<String, Any>>> {
        val kycStates = rpcOps.vaultQueryBy<UserState>().states
        return mapOf("kyc" to kycStates.map {
            mapOf("Name" to it.state.data.Name,
                    "Age" to it.state.data.Age,
                    "Address" to it.state.data.Address,
                    "BirthDate" to it.state.data.BirthDate,
                    "Status" to it.state.data.Status,
                    "Religion" to it.state.data.Religion,
                    "uniqueIdentifier" to it.state.data.linearId)
        })
    }

//    @PostMapping(value = "/login")
//    private fun Authentication(
//            @QueryParam("username") username: String,
//            @QueryParam("password") password: String): Response
//    {
//        if (username == "user1" && password == "test") {
//            //Grant access
//            return Response.status(Response.Status.ACCEPTED).entity("$username sucessfully logged in").build()
//        } else {
//            return Response.status(Response.Status.OK).entity("Invalid username or password").build()
//        }
//    }

    @PostMapping(value = "/login",produces = arrayOf("text/plain"))
    private fun Authentication(
            @QueryParam("username") username: String,
            @QueryParam("password") password: String): ResponseEntity<String> {
        if (username == "user1" && password == "test") {
            //Grant access
            return ResponseEntity.ok("successful $username")
        } else {
            return ResponseEntity.badRequest().body("invalid $username")
        }
    }

//    @PostMapping(value = "/register")
//    private fun register(@QueryParam("Name") Name: String,
//                         @QueryParam("Age") Age: Int,
//                         @QueryParam("Address") Address: String,
//                         @QueryParam("BirthDate") BirthDate: String,
//                         @QueryParam("Status") Status: String,
//                         @QueryParam("Religion") Religion: String
//    ): Response {
//
//        return try {
//            val flowHandle = rpc.proxy.startFlowDynamic(RegisterFlow::class.java,
//                    Name,Age,Address,BirthDate,Status,Religion)
//            val result =flowHandle.returnValue.getOrThrow()
//            Response.status(Response.Status.CREATED).entity("Transaction id ${result.id} committed to ledger.\n").build()
//        } catch (ex: Throwable) {
//            Response.status(Response.Status.BAD_REQUEST).entity(ex.message!!).build()
//        }
//    }

    @PostMapping(value = "/register")
    private fun register(@QueryParam("Name") Name: String,
                         @QueryParam("Age") Age: Int,
                         @QueryParam("Address") Address: String,
                         @QueryParam("BirthDate") BirthDate: String,
                         @QueryParam("Status") Status: String,
                         @QueryParam("Religion") Religion: String
    ): ResponseEntity<String> {

        return try {
            val flowHandle = rpc.proxy.startFlowDynamic(RegisterFlow::class.java,
                    Name,Age,Address,BirthDate,Status,Religion)
            val result = flowHandle.returnValue.getOrThrow()
            ResponseEntity.ok("Registered ${result.id}")
        } catch (ex: Throwable) {
            ResponseEntity.badRequest().body("Invalid Input ${ex.message}")
        }
    }

//    @PostMapping(value = "/update")
//    private fun updateKYC(
//                          @QueryParam("NewName") NewName: String,
//                          @QueryParam("NewAge") NewAge: Int,
//                          @QueryParam("NewAddress") NewAddress: String,
//                          @QueryParam("NewBirthDate") NewBirthDate: String,
//                          @QueryParam("NewStatus") NewStatus: String,
//                          @QueryParam("NewReligion") NewReligion: String,
//                          @QueryParam("Id")ID: String
//    ): Response {
//
//        return try {
//            val uniqueID = UniqueIdentifier.fromString(ID)
//            val flowHandle = rpcOps.startFlowDynamic(UpdateFlow::class.java,
//                    NewName, NewAge, NewAddress, NewBirthDate, NewStatus, NewReligion,uniqueID)
//            val result = flowHandle.returnValue.getOrThrow()
//            Response.status(Response.Status.CREATED).entity("Transaction id ${result.id} committed to ledger.\n").build()
//        } catch (ex: Throwable) {
//            Response.status(Response.Status.BAD_REQUEST).entity(ex.message!!).build()
//        }
//    }

    @PostMapping(value = "/update")
    private fun updateKYC(
            @QueryParam("NewName") NewName: String,
            @QueryParam("NewAge") NewAge: Int,
            @QueryParam("NewAddress") NewAddress: String,
            @QueryParam("NewBirthDate") NewBirthDate: String,
            @QueryParam("NewStatus") NewStatus: String,
            @QueryParam("NewReligion") NewReligion: String,
            @QueryParam("Id")ID: String
    ): ResponseEntity<String> {

        return try {
            val uniqueID = UniqueIdentifier.fromString(ID)
            val flowHandle = rpcOps.startFlowDynamic(UpdateFlow::class.java,
                    NewName, NewAge, NewAddress, NewBirthDate, NewStatus, NewReligion,uniqueID)
            val result = flowHandle.returnValue.getOrThrow()
            ResponseEntity.ok("Updated $ID , $NewName, ${result.id}")
        } catch (ex: Throwable) {
            ResponseEntity.badRequest().body("invalid ${ex.message}")
        }
    }

    @GetMapping(value = "/teers", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = rpcOps.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                //filter out myself, notary and eventual network map started by driver
                .filter { it.organisation !in (myIdentity.organisation) })
    }
}
