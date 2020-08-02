package machinehead.credentials

import arrow.core.Either
import machinehead.model.ClientError
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

interface CredentialsService {
    fun getCredentials(): Either<ClientError, Pair<SSLSocketFactory, X509TrustManager>>
    fun createCredentials()
}

class CredentialsServiceImpl : CredentialsService {
    override fun getCredentials(): Either<ClientError, Pair<SSLSocketFactory, X509TrustManager>> {
        return Either.left(ClientError("Could not create any credentials"))
    }

    override fun createCredentials() {
        println(System.getenv("PASSWORD"))
    }
}
