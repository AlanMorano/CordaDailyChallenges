package com.template.contracts

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
    }

    override fun verify(tx: LedgerTransaction){
        val command = tx.getCommand<CommandData>(0)
        requireThat {
            when(command.value){
                is Commands.Register -> {}
                is Commands.Update -> {}
            }
        }
    }


}