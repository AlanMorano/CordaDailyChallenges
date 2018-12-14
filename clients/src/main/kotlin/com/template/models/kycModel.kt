package com.template.models

import com.fasterxml.jackson.annotation.JsonCreator
import net.corda.core.contracts.UniqueIdentifier

data class kycModel(
        val node : String,
        val name : String,
        val age : Int,
        val address : String,
        val birthday : String,
        val status : String,
        val religion : String,
        val isVerified : Boolean,
        val listOfParties : Any,
        val linearId : Any
)

data class createKYC @JsonCreator constructor(
        val name : String,
        val age : Int,
        val address : String,
        val birthday : String,
        val status : String,
        val religion: String
)