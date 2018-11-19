package com.template.states

import com.template.schema.UserSchema

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import java.util.*

data class UserState(val node : Party,
                     val name : String,
                     val age : Int,
                     val address : String,
                     val birthDate : String,
                     val status : String,
                     val religion : String,
                     val isVerified : Boolean,
                     override val linearId: UniqueIdentifier = UniqueIdentifier()):
        LinearState, QueryableState, ContractState{


    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is UserSchema -> UserSchema.PersistentIOU(
                    this.node.name.toString(),
                    this.name,
                    this.age,
                    this.address,
                    this.birthDate,
                    this.status,
                    this.religion,
                    this.isVerified,
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }
    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(UserSchema)
    override val participants: List<AbstractParty> get() = listOf(node)






}
