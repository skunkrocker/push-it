package machinehead.exceptions

import machinehead.model.ClientError

class ClientCreationException(val clientError: ClientError) : Throwable()