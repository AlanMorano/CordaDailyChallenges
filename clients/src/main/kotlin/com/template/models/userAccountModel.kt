package com.template.models

import com.fasterxml.jackson.annotation.JsonCreator

data class userAccountModel(
        val firstName : String,
        val middleName : String,
        val lastName : String,
        val username : String,
        val password : String,
        val email : String,
        val role : String
)

data class createUserAccount @JsonCreator constructor(
        val firstName : String,
        val middleName : String,
        val lastName : String,
        val username : String,
        val password : String,
        val email : String,
        val role : String
)