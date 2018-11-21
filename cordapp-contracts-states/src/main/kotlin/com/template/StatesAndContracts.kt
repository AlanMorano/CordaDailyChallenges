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
        class Verify : Commands
    }
    
    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val command = tx.getCommand<CommandData>(0)

        requireThat {
            when(command.value){
            is Commands.Register -> {
                "Can't reissue an existing State" using (tx.inputs.isEmpty())
                "Transaction must have one output" using (tx.outputs.size == 1)
                //"Input States are signed by command Signer" using (tx.inputs.Node.owningKey in command.signers)

            }
            is Commands.Update ->{
                "Transaction must have one input" using (tx.inputs.size == 1)
                "Transaction must have one output" using (tx.outputs.size == 1)
                //"Output States are signed by command Signer" using (tx.outputs.Node.owningKey in command.signers)


            }
            is Commands.Verify ->{
                "Transaction must have one input" using (tx.inputs.size == 1)
                "Transaction must have one output" using (tx.outputs.size == 1)
                //"Output States are signed by command Signer" using (tx.outputs.Node.owningKey in command.signers)

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
