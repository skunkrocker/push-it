package machinehead.credentials

import machinehead.exceptions.ClientCreationException
import machinehead.model.ClientError
import mu.KotlinLogging
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.SecureRandom
import java.util.*
import javax.net.ssl.*

interface CredentialsService {
    fun getFactoryAndManager(
        credentials: (factory: SSLSocketFactory?, manager: X509TrustManager?) -> Unit
    )
}

class CredentialsServiceImpl() : CredentialsService {
    private var factory: SSLSocketFactory
    private var manager: X509TrustManager

    private val logger = KotlinLogging.logger {}

    init {
        val password = System.getenv("PASSWORD") ?: throw ClientCreationException(
            ClientError(
                "No password. Use PASSWORD Env variable to provide the password for the p12 certificate"
            )
        )

        val certificate = System.getenv("CERTIFICATE") ?: throw ClientCreationException(
            ClientError(
                "No certificate. Use CERTIFICATE Env variable to provide the p12 file in Base64 encoded form"
            )
        )
        val factoryAndManager = sslFactoryWithTrustManager(certificate, password)

        factory = factoryAndManager.first
        manager = factoryAndManager.second
    }

    override fun getFactoryAndManager(credentials: (factory: SSLSocketFactory?, manager: X509TrustManager?) -> Unit) {
        credentials(factory, manager)
    }


    private fun sslFactoryWithTrustManager(
        certificate: String,
        password: String
    ): Pair<SSLSocketFactory, X509TrustManager> {
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
            logger.error("Could not create ssl factory and trust manager for the provided env credentials. Exception was: $e")
        }
        throw ClientCreationException(ClientError("could not create ssl factory and trust manager for the provided p12 and password"))
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
