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

                val regOutput = tx.outputStates.single() as UserState
                "No inputs should be consumed" using (tx.inputs.isEmpty())
                "Only one output state should be created" using (tx.outputs.size == 1)
                "The output should be UserState" using (tx.getOutput(0) is UserState)
                "The Signer must be signed" using (command.signers.toSet() == regOutput.parties.map { it.owningKey }.toSet())
            }

            is Commands.Update ->{

                val upInput = tx.inputStates.single() as UserState
                val upOutput = tx.outputStates.single() as UserState
                "Transaction must have one input" using (tx.inputs.size == 1)
                "Transaction must have one output" using (tx.outputs.size == 1)
                "The output should be UserState" using (tx.getOutput(0) is UserState)
                "Input Id should be equal Id output " using (upInput.linearId == upOutput.linearId)
                "The Signer must be signed" using (command.signers.toSet() == upOutput.parties.map { it.owningKey }.toSet())
            }

            is Commands.Verify ->{

                val verInput = tx.inputStates.single() as UserState
                val verOutput = tx.outputStates.single() as UserState
                "Transaction must have one input" using (tx.inputs.size == 1)
                "Transaction must have one output" using (tx.outputs.size == 1)
                "Input Id should be equal Id output " using (verInput.linearId == verOutput.linearId)
                "The Signer must be signed" using (command.signers.toSet() == verOutput.parties.map { it.owningKey }.toSet())
            }
                else -> {}
            }

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
                       val parties: List<Party>,
                       val isVerified : Boolean,
                       override val linearId : UniqueIdentifier = UniqueIdentifier()): LinearState{//, QueryableState{
    //participants are all the list of the parties give its information
    override val participants = parties
//    override fun toString() = "${Node.name}: $Name,$Age,$Address,$BirthDate,$Status,$Religion"
//    override fun supportedSchemas() = listOf(UserSchemaV1)
//    override fun generateMappedObject(schema: MappedSchema) = UserSchemaV1.PersistentUserState(
//            Node.name.toString(),Name,Age,Address,BirthDate,Status,Religion
//    )
//
//    object UserSchema
//
//    object UserSchemaV1 : MappedSchema(UserSchema.javaClass,1, listOf(PersistentUserState::class.java)){
//        @Entity
//        @Table(name = "cos")
//        class PersistentUserState(
//                @Column(name = "Node")
//                var Node: String = "",
//                @Column(name = "name")
//                var Name: String = "",
//                @Column(name = "age")
//                var Age: Int = 0,
//                @Column(name = "address")
//                var Address: String = "",
//                @Column(name = "birthdate")
//                var BirthDate: String = "",
//                @Column(name = "status")
//                var Status: String = "",
//                @Column(name = "religion")
//                var Religion: String = ""
//        ) : PersistentState()
//    }
//
//
}
