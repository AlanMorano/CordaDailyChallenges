package com.template.flow

import co.paralleluniverse.fibers.Suspendable
import com.template.contract.UserContract
import com.template.contract.UserContract.Companion.User_ID
import com.template.states.UserState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

object UserUpdateFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(private val reqName : String,
                    private val name : String,
                    private val age : Int,
                    private val address : String,
                    private val birthday :  String,
                    private val status : String,
                    private val religion : String) : FlowLogic<SignedTransaction>(){

        override val progressTracker = ProgressTracker(GETTING_NOTARY, GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION, SIGNING_TRANSACTION, FINALISING_TRANSACTION)

        @Suspendable
        override fun call(): SignedTransaction {
            progressTracker.currentStep = GETTING_NOTARY
            val notary = serviceHub.networkMapCache.notaryIdentities.first()



            progressTracker.currentStep = GENERATING_TRANSACTION
            val inputUserCriteria = QueryCriteria.VaultQueryCriteria()
            val userStates = serviceHub.vaultService.queryBy<UserState>(inputUserCriteria).states

            val inputtedUserStateAndRef = userStates.find { stateAndRef -> stateAndRef.state.data.name == reqName }
           ?: throw java.lang.IllegalArgumentException("No User state that matches with name")


            val outputUserState = UserState(ourIdentity,name,age,address,birthday,status,religion,inputtedUserStateAndRef.state.data.isVerified, listOf(ourIdentity))
            val txCommand = Command(UserContract.Commands.Update(), ourIdentity.owningKey)
            val txBuilder = TransactionBuilder(notary)
                    .addInputState(inputtedUserStateAndRef)
                    .addOutputState(outputUserState, UserContract.User_ID)
                    .addCommand(txCommand)



            progressTracker.currentStep = VERIFYING_TRANSACTION
            txBuilder.verify(serviceHub)

            progressTracker.currentStep = SIGNING_TRANSACTION
            val partySignedTx =
                    serviceHub.signInitialTransaction(txBuilder)

            progressTracker.currentStep = FINALISING_TRANSACTION
            return subFlow(FinalityFlow(partySignedTx))



        }

    }

}
