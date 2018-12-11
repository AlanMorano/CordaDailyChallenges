package com.template.states

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty


data class UserAccountState(val firstName : String,
                            val middleName : String,
                            val lastName : String,
                            val username : String,
                            val password : String,
                            val email : String,
                            val role : String,
                            override val participants: List<AbstractParty>,
                            override val linearId: UniqueIdentifier = UniqueIdentifier()) : ContractState, LinearState{
}