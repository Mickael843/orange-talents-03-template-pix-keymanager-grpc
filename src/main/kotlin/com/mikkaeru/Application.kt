package com.mikkaeru

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("com.mikkaeru")
		.start()
}

