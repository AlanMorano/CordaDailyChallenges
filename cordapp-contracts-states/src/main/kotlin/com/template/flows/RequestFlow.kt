package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.RequestContract
import com.template.states.RequestState
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker


@InitiatingFlow
@StartableByRPC
class RequestFlow(val OtherParty: Party): FlowLogic<Unit>(){
    override val progressTracker = ProgressTracker()

    @Suspendable
    override  fun call(){
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val requestState = RequestState(OtherParty,ourIdentity)

        val cmd = Command(RequestContract.Commands.Request(), ourIdentity.owningKey)

        val txBuilder = TransactionBuilder(notary)
                .addOutputState(requestState,RequestContract.ID)
                .addCommand(cmd)

        txBuilder.verify(serviceHub)

        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        subFlow(FinalityFlow(signedTx))
    }

}