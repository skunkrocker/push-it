package machinehead.model

class ResponsesLoader {
    companion object {
        fun getJson(path: String): String {
            val classLoader = this::class.java.classLoader
            val uri = classLoader.getResource(path)
            val file = java.io.File(uri!!.path)
            return String(file.readBytes(), Charsets.UTF_8)
        }
    }
}