package com.bolyartech.totoproverka3.server.main

import com.bolyartech.forge.server.HttpMethod
import com.bolyartech.forge.server.handler.StaticFileHandler
import com.bolyartech.forge.server.misc.MimeTypeResolverImpl
import com.bolyartech.forge.server.module.HttpModule
import com.bolyartech.forge.server.route.Route
import com.bolyartech.forge.server.route.RouteImpl
import org.example.kforge.dagger.StaticFilesDir
import org.example.kforge.modules.main.endpoints.RootWp
import java.util.*
import javax.inject.Inject

class MainModule @Inject constructor(@StaticFilesDir private val staticFilesDir: String) : HttpModule {
    private val MODULE_SYSTEM_NAME = "main"
    private val MODULE_VERSION_CODE = 1
    private val MODULE_VERSION_NAME = "1.0.0"
    private val PATH_PREFIX = "/api1.0"


    override fun createRoutes(): List<Route> {
        val ret = ArrayList<Route>()

        val mimeTypeResolver = MimeTypeResolverImpl()

        val rootWp = RootWp()
        ret.add(RouteImpl(HttpMethod.GET, PATH_PREFIX, rootWp))
        ret.add(RouteImpl(HttpMethod.POST, PATH_PREFIX, rootWp))
        ret.add(
            RouteImpl(
                HttpMethod.GET,
                "/css",
                StaticFileHandler(staticFilesDir + "css/", mimeTypeResolver, true)
            )
        )

        return ret
    }


    override fun getSystemName(): String {
        return MODULE_SYSTEM_NAME
    }


    override fun getShortDescription(): String {
        return ""
    }


    override fun getVersionCode(): Int {
        return MODULE_VERSION_CODE
    }


    override fun getVersionName(): String {
        return MODULE_VERSION_NAME
    }

}