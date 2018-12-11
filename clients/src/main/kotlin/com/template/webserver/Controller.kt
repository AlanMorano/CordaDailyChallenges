package com.template.webserver

import com.template.AccountState
import com.template.RegisterFlow
import com.template.UpdateFlow
import com.template.UserState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.hash
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.ZoneId
import javax.ws.rs.QueryParam




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
                "linear Id" to linearId,
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

    @PostMapping(value = "/login",produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun Authentication(
            @QueryParam("username") username: String,
            @QueryParam("password") password: String): ResponseEntity<Map<String, Any>> {

            val flowHandle = rpc.proxy.vaultQueryBy<AccountState>().states
            val flowhandle = rpc.proxy.vaultQueryBy<UserState>().states
            val userStates = flowHandle.map { it.state.data.UserName }
            val passStates = flowHandle.map { it.state.data.PassWord }
        if (username == "${userStates.first()}"  && password == "${passStates.first()}") {

            //Grant access
            return ResponseEntity.ok().body(mapOf("status" to "success", "message" to "Successful Log-in",
                    "result" to ""))
        } else {
            return ResponseEntity.badRequest().body(mapOf(username to "$userStates", password to "$passStates",
                    "result" to "[]"))
        }
    }

    @PostMapping(value = "/register", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun register(@QueryParam("Name") Name: String,
                         @QueryParam("Age") Age: Int,
                         @QueryParam("Address") Address: String,
                         @QueryParam("BirthDate") BirthDate: String,
                         @QueryParam("Status") Status: String,
                         @QueryParam("Religion") Religion: String
    ): ResponseEntity<Map<String, Any>> {

        val (status,message) = try {
            val flowHandle = rpc.proxy.startFlowDynamic(RegisterFlow::class.java,
                    Name,Age,Address,BirthDate,Status,Religion)
            val result = flowHandle.use { it.returnValue.getOrThrow()}
            val flowresult = mapOf("Name" to "$Name","Age" to "$Age", "Address" to "$Address", "BirthDate" to "$BirthDate",
                    "Status" to "$Status", "Religion" to "$Religion")
            HttpStatus.CREATED to "${flowresult}Transaction id ${result.id} committed to ledger"
        } catch (ex: Exception) {
            HttpStatus.BAD_REQUEST to ex.message
        }
        return ResponseEntity.status(status).body(mapOf("status" to "success", "message" to "Successful Registered",
                "result" to "$message"))
    }

    @PostMapping(value = "/update")
    private fun updateKYC(
            @QueryParam("NewName") NewName: String,
            @QueryParam("NewAge") NewAge: Int,
            @QueryParam("NewAddress") NewAddress: String,
            @QueryParam("NewBirthDate") NewBirthDate: String,
            @QueryParam("NewStatus") NewStatus: String,
            @QueryParam("NewReligion") NewReligion: String,
            @QueryParam("Id")ID: String
    ): ResponseEntity<Map<String, Any>> {

        val (status , message) = try {
            val uniqueID = UniqueIdentifier.fromString(ID)
            val flowHandle = rpcOps.startFlowDynamic(UpdateFlow::class.java,
                    NewName, NewAge, NewAddress, NewBirthDate, NewStatus, NewReligion,uniqueID)
            val result = flowHandle.use { it.returnValue.getOrThrow()}
            HttpStatus.CREATED to "Updated $ID , $NewName, ${result.id}"
        } catch (ex: Throwable) {
            HttpStatus.BAD_REQUEST to ex.message
        }
        return ResponseEntity.status(status).body(mapOf("status" to "$message"))
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
