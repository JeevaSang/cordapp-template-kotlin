package com.template.webserver

import com.template.flows.CashFlows.Initiator
import com.template.states.CashState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.*
import javax.servlet.http.HttpServletRequest

val SERVICE_NAMES = listOf("Notary", "Network Map Service")

@RestController
@RequestMapping("/api/transfer/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val myLegalName = rpc.proxy.nodeInfo().legalIdentities.first().name

    private val proxy = rpc.proxy

    /**
     * Returns the node's name.
     */
    @GetMapping(value = ["me"], produces = [APPLICATION_JSON_VALUE])
    fun whoami() = mapOf("me" to myLegalName)


    /**
     * Returns all parties registered with the network map service. These names can be used to look up identities using
     * the identity service.
     */
    @GetMapping(value = ["peers"], produces = [APPLICATION_JSON_VALUE])
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = proxy.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                //filter out myself, notary and eventual network map started by driver
                .filter { it.organisation !in (SERVICE_NAMES + myLegalName.organisation) })
    }


    /**
     * Displays all IOU states that exist in the node's vault.
     */
    @GetMapping(produces = [APPLICATION_JSON_VALUE])
    fun getIOUs(): ResponseEntity<List<StateAndRef<ContractState>>> {
        return ResponseEntity.ok(proxy.vaultQuery(CashState::class.java).states)
    }


    @PostMapping(value = ["amount"], produces = [TEXT_PLAIN_VALUE], headers = ["Content-Type=application/x-www-form-urlencoded"])
    fun transferAmount(request: HttpServletRequest): ResponseEntity<String> {
        val amount = request.getParameter("transferorAmount")

        val symbol = amount.substring(0, 3)
        val iouCurrency = Currency.getInstance(symbol)
                ?: return ResponseEntity.badRequest().body("Invalid $symbol currency code.\n")

        val iouAmount = amount.substring(3, amount.length).toBigDecimal()

        val transferCurrency = request.getParameter("transfereeCurrency")
        val partyName = request.getParameter("transferee")
                ?: return ResponseEntity.badRequest().body("Query parameter 'partyName' must not be null.\n")

        if (iouAmount <= BigDecimal.ZERO) {
            return ResponseEntity.badRequest().body("Query parameter 'iouAmount' must be non-negative.\n")
        }

        val currency = Currency.getInstance(transferCurrency)
                ?: return ResponseEntity.badRequest().body("Invalid $transferCurrency currency code.\n")

        val transfereeAmount = Amount(1, iouAmount, iouCurrency)

        val partyX500Name = CordaX500Name.parse(partyName)
        val otherParty = proxy.wellKnownPartyFromX500Name(partyX500Name)
                ?: return ResponseEntity.badRequest().body("Party named $partyName cannot be found.\n")

        return try {
            val signedTx = proxy.startTrackedFlow(::Initiator, transfereeAmount, currency, otherParty).returnValue.getOrThrow()
            val state = signedTx.coreTransaction.outputsOfType(CashState::class.java).first()
            logger.info(state.toString())
            ResponseEntity.status(CREATED).body("Transaction id ${signedTx.id} committed to ledger and data ${signedTx.coreTransaction.outputs.single()}.\n")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }
}