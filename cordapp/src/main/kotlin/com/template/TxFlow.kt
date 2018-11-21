package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class RequestFlow ( val requestName: String,
                    val rParty: Party) : FlowLogic<Unit>(){

    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        // request the info to other Party by its Identity
        val requester = GetState(rParty,this.ourIdentity,requestName)

        val cmd = Command (GetContract.Commands.Request(), listOf(ourIdentity.owningKey))
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(requester, GetContract.Get_Contract_ID)
                .addCommand(cmd)
        // verify transaction
        txBuilder.verify(serviceHub)
        // signed the party
        val partySigned = serviceHub.signInitialTransaction(txBuilder)
        //finalising signature
        subFlow(FinalityFlow(partySigned))

    }
}