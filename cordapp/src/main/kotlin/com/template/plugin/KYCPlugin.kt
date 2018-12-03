package com.template.plugin

import com.template.api.SuperAPI
import net.corda.webserver.services.WebServerPluginRegistry
import java.util.function.Function

class KYCPlugin : WebServerPluginRegistry {
    override val webApis = listOf(Function(::SuperAPI))
//    override val staticServeDirs = mapOf(
//            "kyc" to javaClass.classLoader.getResource("static").toExternalForm())

}
