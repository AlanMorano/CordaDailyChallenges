package com.template.contracts

import com.template.states.UserState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class UserContract : Contract {

    companion object {
        const val ID ="com.template.contracts.UserContract"
    }
    interface Commands : CommandData{
        class Register : Commands
        class Update : Commands
        class Verify : Commands
        class Disseminate: Commands
    }

    override fun verify(tx: LedgerTransaction){
        val command = tx.getCommand<CommandData>(0)
        requireThat {
            when(command.value){
                is Commands.Register -> {
                    "No inputs should be consumed when issuing an IOU." using (tx.inputs.isEmpty())
                    "Only one output state should be creating a record" using (tx.outputs.size == 1)
                    "Output must be a TokenState" using (tx.getOutput(0) is UserState)
                }
                is Commands.Update -> {
                    "Transaction must have one input" using (tx.inputs.size == 1)
                    "Transaction must have one output" using (tx.outputs.size == 1)
                }
                is Commands.Verify ->{
                    "Transaction must have one input" using (tx.inputs.size == 1)
                    "Transaction must have one output" using (tx.outputs.size == 1)

                }
            }
        }
    }


}