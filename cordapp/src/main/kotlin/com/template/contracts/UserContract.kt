package com.template.contracts

import com.template.states.UserState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class UserContract : Contract {

    companion object {
        const val ID ="com.template.contracts.UserContract"
    }
    interface Commands : CommandData{
        class Register : Commands
        class Update : Commands
        class Verify : Commands
        class Approved: Commands
        class Request : Commands
        class Remove : Commands
    }

    override fun verify(tx: LedgerTransaction){
        val command = tx.getCommand<CommandData>(0)
        requireThat {
            when(command.value){
                is Commands.Register -> {
                    "No inputs should be consumed when issuing an IOU." using (tx.inputs.isEmpty())
                    "Only one output state should be creating a record" using (tx.outputs.size == 1)
                    "Output must be a TokenState" using (tx.getOutput(0) is UserState)
//                    val outputRegister = tx.outputsOfType<UserState>().single()
//                    "Must be signed by the Registering nodes" using (command.signers.toSet() == outputRegister.participants.map {it.owningKey})
//                    "Name must not be empty" using (outputRegister.name.isNotEmpty())
//                    "Age must not be empty" using (outputRegister.age >= 1)
//                    "Address must not be empty" using (outputRegister.address.isNotEmpty())
//                    "Birthday must not be empty" using (outputRegister.birthDate.isNotEmpty())
//                    "Status must not be empty" using (outputRegister.status.isNotEmpty())
//                    "Religion must not be empty" using (outputRegister.religion.isNotEmpty())
//                    "Validation must be defaulted into false" using (!outputRegister.isVerified)
//                    "Participants must only be the registering node" using (outputRegister.participants.size == 1)


                }
                is Commands.Update -> {
//                    "Transaction must have one input" using (tx.inputs.size == 1)
//                    "Transaction must have one output" using (tx.outputs.size == 1)
                    val inputUpdate = tx.inputsOfType<UserState>()
                    val outputUpdate = tx.outputsOfType<UserState>()
                    "Only one input should be consumed when validating" using (inputUpdate.size == 1)
                    "Only one output should be consumed when validating" using (outputUpdate.size == 1)
                    "Input must be UserState" using (tx.inputStates[0] is UserState)
                    "Output must be UserState" using (tx.inputStates[0] is UserState)
                    "Command must be Update" using (command.value is Commands.Update)

                }
                is Commands.Verify ->{
//                    "Transaction must have one input" using (tx.inputs.size == 1)
//                    "Transaction must have one output" using (tx.outputs.size == 1)
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
                    "Node inputState and outputState" using (inputValidateState.ownParty == outputValidateState.ownParty)
                    "Name inputState and outputState" using (inputValidateState.name == outputValidateState.name)
                    "Age inputState and outputState" using (inputValidateState.age == outputValidateState.age)
                    "Address inputState and outputState" using (inputValidateState.address == outputValidateState.address)
                    "Birthday inputState and outputState" using (inputValidateState.birthDate == outputValidateState.birthDate)
                    "Status inputState and outputState" using (inputValidateState.status == outputValidateState.status)
                    "Religion inputState and outputState" using (inputValidateState.religion == outputValidateState.religion)
                    "List inputState and outputState" using (inputValidateState.participants == outputValidateState.participants)

                }


                is Commands.Request ->{
//                    val inputRequest = tx.inputsOfType<UserState>()
//                    val outputRequest = tx.outRefsOfType<UserState>()
//                    "Only one input UserState must be consumed" using (inputRequest.size == 1)
//                    "Only one output UserState must be created" using (outputRequest.size == 1)
//                    "Command must be SendRequest" using (command.value is Commands.Request)
//                    val inputRequestState = inputRequest.single()
//                    val outputSendReqState = inputRequest.single()
//
//                    // "OutputState should have new Node" using (inputRequestState.node != outputSendReqState.node)
//                    "Name inputState and outputState" using (inputRequestState.name == outputSendReqState.name)
//                    "Age inputState and outputState" using (inputRequestState.age == outputSendReqState.age)
//                    "Address inputState and outputState" using (inputRequestState.address == outputSendReqState.address)
//                    "Birthday inputState and outputState" using (inputRequestState.birthDate == outputSendReqState.birthDate)
//                    "Status inputState and outputState" using (inputRequestState.status == outputSendReqState.status)
//                    "Religion inputState and outputState" using (inputRequestState.religion == outputSendReqState.religion)
//                    "isVerified inputState and outputState" using (inputRequestState.isVerified == outputSendReqState.isVerified)

                }

            }
        }
    }


}