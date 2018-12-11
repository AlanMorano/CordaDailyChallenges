package com.template.contract

import com.template.states.KYCRequestState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

class KYCRequestContract : Contract {
    companion object {

       const val Request_ID = "com.template.contract.KYCRequestContract"
    }

    interface Commands : CommandData {
        class Request : TypeOnlyCommandData(), Commands
        class AcceptRequest : TypeOnlyCommandData(), Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<KYCRequestContract.Commands>()

        when(command.value){

            is Commands.Request -> requireThat {
                val outputRequest = tx.outputsOfType<KYCRequestState>()

                "There is no input" using (tx.inputStates.isEmpty())
                "There is exactly one output" using (tx.outputStates.size == 1)
                "The output is of type KYCRequestState" using (tx.outputStates[0] is KYCRequestState)
                "Accepted must be defaulted into false" using (!outputRequest.single().accepted)
                "Only two parties are involved" using (outputRequest.single().listOfParties.size == 2)


            }
            is Commands.AcceptRequest -> requireThat {
                val inputAcceptReq = tx.inputsOfType<KYCRequestState>()
                val outputAcceptReq = tx.outRefsOfType<KYCRequestState>()
                "Only one input KYCState must be consumed" using (inputAcceptReq.size == 1)
                "Only one output KYCState must be created" using (outputAcceptReq.size == 1)
                "Command must be SendRequest" using (command.value is Commands.AcceptRequest)


            }

        }
    }
}