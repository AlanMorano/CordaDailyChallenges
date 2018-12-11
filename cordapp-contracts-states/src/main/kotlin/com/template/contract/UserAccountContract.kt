package com.template.contract

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

class UserAccountContract : Contract {
    companion object {
        const val User_ID = "com.template.contract.UserAccountContract"
    }
    interface Commands : CommandData {
        class Register : TypeOnlyCommandData(), Commands
    }


    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<UserAccountContract.Commands>()

        when(command.value){

            is Commands.Register -> requireThat {

            }
        }

    }

}