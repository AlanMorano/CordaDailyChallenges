package com.template.states
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.contracts.ContractState


data class KYCState(val node : Party,
                    val name : String,
                    val age : Int,
                    val address : String,
                    val birthDate : String,
                    val status : String,
                    val religion : String,
                    val isVerified : Boolean,
                    val listOfParties : List<Party>,
                    override val linearId: UniqueIdentifier = UniqueIdentifier()): ContractState, LinearState{
    override val participants = listOfParties


    }




