package com.mikkaeru.pix.shared.handler

import com.mikkaeru.pix.shared.exception.ApiException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionHandlerResolver(@Inject private val handlers: Collection<ApiExceptionHandler<ApiException>>) {

    // TODO Se funcionar sem o defaultHandler, será necessário remove-lo
//    private var defaultHandler: ExceptionHandler<Exception> = DefaultExceptionHandler()
//
//    constructor(handlers: Collection<ApiExceptionHandler<ApiException>>, defaultHandler: ExceptionHandler<Exception>): this(handlers) {
//        this.defaultHandler = defaultHandler
//    }

    fun resolve(e: ApiException): ApiExceptionHandler<ApiException>? {
        val result = handlers.filter { it.supports(e) }
        if (result.size > 1)
            throw IllegalStateException("Há vários handlers suportando a exception ${e.javaClass.name}: $result")
        return result.firstOrNull()
    }
}