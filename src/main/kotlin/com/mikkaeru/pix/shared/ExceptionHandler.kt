package com.mikkaeru.pix.shared

import com.mikkaeru.pix.shared.handler.ExceptionHandlerResolver
import io.grpc.stub.StreamObserver
import io.micronaut.aop.Around
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*

@Around
@Retention(RUNTIME)
@Target(FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, CLASS)
annotation class ExceptionHandler()

@Singleton
@InterceptorBean(ExceptionHandler::class)
private class ErrorInterceptor(@Inject private val resolver: ExceptionHandlerResolver):
    MethodInterceptor<Any, Any> {

    private fun handle(e: RuntimeException, observer: StreamObserver<*>?) {
        val handler = resolver.resolve(e)
        val status = handler?.handle(e)
        observer?.onError(status?.asRuntimeException())
    }

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {
        return try {
            context.proceed()
        } catch (e: RuntimeException) {

            val observer = context
                .parameterValues
                .filterIsInstance<StreamObserver<*>>()
                .firstOrNull() as StreamObserver<*>

            handle(e, observer)

            null
        }
    }
}