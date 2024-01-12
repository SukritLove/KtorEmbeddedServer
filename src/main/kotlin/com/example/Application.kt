package com.example

import com.example.plugins.configureSerialization
import io.ktor.client.engine.cio.*
import io.ktor.network.tls.certificates.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.time.Duration
import kotlin.text.toCharArray

@Serializable
data class Message(val message: String)

fun main() {
    val keyStoreFile = File("build/keystore.jks")
    val keyStoreAlias = "testAlias"
    val keyStorePassword = "password"
    val keyStore = buildKeyStore {
        certificate(keyStoreAlias) {
            password = keyStorePassword
            domains = listOf("192.168.101.51", "localhost")
        }
    }
    keyStore.saveToFile(keyStoreFile, keyStorePassword)


    val environment = applicationEngineEnvironment {
        log = LoggerFactory.getLogger("ktor.application")
        connector {
            host = "192.168.101.51"
            port = 8080
        }
        sslConnector(
            keyStore = keyStore,
            keyAlias = keyStoreAlias,
            keyStorePassword = { keyStorePassword.toCharArray() },
            privateKeyPassword = { keyStorePassword.toCharArray() })
        {
            host = "192.168.101.51"
            port = 8443
            keyStorePath = keyStoreFile
            module(Application::module)
        }
    }
    embeddedServer(Netty, environment)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    routing {
        webSocket("Greeting") {
            sendSerialized(Message("Hello there WHO ARE YOUUUU!!!!!"))
        }
    }
}