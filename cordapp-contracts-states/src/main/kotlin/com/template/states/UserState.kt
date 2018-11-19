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

/*
User State {
Name, Age, Address, BirthDate, Status, Religion, isVerified }

*/


data class UserState(val name : Party,
                     val age : Int,
                     val address : String,
                     val birthDate : Date,
                     val status : String,
                     val religion : String,
                     val isVerified : Boolean,
                     override val linearId: UniqueIdentifier = UniqueIdentifier()):
        LinearState, QueryableState, ContractState{


    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is UserSchema -> UserSchema.PersistentIOU(
                    this.name.name.toString(),
                    this.age,
                    this.address,
                    this.birthDate.toString(),
                    this.status,
                    this.religion,
                    this.isVerified,
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }
    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(UserSchema)
    override val participants: List<AbstractParty> get() = listOf(name)






}
