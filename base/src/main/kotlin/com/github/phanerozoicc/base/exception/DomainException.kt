package com.github.phanerozoicc.base.exception

open class DomainException(message: String, cause: Throwable?): Exception(message,  cause) {
}