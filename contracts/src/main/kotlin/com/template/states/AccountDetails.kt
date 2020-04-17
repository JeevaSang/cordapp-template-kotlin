package com.template.states

import net.corda.core.contracts.Amount
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.util.*

@CordaSerializable
class AccountDetails(val amount: Amount<Currency>, val party: Party)