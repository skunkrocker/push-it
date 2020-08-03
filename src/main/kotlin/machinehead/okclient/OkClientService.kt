package machinehead.okclient

import arrow.core.Either
import machinehead.credentials.CredentialsService
import machinehead.model.ClientError
import okhttp3.OkHttpClient
import org.koin.java.KoinJavaComponent.inject

interface OkClientService {
    fun getHttpClient(): Either<ClientError, OkHttpClient>
}

class OkClientServiceImpl : OkClientService {
    private val credentials by inject(CredentialsService::class.java)
    private val interceptorService by inject(InterceptorChainService::class.java)

    private var okHttpClient: Either<ClientError, OkHttpClient> =
        Either.left(ClientError("failed to create ok http client"))

    init {
        credentials.createCredentials(
            { factory, manager ->
                println("created the http client")
                okHttpClient = Either.right(OkHttpClient())
            },
            {
                okHttpClient = Either.left(it)
            })
    }

    override fun getHttpClient(): Either<ClientError, OkHttpClient> {
        return okHttpClient
    }
}