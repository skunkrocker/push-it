package machinehead.okclient

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import machinehead.model.ClientError
import machinehead.model.Payload
import machinehead.model.yaml.From
import machinehead.model.yaml.YAMLFile
import machinehead.parse.ParseErrors

class PayloadValidator {
    companion object {
        private val errorMessages: ParseErrors
            get() = From the YAMLFile("payload-errors.yml", ParseErrors::class)

        fun validate(payload: Payload): Option<ClientError> {
            if (payload.tokens.isEmpty()) {
                return Some(ClientError(errorMessages.noTokens.orEmpty()))
            }
            if (payload.notification?.aps?.alert == null) {
                return Some(ClientError(errorMessages.noAlert.orEmpty()))
            }

            if (payload.headers.isEmpty()) {
                return Some(ClientError(errorMessages.noTopic.orEmpty()))
            }
            return None
        }
    }
}