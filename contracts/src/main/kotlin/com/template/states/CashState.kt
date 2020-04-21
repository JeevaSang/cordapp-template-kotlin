package com.template.states

import com.template.contracts.CashContract
import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.util.*

@BelongsToContract(CashContract::class)
data class CashState(val exRate: Double, val transferor: AccountDetails, val transferee: AccountDetails,
                     override val participants: List<AbstractParty> = listOf(transferor.party, transferee.party)
) : ContractState

@CordaSerializable
class AccountDetails(val amount: Amount<Currency>, val party: Party)



