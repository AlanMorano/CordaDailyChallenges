package com.template

import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class UserContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val User_Contract_ID = "com.template.UserContract"
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Register : Commands
        class Update : Commands
    }
    
    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val command = tx.getCommand<CommandData>(0)

        requireThat {
            when(command.value){
            is Commands.Register -> {

            }
            is Commands.Update ->{

            }}
        }
        }

}

// *********
// * State *
// *********
data class UserState ( val Node: Party,
                    val Name: String,
                    val Age: Int,
                    val Address : String,
                       val BirthDate: String,
                       val Status: String,
                       val Religion: String,
                       val isVerified : Boolean = false): ContractState{
    override val participants = listOf(Node)
}
