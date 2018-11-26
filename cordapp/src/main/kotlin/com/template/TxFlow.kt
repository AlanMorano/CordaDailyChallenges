package com.template

import co.paralleluniverse.fibers.Suspendable
import com.template.GetContract.Companion.Get_Contract_ID
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class RequestFlow ( val owningParty: Party,
                    val IdState: UniqueIdentifier) : FlowLogic<SignedTransaction>(){

    override val progressTracker = ProgressTracker(
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            NOTARIZE_TRANSACTION,
            FINALISING_TRANSACTION )

    @Suspendable
    override fun call() : SignedTransaction {

        progressTracker.currentStep = GENERATING_TRANSACTION
        // verify notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // request to other party
        val requester = GetState(owningParty,ourIdentity,IdState)

        // valid or invalid in contract
        val cmd = Command (GetContract.Commands.Request(), ourIdentity.owningKey)

        //add transaction Builder
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(requester, Get_Contract_ID)
                .addCommand(cmd)

        progressTracker.currentStep = VERIFYING_TRANSACTION
        //verification of transaction
        txBuilder.verify(serviceHub)

        progressTracker.currentStep = SIGNING_TRANSACTION
        //signed by the participants
        val partySigned = serviceHub.signInitialTransaction(txBuilder)

        progressTracker.currentStep = NOTARIZE_TRANSACTION
        progressTracker.currentStep = FINALISING_TRANSACTION
        //Notarize then Record the transaction
        return subFlow(FinalityFlow(partySigned))

    }
}

@InitiatingFlow
@StartableByRPC
class ShareFlow(val linearId: UniqueIdentifier) : FlowLogic<SignedTransaction>(){

    override val progressTracker = ProgressTracker(
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            NOTARIZE_TRANSACTION,
            FINALISING_TRANSACTION )

    @Suspendable
    override fun call() : SignedTransaction {

        progressTracker.currentStep = GENERATING_TRANSACTION

        // Initiator flow logic goes here from GetState
        val requestCriteria = QueryCriteria.VaultQueryCriteria()

        //verify the request by using requestFlow (GetState)
        val requestVault = serviceHub.vaultService.queryBy<GetState>(requestCriteria).states

        // Initiator flow logic goes here from UserState
        val userCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))

        //get the information from UserState owning Party
        val userVault = serviceHub.vaultService.queryBy<UserState>(userCriteria).states.first()
        val user = userVault.state.data

        //add all the participants in parties
        val parties = mutableListOf<Party>()
        for (data in user.participants){            //search all the participant in the vault
            parties.add(data)                       //add the participants in the parties
        }

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
                user.isVerified,
                user.linearId
        )

        // valid or invalid in contract
        val cmd = Command(GetContract.Commands.Request(), ourIdentity.owningKey)

        //add transaction Builder
        val txBuilder = TransactionBuilder(notary)
                .addInputState(userVault)
                .addOutputState(outputState,Get_Contract_ID)
                .addCommand(cmd)

        for(state in requestVault) {                    //search in the requestVault
            parties.add(state.state.data.requestNode)   //add requestNode in the parties
            txBuilder.addInputState(state)
        }

        progressTracker.currentStep = VERIFYING_TRANSACTION
        //verification of transaction
        txBuilder.verify(serviceHub)

        progressTracker.currentStep = SIGNING_TRANSACTION
        //signed by the participants
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        progressTracker.currentStep = NOTARIZE_TRANSACTION
        progressTracker.currentStep = FINALISING_TRANSACTION
        //Notarize then Record the transaction
        return subFlow(FinalityFlow(signedTx))
    }
}
@InitiatingFlow
@StartableByRPC
class RemoveFlow(   val OwnParty: Party,
                    val linearId: UniqueIdentifier) : FlowLogic<SignedTransaction>(){

    override val progressTracker = ProgressTracker(
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            NOTARIZE_TRANSACTION,
            FINALISING_TRANSACTION
    )

    @Suspendable
    override fun call() : SignedTransaction {

        progressTracker.currentStep = GENERATING_TRANSACTION
        //verify notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // Initiator flow logic goes here from UserState
        val userCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))

        //get the information from UserState owning Party
        val userVault = serviceHub.vaultService.queryBy<UserState>(userCriteria).states.first()
        val user = userVault.state.data

        //add all the participants in parties
        val parties = mutableListOf<Party>()
        for (data in user.participants){            //search all the participant in the vault
            parties.add(data)                       //add the participants in the parties
        }
        parties.remove(OwnParty)

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
                user.isVerified,
                user.linearId
        )

        // valid or invalid in contract
        val cmd = Command(GetContract.Commands.Request(), ourIdentity.owningKey)

        //add transaction Builder
        val txBuilder = TransactionBuilder(notary)
                .addInputState(userVault)
                .addOutputState(outputState,Get_Contract_ID)
                .addCommand(cmd)

        progressTracker.currentStep = VERIFYING_TRANSACTION
        //verification of transaction
        txBuilder.verify(serviceHub)

        progressTracker.currentStep = SIGNING_TRANSACTION
        //signed by the participants
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        progressTracker.currentStep = NOTARIZE_TRANSACTION
        progressTracker.currentStep = FINALISING_TRANSACTION
        //Notarize then Record the transaction
        return subFlow(FinalityFlow(signedTx))
    }
}
