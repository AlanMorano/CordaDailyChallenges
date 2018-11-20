package com.template.flow

import co.paralleluniverse.fibers.Suspendable
import com.template.contract.UserContract
import com.template.contract.UserContract.Companion.ID
import com.template.states.UserState
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.schemas.QueryableState
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

object UserValidateFlow {

    @InitiatingFlow
    @StartableByRPC
    class Initiator : FlowLogic<SignedTransaction>(){

        override val progressTracker = ProgressTracker(GETTING_NOTARY, GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION, SIGNING_TRANSACTION, FINALISING_TRANSACTION)

        @Suspendable
        override fun call(): SignedTransaction {
            progressTracker.currentStep = GETTING_NOTARY
            val notary = serviceHub.networkMapCache.notaryIdentities.first()


            progressTracker.currentStep = GENERATING_TRANSACTION

            val criteria = QueryCriteria.LinearStateQueryCriteria(participants = listOf(ourIdentity))
            val inputState = serviceHub.vaultService.queryBy<UserState>(criteria).states.single()
            val inputStateData = inputState.state.data

            val verification = true
            val outputState = UserState(inputStateData.node,inputStateData.name,inputStateData.age,inputStateData.address,inputStateData.birthDate,inputStateData.status,inputStateData.religion,verification)

            val txCommand =
                    Command(UserContract.Commands.Validate(),ourIdentity.owningKey)

            val txBuilder = TransactionBuilder(notary)
                    .addInputState(inputState)
                    .addOutputState(outputState, ID)
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