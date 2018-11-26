package com.template.contract

import com.template.states.UserState
import net.corda.core.contracts.*
import net.corda.core.contracts.Requirements.using
import net.corda.core.transactions.LedgerTransaction

class UserContract : Contract {
    companion object {

        const val User_ID = "com.template.contract.UserContract"
    }

    interface Commands : CommandData {
        class Register : TypeOnlyCommandData(), Commands
        class Validate : TypeOnlyCommandData(), Commands
        class Update : TypeOnlyCommandData(), Commands
        class SendRequest : TypeOnlyCommandData(), Commands
    }
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<UserContract.Commands>()

        when(command.value){

            is Commands.Register ->   requireThat {
                "No inputs should be consumed when creating KYC." using (tx.inputs.isEmpty())
                "Only one output state should be created" using (tx.outputs.size == 1)
                "Output must be a UserState" using (tx.getOutput(0) is UserState)
                val outputRegister = tx.outputsOfType<UserState>().single()
                "Must be signed by the Registering node" using (command.signers.toSet() == outputRegister.participants.map { it.owningKey }.toSet())
                "Name must not be empty" using (outputRegister.name.isNotEmpty())
                "Age must not be empty" using (outputRegister.age >=1)
                "Address must not be empty" using (outputRegister.address.isNotEmpty())
                "Birthday must not be empty" using (outputRegister.birthDate.isNotEmpty())
                "Status must not be empty" using (outputRegister.status.isNotEmpty())
                "Religion must not be empty" using (outputRegister.religion.isNotEmpty())
                "Validation must be defaulted into false" using (!outputRegister.isVerified)
                "Participants must only be the registering node" using (outputRegister.participants.size == 1)
            }

//            is Commands.Validate -> requireThat {
//                val inputValidate = tx.inputsOfType<UserState>()
//                val outputValidate = tx.outputsOfType<UserState>()
//                "Only one input should be consumed when validating" using (inputValidate.size == 1)
//                "Only one output should be consumed when validating" using (outputValidate.size == 1)
//                "Input must be UserState" using (tx.getInput(0) is UserState)
//                "Output must be UserState" using (tx.getOutput(0) is UserState)
//                "Input Verified must be false" using (!inputValidate.single().isVerified)
//                "Output Verified must be true" using (inputValidate.single().isVerified)
//
//
//
//
//            }

            is Commands.Update -> requireThat {

            }

            is Commands.SendRequest -> requireThat {

            }




        }
    }
}