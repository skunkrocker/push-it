package machinehead.extensions

import machinehead.PushIt
import machinehead.model.ClientError

fun PushIt.reportError(): (ClientError) -> Unit {
    return {
        logger.error { it.message }
        this.clientErrorListener report it
    }
}

fun PushIt.reportCredentialsManagerError(): () -> Unit {
    return {
        logger.error { "the default credential manager was replaced with null" }
        clientErrorListener report ClientError(errorMessages.noCredentialsManager.orEmpty())
    }
}
