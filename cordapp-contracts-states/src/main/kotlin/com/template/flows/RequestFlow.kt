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
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.lang.IllegalArgumentException


@InitiatingFlow
@StartableByRPC
class RequestFlow(private val OtherParty: Party ,
                  private val Id: UniqueIdentifier): FlowLogic<Unit>(){
    override val progressTracker = ProgressTracker()

    @Suspendable
    override  fun call(){

        /* Step 1 - Build the transaction */


        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val requestState = RequestState(OtherParty, ourIdentity,Id)

        val cmd = Command(RequestContract.Commands.Request(), ourIdentity.owningKey)

        val txBuilder = TransactionBuilder(notary)
                .addOutputState(requestState,RequestContract.ID)
                .addCommand(cmd)
        /* Step 2 - Verify the transaction */

        txBuilder.verify(serviceHub)
        /* Step 3 - Sign the transaction */

        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        /* Step 4 and 5 - Notarize then Record the transaction */
         subFlow(FinalityFlow(signedTx))
    }

}