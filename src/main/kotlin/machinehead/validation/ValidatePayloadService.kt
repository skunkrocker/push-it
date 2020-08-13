package machinehead.validation

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import machinehead.model.ClientError
import machinehead.model.Payload
import machinehead.model.yaml.From
import machinehead.model.yaml.YAMLFile
import machinehead.parse.ParseErrors

interface ValidatePayloadService {
    fun isValid(payload: Payload): Option<ClientError>
}

class ValidatePayloadServiceImpl : ValidatePayloadService {

    private var errorMessages: ParseErrors = From the YAMLFile("payload-errors.yml", ParseErrors::class)

    override fun isValid(payload: Payload): Option<ClientError> {
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
