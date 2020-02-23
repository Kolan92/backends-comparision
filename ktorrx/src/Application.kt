package com.ktorrx

import com.fatboyindustrial.gsonjodatime.Converters
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder
import com.google.gson.GsonBuilder
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import io.reactivex.Flowable
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactive.collect
import org.joda.time.DateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit


data class BodyInfo (val weight: Double, val height: Double, val measuredOn: DateTime)
val insertQuery = "insert into body_info (measuredOn, weight_kg, height_cm) values (?,?,?)"

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }

    install(ContentNegotiation) {
        gson {
            Converters.registerDateTime(this)

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
                    }
            }
        }

        post("/api/bmi") {
            val bodyInfo = call.receive<BodyInfo>()
            //val result = connectionPool.connect().get().asSuspending
             //   .sendPreparedStatement(insertQuery, listOf(bodyInfo.weight, bodyInfo.height, bodyInfo.measuredOn))

               call.respond(bodyInfo)
            }
        }

}

val connectionPool = PostgreSQLConnectionBuilder.createConnectionPool{
    username = "postgres"
    host = "172.18.0.5"
    port = 5432
    password = "postgres"
    database = "testdatabase"
    maxActiveConnections = 100
    maxIdleTime = TimeUnit.MINUTES.toMillis(15)
    maxPendingQueries = 10_000
    connectionValidationInterval = TimeUnit.SECONDS.toMillis(30)
}

