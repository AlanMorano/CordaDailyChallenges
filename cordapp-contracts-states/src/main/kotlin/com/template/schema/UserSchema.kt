package com.template.schema

import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object IOUSchema

object UserSchema : MappedSchema(
        schemaFamily = UserSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentIOU::class.java)) {
    @Entity
    @Table(name = "user_states")
    class PersistentIOU(
            @Column(name = "node")
            var node: String,

            @Column(name = "name")
            var name: String,

            @Column(name = "age")
            var age : Int,

            @Column(name = "address")
            var address: String,

            @Column(name = "birthDate")
            var birthDate: String,

            @Column(name = "status")
             var status: String,

            @Column(name = "religion")
            var religion: String,

            @Column(name = "isVerified")
            var isVerified: Boolean,

            @Column(name = "linearId")
             var linearId: UUID
    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor(): this("","", 0,"","","","", false, UUID.randomUUID())
    }
}