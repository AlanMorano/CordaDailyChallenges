package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.RequestContract
import com.template.states.RequestState
import com.template.states.UserState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.lang.IllegalArgumentException


@InitiatingFlow
@StartableByRPC
class RequestFlow(private val OtherParty: Party ,
                  private val Id: UniqueIdentifier): FlowLogic<SignedTransaction>(){


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
    override  fun call():SignedTransaction{

        /* Step 1 - Build the transaction */
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val requestState = RequestState(OtherParty, ourIdentity,Id)

        val cmd = Command(RequestContract.Commands.Request(), ourIdentity.owningKey)

        val txBuilder = TransactionBuilder(notary)
                .addOutputState(requestState,RequestContract.ID)
                .addCommand(cmd)
        progressTracker.currentStep = BUILDING_TRANSACTION


        /* Step 2 - Sign the transaction */
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        progressTracker.currentStep = SIGN_TRANSACTION

        /* Step 3 - Verify the transaction */
        txBuilder.verify(serviceHub)
        progressTracker.currentStep = VERIFY_TRANSACTION

        /* Step 4 and 5 - Notarize then Record the transaction */
        progressTracker.currentStep = NOTARIZE_TRANSACTION
        progressTracker.currentStep = RECORD_TRANSACTION
        return subFlow(FinalityFlow(signedTx))

    }

}