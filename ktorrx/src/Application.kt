package com.ktorrx

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.gson.*
import io.ktor.http.ContentType

import io.reactivex.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactive.*
import java.util.concurrent.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        get("/") {
            val result = Flowable.range(1, 10)
                .map { it * it }
                .delay(300L, TimeUnit.MILLISECONDS)
                .awaitLast()

            call.respondText("LAST ITEM: $result")
        }

        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))
        }

        get("/iter") {
            call.respondTextWriter(ContentType.Text.Plain) {
                val writer = this
                Flowable.range(1, 10)
                    .map { it * it }
                    .delay(300L, TimeUnit.MILLISECONDS)
                    .collect {
                        writer.write("$it,")
                        writer.flush()
                        delay(100L)
                    }
            }
        }
    }
}

