package com.mikkaeru.pix.shared.handler

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionHandlerResolver(@Inject private val handlers: Collection<ApiExceptionHandler<RuntimeException>>) {

    fun resolve(e: RuntimeException): ApiExceptionHandler<RuntimeException>? {
        val result = handlers.filter { it.supports(e) }

        if (result.size > 1) {
            throw IllegalStateException("Há vários handlers suportando a exception ${e.javaClass.name}: $result")
        }

        return result.firstOrNull()
    }
}