package com.template.contract

import com.template.states.RequestKYCState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

class RequestKYCContract : Contract {
    companion object {

       const val Request_ID = "com.template.contract.RequestKYCContract"
    }

    interface Commands : CommandData {
        class Request : TypeOnlyCommandData(), Commands
        class AcceptRequest : TypeOnlyCommandData(), Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<RequestKYCContract.Commands>()

        when(command.value){

            is Commands.Request -> requireThat {
                val outputRequest = tx.outputsOfType<RequestKYCState>()

                "There is no input" using (tx.inputStates.isEmpty())
                "There is exactly one output" using (tx.outputStates.size == 1)
                "The output is of type RequestKYCState" using (tx.outputStates[0] is RequestKYCState)
                "Accepted must be defaulted into false" using (!outputRequest.single().accepted)
                "Only two parties are involved" using (outputRequest.single().listOfParties.size == 2)


            }
            is Commands.AcceptRequest -> requireThat {
                val inputAcceptReq = tx.inputsOfType<RequestKYCState>()
                val outputAcceptReq = tx.outRefsOfType<RequestKYCState>()
                "Only one input UserState must be consumed" using (inputAcceptReq.size == 1)
                "Only one output UserState must be created" using (outputAcceptReq.size == 1)
                "Command must be SendRequest" using (command.value is Commands.AcceptRequest)


            }

        }
    }
}