package com.template.services

import com.fasterxml.jackson.databind.ObjectMapper
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.utilities.loggerFor
import okhttp3.OkHttpClient
import okhttp3.Request

@CordaService
class CurrencyService(serviceHub: AppServiceHub) :
        SingletonSerializeAsToken() {

    private val client = OkHttpClient()
    private val mapper = ObjectMapper()

    fun getCurrent(transferorCurrency: String, transfereeCurrency: String): Double {
        log.info("Sending request for $transferorCurrency to $transfereeCurrency")
        val response = client.newCall(request(transferorCurrency)).execute()
        return response.body?.let {
            log.info("Retrieved response for $transferorCurrency")
            val json = it.string()
            require(json != "Unknown symbol") { "Currency: $transferorCurrency does not exist" }
            val tree = mapper.readTree(json)
            return tree["conversion_rates"][transfereeCurrency].doubleValue()
        } ?: throw IllegalArgumentException("No response")
    }

    private fun request(symbol: String) =
            Request.Builder().url("https://prime.exchangerate-api.com/v5/415671f1946d04a5a56b1231/latest/$symbol").build()


    private companion object {
        val log = loggerFor<CurrencyService>()
    }

}