package machinehead.extensions

import arrow.core.Either
import machinehead.app.PushApp
import machinehead.credentials.CredentialsService
import machinehead.credentials.CredentialsServiceImpl
import machinehead.model.*
import machinehead.okclient.*
import machinehead.servers.Stage
import org.koin.core.context.startKoin
import org.koin.dsl.module
import machinehead.validation.ValidatePayloadService
import machinehead.validation.ValidatePayloadServiceImpl
import okhttp3.RequestBody
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent
import org.koin.java.KoinJavaComponent.get

infix fun Payload.pushIt(report: (Either<ClientError, RequestErrorsAndResults>) -> Unit) {
    val headers = this.headers
    val services = module {
        single { OkClientServiceImpl() as OkClientService }
        single { CredentialsServiceImpl() as CredentialsService }
        single { ValidatePayloadServiceImpl() as ValidatePayloadService }
        single { InterceptorChainServiceImpl(headers) as InterceptorChainService }
        factory { (token: String, stage: Stage, body: RequestBody) ->
            PushNotificationImpl(
                token,
                stage,
                body
            ) as PushNotification
        }
        factory { (token: String, body: RequestBody, stage: Stage) ->
            RequestServiceImpl(token, body, stage) as RequestService
        }
    }
    val appContext = startKoin {
        modules(services)
    }
    PushApp().with(this) {
        report(it)
    }

    get(OkClientService::class.java)
        .releaseResources()
    appContext.close()
}
