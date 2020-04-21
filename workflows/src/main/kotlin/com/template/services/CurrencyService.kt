package com.template.services

import com.fasterxml.jackson.databind.ObjectMapper
import net.corda.core.flows.FlowException
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.utilities.loggerFor
import okhttp3.OkHttpClient
import okhttp3.Request
import java.math.BigDecimal

@CordaService
class CurrencyService(val serviceHub: AppServiceHub) :
        SingletonSerializeAsToken() {

    private val myKey = serviceHub.myInfo.legalIdentities.first().owningKey


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

    fun exchangedAmount(transferorCurrency: String, transfereeCurrency: String, transferorAmount: BigDecimal): ExChangeState =
            getCurrent(transferorCurrency, transfereeCurrency).let {
                log.info("Exchange rate price for $transferorCurrency - $it")
                val transfereeAmount = transferorAmount.multiply(it.toBigDecimal())
                log.info("Receive amount $transfereeAmount")
                ExChangeState(transferorCurrency, transferorAmount, transfereeCurrency, transfereeAmount, it)
            }

    private fun request(symbol: String) =
            Request.Builder().url("https://prime.exchangerate-api.com/v5/415671f1946d04a5a56b1231/latest/$symbol").build()


    private companion object {
        val log = loggerFor<CurrencyService>()
    }


    /* fun sign(ftx: SignedTransaction): TransactionSignature {
         //ftx.verify()
         val output = ftx.tx.outputsOfType<CashState>().single()
         var isValid = true
         serviceHub.cordaService(CurrencyService::class.java).getCurrent(output.transferor.amount.token.currencyCode, output.transferee.amount.token.currencyCode).let {
             isValid = output.exRate == it
         }
         if (isValid) {
             return serviceHub.createSignature(ftx, key)
         } else {
             throw InvalidExchangePriceFlowException("Oracle signature requested over Transaction: ${ftx.id} is invalid")
         }
     }

     private val key = serviceHub.myInfo.legalIdentities.first().owningKey      */

}

class InvalidExchangePriceFlowException(message: String?) : FlowException(message)


