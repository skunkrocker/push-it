package machinehead.servers

enum class Platform {
    IOS,
    IOS_SANDBOX
}

sealed class NotificationServers {
    companion object {
        val DEVICE_PATH = "/3/device/"
        var PRODUCTION = "https://api.push.apple.com:2197"
        var DEVELOPMENT = "https://api.development.push.apple.com:2197"


        fun getURLForDeviceToken(deviceToken: String?, platform: Platform?, forUrl: (String) -> Unit) {
            when (platform) {
                Platform.IOS -> forUrl(PRODUCTION + DEVICE_PATH + deviceToken)
                Platform.IOS_SANDBOX -> forUrl(DEVELOPMENT + DEVICE_PATH + deviceToken)
            }
        }
    }
}