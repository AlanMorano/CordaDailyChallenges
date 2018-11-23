package com.template.contracts

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
        class Disseminate: Commands
    }

    override fun verify(tx: LedgerTransaction){
         val command = tx.getCommand<CommandData>(0)
        requireThat{
            when(command.value){
                is Commands.Request ->{}
            }
        }
    }
}