package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.UserContract
import com.template.states.UserState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.internal.isUploaderTrusted
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import org.apache.logging.log4j.core.tools.picocli.CommandLine
import java.lang.IllegalArgumentException

@InitiatingFlow
@StartableByRPC
class UpdateFlow( private val Id: UniqueIdentifier,
                  private val name: String,
                  private val age: Int,
                  private val address: String,
                  private val birthDate: String,
                  private val status: String,
                  private val religion: String): FlowLogic<Unit>(){

    /* Declare Transaction steps*/

    companion object{
        object BUILDING_TRANSACTION : ProgressTracker.Step("Building Transaction")
        object SIGN_TRANSACTION : ProgressTracker.Step("Signing Transaction")
        object VERIFY_TRANSACTION : ProgressTracker.Step("Verifying Transaction")
        object NOTARIZE_TRANSACTION : ProgressTracker.Step("Notarizing Transaction")
        object RECORD_TRANSACTION : ProgressTracker.Step("Recording Transaction")
    }

    fun tracker() = ProgressTracker(
            BUILDING_TRANSACTION,
            SIGN_TRANSACTION,
            VERIFY_TRANSACTION,
            NOTARIZE_TRANSACTION,
            RECORD_TRANSACTION
    )


    override val progressTracker = tracker()

    @Suspendable
    override fun call() {

        /* Step 1 - Build the transaction */
        val inputCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(Id))
        val inputStateAndRef = serviceHub.vaultService.queryBy<UserState>(inputCriteria).states.first()

        val input = inputStateAndRef.state.data


        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val userState = UserState(ourIdentity,name,age,address,birthDate,status,religion,input.isVerified, listOf(ourIdentity), input.linearId)
        val cmd = Command(UserContract.Commands.Update(),ourIdentity.owningKey)

        val txBuilder = TransactionBuilder(notary)
                .addInputState(inputStateAndRef)
                .addOutputState(userState,UserContract.ID)
                .addCommand(cmd)
        progressTracker.currentStep = BUILDING_TRANSACTION

        /* Step 2 - Verify the transaction */
        txBuilder.verify(serviceHub)
        progressTracker.currentStep = VERIFY_TRANSACTION

        /* Step 3 - Sign the transaction */
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        progressTracker.currentStep = SIGN_TRANSACTION




        /* Step 4 and 5 - Notarize then Record the transaction */
        progressTracker.currentStep = NOTARIZE_TRANSACTION
        progressTracker.currentStep = RECORD_TRANSACTION
         subFlow(FinalityFlow(signedTx))




    }




}