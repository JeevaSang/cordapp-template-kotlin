
import com.fasterxml.jackson.core.JsonGenerationException
import java.io.IOException

import com.fasterxml.jackson.databind.JsonMappingException

import com.fasterxml.jackson.databind.JsonNode

import com.fasterxml.jackson.databind.ObjectMapper
import com.template.services.CurrencyService
import net.corda.core.contracts.Amount
import okhttp3.Request
import java.io.File
import okhttp3.OkHttpClient
import java.math.BigDecimal
import java.util.*

object mapper {
    private val mappers: ObjectMapper = ObjectMapper()

    @JvmStatic
    fun main(args: Array<String>) {
        try {
         // val data = getCurrent("USD","INR")
          //  println(data)
            val am = "0.01303".toBigDecimal();
            val inn = "2000".toLong();

            val cur =Currency.getInstance("USD")
            println(am.multiply(inn.toBigDecimal()))

            println("20".toDouble())

            println("15".toBigDecimal().multiply("76.24".toDouble().toBigDecimal()))

            val data = Amount (1,"26.06".toBigDecimal(),cur)
            //println(data.token.toString())
            //println(cur.currencyCode)
            println(data.displayTokenSize)

        } catch (e: JsonGenerationException) {
            e.printStackTrace()
        } catch (e: JsonMappingException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private val client = OkHttpClient()
    private val mapper = ObjectMapper()

    fun getCurrent(transferorCurrency: String,transfereeCurrency : String): Long {
        println("Sending request for $transferorCurrency to $transfereeCurrency")
        val response = client.newCall(request(transferorCurrency)).execute()
        return response.body?.let {
            println("Retrieved response for $transferorCurrency")
            val json = it.string()
            require(json != "Unknown symbol") { "Currency: $transferorCurrency does not exist" }
            val tree = mapper.readTree(json)
            return tree["conversion_rates"][transfereeCurrency].asLong()
        } ?: throw IllegalArgumentException("No response")
    }

    private fun request(symbol: String) =
            Request.Builder().url("https://prime.exchangerate-api.com/v5/415671f1946d04a5a56b1231/latest/$symbol").build()

}