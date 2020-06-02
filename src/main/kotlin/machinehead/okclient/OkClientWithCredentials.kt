package machinehead.okclient

import arrow.core.Either
import machinehead.result.Response
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

class OkClientWithCredentials {
    companion object {
        fun createOkClient(
            sslSocketFactory: SSLSocketFactory,
            trustManager: X509TrustManager
        ): Either<OkHttpClient, Response> {
            try {
                val okClientBuilder = OkHttpClient().newBuilder()
                okClientBuilder.sslSocketFactory(sslSocketFactory, trustManager)
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
                return Either.left(okClient)
            } catch (e: Exception) {
                println("Could not create ok client")
            }
            return Either.right(Response("500", "Could not create http client"))
        }
    }
}