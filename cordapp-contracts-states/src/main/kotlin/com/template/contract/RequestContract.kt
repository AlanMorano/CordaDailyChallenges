package com.template.contract

import com.template.states.RequestState
import com.template.states.UserState
import net.corda.core.contracts.*
import net.corda.core.contracts.Requirements.using
import net.corda.core.transactions.LedgerTransaction

class RequestContract : Contract {
    companion object {
        @JvmStatic
        val Request_ID = "com.template.contract.RequestContract"
    }

    interface Commands : CommandData {
        class Request : TypeOnlyCommandData(), Commands
    }
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<RequestContract.Commands>()

        when(command.value){

            is Commands.Request -> {

                requireThat {
                    "There are no inputs" using (tx.inputStates.isEmpty())
                    "There is exactly one output" using (tx.outputStates.size == 1)
                    "The output is of type RequestState" using (tx.outputsOfType<RequestState>().size == 1)

                }
            }

        }
    }
}