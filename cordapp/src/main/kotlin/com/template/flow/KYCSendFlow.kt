package com.template.flow

import co.paralleluniverse.fibers.Suspendable
import com.template.contract.RequestKYCContract
import com.template.contract.RequestKYCContract.Companion.Request_ID
import com.template.contract.KYCContract
import com.template.contract.KYCContract.Companion.User_ID
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

object SendKYCFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(private val requestor :  Party,
                    private val name: String) : FlowLogic<SignedTransaction>(){

        override val progressTracker = ProgressTracker(GETTING_NOTARY, GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION, SIGNING_TRANSACTION, FINALISING_TRANSACTION)

        @Suspendable
        override fun call(): SignedTransaction {

            //Getting of notary
            progressTracker.currentStep = GETTING_NOTARY
            val notary = serviceHub.networkMapCache.notaryIdentities.first()

            //Query all unconsumed state
            val inputUserCriteria = QueryCriteria.VaultQueryCriteria()

            //Get all unconsumed states of type UserState
            val userStates = serviceHub.vaultService.queryBy<UserState>(inputUserCriteria).states

            //Get State And Ref of type UserState that is has a data name that matches with the input name
            val inputtedUserStateAndRef = userStates.find { stateAndRef -> stateAndRef.state.data.name == name }
           ?: throw java.lang.IllegalArgumentException("No User state that matches with name")

            //Access the state and ref data
            val inputtedUserStateData = inputtedUserStateAndRef.state.data

            //Prepare and copy current values of the StateAndRef to be used as outputs
            val name = inputtedUserStateData.name
            val age = inputtedUserStateData.age
            val address = inputtedUserStateData.address
            val birthday = inputtedUserStateData.birthDate
            val status = inputtedUserStateData.status
            val religion = inputtedUserStateData.religion
            val isVerified = inputtedUserStateData.isVerified

            //Get current participants of the StateAndRef and put it in this var
            var partiz = mutableListOf<Party>()
            for(singleParticipant in inputtedUserStateData.participants){
                partiz.add(singleParticipant)
            }
            //Add new participant
            partiz.add(requestor)

            //participants + the new participant
            val listOfParties = partiz



            val userState = UserState(this.ourIdentity, name,age,address,birthday,
                    status,religion,isVerified, listOfParties)

            println(inputtedUserStateAndRef)
            println(userState)

            val outputRequestState = RequestState(this.ourIdentity, requestor,name,true, listOf(requestor, ourIdentity))

            //Get all states with type RequestState
            val requestStates = serviceHub.vaultService.queryBy<RequestState>().states

            //Get requestState that has data name, infoOwner, requestor equals to the inputted data
            val inputtedRequestAndSendState = requestStates.find { stateAndRef ->
                (stateAndRef.state.data.name == name &&
                        stateAndRef.state.data.infoOwner == this.ourIdentity &&
                            stateAndRef.state.data.requestor == requestor)}
                    ?: throw IllegalArgumentException("No Request state that matches with name $name found.")

            println(inputtedRequestAndSendState)
           val txCommand = Command(KYCContract.Commands.SendRequest(), ourIdentity.owningKey)

            val txCommand2 = Command(RequestKYCContract.Commands.AcceptRequest(), ourIdentity.owningKey)

            val txBuilder = TransactionBuilder(notary)
                    .addInputState(inputtedUserStateAndRef)
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