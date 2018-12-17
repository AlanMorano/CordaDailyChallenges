package com.template.webserver

import net.corda.core.contracts.UniqueIdentifier
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

class Customers(
//        @Id @GeneratedValue(strategy = GenerationType.AUTO)
//        val Id: String,
        var name: String="",
        var age: Int = 0,
        var address: String="",
        var birthDate: String="",
        var status: String="",
        var religion: String=""


)

class CustomerUpdate(
        val Id: String ="",
        var name: String="",
        var age: Int = 0,
        var address: String="",
        var birthDate: String="",
        var status: String="",
        var religion: String=""

)
