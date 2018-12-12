package com.template

import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction

class AccountContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val Account_Contract_ID = "com.template.UserContract"
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class UserAccount : Commands
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val command = tx.getCommand<CommandData>(0)

        requireThat {
            when(command.value){
                is Commands.UserAccount -> {}
                else -> {}
            }

        }
    }

}
// *********
// * State *
// *********
data class AccountState (val Node: Party,
                         val Username: String,
                         val Password: String,
                         val Firstname: String,
                         val Lastname: String,
                         val Email: String,
                         val Number: Int,
                         val parties: List<Party>,
                      override val linearId : UniqueIdentifier = UniqueIdentifier()): LinearState {//, QueryableState{
//participants are all the list of the parties give its information
override val participants = parties}