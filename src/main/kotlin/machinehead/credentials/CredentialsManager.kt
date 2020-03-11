package machinehead.credentials

import machinehead.result.PushResult
import machinehead.result.Response
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
        return readFromEnv() { certificate, password ->
            if (sslFactory == null || trustManager == null) {
                return@readFromEnv onError()
            }
            return@readFromEnv onCredentials(sslFactory, trustManager)
        }
    }

    private fun readFromEnv(credentials: (String, String) -> PushResult): PushResult {
        val pass = System.getenv("PASSWORD")
        val cert = System.getenv("CERTIFICATE")
        if (pass == null || cert == null) {
            return PushResult(
                hashMapOf(
                    "error" to Response(
                        "500",
                        "Could not find credentials in environment variables"
                    )
                )
            )
        }
        return credentials(cert, cert)
    }
}