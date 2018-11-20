//package com.template
////
////import net.corda.core.contracts.CommandData
////import net.corda.core.contracts.Contract
////import net.corda.core.contracts.ContractState
////import net.corda.core.contracts.requireThat
////import net.corda.core.identity.Party
////import net.corda.core.transactions.LedgerTransaction
////
////
////
////class UserContract : Contract {
////
////    companion object {
////        const val ID ="com.template.UserContract"
////    }
////    interface Commands : CommandData {
////        class Register : Commands
//////        class Update : Commands
////    }
////
////    override fun verify(tx: LedgerTransaction){
////        val command = tx.getCommand<CommandData>(0)
////        requireThat {
////            when(command.value){
////                is Commands.Register -> {}
////            }
////        }
////    }
////}
////
////
////data class UserState(
////        val ownParty: Party,
////        val name: String,
////        val age: Int,
////        val address: String,
////        val birthDate: String,
////        val status: String,
////        val religion: String,
////        val isVerified: Boolean = false
////): ContractState {
////    override val participants = listOf(ownParty)}