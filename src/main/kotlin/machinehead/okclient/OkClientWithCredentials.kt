package machinehead.okclient

import arrow.core.Either
import machinehead.model.ClientError
import machinehead.model.Payload
import mu.KotlinLogging
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

class OkClientWithCredentials {
    companion object {
        private val logger = KotlinLogging.logger { }
        fun createOkClient(
            payload: Payload,
            credentials: Pair<SSLSocketFactory, X509TrustManager>
        ): Either<ClientError, OkHttpClient> {
            try {
                val okClientBuilder = OkHttpClient().newBuilder()
                logger.debug { "ok client builder created" }

                okClientBuilder.sslSocketFactory(credentials.first, credentials.second)
                logger.debug { "added ssl factory and trust manager to the ok client builder" }

                okClientBuilder.addInterceptor(addHeadersToCurrentRequest(payload))
                logger.debug { "ok client builder added request interceptor for headers" }

                val okClient = okClientBuilder.build()
                logger.debug { "ok client was built and will be returned" }
                return Either.right(okClient)
            } catch (e: Exception) {
                logger.error { "Could not create ok client. exception occurred: $e" }
            }
            return Either.left(ClientError("could not create http client"))
        }

        private fun addHeadersToCurrentRequest(payload: Payload): (Interceptor.Chain) -> Response {
            return { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val responseBuilder: Request.Builder = original.newBuilder()

                payload.headers.forEach() {
                    responseBuilder.addHeader(it.key, it.value.toString())
                }

                val request = responseBuilder
                    .method(original.method, original.body)
                    .build()

                chain.proceed(request)
            }
        }

        fun releaseResources(okClient: OkHttpClient) {
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
}