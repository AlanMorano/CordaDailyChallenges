package com.template.states
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party


data class UserState(val node : Party,
                     val name : String,
                     val age : Int,
                     val address : String,
                     val birthDate : String,
                     val status : String,
                     val religion : String,
                     val isVerified : Boolean,
//                   val listOfParties : List<Party>,
                     override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState{

    override val participants = listOf(node)

    }


