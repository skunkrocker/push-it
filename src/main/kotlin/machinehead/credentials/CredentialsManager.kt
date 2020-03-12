package machinehead.credentials

import machinehead.result.PushResult
import machinehead.result.Response
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.SecureRandom
import java.util.*
import javax.net.ssl.*

interface CredentialsManager {
    fun credentials(
        withFactoryAndTrustManager: (SSLSocketFactory, X509TrustManager) -> PushResult,
        onError: () -> PushResult
    ): PushResult
}

class P12CredentialsFromEnv : CredentialsManager {
    override fun credentials(
        withFactoryAndTrustManager: (SSLSocketFactory, X509TrustManager) -> PushResult,
        onError: () -> PushResult
    ): PushResult {
        return readFromEnv() { certificate, password ->
            val factoryAndTrustManager = sslFactoryWithTrustManager(certificate, password)

            val factory = factoryAndTrustManager.first ?: return@readFromEnv onError()
            val manager = factoryAndTrustManager.second ?: return@readFromEnv onError()

            return@readFromEnv withFactoryAndTrustManager(factory, manager)
        }
    }

    private fun readFromEnv(credentials: (String, String) -> PushResult): PushResult {
        val password = System.getenv("PASSWORD") ?: return PushResult(
            hashMapOf(
                "error" to Response(
                    "500",
                    "No password. Use PASSWORD Env variable to provide the password for the p12 certificate"
                )
            )
        )

        val certificate = System.getenv("CERTIFICATE") ?: return PushResult(
            hashMapOf(
                "error" to Response(
                    "500",
                    "No certificate. Use CERTIFICATE Env variable to provide the p12 file in Base64 encoded form"
                )
            )
        )

        return credentials(certificate, password)
    }

    private fun sslFactoryWithTrustManager(
        certificate: String,
        password: String
    ): Pair<SSLSocketFactory?, X509TrustManager?> {
        try {
            val keyStore = KeyStore.getInstance("PKCS12")

            val decodedCertificate = Base64
                .getDecoder()
                .decode(certificate)
                .inputStream()

            keyStore.load(decodedCertificate, password.toCharArray())

            val keyFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm()
            )

            keyFactory.init(keyStore, password.toCharArray())

            val keyManagers = keyFactory.keyManagers
            val trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
            )

            val trustManager: X509TrustManager = getX509TrustManager(keyStore, trustManagerFactory)

            val sslContext = SSLContext.getInstance("TLSv1.2")
            sslContext.init(keyManagers, null, SecureRandom())
            val socketFactory = sslContext.socketFactory

            return Pair(socketFactory, trustManager)

        } catch (e: Exception) {
            println("Could not create ssl factory and trust manager for the provided env credentials. Exception was: $e")
        }
        return Pair(null, null)
    }

    @Throws(KeyStoreException::class)
    private fun getX509TrustManager(
        keyStore: KeyStore,
        trustManagerFactory: TrustManagerFactory
    ): X509TrustManager {

        trustManagerFactory.init(keyStore)

        val trustManagers = trustManagerFactory.trustManagers

        check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
            ("Unexpected default trust managers:" + Arrays.toString(trustManagers))
        }
        return trustManagers[0] as X509TrustManager
    }
}