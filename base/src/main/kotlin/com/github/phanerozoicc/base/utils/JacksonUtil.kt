package com.github.phanerozoicc.base.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import java.io.File
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object JacksonUtil {
    val JSON: ObjectMapper = ObjectMapper()

    init {
        JSON.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, true)
        JSON.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        JSON.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
        JSON.configure(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature(), true)
        JSON.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        JSON.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        JSON.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        JSON.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        JSON.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        JSON.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true)
        //        JSON.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        JSON.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        JSON.setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
        val javaTimeModule = JavaTimeModule()
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        javaTimeModule.addSerializer<LocalDateTime?>(
            LocalDateTime::class.java,
            LocalDateTimeSerializer(dateTimeFormatter)
        )
        javaTimeModule.addDeserializer<LocalDateTime?>(
            LocalDateTime::class.java,
            LocalDateTimeDeserializer(dateTimeFormatter)
        )
        JSON.registerModule(javaTimeModule)
    }

    @Throws(JsonProcessingException::class)
    fun <T> readValue(value: String?, cls: Class<T?>?): T? {
        if (value != null) {
            return JSON.readValue<T?>(value, cls)
        }
        return null
    }

    @Throws(IOException::class)
    fun <T> readValue(file: File?, cls: Class<T?>?): T? {
        if (file != null && file.exists()) {
            return JSON.readValue<T?>(file, cls)
        }
        return null
    }

    @Throws(IOException::class)
    fun <T> readValue(url: URL?, cls: Class<T?>?): T? {
        if (url != null) {
            return JSON.readValue<T?>(url, cls)
        }
        return null
    }

    @Throws(JsonProcessingException::class)
    fun <T> readValue(str: String?, typeReference: TypeReference<T?>): T? {
        if (str != null) {
            return JSON.readValue<T?>(str, typeReference)
        }
        return null
    }

    @Throws(JsonProcessingException::class)
    fun writeValueAsString(any: Any?): String? {
        if (any != null) {
            return JSON.writeValueAsString(any)
        }
        return null
    }

    @Throws(JsonProcessingException::class)
    fun readTree(str: String?): JsonNode? {
        if (str != null) {
            return JSON.readTree(str)
        }
        return null
    }

    @Throws(JsonProcessingException::class)
    fun readStringField(str: String?, fieldName: String?): String? {
        var jsonNode = readTree(str)
        if (jsonNode != null && (jsonNode.get(fieldName).also { jsonNode = it }) != null) {
            return jsonNode?.asText()
        }
        return null
    }

    @Throws(JsonProcessingException::class)
    fun readIntField(str: String?, field: String?): Int {
        var jsonNode = readTree(str)
        if (str != null && (jsonNode?.get(field).also { jsonNode = it }) != null) {
            return jsonNode?.asInt()?:0
        }
        return 0
    }

    @Throws(JsonProcessingException::class)
    fun readDoubleField(str: String?, field: String?): Double {
        var jsonNode = readTree(str)
        if (str != null && (jsonNode?.get(field).also { jsonNode = it }) != null) {
            return jsonNode?.asDouble()?:0.0
        }
        return 0.0
    }

    @Throws(JsonProcessingException::class)
    fun readLongField(str: String?, field: String?): Long {
        var jsonNode = readTree(str)
        if (str != null && (jsonNode?.get(field).also { jsonNode = it }) != null) {
            return jsonNode?.asLong()?:0L
        }
        return 0L
    }

    @Throws(JsonProcessingException::class)
    fun readBooleanField(str: String?, field: String?): Boolean {
        var jsonNode = readTree(str)
        if (str != null && (jsonNode?.get(field).also { jsonNode = it }) != null) {
            return jsonNode?.asBoolean() ?: false
        }
        return false
    }

    fun convertToMap(a: Any?): HashMap<String?, Any?>? {
        return JSON.convertValue<HashMap<String?, Any?>?>(a, object : TypeReference<HashMap<String?, Any?>?>() {
        })
    }


    @Throws(JsonProcessingException::class)
    fun <T> readList(str: String?, cls: Class<T?>?): MutableList<T?>? {
        return JSON.readValue<MutableList<T?>?>(str, object : TypeReference<MutableList<T?>?>() {
        })
    }
}
