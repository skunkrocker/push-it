package machinehead.extensions

import arrow.core.Either
import machinehead.app.PushApp
import machinehead.credentials.CredentialsService
import machinehead.credentials.CredentialsServiceImpl
import machinehead.model.ClientError
import machinehead.model.Payload
import machinehead.model.RequestErrorsAndResults
import machinehead.okclient.*
import machinehead.parse.gson
import machinehead.servers.Stage
import machinehead.validation.ValidatePayloadService
import machinehead.validation.ValidatePayloadServiceImpl
import okhttp3.RequestBody
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.get

infix fun Payload.notificationAsString(onParsed: (notification: String) -> Unit) {
    onParsed(notificationAsString())
}

fun Payload.notificationAsString(): String {
    if (this.custom.isNotEmpty()) {
        val notification = gson.toJsonTree(this.notification)
        for (custom in this.custom) {
            val toJsonTree = gson.toJsonTree(custom.value)
            notification?.asJsonObject?.add(custom.key, toJsonTree)
        }
        return gson.toJson(notification).toString()
    }
    return gson.toJson(this.notification).toString()
}

infix fun Payload.push(report: (Either<ClientError, RequestErrorsAndResults>) -> Unit) {
    val headers = this.headers
    val services = module {
        single { PushApp() }
        single { OkClientServiceImpl() as OkClientService }
        single { CredentialsServiceImpl() as CredentialsService }
        single { ValidatePayloadServiceImpl() as ValidatePayloadService }
        single { InterceptorChainServiceImpl(headers) as InterceptorChainService }
        factory { (token: String, body: RequestBody, stage: Stage) ->
            RequestServiceImpl(token, body, stage) as RequestService
        }
    }
    val appContext = startKoin {
        modules(services)
    }
    get(PushApp::class.java)
        .with(this) {
            report(it)
        }

    get(OkClientService::class.java)
        .releaseResources()

    appContext.unloadModules(services)
    stopKoin()
}
