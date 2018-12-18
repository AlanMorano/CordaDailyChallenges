package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class UserRegisterFlow ( val Username : String,
                    val Password : String,
                    val Firstname : String,
                    val Lastname : String,
                    val Email: String,
                    val Number: Int) : FlowLogic<SignedTransaction>() {

    override val progressTracker = ProgressTracker(
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            NOTARIZE_TRANSACTION,
            FINALISING_TRANSACTION )

    @Suspendable
    override fun call() : SignedTransaction {

        progressTracker.currentStep = GENERATING_TRANSACTION
        // Initiator flow logic goes here.
        // verify notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // belong to the transaction
        val outputState = UserState(ourIdentity,Username,Password,Firstname, Lastname,Email,Number, listOf(ourIdentity))

        // valid or invalid in contract
        val cmd = Command(UserContract.Commands.Register(),ourIdentity.owningKey)

        //add transaction Builder
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(outputState, UserContract.User_Contract_ID)
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
class UserUpdateFlow ( val Firstname: String,
                   val Lastname: String,
                   val Email: String,
                   val Number: Int,
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
        //get the information from UserState
        val Vault = serviceHub.vaultService.queryBy<UserState>(criteria).states.first()
        val input = Vault.state.data

        //if you like to name of your verification/identity of the user
//        val name = Vault.state.data.Name
//        if (Name != name){
//            throw IllegalArgumentException("Invalid Name") }

        // belong to the transaction
        val outputState = UserState(ourIdentity,input.Username,input.Password,Firstname,Lastname,Email,Number,
                listOf(ourIdentity), input.linearId)

        // valid or invalid in contract
        val cmd = Command(UserContract.Commands.Update(),ourIdentity.owningKey)

        //add transaction Builder
        val txBuilder = TransactionBuilder(notary)
                .addInputState(Vault)
                .addOutputState(outputState, UserContract.User_Contract_ID)
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