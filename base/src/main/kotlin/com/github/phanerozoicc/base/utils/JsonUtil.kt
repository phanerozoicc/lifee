package com.github.phanerozoicc.base.utils

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode

object JsonUtil {
    fun <T> readValue(json: String?, clazz: Class<T?>?): T? {
        try {
            return JacksonUtil.readValue<T?>(json, clazz)
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }


    fun writeValueAsString(obj: Any?): String? {
        try {
            return JacksonUtil.writeValueAsString(obj)
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }

    fun <T> readValue(json: String?, typeReference: TypeReference<T?>): T? {
        try {
            return JacksonUtil.readValue<T?>(json, typeReference)
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }

    fun readTree(json: String?): JsonNode? {
        try {
            return JacksonUtil.readTree(json)
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }

    fun convertToMap(obj: Any?): MutableMap<String?, Any?>? {
        return JacksonUtil.convertToMap(obj)
    }

    fun <T> readList(json: String?, clazz: Class<T?>?): MutableList<T?>? {
        try {
            return JacksonUtil.readList<T?>(json, clazz)
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }
}
