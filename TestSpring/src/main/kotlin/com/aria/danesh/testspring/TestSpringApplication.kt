package com.aria.danesh.testspring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TestSpringApplication

fun main(args: Array<String>) {
    runApplication<TestSpringApplication>(*args)
}
