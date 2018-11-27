package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.UserContract
import com.template.states.UserState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class RegisterFlow(
        private val ownParty: Party,
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
    override fun call(){
        /* Step 1 - Build the transaction */
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val userState =UserState(ownParty,name,age,address,birthDate,status,religion,false, listOf(ourIdentity), UniqueIdentifier())
        val cmd = Command(UserContract.Commands.Register(),ownParty.owningKey)


        val txBuilder = TransactionBuilder(notary)
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

