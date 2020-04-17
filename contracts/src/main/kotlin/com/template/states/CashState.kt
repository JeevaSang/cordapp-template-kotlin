package com.template.states

import com.template.contracts.CashContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty

@BelongsToContract(CashContract::class)
data class CashState(val exRate: Long, val transferor: AccountDetails, val transferee: AccountDetails,
                     override val participants: List<AbstractParty> = listOf(transferor.party, transferee.party)
) : ContractState



