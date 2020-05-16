package org.example.kforge

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.JoranException
import com.bolyartech.forge.server.config.FileForgeServerConfigurationLoader
import com.bolyartech.forge.server.config.ForgeConfigurationException
import com.bolyartech.forge.server.config.ForgeServerConfiguration
import com.bolyartech.forge.server.jetty.ForgeJettyConfiguration
import com.bolyartech.forge.server.jetty.ForgeJettyConfigurationLoaderFile
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.example.kforge.dagger.DaggerServerDaggerComponent
import org.example.kforge.dagger.DbDaggerModule
import org.example.kforge.dagger.ServerModule
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    var logger: org.slf4j.Logger = LoggerFactory.getLogger("pvpzone")

//    Class.forName("org.postgresql.Driver")
    val cmd: CommandLine = parseCommandLine(args)

    var jettyConfigPath = cmd.getOptionValue("config-file")
    if (jettyConfigPath == null) {
        jettyConfigPath = ForgeJettyConfigurationLoaderFile.detectJettyConfigFilePath()
    }

    if (jettyConfigPath != null) {
        val loader = ForgeJettyConfigurationLoaderFile(jettyConfigPath)

        val conf: ForgeJettyConfiguration
        try {
            conf = loader.load()
        } catch (e: ForgeConfigurationException) {
            throw IllegalStateException(e)
        }

        val configDir: String = if (conf.configDir.isEmpty()) {
            val f = File(jettyConfigPath)
            f.parent
        } else {
            conf.configDir
        }

        val forgeConfLoader = FileForgeServerConfigurationLoader(configDir)
        val forgeConf: ForgeServerConfiguration = try {
            forgeConfLoader.load()
        } catch (e: ForgeConfigurationException) {
            logger.error("Cannot load forge.conf")
            exitProcess(1)
        }

        val server = DaggerServerDaggerComponent.builder().dbDaggerModule(DbDaggerModule(configDir))
            .serverModule(ServerModule(forgeConf.staticFilesDir)).build().provideServer()

        initLog(configDir, forgeConf.serverLogName)
        server.start(conf, forgeConf.isPathInfoEnabled, forgeConf.maxSlashesInPathInfo)
    } else {
        logger.error("No configuration. Aborting.")
    }
}

fun parseCommandLine(args: Array<String>): CommandLine {
    val argsParser = DefaultParser()

    return argsParser.parse(createCliArgOptions(), args)
}

fun createCliArgOptions(): Options {
    val cliOptions = Options()
    cliOptions.addOption("c", "config-file", true, "path to jetty configuration file")
    return cliOptions
}

private fun initLog(configDir: String, logFilenamePrefix: String = "", serverNameSuffix: String = "") {
    var logger: org.slf4j.Logger = LoggerFactory.getLogger("pvpzone")

    val context = LoggerFactory.getILoggerFactory() as LoggerContext
    val jc = JoranConfigurator()
    jc.context = context
    context.reset()

    context.putProperty("application-name", logFilenamePrefix + serverNameSuffix)

    val f = File(configDir, "logback.xml")
    println("Will try logback config: " + f.absolutePath)
    if (f.exists()) {
        val logbackConfigFilePath = f.absolutePath
        try {
            jc.doConfigure(logbackConfigFilePath)
            logger.info("+++ logback initialized OK")
        } catch (e: JoranException) {
            e.printStackTrace()
        }
    } else {
        println("!!! No logback configuration file found. Using default conf")
    }
}