package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class AccountFlow ( val UserName : String,
                    val PassWord : String,
                    val FirstName : String,
                    val LastName : String,
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
        val outputState = AccountState(ourIdentity,UserName,PassWord,FirstName, LastName,Email,Number, listOf(ourIdentity))

        // valid or invalid in contract
        val cmd = Command(AccountContract.Commands.UserAccount(),ourIdentity.owningKey)

        //add transaction Builder
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(outputState, AccountContract.Account_Contract_ID)
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