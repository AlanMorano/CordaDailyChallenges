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
import org.apache.logging.log4j.core.tools.picocli.CommandLine

@InitiatingFlow
@StartableByRPC
class UpdateFlow(val name: String,
                 val age: Int,
                 val address: String,
                 val birthDate: String,
                 val status: String,
                 val religion: String): FlowLogic<Unit>(){
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {

        /* Step 1 - Build the transaction */
        val inputCriteria = QueryCriteria.VaultQueryCriteria()
        val inputStateAndRef = serviceHub.vaultService.queryBy<UserState>(inputCriteria).states.first()
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val userState = UserState(ourIdentity,name,age,address,birthDate,status,religion)
        val cmd = Command(UserContract.Commands.Update(),ourIdentity.owningKey)

        val txBuilder = TransactionBuilder(notary)
                .addInputState(inputStateAndRef)
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