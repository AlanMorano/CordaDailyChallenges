package com.template.webserver

import com.fasterxml.jackson.annotation.JsonCreator
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party


data class Logs(
        val Username: String,
        val Password: String
)

data class NewRegsKYC @JsonCreator constructor(
        val Name: String,
        val Age: Int,
        val Address: String,
        val BirthDate: String,
        val Status: String,
        val Religion: String
)

data class NewUpdateKYC @JsonCreator constructor(
        val NewName: String,
        val NewAge: Int,
        val NewAddress: String,
        val NewBirthDate: String,
        val NewStatus: String,
        val NewReligion: String,
        val ID: String
)

data class NewRegsUser @JsonCreator constructor(
        val Username: String,
        val Password: String,
        val Firstname: String,
        val Lastname: String,
        val Email: String,
        val Number: Int
)

data class NewUpdateUser @JsonCreator constructor(
        val NewFirstname: String,
        val NewLastname: String,
        val NewEmail: String,
        val NewNumber: Int,
        val ID: String
)

data class NewRegsRequest @JsonCreator constructor(
        val Owner: String,
        val ID: String
)

data class NewShare @JsonCreator constructor(
        val ID: String
)