package com.template.states

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

data class RequestState(val infoOwner : Party,
                        val requestor : Party,
                        val name : String,
                        val accepted: Boolean,
                    val listOfParties : List<Party>,
                        override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {
    override val participants = listOfParties
}