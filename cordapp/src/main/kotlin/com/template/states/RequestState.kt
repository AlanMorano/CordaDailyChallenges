 package com.template.states

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

data class RequestState( val OtherParty : Party,
                         val requestParty : Party,
                         val Id: UniqueIdentifier): ContractState{
    override val participants = listOf(OtherParty,requestParty)
}
