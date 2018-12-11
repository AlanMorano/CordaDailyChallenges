package com.template.flow

import co.paralleluniverse.fibers.Suspendable
import com.template.contract.RequestKYCContract
import com.template.contract.RequestKYCContract.Companion.Request_ID
import com.template.states.RequestState
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC

import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

object RequestKYCFlow {

    @InitiatingFlow
    @StartableByRPC
    class Initiator (private val infoOwner: String,
                     private val name: String): FlowLogic<SignedTransaction>(){

        override val progressTracker = ProgressTracker(GETTING_NOTARY, GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION, SIGNING_TRANSACTION, FINALISING_TRANSACTION)

        @Suspendable
        override fun call(): SignedTransaction {

            //Getting the first notary in the network map
            progressTracker.currentStep = GETTING_NOTARY
            val notary = serviceHub.networkMapCache.notaryIdentities.first()

            //Searching the serviceHub for the input infoOwner and gets it if it has a match, else, throw exc
            val infoOwnerRef = serviceHub.identityService.partiesFromName(infoOwner, false).singleOrNull()
                    ?: throw IllegalArgumentException("No match found for infoOwner $infoOwner.")

            progressTracker.currentStep = GENERATING_TRANSACTION
            //Build transaction
            val txCommand = Command(RequestKYCContract.Commands.Request(), ourIdentity.owningKey)
            //The state to be used as output
            val requestState = RequestState(infoOwnerRef, this.ourIdentity,name,false, listOf(infoOwnerRef,ourIdentity))
                     val txBuilder = TransactionBuilder(notary)
                             .addOutputState(requestState, Request_ID)
                             .addCommand(txCommand)



            progressTracker.currentStep = VERIFYING_TRANSACTION
            txBuilder.verify(serviceHub)

            progressTracker.currentStep = SIGNING_TRANSACTION
            val partySignedTx =
                    serviceHub.signInitialTransaction(txBuilder)

            progressTracker.currentStep = FINALISING_TRANSACTION
            return subFlow(FinalityFlow(partySignedTx))

        }

    }
}
