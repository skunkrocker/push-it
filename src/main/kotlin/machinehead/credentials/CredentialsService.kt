package machinehead.credentials

interface CredentialsService {
    fun createCredentials()
}

class CredentialsServiceImpl() : CredentialsService {
    init {
        println(System.getenv("PASSWORD"))
        println(System.getenv("CERTIFICATE"))
    }

    override fun createCredentials() {
        println("certificate will be returned")
    }
}
