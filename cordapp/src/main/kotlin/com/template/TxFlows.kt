package com.template

import co.paralleluniverse.fibers.Suspendable
import com.template.RequestContract.Companion.Request_Contract_ID
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
class KYCRequestFlow ( val owningParty: String,
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

        //Searching the serviceHub for the input Owner and gets it if it has a match, else, throw exc
        val OwnerRef = serviceHub.identityService.partiesFromName(owningParty, false).singleOrNull()
                ?: throw IllegalArgumentException("No match found for Owner $owningParty.")

        // request to other party
        val requester = RequestState(OwnerRef,ourIdentity,IdState)

        // valid or invalid in contract
        val cmd = Command (RequestContract.Commands.Request(), ourIdentity.owningKey)

        //add transaction Builder
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(requester, Request_Contract_ID)
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
class KYCShareFlow(val linearId: UniqueIdentifier) : FlowLogic<SignedTransaction>(){

    override val progressTracker = ProgressTracker(
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            NOTARIZE_TRANSACTION,
            FINALISING_TRANSACTION )

    @Suspendable
    override fun call() : SignedTransaction {

        progressTracker.currentStep = GENERATING_TRANSACTION

        // Initiator flow logic goes here from RequestState
        val requestCriteria = QueryCriteria.VaultQueryCriteria()

        //verify the request by using requestFlow (RequestState)
        val requestVault = serviceHub.vaultService.queryBy<RequestState>(requestCriteria).states

        // Initiator flow logic goes here from KYCState
        val userCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))

        //get the information from KYCState owning Party
        val userVault = serviceHub.vaultService.queryBy<KYCState>(userCriteria).states.first()
        val user = userVault.state.data

        //add all the participants in parties
        val parties = mutableListOf<Party>()
        for (data in user.participants){            //search all the participant in the vault
            parties.add(data)                       //add the participants in the parties
        }

        //verify notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // belong to the transaction
        val outputState = KYCState(
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
        val cmd = Command(RequestContract.Commands.Request(), ourIdentity.owningKey)

        //add transaction Builder
        val txBuilder = TransactionBuilder(notary)
                .addInputState(userVault)
                .addOutputState(outputState,Request_Contract_ID)
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
class KYCRemoveFlow(val OwnParty: Party,
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

        // Initiator flow logic goes here from KYCState
        val userCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))

        //get the information from UserState owning Party
        val userVault = serviceHub.vaultService.queryBy<KYCState>(userCriteria).states.first()
        val user = userVault.state.data

        //add all the participants in parties
        val parties = mutableListOf<Party>()
        for (data in user.participants){            //search all the participant in the vault
            parties.add(data)                       //add the participants in the parties
        }
        parties.remove(OwnParty)

        // belong to the transaction
        val outputState = KYCState(
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
        val cmd = Command(RequestContract.Commands.Request(), ourIdentity.owningKey)

        //add transaction Builder
        val txBuilder = TransactionBuilder(notary)
                .addInputState(userVault)
                .addOutputState(outputState,Request_Contract_ID)
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
