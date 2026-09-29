package ru.sber.transport.config

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.config.server.EnableConfigServer

@EnableConfigServer
@SpringBootApplication
class Application

fun main(vararg args: String) {
    runApplication<Application>(*args)
}