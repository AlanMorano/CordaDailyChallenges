package com.template.states

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import java.util.*

data class UserState(
                    val ownParty: Party,
                    val name: String,
                    val age: Int,
                    val address: String,
                    val birthDate: String,
                    val status: String,
                    val religion: String,
                    val isVerified: Boolean,
                    override val participants: List<Party>,
                    override  val linearId: UniqueIdentifier) : LinearState
