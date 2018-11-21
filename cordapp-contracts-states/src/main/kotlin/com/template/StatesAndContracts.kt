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
        const val USER_CONTRACT_ID = "com.template.UserContract"
    }

    interface Commands : CommandData {
        class Register : Commands
        class Update : Commands
        class Verify : Commands
        class Disseminate : Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.getCommand<CommandData>(0)

        requireThat {
            when(command.value) {
                is Commands.Register -> {
                    /* Shape Constraints */
                    "Transaction must have no input" using (tx.inputs.isEmpty())
                    "Transaction must have one output" using (tx.outputs.size == 1)
                }
                is Commands.Update -> {
                    /* Shape Constraints */
                    "Transaction must have one input" using (tx.inputs.size == 1)
                    "Transaction must have one output" using (tx.outputs.size == 1)
                }

                is Commands.Verify -> {
                    /* Shape Constraints */
                    "Transaction must have one input" using (tx.inputs.size == 1)
                    "Transaction must have one output" using (tx.outputs.size == 1)
                }
            }
        }
    }
}

class KYCContract : Contract {
    companion object {
        const val KYC_CONTRACT_ID = "com.template.KYCContract"
    }

    interface Commands : CommandData {
        class Send : Commands
        class Validate :  Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command =  tx.getCommand<CommandData>(0)

        requireThat {
            when(command.value) {
                is Commands.Send -> {
                    /* Shape Constraints */
                    "Transaction must have no input" using (tx.inputs.isEmpty())
                    "Transaction must have one output" using (tx.outputs.size == 1)
                }
                is Commands.Validate -> {
                    /* Shape Constraints */
                    "Transaction must have one input" using (tx.inputs.size == 1)
                    "Transaction must have one output" using (tx.outputs.size == 1)

                    /* Send Specific Constraints */
                    "User must have already sent an ID." using tx.inRef<KYCState>(0).state.data.isSent
                    "User must not yet be validated." using (!tx.inRef<KYCState>(0).state.data.isValidated)
                }
            }
        }
    }
}

class RequestContract : Contract {
    companion object {
        const val REQUEST_CONTRACT_ID = "com.template.RequestContract"
    }

    interface Commands : CommandData {
        class Request : Commands
    }

    override fun verify(tx: LedgerTransaction) {

    }

}

// **********
// * States *
// **********
data class UserState(val owningNode: Party,
                     val name: String,
                     val age: Int,
                     val address: String,
                     val birthDate: String,
                     val status: String,
                     val religion: String,
                     val parties: List<Party>,
                     val isVerified: Boolean = false) : ContractState {
    override val participants = parties
}

data class KYCState(val owningNode: Party,
                    val isSent: Boolean = false,
                    val isValidated: Boolean = false) : ContractState {
    override val participants = listOf(owningNode)
}

data class RequestState(val owningNode: Party,
                        val requestingNode: Party,
                        val isSent: Boolean = false) : ContractState {
    override val participants = listOf(owningNode, requestingNode)
}