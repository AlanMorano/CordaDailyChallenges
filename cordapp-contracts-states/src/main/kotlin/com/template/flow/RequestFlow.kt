package com.template.flow

import co.paralleluniverse.fibers.Suspendable
import com.template.contract.RequestContract
import com.template.contract.RequestContract.Companion.Request_ID
import com.template.states.RequestState
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party

import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

object RequestFlow {

    @InitiatingFlow
    @StartableByRPC
    class Initiator (private val infoOwner: Party,
                     private val name: String): FlowLogic<SignedTransaction>(){

        override val progressTracker = ProgressTracker(GETTING_NOTARY, GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION, SIGNING_TRANSACTION, FINALISING_TRANSACTION)

        @Suspendable
        override fun call(): SignedTransaction {
            progressTracker.currentStep = GETTING_NOTARY
            val notary = serviceHub.networkMapCache.notaryIdentities.first()


            progressTracker.currentStep = GENERATING_TRANSACTION

            val requestState = RequestState(infoOwner, this.ourIdentity, name)


           val txCommand = Command(RequestContract.Commands.Request(), ourIdentity.owningKey)
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
