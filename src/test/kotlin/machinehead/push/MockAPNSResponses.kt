package machinehead.push

import com.squareup.okhttp.mockwebserver.Dispatcher
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.RecordedRequest
import machinehead.model.ResponsesLoader.Companion.getJson
import machinehead.servers.NotificationServers

class MockAPNSResponses : Dispatcher() {
    override fun dispatch(request: RecordedRequest?): MockResponse {
        val path = request?.path
        if (path!! == "${NotificationServers.DEVICE_PATH}${TestData.TOKEN}") {

            return MockResponse()
                .setResponseCode(200)
                .setBody(getJson("test_success.json"))
        }
        return MockResponse()
            .setResponseCode(400)
            .setBody(
                getJson("missing_token.json")
            )
    }
}
