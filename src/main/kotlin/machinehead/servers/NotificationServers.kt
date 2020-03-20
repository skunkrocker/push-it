package machinehead.servers

enum class Stage {
    PRODUCTION,
    DEVELOPMENT
}

sealed class NotificationServers {
    companion object {
        private const val DEVICE_PATH = "/3/device/"
        private const val PRODUCTION = "https://api.push.apple.com:2197"
        private const val DEVELOPMENT = "https://api.development.push.apple.com:2197"

        fun urlFor(stage: Stage?, deviceToken: String?, forUrl: (String) -> Unit) {
            when (stage) {
                Stage.PRODUCTION -> forUrl(PRODUCTION + DEVICE_PATH + deviceToken)
                Stage.DEVELOPMENT -> forUrl(DEVELOPMENT + DEVICE_PATH + deviceToken)
            }
        }
    }
}