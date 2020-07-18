package machinehead.push

import com.squareup.okhttp.mockwebserver.Dispatcher
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.RecordedRequest
import machinehead.model.ResponsesLoader.Companion.getJson
import machinehead.push.APNSHeaders.Companion.APNS_COLLAPSE_ID
import machinehead.push.APNSHeaders.Companion.APNS_EXPIRATION
import machinehead.push.APNSHeaders.Companion.APNS_PRIORITY
import machinehead.servers.NotificationServers

class APNSHeaders {
    companion object {
        const val APNS_PRIORITY     = "apns-priority"
        const val APNS_EXPIRATION   = "apns-expiration"
        const val APNS_COLLAPSE_ID  = "apns-collapse-id"
    }
}

class MockAPNSResponses : Dispatcher() {
    override fun dispatch(request: RecordedRequest?): MockResponse {
        val path = request?.path

        if (request?.getHeader(APNS_EXPIRATION) != null) {
            return MockResponse()
                .setResponseCode(400)
                .setBody(getJson("bad_expiration_date.json"))
        }

        if (request?.getHeader(APNS_COLLAPSE_ID) != null) {
            return MockResponse()
                .setResponseCode(400)
                .setBody(getJson("bad_collapse_id.json"))
        }

        if (request?.getHeader(APNS_PRIORITY) != null) {
            return MockResponse()
                .setResponseCode(400)
                .setBody(getJson("bad_priority.json"))
        }

        if (path!! == "${NotificationServers.DEVICE_PATH}${TestData.BAD_DEVICE_TOKEN}") {
            return MockResponse()
                .setResponseCode(200)
                .setBody(getJson("bad_device_token.json"))
        }
        if (path == "${NotificationServers.DEVICE_PATH}${TestData.TOKEN}") {
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
