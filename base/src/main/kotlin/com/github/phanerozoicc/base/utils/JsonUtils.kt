package com.github.phanerozoicc.base.utils

import com.fasterxml.jackson.core.JsonProcessingException

object JsonUtils {
    fun <T> readValue(json: kotlin.String?, clazz: java.lang.Class<T?>?): T? {
        try {
            return JacksonUtils.readValue<T?>(json, clazz)
        } catch (e: JsonProcessingException) {
            throw java.lang.RuntimeException(e)
        }
    }


    fun writeValueAsString(obj: kotlin.Any?): kotlin.String {
        try {
            return JacksonUtils.writeValueAsString(obj)
        } catch (e: JsonProcessingException) {
            throw java.lang.RuntimeException(e)
        }
    }

    fun <T> readValue(json: kotlin.String?, typeReference: TypeReference<T?>?): T? {
        try {
            return JacksonUtils.readValue(json, typeReference)
        } catch (e: JsonProcessingException) {
            throw java.lang.RuntimeException(e)
        }
    }

    fun readTree(json: kotlin.String?): JsonNode {
        try {
            return JacksonUtils.readTree(json)
        } catch (e: JsonProcessingException) {
            throw java.lang.RuntimeException(e)
        }
    }

    fun convertToMap(obj: kotlin.Any?): kotlin.collections.MutableMap<kotlin.String?, kotlin.Any?> {
        return JacksonUtils.convertToMap(obj)
    }

    fun <T> readList(json: kotlin.String?, clazz: java.lang.Class<T?>?): kotlin.collections.MutableList<T?> {
        try {
            return JacksonUtils.readList<T?>(json, clazz)
        } catch (e: JsonProcessingException) {
            throw java.lang.RuntimeException(e)
        }
    }
}
