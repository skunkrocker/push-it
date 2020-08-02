package machinehead.okclient

import machinehead.credentials.CredentialsService
import org.koin.java.KoinJavaComponent.inject

interface OkClientService {
    fun createOkClient()
}

class OkClientServiceImpl : OkClientService {
    private val credentials by inject(CredentialsService::class.java)
    override fun createOkClient() {
        credentials.createCredentials()
        println("ok client created")
    }
}