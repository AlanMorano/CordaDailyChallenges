package com.template.flow

import co.paralleluniverse.fibers.Suspendable
import com.template.contract.UserAccountContract
import com.template.states.UserAccountState
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import com.template.flow.Encryption.md5


class UserAccountRegisterFlow {
    @InitiatingFlow
    @StartableByRPC

    class Initiator(private val firstName : String,
                    private val middleName : String,
                    private val lastName : String,
                    private val userName :  String,
                    private val password : String,
                    private val email : String,
                    private val role : String) : FlowLogic<SignedTransaction>(){

        override val progressTracker = ProgressTracker(GETTING_NOTARY, GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION, SIGNING_TRANSACTION, FINALISING_TRANSACTION)


        @Suspendable
        override fun call(): SignedTransaction {
            //Get first notary
            progressTracker.currentStep = GETTING_NOTARY
            val notary = serviceHub.networkMapCache.notaryIdentities.first()

            progressTracker.currentStep = GENERATING_TRANSACTION

            val userState = UserAccountState(firstName,middleName,lastName,userName,password.md5(),email,role, listOf(ourIdentity))
            val txCommand = Command(UserAccountContract.Commands.Register(), userState.participants.map { it.owningKey })
            val txBuilder = TransactionBuilder(notary)
                    .addOutputState(userState, UserAccountContract.User_ID)
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