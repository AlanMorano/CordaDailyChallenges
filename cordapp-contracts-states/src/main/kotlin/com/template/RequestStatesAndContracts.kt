package com.template

import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class RequestContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val Request_Contract_ID = "com.template.RequestContract"
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Request : Commands
        class Share : Commands
        class Remove : Commands
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val command = tx.getCommand<CommandData>(0)

        requireThat {
            when(command.value){
                is Commands.Request ->{
                }
                is Commands.Share ->{
                }
                is Commands.Remove ->{
                }
            }
        }
    }
}


// *********
// * State *
// *********

data class RequestState (val ownNode: Party,
                     val requestNode: Party,
                     val IdState: UniqueIdentifier) : ContractState {

    //participants are owningNode and requestingNode in this State
    override val participants = listOf(ownNode,requestNode)
}
