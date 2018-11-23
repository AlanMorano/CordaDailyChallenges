package com.template.contract

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

class RequestContract : Contract {
    companion object {

       const val Request_ID = "com.template.contract.RequestContract"
    }

    interface Commands : CommandData {
        class Request : TypeOnlyCommandData(), Commands
        class AcceptRequest : TypeOnlyCommandData(), Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<RequestContract.Commands>()

//        when(command.value){
//
//            is Commands.Request -> requireThat {
//                "There are no inputs" using (tx.inputStates.isEmpty())
//                "There is exactly one output" using (tx.outputStates.size == 1)
//                "The output is of type RequestState" using (tx.outputsOfType<RequestState>().size == 1)
//
//            }
//            is Commands.AcceptRequest -> requireThat {
//                "There are no inputs" using (true)
//                    "There is two output" using (tx.outputStates.size == 2)
//                    "The output is of type RequestState" using (tx.outputsOfType<RequestState>().size == 1)
//
//            }
//
//        }
    }
}