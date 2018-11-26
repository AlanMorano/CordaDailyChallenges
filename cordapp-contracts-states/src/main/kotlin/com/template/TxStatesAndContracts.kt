package com.template

import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class GetContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val Get_Contract_ID = "com.template.GetContract"
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

                    "Transaction must have one output" using (tx.outputs.size == 1)
                }

                is Commands.Share ->{

                    "Transaction must have one output" using (tx.outputs.size == 1)
                }
                is Commands.Remove ->{

                    "Transaction must have one output" using (tx.outputs.size == 1)
                }
            }
        }
    }
}


// *********
// * State *
// *********

data class GetState (val ownNode: Party,
                     val requestNode: Party,
                     val IdState: UniqueIdentifier) : ContractState {

    //participants are owningNode and requestingNode in this State
    override val participants = listOf(ownNode,requestNode)
}
