package com.template.contracts

import com.template.states.RequestState
import com.template.states.UserState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.contracts.Contract

class RequestContract: Contract{
    companion object{
        const val ID = "com.template.contracts.RequestContract"

    }

    interface Commands : CommandData {
        class Request : Commands
        class Approved : Commands

    }

    override fun verify(tx: LedgerTransaction){
         val command = tx.getCommand<CommandData>(0)
        requireThat{
            when(command.value){
                is Commands.Request ->{
//                    "There is no input" using (tx.outputStates.isEmpty())
//                    "There is exactly one output" using (tx.outputStates.size == 1)
//                    "The output is of type RequestState" using (tx.outputStates[0] is UserState)
                }
                is Commands.Approved ->{
                    val inputApproved = tx.inputsOfType<RequestState>()
                    val outputApproved = tx.outRefsOfType<RequestState>()
                    "Only one input UserState must be consumed" using (inputApproved.size == 1)
                    "Only one output UserState must be created" using (outputApproved.size == 1)
                    "Command must be SendRequest" using (command.value is Commands.Approved)
                }
            }
        }
    }
}