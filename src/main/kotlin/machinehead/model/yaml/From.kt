package machinehead.model.yaml

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.nio.file.Files
import java.nio.file.Path
import kotlin.reflect.KClass

data class YAMLFile<T : Any>(val fileName: String, val clazz: KClass<T>)

object From {
    private val mapper = let {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        mapper
    }

    infix fun <T : Any> the(yamlFile: YAMLFile<T>): T {
        val clazz = yamlFile.clazz.java
        val fileName = yamlFile.fileName
        return Files.newBufferedReader(Path.of("src/main/resources/$fileName"))
            .use { mapper.readValue(it, clazz) }
    }
}