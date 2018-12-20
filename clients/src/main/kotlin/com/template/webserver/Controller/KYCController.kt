package com.template.webserver.Controller

import com.template.*
import com.template.webserver.*
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/KYC") // The paths for HTTP requests are relative to this base path.
class KYCController(
        private val rpc: NodeRPCConnection) {

    companion object {
        private val logger = loggerFor<KYCController>()
    }

    /** Return all the Users of KYCStates **/
    @GetMapping(value = "/Users", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun kycstates(): Map<String, Any> {

        val KYCStateAndRefs = rpc.proxy.vaultQueryBy<KYCState>().states
        val KYCStates = KYCStateAndRefs.map { it.state.data }
        val list = KYCStates.map {
            mapOf(
                    "Party"         to  it.Node.name.organisation,
                    "name"          to  it.Name,
                    "age"           to  it.Age,
                    "address"       to  it.Address,
                    "birthdate"     to  it.BirthDate,
                    "status"        to  it.Status,
                    "religion"      to  it.Religion,
                    "isVerified"    to  it.isVerified,
                    "linear Id"     to  it.linearId.id,
                    "list parties"  to  it.parties.toString()
            )
        }
        val status = "status" to "Success"
        val message = "message" to "Successful in Returning All KYCState"
        return mapOf(status, message, "result" to list)
    }

    /** Return one of the Users of KYCStates **/
    @GetMapping(value = "/Users/{Id}", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun kycUser(@PathVariable("Id") linearID : String,@RequestBody getKYC: GetKYC): ResponseEntity<Map<String,Any>> {


         val uniqueID = UniqueIdentifier.fromString(linearID)
         val flowHandle = rpc.proxy.vaultQueryBy<KYCState>().states
         val data = flowHandle.find { stateAndRef ->
               stateAndRef.state.data.linearId == uniqueID
         }

          return if (data != null) {
              getKYC.Name       =   data.state.data.Name
              getKYC.Age        =   data.state.data.Age
              getKYC.Address    =   data.state.data.Address
              getKYC.BirthDate  =   data.state.data.BirthDate
              getKYC.Status     =   data.state.data.Status
              getKYC.Religion   =   data.state.data.Religion
              getKYC.LinearId   =   data.state.data.linearId.id.toString()

              val list = mapOf(
                "Name"          to  getKYC.Name,
                "Age"           to  getKYC.Age,
                "Address"       to  getKYC.Address,
                "BirthDate"     to  getKYC.BirthDate,
                "Status"        to  getKYC.Status,
                "Religion"      to  getKYC.Religion,
                "LinearId"      to  getKYC.LinearId
          )

          ResponseEntity.ok().body(
                 mapOf(
                       "status" to "Success",
                       "message" to "Successful Returning User's Information",
                       "result" to list))
          } else {
          ResponseEntity.badRequest().body(
                 mapOf(
                       "status" to "Failed",
                       "message" to "Failed in Returning User's Information",
                       "result" to "[]")
          )
        }
    }

    /** Register New User in KYCStates **/
    @PostMapping(value = "/Register", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun kycregister(@RequestBody regsUsers: NewRegsKYC): ResponseEntity<Map<String, Any>> {

        return try {
            val flowHandle = rpc.proxy.startFlowDynamic(KYCRegisterFlow::class.java,
                    regsUsers.Name,
                    regsUsers.Age,
                    regsUsers.Address,
                    regsUsers.BirthDate,
                    regsUsers.Status,
                    regsUsers.Religion)

            val result = flowHandle.use { it.returnValue.getOrThrow() }
            val flowResult = mapOf(
                    "Name"              to  regsUsers.Name,
                    "Age"               to  regsUsers.Age,
                    "Address"           to  regsUsers.Address,
                    "BirthDate"         to  regsUsers.BirthDate,
                    "Status"            to  regsUsers.Status,
                    "Religion"          to  regsUsers.Religion,
                    "Transaction ID"    to  result.id.toString())

            ResponseEntity.ok().body(
                    mapOf(
                            "status" to "Success",
                            "message" to "Register Successful",
                            "result" to flowResult)
            )
        } catch (ex: Exception) {
            ResponseEntity.badRequest().body(
                    mapOf(
                            "status" to "Failed",
                            "message" to "Register Failed",
                            "result" to "[]")
            )
        }
    }

    /** Update Users in KYCStates **/
    @PostMapping(value = "/Update", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun kycupdate(@RequestBody updateUser: NewUpdateKYC): ResponseEntity<Map<String, Any>> {

        return try {

            val uniqueID = UniqueIdentifier.fromString(updateUser.ID)
            val flowHandle = rpc.proxy.startFlowDynamic(KYCUpdateFlow::class.java,
                    updateUser.NewName,
                    updateUser.NewAge,
                    updateUser.NewAddress,
                    updateUser.NewBirthDate,
                    updateUser.NewStatus,
                    updateUser.NewReligion,
                    uniqueID)
            val result = flowHandle.use { it.returnValue.getOrThrow() }

            val flowResult = mapOf(
                    "Name"              to  updateUser.NewName,
                    "Age"               to  updateUser.NewAge,
                    "Address"           to  updateUser.NewAddress,
                    "BirthDate"         to  updateUser.NewBirthDate,
                    "Status"            to  updateUser.NewStatus,
                    "Religion"          to  updateUser.NewReligion,
                    "Transaction ID"    to  result.id.toString()
            )
            ResponseEntity.ok().body(
                    mapOf(
                            "status" to "Success",
                            "message" to "Update Successful",
                            "result" to flowResult)
            )
        } catch (ex: Exception) {
            ResponseEntity.badRequest().body(
                    mapOf(
                            "status" to "Failed",
                            "message" to "Update Failed",
                            "result" to "[]")
            )
        }
    }

    /** Verify User's Information in KYCStates **/
    @PostMapping(value = "/Verify", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun verify(@RequestBody verify: NewVerify) : ResponseEntity<Map<String, Any>> {

        return try {

            val OwnerIdentity = rpc.proxy.partiesFromName(verify.Owner, exactMatch = false).singleOrNull()
                    ?: throw IllegalStateException("No ${verify.Owner} in the network map.")
            val uniqueID = UniqueIdentifier.fromString(verify.ID)

            val flowHandle = rpc.proxy.startFlowDynamic(KYCVerifyFlow::class.java,
                    OwnerIdentity.name.organisation,
                    uniqueID)
            val result = flowHandle.use { it.returnValue.getOrThrow() }

            val list = mapOf(
                    "Party" to OwnerIdentity.toString(),
                    "ID"    to uniqueID.toString(),
                    "Transaction Id" to result.id.toString()
            )
            ResponseEntity.ok().body(
                    mapOf(
                            "status" to "Success",
                            "message" to "User's Verify Successful",
                            "result" to list))

        } catch (ex: Exception) {
            ResponseEntity.badRequest().body(
                    mapOf(
                            "status" to "Failed",
                            "message" to "Verify Failed",
                            "result" to "[]")
            )
        }
    }

    /** Remove Participants **/
    @PostMapping(value = "/Remove", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun remove(@RequestBody remove: NewRemove): ResponseEntity<Map<String, Any>> {

        return try {

            val OwnerIdentity = rpc.proxy.partiesFromName(remove.Owner, exactMatch = false).singleOrNull()
                    ?: throw IllegalStateException("No ${remove.Owner} in the network map.")
            val uniqueID = UniqueIdentifier.fromString(remove.ID)

            val flowHandle = rpc.proxy.startFlowDynamic(KYCRemoveFlow::class.java,
                    OwnerIdentity,
                    uniqueID)
            val result = flowHandle.use { it.returnValue.getOrThrow() }

            val list = mapOf(
                    "Party" to OwnerIdentity.toString(),
                    "ID"    to uniqueID.toString(),
                    "Transaction Id" to result.id.toString()
            )
            ResponseEntity.ok().body(
                    mapOf(
                            "status" to "Success",
                            "message" to "Remove Successful in the Party",
                            "result" to list)
            )
        } catch (ex: Exception) {

            ResponseEntity.badRequest().body(
                    mapOf(
                            "status" to "Failed",
                            "message" to "Remove Failed in the Party",
                            "result" to "[]")
            )
        }
    }
}