package machinehead.credentials

import machinehead.result.PushResult
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

interface CredentialsManager {
    fun credentials(
        onCredentials: (SSLSocketFactory, X509TrustManager) -> PushResult,
        onError: () -> PushResult
    ): PushResult
}

class P12CredentialsFromEnv : CredentialsManager {
    override fun credentials(
        onCredentials: (SSLSocketFactory, X509TrustManager) -> PushResult,
        onError: () -> PushResult
    ): PushResult {
        val sslFactory: SSLSocketFactory? = null
        val trustManager: X509TrustManager? = null
        if (sslFactory == null || trustManager == null) {
            return onError()
        }
        return onCredentials(sslFactory, trustManager)
    }
}