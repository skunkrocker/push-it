package machinehead.okclient

import machinehead.model.Payload
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

interface InterceptorChainService {
    fun createInterceptor(payload: Payload): (Interceptor.Chain) -> Response
}

class InterceptorChainServiceImpl(val headers: Map<String, Any>) : InterceptorChainService {
    override fun createInterceptor(payload: Payload): (Interceptor.Chain) -> Response {
        return { chain: Interceptor.Chain ->
            val original: Request = chain.request()
            val responseBuilder: Request.Builder = original.newBuilder()

            headers.forEach() {
                responseBuilder.addHeader(it.key, it.value.toString())
            }

            val request = responseBuilder
                .method(original.method, original.body)
                .build()

            chain.proceed(request)
        }
    }
}