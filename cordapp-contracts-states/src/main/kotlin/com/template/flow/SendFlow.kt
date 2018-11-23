package com.template.flow

import co.paralleluniverse.fibers.Suspendable
import com.template.contract.RequestContract
import com.template.contract.RequestContract.Companion.Request_ID
import com.template.contract.UserContract
import com.template.contract.UserContract.Companion.User_ID
import com.template.states.RequestState
import com.template.states.UserState
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

object SendFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(private val requestor :  Party,
                    private val name: String) : FlowLogic<SignedTransaction>(){

        override val progressTracker = ProgressTracker(GETTING_NOTARY, GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION, SIGNING_TRANSACTION, FINALISING_TRANSACTION)

        @Suspendable
        override fun call(): SignedTransaction {
            progressTracker.currentStep = GETTING_NOTARY
            val notary = serviceHub.networkMapCache.notaryIdentities.first()

            val inputUserCriteria = QueryCriteria.VaultQueryCriteria()
            val userStates = serviceHub.vaultService.queryBy<UserState>(inputUserCriteria).states

            val inputtedUserStateAndRef = userStates.find { stateAndRef -> stateAndRef.state.data.name == name }
           ?: throw java.lang.IllegalArgumentException("No User state that matches with name")

//            var inputtedUserStateAndRef : StateAndRef<UserState>? = null
//
//            for(x in userStates){
//                if(x.state.data.name == name){
//                    txBuilder.addInputState(x)
//
//                    inputtedUserStateAndRef = x
//                }
//            }



            val inputtedUserStateData = inputtedUserStateAndRef.state.data

            val name = inputtedUserStateData.name
            val age = inputtedUserStateData.age
            val address = inputtedUserStateData.address
            val birthday = inputtedUserStateData.birthDate
            val status = inputtedUserStateData.status
            val religion = inputtedUserStateData.religion
            val isVerified = inputtedUserStateData.isVerified
            val listOfParties = listOf(requestor,ourIdentity)



            val userState = UserState(this.ourIdentity, name,age,address,birthday,
                    status,religion,isVerified, listOfParties)



            val outputRequestState = RequestState(requestor, this.ourIdentity,name,true, listOf(requestor, ourIdentity))

            val requestStates = serviceHub.vaultService.queryBy<RequestState>().states

            val inputtedRequestAndSendState = requestStates.find { stateAndRef -> stateAndRef.state.data.name == name }
                    ?: throw IllegalArgumentException("No Request state that matches with name $name found.")

           val txCommand = Command(UserContract.Commands.SendRequest(), ourIdentity.owningKey)

            val txCommand2 = Command(RequestContract.Commands.AcceptRequest(), ourIdentity.owningKey)

            val txBuilder = TransactionBuilder(notary)
                    .addInputState(inputtedRequestAndSendState)
                    .addOutputState(userState, User_ID)
                    .addOutputState(outputRequestState, Request_ID)
                    .addCommand(txCommand2)
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