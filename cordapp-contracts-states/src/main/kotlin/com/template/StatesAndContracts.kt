package com.template

import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class UserContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val USER_CONTRACT_ID = "com.template.UserContract"
    }

    interface Commands : CommandData {
        class Register : Commands
        class Update : Commands
    }

    override fun verify(tx: LedgerTransaction) {

    }
}

// *********
// * State *
// *********
data class UserState(val owningNode: Party,
                     val name: String,
                     val age: Int,
                     val address: String,
                     val birthDate: String,
                     val status: String,
                     val religion: String,
                     val isVerified: Boolean) : ContractState {
    override val participants = listOf(owningNode)
}