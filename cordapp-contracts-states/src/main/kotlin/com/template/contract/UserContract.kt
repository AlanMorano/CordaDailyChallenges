package com.template.contract

import net.corda.core.contracts.*
import net.corda.core.contracts.Requirements.using
import net.corda.core.transactions.LedgerTransaction

class UserContract : Contract {
    companion object {
        @JvmStatic
        val ID = "com.template.contract.UserContract"
    }

    interface Commands : CommandData {
        class Register : TypeOnlyCommandData(), Commands
    }
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<UserContract.Commands>()

        when(command.value){

            is Commands.Register -> requireThat {


//                "No inputs should be consumed when issuing an IOU." using (tx.inputs.isEmpty())
//                "Only one output state should be created when issuing an IOU." using (tx.outputs.size == 1)
//                val out = tx.outputStates.single() as TokenState
//                "A newly issued IOU must have a positive amount." using (out.value > 0)
//                // "The lender and owner cannot have the same identity." using (out.owner != out.lender)
//                "Both lender and owner together only may sign IOU issue transaction." using
//                        (command.signers.toSet() == out.participants.map { it.owningKey }.toSet())
//                "Output must be a TokenState" using (tx.getOutput(0) is TokenState)

            }

        }
    }
}