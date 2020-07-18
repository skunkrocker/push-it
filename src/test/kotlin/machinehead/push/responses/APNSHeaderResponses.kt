package machinehead.push.responses

import com.squareup.okhttp.mockwebserver.Dispatcher
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.RecordedRequest
import machinehead.model.ResponsesLoader

class APNSHeaderResponses : Dispatcher() {
    override fun dispatch(request: RecordedRequest?): MockResponse {

        if (request?.getHeader(APNSHeaders.APNS_EXPIRATION) != null) {
            return MockResponse()
                .setResponseCode(400)
                .setBody(ResponsesLoader.getJson("bad_expiration_date.json"))
        }

        if (request?.getHeader(APNSHeaders.APNS_COLLAPSE_ID) != null) {
            return MockResponse()
                .setResponseCode(400)
                .setBody(ResponsesLoader.getJson("bad_collapse_id.json"))
        }

        return MockResponse()
            .setResponseCode(400)
            .setBody(ResponsesLoader.getJson("bad_priority.json"))

    }
}