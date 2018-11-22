package com.template

import co.paralleluniverse.fibers.Suspendable
import com.template.GetContract.Companion.Get_Contract_ID
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class RequestFlow ( val owningParty: Party) : FlowLogic<Unit>(){

    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {

        // verify notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // request to other party
        val requester = GetState(owningParty,ourIdentity)

        // valid or invalid in contract
        val cmd = Command (GetContract.Commands.Request(), ourIdentity.owningKey)

        //add transaction Builder
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(requester, GetContract.Get_Contract_ID)
                .addCommand(cmd)

        //verification of transaction
        txBuilder.verify(serviceHub)

        //signed by the participants
        val partySigned = serviceHub.signInitialTransaction(txBuilder)

        //Notarize then Record the transaction
        subFlow(FinalityFlow(partySigned))

    }
}

@InitiatingFlow
@StartableByRPC
class ShareFlow() : FlowLogic<Unit>(){

    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {

        // Initiator flow logic goes here from GetState
        val inputRequestCriteria = QueryCriteria.VaultQueryCriteria()

        //verify the request by using requestFlow (GetState)
        val inputRequestStateAndRef = serviceHub.vaultService.queryBy<GetState>(inputRequestCriteria).states.first()
        val request = inputRequestStateAndRef.state.data

        // Initiator flow logic goes here from UserState
        val inputUserCriteria = QueryCriteria.VaultQueryCriteria()

        //get the information from UserState owning Party
        val inputUserStateAndRef = serviceHub.vaultService.queryBy<UserState>(inputUserCriteria).states.first()
        val user = inputUserStateAndRef.state.data

        //to add the current information of all parties
        val parties = mutableListOf<Party>()
        for(x in user.parties) {
            if (x.equals(x))
            println((x))
            parties.add(x)
        }
        parties.add(request.requestingNode)

        //verify notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // belong to the transaction
        val outputState = UserState(
                ourIdentity,
                user.Name,
                user.Age,
                user.Address,
                user.BirthDate,
                user.Status,
                user.Religion,
                parties,
                true
        )

        // valid or invalid in contract
        val cmd = Command(GetContract.Commands.Request(), ourIdentity.owningKey)

        //add transaction Builder
        val txBuilder = TransactionBuilder(notary)
                .addInputState(inputRequestStateAndRef)
                .addInputState(inputUserStateAndRef)
                .addOutputState(outputState,Get_Contract_ID)
                .addCommand(cmd)

        //verification of transaction
        txBuilder.verify(serviceHub)

        //signed by the participants
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        //Notarize then Record the transaction
        subFlow(FinalityFlow(signedTx))
    }
}