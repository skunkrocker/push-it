package machinehead.okclient

import arrow.core.Either
import machinehead.ClientError
import machinehead.model.Payload
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

class OkClientWithCredentials {
    companion object {
        fun createOkClient(
            payload: Payload,
            credentials: Pair<SSLSocketFactory, X509TrustManager>
        ): Either<ClientError, OkHttpClient> {
            try {
                val okClientBuilder = OkHttpClient().newBuilder()
                okClientBuilder.sslSocketFactory(credentials.first, credentials.second)
                okClientBuilder.addInterceptor(addHeadersToCurrentRequest(payload))
                val okClient = okClientBuilder.build()
                return Either.right(okClient)
            } catch (e: Exception) {
                println("Could not create ok client")
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
    }
}