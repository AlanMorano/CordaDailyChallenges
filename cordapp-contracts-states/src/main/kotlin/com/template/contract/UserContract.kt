package com.template.contract

import com.template.states.UserState
import net.corda.core.contracts.*
import net.corda.core.contracts.Requirements.using
import net.corda.core.transactions.LedgerTransaction

class UserContract : Contract {
    companion object {
        @JvmStatic
        val User_ID = "com.template.contract.UserContract"
    }

    interface Commands : CommandData {
        class Register : TypeOnlyCommandData(), Commands
        class Validate : TypeOnlyCommandData(), Commands
    }
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<UserContract.Commands>()

        when(command.value){

            is Commands.Register -> {

                requireThat {
                    "No inputs should be consumed when issuing an IOU." using (tx.inputs.isEmpty())
                    "Only one output state should be creating a record" using (tx.outputs.size == 1)
                    "Output must be a UserState" using (tx.getOutput(0) is UserState)



                }










            }

        }
    }
}