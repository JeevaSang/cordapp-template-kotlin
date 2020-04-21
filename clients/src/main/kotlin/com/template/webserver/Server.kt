package com.template.webserver

import com.fasterxml.jackson.databind.ObjectMapper
import net.corda.client.jackson.JacksonSupport
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType.SERVLET
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean


/**
 * Our Spring Boot application.
 */
@SpringBootApplication
private open class Starter {
    @Bean
    open fun registerModule(): ObjectMapper {
        return JacksonSupport.createNonRpcMapper()
    }
    /* open fun objectMapper(): ObjectMapper? {
         val mapper = ObjectMapper()
         mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
         mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
         return mapper
     }*/
}

/**
 * Starts our Spring Boot application.
 */
fun main(args: Array<String>) {
    val app = SpringApplication(Starter::class.java)
    app.setBannerMode(Banner.Mode.OFF)
    app.webApplicationType = SERVLET
    app.run(*args)
}
