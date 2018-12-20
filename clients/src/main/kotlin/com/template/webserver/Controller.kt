//package com.template.webserver
//
//import com.template.flows.ApprovedFlow
//import com.template.flows.RegisterFlow
//import com.template.flows.RequestFlow
//import com.template.flows.UpdateFlow
//import com.template.states.RequestState
//import com.template.states.UserState
//import net.corda.core.contracts.*
//import net.corda.core.identity.CordaX500Name
//import net.corda.core.identity.Party
//import net.corda.core.identity.excludeNotary
//import net.corda.core.internal.declaredField
//import net.corda.core.messaging.vaultQueryBy
//import net.corda.core.utilities.Id
//import net.corda.core.utilities.getOrThrow
//import org.apache.commons.beanutils.BeanUtils
//
//import org.slf4j.LoggerFactory
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.http.HttpStatus
//import org.springframework.http.ResponseEntity
//
//import org.springframework.messaging.simp.SimpMessagingTemplate
//import org.springframework.web.bind.annotation.*
//import java.time.LocalDateTime
//import java.time.ZoneId
//import javax.servlet.http.HttpServletRequest
//import javax.ws.rs.PUT
//
//import javax.ws.rs.QueryParam
//import javax.ws.rs.core.MediaType
//import javax.ws.rs.core.Response
//import javax.ws.rs.core.Response.accepted
//import javax.ws.rs.core.Response.ok
//
//import javax.xml.bind.annotation.XmlElement
//import javax.xml.bind.annotation.XmlRootElement
//import org.springframework.web.bind.annotation.PutMapping
//import java.lang.IllegalStateException
//import javax.ws.rs.core.Request
//
//
//private const val CONTROLLER_NAME = "config.controller.name"
///**
// * Define your API endpoints here.
// */
//@RestController
//@RequestMapping("/kyc") // The paths for HTTP requests are relative to this base path.
// class Controller(
//        private val rpc: NodeRPCConnection,@Value("\${$CONTROLLER_NAME}") private val controllerName: String) {
//
//        companion object {
//        private val logger = LoggerFactory.getLogger(RestController::class.java)
//    }
//        private val myName = rpc.proxy.nodeInfo().legalIdentities.first().name
//        private val proxy = rpc.proxy
//
//        /** Returns the node's name. */
//        @GetMapping(value = "/myname", produces = arrayOf("text/plain"))
//        private fun myName() = myName.toString()
//
//        @GetMapping(value = "/status", produces = arrayOf("text/plain"))
//        private fun status() = "Single"
//
//        @GetMapping(value = "/servertime", produces = arrayOf("text/plain"))
//        private fun serverTime() = LocalDateTime.ofInstant(proxy.currentNodeTime(), ZoneId.of("UTC")).toString()
//
//        @GetMapping(value = "/addresses", produces = arrayOf("text/plain"))
//        private fun addresses() = proxy.nodeInfo().addresses.toString()
//
//        @GetMapping(value = "/identities", produces = arrayOf("text/plain"))
//        private fun identities() = proxy.nodeInfo().legalIdentities.toString()
//
//        @GetMapping(value = "/platformversion", produces = arrayOf("text/plain"))
//        private fun platformVersion() = proxy.nodeInfo().platformVersion.toString()
//
//        @GetMapping(value = "/peers", produces = arrayOf("text/plain"))
//        private fun peers() = proxy.networkMapSnapshot().flatMap { it.legalIdentities }.toString()
//
//        @GetMapping(value = "/notaries", produces = arrayOf("text/plain"))
//        private fun notaries() = proxy.notaryIdentities().toString()
//
//        @GetMapping(value = "/flows", produces = arrayOf("text/plain"))
//        private fun flows() = proxy.registeredFlows().toString()
//
////    @GetMapping(value = "/states", produces = arrayOf("application/json"))
////    private fun states() = proxy.vaultQueryBy<UserState>().states.toString()
//
//
//
//        /** Maps a UserState to a JSON object. */ /**Get all data Registered in states using API*/
//        private fun UserState.toJson(): Map<String, Any> {
//
//            return mapOf(
//
//                    "ownParty" to ownParty.toString(),
//                    "name" to name,
//                    "age" to age,
//                    "address" to address,
//                    "birthDate" to birthDate,
//                    "status" to status,
//                    "religion" to religion,
//                    "participants" to participants.toString(),
//                    "linearId" to linearId,
//                    "verified" to isVerified,
//                    "notary" to notaries()
//
//
//            )
//        }
//
//        /** Returns a list of existing state's. */ /**Get all data Registered in states using API*/
//        @GetMapping(value = "/states", produces = arrayOf("application/json"))
//        private fun getStates(): List<Map<String, Any>> {
//            val StateAndRefs = rpc.proxy.vaultQueryBy<UserState>().states
//            val States = StateAndRefs.map { it.state.data }
//            return States.map { it.toJson() }
//        }
//
//        /** Returns a list of the node's network peers. */
//        @GetMapping(value = "/peersnames", produces = arrayOf("application/json"))
//        private fun peersNames(): Map<String, List<String>> {
//            val nodes = rpc.proxy.networkMapSnapshot()
//            val nodeNames = nodes.map { it.legalIdentities.first().name }
//            val filteredNodeNames = nodeNames.filter { it.organisation !in listOf(controllerName, myName) }
//            val filteredNodeNamesToStr = filteredNodeNames.map { it.toString() }
//            return mapOf("peers" to filteredNodeNamesToStr)
//        }
//
//
//            /**get names from UserState*/
//        @GetMapping(value = "/getnames", produces = arrayOf("application/json"))
//        private fun getnames() : Map<String, List<Any>> {
//            val userStateAndRef = rpc.proxy.vaultQueryBy<UserState>().states
//            val userStates = userStateAndRef.map { it.state.data }
//            val stateName = userStates.map { it.toJson() }
//            return mapOf("ownParty" to stateName)
//        }
////    @PostMapping(value = "/display", produces = arrayOf("application/json"))
////    private fun getDisplay(): List<Map<String, Any>> {
////        val State = rpc.proxy.vaultQueryBy<UserState>().states
////        val Sta = State.map { it.state.data }
////        return Sta.map { it.toJson() }
////    }
//            /** request states from Request States*/
//        @GetMapping(value = "/request-states", produces = arrayOf("application/json"))
//        private fun getRequestStates(): Map<String, Any>{
//
//            val requestStateAndRefs = rpc.proxy.vaultQueryBy<RequestState>().states
//            val requestStates = requestStateAndRefs.map { it.state.data }
//            val list1 = requestStates.map { it.toString() }
//            val status = "status" to "success"
//            val message = "message" to "successful in getting ContractState of type UserState"
//            return mapOf(status,message,"result" to list1)
//        }
//
//        /** login API*/
//        @PostMapping(value = "/login")
//        private fun Authentication(
//                @QueryParam("username") username: String,
//                @QueryParam("password") password: String): Response {
//            if (username == "user2" && password == "test2") {
//                //Grant access
////                return Response.status(Response.Status.ACCEPTED).entity("$username sucessfully logged in").build()
//                return accepted("$username successfully logged in").build()
//            } else
//                return ok("Invalid username or password").build()
//            }
//
//        /** login 2 API*/
//    @PostMapping(value = "/login2", produces = arrayOf("application/json"))
//    private fun login(@QueryParam("username") username: String,
//                      @QueryParam("password") password: String):Map<String, String>{
//
//        val success = "User" to "$username successfully login!!!"
//        val status = "Status" to "Status"
//        val message = "message" to "message"
//        val failed ="User" to "$username failed to login!!!"
//        if (username == "user" && password == "test") {
//
//            return mapOf(success,status,message)
//        }else
//            return mapOf(failed,status)
//
//
//
//    }
//
//        /**Register data API */
//@PostMapping(value = "/register",produces = arrayOf("application/json"))
//    private fun getRegister(@RequestBody customers: Customers): ResponseEntity<Map<String,Any>> {
//        val (status, message) = try {
//            val registerFlow = proxy.startFlowDynamic(RegisterFlow::class.java,
//
//                    customers.name,
//                    customers.age,
//                    customers.address,
//                    customers.birthDate,
//                    customers.status,
//                    customers.religion
//            )
//            val data = mapOf("data Successfully Registered" to customers)
//            val result = registerFlow.use { it.returnValue.getOrThrow() }
//            HttpStatus.CREATED to data
//        } catch (ex: Exception) {
//            HttpStatus.BAD_REQUEST to "Failed to Register"
//        }
//        return ResponseEntity.status(status).body(mapOf( "data" to message))
//
//    }
//
////        /**Get all data Registered in states using API*/
////        @GetMapping(value = "getDataRegistered", produces = arrayOf("application/json"))
////        private fun getDataRegistered(): ResponseEntity<Map<String,Any>>{
////        val (status, message) = try {
////            val RegisterStateAndRefs = rpc.proxy.vaultQueryBy<UserState>().states
////            val RegisterStates = RegisterStateAndRefs.map { it.state.data }
////            val list = RegisterStates.map {
////                mapOf(
////                        "OwnParty" to it.ownParty.name.toString(),
////                        "name"     to  it.name,
////                        "age"    to  it.age.toString(),
////                        "address"      to  it.address,
////                        "birthDate" to it.birthDate,
////                        "status" to it.status,
////                        "religion" to it.religion,
////                        "isVerified" to it.isVerified.toString(),
////                        "participants" to it.participants,
////                        "linearId"      to  it.linearId)
////
////
////            }
////
////            HttpStatus.CREATED to list
////        } catch (ex: Exception) {
////            HttpStatus.BAD_REQUEST to "Failed to Request"
////        }
////            return ResponseEntity.status(status).body(mapOf("data" to message))
////        }
//
//
//
//
//
//        /**Update data API*/
//    @PutMapping(value = "/update",produces = arrayOf("application/json"))
//    private fun getUpdate(@RequestBody customerUpdates: CustomerUpdate): ResponseEntity<Map<String,Any>> {
//        val (status, message) = try {
//
//            val LinearId= UniqueIdentifier.fromString(customerUpdates.Id)
//            val registerFlow = proxy.startFlowDynamic(UpdateFlow::class.java,
//                    LinearId,
//                    customerUpdates.name,
//                    customerUpdates.age,
//                    customerUpdates.address,
//                    customerUpdates.birthDate,
//                    customerUpdates.status,
//                    customerUpdates.religion
//            )
//            val data = mapOf("data successfully updated!!!" to customerUpdates)
//            val result = registerFlow.use { it.returnValue.getOrThrow() }
//            HttpStatus.CREATED to data
//        } catch (ex: Exception) {
//            HttpStatus.BAD_REQUEST to "Failed to Update"
//        }
//        return ResponseEntity.status(status).body(mapOf( "data" to message))
//
//    }
//
//
////    @PostMapping(value = "request", produces = arrayOf("application/json"))
////    private fun getRequest(@RequestBody customerRequest: CustomerRequest): ResponseEntity<Map<String,Any>>{
////        val (status, message) = try {
////            val requestStateRef = rpc.proxy.vaultQueryBy<RequestState>().states
////            val LinearId= UniqueIdentifier.fromString(customerRequest.Id)
////            val requestFlow = proxy.startFlowDynamic(RequestFlow::class.java,
//////                    RequestParty.toString(),
////                   LinearId)
////
////            val data = mapOf("data successfully Requested!!!" to customerRequest)
////            val result = requestFlow.use { it.returnValue.getOrThrow() }
////            HttpStatus.CREATED to data
////        } catch (ex: Exception) {
////            HttpStatus.BAD_REQUEST to "Failed to Request"
////        }
////        return ResponseEntity.status(status).body(mapOf( "data" to message))
////
////
////    }
//        /**Request data API*/
//    @PostMapping(value = "createRequest", produces = arrayOf("application/json"))
//    private fun getRequest(@RequestBody customerRequest: CustomerRequest): ResponseEntity<Map<String,Any>>{
//        val (status, message) = try {
//
//
//            val OwnIdentity = rpc.proxy.partiesFromName(customerRequest.OtherParty,exactMatch = false).singleOrNull()
//                ?: throw IllegalStateException("No ${customerRequest.OtherParty} in the network map.")
//            val LinearId= UniqueIdentifier.fromString(customerRequest.Id)
//            val requestHandle = rpc.proxy.startFlowDynamic(RequestFlow::class.java,
//                    OwnIdentity,
//                    LinearId)
//
//            val result = requestHandle.use{it.returnValue.getOrThrow()}
//
//
//            val list = mapOf("Successful send Request" to customerRequest)
//
//            HttpStatus.CREATED to list
//        } catch (ex: Exception) { HttpStatus.BAD_REQUEST to "Failed to Request" }
//        return ResponseEntity.status(status).body(mapOf( "data" to message))
//
//    }
//
//    /** Return all the Users of RequestStates **/
//    @GetMapping(value = "/requestData", produces = arrayOf("application/json"))
//    private fun requestData():ResponseEntity <Map<String, Any>> {
//        val (status, message) = try {
//
//
//        val RequestStateAndRefs = rpc.proxy.vaultQueryBy<RequestState>().states
//        val RequestStates = RequestStateAndRefs.map { it.state.data }
//        val list = RequestStates.map {
//            mapOf(
//                    "OtherParty"     to  it.OtherParty.name.toString(),
//                    "requestParty"     to  it.requestParty.name.toString(),
//                    "linearId"      to  it.Id.toString()
//            )
//         }
//
//        HttpStatus.CREATED to list
//         } catch (ex: Exception) { HttpStatus.BAD_REQUEST to "Failed to Request" }
//        return ResponseEntity.status(status).body(mapOf("data" to message))
//    }
//
///** Approved request API*/
//
//
//    @PostMapping(value="/approvedRequest", produces = arrayOf("application/json"))
//    private fun getApproved(@RequestBody customerApproved: CustomerApproved):ResponseEntity<Map<String,Any>>{
//    val (status, message) = try {
//
//        val LinearId = UniqueIdentifier.fromString(customerApproved.Id)
//        val requestHandle = rpc.proxy.startFlowDynamic(ApprovedFlow::class.java,
//               LinearId
//        )
//
//        val result = requestHandle.use{it.returnValue.getOrThrow()}
//
//
//        val list = mapOf("Successful Approved Request" to customerApproved)
//        HttpStatus.CREATED to list
//
//    }catch (ex: Exception) { HttpStatus.BAD_REQUEST to "Failed to Approved Request" }
//    return ResponseEntity.status(status).body(mapOf( "data" to message))
//
//}
//
//
////    @PostMapping(value = "/register", produces = arrayOf("application/json"))
////    private fun getRegister(
////            @RequestParam("name")name : String,
////            @RequestParam("age")age : Int,
////            @RequestParam("address")address : String,
////            @RequestParam("birthDate")birthDate: String,
////            @RequestParam("status")status : String,
////            @RequestParam("religion")religion : String) : ResponseEntity<Map<String, Any>> {
////
////        val (status, message) = try {
////            val registerFlow = proxy.startFlowDynamic(RegisterFlow::class.java,
////                    name,
////                    age,
////                    address,
////                    birthDate,
////                    status,
////                    religion
////            )
////            val data = mapOf( "name" to "$name",
////                                                "age" to "$age",
////                                                "address" to "$address",
////                                                "birthDate" to "$birthDate",
////                                                "status" to "$status",
////                                                "religion" to "$religion")
////            val result = registerFlow.use { it.returnValue.getOrThrow() }
////            HttpStatus.CREATED to data
////        }catch ( ex: Exception) {
////            HttpStatus.BAD_REQUEST to "Failed to Register"
////        }
////        return ResponseEntity.status(status).body(mapOf("status" to "Successfully Registered!!!", "data inserted" to message))
////    }
//
//
////
////    @PutMapping(value = "/update",produces = arrayOf("application/json"))
////    private fun getUpdate(@RequestParam("Id")Id : UniqueIdentifier,
////                          @RequestParam("name")name : String,
////                          @RequestParam("age")age : Int,
////                          @RequestParam("address")address : String,
////                          @RequestParam("birthDate")birthDate: String,
////                          @RequestParam("status")status : String,
////                          @RequestParam("religion")religion : String
////                          ):ResponseEntity<Map<String, Any>>{ val (status, message) = try {
////        val registerFlow = proxy.startFlowDynamic(UpdateFlow::class.java,
////                Id,
////                name,
////                age,
////                address,
////                birthDate,
////                status,
////                religion
////        )
////        val data = mapOf(
////                "name" to "$name",
////                "age" to "$age",
////                "address" to "$address",
////                "birthDate" to "$birthDate",
////                "status" to "$status",
////                "religion" to "$religion")
////        val result = registerFlow.use { it.returnValue.getOrThrow() }
////        HttpStatus.CREATED to data
////    }catch ( ex: Exception) {
////        HttpStatus.BAD_REQUEST to "Failed to Updated"
////    }
////        return ResponseEntity.status(status).body(mapOf("status" to "Successfully Updated!!!", "data updated" to message))
////
////    }
//
//
//
//
//
//
//
//
////        @PostMapping(value = "/login", produces = arrayOf("application/json"))
////        private fun Authentication(
////                @QueryParam("username") username: String,
////                @QueryParam("password") password: String):ResponseEntity<String> {
////            if (username == "user2" && password == "test2") {
////                //Grant access
//////                return Response.status(Response.Status.ACCEPTED).entity("$username sucessfully logged in").build()
////                return ResponseEntity.ok("$username successfully logged in ")
////
////            } else
////                return ResponseEntity.badRequest().body("Invalid username or password")
////        }
//
//
//
////
////    /** Sends a Yo to a counterparty. */
////    @PostMapping(value = "/sendyo", produces = arrayOf("text/plain"), headers = arrayOf("Content-Type=application/x-www-form-urlencoded"))
////    private fun sendYo(request: HttpServletRequest): ResponseEntity<String> {
////        val targetName = request.getParameter("target")
////        val targetX500Name = CordaX500Name.parse(targetName)
////        val target = rpc.proxy.wellKnownPartyFromX500Name(targetX500Name) ?: throw IllegalArgumentException("Unrecognised peer.")
////        val flow = rpc.proxy.startFlowDynamic(RequestFlow::class.java, target)
////
////        return try {
////            flow.returnValue.getOrThrow()
////            ResponseEntity.ok("You just sent a Yo! to ${target.name}")
////        } catch (e: TransactionVerificationException.ContractRejection) {
////            ResponseEntity.badRequest().body("The Yo! was invalid - ${e.cause?.message}")
////        }
////    }
//
//}