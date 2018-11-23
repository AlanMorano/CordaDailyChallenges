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
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class DisseminateFlow(private  val linearId: UniqueIdentifier) : FlowLogic<Unit>(){

    override  val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(){
        /* Step 1 - Build the transaction */
        val inputRequestCriteria = QueryCriteria.VaultQueryCriteria()
        val inputRequestStateAndRef = serviceHub.vaultService.queryBy<RequestState>(inputRequestCriteria).states
//        val request = inputRequestStateAndRef.state.data

        val inputUserCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
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
        val cmd = Command(UserContract.Commands.Disseminate(), ourIdentity.owningKey)

        val txBuilder = TransactionBuilder(notary)
                .addInputState(inputUserStateAndRef)
                .addOutputState(outputState, UserContract.ID)
                .addCommand(cmd)
        /* Step 2 - Verify the transaction */

        for(state in inputRequestStateAndRef) {
            if (state.state.data.Id == linearId) {
                participants.add(state.state.data.requestParty)
                txBuilder.addInputState(state)

            }
        }

        txBuilder.verify(serviceHub)
        /* Step 3 - Sign the transaction */

        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        /* Step 4 and 5 - Notarize then Record the transaction */
        subFlow(FinalityFlow(signedTx))
    }
}