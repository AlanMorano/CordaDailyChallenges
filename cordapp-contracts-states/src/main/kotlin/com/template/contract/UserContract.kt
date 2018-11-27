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

            is Commands.Validate -> requireThat {
                val inputValidate = tx.inputsOfType<UserState>()
                val outputValidate = tx.outputsOfType<UserState>()
                "Only one input should be consumed when validating" using (inputValidate.size == 1)
                "Only one output should be consumed when validating" using (outputValidate.size == 1)

                "Input must be UserState" using (tx.inputStates[0] is UserState)
                "Output must be UserState" using (tx.outputStates[0] is UserState)

                "Input Verified must be false" using (!inputValidate.single().isVerified)
                "Output Verified must be true" using (outputValidate.single().isVerified)
                val inputValidateState = inputValidate.single()
                val outputValidateState = inputValidate.single()
                "Node inputState and outputState" using (inputValidateState.node == outputValidateState.node)
                "Name inputState and outputState" using (inputValidateState.name == outputValidateState.name)
                "Age inputState and outputState" using (inputValidateState.age == outputValidateState.age)
                "Address inputState and outputState" using (inputValidateState.address == outputValidateState.address)
                "Birthday inputState and outputState" using (inputValidateState.birthDate == outputValidateState.birthDate)
                "Status inputState and outputState" using (inputValidateState.status == outputValidateState.status)
                "Religion inputState and outputState" using (inputValidateState.religion == outputValidateState.religion)
                "List inputState and outputState" using (inputValidateState.listOfParties == outputValidateState.listOfParties)

            }

            is Commands.Update -> requireThat {
                val inputUpdate = tx.inputsOfType<UserState>()
                val outputUpdate = tx.outputsOfType<UserState>()
                "Only one input should be consumed when validating" using (inputUpdate.size == 1)
                "Only one output should be consumed when validating" using (outputUpdate.size == 1)
                "Input must be UserState" using (tx.inputStates[0] is UserState)
                "Output must be UserState" using (tx.inputStates[0] is UserState)
                "Command must be Update" using (command.value is Commands.Update)


            }

            is Commands.SendRequest -> requireThat {
                val inputSendReq = tx.inputsOfType<UserState>()
                val outputSendReq = tx.outRefsOfType<UserState>()
                "Only one input UserState must be consumed" using (inputSendReq.size == 1)
                "Only one output UserState must be created" using (outputSendReq.size == 1)
                "Command must be SendRequest" using (command.value is Commands.SendRequest)
                val inputSendReqState = inputSendReq.single()
                val outputSendReqState = inputSendReq.single()
               // "OutputState should have new Node" using (inputSendReqState.node != outputSendReqState.node)
                "Name inputState and outputState" using (inputSendReqState.name == outputSendReqState.name)
                "Age inputState and outputState" using (inputSendReqState.age == outputSendReqState.age)
                "Address inputState and outputState" using (inputSendReqState.address == outputSendReqState.address)
                "Birthday inputState and outputState" using (inputSendReqState.birthDate == outputSendReqState.birthDate)
                "Status inputState and outputState" using (inputSendReqState.status == outputSendReqState.status)
                "Religion inputState and outputState" using (inputSendReqState.religion == outputSendReqState.religion)
                "isVerified inputState and outputState" using (inputSendReqState.isVerified == outputSendReqState.isVerified)
//                val currentParticipantSize  = inputSendReqState.listOfParties.size //1
//                val newParticipantSize = outputSendReqState.listOfParties.size //2
//                "New one participant should be added" using ((currentParticipantSize+1) == newParticipantSize)

            }




        }
    }
}