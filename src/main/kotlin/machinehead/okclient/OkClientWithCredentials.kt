package machinehead.okclient

import arrow.core.Either
import machinehead.ClientError
import machinehead.model.Payload
import okhttp3.Interceptor
import okhttp3.OkHttpClient
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
                okClientBuilder.addInterceptor { chain: Interceptor.Chain ->
                    val original = chain.request()
                    val responseBuilder = original.newBuilder()
                    //TODO add the headers

                    chain.proceed(
                        responseBuilder
                            .method(original.method, original.body)
                            .build()
                    )
                }
                val okClient = okClientBuilder.build()
                return Either.right(okClient)
            } catch (e: Exception) {
                println("Could not create ok client")
            }
            return Either.left(ClientError("could not create http client"))
        }
    }
}