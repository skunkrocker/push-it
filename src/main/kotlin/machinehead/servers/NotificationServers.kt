package machinehead.servers

enum class Platform {
    IOS,
    IOS_SANDBOX
}

sealed class NotificationServers {
    companion object {
        private const val DEVICE_PATH = "/3/device/"
        private const val PRODUCTION = "https://api.push.apple.com:2197"
        private const val DEVELOPMENT = "https://api.development.push.apple.com:2197"

        fun urlFor(platform: Platform?, deviceToken: String?, forUrl: (String) -> Unit) {
            when (platform) {
                Platform.IOS -> forUrl(PRODUCTION + DEVICE_PATH + deviceToken)
                Platform.IOS_SANDBOX -> forUrl(DEVELOPMENT + DEVICE_PATH + deviceToken)
            }
        }
    }
}