package machinehead.extensions

import arrow.core.Either
import machinehead.app.PushApp
import machinehead.credentials.CredentialsService
import machinehead.credentials.CredentialsServiceImpl
import machinehead.model.*
import machinehead.okclient.*
import org.koin.core.context.startKoin
import org.koin.dsl.module
import machinehead.validation.ValidatePayloadService
import machinehead.validation.ValidatePayloadServiceImpl

infix fun Payload.pushIt(report: (Either<ClientError, RequestErrorsAndResults>) -> Unit) {
    val headers = this.headers
    val services = module {
        single { OkClientServiceImpl() as OkClientService }
        single { CredentialsServiceImpl() as CredentialsService }
        single { ValidatePayloadServiceImpl() as ValidatePayloadService }
        single { InterceptorChainServiceImpl(headers) as InterceptorChainService }
        factory { (token: String) -> PushNotificationImpl(token) as PushNotification }
    }
    val appContext = startKoin {
        modules(services)
    }
    PushApp().with(this) {
        report(it)
    }
    appContext.close()
}
