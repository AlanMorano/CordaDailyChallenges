package com.template

import co.paralleluniverse.fibers.Suspendable
import com.template.UserContract.Companion.User_Contract_ID
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class RegisterFlow ( val Name: String,
                     val Age: Int,
                     val Address: String,
                     val BirthDate: String,
                     val Status: String,
                     val Religion: String) : FlowLogic<Unit>() {


    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        // Initiator flow logic goes here.
        // verify notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        // belong to the transaction
        val outputState = UserState(ourIdentity,Name, Age, Address, BirthDate,Status, Religion, listOf(ourIdentity))
        // valid or invalid in contract
        val cmd = Command(UserContract.Commands.Register(),ourIdentity.owningKey)
        //add transaction Builder
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(outputState, User_Contract_ID)
                .addCommand(cmd)
        //verification of transaction
        txBuilder.verify(serviceHub)
        //signed by the participants
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        //verify signature
        signedTx.verify(serviceHub)
        //finalizing signature
        subFlow(FinalityFlow(signedTx))
    }
}

@InitiatingFlow
@StartableByRPC
class UpdateFlow ( val Name: String,
                   val Age: Int,
                   val Address: String,
                   val BirthDate: String,
                   val Status: String,
                   val Religion: String) : FlowLogic<Unit>(){

    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        // Initiator flow logic goes here.
        val criteria = QueryCriteria.VaultQueryCriteria()
        val Vault = serviceHub.vaultService.queryBy<UserState>(criteria).states.first()
        val input = Vault.state.data
        // verify notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        // belong to the transaction
        val outputState = UserState(ourIdentity,Name,Age,Address,BirthDate,Status,Religion,
                listOf(ourIdentity),input.isVerified)
        // valid or invalid in contract
        val cmd = Command(UserContract.Commands.Update(),ourIdentity.owningKey)
        //add transaction Builder
        val txBuilder = TransactionBuilder(notary)
                .addInputState(Vault)
                .addOutputState(outputState, User_Contract_ID)
                .addCommand(cmd)
        //verification of transaction
        txBuilder.verify(serviceHub)
        //signed by the participants
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        //Notarize then Record the transaction
        subFlow(FinalityFlow(signedTx))
    }

}

@InitiatingFlow
@StartableByRPC
class VerifyFlow () : FlowLogic<Unit>(){

    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        // Initiator flow logic goes here.
        val criteria = QueryCriteria.VaultQueryCriteria()
        val Vault = serviceHub.vaultService.queryBy<UserState>(criteria).states.single()
        val input = Vault.state.data

        // verify notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        // belong to the transaction
        val outputState = UserState(ourIdentity,input.Name,input.Age,
                input.Address,input.BirthDate,input.Status,input.Religion, listOf(ourIdentity),true)
        // valid or invalid in contract
        val cmd = Command(UserContract.Commands.Verify(),ourIdentity.owningKey)
        //add transaction Builder
        val txBuilder = TransactionBuilder(notary)
                .addInputState(Vault)
                .addOutputState(outputState, User_Contract_ID)
                .addCommand(cmd)
        //verification of transaction
        txBuilder.verify(serviceHub)
        //signed by the participants
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        //Notarize then Record the transaction
        subFlow(FinalityFlow(signedTx))
    }

}