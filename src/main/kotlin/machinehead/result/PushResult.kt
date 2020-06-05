package machinehead.result


data class PlatformResponse(val status: String, val message: String)
data class PushResult(val responses: HashMap<String, PlatformResponse?>)
