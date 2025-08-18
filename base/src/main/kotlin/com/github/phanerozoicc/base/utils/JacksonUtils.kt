package com.github.phanerozoicc.base.utils

import com.fasterxml.jackson.annotation.JsonInclude

object JacksonUtils {
    val JSON: ObjectMapper = ObjectMapper()

    init {
        JacksonUtils.JSON.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, true)
        JacksonUtils.JSON.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        JacksonUtils.JSON.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
        JacksonUtils.JSON.configure(ALLOW_NON_NUMERIC_NUMBERS.mappedFeature(), true)
        JacksonUtils.JSON.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        JacksonUtils.JSON.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        JacksonUtils.JSON.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        JacksonUtils.JSON.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        JacksonUtils.JSON.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        JacksonUtils.JSON.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true)
        //        JSON.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        JacksonUtils.JSON.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        JacksonUtils.JSON.setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
        val javaTimeModule: JavaTimeModule = JavaTimeModule()
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        javaTimeModule.addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(dateTimeFormatter))
        javaTimeModule.addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer(dateTimeFormatter))
        JacksonUtils.JSON.registerModule(javaTimeModule)
    }

    @Throws(JsonProcessingException::class)
    fun <T> readValue(value: kotlin.String?, cls: java.lang.Class<T?>?): T? {
        if (value != null) {
            return JacksonUtils.JSON.readValue(value, cls)
        }
        return null
    }

    @Throws(java.io.IOException::class)
    fun <T> readValue(file: java.io.File?, cls: java.lang.Class<T?>?): T? {
        if (file != null && file.exists()) {
            return JacksonUtils.JSON.readValue(file, cls)
        }
        return null
    }

    @Throws(java.io.IOException::class)
    fun <T> readValue(url: java.net.URL?, cls: java.lang.Class<T?>?): T? {
        if (url != null) {
            return JacksonUtils.JSON.readValue(url, cls)
        }
        return null
    }

    @Throws(JsonProcessingException::class)
    fun <T> readValue(str: kotlin.String?, typeReference: TypeReference<T?>?): T? {
        if (str != null) {
            return JacksonUtils.JSON.readValue(str, typeReference)
        }
        return null
    }

    @JvmStatic
    @Throws(JsonProcessingException::class)
    fun writeValueAsString(`object`: kotlin.Any?): kotlin.String {
        if (`object` != null) {
            return JacksonUtils.JSON.writeValueAsString(`object`)
        }
        return null
    }

    @JvmStatic
    @Throws(JsonProcessingException::class)
    fun readTree(str: kotlin.String?): JsonNode {
        if (str != null) {
            return JacksonUtils.JSON.readTree(str)
        }
        return null
    }

    @Throws(JsonProcessingException::class)
    fun readStringField(str: kotlin.String?, fieldName: kotlin.String?): kotlin.String? {
        var jsonNode: JsonNode? = JacksonUtils.readTree(str)
        if (jsonNode != null && (jsonNode.get(fieldName).also { jsonNode = it }) != null) {
            return jsonNode.asText()
        }
        return null
    }

    @Throws(JsonProcessingException::class)
    fun readIntField(str: kotlin.String?, field: kotlin.String?): Int {
        var jsonNode: JsonNode = JacksonUtils.readTree(str)
        if (str != null && (jsonNode.get(field).also { jsonNode = it }) != null) {
            return jsonNode.asInt()
        }
        return 0
    }

    @Throws(JsonProcessingException::class)
    fun readDoubleField(str: kotlin.String?, field: kotlin.String?): kotlin.Double {
        var jsonNode: JsonNode = JacksonUtils.readTree(str)
        if (str != null && (jsonNode.get(field).also { jsonNode = it }) != null) {
            return jsonNode.asDouble()
        }
        return 0.0
    }

    @Throws(JsonProcessingException::class)
    fun readLongField(str: kotlin.String?, field: kotlin.String?): Long {
        var jsonNode: JsonNode = JacksonUtils.readTree(str)
        if (str != null && (jsonNode.get(field).also { jsonNode = it }) != null) {
            return jsonNode.asLong()
        }
        return 0L
    }

    @Throws(JsonProcessingException::class)
    fun readBooleanField(str: kotlin.String?, field: kotlin.String?): Boolean {
        var jsonNode: JsonNode = JacksonUtils.readTree(str)
        if (str != null && (jsonNode.get(field).also { jsonNode = it }) != null) {
            return jsonNode.asBoolean()
        }
        return false
    }

    @JvmStatic
    fun convertToMap(a: kotlin.Any?): HashMap<kotlin.String?, kotlin.Any?> {
        return JacksonUtils.JSON.convertValue(a, object : TypeReference<HashMap<kotlin.String?, kotlin.Any?>?>() {
        })
    }


    @Throws(JsonProcessingException::class)
    fun <T> readList(str: kotlin.String?, cls: java.lang.Class<T?>?): kotlin.collections.MutableList<T?> {
        return JacksonUtils.JSON.readValue(str, object : TypeReference<kotlin.collections.MutableList<T?>?>() {
        })
    }
}
