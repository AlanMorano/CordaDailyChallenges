package com.template.states
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import net.corda.webserver.services.WebServerPluginRegistry
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table


data class UserState(val node : Party,
                     val name : String,
                     val age : Int,
                     val address : String,
                     val birthDate : String,
                     val status : String,
                     val religion : String,
                     val isVerified : Boolean,
                     val listOfParties : List<Party>,
                     override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState, QueryableState{
    override val participants = listOfParties
    override fun supportedSchemas() = listOf(UserStateSchemaV1)
    override fun generateMappedObject(schema: MappedSchema) = UserStateSchemaV1.PersistentUserState(
            node.toString(),
            name,
            age.toString(),
            address,birthDate,
            status,
            religion,
            isVerified.toString(),
            listOfParties.toString(),
            participants.toString()
    )
    object UserStateSchema

    object UserStateSchemaV1 : MappedSchema(UserStateSchema.javaClass, 1, listOf(PersistentUserState::class.java)) {
        @Entity
        @Table(name = "user")
        class PersistentUserState(
                @Column(name = "node")
                var node: String = "",
                @Column(name = "name")
                var name: String = "",
                @Column(name = "age")
                var age: String = "",
                @Column(name = "address")
                var address: String = "",
                @Column(name = "birthDate")
                var birthDate: String = "",
                @Column(name = "status")
                var status: String = "",
                @Column(name = "religion")
                var religion: String = "",
                @Column(name = "isVerified")
                var isVerified: String = "",
                @Column(name = "listOfParties")
                var listOfParties: String = "",
                @Column(name = "participants")
                var participants: String = ""

        ) : PersistentState()
    }

    }




