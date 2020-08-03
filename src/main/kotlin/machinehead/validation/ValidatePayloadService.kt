package machinehead.validation

import arrow.core.None
import arrow.core.Option
import machinehead.model.ClientError
import machinehead.model.Payload

interface ValidatePayloadService {
    fun isValid(payload: Payload): Option<ClientError>
}

class ValidatePayloadServiceImpl : ValidatePayloadService {
    override fun isValid(payload: Payload): Option<ClientError> {
        return None
    }
}
