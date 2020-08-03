package machinehead.credentials

import machinehead.model.ClientError
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

interface CredentialsService {
    fun createCredentials(
        onCreate: (factory: SSLSocketFactory?, manager: X509TrustManager?) -> Unit,
        onFail: (ClientError) -> Unit
    )
}

class CredentialsServiceImpl() : CredentialsService {
    init {
        println(System.getenv("PASSWORD"))
        println(System.getenv("CERTIFICATE"))
    }

    override fun createCredentials(
        onCreate: (factory: SSLSocketFactory?, manager: X509TrustManager?) -> Unit,
        onFail: (ClientError) -> Unit
    ) {
        onCreate(null, null)
        println("certificate will be returned")
    }
}
