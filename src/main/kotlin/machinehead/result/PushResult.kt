package machinehead.result


data class Response(val status: String, val message: String)
data class PushResult(val responses: HashMap<String, Response?>)
