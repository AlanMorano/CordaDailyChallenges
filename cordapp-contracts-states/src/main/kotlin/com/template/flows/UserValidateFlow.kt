package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.UserContract
import com.template.states.UserState
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker


@InitiatingFlow
@StartableByRPC
class ValidateFlow : FlowLogic<Unit>(){

    override val progressTracker = ProgressTracker()
    @Suspendable
    override fun call(){
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val criteria = QueryCriteria.VaultQueryCriteria()

        val inputState = serviceHub.vaultService.queryBy<UserState>(criteria).states.single()

        val inputStateData = inputState.state.data

        val verification = true

        val userState = UserState(ourIdentity,inputStateData.name,inputStateData.age,inputStateData.address,inputStateData.birthDate,inputStateData.status,inputStateData.religion,verification)

        val cmd = Command(UserContract.Commands.Validate(),ourIdentity.owningKey)

        val txBuilder = TransactionBuilder(notary)
                .addInputState(inputState)
                .addOutputState(userState,UserContract.ID)
                .addCommand(cmd)
        /* Step 2 - Verify the transaction */

        txBuilder.verify(serviceHub)
        /* Step 3 - Sign the transaction */
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        /* Step 4 and 5 - Notarize then Record the transaction */
        subFlow(FinalityFlow(signedTx))


    }
}