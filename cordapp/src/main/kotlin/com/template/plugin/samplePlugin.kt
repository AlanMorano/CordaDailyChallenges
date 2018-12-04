package com.template.plugin

import com.template.api.sampleAPI
import net.corda.webserver.services.WebServerPluginRegistry
import java.util.function.Function

class samplePlugin : WebServerPluginRegistry{

    override val webApis = listOf(Function(::sampleAPI))



override  val staticServeDirs = mapOf(

        "sample" to javaClass.classLoader.getResource("static").toExternalForm()
)

}
