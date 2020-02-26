package machinehead.parse

import machinehead.model.Payload
import machinehead.model.yaml.From
import machinehead.model.yaml.YAMLFile

data class ParseErrors(val noTokens: String, val noAlert: String)
data class ParseInformation(val wasParsed: Boolean, val error: String)
data class ParsedPayload(val parsedPayload: String, val tokens: List<String>, val isProduction: Boolean)

object PayloadParser {
    private val errors = From the YAMLFile("payload-errors.yml", ParseErrors::class)

    fun parse(payload: Payload, onParsed: (parsedPayload: ParsedPayload) -> Unit): ParseInformation {
        this.validate(payload, {

        }, {

        })
        return ParseInformation(false, "")
    }

    private fun validate(payload: Payload, ifValid: (Payload) -> Unit, ifNotValid: (ParseInformation) -> Unit) {
        if (payload.notification?.aps?.alert == null) {
            ifNotValid(ParseInformation(false, errors.noAlert))
        }
        if (payload.tokens.isEmpty()) {
            ifNotValid(ParseInformation(false, errors.noTokens))
        }
        ifValid(payload)
    }
}
