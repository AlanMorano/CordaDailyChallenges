package com.template.webserver.Controller

import com.template.KYCRegisterFlow
import com.template.KYCState
import com.template.KYCUpdateFlow
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
                    "Party" to it.Node.name.organisation,
                    "name" to it.Name,
                    "age" to it.Age,
                    "address" to it.Address,
                    "birthdate" to it.BirthDate,
                    "status" to it.Status,
                    "religion" to it.Religion,
                    "linear Id" to it.linearId.id,
                    "list parties" to it.parties.toString()
            )
        }
        val status = "status" to "success"
        val message = "message" to "Successful in getting all UserState"
        return mapOf(status, message, "result" to list)
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
                            "status" to "success",
                            "message" to "Successful Registered",
                            "result" to flowResult))
        } catch (ex: Exception) {
            ResponseEntity.badRequest().body(
                    mapOf(
                            "status" to "Failed",
                            "message" to "Failed Registered",
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
                    "Name"      to  updateUser.NewName,
                    "Age"       to  updateUser.NewAge,
                    "Address"   to  updateUser.NewAddress,
                    "BirthDate" to  updateUser.NewBirthDate,
                    "Status"    to  updateUser.NewStatus,
                    "Religion"  to  updateUser.NewReligion,
                    "Transaction ID"  to result.id.toString()
            )
            ResponseEntity.ok().body(
                    mapOf(
                            "status" to "success",
                            "message" to "Successful Registered",
                            "result" to flowResult)
            )
        } catch (ex: Exception) {
            ResponseEntity.badRequest().body(
                    mapOf(
                            "status" to updateUser.ID,
                            "message" to "",
                            "result" to ""))
        }
    }
}