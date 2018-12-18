package com.template

import co.paralleluniverse.fibers.Suspendable
import com.template.KYCContract.Companion.KYC_Contract_ID
import com.template.UserContract.Companion.User_Contract_ID
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class KYCRegisterFlow ( val Name: String,
                     val Age: Int,
                     val Address: String,
                     val BirthDate: String,
                     val Status: String,
                     val Religion: String) : FlowLogic<SignedTransaction>() {

    override val progressTracker = ProgressTracker(
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            NOTARIZE_TRANSACTION,
            FINALISING_TRANSACTION )

    @Suspendable
    override fun call() : SignedTransaction{

        progressTracker.currentStep = GENERATING_TRANSACTION
        // Initiator flow logic goes here.
        // verify notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // belong to the transaction
        val outputState = KYCState(ourIdentity,Name, Age, Address, BirthDate,Status,
                Religion, listOf(ourIdentity),false, UniqueIdentifier())

        // valid or invalid in contract
        val cmd = Command(KYCContract.Commands.Register(),ourIdentity.owningKey)

        //add transaction Builder
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(outputState, KYC_Contract_ID)
                .addCommand(cmd)

        progressTracker.currentStep = VERIFYING_TRANSACTION
        //verification of transaction
        txBuilder.verify(serviceHub)

        progressTracker.currentStep = SIGNING_TRANSACTION
        //signed by the participants
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        progressTracker.currentStep = NOTARIZE_TRANSACTION
        progressTracker.currentStep = FINALISING_TRANSACTION
        //finalizing signature
        return subFlow(FinalityFlow(signedTx))
    }
}

@InitiatingFlow
@StartableByRPC
class KYCUpdateFlow ( val Name: String,
                   val Age: Int,
                   val Address: String,
                   val BirthDate: String,
                   val Status: String,
                   val Religion: String,
                   val linearId: UniqueIdentifier) : FlowLogic<SignedTransaction>(){

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

        // Initiator flow logic goes here.
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        //get the information from KYCState
        val Vault = serviceHub.vaultService.queryBy<KYCState>(criteria).states.first()
        val input = Vault.state.data

        //if you like to name of your verification/identity of the kyc
//        val name = Vault.state.data.Name
//        if (Name != name){
//            throw IllegalArgumentException("Invalid Name") }

        // belong to the transaction
        val outputState = KYCState(ourIdentity,Name,Age,Address,BirthDate,Status,Religion,
                listOf(ourIdentity),input.isVerified, input.linearId)

        // valid or invalid in contract
        val cmd = Command(KYCContract.Commands.Update(),ourIdentity.owningKey)

        //add transaction Builder
        val txBuilder = TransactionBuilder(notary)
                .addInputState(Vault)
                .addOutputState(outputState, KYC_Contract_ID)
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

@InitiatingFlow
@StartableByRPC
class KYCVerifyFlow (val linearId: UniqueIdentifier) : FlowLogic<SignedTransaction>(){

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

        // Initiator flow logic goes here.
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))

        //get the information from KYCState
        val Vault = serviceHub.vaultService.queryBy<KYCState>(criteria).states.single()
        val input = Vault.state.data

        // belong to the transaction
        val outputState = KYCState(ourIdentity,input.Name,input.Age,
                input.Address,input.BirthDate,input.Status,input.Religion,
                listOf(ourIdentity),true,input.linearId)

        // valid or invalid in contract
        val cmd = Command(KYCContract.Commands.Verify(),ourIdentity.owningKey)

        //add transaction Builder
        val txBuilder = TransactionBuilder(notary)
                .addInputState(Vault)
                .addOutputState(outputState, KYC_Contract_ID)
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