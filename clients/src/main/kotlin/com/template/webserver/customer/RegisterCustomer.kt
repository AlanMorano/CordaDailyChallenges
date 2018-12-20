package com.template.webserver.customer

class Customers(
//        @Id @GeneratedValue(strategy = GenerationType.AUTO)
//        val Id: String,
        var name: String="",
        var age: Int = 0,
        var address: String="",
        var birthDate: String="",
        var status: String="",
        var religion: String="")