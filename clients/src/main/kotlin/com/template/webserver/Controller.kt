package com.template.webserver

import com.template.*
import net.corda.core.contracts.UniqueIdentifier
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
    private fun UserState.toUsers(): Map<String, Any> {
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
                "notary" to notaries()
        )
    }

    /** Maps a AccountState to a JSON object. */
    private fun GetState.toRequest(): Map<String, Any>{
        return mapOf(
                "infoOwner" to ownNode.name.toString(),
                "requestor" to requestNode.name.toString(),
                "linearId" to IdState.toString()
        )
    }

    private fun UserState.toName(): Map<String, Any> {
        return mapOf(
                "name" to Name
        )
    }

    private fun AccountState.toLog(): Map<String, Any> {
        return mapOf(
                "Username" to Username,
                "Password" to Password,
                "Firstname" to Firstname,
                "Lastname" to Lastname,
                "Email" to Email,
                "Number" to Number,
                "linear ID" to linearId
        )
    }

    /** Returns the node's name. */
    @GetMapping(value = "/myname", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun myName() = myIdentity.toString()

    /** Get the Status **/
    @GetMapping(value = "/status", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun status() = "200"

    /** Get the Servertime **/
    @GetMapping(value = "/servertime", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun serverTime() = LocalDateTime.ofInstant(rpcOps.currentNodeTime(), ZoneId.of("UTC")).toString()

    /** Get the Address **/
    @GetMapping(value = "/addresses", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun addresses() = rpcOps.nodeInfo().addresses.toString()

    /** Get the PlatFormVersion **/
    @GetMapping(value = "/platformversion", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun platformVersion() = rpcOps.nodeInfo().platformVersion.toString()

    /** Get all the Peers **/
    @GetMapping(value = "/peers", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun peers() = rpcOps.networkMapSnapshot().flatMap { it.legalIdentities }.toString()

    /** Get all the notaries **/
    @GetMapping(value = "/notaries", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun notaries() = rpcOps.notaryIdentities().toString()

    /** Get all the Flows **/
    @GetMapping(value = "/flows", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun flows() = rpcOps.registeredFlows().toString()

    /** Return all the Names of UserStates Listed **/
    @GetMapping(value = "/listnames", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun listnames(): Map<String, List<String>> {
        val userStateAndRef = rpc.proxy.vaultQueryBy<UserState>().states
        val userStates = userStateAndRef.map { it.state.data }
        val statename = userStates.map { it.Name }
        return mapOf("name" to statename)
    }

    /** Return all the Names of UserStates **/
    @GetMapping(value = "/getnames", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun getNames(): List<Map<String, Any>> {
        val userStateAndRefs = rpc.proxy.vaultQueryBy<UserState>().states
        val userStates = userStateAndRefs.map { it.state.data }
        return userStates.map { it.toName() }
    }

    /** Return all the Users of UserStates **/
    @GetMapping(value = "/users", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun details(): Map<String, Any> {
        val userStateAndRefs = rpc.proxy.vaultQueryBy<UserState>().states
        val userStates = userStateAndRefs.map { it.state.data }
        val list =  userStates.map { it.toUsers() }
        val status = "status" to "success"
        val message = "message" to "Successful in getting all UserState"
        return mapOf(status,message, "result" to list)
    }

    /** Return all the Users of GetStates **/
    @GetMapping(value = "/account", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun request(): Map<String, Any> {
        val userStateAndRefs = rpc.proxy.vaultQueryBy<GetState>().states
        val userStates = userStateAndRefs.map { it.state.data }
        val list =  userStates.map { it.toRequest() }
        val status = "status" to "success"
        val message = "message" to "Successful in getting all UserState"
        return mapOf(status,message, "result" to list)
    }

    /** Return all the Users of AccountStates **/
    @GetMapping(value = "/account", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun account(): Map<String, Any> {
        val userStateAndRefs = rpc.proxy.vaultQueryBy<AccountState>().states
        val userStates = userStateAndRefs.map { it.state.data }
        val list =  userStates.map { it.toLog() }
        val status = "status" to "success"
        val message = "message" to "Successful in getting all UserState"
        return mapOf(status,message, "result" to list)
    }

    /** Log-in of the Users in AccountStates **/
    @PostMapping(value = "/login",produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun Authentication(
            @QueryParam("username") username: String,
            @QueryParam("password") password: String): ResponseEntity<Map<String, Any>> {

            val flowHandle = rpc.proxy.vaultQueryBy<AccountState>().states
            val user = flowHandle.find { stateAndRef -> stateAndRef.state.data.Username == username
                    && stateAndRef.state.data.Password == password }

        return if ( user != null){
            val input = user!!.state.data.toLog()

            ResponseEntity.ok().body(mapOf("status" to "success", "message" to "Successful Log-in",
                    "result" to input))
        } else {
            ResponseEntity.badRequest().body(mapOf("status" to "failed", "message" to "Failed to login",
                    "result" to "[]"))
        }
    }

    /** Register New User in UserStates **/
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
            val flowresult = mapOf("Name" to Name,"Age" to Age, "Address" to Address, "BirthDate" to BirthDate,
                    "Status" to Status, "Religion" to Religion, "Transaction ID"  to result.id)
            HttpStatus.CREATED to flowresult
        } catch (ex: Exception) {
            HttpStatus.BAD_REQUEST to "Register Failed"
        }
        return ResponseEntity.status(status).body(mapOf("status" to "success", "message" to "Successful Registered",
                "result" to message))
    }

    /** Register New User in AccountStates **/
    @PostMapping(value = "/createuser", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun createuser(@QueryParam("Username") Username: String,
                         @QueryParam("Password") Password: String,
                         @QueryParam("Firstname") Firstname: String,
                         @QueryParam("Lastname") Lastname: String,
                         @QueryParam("Email") Email: String,
                         @QueryParam("Number") Number: Int
    ): ResponseEntity<Map<String, Any>> {

        val (status,message) = try {
            val flowHandle = rpc.proxy.startFlowDynamic(AccountFlow::class.java,
                    Username,Password,Firstname,Lastname,Email, Number)
            val result = flowHandle.use { it.returnValue.getOrThrow()}
            val flowresult = mapOf("Username" to Username, "Password" to "***","Firstname" to Firstname,
                    "Lastname" to Lastname, "Email" to Email, "Number" to Number, "Transaction ID"  to result.id)
            HttpStatus.CREATED to flowresult
        } catch (ex: Exception) {
            HttpStatus.BAD_REQUEST to "Registered Failed"
        }
        return ResponseEntity.status(status).body(mapOf("status" to "success", "message" to "Successful Registered",
                "result" to message))
    }

    /** Update Users in UserStates **/
    @PostMapping(value = "/updateUser")
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
            val flowresult = mapOf("Name" to NewName,"Age" to NewAge, "Address" to NewAddress, "BirthDate" to NewBirthDate,
                    "Status" to NewStatus, "Religion" to NewReligion, "Transaction ID"  to result.id)
            HttpStatus.CREATED to flowresult
        } catch (ex: Throwable) {
            HttpStatus.BAD_REQUEST to "Update Failed"
        }
        return ResponseEntity.status(status).body(mapOf("status" to "success", "message" to "Successful Registered",
                "result" to message))
    }

    /** Update Users in AccountStates **/
    @PostMapping(value = "/updateAccount")
    private fun updateAccount(
            @QueryParam("NewFirstName") NewFirstName: String,
            @QueryParam("NewLastName") NewLastName: String,
            @QueryParam("NewEmail") NewEmail: String,
            @QueryParam("NewNumber") NewNumber: Int,
            @QueryParam("Id")ID: String
    ): ResponseEntity<Map<String, Any>> {

        val (status , message) = try {
            val uniqueID = UniqueIdentifier.fromString(ID)
            val flowHandle = rpcOps.startFlowDynamic(UpdateAccountFlow::class.java,
                    NewFirstName, NewLastName, NewEmail, NewNumber,uniqueID)
            val result = flowHandle.use { it.returnValue.getOrThrow()}
            val flowresult = mapOf("FirstName" to NewFirstName ,"LastName" to NewLastName,
                    "Email" to NewEmail, "Number" to NewNumber,"Transaction ID"  to result.id)
            HttpStatus.CREATED to flowresult
        } catch (ex: Throwable) {
            HttpStatus.BAD_REQUEST to "Update Failed"
        }
        return ResponseEntity.status(status).body(mapOf("status" to "success", "message" to "Successful Registered",
                "result" to message))
    }

}