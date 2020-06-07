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

        fun forUrl(stage: Stage?, deviceToken: String?): String {
            if (stage == Stage.PRODUCTION) {
                return PRODUCTION + DEVICE_PATH + deviceToken
            }
            return DEVELOPMENT + DEVICE_PATH + deviceToken
        }
    }
}