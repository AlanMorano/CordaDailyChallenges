package com.template

import co.paralleluniverse.fibers.Suspendable
import com.template.UserContract.Companion.USER_CONTRACT_ID
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class RegisterFlow(val owningNode: Party,
                   val name: String,
                   val age: Int,
                   val address: String,
                   val birthDate: String,
                   val status: String,
                   val religion: String,
                   val isVerified: Boolean) : FlowLogic<Unit>() {

    /* Declare Transaction Steps */
    companion object {
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
        progressTracker.currentStep = BUILDING_TRANSACTION
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val outputState = UserState(
                owningNode,
                name,
                age,
                address,
                birthDate,
                status,
                religion,
                isVerified)
        val cmd = Command(UserContract.Commands.Register(), owningNode.owningKey)

        val txBuilder = TransactionBuilder(notary)
                .addOutputState(outputState, USER_CONTRACT_ID)
                .addCommand(cmd)

        /* Step 2 - Sign the transaction */
        progressTracker.currentStep = SIGN_TRANSACTION
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        /* Step 3 - Verify the transaction */
        progressTracker.currentStep = VERIFY_TRANSACTION
        signedTx.verify(serviceHub)

        /* Step 4 and 5 - Notarize then Record the transaction */
        progressTracker.currentStep = NOTARIZE_TRANSACTION
        progressTracker.currentStep = RECORD_TRANSACTION
        subFlow(FinalityFlow(signedTx))
    }
}

@InitiatingFlow
@StartableByRPC
class UpdateFlow(val owningNode: Party,
                 val name: String,
                 val age: Int,
                 val address: String,
                 val birthDate: String,
                 val status: String,
                 val religion: String,
                 val isVerified: Boolean) : FlowLogic<Unit>() {

    /* Declare Transaction Steps */
    companion object {
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
        progressTracker.currentStep = BUILDING_TRANSACTION
        val inputCriteria = QueryCriteria.VaultQueryCriteria()
        val inputStateAndRef = serviceHub.vaultService.queryBy<UserState>(inputCriteria).states.first()
//        val input = inputStateAndRef.state.data

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val outputState = UserState(
                owningNode,
                name,
                age,
                address,
                birthDate,
                status,
                religion,
                isVerified)
        val cmd = Command(UserContract.Commands.Register(), owningNode.owningKey)

        val txBuilder = TransactionBuilder(notary)
                .addInputState(inputStateAndRef)
                .addOutputState(outputState, USER_CONTRACT_ID)
                .addCommand(cmd)

        /* Step 2 - Sign the transaction */
        progressTracker.currentStep = SIGN_TRANSACTION
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        /* Step 3 - Verify the transaction */
        progressTracker.currentStep = VERIFY_TRANSACTION
        signedTx.verify(serviceHub)

        /* Step 4 and 5 - Notarize then Record the transaction */
        progressTracker.currentStep = NOTARIZE_TRANSACTION
        progressTracker.currentStep = RECORD_TRANSACTION
        subFlow(FinalityFlow(signedTx))
    }
}