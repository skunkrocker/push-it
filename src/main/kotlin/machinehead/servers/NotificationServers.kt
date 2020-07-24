package machinehead.servers

enum class Stage {
    TEST,
    PRODUCTION,
    DEVELOPMENT
}

sealed class NotificationServers {
    companion object {
        const val DEVICE_PATH = "/3/device/"

        private const val PRODUCTION_URL = "https://api.push.apple.com:2197"
        private const val DEVELOPMENT_URL = "https://api.development.push.apple.com:2197"

        fun urlForStage(stage: Stage?): String =
            when (stage) {
                Stage.PRODUCTION -> PRODUCTION_URL + DEVICE_PATH
                Stage.DEVELOPMENT -> DEVELOPMENT_URL + DEVICE_PATH
                else -> System.getProperty("localhost.url") + DEVICE_PATH
            }
    }
}