package com.template.webserver.Controller

import com.template.flow.KYCRegisterFlow
import com.template.flow.KYCRequestFlow
import com.template.flow.UserAccountRegisterFlow
import com.template.states.KYCRequestState
import com.template.states.KYCState
import com.template.states.UserAccountState
import com.template.webserver.NodeRPCConnection
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.template.flow.Encryption.md5
import com.template.models.*
import net.corda.core.contracts.StateAndRef
import javax.servlet.http.HttpServletRequest


private const val CONTROLLER_NAME = "config.controller.name"
//@Value("\${$CONTROLLER_NAME}") private val controllerName: String
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class KYCController(
        private val rpc: NodeRPCConnection
) {
    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }
    private val proxy = rpc.proxy

    /**
     * Return all KYCState
     */
    @GetMapping(value = "/states/kyc", produces = arrayOf("application/json"))
    private fun getKYCStates() : ResponseEntity<Map<String,Any>>{
        val (status, result ) = try {
            val kycStateRef = rpc.proxy.vaultQueryBy<KYCState>().states
            val kycStates = kycStateRef.map { it.state.data }
            val list = kycStates.map {
                kycModel(
                        node = it.node.name.toString(),
                        name = it.name,
                        age = it.age,
                        address = it.address,
                        birthday = it.birthDate,
                        status = it.status,
                        religion = it.religion,
                        isVerified = it.isVerified,
                        listOfParties = it.listOfParties.toString(),
                        linearId = it.linearId.toString())
            }
            HttpStatus.CREATED to list
        }catch( e: Exception){
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status==HttpStatus.CREATED){
            "message" to "Successful in getting ContractState of type KYCState"}
        else{ "message" to "Failed to get ContractState of type KYCState"}
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat,mess,res))


    }

    /**
     * REGISTER - KYCRegister
     */

    @PostMapping(value = "/states/kyc/create", produces = arrayOf("application/json"))
    private fun createKYC(@RequestBody createKYC: createKYC) : ResponseEntity<Map<String,Any>> {

        val (status, result) = try {
            val kyc = createKYC(
                    name = createKYC.name,
                    age = createKYC.age,
                    address = createKYC.address,
                    birthday = createKYC.birthday,
                    status = createKYC.status,
                    religion = createKYC.religion
            )

            val registerFlow = proxy.startFlowDynamic(
                    KYCRegisterFlow.Initiator::class.java,
                    kyc.name,
                    kyc.age,
                    kyc.address,
                    kyc.birthday,
                    kyc.status,
                    kyc.religion
            )
//            val out = registerFlow.use { it.returnValue.getOrThrow() }
            val kycStateRef = proxy.vaultQueryBy<KYCState>().states.last()
            val kycStateData = kycStateRef.state.data
            val list = kycModel(
                    node = kycStateData.node.name.toString(),
                    name = kycStateData.name,
                    age = kycStateData.age,
                    address = kycStateData.address,
                    birthday = kycStateData.birthDate,
                    status = kycStateData.status,
                    religion = kycStateData.religion,
                    isVerified = kycStateData.isVerified,
                    listOfParties = kycStateData.listOfParties.toString(),
                    linearId = kycStateData.linearId.toString()
            )
            HttpStatus.CREATED to list
        }catch (e: Exception){
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status==HttpStatus.CREATED){
            "message" to "Successful in creating ContractState of type KYCState"}
        else{ "message" to "Failed to create ContractState of type KYCState"}
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat,mess,res))

    }

}
