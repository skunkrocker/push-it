package machinehead

import machinehead.extensions.*
import machinehead.model.*
import machinehead.servers.Stage
import mu.KotlinLogging
import java.time.Duration
import java.time.Instant

fun main() {
    val logger = KotlinLogging.logger { }
    val theTokens = mutableListOf<String>()
    repeat(1000) {
        if (it % 2 == 0)
            theTokens.add("3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a55")
        else
            theTokens.add("3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a54")
    }
    val start = Instant.now()

    payload {
        notification {
            aps {
                alert {
                    body = "Hello"
                    subtitle = "Subtitle"
                }
            }
        }
        headers = hashMapOf(
            "apns-topic" to "org.your.app.bundle.id"
        )
        custom = hashMapOf(
            "custom-property" to "hello custom",
            "blow-up" to true
        )
        stage = Stage.DEVELOPMENT
        /*
        tokens = listOf(
            "3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a55",
            "3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a51"
        )
        */
        tokens = theTokens

    } push { errorsAndResponses ->
        errorsAndResponses
            .fold({
                logger.error { it.message }
            }, {
                logger.info { "errors: ${it.errors} and results: ${it.results}" }
            })
    }
    /*
        .push {
        logger.info { it.responses.size }
    }*/

    val end = Instant.now()
    logger.info { "duration time: ${Duration.between(start, end).toSeconds()} s" }
    logger.info { "its done" }
}
