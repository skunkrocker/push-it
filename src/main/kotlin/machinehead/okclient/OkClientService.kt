package machinehead.okclient

import arrow.core.Either
import machinehead.credentials.CredentialsService
import machinehead.exceptions.ClientCreationException
import machinehead.model.ClientError
import mu.KotlinLogging
import okhttp3.OkHttpClient
import org.koin.java.KoinJavaComponent.inject
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

interface OkClientService {
    fun getHttpClient(): Either<ClientError, OkHttpClient>
    fun releaseResources()
}

class OkClientServiceImpl : OkClientService {
    private val credentials by inject(CredentialsService::class.java)
    private val interceptorService by inject(InterceptorChainService::class.java)

    private val logger = KotlinLogging.logger { }

    private var okHttpClient: Either<ClientError, OkHttpClient> =
        Either.left(ClientError("failed to create ok http client"))

    init {
        credentials.getFactoryAndManager { factory, manager ->
            println("created the http client")
            okHttpClient = Either.right(createOkClient(factory, manager))
        }
    }

    override fun getHttpClient(): Either<ClientError, OkHttpClient> {
        return okHttpClient
    }

    override fun releaseResources() {
        this.getHttpClient()
            .map { okClient ->
                val dispatcher = okClient.dispatcher
                if (dispatcher.queuedCallsCount() == 0 && dispatcher.runningCallsCount() == 0) {
                    okClient.dispatcher.executorService.shutdownNow()
                    logger.debug { "ok client executor service shutting down" }
                    okClient.connectionPool.evictAll()
                    logger.debug { "ok client evict all from connection pool" }
                    okClient.cache?.close()
                    logger.debug { "ok client clean cache" }

                }
            }
    }

    private fun createOkClient(factory: SSLSocketFactory, manager: X509TrustManager): OkHttpClient {
        try {
            val okClientBuilder = OkHttpClient().newBuilder()
            logger.debug { "ok client builder created" }

            okClientBuilder.sslSocketFactory(factory, manager)
            logger.debug { "added ssl factory and trust manager to the ok client builder" }

            okClientBuilder.addInterceptor(interceptorService.createInterceptor())
            logger.debug { "ok client builder added request interceptor for headers" }

            val okClient = okClientBuilder.build()
            logger.debug { "ok client was built and will be returned" }
            return okClient
        } catch (e: Exception) {
            logger.error { "Could not create ok client. exception occurred: $e" }
        }
        throw ClientCreationException(ClientError("could not create http client"))
    }
}