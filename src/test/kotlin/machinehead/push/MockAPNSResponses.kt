package machinehead.push

import com.squareup.okhttp.mockwebserver.Dispatcher
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.RecordedRequest
import machinehead.servers.NotificationServers

class MockAPNSResponses : Dispatcher() {
    override fun dispatch(request: RecordedRequest?): MockResponse {
        val path = request?.path
        if (path!! == "${NotificationServers.DEVICE_PATH}${TestData.TOKEN}") {
            val ok = MockResponse()
            return ok
                .setResponseCode(200)
                .setBody(ok.getJson("test_success.json"))
        }

        val default = MockResponse()
        return default
            .setResponseCode(400)
            .setBody(
                default.getJson("missing_token.json")
            )
    }
}
