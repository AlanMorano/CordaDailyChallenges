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
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker


@InitiatingFlow
@StartableByRPC
class VerifyFlow (private  val Id: UniqueIdentifier): FlowLogic<SignedTransaction>(){

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
    override fun call(): SignedTransaction{
        /* Step 1 - Build the transaction */
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(Id))

        val inputState = serviceHub.vaultService.queryBy<UserState>(criteria).states.single()

        val input = inputState.state.data

        val userState = UserState(ourIdentity,input.name,input.age,input.address,input.birthDate,input.status,input.religion,true, listOf(ourIdentity),input.linearId)

        val cmd = Command(UserContract.Commands.Verify(),ourIdentity.owningKey)

        val txBuilder = TransactionBuilder(notary)
                .addInputState(inputState)
                .addOutputState(userState,UserContract.ID)
                .addCommand(cmd)
        progressTracker.currentStep = BUILDING_TRANSACTION


        /* Step 2 - Verify the transaction */
        progressTracker.currentStep = VERIFY_TRANSACTION
        txBuilder.verify(serviceHub)


        /* Step 3 - Sign the transaction */
        progressTracker.currentStep = SIGN_TRANSACTION
        val signedTx = serviceHub.signInitialTransaction(txBuilder)



        /* Step 4 and 5 - Notarize then Record the transaction */
        progressTracker.currentStep = NOTARIZE_TRANSACTION
        progressTracker.currentStep = RECORD_TRANSACTION
        return subFlow(FinalityFlow(signedTx))


    }
}