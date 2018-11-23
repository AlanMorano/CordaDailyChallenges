package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.UserContract.Companion.ID
import com.template.contracts.UserContract
import com.template.states.UserState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class RegisterFlow(
        private val ownParty: Party,
        private val name: String,
        private val age: Int,
        private val address: String,
        private val birthDate: String,
        private val status: String,
        private val religion: String,
        private val isVerified: Boolean): FlowLogic<Unit>(){
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(){
        /* Step 1 - Build the transaction */
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val userState =UserState(ownParty,name,age,address,birthDate,status,religion,false, listOf(ourIdentity), UniqueIdentifier())
        val cmd = Command(UserContract.Commands.Register(),ownParty.owningKey)


        val txBuilder = TransactionBuilder(notary)
                .addOutputState(userState,UserContract.ID)
                .addCommand(cmd)
        txBuilder.verify(serviceHub)
        /* Step 2 - Sign the transaction */
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        /* Step 3 - Verify the transaction */
        signedTx.verify(serviceHub)
        /* Step 4 and 5 - Notarize then Record the transaction */
         subFlow(FinalityFlow(signedTx))
    }

}

