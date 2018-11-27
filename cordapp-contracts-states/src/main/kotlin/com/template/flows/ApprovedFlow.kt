package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.RequestContract
import com.template.contracts.UserContract
import com.template.states.RequestState
import com.template.states.UserState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
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

@InitiatingFlow
@StartableByRPC
class ApprovedFlow(private  val Id: UniqueIdentifier) : FlowLogic<Unit>(){



    /* Declare Transaction steps*/

    companion object{
        object BUILDING_TRANSACTION : ProgressTracker.Step("Building Transaction")
        object SIGN_TRANSACTION : ProgressTracker.Step("Signing Transaction")
        object VERIFY_TRANSACTION : ProgressTracker.Step("Verifying Transaction")
        object NOTARIZE_TRANSACTION : ProgressTracker.Step("Notarizing Transaction")
        object RECORD_TRANSACTION : ProgressTracker.Step("Recording Transaction")
    }

    fun tracker() = ProgressTracker(
            BUILDING_TRANSACTION,
            SIGN_TRANSACTION,
            VERIFY_TRANSACTION,
            NOTARIZE_TRANSACTION,
            RECORD_TRANSACTION
    )


    override  val progressTracker = tracker()

    @Suspendable
    override fun call() {
        /* Step 1 - Build the transaction */
        val inputRequestCriteria = QueryCriteria.VaultQueryCriteria()
        val inputRequestStateAndRef = serviceHub.vaultService.queryBy<RequestState>(inputRequestCriteria).states


        val inputUserCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(Id))
        val inputUserStateAndRef = serviceHub.vaultService.queryBy<UserState>(inputUserCriteria).states.single()
        val user = inputUserStateAndRef.state.data

        val participants = mutableListOf<Party>()

        for(data in user.participants){
            participants.add(data)

        }

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val outputState = UserState(
                ourIdentity,
                user.name,
                user.age,
                user.address,
                user.birthDate,
                user.status,
                user.religion,
                user.isVerified,
                participants,
                user.linearId
        )
        val cmd = Command(UserContract.Commands.Approved(), ourIdentity.owningKey)

        val txBuilder = TransactionBuilder(notary)
                .addInputState(inputUserStateAndRef)
                .addOutputState(outputState, UserContract.ID)
                .addCommand(cmd)
        progressTracker.currentStep = BUILDING_TRANSACTION


        /* Step 2 - Verify the transaction */
        progressTracker.currentStep = VERIFY_TRANSACTION
        for(state in inputRequestStateAndRef) {
            if (state.state.data.Id == Id) {
                participants.add(state.state.data.requestParty)
                txBuilder.addInputState(state)

            }
        }

        txBuilder.verify(serviceHub)

        /* Step 3 - Sign the transaction */
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        progressTracker.currentStep = SIGN_TRANSACTION



        /* Step 4 and 5 - Notarize then Record the transaction */
        progressTracker.currentStep = NOTARIZE_TRANSACTION
        progressTracker.currentStep = RECORD_TRANSACTION
         subFlow(FinalityFlow(signedTx))

    }
}